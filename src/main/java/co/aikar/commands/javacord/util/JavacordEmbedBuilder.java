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

package co.aikar.commands.javacord.util;

import org.javacord.api.entity.Icon;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EditableEmbedField;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.message.embed.EmbedField;
import org.javacord.api.entity.user.User;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class JavacordEmbedBuilder {

    private final TextChannel channel;
    private final EmbedBuilder embed;

    private JavacordEmbedBuilder(TextChannel channel) {
        this.channel = channel;
        embed = new EmbedBuilder();
    }

    public static JavacordEmbedBuilder forChannel(@NotNull TextChannel channel) {
        return new JavacordEmbedBuilder(channel);
    }

    /**
     * Send the embed to the channel.
     */
    public CompletableFuture<Message> send() {
        return channel.sendMessage(embed);
    }

    /**
     * Sets the title of the embed.
     *
     * @param title the title of the embed.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setTitle(@NotNull String title) {
        embed.setTitle(title);
        return this;
    }

    /**
     * Sets the description of the embed.
     *
     * @param description the description of the embed.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setDescription(@NotNull String description) {
        embed.setDescription(description);
        return this;
    }

    /**
     * Sets the url of the embed.
     *
     * @param url the url of the embed.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setUrl(@NotNull String url) {
        embed.setUrl(url);
        return this;
    }

    /**
     * Sets the current time as timestamp of the embed.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setTimestampToNow() {
        embed.setTimestampToNow();
        return this;
    }

    /**
     * Sets the timestamp of the embed.
     *
     * @param timestamp the timestamp to set.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setTimestamp(@NotNull Instant timestamp) {
        embed.setTimestamp(timestamp);
        return this;
    }

    /**
     * Sets the color of the embed.
     *
     * @param color the color of the embed.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setColor(@NotNull Color color) {
        embed.setColor(color);
        return this;
    }

    /**
     * Sets the footer of the embed.
     *
     * @param text the text of the footer.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setFooter(@NotNull String text) {
        embed.setFooter(text);
        return this;
    }

    /**
     * Sets the footer of the embed.
     *
     * @param text the text of the footer.
     * @param iconUrl the url of the footer's icon.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setFooter(@NotNull String text, @NotNull String iconUrl) {
        embed.setFooter(text, iconUrl);
        return this;
    }

    /**
     * Sets the footer of the embed.
     *
     * @param text the text of the footer.
     * @param icon the footer's icon.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setFooter(@NotNull String text, @NotNull Icon icon) {
        embed.setFooter(text, icon);
        return this;
    }

    /**
     * Sets the footer of the embed.
     *
     * @param text the text of the footer.
     * @param icon the footer's icon.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setFooter(@NotNull String text, @NotNull File icon) {
        embed.setFooter(text, icon);
        return this;
    }

    /**
     * Sets the footer of the embed.
     * This method assumes the file type is "png"!
     *
     * @param text the text of the footer.
     * @param icon the footer's icon.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setFooter(@NotNull String text, @NotNull InputStream icon) {
        embed.setFooter(text, icon);
        return this;
    }

    /**
     * Sets the footer of the embed.
     *
     * @param text the text of the footer.
     * @param icon the footer's icon.
     * @param fileType the type of the file, e.g. "png" or "gif".
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setFooter(@NotNull String text, @NotNull InputStream icon, @NotNull String fileType) {
        embed.setFooter(text, icon, fileType);
        return this;
    }

    /**
     * Sets the footer of the embed.
     * This method assumes the file type is "png"!
     *
     * @param text the text of the footer.
     * @param icon the footer's icon.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setFooter(@NotNull String text, byte[] icon) {
        embed.setFooter(text, icon);
        return this;
    }

    /**
     * Sets the footer of the embed.
     *
     * @param text the text of the footer.
     * @param icon the footer's icon.
     * @param fileType the type of the file, e.g. "png" or "gif".
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setFooter(@NotNull String text, byte[] icon, @NotNull String fileType) {
        embed.setFooter(text, icon, fileType);
        return this;
    }

    /**
     * Sets the footer of the embed.
     * This method assumes the file type is "png"!
     *
     * @param text the text of the footer.
     * @param icon the footer's icon.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setFooter(@NotNull String text, @NotNull BufferedImage icon) {
        embed.setFooter(text, icon);
        return this;
    }

    /**
     * Sets the footer of the embed.
     *
     * @param text the text of the footer.
     * @param icon the footer's icon.
     * @param fileType the type of the file, e.g. "png" or "gif".
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setFooter(@NotNull String text, @NotNull BufferedImage icon, @NotNull String fileType) {
        embed.setFooter(text, icon, fileType);
        return this;
    }

    /**
     * Sets the image of the embed.
     *
     * @param url the url of the image.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setImage(@NotNull String url) {
        embed.setImage(url);
        return this;
    }

    /**
     * Sets the image of the embed.
     *
     * @param image the image.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setImage(@NotNull Icon image) {
        embed.setImage(image);
        return this;
    }

    /**
     * Sets the image of the embed.
     *
     * @param image the image.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setImage(@NotNull File image) {
        embed.setImage(image);
        return this;
    }

    /**
     * Sets the image of the embed.
     * This method assumes the file type is "png"!
     *
     * @param image the image.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setImage(@NotNull InputStream image) {
        embed.setImage(image);
        return this;
    }

    /**
     * Sets the image of the embed.
     *
     * @param image the image.
     * @param fileType the type of the file, e.g. "png" or "gif".
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setImage(@NotNull InputStream image, @NotNull String fileType) {
        embed.setImage(image, fileType);
        return this;
    }

    /**
     * Sets the image of the embed.
     * This method assumes the file type is "png"!
     *
     * @param image the image.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setImage(byte[] image) {
        embed.setImage(image);
        return this;
    }

    /**
     * Sets the image of the embed.
     *
     * @param image the image.
     * @param fileType the type of the file, e.g. "png" or "gif".
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setImage(byte[] image, @NotNull String fileType) {
        embed.setImage(image, fileType);
        return this;
    }

    /**
     * Sets the image of the embed.
     * This method assumes the file type is "png"!
     *
     * @param image the image.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setImage(@NotNull BufferedImage image) {
        embed.setImage(image);
        return this;
    }

    /**
     * Sets the image of the embed.
     *
     * @param image the image.
     * @param fileType the type of the file, e.g. "png" or "gif".
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setImage(@NotNull BufferedImage image, @NotNull String fileType) {
        embed.setImage(image, fileType);
        return this;
    }

    /**
     * Sets the author of the embed.
     *
     * @param author the message author which should be used as author.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setAuthor(@NotNull MessageAuthor author) {
        embed.setAuthor(author);
        return this;
    }

    /**
     * Sets the author of the embed.
     *
     * @param author the user which should be used as author.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setAuthor(@NotNull User author) {
        embed.setAuthor(author);
        return this;
    }

    /**
     * Sets the author of the embed.
     *
     * @param name the name of the author.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setAuthor(@NotNull String name) {
        embed.setAuthor(name);
        return this;
    }

    /**
     * Sets the author of the embed.
     *
     * @param name the name of the author.
     * @param url the url of the author.
     * @param iconUrl the url of the author's icon.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setAuthor(@NotNull String name, @Nullable String url, @NotNull String iconUrl) {
        embed.setAuthor(name, url, iconUrl);
        return this;
    }

    /**
     * Sets the author of the embed.
     *
     * @param name the name of the author.
     * @param url the url of the author.
     * @param icon the author's icon.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setAuthor(@NotNull String name, @Nullable String url, @NotNull Icon icon) {
        embed.setAuthor(name, url, icon);
        return this;
    }

    /**
     * Sets the author of the embed.
     *
     * @param name the name of the author.
     * @param url the url of the author.
     * @param icon the author's icon.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setAuthor(@NotNull String name, @Nullable String url, @NotNull File icon) {
        embed.setAuthor(name, url, icon);
        return this;
    }

    /**
     * Sets the author of the embed.
     * This method assumes the file type is "png"!
     *
     * @param name the name of the author.
     * @param url the url of the author.
     * @param icon the author's icon.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setAuthor(@NotNull String name, @Nullable String url, @NotNull InputStream icon) {
        embed.setAuthor(name, url, icon);
        return this;
    }

    /**
     * Sets the author of the embed.
     *
     * @param name the name of the author.
     * @param url the url of the author.
     * @param icon the author's icon.
     * @param fileType the type of the file, e.g. "png" or "gif".
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setAuthor(@NotNull String name, @Nullable String url, @NotNull InputStream icon, @NotNull String fileType) {
        embed.setAuthor(name, url, icon, fileType);
        return this;
    }

    /**
     * Sets the author of the embed.
     * This method assumes the file type is "png"!
     *
     * @param name the name of the author.
     * @param url the url of the author.
     * @param icon the author's icon.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setAuthor(@NotNull String name, @Nullable String url, byte[] icon) {
        embed.setAuthor(name, url, icon);
        return this;
    }

    /**
     * Sets the author of the embed.
     *
     * @param name the name of the author.
     * @param url the url of the author.
     * @param icon the author's icon.
     * @param fileType the type of the file, e.g. "png" or "gif".
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setAuthor(@NotNull String name, @Nullable String url, byte[] icon, @NotNull String fileType) {
        embed.setAuthor(name, url, icon, fileType);
        return this;
    }

    /**
     * Sets the author of the embed.
     * This method assumes the file type is "png"!
     *
     * @param name the name of the author.
     * @param url the url of the author.
     * @param icon the author's icon.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setAuthor(@NotNull String name, @Nullable String url, @NotNull BufferedImage icon) {
        embed.setAuthor(name, url, icon);
        return this;
    }

    /**
     * Sets the author of the embed.
     *
     * @param name the name of the author.
     * @param url the url of the author.
     * @param icon the author's icon.
     * @param fileType the type of the file, e.g. "png" or "gif".
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setAuthor(@NotNull String name, @Nullable String url, @NotNull BufferedImage icon, @NotNull String fileType) {
        embed.setAuthor(name, url, icon, fileType);
        return this;
    }

    /**
     * Sets the thumbnail of the embed.
     *
     * @param url the url of the thumbnail.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setThumbnail(@NotNull String url) {
        embed.setThumbnail(url);
        return this;
    }

    /**
     * Sets the thumbnail of the embed.
     *
     * @param thumbnail the thumbnail.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setThumbnail(@NotNull Icon thumbnail) {
        embed.setThumbnail(thumbnail);
        return this;
    }

    /**
     * Sets the thumbnail of the embed.
     *
     * @param thumbnail the thumbnail.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setThumbnail(@NotNull File thumbnail) {
        embed.setThumbnail(thumbnail);
        return this;
    }

    /**
     * Sets the thumbnail of the embed.
     * This method assumes the file type is "png"!
     *
     * @param thumbnail the thumbnail.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setThumbnail(@NotNull InputStream thumbnail) {
        embed.setThumbnail(thumbnail);
        return this;
    }

    /**
     * Sets the thumbnail of the embed.
     *
     * @param thumbnail the thumbnail.
     * @param fileType the type of the file, e.g. "png" or "gif".
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setThumbnail(@NotNull InputStream thumbnail, @NotNull String fileType) {
        embed.setThumbnail(thumbnail, fileType);
        return this;
    }

    /**
     * Sets the thumbnail of the embed.
     * This method assumes the file type is "png"!
     *
     * @param thumbnail the thumbnail.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setThumbnail(byte[] thumbnail) {
        embed.setThumbnail(thumbnail);
        return this;
    }

    /**
     * Sets the thumbnail of the embed.
     *
     * @param thumbnail the thumbnail.
     * @param fileType the type of the file, e.g. "png" or "gif".
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setThumbnail(byte[] thumbnail, String fileType) {
        embed.setThumbnail(thumbnail, fileType);
        return this;
    }

    /**
     * Sets the thumbnail of the embed.
     * This method assumes the file type is "png"!
     *
     * @param thumbnail the thumbnail.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setThumbnail(@NotNull BufferedImage thumbnail) {
        embed.setThumbnail(thumbnail);
        return this;
    }

    /**
     * Sets the thumbnail of the embed.
     *
     * @param thumbnail the thumbnail.
     * @param fileType the type of the file, e.g. "png" or "gif".
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder setThumbnail(@NotNull BufferedImage thumbnail, @NotNull String fileType) {
        embed.setThumbnail(thumbnail, fileType);
        return this;
    }

    /**
     * Adds an inline field to the embed.
     *
     * @param name the name of the field.
     * @param value the value of the field.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder addInlineField(@NotNull String name, @NotNull String value) {
        embed.addField(name, value, true);
        return this;
    }

    /**
     * Adds a non-inline field to the embed.
     *
     * @param name the name of the field.
     * @param value the value of the field.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder addField(@NotNull String name, @NotNull String value) {
        embed.addField(name, value, false);
        return this;
    }

    /**
     * Adds a field to the embed.
     *
     * @param name the name of the field.
     * @param value the value of the field.
     * @param inline Whether the field should be inline or not.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder addField(@NotNull String name, @NotNull String value, boolean inline) {
        embed.addField(name, value, inline);
        return this;
    }

    /**
     * Updates all fields of the embed that satisfy the given predicate using the given updater.
     *
     * @param predicate the predicate that fields have to satisfy to get updated.
     * @param updater the updater for the fields; the {@code EditableEmbedField} is only valid during the run of the
     *                updater; any try to save it in a variable and reuse it later after this method call will fail
     *                with exceptions.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder updateFields(@NotNull Predicate<EmbedField> predicate, @NotNull Consumer<EditableEmbedField> updater) {
        embed.updateFields(predicate, updater);
        return this;
    }

    /**
     * Updates all fields of the embed using the given updater.
     *
     * @param updater the updater for the fields; the {@code EditableEmbedField} is only valid during the run of the
     *                updater; any try to save it in a variable and reuse it later after this method call will fail
     *                with exceptions.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder updateAllFields(@NotNull Consumer<EditableEmbedField> updater) {
        embed.updateFields(field -> true, updater);
        return this;
    }

    /**
     * Removes all fields of the embed that satisfy the given predicate.
     *
     * @param predicate the predicate that fields have to satisfy to get removed.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder removeFields(@NotNull Predicate<EmbedField> predicate) {
        embed.removeFields(predicate);
        return this;
    }

    /**
     * Removes all fields of the embed.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public JavacordEmbedBuilder removeAllFields() {
        embed.removeFields(field -> true);
        return this;
    }

    /**
     * Checks if this embed requires any attachments.
     *
     * @return the {@code JavacordEmbedBuilder} instance to chain methods.
     */
    public boolean requiresAttachments() {
        return embed.requiresAttachments();
    }
}
