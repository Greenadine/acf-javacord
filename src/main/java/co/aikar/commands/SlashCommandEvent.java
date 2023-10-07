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
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionFollowupMessageBuilder;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a command event that was triggered by a slash command.
 *
 * @since 0.5.0
 * @see JavacordCommandEvent
 * @see MessageCommandEvent
 */
public class SlashCommandEvent extends JavacordCommandEvent {

    private final SlashCommandCreateEvent event;

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public SlashCommandEvent(@NotNull SlashCommandManager manager, @NotNull SlashCommandCreateEvent event) {
        super(manager, event.getInteraction().getUser(), event.getInteraction().getServer().orElse(null), event.getInteraction().getChannel().get());

        this.event = event;
    }

    @Override
    @NotNull
    public SlashCommandManager getManager() {
        return (SlashCommandManager) manager;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SlashCommandCreateEvent getIssuer() {
        return event;
    }

    @Override
    public void sendMessageInternal(String message) {
        newImmediateResponse().setContent(message).respond();
    }

    /**
     * Returns the {@link SlashCommandInteraction} that triggered this command.
     *
     * @return the {@link SlashCommandInteraction} that triggered this command.
     */
    @NotNull
    public SlashCommandInteraction getInteraction() {
        return event.getSlashCommandInteraction();
    }

    /**
     * Acknowledges the interaction. This will confirm to the user that the interaction was received,
     * without doing anything else. This cam be useful for commands that take a long time to process, or when you plan
     * on handling the command without using Discord's built-in response system.
     */
    public void confirm() {
        newImmediateResponse().respond();
    }

    /**
     * Creates and returns a new {@link InteractionImmediateResponseBuilder}.
     *
     * @return the new {@link InteractionImmediateResponseBuilder}.
     *
     * @throws IllegalStateException if the command was not invoked from an interaction.
     */
    @NotNull
    public InteractionImmediateResponseBuilder newImmediateResponse() {
        return getInteraction().createImmediateResponder();
    }


    /**
     * Creates and returns a new {@link InteractionFollowupMessageBuilder}.
     *
     * @return the new {@link InteractionFollowupMessageBuilder}.
     *
     * @throws IllegalStateException if the command was not invoked from an interaction.
     */
    @NotNull
    public InteractionFollowupMessageBuilder newFollowupMessage() {
        return getInteraction().createFollowupMessageBuilder();
    }
}
