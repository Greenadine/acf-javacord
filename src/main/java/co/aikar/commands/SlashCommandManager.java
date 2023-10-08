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

import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.javacord.exception.SlashCommandRegistryException;
import com.google.common.base.Preconditions;
import org.javacord.api.DiscordApi;
import org.javacord.api.event.interaction.InteractionCreateEvent;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandInteractionOption;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@link CommandManager} implementation for Javacord for slash commands.
 *
 * @since 0.5.0
 * @see AbstractJavacordCommandManager
 * @see MessageCommandManager
 */
public class SlashCommandManager
        extends AbstractJavacordCommandManager<
                    SlashCommandEvent,
                    SlashCommandExecutionContext,
                    SlashCommandConditionContext>
{
    protected final ConcurrentHashMap<String, SlashCommandRegistry> commandRegistry;

    public SlashCommandManager(@NotNull DiscordApi api) {
        this(api, new JavacordOptions());
    }

    public SlashCommandManager(@NotNull DiscordApi api, @NotNull JavacordOptions options) {
        super(api, options);

        this.contexts = new SlashCommandContexts(this);
        this.configProvider = options.slashConfigProvider;
        this.commandRegistry = new ConcurrentHashMap<>();

        // Register slash command listener
        api.addSlashCommandCreateListener(new JavacordSlashCommandListener(this));
    }

    @Override
    public void registerCommand(BaseCommand command) {
        throw new UnsupportedOperationException("Use registerSlashCommand() instead, or use MessageCommandManager to register regular commands.");
    }

    @Override
    public boolean hasRegisteredCommands() {
        return !commands.isEmpty();
    }

    @Override
    public SlashCommandEvent getCommandIssuer(Object issuer) {
        if (!(issuer instanceof SlashCommandCreateEvent)) {
            throw new IllegalArgumentException("Issuer must be a InteractionCreateEvent");
        }
        return new SlashCommandEvent(this, (SlashCommandCreateEvent) issuer);
    }

    @Override
    public boolean isCommandIssuer(Class<?> type) {
        return SlashCommandCreateEvent.class.isAssignableFrom(type);
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
    public SlashCommandExecutionContext createCommandContext(RegisteredCommand command, CommandParameter parameter, CommandIssuer sender, List<String> args, int i, Map<String, Object> passedArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SlashRootCommand createRootCommand(String cmd) {
        return new SlashRootCommand(this, cmd);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public RegisteredCommand createRegisteredCommand(BaseCommand command, String cmdname, Method method, String prefSubcommand) {
        if (method.isAnnotationPresent(Subcommand.class)) {
            Preconditions.checkArgument(method.isAnnotationPresent(Description.class), "Subcommand is missing @Description annotation.");
        } else {
            if (method.isAnnotationPresent(CatchUnknown.class)) {
                throw new SlashCommandRegistryException("@CatchUnknown is not supported for slash commands.");
            }
            if (!method.isAnnotationPresent(Default.class)) {
                throw new SlashCommandRegistryException("Method is missing a @Subcommand or @Default annotation.");
            }
        }
        return new SlashRegisteredCommand(command, cmdname, method, prefSubcommand);
    }

    @SuppressWarnings("rawtypes")
    public SlashCommandExecutionContext createCommandContext(@NotNull SlashRegisteredCommand command, @NotNull CommandParameter parameter, @NotNull SlashCommandEvent event, @NotNull List<SlashCommandInteractionOption> args) {
        return new SlashCommandExecutionContext(command, parameter, event, args);
    }

    /**
     * Registers a {@link BaseCommand} as a slash command.
     *
     * @param command the command to register.
     */
    public void registerSlashCommand(BaseCommand command) {
        super.registerCommand(command);

        for (Map.Entry<String, RootCommand> entry : command.registeredCommands.entrySet()) {
            String commandName = entry.getKey().toLowerCase(Locale.ENGLISH);
            JavacordRootCommand rootCmd = (JavacordRootCommand) entry.getValue();
            SlashCommandNode rootNode = new SlashCommandNode(rootCmd, command);
            rootNode.register(this).thenAccept(registry -> {
                commandRegistry.put(rootCmd.getCommandName(), registry);
                if (!rootCmd.isRegistered) {
                    rootCmd.isRegistered = true;
                    commands.put(commandName, rootCmd);
                }
            }).exceptionally(throwable -> {
                log(LogLevel.ERROR, "Failed to register slash command '" + rootCmd.getCommandName() + "'.", throwable);
                return null;
            });
        }
    }

    /**
     * Dispatches a {@link InteractionCreateEvent} to the command manager.
     *
     * @param event the {@code InteractionCreateEvent} to dispatch.
     */
    void dispatchEvent(@NotNull SlashCommandCreateEvent event) {
        SlashCommandInteraction interaction = event.getSlashCommandInteraction();
        String[] cmdArr = ACFPatterns.SPACE.split(interaction.getFullCommandName());
        String cmd = cmdArr[0];
        String cmdArgs = cmdArr.length > 1 ? ACFUtil.join(cmdArr, 1) : " ";
        String[] args = ACFPatterns.SPACE.split(cmdArgs);

        executeRootCommand(event, cmd, args);
    }
}
