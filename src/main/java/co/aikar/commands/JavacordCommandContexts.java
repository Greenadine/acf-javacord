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

import co.aikar.commands.annotations.Author;
import co.aikar.commands.annotations.CrossServer;
import co.aikar.commands.annotations.SelfUser;
import co.aikar.commands.javacord.contexts.Member;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.util.DiscordRegexPattern;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class JavacordCommandContexts extends CommandContexts<JavacordCommandExecutionContext> {

    private final JavacordCommandManager manager;
    private final DiscordApi api;

    public JavacordCommandContexts(JavacordCommandManager manager) {
        super(manager);
        this.manager = manager;
        this.api = this.manager.getApi();
        this.registerIssuerOnlyContext(JavacordCommandEvent.class, CommandExecutionContext::getIssuer);
        this.registerIssuerOnlyContext(MessageCreateEvent.class, c -> c.issuer.getIssuer());
        this.registerIssuerOnlyContext(Message.class, c -> c.issuer.getIssuer().getMessage());
        this.registerIssuerOnlyContext(ChannelType.class, c -> c.issuer.getIssuer().getChannel().getType());
        this.registerIssuerOnlyContext(DiscordApi.class, c -> api);
        this.registerIssuerOnlyContext(Server.class, c -> {
            MessageCreateEvent event = c.issuer.getIssuer();
            if (!event.getServer().isPresent()) {
                throw new InvalidCommandArgument(JavacordMessageKeys.SERVER_ONLY, false);
            } else {
                return event.getServer().get();
            }
        });
        this.registerContext(TextChannel.class, c -> {
            if (c.hasAnnotation(Author.class)) {
                return c.getIssuer().getEvent().getChannel();
            }
            boolean isCrossServer = c.hasAnnotation(CrossServer.class);
            String arg = c.popFirstArg();
            Optional<ServerTextChannel> channel = Optional.empty();
            if (arg.startsWith("<#")) {
                String id = arg.substring(2, arg.length() - 1);
                channel = (!isCrossServer && c.issuer.getIssuer().getServer().isPresent())
                        ? c.issuer.getIssuer().getServer().get().getTextChannelById(id)
                        : api.getServerTextChannelById(id);
            } else {
                Collection<ServerTextChannel> channels = (!isCrossServer && c.issuer.getIssuer().getServer().isPresent())
                        ? c.issuer.getIssuer().getServer().get().getTextChannelsByName(arg)
                        : api.getServerTextChannelsByName(arg);
                if (channels.size() > 1) {
                    throw new InvalidCommandArgument(JavacordMessageKeys.TOO_MANY_CHANNELS_WITH_NAME, false);
                } else if (channels.size() == 1) {
                    channel = Optional.of(ACFUtil.getFirstElement(channels));
                }
            }
            if (!channel.isPresent()) {
                throw new InvalidCommandArgument(JavacordMessageKeys.COULD_NOT_FIND_CHANNEL, false);
            }
            return channel.get();
        });
        this.registerContext(ServerTextChannel.class, c -> {
            if (!c.issuer.getIssuer().getServer().isPresent()) {
                throw new InvalidCommandArgument(JavacordMessageKeys.SERVER_ONLY, false);
            }
            if (c.hasAnnotation(Author.class)) {
                //noinspection all
                return c.getIssuer().getChannel().asServerTextChannel().get();
            }
            boolean isCrossServer = c.hasAnnotation(CrossServer.class);
            String arg = c.popFirstArg();
            Optional<ServerTextChannel> channel = Optional.empty();
            if (arg.startsWith("<#")) {
                String id = arg.substring(2, arg.length() - 1);
                channel = (!isCrossServer && c.issuer.getIssuer().getServer().isPresent())
                        ? c.issuer.getIssuer().getServer().get().getTextChannelById(id)
                        : api.getServerTextChannelById(id);
            } else {
                Collection<ServerTextChannel> channels = (!isCrossServer && c.issuer.getIssuer().getServer().isPresent())
                        ? c.issuer.getIssuer().getServer().get().getTextChannelsByName(arg)
                        : api.getServerTextChannelsByName(arg);
                if (channels.size() > 1) {
                    throw new InvalidCommandArgument(JavacordMessageKeys.TOO_MANY_CHANNELS_WITH_NAME, false);
                } else if (channels.size() == 1) {
                    channel = Optional.of(ACFUtil.getFirstElement(channels));
                }
            }
            if (!channel.isPresent()) {
                throw new InvalidCommandArgument(JavacordMessageKeys.COULD_NOT_FIND_CHANNEL, false);
            }
            return channel.get();
        });
        this.registerContext(User.class, c -> {
            if (c.hasAnnotation(SelfUser.class)) {
                return api.getYourself();
            }

            String arg = c.isLastArg() ? String.join(" ", c.getArgs()) : c.popFirstArg();

            if (c.isOptional() && (arg == null || arg.isEmpty())) {
                return null;
            }
            User user = null;
            if (arg.startsWith("<@!")) {
                user = api.getUserById(arg.substring(3, arg.length() - 1)).join();
            } else if (arg.startsWith("<@")) {
                user = api.getUserById(arg.substring(2, arg.length() - 1)).join();
            } else {
                Collection<User> users = api.getCachedUsersByNameIgnoreCase(arg);
                if (users.size() > 1) {
                    throw new InvalidCommandArgument(JavacordMessageKeys.TOO_MANY_USERS_WITH_NAME, false);
                }
                if (!users.isEmpty()) {
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

            if (c.hasAnnotation(SelfUser.class)) {
                return new Member(api.getYourself(), c.issuer.getIssuer().getServer().get());
            }

            String arg = c.isLastArg() ? String.join(" ", c.getArgs()) : c.popFirstArg();

            if (c.isOptional() && (arg == null || arg.isEmpty())) {
                return null;
            }
            User user = null;
            if (arg.startsWith("<@!")) {
                user = api.getUserById(arg.substring(3, arg.length() - 1)).join();
            } else if (arg.startsWith("<@")) {
                user = api.getUserById(arg.substring(2, arg.length() - 1)).join();
            } else {
                Collection<User> users = api.getCachedUsersByNameIgnoreCase(arg);
                if (users.size() > 1) {
                    throw new InvalidCommandArgument(JavacordMessageKeys.TOO_MANY_USERS_WITH_NAME, false);
                }
                if (!users.isEmpty()) {
                    user = ACFUtil.getFirstElement(users);
                }
            }
            if (user == null) {
                throw new InvalidCommandArgument(JavacordMessageKeys.COULD_NOT_FIND_USER, false);
            }
            if (!c.issuer.getIssuer().getServer().get().isMember(user)) {
                throw new InvalidCommandArgument(JavacordMessageKeys.USER_NOT_MEMBER_OF_SERVER, false);
            }

            return new Member(user, c.issuer.getIssuer().getServer().get());
        });
        this.registerContext(Role.class, c -> {
            boolean isCrossServer = c.hasAnnotation(CrossServer.class);

            String arg = c.isLastArg() ? String.join(" ", c.getArgs()) : c.popFirstArg();

            Optional<Role> role = Optional.empty();
            if (DiscordRegexPattern.ROLE_MENTION.matcher(arg).matches()) {
                String id = arg.substring(3, arg.length() - 1);
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
                    List<Role> roles = (!isCrossServer && c.issuer.getIssuer().getServer().isPresent())
                            ? c.issuer.getIssuer().getServer().get().getRolesByNameIgnoreCase(arg)
                            : new ArrayList<>(api.getRolesByNameIgnoreCase(arg));

                    if (roles.size() > 1) {
                        throw new InvalidCommandArgument(JavacordMessageKeys.TOO_MANY_ROLES_WITH_NAME, false);
                    }
                    if (!roles.isEmpty()) {
                        role = Optional.of(roles.get(0));
                    }
                }
            }

            if (!role.isPresent()) {
                throw new InvalidCommandArgument(JavacordMessageKeys.COULD_NOT_FIND_ROLE, false);
            }
            return role.get();
        });
    }
}
