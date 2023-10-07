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

import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;

/**
 * @since 0.5.0
 */
public class JavacordSlashCommandListener implements SlashCommandCreateListener {

    private final SlashCommandManager manager;

    JavacordSlashCommandListener(SlashCommandManager manager) {
        this.manager = manager;
    }

    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        // Check if the command is from the bots instance
        if (event.getSlashCommandInteraction().getApplicationId() != manager.getApi().getClientId()) {
            return;
        }

        this.manager.dispatchEvent(event);
    }
}
