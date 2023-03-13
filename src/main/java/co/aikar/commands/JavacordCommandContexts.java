/*
 * Copyright c 2021 Kevin Zuman (Greenadine)
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

import co.aikar.commands.annotation.*;
import co.aikar.commands.javacord.contexts.Member;
import co.aikar.commands.javacord.contexts.UnicodeEmoji;
import com.google.common.collect.Iterables;
import com.vdurmont.emoji.EmojiManager;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.*;
import org.javacord.api.entity.emoji.CustomEmoji;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.emoji.KnownCustomEmoji;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.util.DiscordRegexPattern;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @since 0.1
 * @author Greenadine
 */
public class JavacordCommandContexts extends CommandContexts<JavacordCommandExecutionContext> {

    private final DiscordApi api;

    @SuppressWarnings("unchecked,OptionalGetWithoutIsPresent")
    public JavacordCommandContexts(JavacordCommandManager manager) {
        super(manager);
        this.api = manager.getApi();

        // Override ACF core context resolvers
        this.registerContext(Short.class, c -> {
            String number = c.popFirstArg();

            try {
                return this.parseAndValidateNumber(number, c, -32768, (short)32767).shortValue();
            } catch (NumberFormatException ex) {
                throw new JavacordInvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", number);
            }
        });
        this.registerContext(Short.TYPE, c -> {
            String number = c.popFirstArg();

            try {
                return this.parseAndValidateNumber(number, c, -32768, (short)32767).shortValue();
            } catch (NumberFormatException ex) {
                throw new JavacordInvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", number);
            }
        });
        this.registerContext(Integer.class, c -> {
            String number = c.popFirstArg();

            try {
                return this.parseAndValidateNumber(number, c, -2147483648, 2147483647).intValue();
            } catch (NumberFormatException ex) {
                throw new JavacordInvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", number);
            }
        });
        this.registerContext(Integer.TYPE, c -> {
            String number = c.popFirstArg();

            try {
                return this.parseAndValidateNumber(number, c, -2147483648, 2147483647).intValue();
            } catch (NumberFormatException ex) {
                throw new JavacordInvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", number);
            }
        });
        this.registerContext(Long.class, c -> {
            String number = c.popFirstArg();

            try {
                return this.parseAndValidateNumber(number, c, -9223372036854775808L, 9223372036854775807L).longValue();
            } catch (NumberFormatException ex) {
                throw new JavacordInvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", number);
            }
        });
        this.registerContext(Long.TYPE, c -> {
            String number = c.popFirstArg();

            try {
                return this.parseAndValidateNumber(number, c, -9223372036854775808L, 9223372036854775807L).longValue();
            } catch (NumberFormatException ex) {
                throw new JavacordInvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", number);
            }
        });
        this.registerContext(Float.class, c -> {
            String number = c.popFirstArg();

            try {
                return this.parseAndValidateNumber(number, c, -3.4028235E38F, 3.4028235E38F).floatValue();
            } catch (NumberFormatException ex) {
                throw new JavacordInvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", number);
            }
        });
        this.registerContext(Float.TYPE, c -> {
            String number = c.popFirstArg();

            try {
                return this.parseAndValidateNumber(number, c, -3.4028235E38F, 3.4028235E38F).floatValue();
            } catch (NumberFormatException ex) {
                throw new JavacordInvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", number);
            }
        });
        this.registerContext(Double.class, c -> {
            String number = c.popFirstArg();

            try {
                return this.parseAndValidateNumber(number, c, -1.7976931348623157E308D, 1.7976931348623157E308D).doubleValue();
            } catch (NumberFormatException ex) {
                throw new JavacordInvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", number);
            }
        });
        this.registerContext(Double.TYPE, c -> {
            String number = c.popFirstArg();

            try {
                return this.parseAndValidateNumber(number, c, -1.7976931348623157E308D, 1.7976931348623157E308D).doubleValue();
            } catch (NumberFormatException ex) {
                throw new JavacordInvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", number);
            }
        });
        this.registerContext(Number.class, c -> {
            String number = c.popFirstArg();

            try {
                return this.parseAndValidateNumber(number, c, -1.7976931348623157E308D, 1.7976931348623157E308D);
            } catch (NumberFormatException ex) {
                throw new JavacordInvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", number);
            }
        });
        this.registerContext(BigDecimal.class, c -> {
            String numberStr = c.popFirstArg();

            try {
                BigDecimal number = ACFUtil.parseBigNumber(numberStr, c.hasFlag("suffixes"));
                this.validateMinMax(c, number);
                return number;
            } catch (NumberFormatException ex) {
                throw new JavacordInvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", numberStr);
            }
        });
        this.registerContext(BigInteger.class, c -> {
            String numberStr = c.popFirstArg();

            try {
                BigDecimal number = ACFUtil.parseBigNumber(numberStr, c.hasFlag("suffixes"));
                this.validateMinMax(c, number);
                return number.toBigIntegerExact();
            } catch (NumberFormatException ex) {
                throw new JavacordInvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", numberStr);
            }
        });
        this.registerContext(Character.TYPE, c -> {
            String s = c.popFirstArg();
            if (s.length() > 1) {
                throw new JavacordInvalidCommandArgument(MessageKeys.MUST_BE_MAX_LENGTH, "{max}", String.valueOf(1));
            } else {
                return s.charAt(0);
            }
        });
        this.registerContext(String.class, c -> {
            if (c.hasAnnotation(Values.class)) {
                return c.popFirstArg();
            } else {
                String ret = c.isLastArg() && !c.hasAnnotation(Single.class) ? ACFUtil.join(c.getArgs()) : c.popFirstArg();
                Integer minLen = c.getFlagValue("minlen", (Integer) null);
                Integer maxLen = c.getFlagValue("maxlen", (Integer) null);
                if (minLen != null && ret.length() < minLen) {
                    throw new JavacordInvalidCommandArgument(MessageKeys.MUST_BE_MIN_LENGTH, "{min}", String.valueOf(minLen));
                } else if (maxLen != null && ret.length() > maxLen) {
                    throw new JavacordInvalidCommandArgument(MessageKeys.MUST_BE_MAX_LENGTH, "{max}", String.valueOf(minLen));
                } else {
                    return ret;
                }
            }
        });
        this.registerContext(String[].class, c -> {
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
        this.registerContext(Enum.class, (c) -> {
            String first = c.popFirstArg();
            Class<? extends Enum<?>> enumCls = (Class<? extends Enum<?>>) c.getParam().getType();
            Enum<?> match = ACFUtil.simpleMatch(enumCls, first);
            if (match == null) {
                List<String> names = ACFUtil.enumNames(enumCls);
                throw new InvalidCommandArgument(MessageKeys.PLEASE_SPECIFY_ONE_OF, "{valid}", ACFUtil.join(names, ", "));
            } else {
                return match;
            }
        });

        // Javacord context resolvers
        this.registerIssuerOnlyContext(JavacordCommandEvent.class, CommandExecutionContext::getIssuer);
        this.registerIssuerOnlyContext(MessageCreateEvent.class, c -> c.issuer.getIssuer());
        this.registerIssuerOnlyContext(DiscordApi.class, c -> api);
        this.registerIssuerOnlyContext(Message.class, c -> c.issuer.getIssuer().getMessage());
        this.registerIssuerOnlyContext(ChannelType.class, c -> c.issuer.getIssuer().getChannel().getType());
        this.registerIssuerOnlyContext(Server.class, c -> {
            if (!c.issuer.getIssuer().isServerMessage()) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.SERVER_ONLY);
            } else {
                return c.issuer.getIssuer().getServer().get(); // No need for 'isPresent()' due to 'MessageCreateEvent#isServerMessage()'
            }
        });
        this.registerIssuerAwareContext(User.class, c -> {
            if (c.hasAnnotation(BotUser.class)) {
                return api.getYourself();
            }

            if (!c.hasFlag("other")) {
                return c.issuer.getUser();
            } else {
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
            }
        });
        this.registerIssuerAwareContext(Member.class, c -> {
            if (!c.issuer.getIssuer().isServerMessage()) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.SERVER_ONLY);
            }

            if (c.hasAnnotation(BotUser.class)) {
                return new Member(api.getYourself(), c.issuer.getIssuer().getServer().get());
            }

            if (!c.hasFlag("other")) {
                return c.issuer.getMember();
            } else {
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
                if (user != null && !c.issuer.getIssuer().getServer().get().isMember(user)) {
                    throw new JavacordInvalidCommandArgument(JavacordMessageKeys.USER_NOT_MEMBER_OF_SERVER);
                }
                if (user != null && c.hasFlag("humanonly") && user.isBot()) {
                    throw new JavacordInvalidCommandArgument(JavacordMessageKeys.USER_IS_BOT);
                }

                return user != null ? new Member(user, c.issuer.getIssuer().getServer().get()) : null;
            }
        });
        this.registerIssuerAwareContext(Channel.class, c -> {
            if (!c.hasFlag("other")) {
                return c.issuer.getEvent().getChannel();
            }

            boolean isCrossServer = c.hasAnnotation(CrossServer.class);
            String arg = c.getFirstArg(); // Test input
            Channel channel = null;
            if (DiscordRegexPattern.CHANNEL_MENTION.matcher(arg).matches()) {
                String id = arg.replaceAll("[^0-9]", ""); // Extract non-negative integers to retrieve ID
                channel = isCrossServer || !c.issuer.getIssuer().getServer().isPresent()
                        ? api.getChannelById(id).isPresent()
                            ? api.getChannelById(id).get() : null
                        : c.issuer.getIssuer().getServer().get().getChannelById(id).isPresent()
                            ? c.issuer.getIssuer().getServer().get().getChannelById(id).get() : null;
            }

            if (channel != null)
                c.popFirstArg(); // Consume input
            else {
                if (!c.hasFlag("require")) {
                    throw new JavacordInvalidCommandArgument(JavacordMessageKeys.COULD_NOT_FIND_CHANNEL);
                }
                channel = c.issuer.getEvent().getChannel();
            }
            return channel;
        });
        this.registerIssuerOnlyContext(PrivateChannel.class, c -> {
            if (!c.issuer.getIssuer().isPrivateMessage()) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.PRIVATE_ONLY);
            }
            return c.issuer.getIssuer().getPrivateChannel().get(); // No need for 'isPresent()' due to 'MessageCreateEvent#isPrivateMessage()'
        });
