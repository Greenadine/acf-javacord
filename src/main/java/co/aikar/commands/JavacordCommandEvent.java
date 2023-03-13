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

import co.aikar.commands.javacord.contexts.Member;
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

/**
 * @since 0.1
 * @author Greenadine
 */
@SuppressWarnings("OptionalGetWithoutIsPresent,unused")
public class JavacordCommandEvent implements CommandIssuer {

    private final MessageCreateEvent event;
    private final JavacordCommandManager manager;

    public JavacordCommandEvent(JavacordCommandManager manager, MessageCreateEvent event) {
        this.manager = manager;
        this.event = event;
    }

    @Override
    @SuppressWarnings("unchecked")
    public MessageCreateEvent getIssuer() {
        return event;
    }

    @Override
    @SuppressWarnings("all")
    public CommandManager getManager() {
        return this.manager;
    }

    public String getExecCommandLabel() {
        return event.getMessage().getContent().trim().split("\\s+")[0].substring(manager.getCommandPrefix(this).length());
    }

    public User getUser() {
        return event.getMessageAuthor().asUser().get();
    }

    public Member getMember() {
        return new Member(getUser(), event.getServer().get());
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
        return new UUID(0L, authorId);
    }

    @Override
    public boolean hasPermission(String permission) {
        CommandPermissionResolver permissionResolver = this.manager.getPermissionResolver();
        return permissionResolver == null || permissionResolver.hasPermission(manager, this, permission);
    }

    @Override
    public void sendMessageInternal(String message) {
        this.replyEmbed(Color.RED, message);
    }

    /**
     * Send a message in the channel in which the command was invoked.
     *
     * @param message the message to send.
     */
    public @NotNull CompletableFuture<Message> reply(@NotNull String message) {
        return getChannel().sendMessage(message);
    }

    /**
     * Send an embed in the channel in which the command was invoked.
     *
     * @param embed the embed to send.
     */
    public @NotNull CompletableFuture<Message> reply(@NotNull EmbedBuilder embed) {
        return getChannel().sendMessage(embed);
    }

    /**
     * Send a message and embed in the channel in which the command was invoked.
     *
     * @param message the message to send.
     * @param embed the embed to send.
     */
    public @NotNull CompletableFuture<Message> reply(@NotNull String message, @NotNull EmbedBuilder embed) {
        return getChannel().sendMessage(message, embed);
    }

    /**
     * Send files in the channel in which the command was invoked.
     *
     * @param file the file(s) to send.
     */
    public @NotNull CompletableFuture<Message> reply(File... file) {
        return getChannel().sendMessage(file);
    }

    /**
     * Send an {@link InputStream} as a file with the given file name in the channel in which the command was invoked.
     *
     * @param is the {@code InputStream} to send as file.
     * @param fileName the name of the file.
     */
    public @NotNull CompletableFuture<Message> reply(@NotNull InputStream is, @NotNull String fileName) {
        return getChannel().sendMessage(is, fileName);
    }

    /**
     * Send a message in the channel in which the command was invoked.
     * Allows for formatting according to {@link String#format(String, Object...)}.
     *
     * @param message the message to send, with formatting.
     * @param replacements the replacement objects for formatting.
     */
    public @NotNull CompletableFuture<Message> replyf(@NotNull String message, Object... replacements) {
        return getChannel().sendMessage(String.format(message, replacements));
    }

    /**
     * Sends an embed in the channel in which the command was invoked, with the given description and the given color.
     *
     * @param color the color of the embed.
     * @param description the description of the embed.
     */
    public @NotNull CompletableFuture<Message> replyEmbed(@NotNull Color color, @NotNull String description) {
        return embedBuilder().setColor(color).setDescription(description).send();
    }

    /**
     * Sends an embed in the channel in which the command was invoked,
     * with the given title, the given description and the given color.
     *
     * @param color the color of the embed.
     * @param title the title of the embed.
     * @param description the description of the embed.
     */
    public @NotNull CompletableFuture<Message> replyEmbed(@NotNull Color color, @NotNull String title, @NotNull String description) {
        return embedBuilder().setColor(color).setTitle(title).setDescription(description).send();
    }

    /**
     * Sends an embed in the channel in which the command was invoked,
     * with the given user as author, the given description and the given color.
     *
     * @param color the color of the embed.
     * @param author the author of the embed.
     * @param description the description of the embed.
     */
    public @NotNull CompletableFuture<Message> replyEmbed(@NotNull Color color, @NotNull User author, @NotNull String description) {
        return embedBuilder().setColor(color).setAuthor(author).setDescription(description).send();
    }

    /**
     * Sends an embed in the channel in which the command was invoked,
     * with the given user as author, the given title, the given image and the given color.
     *
     * @param color the color of the embed.
     * @param author the author of the embed.
     * @param title the title of the embed.
     * @param image the image included in the embed.
     */
    public @NotNull CompletableFuture<Message> replyEmbed(@NotNull Color color, @NotNull User author, @NotNull String title, @NotNull File image) {
        return embedBuilder().setColor(color).setAuthor(author).setTitle(title).setImage(image).send();
    }

