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

import com.google.common.base.Preconditions;
import org.javacord.api.DiscordApi;
import org.javacord.api.event.interaction.InteractionCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandInteractionOption;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.*;

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

    public SlashCommandManager(@NotNull DiscordApi api) {
        this(api, new JavacordOptions());
    }

    public SlashCommandManager(@NotNull DiscordApi api, @NotNull JavacordOptions options) {
        super(api, options);

        this.contexts = new SlashCommandContexts(this);
        this.configProvider = options.slashConfigProvider;

        // Register slash command listener
        api.addInteractionCreateListener(new JavacordInteractionListener(this));
    }

    @Override
    public void registerCommand(BaseCommand command) {
        Preconditions.checkState(command instanceof SlashBaseCommand, "Command must be a SlashBaseCommand");
        super.registerCommand(command);
    }

    @Override
    public boolean hasRegisteredCommands() {
        return !commands.isEmpty();
    }

    @Override
    public SlashCommandEvent getCommandIssuer(Object issuer) {
        if (!(issuer instanceof InteractionCreateEvent)) {
            throw new IllegalArgumentException("Issuer must be a InteractionCreateEvent");
        }
        return new SlashCommandEvent(this, (InteractionCreateEvent) issuer);
    }

    @Override
    public boolean isCommandIssuer(Class<?> type) {
        return InteractionCreateEvent.class.isAssignableFrom(type);
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
        return new SlashRegisteredCommand(command, cmdname, method, prefSubcommand);
    }

    public SlashCommandExecutionContext createCommandContext(@NotNull SlashRegisteredCommand command, @NotNull CommandParameter parameter, @NotNull SlashCommandEvent event, @NotNull List<SlashCommandInteractionOption> args) {
        return new SlashCommandExecutionContext(command, parameter, event, args);
    }

    /**
     * Dispatches a {@link InteractionCreateEvent} to the command manager.
     *
     * @param event the {@code InteractionCreateEvent} to dispatch.
     */
    void dispatchEvent(@NotNull InteractionCreateEvent event) {
        //noinspection OptionalGetWithoutIsPresent
        SlashCommandInteraction interaction = event.getSlashCommandInteraction().get();
        String[] cmdArr = ACFPatterns.SPACE.split(interaction.getFullCommandName());
        String cmd = cmdArr[0];
        String cmdArgs = cmdArr.length > 1 ? ACFUtil.join(cmdArr, 1) : " ";

        String[] args = ACFPatterns.SPACE.split(cmdArgs);

        if (args.length == 0) {
            return;
        }
        executeRootCommand(event, cmd, args);
    }
}
