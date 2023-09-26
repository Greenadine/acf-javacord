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

import co.aikar.commands.javacord.annotation.CommandOptions;
import co.aikar.commands.javacord.annotation.SubcommandOptions;
import co.aikar.commands.javacord.util.Util;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionBuilder;
import org.javacord.api.interaction.SlashCommandOptionType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * A {@code BaseSlashCommand} is defined as a command group of related slash commands.
 * <p>
 * It is up to the end user how to organize their command.
 * You should use 1 base command per command in your application.
 * <p>
 * Optionally (and encouraged), you can use the base command to represent a root command,
 * and then each actionable command is a subcommand.
 *
 * @since 0.5.0
 * @see BaseCommand
 */
@SuppressWarnings("rawtypes")
public class SlashBaseCommand extends BaseCommand {

    /**
     * Gets the manager of the command as a {@link SlashCommandManager}.
     *
     * @return the manager of the command.
     */
    SlashCommandManager getManager() {
        return (SlashCommandManager) manager;
    }

    @Override
    void onRegister(@NotNull CommandManager manager) {
        super.onRegister(manager);
        registerAsSlashCommand();
    }

    /**
     * Registers the command as a slash command to Discord.
     */
    @SuppressWarnings("DataFlowIssue")
    private void registerAsSlashCommand() {
        // Create base slash command
        SlashCommandBuilder builder = createAndApplyOptions(getClass(), commandName, description);

        // Register methods as subcommands
        for (Map.Entry<String, RegisteredCommand> entry : subCommands.entries()) {
            if (ACFPatterns.SPACE.split(entry.getKey()).length > 1) {
                continue; // These are registered as subcommand groups
            }
            System.out.println("Registering subcommand: " + entry.getKey() + " for " + commandName + "...'");
            SlashCommandOption option = createSubcommand(entry.getKey(), entry.getValue());
            builder.addOption(option);
        }

        /*
        TODO fix subcommand groups

        final Annotations annotations = manager.getAnnotations();

        // Register sub scopes as subcommand groups with their own subcommands
        for (BaseCommand subScope : subScopes) {
            if (!annotations.hasAnnotation(subScope.getClass(), Subcommand.class)) {
                manager.log(LogLevel.ERROR, "Subcommand group '" + subScope.getClass().getSimpleName() + "' of command '" + commandName + "' is missing the @Subcommand annotation.");
                continue;
            }
            String name = annotations.getAnnotationValue(subScope.getClass(), Subcommand.class);
            System.out.println("Registering subcommand group '" + name + "' for command '" + commandName + "'...");

            SlashCommandOptionBuilder subCommandGroup = createSubcommandGroup(name, subScope);
            builder.addOption(subCommandGroup.build());
        }*/

        // Notify that subcommand groups are currently unsupported
        if (!subScopes.isEmpty()) {
            manager.log(LogLevel.INFO, "Found subcommand groups (inner classes) for command '" + commandName + "'. Subcommand groups are not yet supported. Subcommands will for now not be registered.");
        }

        builder.createGlobal(getManager().api)
                .exceptionally(throwable -> {
                    String directCause = throwable.getClass().getSimpleName();
                    String rootCause = Util.getRootCause(throwable).getClass().getSimpleName();
                    if (directCause.equals(rootCause)) {
                        manager.log(LogLevel.ERROR, "Failed to register slash command '" + commandName + "'. Cause: " + directCause + ".", throwable);
                    } else {
                        manager.log(LogLevel.ERROR, "Failed to register slash command '" + commandName + "'. Cause: " + directCause +  " (" + rootCause + ").", throwable);
                    }
                    return null;
                });
    }

    /**
     * Creates a new {@link SlashCommandBuilder} from the given command with the given name and description
     * and applies the options from the {@link CommandOptions} annotation.
     *
     * @param clazz the command's class.
     * @param name the name of the command.
     * @param description the description of the command.
     *
     * @return the created {@link SlashCommandBuilder}.
     */
    @NotNull
    private SlashCommandBuilder createAndApplyOptions(@NotNull Class<? extends BaseCommand> clazz, @NotNull String name, @NotNull String description) {
        SlashCommandBuilder builder = new SlashCommandBuilder()
                .setName(name)
                .setDescription(description);
        if (!clazz.isAnnotationPresent(CommandOptions.class)) {
            return builder;
        }

        final CommandOptions options = clazz.getAnnotation(CommandOptions.class);

        builder.setEnabledInDms(options.enabledInDms());
        builder.setNsfw(options.isNsfw());

        if (options.defaultEnabledForEveryone()) {
            builder.setDefaultEnabledForEveryone();
        }
        if (options.defaultEnabledForPermissions().length > 0) {
            builder.setDefaultEnabledForPermissions(options.defaultEnabledForPermissions());
        }
        if (options.defaultDisabled()) {
            builder.setDefaultDisabled();
        }
        return builder;
    }

    /**
     * Creates a new {@link SlashCommandOptionBuilder subcommand group} from the given sub scope.
     *
     * @param subScope the sub scope to create the subcommand group from.
     *
     * @return the created {@link SlashCommandOptionBuilder}.
     */
    @NotNull
    private SlashCommandOptionBuilder createSubcommandGroup(@NotNull String name, @NotNull BaseCommand subScope) {
        SlashCommandOptionBuilder builder = new SlashCommandOptionBuilder()
                .setType(SlashCommandOptionType.SUB_COMMAND_GROUP)
                .setName(name)
                .setDescription(subScope.description);

        // Register methods as subcommands
        for (Map.Entry<String, RegisteredCommand> entry : subScope.subCommands.entries()) {
            SlashCommandOption option = createSubcommand(entry.getKey(), entry.getValue());
            builder.addOption(option);
        }

        return builder;
    }

    /**
     * Creates a new {@link SlashCommandOption} from the given command.
     *
     * @param name the name of the command.
     * @param command the command to create the subcommand from.
     *
     * @return the created {@link SlashCommandOption}.
     */
    @NotNull
    private SlashCommandOption createSubcommand(@NotNull String name, @NotNull RegisteredCommand command) {
        SlashCommandOptionBuilder builder = new SlashCommandOptionBuilder()
                .setType(SlashCommandOptionType.SUB_COMMAND)
                .setName(name)
                .setDescription(command.helpText);

        if (command.method.isAnnotationPresent(SubcommandOptions.class)) {
            SubcommandOptions options = command.method.getAnnotation(SubcommandOptions.class);

            builder.setAutocompletable(options.isAutoCompletable());
            builder.setLongMinValue(options.longMinValue());
            builder.setLongMaxValue(options.longMaxValue());
            builder.setDecimalMinValue(options.decimalMinValue());
            builder.setDecimalMaxValue(options.decimalMaxValue());
            builder.setMinLength(options.minLength());
            builder.setMaxLength(options.maxLength());
        }

        return builder.build();
    }
}
