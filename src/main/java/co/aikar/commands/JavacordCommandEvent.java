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

import co.aikar.commands.javacord.context.Member;
import co.aikar.commands.javacord.util.JavacordEmbedBuilder;
import com.google.common.base.Preconditions;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a command event.
 *
 * @since 0.5.0
 * @see MessageCommandEvent
 * @see SlashCommandEvent
 */
@SuppressWarnings("rawtypes")
public abstract class JavacordCommandEvent implements CommandIssuer {

    protected final AbstractJavacordCommandManager manager;
    protected final User user;
    protected final Server server;
    protected final TextChannel channel;
    protected final boolean inServer;

    protected JavacordCommandEvent(@NotNull AbstractJavacordCommandManager manager, @NotNull User user, @Nullable Server server, @NotNull TextChannel channel) {
        this.manager = manager;
        this.user = user;
        this.server = server;
        this.channel = channel;
        this.inServer = server != null;
    }

    @Override
    @NotNull
    @SuppressWarnings("rawtypes")
    public CommandManager getManager() {
        return manager;
    }

    @Override
    public boolean isPlayer() {
        return false;
    }

    @Override
    public @NotNull UUID getUniqueId() {
        // Discord id only have 64 bit width (long) while UUIDs have twice the size.
        // In order to keep it unique we use 0L for the first 64 bit.
        long authorId = user.getId();
        return new UUID(0L, authorId);
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        PermissionResolver permissionResolver = this.manager.getPermissionResolver();
        //noinspection unchecked
        return permissionResolver == null || permissionResolver.hasPermission(manager, this, permission);
    }

    /**
     * Gets the {@link User} that invoked the command.
     *
     * @return the user that invoked the command.
     */
    @NotNull
    public User getUser() {
        return user;
    }

    /**
     * Gets whether the command was invoked by a bot or webhook instead of a regular user.
     *
     * @return {@code true} if the command was invoked by a bot or webhook, {@code false} if otherwise.
     */
    public boolean isBot() {
        return user.isBot();
    }

    /**
     * Gets the {@link Member} that invoked the command.
     *
     * @return the member that invoked the command.
     *
     * @throws IllegalStateException if the command was not invoked in a server.
     */
    @NotNull
    public Member getMember() {
        Preconditions.checkState(server != null, "Cannot get member from non-server event");
        return new Member(getUser(), server);
    }

    /**
     * Gets the {@link Server} in which the command was invoked.
     *
     * @return the server in which the command was invoked, or {@link Optional#empty()} if the command was not invoked in a server.
     */
    @NotNull
    public Optional<Server> getServer() {
        return Optional.ofNullable(server);
    }

    /**
     * Gets the {@link TextChannel} in which the command was invoked.
     *
     * @return the channel in which the command was invoked.
     */
    @NotNull
    public TextChannel getChannel() {
        return channel;
    }

    /**
     * Gets whether the command was invoked in a server.
     *
     * @return {@code true} if the command was invoked in a server, {@code false} if otherwise.
     */
    public boolean isInServer() {
        return inServer;
    }

    /**
     * Gets whether the command was invoked in a private channel.
     *
     * @return {@code true} if the command was invoked in a private channel, {@code false} if otherwise.
     */
    public boolean isInPrivate() {
        return !inServer;
    }

    /**
     * Send a message in the channel in which the command was invoked.
     *
     * @param message the message to send.
     */
    @NotNull
    public CompletableFuture<Message> reply(@NotNull String message) {
        return channel.sendMessage(message);
    }

    /**
     * Send an embed in the channel in which the command was invoked.
     *
     * @param embed the embed to send.
     */
    @NotNull
    public CompletableFuture<Message> reply(@NotNull EmbedBuilder embed) {
        return channel.sendMessage(embed);
    }

    /**
     * Send a message and embed in the channel in which the command was invoked.
     *
     * @param message the message to send.
     * @param embed the embed to send.
     */
    @NotNull
    public CompletableFuture<Message> reply(@NotNull String message, @NotNull EmbedBuilder embed) {
        return channel.sendMessage(message, embed);
    }

    /**
     * Send files in the channel in which the command was invoked.
     *
     * @param file the file(s) to send.
     */
    @NotNull
    public CompletableFuture<Message> reply(File... file) {
        return channel.sendMessage(file);
    }

