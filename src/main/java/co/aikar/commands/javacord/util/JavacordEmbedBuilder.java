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

/**
 * Convenience class for building quick embeds within the contexts {@link TextChannel}.
 *
 * @since 0.1
 * @author Greenadine
 */
public class JavacordEmbedBuilder extends EmbedBuilder {

    private final TextChannel channel;

    private JavacordEmbedBuilder(TextChannel channel) {
        this.channel = channel;
    }

    public static JavacordEmbedBuilder forChannel(@NotNull TextChannel channel) {
        return new JavacordEmbedBuilder(channel);
    }

    /**
     * Send the embed to the channel.
     */
    public CompletableFuture<Message> send() {
        return channel.sendMessage(this);
    }

    @Override
    public JavacordEmbedBuilder setTitle(String title) {
        super.setTitle(title);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setDescription(String description) {
        super.setDescription(description);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setUrl(String url) {
        super.setUrl(url);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setTimestampToNow() {
        super.setTimestampToNow();
        return this;
    }

    @Override
    public JavacordEmbedBuilder setTimestamp(Instant timestamp) {
        super.setTimestamp(timestamp);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setColor(Color color) {
        super.setColor(color);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setFooter(String text) {
        super.setFooter(text);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setFooter(String text, String iconUrl) {
        super.setFooter(text, iconUrl);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setFooter(String text, Icon icon) {
        super.setFooter(text, icon);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setFooter(String text, File icon) {
        super.setFooter(text, icon);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setFooter(String text, InputStream icon) {
        super.setFooter(text, icon);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setFooter(String text, InputStream icon, String fileType) {
        super.setFooter(text, icon, fileType);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setFooter(String text, byte[] icon) {
        super.setFooter(text, icon);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setFooter(String text, byte[] icon, String fileType) {
        super.setFooter(text, icon, fileType);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setFooter(String text, BufferedImage icon) {
        super.setFooter(text, icon);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setFooter(String text, BufferedImage icon, String fileType) {
        super.setFooter(text, icon, fileType);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setImage(String url) {
        super.setImage(url);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setImage(Icon image) {
        super.setImage(image);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setImage(File image) {
        super.setImage(image);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setImage(InputStream image) {
        super.setImage(image);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setImage(InputStream image, String fileType) {
        super.setImage(image, fileType);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setImage(byte[] image) {
        super.setImage(image);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setImage(byte[] image, String fileType) {
        super.setImage(image, fileType);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setImage(BufferedImage image) {
        super.setImage(image);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setImage(BufferedImage image, String fileType) {
        super.setImage(image, fileType);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setAuthor(MessageAuthor author) {
        super.setAuthor(author);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setAuthor(User author) {
        super.setAuthor(author);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setAuthor(String name) {
        super.setAuthor(name);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setAuthor(String name, String url, String iconUrl) {
        super.setAuthor(name, url, iconUrl);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setAuthor(String name, String url, Icon icon) {
        super.setAuthor(name, url, icon);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setAuthor(String name, String url, File icon) {
        super.setAuthor(name, url, icon);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setAuthor(String name, String url, InputStream icon) {
        super.setAuthor(name, url, icon);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setAuthor(String name, String url, InputStream icon, String fileType) {
        super.setAuthor(name, url, icon, fileType);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setAuthor(String name, String url, byte[] icon) {
        super.setAuthor(name, url, icon);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setAuthor(String name, String url, byte[] icon, String fileType) {
        super.setAuthor(name, url, icon, fileType);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setAuthor(String name, String url, BufferedImage icon) {
        super.setAuthor(name, url, icon);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setAuthor(String name, String url, BufferedImage icon, String fileType) {
        super.setAuthor(name, url, icon, fileType);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setThumbnail(String url) {
        super.setThumbnail(url);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setThumbnail(Icon thumbnail) {
        super.setThumbnail(thumbnail);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setThumbnail(File thumbnail) {
        super.setThumbnail(thumbnail);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setThumbnail(InputStream thumbnail) {
        super.setThumbnail(thumbnail);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setThumbnail(InputStream thumbnail, String fileType) {
        super.setThumbnail(thumbnail, fileType);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setThumbnail(byte[] thumbnail) {
        super.setThumbnail(thumbnail);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setThumbnail(byte[] thumbnail, String fileType) {
        super.setThumbnail(thumbnail, fileType);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setThumbnail(BufferedImage thumbnail) {
        super.setThumbnail(thumbnail);
        return this;
    }

    @Override
    public JavacordEmbedBuilder setThumbnail(BufferedImage thumbnail, String fileType) {
        super.setThumbnail(thumbnail, fileType);
        return this;
    }

    @Override
    public JavacordEmbedBuilder addInlineField(String name, String value) {
        super.addInlineField(name, value);
        return this;
    }

    @Override
    public JavacordEmbedBuilder addField(String name, String value) {
        super.addField(name, value);
        return this;
    }

    @Override
    public JavacordEmbedBuilder addField(String name, String value, boolean inline) {
        super.addField(name, value, inline);
        return this;
    }

    @Override
    public JavacordEmbedBuilder updateFields(Predicate<EmbedField> predicate, Consumer<EditableEmbedField> updater) {
        super.updateFields(predicate, updater);
        return this;
    }

    @Override
    public JavacordEmbedBuilder updateAllFields(Consumer<EditableEmbedField> updater) {
        super.updateAllFields(updater);
        return this;
    }

    @Override
    public JavacordEmbedBuilder removeFields(Predicate<EmbedField> predicate) {
        super.removeFields(predicate);
        return this;
    }

    @Override
    public JavacordEmbedBuilder removeAllFields() {
        super.removeAllFields();
        return this;
    }

    @Override
    public boolean requiresAttachments() {
        return super.requiresAttachments();
    }
}
