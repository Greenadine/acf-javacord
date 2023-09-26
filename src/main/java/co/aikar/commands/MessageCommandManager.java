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

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.MessageCreateEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A {@link CommandManager} implementation for Javacord for message commands.
 *
 * @since 0.5.0
 * @see AbstractJavacordCommandManager
 * @see SlashCommandManager
 */
public class MessageCommandManager
        extends AbstractJavacordCommandManager<
                    MessageCommandEvent,
                    MessageCommandExecutionContext,
                    MessageCommandConditionContext>
{

    public MessageCommandManager(@NotNull DiscordApi api) {
        this(api, new JavacordOptions());
    }

    public MessageCommandManager(@NotNull DiscordApi api, JavacordOptions options) {
        super(api, options);

        this.contexts = new MessageCommandContexts(this);
        this.configProvider = options.messageConfigProvider;

        // Register message listener
        api.addMessageCreateListener(new JavacordMessageListener(this));
    }

    @Override
    public boolean hasRegisteredCommands() {
        return !commands.isEmpty();
    }

    @Override
    public MessageCommandEvent getCommandIssuer(Object issuer) {
        if (!(issuer instanceof MessageCreateEvent)) {
            throw new IllegalArgumentException("Issuer must be a MessageCreateEvent");
        }
        return new MessageCommandEvent(this, (MessageCreateEvent) issuer);
    }

    @Override
    public boolean isCommandIssuer(Class<?> type) {
        return MessageCommandEvent.class.isAssignableFrom(type);
    }

    @Override
    public CommandCompletions<?> getCommandCompletions() {
        return null;
    }

    @Override
    public Collection<RootCommand> getRegisteredRootCommands() {
        return Collections.unmodifiableCollection(commands.values());
    }

    @Override
    public MessageCommandExecutionContext createCommandContext(RegisteredCommand command, CommandParameter parameter, CommandIssuer sender, List<String> args, int i, Map<String, Object> passedArgs) {
        return new MessageCommandExecutionContext(command, parameter, (MessageCommandEvent) sender, args, i, passedArgs);
    }

    /**
     * Dispatches a {@link MessageCreateEvent} to the command manager.
     *
     * @param event the {@code MessageCreateEvent} to dispatch.
     */
    void dispatchEvent(@NotNull MessageCreateEvent event) {
        Message message = event.getMessage();
        String msg = message.getContent();

        CommandConfig config = getCommandConfig(event);

        String prefixFound = null;
        for (String prefix : config.getCommandPrefixes()) {
            if (msg.startsWith(prefix)) {
                prefixFound = prefix;
                break;
            }
        }
        if (prefixFound == null) {
            return;
        }

        String[] args = ACFPatterns.SPACE.split(msg.substring(prefixFound.length()), -1);
        if (args.length == 0) {
            return;
        }
        String cmd = args[0].toLowerCase(Locale.ENGLISH);
        executeRootCommand(event, cmd, args);
    }

    /**
     * Gets the command configuration for the specified {@link MessageCreateEvent}.
     *
     * @param event the {@code MessageCreateEvent} to get the command configuration for.
     *
     *
     * @return the command configuration.
     */
    private CommandConfig getCommandConfig(@NotNull MessageCreateEvent event) {
        CommandConfig config = this.defaultConfig;
        if (this.configProvider != null) {
            CommandConfig provider = this.configProvider.provide(event);
            if (provider != null) {
                config = provider;
            }
        }
        return config;
    }
}
