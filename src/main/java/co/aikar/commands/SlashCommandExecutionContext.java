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

import org.javacord.api.entity.Attachment;
import org.javacord.api.entity.Mentionable;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandInteractionOption;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 0.5.0
 */
public class SlashCommandExecutionContext extends JavacordCommandExecutionContext<SlashCommandEvent, SlashCommandExecutionContext> {

    private final List<SlashCommandInteractionOption> args;

    SlashCommandExecutionContext(@NotNull SlashRegisteredCommand cmd, @NotNull CommandParameter parameter, @NotNull SlashCommandEvent event, @NotNull List<SlashCommandInteractionOption> args) {
        super(cmd, parameter, event, null, 0, null);

        this.args = args;
    }

    /**
     * Gets the list of arguments.
     *
     * @return the list of arguments.
     */
    public List<SlashCommandInteractionOption> getArguments() {
        return args;
    }

    /**
     * Removes and returns the next argument from the list of arguments.
     *
     * @return the next argument, or {@code null} if there are no more arguments.
     */
    public SlashCommandInteractionOption popNextArg() {
        return !args.isEmpty() ? args.remove(0) : null;
    }

    /**
     * Removes and returns the last argument from the list of arguments.
     *
     * @return the last argument, or {@code null} if there are no more arguments.
     */
    public SlashCommandInteractionOption popFinalArg() {
        return !args.isEmpty() ? args.remove(args.size() - 1) : null;
    }

    /**
     * Gets the next argument from the list of arguments.
     *
     * @return the next argument, or {@code null} if there are no more arguments.
     */
    public SlashCommandInteractionOption getNextArg() {
        return !args.isEmpty() ? args.get(0) : null;
    }

    /**
     * Gets the last argument from the list of arguments.
     *
     * @return the last argument, or {@code null} if there are no more arguments.
     */
    public SlashCommandInteractionOption getFinalArg() {
        return !args.isEmpty() ? args.get(args.size() - 1) : null;
    }

    /**
     * Checks whether the next argument in the list of arguments is a string.
     *
     * @return {@code true} if the next argument is a string, {@code false} otherwise.
     */
    public boolean isNextString() {
        return getNextArg().getStringValue().isPresent();
    }

    /**
     * Checks whether the next argument in the list of arguments is a {@code boolean}.
     *
     * @return {@code true} if the next argument is a {@code boolean}, {@code false} otherwise.
     */
    public boolean isNextBoolean() {
        return getNextArg().getBooleanValue().isPresent();
    }

    /**
     * Checks whether the next argument in the list of arguments is a {@code long}.
     *
     * @return {@code true} if the next argument is a {@code long}, {@code false} otherwise.
     */
    public boolean isNextLong() {
        return getNextArg().getLongValue().isPresent();
    }

    /**
     * Checks whether the next argument in the list of arguments is a {@code double}.
     *
     * @return {@code true} if the next argument is a {@code double}, {@code false} otherwise.
     */
    public boolean isNextDecimal() {
        return getNextArg().getDecimalValue().isPresent();
    }

    /**
     * Checks whether the next argument in the list of arguments is a {@link User user}.
     *
     * @return {@code true} if the next argument is a user, {@code false} otherwise.
     */
    public boolean isNextUser() {
        return getNextArg().getUserValue().isPresent();
    }

    /**
     * Checks whether the next argument in the list of arguments is a {@link Channel channel}.
     *
     * @return {@code true} if the next argument is a channel, {@code false} otherwise.`
     */
    public boolean isNextChannel() {
        return getNextArg().getChannelValue().isPresent();
    }

    /**
     * Checks whether the next argument in the list of arguments is a {@link Role role}.
     *
     * @return {@code true} if the next argument is a role, {@code false} otherwise.`
     */
    public boolean isNextRole() {
        return getNextArg().getRoleValue().isPresent();
    }

    /**
     * Checks whether the next argument in the list of arguments is a {@link Mentionable mentionable}.
     *
     * @return {@code true} if the next argument is a mentionable, {@code false} otherwise.`
     */
    public boolean isNextMentionable() {
        return getNextArg().getMentionableValue().isPresent();
    }

    /**
     * Checks whether the next argument in the list of arguments is an {@link Attachment attachment}.
     *
     * @return {@code true} if the next argument is an attachment, {@code false} otherwise.`
     */
    public boolean isNextAttachment() {
        return getNextArg().getAttachmentValue().isPresent();
    }

    /**
     * @throws UnsupportedOperationException always.
     * @deprecated Use {@link #popNextArg()} instead.
     */
    @Override
    @Deprecated
    public String popFirstArg() {
        throw new UnsupportedOperationException("popFirstArg() is not supported for slash commands. Use popNextArg() instead.");
    }

    /**
     * @throws UnsupportedOperationException always.
     * @deprecated Use {@link #popFinalArg()} instead.
     */
    @Override
    @Deprecated
    public String popLastArg() {
        throw new UnsupportedOperationException("popLastArg() is not supported for slash commands. Use popFinalArg() instead.");
    }

    /**
     * @throws UnsupportedOperationException always.
     * @deprecated Use {@link #getNextArg()} instead.
     */
    @Override
    @Deprecated
    public String getFirstArg() {
        throw new UnsupportedOperationException("getFirstArg() is not supported for slash commands. Use getNextArg() instead.");
    }

    /**
     * @throws UnsupportedOperationException always.
     * @deprecated Use {@link #getFinalArg()} instead.
     */
    @Override
    @Deprecated
    public String getLastArg() {
        throw new UnsupportedOperationException("getLastArg() is not supported for slash commands. Use getFinalArg() instead.");
    }
}