    /**
     * Send an {@link InputStream} as a file with the given file name in the channel in which the command was invoked.
     *
     * @param is the {@code InputStream} to send as file.
     * @param fileName the name of the file.
     */
    @NotNull
    public CompletableFuture<Message> reply(@NotNull InputStream is, @NotNull String fileName) {
        return channel.sendMessage(is, fileName);
    }

    /**
     * Send a message in the channel in which the command was invoked.
     * Allows for formatting according to {@link String#format(String, Object...)}.
     *
     * @param message the message to send, with formatting.
     * @param replacements the replacement objects for formatting.
     */
    @NotNull
    public CompletableFuture<Message> replyf(@NotNull String message, Object... replacements) {
        return channel.sendMessage(String.format(message, replacements));
    }

    /**
     * Sends an embed in the channel in which the command was invoked, with the given description and the given color.
     *
     * @param color the color of the embed.
     * @param description the description of the embed.
     */
    @NotNull
    public CompletableFuture<Message> replyEmbed(@NotNull Color color, @NotNull String description) {
        return newEmbed().setColor(color).setDescription(description).send();
    }

    /**
     * Sends an embed in the channel in which the command was invoked,
     * with the given title, the given description and the given color.
     *
     * @param color the color of the embed.
     * @param title the title of the embed.
     * @param description the description of the embed.
     */
    @NotNull
    public CompletableFuture<Message> replyEmbed(@NotNull Color color, @NotNull String title, @NotNull String description) {
        return newEmbed().setColor(color).setTitle(title).setDescription(description).send();
    }

    /**
     * Sends an embed in the channel in which the command was invoked,
     * with the given user as author, the given description and the given color.
     *
     * @param color the color of the embed.
     * @param author the author of the embed.
     * @param description the description of the embed.
     */
    @NotNull
    public CompletableFuture<Message> replyEmbed(@NotNull Color color, @NotNull User author, @NotNull String description) {
        return newEmbed().setColor(color).setAuthor(author).setDescription(description).send();
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
    @NotNull
    public CompletableFuture<Message> replyEmbed(@NotNull Color color, @NotNull User author, @NotNull String title, @NotNull File image) {
        return newEmbed().setColor(color).setAuthor(author).setTitle(title).setImage(image).send();
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
    @NotNull
    public CompletableFuture<Message> replyEmbed(@NotNull Color color, @NotNull User author, @NotNull File image, @NotNull String description) {
        return newEmbed().setColor(color).setAuthor(author).setImage(image).setDescription(description).send();
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
    @NotNull
    public CompletableFuture<Message> replyEmbed(@NotNull Color color, @NotNull File authorIcon, @NotNull String author, String description) {
        return newEmbed().setColor(color).setAuthor(author, null, authorIcon).setDescription(description).send();
    }

    /**
     * Sends an embed in the channel in which the command was invoked, with the given description and the given color.
     * Allows for formatting according to {@link String#format(String, Object...)}.
     *
     * @param color the color of the embed.
     * @param description the message to send, with formatting.
     * @param replacements the replacement objects for formatting.
     */
    @NotNull
    public CompletableFuture<Message> replyfEmbed(@NotNull Color color, @NotNull String description, Object... replacements) {
        return newEmbed().setColor(color).setDescription(String.format(description, replacements)).send();
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
    @NotNull
    public CompletableFuture<Message> replyfEmbed(@NotNull Color color, @NotNull User author, @NotNull String description, Object... replacements) {
        return newEmbed().setColor(color).setAuthor(author).setDescription(String.format(description, replacements)).send();
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
    @NotNull
    public CompletableFuture<Message> replyfEmbed(@NotNull Color color, @NotNull File authorIcon, @NotNull String author, @NotNull String description, Object... replacements) {
        return newEmbed().setColor(color).setAuthor(author, null, authorIcon).setDescription(String.format(description, replacements)).send();
    }

    /**
     * Creates a new {@link JavacordEmbedBuilder} for building a new embed within the context.
     *
     * @return the new {@link JavacordEmbedBuilder}.
     *
     * @throws IllegalStateException if the command was invoked in a private conversation.
     */
    @NotNull
    public JavacordEmbedBuilder newEmbed() {
        return JavacordEmbedBuilder.forChannel(channel);
    }
}
