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

import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.MessageCreateEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Represents a command event that was triggered by a message.
 *
 * @since 0.5.0
 * @see JavacordCommandEvent
 * @see SlashCommandEvent
 */
public class MessageCommandEvent extends JavacordCommandEvent {

    private final MessageCreateEvent event;

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    protected MessageCommandEvent(@NotNull MessageCommandManager manager, @NotNull MessageCreateEvent event) {
        super(manager, event.getMessageAuthor().asUser().get(), event.getServer().orElse(null), event.getChannel());

        this.event = event;
    }

    @Override
    @NotNull
    @SuppressWarnings("unchecked")
    public MessageCreateEvent getIssuer() {
        return event;
    }

    @Override
    public boolean isInServer() {
        return event.isServerMessage();
    }

    @Override
    public boolean isInPrivate() {
        return event.isPrivateMessage();
    }

    /**
     * Gets the message that was sent.
     *
     * @return the message that was sent.
     */
    public Message getMessage() {
        return event.getMessage();
    }

    /**
     * Delete the message that was sent.
     */
    @NotNull
    public CompletableFuture<Void> deleteMessage() {
        return getMessage().delete();
    }
}
