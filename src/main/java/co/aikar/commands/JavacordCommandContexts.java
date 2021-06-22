/*
 * Copyright (c) 2021 Kevin Zuman (Greenadine)
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

import co.aikar.commands.annotation.Author;
import co.aikar.commands.annotation.CrossServer;
import co.aikar.commands.annotation.Require;
import co.aikar.commands.annotation.SelfUser;
import co.aikar.commands.javacord.contexts.Member;
import co.aikar.commands.javacord.contexts.UnicodeEmoji;
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

import java.util.Collection;
import java.util.Optional;

public class JavacordCommandContexts extends CommandContexts<JavacordCommandExecutionContext> {

    private final DiscordApi api;

    public JavacordCommandContexts(JavacordCommandManager manager) {
        super(manager);
        this.api = manager.getApi();
        this.registerIssuerOnlyContext(JavacordCommandEvent.class, CommandExecutionContext::getIssuer);
        this.registerIssuerOnlyContext(MessageCreateEvent.class, c -> c.issuer.getIssuer());
        this.registerIssuerOnlyContext(Message.class, c -> c.issuer.getIssuer().getMessage());
        this.registerIssuerOnlyContext(ChannelType.class, c -> c.issuer.getIssuer().getChannel().getType());
        this.registerIssuerOnlyContext(DiscordApi.class, c -> api);
        this.registerIssuerOnlyContext(Server.class, c -> {
            if (!c.issuer.getIssuer().getServer().isPresent()) {
                throw new InvalidCommandArgument(JavacordMessageKeys.SERVER_ONLY, false);
            } else {
                return c.issuer.getIssuer().getServer().get();
            }
        });
        this.registerContext(User.class, c -> {
            if (c.hasAnnotation(SelfUser.class)) {
                return api.getYourself();
            }
            if ("false".equalsIgnoreCase(c.getFlagValue("other", "false"))) {
                //noinspection OptionalGetWithoutIsPresent
                return c.issuer.getIssuer().getMessageAuthor().asUser().get();
            }

            String arg = c.isLastArg() ? String.join(" ", c.getArgs()) : c.popFirstArg();

            if (!c.isOptional() && (arg == null || arg.isEmpty())) {
                throw new InvalidCommandArgument(JavacordMessageKeys.PLEASE_SPECIFY_USER, false);
            }

            User user = null;
            if (DiscordRegexPattern.USER_MENTION.matcher(arg).matches()) {
                String id = arg.replaceAll("[^0-9]", ""); // Extract non-negative integers to retrieve ID
                user = api.getUserById(id).join();
            } else {
                Collection<User> users = api.getCachedUsersByNameIgnoreCase(arg);
                if (users.size() > 1) {
                    throw new InvalidCommandArgument(JavacordMessageKeys.TOO_MANY_USERS_WITH_NAME, false);
                } else if (!users.isEmpty()) {
                    user = ACFUtil.getFirstElement(users);
                }
            }

            if (user == null) {
                throw new InvalidCommandArgument(JavacordMessageKeys.COULD_NOT_FIND_USER, false);
            }
            return user;
        });
        this.registerContext(Member.class, c -> {
            if (!c.issuer.getIssuer().getServer().isPresent()) {
                throw new InvalidCommandArgument(JavacordMessageKeys.SERVER_ONLY, false);
            }

            Server server = c.issuer.getIssuer().getServer().get();
            if (c.hasAnnotation(SelfUser.class)) {
                return new Member(api.getYourself(), server);
            }

            if ("false".equalsIgnoreCase(c.getFlagValue("other", "false"))) {
                //noinspection OptionalGetWithoutIsPresent
                return new Member(c.issuer.getIssuer().getMessageAuthor().asUser().get(), server);
            }

            String arg = c.isLastArg() ? String.join(" ", c.getArgs()) : c.popFirstArg();

            if (!c.isOptional() && (arg == null || arg.isEmpty())) {
                throw new InvalidCommandArgument(JavacordMessageKeys.PLEASE_SPECIFY_USER, false);
            }

            User user = null;
            if (DiscordRegexPattern.USER_MENTION.matcher(arg).matches()) {
                String id = arg.replaceAll("[^0-9]", ""); // Extract non-negative integers to retrieve ID
                user = api.getUserById(id).join();
            } else {
                Collection<User> users = api.getCachedUsersByNameIgnoreCase(arg);
                if (users.size() > 1) {
                    throw new InvalidCommandArgument(JavacordMessageKeys.TOO_MANY_USERS_WITH_NAME, false);
                } else if (!users.isEmpty()) {
                    user = ACFUtil.getFirstElement(users);
                }
            }

            if (user == null)
                throw new InvalidCommandArgument(JavacordMessageKeys.COULD_NOT_FIND_USER, false);
            else if (!c.issuer.getIssuer().getServer().get().isMember(user))
                throw new InvalidCommandArgument(JavacordMessageKeys.USER_NOT_MEMBER_OF_SERVER, false);
            return new Member(user, server);
        });
        this.registerIssuerAwareContext(Channel.class, c -> {
            if (c.hasAnnotation(Author.class)) {
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
                if (c.hasAnnotation(Require.class)) {
                    throw new InvalidCommandArgument(JavacordMessageKeys.COULD_NOT_FIND_CHANNEL, false);
                }
                channel = c.issuer.getEvent().getChannel();
            }
            return channel;
        });
        this.registerIssuerOnlyContext(PrivateChannel.class, c -> {
            if (!c.issuer.getIssuer().isPrivateMessage()) {
                throw new InvalidCommandArgument(JavacordMessageKeys.PRIVATE_ONLY, false);
            }
            return (PrivateChannel) c.issuer.getIssuer().getChannel();
        });
        this.registerIssuerOnlyContext(GroupChannel.class, c -> {
            if (!c.issuer.getIssuer().isGroupMessage()) {
                throw new InvalidCommandArgument(JavacordMessageKeys.GROUP_ONLY, false);
            }
            //noinspection OptionalGetWithoutIsPresent
            return c.issuer.getIssuer().getGroupChannel().get();
        });
        this.registerIssuerAwareContext(TextChannel.class, c -> {
            if (c.hasAnnotation(Author.class)) {
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
                if (c.hasAnnotation(Require.class)) {
                    throw new InvalidCommandArgument(JavacordMessageKeys.COULD_NOT_FIND_CHANNEL, false);
                }
                channel = c.issuer.getChannel();
            }
            return channel;
        });
        this.registerIssuerAwareContext(ServerTextChannel.class, c -> {
            if (!c.issuer.getIssuer().getServer().isPresent()) {
                throw new InvalidCommandArgument(JavacordMessageKeys.SERVER_ONLY, false);
            }
            if (c.hasAnnotation(Author.class)) {
                //noinspection OptionalGetWithoutIsPresent
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
                if (c.hasAnnotation(Require.class)) {
                    throw new InvalidCommandArgument(JavacordMessageKeys.COULD_NOT_FIND_CHANNEL, false);
                }
                //noinspection OptionalGetWithoutIsPresent
                channel = c.issuer.getIssuer().getServerTextChannel().get();
            }
            return channel;
        });
        this.registerIssuerAwareContext(VoiceChannel.class, c -> {
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
                if (c.hasAnnotation(Require.class)) {
                    throw new InvalidCommandArgument(JavacordMessageKeys.COULD_NOT_FIND_CHANNEL, false);
                }
                if (c.issuer.getIssuer().isServerMessage()) {
                    //noinspection OptionalGetWithoutIsPresent
                    Member member = new Member(c.issuer.getIssuer().getMessageAuthor().asUser().get(), c.issuer.getIssuer().getServer().get());
                    if (!member.isInVoiceChannel()) {
                        throw new InvalidCommandArgument(JavacordMessageKeys.USER_NOT_IN_VOICE_CHANNEL, false);
                    }
                    //noinspection OptionalGetWithoutIsPresent
                    channel = member.getConnectedVoiceChannel().get();
                }
            }
            return channel;
        });
        this.registerIssuerAwareContext(ServerVoiceChannel.class, c -> {
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
                if (c.hasAnnotation(Require.class)) {
                    throw new InvalidCommandArgument(JavacordMessageKeys.COULD_NOT_FIND_CHANNEL, false);
                }
                if (c.issuer.getIssuer().isServerMessage()) {
                    //noinspection OptionalGetWithoutIsPresent
                    Member member = new Member(c.issuer.getIssuer().getMessageAuthor().asUser().get(), c.issuer.getIssuer().getServer().get());
                    if (!member.isInVoiceChannel()) {
                        throw new InvalidCommandArgument(JavacordMessageKeys.USER_NOT_IN_VOICE_CHANNEL, false);
                    }
                    //noinspection OptionalGetWithoutIsPresent
                    channel = member.getConnectedVoiceChannel().get();
                }
            }
            return channel;
        });
        this.registerContext(Role.class, c -> {
            boolean isCrossServer = c.hasAnnotation(CrossServer.class);

            String arg = c.isLastArg() ? String.join(" ", c.getArgs()) : c.popFirstArg();

            if (!c.isOptional() && (arg == null || arg.isEmpty())) {
                throw new InvalidCommandArgument(JavacordMessageKeys.PLEASE_SPECIFY_ROLE, false);
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
                        throw new InvalidCommandArgument(JavacordMessageKeys.TOO_MANY_ROLES_WITH_NAME, false);
                    }
                    if (!roles.isEmpty()) {
                        role = Optional.of(ACFUtil.getFirstElement(roles));
                    }
                }
            }

            if (!role.isPresent()) {
                throw new InvalidCommandArgument(JavacordMessageKeys.COULD_NOT_FIND_ROLE, false);
            }
            return role.get();
        });
        this.registerContext(Emoji.class, c -> {
            String arg = c.popFirstArg();

            if (!c.isOptional() && (arg == null || arg.isEmpty())) {
                throw new InvalidCommandArgument(JavacordMessageKeys.PLEASE_SPECIFY_EMOJI, false);
            }

            Emoji emoji = null;
            if (DiscordRegexPattern.CUSTOM_EMOJI.matcher(arg).matches()) {
                String id = arg.replaceAll("[^0-9]", ""); // Extract non-negative integers to retrieve ID
                if (!api.getCustomEmojiById(id).isPresent()) {
                    throw new InvalidCommandArgument(JavacordMessageKeys.COULD_NOT_FIND_EMOJI, false);
                }
                emoji = api.getCustomEmojiById(id).get();
            } else if (EmojiManager.isEmoji(arg)) {
                emoji = new UnicodeEmoji(arg);
            }

            if (emoji == null) {
                throw new InvalidCommandArgument(JavacordMessageKeys.COULD_NOT_FIND_EMOJI, false);
            }
            return emoji;
        });
        this.registerContext(UnicodeEmoji.class, c -> {
            String arg = c.popFirstArg();

            if (!c.isOptional() && (arg == null || arg.isEmpty())) {
                throw new InvalidCommandArgument(JavacordMessageKeys.PLEASE_SPECIFY_EMOJI, false);
            }

            if (!EmojiManager.isEmoji(arg)) {
                throw new InvalidCommandArgument(JavacordMessageKeys.COULD_NOT_FIND_EMOJI, false);
            }
            return new UnicodeEmoji(arg);
        });
        this.registerContext(CustomEmoji.class, c -> {
            String arg = c.popFirstArg();

            if (!c.isOptional() && (arg == null || arg.isEmpty())) {
                throw new InvalidCommandArgument(JavacordMessageKeys.PLEASE_SPECIFY_EMOJI, false);
            }

            Optional<KnownCustomEmoji> emoji = Optional.empty();
            if (DiscordRegexPattern.CUSTOM_EMOJI.matcher(arg).matches()) {
                String id = arg.replaceAll("[^0-9]", ""); // Extract non-negative integers to retrieve ID
                emoji = api.getCustomEmojiById(id);
            } else {
                Collection<KnownCustomEmoji> emojis = api.getCustomEmojisByName(arg);
                if (emojis.size() > 1) {
                    throw new InvalidCommandArgument(JavacordMessageKeys.TOO_MANY_EMOJIS_WITH_NAME);
                }
                if (!emojis.isEmpty()) {
                    emoji = Optional.of(ACFUtil.getFirstElement(emojis));
                }
            }

            if (!emoji.isPresent()) {
                throw new InvalidCommandArgument(JavacordMessageKeys.COULD_NOT_FIND_EMOJI);
            }
            return emoji.get();
        });
        this.registerContext(KnownCustomEmoji.class, c -> {
            String arg = c.popFirstArg();

            if (!c.isOptional() && (arg == null || arg.isEmpty())) {
                throw new InvalidCommandArgument(JavacordMessageKeys.PLEASE_SPECIFY_EMOJI, false);
            }

            Optional<KnownCustomEmoji> emoji = Optional.empty();
            if (DiscordRegexPattern.CUSTOM_EMOJI.matcher(arg).matches()) {
                String id = arg.replaceAll("[^0-9]", ""); // Extract non-negative integers to retrieve ID
                emoji = api.getCustomEmojiById(id);
            } else {
                Collection<KnownCustomEmoji> emojis = api.getCustomEmojisByName(arg);
                if (emojis.size() > 1) {
                    throw new InvalidCommandArgument(JavacordMessageKeys.TOO_MANY_EMOJIS_WITH_NAME);
                }
                if (!emojis.isEmpty()) {
                    emoji = Optional.of(ACFUtil.getFirstElement(emojis));
                }
            }

            if (!emoji.isPresent()) {
                throw new InvalidCommandArgument(JavacordMessageKeys.COULD_NOT_FIND_EMOJI);
            }
            return emoji.get();
        });
    }
}