//        this.registerIssuerOnlyContext(GroupChannel.class, c -> {
//            if (!c.issuer.getIssuer().isGroupMessage()) {
//                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.GROUP_ONLY);
//            }
//            return c.issuer.getIssuer().getGroupChannel().get(); // No need for 'isPresent()' due to 'MessageCreateEvent#isGroupMessage()'
//        });
        this.registerIssuerAwareContext(TextChannel.class, c -> {
            if (!c.hasFlag("other")) {
                return c.issuer.getEvent().getChannel();
            }
            boolean isCrossServer = c.hasAnnotation(CrossServer.class);
            String arg = c.getFirstArg(); // Test input
            TextChannel channel = null;
            if (DiscordRegexPattern.CHANNEL_MENTION.matcher(arg).matches()) {
                String id = arg.replaceAll("[^0-9]", ""); // Extract non-negative integers to retrieve ID
                channel = isCrossServer || !c.issuer.getIssuer().getServer().isPresent()
                        ? api.getTextChannelById(id).isPresent()
                            ? api.getTextChannelById(id).get() : null
                        : c.issuer.getIssuer().getServer().get().getTextChannelById(id).isPresent()
                            ? c.issuer.getIssuer().getServer().get().getTextChannelById(id).get() : null;
            }

            if (channel != null)
                c.popFirstArg(); // Consume input
            else {
                if (!c.hasFlag("require")) {
                    throw new JavacordInvalidCommandArgument(JavacordMessageKeys.COULD_NOT_FIND_CHANNEL);
                }
                channel = c.issuer.getChannel();
            }
            return channel;
        });
        this.registerIssuerAwareContext(ServerTextChannel.class, c -> {
            if (!c.issuer.getIssuer().getServer().isPresent()) {
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
                channel = isCrossServer || !c.issuer.getIssuer().getServer().isPresent()
                        ? api.getServerTextChannelById(id).isPresent()
                            ? api.getServerTextChannelById(id).get() : null
                        : c.issuer.getIssuer().getServer().get().getTextChannelById(id).isPresent()
                            ? c.issuer.getIssuer().getServer().get().getTextChannelById(id).get() : null;
            }

            if (channel != null)
                c.popFirstArg(); // Consume input
            else {
                if (!c.hasFlag("require")) {
                    throw new JavacordInvalidCommandArgument(JavacordMessageKeys.COULD_NOT_FIND_CHANNEL);
                }
                channel = c.issuer.getIssuer().getServerTextChannel().get();
            }
            return channel;
        });
        this.registerIssuerAwareContext(VoiceChannel.class, c -> {
            if (!c.hasFlag("other")) {
                if (c.issuer.getIssuer().isServerMessage() && c.issuer.getMember().isInVoiceChannel()) {
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
                channel = isCrossServer || !c.issuer.getIssuer().getServer().isPresent()
                        ? api.getVoiceChannelById(id).isPresent()
                            ? api.getVoiceChannelById(id).get() : null
                        : c.issuer.getIssuer().getServer().get().getVoiceChannelById(id).isPresent()
                            ? c.issuer.getIssuer().getServer().get().getVoiceChannelById(id).get() : null;
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
        this.registerIssuerAwareContext(ServerVoiceChannel.class, c -> {
            if (!c.hasFlag("other")) {
                if (c.issuer.getIssuer().isServerMessage() && c.issuer.getMember().isInVoiceChannel()) {
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
                channel = isCrossServer || !c.issuer.getIssuer().getServer().isPresent()
                        ? api.getServerVoiceChannelById(id).isPresent()
                            ? api.getServerVoiceChannelById(id).get() : null
                        : c.issuer.getIssuer().getServer().get().getVoiceChannelById(id).isPresent()
                            ? c.issuer.getIssuer().getServer().get().getVoiceChannelById(id).get() : null;
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
        this.registerContext(Role.class, c -> {
            boolean isCrossServer = c.hasAnnotation(CrossServer.class);

            String arg = c.isLastArg() ? String.join(" ", c.getArgs()) : c.popFirstArg();

            if (!c.isOptional() && (arg == null || arg.isEmpty())) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.PLEASE_SPECIFY_ROLE);
            }

            Optional<Role> role = Optional.empty();
            if (DiscordRegexPattern.ROLE_MENTION.matcher(arg).matches()) {
                String id = arg.replaceAll("[^0-9]", ""); // Extract non-negative integers to retrieve ID
                role = (!isCrossServer && c.issuer.getIssuer().getServer().isPresent())
                        ? c.issuer.getIssuer().getServer().get().getRoleById(id)
                        : api.getRoleById(id);
            }
            else {
                try {
                    long id = Long.parseLong(arg);
                    role = (!isCrossServer && c.issuer.getIssuer().getServer().isPresent())
                            ? c.issuer.getIssuer().getServer().get().getRoleById(id)
                            : api.getRoleById(id);
                } catch (NumberFormatException ex) {
                    Collection<Role> roles = (!isCrossServer && c.issuer.getIssuer().getServer().isPresent())
                            ? c.issuer.getIssuer().getServer().get().getRolesByNameIgnoreCase(arg)
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
        this.registerContext(Emoji.class, c -> {
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
        this.registerContext(UnicodeEmoji.class, c -> {
            String arg = c.popFirstArg();

            if (!c.isOptional() && (arg == null || arg.isEmpty())) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.PLEASE_SPECIFY_EMOJI);
            }

            if (!EmojiManager.isEmoji(arg)) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.COULD_NOT_FIND_EMOJI);
            }
            return new UnicodeEmoji(arg);
        });
        this.registerContext(CustomEmoji.class, c -> {
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
        this.registerContext(KnownCustomEmoji.class, c -> {
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

    @NotNull
    private Number parseAndValidateNumber(String number, JavacordCommandExecutionContext c, Number minValue, Number maxValue) throws InvalidCommandArgument {
        Number val = ACFUtil.parseNumber(number, c.hasFlag("suffixes"));
        this.validateMinMax(c, val, minValue, maxValue);
        return val;
    }

    private void validateMinMax(JavacordCommandExecutionContext c, Number val) throws InvalidCommandArgument {
        this.validateMinMax(c, val, null, null);
    }

    private void validateMinMax(JavacordCommandExecutionContext c, Number val, Number minValue, Number maxValue) throws InvalidCommandArgument {
        minValue = c.getFlagValue("min", minValue);
        maxValue = c.getFlagValue("max", maxValue);
        if (maxValue != null && val.doubleValue() > maxValue.doubleValue()) {
            throw new JavacordInvalidCommandArgument(MessageKeys.PLEASE_SPECIFY_AT_MOST, "{max}", String.valueOf(maxValue));
        } else if (minValue != null && val.doubleValue() < minValue.doubleValue()) {
            throw new JavacordInvalidCommandArgument(MessageKeys.PLEASE_SPECIFY_AT_LEAST, "{min}", String.valueOf(minValue));
        }
    }
}
