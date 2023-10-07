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
import org.jetbrains.annotations.NotNull;

/**
 * A {@link CommandManager} that handles both slash commands and message commands. It is a convenience class that
 * combines {@link MessageCommandManager} and {@link SlashCommandManager}.
 *
 * @since 0.5.0
 * @see AbstractJavacordCommandManager
 * @see MessageCommandManager
 * @see SlashCommandManager
 */
public class JavacordCommandManager {

    final MessageCommandManager messageCommandManager;
    final SlashCommandManager slashCommandManager;

    public JavacordCommandManager(@NotNull DiscordApi api) {
        this(api, new JavacordOptions());
    }

    public JavacordCommandManager(@NotNull DiscordApi api, @NotNull JavacordOptions options) {
        messageCommandManager = new MessageCommandManager(api, options);
        slashCommandManager = new SlashCommandManager(api, options);
    }

    /**
     * Gets the message command manager.
     *
     * @return the message command manager.
     */
    @NotNull
    public MessageCommandManager getMessageCommandManager() {
        return messageCommandManager;
    }

    /**
     * Gets the slash command manager.
     *
     * @return the slash command manager.
     */
    @NotNull
    public SlashCommandManager getSlashCommandManager() {
        return slashCommandManager;
    }

    /**
     * Registers a new command to its respective manager.
     *
     * @param command the command to register.
     */
    public void registerCommand(@NotNull BaseCommand command) {
        if (command instanceof SlashBaseCommand) {
            slashCommandManager.registerCommand(command);
        } else {
            messageCommandManager.registerCommand(command);
        }
    }

    /**
     * Adds command replacements to both managers.
     *
     * @param replacements the replacements to add.
     */
    public void addReplacements(@NotNull String... replacements) {
        slashCommandManager.getCommandReplacements().addReplacements(replacements);
        messageCommandManager.getCommandReplacements().addReplacements(replacements);
    }
}