    /**
     * Sends an embed in the channel in which the command was invoked,
     * with the given user as author, the given image, the given description and the given color.
     *
     * @param color the color of the embed.
     * @param author the author of the embed.
     * @param image the image included in the embed.
     * @param description the description of the embed.
     */
    public @NotNull CompletableFuture<Message> replyEmbed(@NotNull Color color, @NotNull User author, @NotNull File image, @NotNull String description) {
        return embedBuilder().setColor(color).setAuthor(author).setImage(image).setDescription(description).send();
    }

    /**
     * Sends an embed in the channel in which the command was invoked,
     * with the given icon and string as author, the given description and the given color.
     *
     * @param color the color of the embed.
     * @param authorIcon the icon of the author.
     * @param author the name of the author.
     * @param description the description of the embed.
     */
    public @NotNull CompletableFuture<Message> replyEmbed(@NotNull Color color, @NotNull File authorIcon, @NotNull String author, String description) {
        return embedBuilder().setColor(color).setAuthor(author, null, authorIcon).setDescription(description).send();
    }

    /**
     * Sends an embed in the channel in which the command was invoked, with the given description and the given color.
     * Allows for formatting according to {@link String#format(String, Object...)}.
     *
     * @param color the color of the embed.
     * @param description the message to send, with formatting.
     * @param replacements the replacement objects for formatting.
     */
    public @NotNull CompletableFuture<Message> replyfEmbed(@NotNull Color color, @NotNull String description, Object... replacements) {
        return embedBuilder().setColor(color).setDescription(String.format(description, replacements)).send();
    }

    /**
     * Sends an embed in the channel in which the command was invoked, with the given color, author and description.
     * Allows for formatting according to {@link String#format(String, Object...)}.
     *
     * @param color the color of the embed.
     * @param author the author of the embed.
     * @param description the description of the embed.
     * @param replacements the replacement objects for formatting.
     */
    public @NotNull CompletableFuture<Message> replyfEmbed(@NotNull Color color, @NotNull User author, @NotNull String description, Object... replacements) {
        return embedBuilder().setColor(color).setAuthor(author).setDescription(String.format(description, replacements)).send();
    }

    /**
     * Sends an embed in the channel in which the command was invoked, with the given color, author and description.
     *
     * @param color the color of the embed.
     * @param authorIcon the icon of the author.
     * @param author the name of the author.
     * @param description the description of the embed.
     * @param replacements the replacement objects for formatting.
     */
    public @NotNull CompletableFuture<Message> replyfEmbed(@NotNull Color color, @NotNull File authorIcon, @NotNull String author, @NotNull String description, Object... replacements) {
        return embedBuilder().setColor(color).setAuthor(author, null, authorIcon).setDescription(String.format(description, replacements)).send();
    }

    // TODO message builder
//    /**
//     * Creates a new {@link JavacordMessageBuilder} for building a new message within the context.
//     *
//     * @return the new {@link JavacordMessageBuilder}.
//     */
//    public @NotNull JavacordMessageBuilder messageBuilder() {
//        return JavacordMessageBuilder.forChannel(getChannel());
//    }

    /**
     * Creates a new {@link JavacordEmbedBuilder} for building a new embed within the context.
     *
     * @return The new {@link JavacordEmbedBuilder}.
     */
    public @NotNull JavacordEmbedBuilder newEmbed() {
        return JavacordEmbedBuilder.forChannel(getChannel());
    }

    /**
     * Creates a new {@link JavacordEmbedBuilder} for building a new embed within the context.
     *
     * @return The new {@link JavacordEmbedBuilder}.
     *
     * @deprecated In favor of {@link #newEmbed()}.
     */
    @Deprecated
    public @NotNull JavacordEmbedBuilder embedBuilder() {
        return JavacordEmbedBuilder.forChannel(getChannel());
    }

    /**
     * Delete the message that was sent.
     */
    public @NotNull CompletableFuture<Void> deleteMessage() {
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

    /**
     * Returns whether the command issuer is a human.
     *
     * @return {@code true} if the command issuer is a human, {@code false} otherwise.
     */
    public boolean isHuman() {
        return !isBot();
    }

    /**
     * Returns whether the command was issued from within a server.
     *
     * @return {@code true} if the command was issued from within a server, {@code false} otherwise.
     */
    public boolean isInServer() {
        return event.isServerMessage();
    }

    /**
     * Returns whether the command was issued from within a private conversation between the bot and a user.
     *
     * @return {@code true} if the command was issued from within a private conversation, {@code false} otherwise.
     */
    public boolean isInPrivate() {
        return event.isPrivateMessage();
    }
}
