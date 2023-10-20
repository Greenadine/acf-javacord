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

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionFollowupMessageBuilder;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
     * Creates and returns a new {@link InteractionImmediateResponseBuilder}.
     *
     * @return the new {@link InteractionImmediateResponseBuilder}.
     */
    @NotNull
    public InteractionImmediateResponseBuilder newImmediateResponse() {
        return getInteraction().createImmediateResponder();
    }

    /**
     * Creates and returns a new {@link InteractionFollowupMessageBuilder}.
     *
     * @return the new {@link InteractionFollowupMessageBuilder}.
     */
    @NotNull
    public InteractionFollowupMessageBuilder newFollowupMessage() {
        return getInteraction().createFollowupMessageBuilder();
    }

    /**
     * Acknowledges the interaction. This will confirm to Discord that the interaction was received, without doing
     * anything else. This can be useful for commands that take a long time to process, or when you plan on handling
     * the command without using Discord's built-in interaction response system (e.g. by using {@link #reply(String)}
     * instead).
     */
    public void confirm() {
        newImmediateResponse().respond();
    }

    /**
     * Responds to the interaction with a message.
     *
     * @param message the message to respond with.
     */
    public void respond(@NotNull String message) {
        newImmediateResponse().setContent(message).respond();
    }

    /**
     * Responds to the interaction with an embed.
     *
     * @param embed the embed to respond with.
     */
    public void respond(@NotNull EmbedBuilder embed) {
        newImmediateResponse().addEmbed(embed).respond();
    }

    /**
     * Responds to the interaction with multiple embeds.
     *
     * @param embeds the embeds to respond with.
     */
    public void respond(@NotNull EmbedBuilder... embeds) {
        newImmediateResponse().addEmbeds(embeds).respond();
    }

    /**
     * Responds to the interaction with multiple embeds.
     *
     * @param embeds a list of embeds to respond with.
     */
    public void respond(@NotNull List<EmbedBuilder> embeds) {
        newImmediateResponse().addEmbeds(embeds).respond();
    }

    /**
     * Responds to the interaction with a message and an embed.
     *
     * @param message the message to respond with.
     * @param embed the embed to respond with.
     */
    public void respond(@NotNull String message, @NotNull EmbedBuilder embed) {
        newImmediateResponse().setContent(message).addEmbed(embed).respond();
    }

    /**
     * Responds to the interaction with a message and multiple embeds.
     *
     * @param message the message to respond with.
     * @param embeds the embeds to respond with.
     */
    public void respond(@NotNull String message, @NotNull EmbedBuilder... embeds) {
        newImmediateResponse().setContent(message).addEmbeds(embeds).respond();
    }

    /**
     * Responds to the interaction with a message and multiple embeds.
     *
     * @param message the message to respond with.
     * @param embeds a list of embeds to respond with.
     */
    public void respond(@NotNull String message, @NotNull List<EmbedBuilder> embeds) {
        newImmediateResponse().setContent(message).addEmbeds(embeds).respond();
    }

    /**
     * Responds to the interaction with a formatted message.
     *
     * @param message the message to respond with.
     * @param replacements the replacements to apply to the message.
     *
     * @see String#format(String, Object...)
     */
    public void respondf(@NotNull String message, @NotNull Object... replacements) {
        respond(String.format(message, replacements));
    }

    /**
     * Responds to the interaction with a formatted message and an embed.
     *
     * @param embed the embed to respond with.
     * @param message the message to respond with.
     * @param replacements the replacements to apply to the message.
     */
    public void respondf(@NotNull EmbedBuilder embed, @NotNull String message, @NotNull Object... replacements) {
        respond(String.format(message, replacements), embed);
    }
}
