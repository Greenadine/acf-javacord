/*
 * Copyright (c) 2023 Kevin Zuman (Greenadine)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package co.aikar.commands;

import co.aikar.commands.annotation.Split;
import co.aikar.commands.javacord.annotation.BotUser;
import co.aikar.commands.javacord.annotation.Issuer;
import co.aikar.commands.javacord.context.Member;
import co.aikar.commands.javacord.context.UnicodeEmoji;
import co.aikar.commands.javacord.exception.JavacordInvalidCommandArgument;
import co.aikar.commands.javacord.util.StringUtils;
import org.javacord.api.entity.channel.*;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.emoji.KnownCustomEmoji;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.util.DiscordRegexPattern;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class SlashCommandContexts extends JavacordCommandContexts<SlashCommandEvent, SlashCommandExecutionContext> {

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    SlashCommandContexts(@NotNull SlashCommandManager manager) {
        super(manager);

        // Override ACF core's default resolvers to fit slash commands
        //region Resolvers
        //region Primitives & numbers
        registerOptionalContext(Long.class, Long.TYPE, c -> resolveNumber(c, Long.MIN_VALUE, Long.MAX_VALUE).map(Number::longValue).orElse(null));
        registerOptionalContext(Integer.class, Integer.TYPE, c -> resolveNumber(c, Integer.MIN_VALUE, Integer.MAX_VALUE).map(Number::intValue).orElse(null));
        registerOptionalContext(Short.class, Short.TYPE, c -> resolveNumber(c, Short.MIN_VALUE, Short.MAX_VALUE).map(Number::shortValue).orElse(null));
        registerOptionalContext(Byte.class, Byte.TYPE, c -> resolveNumber(c, Byte.MIN_VALUE, Byte.MAX_VALUE).map(Number::byteValue).orElse(null));
        registerOptionalContext(Double.class, Double.TYPE, c -> resolveDecimalNumber(c, Double.MIN_VALUE, Double.MAX_VALUE).map(Number::doubleValue).orElse(null));
        registerOptionalContext(Float.class, Float.TYPE, c -> resolveDecimalNumber(c, Float.MIN_VALUE, Float.MAX_VALUE).map(Number::floatValue).orElse(null));
        registerOptionalContext(Number.class, c -> resolveNumber(c, Double.MIN_VALUE, Double.MAX_VALUE).orElse(null));
        registerOptionalContext(BigInteger.class, this::resolveBigNumber);
        registerOptionalContext(BigDecimal.class, this::resolveBigNumber);
        registerOptionalContext(Boolean.class, Boolean.TYPE, c -> {
            if (!c.isNextBoolean() && c.isOptional()) {
                return false;
            }
            return c.popNextBoolean();
        });
        registerOptionalContext(Character.class, Character.TYPE, c -> {
            if (!c.isNextString() && c.isOptional()) {
                return null;
            }
            String arg = c.popNextString();
            if (arg.length() > 1) {
                if (c.isOptional()) {
                    return null;
                }
                throw new JavacordInvalidCommandArgument(MessageKeys.MUST_BE_MAX_LENGTH, "{max}", String.valueOf(1));
            } else {
                return arg.charAt(0);
            }
        });
        //endregion

        //region Strings & enums
        registerOptionalContext(String.class, c -> {
            if (!c.isNextString() && c.isOptional()) {
                return null;
            }
            return c.popNextString();
        });
        registerOptionalContext(String[].class, c -> {
            if (!c.isNextString() && c.isOptional()) {
                return null;
            }
            String arg = c.popNextString();
            String split = c.getAnnotationValue(Split.class, Annotations.NOTHING | Annotations.NO_EMPTY);
            if (split != null) {
                if (arg.isEmpty()) {
                    throw new JavacordInvalidCommandArgument(); // TODO message key?
                }
                return ACFPatterns.getPattern(split).split(arg);
            }
            String[] result = StringUtils.splitOnWhitespace(arg);
            c.getArguments().clear();
            return result;
        });
        registerOptionalContext(Enum.class, c -> {
            if (!c.isNextString() && c.isOptional()) {
                return null;
            }
            String arg = c.popNextString();
            //noinspection unchecked
            Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) c.getCommandParameter().getType();
            Enum<?> match = ACFUtil.simpleMatch(enumClass, arg);
            if (match == null) {
                List<String> names = ACFUtil.enumNames(enumClass);
                throw new JavacordInvalidCommandArgument(MessageKeys.PLEASE_SPECIFY_ONE_OF, "{valid}", ACFUtil.join(names, ", "));
            }
            return match;
        });
        //endregion
        //endregion

        // Javacord-specific resolvers
        //region Resolvers
        registerIssuerOnlyContext(SlashCommandEvent.class, CommandExecutionContext::getIssuer);

        //region Users, members & roles
        registerIssuerAwareContext(User.class, c -> {
            if (c.hasAnnotation(BotUser.class)) {
                return api.getYourself();
            }
            if (c.hasAnnotation(Issuer.class)) {
                return c.issuer.getUser();
            }
            if (!c.isNextUser() && c.isOptional()) {
                return null;
            }
            return c.popNextUser();
        });
        registerIssuerAwareContext(Member.class, c -> {
            if (!c.isInServer()) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.SERVER_ONLY);
            }
            if (c.hasAnnotation(BotUser.class)) {
                return new Member(api.getYourself(), c.issuer.getServer().get());
            }
            if (c.hasAnnotation(Issuer.class)) {
                return c.issuer.getMember();
            }
            if (!c.isNextUser() && c.isOptional()) {
                return null;
            }
            return new Member(c.popNextUser(), c.issuer.getServer().get());
        });
        registerOptionalContext(Role.class, c -> {
            if (!c.isNextRole() && c.isOptional()) {
                return null;
            }
            return c.popNextRole();
        });
        //endregion

        //region Channels
        registerIssuerAwareContext(ChannelType.class, c -> {
            if (c.hasAnnotation(Issuer.class)) {
                return c.issuer.getChannel().getType();
            }
            if (!c.isNextString() && c.isOptional()) {
                return null;
            }
            String arg = c.getNextString(); // Test input before consuming
            ChannelType type;
            try {
                type = ChannelType.valueOf(arg.toUpperCase().replaceAll(" ", "_"));
            } catch (IllegalArgumentException ex) {
                if (c.isOptional()) {
                    return null;
                }
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.INVALID_CHANNEL_TYPE, arg);
            }
            c.popNextArg(); // Consume input
            return type;
        });
        registerIssuerAwareContext(ServerChannel.class, c -> {
            if (!c.isInServer()) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.SERVER_ONLY);
            }
            if (c.hasAnnotation(Issuer.class)) {
                return c.issuer.getChannel().asServerChannel().get();
            }
            if (!c.isNextChannel() && c.isOptional()) {
                return null;
            }
            return c.popNextChannel();
        });
        registerIssuerAwareContext(ServerTextChannel.class, c -> {
            if (!c.isInServer()) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.SERVER_ONLY);
            }
            if (c.hasAnnotation(Issuer.class)) {
                return c.issuer.getChannel().asServerTextChannel().get();
            }
            if (!c.isNextChannel() && c.isOptional()) {
                return null;
            }
            return c.getNextChannel().asServerTextChannel().get();
        });
        registerIssuerAwareContext(ServerVoiceChannel.class, c -> {
            if (!c.isInServer()) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.SERVER_ONLY);
            }
            if (c.hasAnnotation(Issuer.class)) {
                // Get the voice channel the issuer connected to within the context's server
                return c.issuer.getUser().getConnectedVoiceChannel(c.issuer.getServer().get())
                        .orElseThrow(() -> new JavacordInvalidCommandArgument(JavacordMessageKeys.USER_NOT_IN_VOICE_CHANNEL));
            }
            if (!c.isNextChannel() && c.isOptional()) {
                return null;
            }
            return c.getNextChannel().asServerVoiceChannel().get();
        });
        registerIssuerAwareContext(ServerForumChannel.class, c -> {
            if (!c.isInServer()) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.SERVER_ONLY);
            }
            if (!c.isNextChannel() && c.isOptional()) {
                return null;
            }
            return c.getNextChannel().asServerForumChannel().get();
        });
        registerIssuerAwareContext(ServerThreadChannel.class, c -> {
            if (!c.isInServer()) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.SERVER_ONLY);
            }
            if (!c.isNextChannel() && c.isOptional()) {
                return null;
            }
            return c.getNextChannel().asServerThreadChannel().get();
        });
        registerIssuerAwareContext(ServerStageVoiceChannel.class, c -> {
            if (!c.isInServer()) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.SERVER_ONLY);
            }
            if (!c.isNextChannel() && c.isOptional()) {
                return null;
            }
            return c.getNextChannel().asServerStageVoiceChannel().get();
        });
        registerContext(ChannelCategory.class, c -> {
            if (!c.isInServer()) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.SERVER_ONLY);
            }
            if (!c.isNextChannel() && c.isOptional()) {
                return null;
            }
            return c.getNextChannel().asChannelCategory().get();
        });
        //endregion

        //region Emojis
        registerOptionalContext(Emoji.class, c -> {
            if (!c.isNextString() && c.isOptional()) {
                return null;
            }
            String arg = c.getNextString(); // Test input before consuming
            Emoji emoji = null;
            if (DiscordRegexPattern.CUSTOM_EMOJI.matcher(arg).matches()) {
                String id = arg.replaceAll("[^0-9]", ""); // Extract non-negative integers to retrieve ID
                emoji = api.getCustomEmojiById(id).orElse(null);
            } else {
                if (UnicodeEmoji.isUnicodeEmoji(arg)) {
                    c.popNextArg(); // Consume input
                    emoji = UnicodeEmoji.from(arg);
                }
            }
            if (emoji == null && !c.isOptional()) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.COULD_NOT_FIND_EMOJI);
            }
            return emoji;
        });
        registerOptionalContext(UnicodeEmoji.class, c -> {
            if (!c.isNextString() && c.isOptional()) {
                return null;
            }
            String arg = c.getNextString(); // Test input before consuming
            if (!UnicodeEmoji.isUnicodeEmoji(arg) && !c.isOptional()) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.COULD_NOT_FIND_UNICODE_EMOJI);
            }
            c.popNextArg(); // Consume input
            return UnicodeEmoji.from(arg);
        });
        registerOptionalContext(KnownCustomEmoji.class, c -> {
            if (!c.isNextString() && c.isOptional()) {
                return null;
            }
            String arg = c.getNextString(); // Test input before consuming
            KnownCustomEmoji emoji = null;
            if (DiscordRegexPattern.CUSTOM_EMOJI.matcher(arg).matches()) {
                String id = arg.replaceAll("[^0-9]", ""); // Extract non-negative integers to retrieve ID
                emoji = api.getCustomEmojiById(id).orElse(null);
            } else {
                Collection<KnownCustomEmoji> emojis = api.getCustomEmojisByName(arg);
                if (emojis.size() > 1) {
                    throw new JavacordInvalidCommandArgument(JavacordMessageKeys.TOO_MANY_EMOJIS_WITH_NAME);
                }
                if (!emojis.isEmpty()) {
                    emoji = ACFUtil.getFirstElement(emojis);
                }
            }
            if (emoji == null) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.COULD_NOT_FIND_EMOJI);
            }
            return emoji;
        });
        //endregion
        //endregion
    }

    private Optional<Number> resolveNumber(@NotNull SlashCommandExecutionContext c, @NotNull Number minValue, @NotNull Number maxValue) {
        if (!c.isNextLong() && c.isOptional()) {
            return Optional.empty();
        }
        Number number = c.popNextLong();
        validateMinMax(c, number, minValue, maxValue);
        return Optional.of(number);
    }

    private Optional<Number> resolveDecimalNumber(@NotNull SlashCommandExecutionContext c, @NotNull Number minValue, @NotNull Number maxValue) {
        if (!c.isNextDecimal() && c.isOptional()) {
            return Optional.empty();
        }
        Number number = c.popNextDecimal();
        validateMinMax(c, number, minValue, maxValue);
        return Optional.of(number);
    }

    private <T extends Number> T resolveBigNumber(@NotNull SlashCommandExecutionContext c) {
        if (!c.isNextString()) {
            if (c.isOptional()) {
                return null;
            }
            throw new JavacordInvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", c.getNextArg().getName());
        }
        String arg = c.popNextString();
        try {
            //noinspection unchecked
            T number = (T) ACFUtil.parseBigNumber(arg, c.hasFlag("suffixes"));
            this.validateMinMax(c, number);
            return number;
        } catch (NumberFormatException e) {
            throw new JavacordInvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", arg);
        }
    }
}
