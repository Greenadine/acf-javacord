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
import co.aikar.commands.javacord.exception.JavacordInvalidCommandArgument;
import co.aikar.commands.javacord.util.StringUtils;
import org.javacord.api.entity.channel.*;
import org.javacord.api.entity.user.User;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class SlashCommandContexts extends JavacordCommandContexts<SlashCommandEvent, SlashCommandExecutionContext> {

    SlashCommandContexts(@NotNull AbstractJavacordCommandManager manager) {
        super(manager);

        /* Override ACF core's default resolvers to better fit Discord (& slash commands) */

        // Primitives
        registerOptionalContext(Long.class, Long.TYPE, c -> resolveNumber(c, Long.MIN_VALUE, Long.MAX_VALUE).map(Number::longValue).orElse(null));
        registerOptionalContext(Integer.class, Integer.TYPE, c -> resolveNumber(c, Integer.MIN_VALUE, Integer.MAX_VALUE).map(Number::intValue).orElse(null));
        registerOptionalContext(Short.class, Short.TYPE, c -> resolveNumber(c, Short.MIN_VALUE, Short.MAX_VALUE).map(Number::shortValue).orElse(null));
        registerOptionalContext(Byte.class, Byte.TYPE, c -> resolveNumber(c, Byte.MIN_VALUE, Byte.MAX_VALUE).map(Number::byteValue).orElse(null));
        registerOptionalContext(Double.class, Double.TYPE, c -> resolveDecimalNumber(c, Double.MIN_VALUE, Double.MAX_VALUE).map(Number::doubleValue).orElse(null));
        registerOptionalContext(Float.class, Float.TYPE, c -> resolveDecimalNumber(c, Float.MIN_VALUE, Float.MAX_VALUE).map(Number::floatValue).orElse(null));
        registerOptionalContext(Boolean.class, Boolean.TYPE, c -> {
            if (!c.isNextBoolean()) {
                if (c.isOptional()) {
                    return false;
                }
            }
            return c.popNextBoolean();
        });
        registerOptionalContext(Character.class, Character.TYPE, c -> {
            if (!c.isNextString()) {
                if (c.isOptional()) {
                    return null;
                }
                throw new JavacordInvalidCommandArgument("Please provide a character."); // TODO message key
            }
            String arg = c.popNextString();
            if (arg.length() > 1) {
                throw new JavacordInvalidCommandArgument(MessageKeys.MUST_BE_MAX_LENGTH, "{max}", String.valueOf(1));
            } else {
                return arg.charAt(0);
            }
        });

        // Numbers
        registerContext(BigInteger.class, BigDecimal.class, this::resolveBigNumber);

        // Strings
        registerOptionalContext(String.class, c -> {
            if (!c.isNextString()) {
                if (c.isOptional()) {
                    return null;
                }
                throw new JavacordInvalidCommandArgument("Please provide a string."); // TODO message key
            }
            return c.popNextString();
        });
        registerOptionalContext(String[].class, c -> {
            if (!c.isNextString()) {
                if (c.isOptional()) {
                    return null;
                }
                throw new JavacordInvalidCommandArgument("Please provide a string."); // TODO message key
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

        /* Javacord-specific resolvers */
        registerIssuerOnlyContext(SlashCommandEvent.class, CommandExecutionContext::getIssuer);
        registerIssuerAwareContext(User.class, c -> {
            if (c.hasAnnotation(BotUser.class)) {
                return api.getYourself();
            }
            if (!c.hasAnnotation(Issuer.class)) {
                return c.issuer.getUser();
            }
            if (!c.isNextUser()) {
                throw new JavacordInvalidCommandArgument("Please provide a user."); // TODO message key
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
            if (!c.hasAnnotation(Issuer.class)) {
                return c.issuer.getMember();
            }
            if (!c.isNextUser()) {
                throw new JavacordInvalidCommandArgument("Please provide a user."); // TODO message key
            }
            return new Member(c.popNextUser(), c.issuer.getServer().get());
        });
        registerIssuerAwareContext(ChannelType.class, c -> {
            if (!c.hasAnnotation(Issuer.class)) {
                return c.issuer.getChannel().getType();
            }
            if (!c.isNextString()) {
                throw new JavacordInvalidCommandArgument("Please provide a channel type."); // TODO message key
            }
            String arg = c.popNextString();
            ChannelType type;
            try {
                type = ChannelType.valueOf(arg.toUpperCase().replaceAll(" ", "_"));
            } catch (IllegalArgumentException ex) {
                throw new JavacordInvalidCommandArgument("'%s' is not a valid channel type.", arg); // TODO message key
            }
            return type;
        });
        registerIssuerAwareContext(ServerChannel.class, c -> {
            if (!c.isInServer()) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.SERVER_ONLY);
            }
            if (!c.hasAnnotation(Issuer.class)) {
                return c.issuer.getChannel().asServerChannel().get();
            }
            if (!c.isNextChannel()) {
                if (c.isOptional()) {
                    return null;
                }
                throw new JavacordInvalidCommandArgument("Please provide a channel."); // TODO message key
            }
            return c.popNextChannel();
        });
        registerIssuerAwareContext(ServerTextChannel.class, c -> {
            if (!c.isInServer()) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.SERVER_ONLY);
            }
            if (!c.hasAnnotation(Issuer.class)) {
                return c.issuer.getChannel().asServerTextChannel().get();
            }
            if (!c.isNextChannel()) {
                if (c.isOptional()) {
                    return null;
                }
                throw new JavacordInvalidCommandArgument("Please provide a channel."); // TODO message key
            }
            ServerChannel channel = c.getNextChannel();
            if (!channel.asServerTextChannel().isPresent()) {
                throw new JavacordInvalidCommandArgument("Channel '%s' is not a text channel.", channel.getName()); // TODO message key
            }
            return channel.asServerTextChannel().get();
        });
        registerIssuerAwareContext(ServerVoiceChannel.class, c -> {
            if (!c.isInServer()) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.SERVER_ONLY);
            }
            if (!c.hasAnnotation(Issuer.class)) {
                // Get the voice channel the issuer connected to within the context's server
                return c.issuer.getUser().getConnectedVoiceChannel(c.issuer.getServer().get()).get();
            }
            if (!c.isNextChannel()) {
                if (c.isOptional()) {
                    return null;
                }
                throw new JavacordInvalidCommandArgument("Please provide a channel."); // TODO message key
            }
            ServerChannel channel = c.getNextChannel();
            if (!channel.asServerVoiceChannel().isPresent()) {
                throw new JavacordInvalidCommandArgument("Channel '%s' is not a voice channel.", channel.getName()); // TODO message key
            }
            return channel.asServerVoiceChannel().get();
        });
        registerIssuerAwareContext(ServerForumChannel.class, c -> {
            if (!c.isInServer()) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.SERVER_ONLY);
            }
            if (!c.isNextChannel()) {
                if (c.isOptional()) {
                    return null;
                }
                throw new JavacordInvalidCommandArgument("Please provide a channel."); // TODO message key
            }
            ServerChannel channel = c.getNextChannel();
            if (!channel.asServerForumChannel().isPresent()) {
                throw new JavacordInvalidCommandArgument("Channel '%s' is not a forum channel.", channel.getName()); // TODO message key
            }
            return channel.asServerForumChannel().get();
        });
        registerIssuerAwareContext(ServerThreadChannel.class, c -> {
            if (!c.isInServer()) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.SERVER_ONLY);
            }
            if (!c.isNextChannel()) {
                if (c.isOptional()) {
                    return null;
                }
                throw new JavacordInvalidCommandArgument("Please provide a channel."); // TODO message key
            }
            ServerChannel channel = c.getNextChannel();
            if (!channel.asServerThreadChannel().isPresent()) {
                throw new JavacordInvalidCommandArgument("Channel '%s' is not a thread channel.", channel.getName()); // TODO message key
            }
            return channel.asServerThreadChannel().get();
        });
        registerIssuerAwareContext(ServerStageVoiceChannel.class, c -> {
            if (!c.isInServer()) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.SERVER_ONLY);
            }
            if (!c.isNextChannel()) {
                if (c.isOptional()) {
                    return null;
                }
                throw new JavacordInvalidCommandArgument("Please provide a channel."); // TODO message key
            }
            ServerChannel channel = c.getNextChannel();
            if (!channel.asServerStageVoiceChannel().isPresent()) {
                throw new JavacordInvalidCommandArgument("Channel %s is not a stage voice channel."); // TODO message key
            }
            return channel.asServerStageVoiceChannel().get();
        });
        registerContext(ChannelCategory.class, c -> {
            // TODO: implement
            return null;
        });
    }

    private Optional<Number> resolveNumber(@NotNull SlashCommandExecutionContext c, @NotNull Number minValue, @NotNull Number maxValue) {
        if (!c.isNextLong()) {
            if (c.isOptional()) {
                return Optional.empty();
            }
            throw new JavacordInvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", c.getNextArg().getName());
        }
        Number number = c.popNextLong();
        System.out.println("Number: " + number);
        validateMinMax(c, number, minValue, maxValue);
        return Optional.of(number);
    }

    private Optional<Number> resolveDecimalNumber(@NotNull SlashCommandExecutionContext c, @NotNull Number minValue, @NotNull Number maxValue) {
        if (!c.isNextDecimal()) {
            if (c.isOptional()) {
                return Optional.empty();
            }
            throw new JavacordInvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", c.getNextArg().getName());
        }
        Number number = c.popNextDecimal();
        System.out.println("Decimal number: " + number);
        validateMinMax(c, number, minValue, maxValue);
        return Optional.of(number);
    }

    @SuppressWarnings("unchecked")
    private <T extends Number> T resolveBigNumber(@NotNull SlashCommandExecutionContext c) {
        if (!c.isNextString()) {
            if (c.isOptional()) {
                return null;
            }
            throw new JavacordInvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", c.getNextArg().getName());
        }
        String arg = c.popNextString();
        try {
            T number = (T) ACFUtil.parseBigNumber(arg, c.hasFlag("suffixes"));
            this.validateMinMax(c, number);
            return number;
        } catch (NumberFormatException e) {
            throw new JavacordInvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", arg);
        }
    }
}
