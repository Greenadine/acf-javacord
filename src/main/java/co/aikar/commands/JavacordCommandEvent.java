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

import co.aikar.commands.javacord.util.JavacordEmbedBuilder;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class JavacordCommandEvent implements CommandIssuer {

    private MessageCreateEvent event;
    private JavacordCommandManager manager;

    public JavacordCommandEvent(JavacordCommandManager manager, MessageCreateEvent event) {
        this.manager = manager;
        this.event = event;
    }

    @Override
    public MessageCreateEvent getIssuer() {
        return event;
    }

    @Override
    public CommandManager getManager() {
        return this.manager;
    }

    @Override
    public boolean isPlayer() {
        return false;
    }

    @Override
    public @NotNull UUID getUniqueId() {
        // Discord id only have 64 bit width (long) while UUIDs have twice the size.
        // In order to keep it unique we use 0L for the first 64 bit.
        long authorId = event.getMessageAuthor().getId();
        return new UUID(0, authorId);
    }

    @Override
    public boolean hasPermission(String permission) {
        CommandPermissionResolver permissionResolver = this.manager.getPermissionResolver();
        return permissionResolver == null || permissionResolver.hasPermission(manager, this, permission);
    }

    @Override
    public void sendMessageInternal(String message) {
        this.event.getChannel().sendMessage(message);
    }

    /**
     * Send a message in the channel in which the command was invoked.
     *
     * @param message the message to send.
     */
    public CompletableFuture<Message> reply(String message) {
        return getChannel().sendMessage(message);
    }

    /**
     * Send an embed in the channel in which the command was invoked.
     *
     * @param embed the embed to send.
     */
    public CompletableFuture<Message> reply(EmbedBuilder embed) {
        return getChannel().sendMessage(embed);
    }

    /**
     * Send a message and embed in the channel in which the command was invoked.
     *
     * @param message the message to send.
     * @param embed the embed to send.
     */
    public CompletableFuture<Message> reply(String message, EmbedBuilder embed) {
        return getChannel().sendMessage(message, embed);
    }

    /**
     * Send files in the channel in which the command was invoked.
     *
     * @param file the file(s) to send.
     */
    public CompletableFuture<Message> reply(File... file) {
        return getChannel().sendMessage(file);
    }

    /**
     * Send an {@link InputStream} as a file with the given file name in the channel in which the command was invoked.
     *
     * @param is the {@code InputStream} to send as file.
     * @param fileName the name of the file.
     */
    public CompletableFuture<Message> reply(InputStream is, String fileName) {
        return getChannel().sendMessage(is, fileName);
    }

    /**
     * Sends an embed in the channel in which the command was invoked, with the given description and the given color.
     * @param description the description of the embed.
     * @param color the color of the embed.
     */
    public CompletableFuture<Message> replyEmbed(String description, Color color) {
        return newEmbed().setDescription(description).setColor(color).send();
    }

    /**
     * Sends an embed in the channel in which the command was invoked,
     * with the given title, the given description and the given color.
     *
     * @param title the title of the embed.
     * @param description the description of the embed.
     * @param color the color of the embed.
     */
    public CompletableFuture<Message> replyEmbed(String title, String description, Color color) {
        return newEmbed().setTitle(title).setDescription(description).setColor(color).send();
    }

    /**
     * Sends an embed in the channel in which the command was invoked,
     * with the given user as author, the given description and the given color.
     *
     * @param author the author of the embed.
     * @param description the description of the embed.
     * @param color the color of the embed.
     */
    public CompletableFuture<Message> replyEmbed(User author, String description, Color color) {
        return newEmbed().setAuthor(author).setDescription(description).setColor(color).send();
    }

    /**
     * Sends an embed in the channel in which the command was invoked,
     * with the given user as author, the given title, the given image and the given color.
     *
     * @param author the author of the embed.
     * @param title the title of the embed.
     * @param image the image included in the embed.
     * @param color the color of the embed.
     */
    public CompletableFuture<Message> replyEmbed(User author, String title, File image, Color color) {
        return newEmbed().setAuthor(author).setTitle(title).setImage(image).setColor(color).send();
    }

    /**
     * Sends an embed in the channel in which the command was invoked,
     * with the given user as author, the given image, the given description and the given color.
     *
     * @param author the author of the embed.
     * @param image the image included in the embed.
     * @param description the description of the embed.
     * @param color the color of the embed.
     */
    public CompletableFuture<Message> replyEmbed(User author, File image, String description, Color color) {
        return newEmbed().setAuthor(author).setImage(image).setDescription(description).setColor(color).send();
    }

    /**
     * Sends an embed in the channel in which the command was invoked,
     * with the given icon and string as author, the given description and the given color.
     *
     * @param icon the icon of the author.
     * @param author the name of the author.
     * @param description the description of the embed.
     * @param color the color of the embed.
     */
    public CompletableFuture<Message> replyEmbed(File icon, String author, String description, Color color) {
        return newEmbed().setAuthor(author, null, icon).setDescription(description).setColor(color).send();
    }

    /**
     * Creates a new {@link JavacordEmbedBuilder} for building a new embed within the context.
     *
     * @return the new {@code JavacordEmbedBuilder}.
     */
    public JavacordEmbedBuilder newEmbed() {
        return JavacordEmbedBuilder.forChannel(getChannel());
    }

    /**
     * Delete the message that was sent.
     */
    public CompletableFuture<Void> deleteMessage() {
        return getMessage().delete();
    }

    /**
     * Gets the {@link TextChannel} in which the command was invoked.
     *
     * @return the channel in which the command was invoked.
     */
    public TextChannel getChannel() {
        return event.getChannel();
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
     * Gets the {@link MessageCreateEvent}.
     *
     * @return the {@code MessageCreateEvent}.
     */
    public MessageCreateEvent getEvent() {
        return event;
    }

    /**
     * Returns whether the command issuer is a bot.
     *
     * @return {@code true} if the command issuer is a bot, {@code false} otherwise.
     */
    public boolean isBot() {
        if (!getEvent().getMessageAuthor().isUser()) return false;
        return getEvent().getMessageAuthor().asUser().get().isBot();
    }
}
