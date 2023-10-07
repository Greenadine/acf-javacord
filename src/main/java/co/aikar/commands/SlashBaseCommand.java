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
import org.jetbrains.annotations.NotNull;

/**
 * A {@code BaseSlashCommand} is defined as a command group of related slash commands.
 * <p>
 * It is up to the end user how to organize their command.
 * You should use 1 base command per command in your application.
 * <p>
 * Optionally (and encouraged), you can use the base command to represent a root command,
 * and then each actionable command is a subcommand.
 *
 * @since 0.5.0
 * @see BaseCommand
 */
public class SlashBaseCommand extends BaseCommand {

    SlashCommandManager manager;

    @Override
    void onRegister(@NotNull CommandManager m) {
        Preconditions.checkState(m instanceof SlashCommandManager, "Slash commands can only be registered with a SlashCommandManager.");

        this.manager = (SlashCommandManager) m;
        super.onRegister(m);

        for (RootCommand cmd : registeredCommands.values()) {
            SlashCommandNode root = new SlashCommandNode(cmd, this);
            root.register(manager)
                    .thenAccept(registry -> {
                        manager.commandRegistry.put(cmd.getCommandName(), registry);
                        manager.log(LogLevel.INFO, "Registered slash command '" + cmd.getCommandName() + "'.");
                    })
                    .exceptionally(throwable -> {
                            manager.log(LogLevel.ERROR, "Failed to register slash command '" + cmd.getCommandName() + "'.", throwable);
                            return null;
                    });
        }
    }
}
