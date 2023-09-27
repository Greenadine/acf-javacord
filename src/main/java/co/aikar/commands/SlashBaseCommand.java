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

import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.contexts.ContextResolver;
import co.aikar.commands.contexts.IssuerOnlyContextResolver;
import co.aikar.commands.javacord.annotation.CommandOptions;
import co.aikar.commands.javacord.annotation.ServerCommand;
import co.aikar.commands.javacord.util.JavacordUtils;
import co.aikar.commands.javacord.util.StringUtils;
import com.google.common.base.Preconditions;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.Mentionable;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.*;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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

    /* TODO:
     *  - Add support for name localizations
     *  - Add support for description localizations
     */
    
    private DiscordApi api;
    private Annotations annotations;

    @Override
    void onRegister(@NotNull CommandManager manager) {
        super.onRegister(manager);
        
        init();
        registerAsSlashCommand();
    }

    /**
     * Initializes fields used for registering the command as a slash command.
     */
    private void init() {
        this.api = ((SlashCommandManager) manager).api;
        this.annotations = manager.getAnnotations();
    }

    /**
     * Registers the command as a slash command to Discord.
     */
    private void registerAsSlashCommand() {
        Preconditions.checkArgument(commandName != null && !commandName.isEmpty() && commandName.length() <= 32,
                "Command name must be between 1-32 characters.");
        Preconditions.checkArgument(description != null && !description.isEmpty() && description.length() <= 100,
                "Command description must be between 1-100 characters.");

        // Create base slash command
        SlashCommandBuilder builder = createAndApplyOptions(getClass(), commandName, description);

        // Register all direct subcommands
        createSubcommands(this).forEach(builder::addOption);
        createSubcommandGroups(this).forEach(builder::addOption);

        // Determine whether to register globally or for a specific server
        Server server = null;

        if (getClass().isAnnotationPresent(ServerCommand.class)) {
            ServerCommand serverCommand = getClass().getAnnotation(ServerCommand.class);

            // If an ID has been defined
            if (serverCommand.id() != 0L) {
                server = api.getServerById(serverCommand.id()).orElse(null);
            }
            else if (!serverCommand.name().isEmpty()) {
                server = api.getServersByName(serverCommand.name()).stream().findAny().orElse(null);
            }
        }

        if (server != null) {
            registerForServer(builder, server);
        } else {
            registerGlobally(builder);
        }
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

        // TODO test if this works as intended
        if (clazz.isAnnotationPresent(CommandOptions.class)) {
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
        }
        return builder;
    }

    /**
     * Registers all sub-scopes of the given scope as subcommand groups.
     */
    @NotNull
    private List<SlashCommandOption> createSubcommandGroups(@NotNull BaseCommand scope) {
        final ArrayList<SlashCommandOption> subcommandGroups = new ArrayList<>();

        if (!scope.subScopes.isEmpty()) {
            for (BaseCommand subScope : scope.subScopes) {
                if (!annotations.hasAnnotation(subScope.getClass(), Subcommand.class)) {
                    manager.log(LogLevel.ERROR, "Subcommand group '" + subScope.getClass().getSimpleName() + "' of scope '" + scope.commandName + "' is missing the @Subcommand annotation.");
                }

                String name = annotations.getAnnotationValue(subScope.getClass(), Subcommand.class);
                SlashCommandOptionBuilder builder = createSubcommandGroup(name, subScope);

                // Register nested subcommand groups
                // TODO this does not work with a nested class within a nested class, for whatever reason
                createSubcommandGroups(subScope).forEach(builder::addOption);

                subcommandGroups.add(builder.build());
            }
        }
        return subcommandGroups;
    }

    /**
     * Creates a new {@link SlashCommandOptionBuilder subcommand group} from the given scope.
     *
     * @param scope the scope to create the subcommand group from.
     *
     * @return the created {@link SlashCommandOptionBuilder}.
     */
    @NotNull
    private SlashCommandOptionBuilder createSubcommandGroup(@NotNull String name, @NotNull BaseCommand scope) {
        SlashCommandOptionBuilder builder = new SlashCommandOptionBuilder()
                .setType(SlashCommandOptionType.SUB_COMMAND_GROUP)
                .setName(name)
                .setDescription(scope.description);

        // Register methods as subcommands
        for (Map.Entry<String, RegisteredCommand> entry : scope.subCommands.entries()) {
            String subcommandName = getSubcommandName(entry.getKey());
            SlashCommandOption option = createSubcommand(subcommandName, entry.getValue());
            builder.addOption(option);
        }

        return builder;
    }

    /**
     * Gets all registered subcommands of the given scope and creates a new {@link SlashCommandOption subcommand} from them.
     *
     * @param scope the scope to create the subcommands from.
     *
     * @return the created {@link SlashCommandOption subcommands}.
     */
    @NotNull
    private List<SlashCommandOption> createSubcommands(@NotNull BaseCommand scope) {
        final List<SlashCommandOption> subcommands = new ArrayList<>();

        for (Map.Entry<String, RegisteredCommand> entry : scope.subCommands.entries()) {
            if (StringUtils.containsWhitespace(entry.getKey())) {
                continue; // These are registered as subcommand groups
            }
            String subcommandName = getSubcommandName(entry.getKey());
            subcommands.add(createSubcommand(subcommandName, entry.getValue()));
        }
        return subcommands;
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

        // Check parameters
        for (CommandParameter parameter : command.parameters) {
            if (isIssuerOnlyParameter(parameter)) { // Ignore issuer-only parameters
                continue;
            }

            createParameter(parameter.getName(), parameter);

            // TODO register parameters
        }
        return builder.build();
    }

    /**
     * Creates a new {@link SlashCommandOption} for the given command parameter.
     *
     * @param name the name of the parameter.
     * @param parameter the parameter to create the option from.
     *
     * @return the created {@link SlashCommandOption}.
     */
    private SlashCommandOption createParameter(@NotNull String name, @NotNull CommandParameter parameter) {
        Class<? extends Annotation> paramAnnoClass = co.aikar.commands.javacord.annotation.CommandParameter.class;

        SlashCommandOptionBuilder builder = new SlashCommandOptionBuilder()
                .setType(getParameterType(parameter))
                .setName(name)
                .setDescription(annotations.getAnnotationValue(parameter.getParameter(), paramAnnoClass))
                .setRequired(!parameter.isOptional());

        // TODO implement

        return builder.build();
    }

    /**
     * Gets the appropriate {@link SlashCommandOptionType} for the given {@link CommandParameter}.
     *
     * @param parameter the parameter to get the type for.
     *
     * @return the appropriate {@link SlashCommandOptionType} for the parameter.
     */
    private SlashCommandOptionType getParameterType(@NotNull CommandParameter parameter) {
        Class<?> type = parameter.getType();

        if (Integer.class.isAssignableFrom(type)
                || Long.class.isAssignableFrom(type)) {
            return SlashCommandOptionType.LONG;
        }
        else if (Boolean.class.isAssignableFrom(type)) {
            return SlashCommandOptionType.BOOLEAN;
        }
        else if (User.class.isAssignableFrom(type)) {
            return SlashCommandOptionType.USER;
        }
        else if (Channel.class.isAssignableFrom(type)) {
            return SlashCommandOptionType.CHANNEL;
        }
        else if (Role.class.isAssignableFrom(type)) {
            return SlashCommandOptionType.ROLE;
        }
        else if (Mentionable.class.isAssignableFrom(type)) {
            return SlashCommandOptionType.MENTIONABLE;
        }
        else if (Double.class.isAssignableFrom(type)
                || Float.class.isAssignableFrom(type)) { // TODO check if float is supported
            return SlashCommandOptionType.DECIMAL;
        }
        return SlashCommandOptionType.STRING;
    }

    /**
     * Creates a new choice with the given name and value for a command parameter.
     *
     * @param name the name of the choice.
     * @param value the value of the choice.
     *
     * @return the created {@link SlashCommandOptionChoice}.
     */
    private SlashCommandOptionChoice createChoice(@NotNull String name, @NotNull String value) {
        return SlashCommandOptionChoice.create(name, value);
    }

    /**
     * Returns whether the given command parameter's type is registered as an issuer-only parameter,
     * meaning it should be ignored while registering the command as a slash command.
     *
     * @param parameter the parameter to check.
     *
     * @return {@code true} if the parameter is an issuer-only parameter, {@code false} otherwise.
     */
    private boolean isIssuerOnlyParameter(@NotNull CommandParameter parameter) {
        Map<Class<?>, ? extends ContextResolver<?, ?>> contextMap = manager.getCommandContexts().contextMap;
        if (!contextMap.containsKey(parameter.getType())) {
            return false;
        }
        return contextMap.get(parameter.getType()) instanceof IssuerOnlyContextResolver;
    }

    /**
     * Creates a new choice with the given name and value for a command parameter.
     *
     * @param name the name of the choice.
     * @param value the value of the choice.
     *
     * @return the created {@link SlashCommandOptionChoice}.
     */
    private SlashCommandOptionChoice createChoice(@NotNull String name, long value) {
        return SlashCommandOptionChoice.create(name, value);
    }

    /**
     * Gets the correct name of the subcommand from the given string.
     *
     * @param name the string to get the subcommand name from.
     *
     * @return the correct name of the subcommand.
     */
    @NotNull
    private String getSubcommandName(@NotNull String name) {
        if (StringUtils.containsWhitespace(name)) {
            String[] split = StringUtils.splitOnWhitespace(name);
            return split[split.length - 1];
        }
        return name;
    }

    /**
     * Registers the given slash command globally.
     *
     * @param builder the slash command to register.
     */
    private void registerGlobally(@NotNull SlashCommandBuilder builder) {
        handleException(builder.createGlobal(api));
    }

    /**
     * Registers the given slash command for the given server.
     *
     * @param builder the slash command to register.
     * @param server the server to register the slash command for.
     */
    private void registerForServer(@NotNull SlashCommandBuilder builder, @NotNull Server server) {
        handleException(builder.createForServer(api, server.getId()));
    }

    /**
     * Handles any exceptions thrown while registering the command as a slash command.
     *
     * @param future the future to handle.
     */
    private void handleException(CompletableFuture<?> future) {
        future.exceptionally(throwable -> {
            String directCause = throwable.getClass().getSimpleName();
            String rootCause = JavacordUtils.getRootCause(throwable).getClass().getSimpleName();
            if (directCause.equals(rootCause)) {
                manager.log(LogLevel.ERROR, "Failed to register slash command '" + commandName + "'. Cause: " + directCause + ".", throwable);
            } else {
                manager.log(LogLevel.ERROR, "Failed to register slash command '" + commandName + "'. Cause: " + directCause +  " (" + rootCause + ").", throwable);
            }
            return null;
        });
    }
}
