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

import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Split;
import co.aikar.commands.annotation.Values;
import co.aikar.commands.javacord.annotation.BotUser;
import co.aikar.commands.javacord.annotation.CrossServer;
import co.aikar.commands.javacord.context.Member;
import co.aikar.commands.javacord.context.UnicodeEmoji;
import co.aikar.commands.javacord.exception.JavacordInvalidCommandArgument;
import com.google.common.collect.Iterables;
import com.vdurmont.emoji.EmojiManager;
import org.javacord.api.entity.channel.*;
import org.javacord.api.entity.emoji.CustomEmoji;
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

/**
 * @since 0.5.0
 * @see JavacordCommandContexts
 */
@SuppressWarnings("unchecked")
public class MessageCommandContexts extends JavacordCommandContexts<MessageCommandEvent, MessageCommandExecutionContext> {

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    MessageCommandContexts(@NotNull MessageCommandManager manager) {
        super(manager);

        /* Override ACF core's default resolvers to better fit Discord */

        // Primitives
        registerContext(Long.class, Long.TYPE, c -> resolveNumber(c, Long.MIN_VALUE, Long.MAX_VALUE).longValue());
        registerContext(Integer.class, Integer.TYPE, c -> resolveNumber(c, Integer.MIN_VALUE, Integer.MAX_VALUE).intValue());
        registerContext(Short.class, Short.TYPE, c -> resolveNumber(c, Short.MIN_VALUE, Short.MAX_VALUE).shortValue());
        registerContext(Byte.class, Byte.TYPE, c -> resolveNumber(c, Byte.MIN_VALUE, Byte.MAX_VALUE).byteValue());
        registerContext(Double.class, Double.TYPE, c -> resolveNumber(c, Double.MIN_VALUE, Double.MAX_VALUE).doubleValue());
        registerContext(Float.class, Float.TYPE, c -> resolveNumber(c, Float.MIN_VALUE, Float.MAX_VALUE).floatValue());
        registerContext(Boolean.class, Boolean.TYPE, c -> ACFUtil.isTruthy(c.popFirstArg()));
        registerContext(Character.class, Character.TYPE, c -> {
            String arg = c.popFirstArg();
            if (arg.length() > 1) {
                throw new JavacordInvalidCommandArgument(MessageKeys.MUST_BE_MAX_LENGTH, "{max}", String.valueOf(1));
            } else {
                return arg.charAt(0);
            }
        });

        // Numbers
        registerContext(Number.class, c -> resolveNumber(c, Double.MIN_VALUE, Double.MAX_VALUE));
        registerContext(BigDecimal.class, this::resolveBigNumber);
        registerContext(BigInteger.class, this::resolveBigNumber);

        // Strings
        registerContext(String.class, c -> {
            if (c.hasAnnotation(Values.class)) {
                return c.popFirstArg();
            }

            String val = c.isLastArg() && !c.hasAnnotation(Single.class) ? ACFUtil.join(c.getArgs()) : c.popFirstArg();
            Integer minLen = c.getFlagValue("minlen", (Integer) null);
            Integer maxLen = c.getFlagValue("maxlen", (Integer) null);
            if (minLen != null && val.length() < minLen) {
                throw new JavacordInvalidCommandArgument(MessageKeys.MUST_BE_MIN_LENGTH, "{min}", String.valueOf(minLen));
            } else if (maxLen != null && val.length() > maxLen) {
                throw new JavacordInvalidCommandArgument(MessageKeys.MUST_BE_MAX_LENGTH, "{max}", String.valueOf(minLen));
            } else {
                return val;
            }
        });
        registerContext(String[].class, c -> {
            List<String> args = c.getArgs();
            String val;
            if (c.isLastArg() && !c.hasAnnotation(Single.class)) {
                val = ACFUtil.join(args);
            } else {
                val = c.popFirstArg();
            }

            String split = c.getAnnotationValue(Split.class, 8);
            if (split != null) {
                if (val.isEmpty()) {
                    throw new JavacordInvalidCommandArgument();
                } else {
                    return ACFPatterns.getPattern(split).split(val);
                }
            } else {
                if (!c.isLastArg()) {
                    ACFUtil.sneaky(new IllegalStateException("Weird Command signature... String[] should be last or @Split"));
                }

                String[] result = args.toArray(new String[0]);
                args.clear();
                return result;
            }
        });

        // Enums
        registerContext(Enum.class, (c) -> {
            String first = c.popFirstArg();
            //noinspection deprecation
            Class<? extends Enum<?>> enumCls = (Class<? extends Enum<?>>) c.getParam().getType();
            Enum<?> match = ACFUtil.simpleMatch(enumCls, first);
            if (match == null) {
                List<String> names = ACFUtil.enumNames(enumCls);
                throw new JavacordInvalidCommandArgument(MessageKeys.PLEASE_SPECIFY_ONE_OF, "{valid}", ACFUtil.join(names, ", "));
            } else {
                return match;
            }
        });


        /* Javacord-specific resolvers */
        registerIssuerOnlyContext(MessageCommandEvent.class, c -> c.issuer);
        registerIssuerAwareContext(User.class, c -> {
            if (c.hasAnnotation(BotUser.class)) {
                return api.getYourself();
            }
            if (!c.hasFlag("other")) {
                return c.issuer.getUser();
            }

            boolean isOptional = c.isOptional();
            String arg = c.isLastArg() ? String.join(" ", c.getArgs()) : c.getFirstArg(); // Test input

            if (!isOptional && (arg == null || arg.isEmpty())) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.PLEASE_SPECIFY_USER);
            }

            User user = null;
            if (!(arg == null || arg.isEmpty())) {
                if (DiscordRegexPattern.USER_MENTION.matcher(arg).matches()) {
                    String id = arg.replaceAll("[^0-9]", ""); // Extract non-negative integers from string
                    user = api.getUserById(id).join();

                    if (c.hasFlag("humanonly") && user.isBot()) {
                        throw new JavacordInvalidCommandArgument(JavacordMessageKeys.USER_IS_BOT);
                    }

                    c.popFirstArg(); // Consume input
                } else {
                    Collection<User> users = api.getCachedUsersByNameIgnoreCase(arg);
                    if (users.size() > 1 && !c.isOptional()) {
                        throw new JavacordInvalidCommandArgument(JavacordMessageKeys.TOO_MANY_USERS_WITH_NAME);
                    } else if (!users.isEmpty()) {
                        user = Iterables.get(users, 0);
                        c.popFirstArg(); // Consume input
                    }
                }
            }

            if (user == null && !isOptional) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.COULD_NOT_FIND_USER);
            }
            return user;
        });
        registerIssuerAwareContext(Member.class, c -> {
            if (!c.issuer.isInServer()) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.SERVER_ONLY);
            }
            if (c.hasAnnotation(BotUser.class)) {
                return new Member(api.getYourself(), c.issuer.getServer().get());
            }
            if (!c.hasFlag("other")) {
                return c.issuer.getMember();
            }

            boolean isOptional = c.isOptional();
            String arg = c.isLastArg() ? String.join(" ", c.getArgs()) : c.getFirstArg(); // Test input

            if (!isOptional && (arg == null || arg.isEmpty())) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.PLEASE_SPECIFY_USER);
            }

            User user = null;
            if (!(arg == null || arg.isEmpty())) {
                if (DiscordRegexPattern.USER_MENTION.matcher(arg).matches()) {
                    String id = arg.replaceAll("[^0-9]", ""); // Extract non-negative integers from string
                    user = api.getUserById(id).join();
                    c.popFirstArg(); // Consume input
                } else {
                    Collection<User> users = api.getCachedUsersByNameIgnoreCase(arg);
                    if (users.size() > 1 && !c.isOptional()) {
                        throw new JavacordInvalidCommandArgument(JavacordMessageKeys.TOO_MANY_USERS_WITH_NAME);
                    } else if (!users.isEmpty()) {
                        user = Iterables.get(users, 0);
                        c.popFirstArg(); // Consume input
                    }
                }
            }

            if (user == null && !isOptional) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.COULD_NOT_FIND_USER);
            }
            if (user != null && !c.issuer.getServer().get().isMember(user)) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.USER_NOT_MEMBER_OF_SERVER);
            }
            if (user != null && c.hasFlag("humanonly") && user.isBot()) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.USER_IS_BOT);
            }

            return user != null ? new Member(user, c.issuer.getServer().get()) : null;
        });
        registerIssuerAwareContext(Channel.class, c -> {
            if (!c.hasFlag("other")) {
                return c.issuer.getChannel();
            }

            boolean isCrossServer = c.hasAnnotation(CrossServer.class);
            String arg = c.getFirstArg(); // Test input
            Channel channel = null;
            if (DiscordRegexPattern.CHANNEL_MENTION.matcher(arg).matches()) {
                String id = arg.replaceAll("[^0-9]", ""); // Extract non-negative integers to retrieve ID
                channel = isCrossServer || !c.issuer.getServer().isPresent()
                        ? api.getChannelById(id).isPresent()
                        ? api.getChannelById(id).get() : null
                        : c.issuer.getServer().get().getChannelById(id).isPresent()
                        ? c.issuer.getServer().get().getChannelById(id).get() : null;
            }

            if (channel != null) {
                c.popFirstArg(); // Consume input
            } else {
                if (!c.hasFlag("require")) {
                    throw new JavacordInvalidCommandArgument(JavacordMessageKeys.COULD_NOT_FIND_CHANNEL);
                }
                channel = c.issuer.getChannel();
            }
            return channel;
        });
        registerIssuerOnlyContext(PrivateChannel.class, c -> {
            if (!c.issuer.isInPrivate()) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.PRIVATE_ONLY);
            }
            return (PrivateChannel) c.issuer.getChannel();
        });
        registerIssuerAwareContext(TextChannel.class, c -> {
            if (!c.hasFlag("other")) {
                return c.issuer.getChannel();
            }

            boolean isCrossServer = c.hasAnnotation(CrossServer.class);
            String arg = c.getFirstArg(); // Test input
            TextChannel channel = null;
            if (DiscordRegexPattern.CHANNEL_MENTION.matcher(arg).matches()) {
                String id = arg.replaceAll("[^0-9]", ""); // Extract non-negative integers to retrieve ID
                channel = isCrossServer || !c.issuer.getServer().isPresent()
                        ? api.getTextChannelById(id).isPresent()
                        ? api.getTextChannelById(id).get() : null
                        : c.issuer.getServer().get().getTextChannelById(id).isPresent()
                        ? c.issuer.getServer().get().getTextChannelById(id).get() : null;
            }

            if (channel != null) {
                c.popFirstArg(); // Consume input
            } else {
                if (!c.hasFlag("require")) {
                    throw new JavacordInvalidCommandArgument(JavacordMessageKeys.COULD_NOT_FIND_CHANNEL);
                }
                channel = c.issuer.getChannel();
            }
            return channel;
        });
        registerIssuerAwareContext(ServerTextChannel.class, c -> {
            if (!c.issuer.isInServer()) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.SERVER_ONLY);
            }
            if (!c.hasFlag("other")) {
                return c.issuer.getChannel().asServerTextChannel().get();
            }

            boolean isCrossServer = c.hasAnnotation(CrossServer.class);
            String arg = c.getFirstArg(); // Test input
            ServerTextChannel channel = null;
            if (DiscordRegexPattern.CHANNEL_MENTION.matcher(arg).matches()) {
                String id = arg.replaceAll("[^0-9]", ""); // Extract non-negative integers to retrieve ID
                channel = isCrossServer || !c.issuer.getServer().isPresent()
                        ? api.getServerTextChannelById(id).isPresent()
                        ? api.getServerTextChannelById(id).get() : null
                        : c.issuer.getServer().get().getTextChannelById(id).isPresent()
                        ? c.issuer.getServer().get().getTextChannelById(id).get() : null;
            }

            if (channel != null) {
                c.popFirstArg(); // Consume input
            } else {
                if (!c.hasFlag("require")) {
                    throw new JavacordInvalidCommandArgument(JavacordMessageKeys.COULD_NOT_FIND_CHANNEL);
                }
                channel = c.issuer.getChannel().asServerTextChannel().get();
            }
            return channel;
        });
        registerIssuerAwareContext(VoiceChannel.class, c -> {
            if (!c.hasFlag("other")) {
                if (c.issuer.isInServer() && c.issuer.getMember().isInVoiceChannel()) {
                    return c.issuer.getMember().getConnectedVoiceChannel().get(); // No need for 'isPresent()' due to 'Member#isInVoiceChannel()'
                } else {
                    throw new JavacordInvalidCommandArgument(JavacordMessageKeys.USER_NOT_IN_VOICE_CHANNEL);
                }
            }
            boolean isCrossServer = c.hasAnnotation(CrossServer.class);
            String arg = c.getFirstArg(); // Test input
            VoiceChannel channel = null;
            if (DiscordRegexPattern.CHANNEL_MENTION.matcher(arg).matches()) {
                String id = arg.replaceAll("[^0-9]", ""); // Extract non-negative integers to retrieve ID
                channel = isCrossServer || !c.issuer.getServer().isPresent()
                        ? api.getVoiceChannelById(id).isPresent()
                        ? api.getVoiceChannelById(id).get() : null
                        : c.issuer.getServer().get().getVoiceChannelById(id).isPresent()
                        ? c.issuer.getServer().get().getVoiceChannelById(id).get() : null;
            }

            if (channel != null) {
                c.popFirstArg();
            } else {
                if (!c.hasFlag("require")) {
                    throw new JavacordInvalidCommandArgument(JavacordMessageKeys.COULD_NOT_FIND_CHANNEL);
                }
            }
            return channel;
        });
        registerIssuerAwareContext(ServerVoiceChannel.class, c -> {
            if (!c.hasFlag("other")) {
                if (c.issuer.isInServer() && c.issuer.getMember().isInVoiceChannel()) {
                    return c.issuer.getMember().getConnectedVoiceChannel().get(); // No need for 'isPresent()' due to 'Member#isInVoiceChannel()'
                } else {
                    throw new JavacordInvalidCommandArgument(JavacordMessageKeys.USER_NOT_IN_VOICE_CHANNEL);
                }
            }
            boolean isCrossServer = c.hasAnnotation(CrossServer.class);
            String arg = c.getFirstArg(); // Test input
            ServerVoiceChannel channel = null;
            if (DiscordRegexPattern.CHANNEL_MENTION.matcher(arg).matches()) {
                String id = arg.replaceAll("[^0-9]", ""); // Extract non-negative integers to retrieve ID
                channel = isCrossServer || !c.issuer.getServer().isPresent()
                        ? api.getServerVoiceChannelById(id).isPresent()
                        ? api.getServerVoiceChannelById(id).get() : null
                        : c.issuer.getServer().get().getVoiceChannelById(id).isPresent()
                        ? c.issuer.getServer().get().getVoiceChannelById(id).get() : null;
            }

            if (channel != null) {
                c.popFirstArg();
            } else {
                if (c.hasFlag("require")) {
                    throw new JavacordInvalidCommandArgument(JavacordMessageKeys.COULD_NOT_FIND_CHANNEL);
                }
            }
            return channel;
        });
        registerContext(Role.class, c -> {
            boolean isCrossServer = c.hasAnnotation(CrossServer.class);

            String arg = c.isLastArg() ? String.join(" ", c.getArgs()) : c.popFirstArg();

            if (!c.isOptional() && (arg == null || arg.isEmpty())) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.PLEASE_SPECIFY_ROLE);
            }

            Optional<Role> role = Optional.empty();
            if (DiscordRegexPattern.ROLE_MENTION.matcher(arg).matches()) {
                String id = arg.replaceAll("[^0-9]", ""); // Extract non-negative integers to retrieve ID
                role = (!isCrossServer && c.issuer.getServer().isPresent())
                        ? c.issuer.getServer().get().getRoleById(id)
                        : api.getRoleById(id);
            }
            else {
                try {
                    long id = Long.parseLong(arg);
                    role = (!isCrossServer && c.issuer.getServer().isPresent())
                            ? c.issuer.getServer().get().getRoleById(id)
                            : api.getRoleById(id);
                } catch (NumberFormatException ex) {
                    Collection<Role> roles = (!isCrossServer && c.issuer.getServer().isPresent())
                            ? c.issuer.getServer().get().getRolesByNameIgnoreCase(arg)
                            : api.getRolesByNameIgnoreCase(arg);

                    if (roles.size() > 1) {
                        throw new JavacordInvalidCommandArgument(JavacordMessageKeys.TOO_MANY_ROLES_WITH_NAME);
                    }
                    if (!roles.isEmpty()) {
                        role = Optional.of(ACFUtil.getFirstElement(roles));
                    }
                }
            }

            if (!role.isPresent()) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.COULD_NOT_FIND_ROLE);
            }
            return role.get();
        });
        registerContext(Emoji.class, c -> {
            String arg = c.popFirstArg();

            if (!c.isOptional() && (arg == null || arg.isEmpty())) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.PLEASE_SPECIFY_EMOJI);
            }

            Emoji emoji = null;
            if (DiscordRegexPattern.CUSTOM_EMOJI.matcher(arg).matches()) {
                String id = arg.replaceAll("[^0-9]", ""); // Extract non-negative integers to retrieve ID
                if (!api.getCustomEmojiById(id).isPresent()) {
                    throw new JavacordInvalidCommandArgument(JavacordMessageKeys.COULD_NOT_FIND_EMOJI);
                }
                emoji = api.getCustomEmojiById(id).get();
            } else if (EmojiManager.isEmoji(arg)) {
                emoji = new UnicodeEmoji(arg);
            }

            if (emoji == null) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.COULD_NOT_FIND_EMOJI);
            }
            return emoji;
        });
        registerContext(UnicodeEmoji.class, c -> {
            String arg = c.popFirstArg();

            if (!c.isOptional() && (arg == null || arg.isEmpty())) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.PLEASE_SPECIFY_EMOJI);
            }

            if (!EmojiManager.isEmoji(arg)) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.COULD_NOT_FIND_EMOJI);
            }
            return new UnicodeEmoji(arg);
        });
        registerContext(CustomEmoji.class, c -> {
            String arg = c.popFirstArg();

            if (!c.isOptional() && (arg == null || arg.isEmpty())) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.PLEASE_SPECIFY_EMOJI);
            }

            Optional<KnownCustomEmoji> emoji = Optional.empty();
            if (DiscordRegexPattern.CUSTOM_EMOJI.matcher(arg).matches()) {
                String id = arg.replaceAll("[^0-9]", ""); // Extract non-negative integers to retrieve ID
                emoji = api.getCustomEmojiById(id);
            } else {
                Collection<KnownCustomEmoji> emojis = api.getCustomEmojisByName(arg);
                if (emojis.size() > 1) {
                    throw new JavacordInvalidCommandArgument(JavacordMessageKeys.TOO_MANY_EMOJIS_WITH_NAME);
                }
                if (!emojis.isEmpty()) {
                    emoji = Optional.of(ACFUtil.getFirstElement(emojis));
                }
            }

            if (!emoji.isPresent()) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.COULD_NOT_FIND_EMOJI);
            }
            return emoji.get();
        });
        registerContext(KnownCustomEmoji.class, c -> {
            String arg = c.popFirstArg();

            if (!c.isOptional() && (arg == null || arg.isEmpty())) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.PLEASE_SPECIFY_EMOJI);
            }

            Optional<KnownCustomEmoji> emoji = Optional.empty();
            if (DiscordRegexPattern.CUSTOM_EMOJI.matcher(arg).matches()) {
                String id = arg.replaceAll("[^0-9]", ""); // Extract non-negative integers to retrieve ID
                emoji = api.getCustomEmojiById(id);
            } else {
                Collection<KnownCustomEmoji> emojis = api.getCustomEmojisByName(arg);
                if (emojis.size() > 1) {
                    throw new JavacordInvalidCommandArgument(JavacordMessageKeys.TOO_MANY_EMOJIS_WITH_NAME);
                }
                if (!emojis.isEmpty()) {
                    emoji = Optional.of(ACFUtil.getFirstElement(emojis));
                }
            }

            if (!emoji.isPresent()) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.COULD_NOT_FIND_EMOJI);
            }
            return emoji.get();
        });
    }

    /* Utility methods */

    private <T extends Number> T resolveBigNumber(@NotNull MessageCommandExecutionContext c) {
        String arg = c.popFirstArg();
        try {
            T number = (T) ACFUtil.parseBigNumber(arg, c.hasFlag("suffixes"));
            this.validateMinMax(c, number);
            return number;
        } catch (NumberFormatException ex) {
            throw new JavacordInvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", arg);
        }
    }

    private Number resolveNumber(@NotNull MessageCommandExecutionContext c, @NotNull Number minValue, @NotNull Number maxValue) {
        String number = c.popFirstArg();
        try {
            return this.parseAndValidateNumber(number, c, minValue, maxValue).shortValue();
        } catch (NumberFormatException ex) {
            throw new JavacordInvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", number);
        }
    }

    @NotNull
    private Number parseAndValidateNumber(String number, MessageCommandExecutionContext c, Number minValue, Number maxValue) throws JavacordInvalidCommandArgument {
        Number val = ACFUtil.parseNumber(number, c.hasFlag("suffixes"));
        this.validateMinMax(c, val, minValue, maxValue);
        return val;
    }
}
