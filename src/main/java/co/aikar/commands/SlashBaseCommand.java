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

import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.contexts.ContextResolver;
import co.aikar.commands.contexts.IssuerOnlyContextResolver;
import co.aikar.commands.javacord.annotation.Choices;
import co.aikar.commands.javacord.annotation.CommandOptions;
import co.aikar.commands.javacord.annotation.ServerCommand;
import co.aikar.commands.javacord.util.JavacordUtils;
import co.aikar.commands.javacord.util.StringUtils;
import com.google.common.base.Preconditions;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.Mentionable;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     *  - Add support for parameter completions/possible values (in SlashRegisteredCommand#resolveContexts)
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
     * @deprecated Tab completion is (currently) not supported for slash commands.
     * @throws UnsupportedOperationException always.
     */
    @Override
    @Deprecated
    public List<String> tabComplete(CommandIssuer issuer, String commandLabel, String[] args) {
        throw new UnsupportedOperationException("Tab completion is (currently) not supported for slash commands.");
    }

    /**
     * @deprecated Tab completion is (currently) not supported for slash commands.
     * @throws UnsupportedOperationException always.
     */
    @Override
    @Deprecated
    public List<String> tabComplete(CommandIssuer issuer, String commandLabel, String[] args, boolean isAsync) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Tab completion is (currently) not supported for slash commands.");
    }

    /**
     * @deprecated Feature is not necessary, as Discord shows the syntax of a command in the slash command UI.
     * @throws UnsupportedOperationException always.
     */
    @Override
    @Deprecated
    public void showSyntax(CommandIssuer issuer, RegisteredCommand cmd) {
        throw new UnsupportedOperationException("Showing the syntax of a slash command is not supported.");
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
        SlashCommandBuilder builder = createAndApplyOptions();

        // Register all direct subcommands
        createSubcommands(this, false).forEach(builder::addOption);
        createSubcommandGroups(this).forEach(builder::addOption);

        // Determine whether to register globally or for a specific server
        Server server = null;

        if (getClass().isAnnotationPresent(ServerCommand.class)) {
            ServerCommand serverCommand = getClass().getAnnotation(ServerCommand.class);

            // If an ID has been defined
            if (serverCommand.value() != 0L) {
                server = api.getServerById(serverCommand.value()).orElse(null);
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
     * Creates a new {@link SlashCommandBuilder} for this command and applies the options from the
     * {@link CommandOptions} annotation, if present.
     *
     * @return the created {@link SlashCommandBuilder}.
     */
    @NotNull
    private SlashCommandBuilder createAndApplyOptions() {
        SlashCommandBuilder builder = new SlashCommandBuilder()
                .setName(commandName)
                .setDescription(description);

        // TODO test if this works as intended
        if (getClass().isAnnotationPresent(CommandOptions.class)) {
            final CommandOptions options = getClass().getAnnotation(CommandOptions.class);

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
                //createSubcommandGroups(subScope).forEach(builder::addOption);

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
        return new SlashCommandOptionBuilder()
                .setType(SlashCommandOptionType.SUB_COMMAND_GROUP)
                .setName(name)
                .setDescription(scope.description)
                .setOptions(createSubcommands(scope, true));
    }

    /**
     * Gets all registered subcommands of the given scope and creates a new {@link SlashCommandOption subcommand} from them.
     *
     * @param scope the scope to create the subcommands from.
     *
     * @return the created {@link SlashCommandOption subcommands}.
     */
    @NotNull
    private List<SlashCommandOption> createSubcommands(@NotNull BaseCommand scope, boolean isSubcommandGroup) {
        final List<SlashCommandOption> subcommands = new ArrayList<>();

        for (Map.Entry<String, RegisteredCommand> entry : scope.subCommands.entries()) {
            if (isSubcommandGroup && StringUtils.containsWhitespace(entry.getKey())) {
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
            if (!parameter.getParameter().isAnnotationPresent(Description.class)) {
                manager.log(LogLevel.ERROR, "Parameter '" + parameter.getName() + "' for subcommand '" + name + "' is missing the @Description annotation.");
                continue;
            }
//            System.out.println("Registering parameter '" + parameter.getName() + "' for subcommand '" + name + "'.");
            SlashCommandOption paramOption = createParameter(parameter.getName(), parameter);
            if (paramOption != null) {
                builder.addOption(paramOption);
            }
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
    @Nullable
    private SlashCommandOption createParameter(@NotNull String name, @NotNull CommandParameter parameter) {
        SlashCommandOptionType parameterType = getParameterType(parameter);
        System.out.println("Parameter type for '" + parameter.getName() + "' of command '" + commandName + "' is '" + parameterType.name() + "'.");
        if (parameterType == SlashCommandOptionType.UNKNOWN) {
            manager.log(LogLevel.ERROR, "Invalid parameter type '" + parameter.getType().getSimpleName() + "' for parameter '" + parameter.getName() + "'.");
            return null;
        }

        String description = annotations.getAnnotationValue(parameter.getParameter(), Description.class);
        if (description.isEmpty()) {
            manager.log(LogLevel.ERROR, "Description for parameter '" + parameter.getName() + "' cannot be empty.");
            return null;
        }

        SlashCommandOptionBuilder builder = new SlashCommandOptionBuilder()
                .setType(parameterType)
                .setName(name)
                .setDescription(description)
                .setRequired(!parameter.isOptional());

        // Get options from flags
        //noinspection unchecked
        Map<String, String> flags = parameter.getFlags();
        switch (parameterType) {
            case STRING:
                if (flags.containsKey("autocomplete")) {
                    builder.setAutocompletable(true);
//                    System.out.println("- Marked as auto-completable");
                }
                if (flags.containsKey("minlen")) {
                    long minLength = Long.parseLong(flags.get("minlen"));
                    builder.setMinLength(minLength);
//                    System.out.println("- Set min length to" + minLength);
                }
                if (flags.containsKey("maxlen")) {
                    long maxLength = Long.parseLong(flags.get("maxlen"));
                    builder.setMaxLength(maxLength);
//                    System.out.println("- Set max length to" + maxLength);
                }
                registerChoices(builder, parameter);
                break;
            case LONG:
            case DECIMAL:
                if (flags.containsKey("autocomplete")) {
                    builder.setAutocompletable(true);
//                    System.out.println("- Marked as auto-completable");
                }
                registerChoices(builder, parameter);
                break;
            case CHANNEL:
                Class<?> paramType = parameter.getType();

                // TODO: check if this works as intended
                switch (paramType.getSimpleName()) {
                    case "ServerChannel":
                        // Add all server channel types
                        for (ChannelType channelType : ChannelType.values()) {
                            if (channelType.name().startsWith("SERVER_")) {
                                builder.addChannelType(channelType);
                            }
                        }
                        break;
                    case "ServerTextChannel":
                        builder.addChannelType(ChannelType.SERVER_TEXT_CHANNEL);
                        break;
                    case "ServerVoiceChannel":
                        builder.addChannelType(ChannelType.SERVER_VOICE_CHANNEL);
                        break;
                    case "ServerForumChannel":
                        builder.addChannelType(ChannelType.SERVER_FORUM_CHANNEL);
                        break;
                    case "ServerThreadChannel":
                        builder.addChannelType(ChannelType.SERVER_PRIVATE_THREAD);
                        builder.addChannelType(ChannelType.SERVER_PUBLIC_THREAD);
                        builder.addChannelType(ChannelType.SERVER_NEWS_THREAD);
                        break;
                    case "ServerStageVoiceChannel":
                        builder.addChannelType(ChannelType.SERVER_STAGE_VOICE_CHANNEL);
                        break;
                }
                break;
        }
        return builder.build();
    }

    /**
     * Registers the choices for the given {@link CommandParameter command parameter}.
     *
     * @param builder the {@link SlashCommandOptionBuilder builder} to register the choices to.
     * @param parameter the command parameter to register the choices for.
     */
    private void registerChoices(@NotNull SlashCommandOptionBuilder builder, @NotNull CommandParameter parameter) {
        // TODO: check if this works as intended
        if (annotations.hasAnnotation(parameter.getParameter(), Choices.class)) {
            String choices = annotations.getAnnotationValue(parameter.getParameter(), Choices.class);
            for (String choice : choices.split(",")) {
                String choiceName;
                String choiceValue;
                if (StringUtils.containsEquals(choice)) {
                    String[] choiceSplit = StringUtils.splitOnEquals(choice);
                    if (choiceSplit.length != 2) {
                        manager.log(LogLevel.ERROR, "Invalid choice '" + choice + "' for parameter '" + parameter.getName() + "'.");
                        continue;
                    }
                    choiceName = choiceSplit[0];
                    choiceValue = choiceSplit[1];
                } else {
                    choiceName = choice;
                    choiceValue = choice;
                }

//                System.out.println("- Added choice '" + choiceName + "' with value '" + choiceValue + "'.");

                try {
                    long value = Long.parseLong(choiceValue);
                    builder.addChoice(createChoice(choiceName, value));
                    continue;
                } catch (NumberFormatException ignored) { }

                builder.addChoice(createChoice(choiceName, choiceValue));
            }
        }
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

        if (isInstanceOfAny(type, Double.class, Double.TYPE, Float.class, Float.TYPE)) {
            return SlashCommandOptionType.DECIMAL;
        }
        else if (isInstanceOfAny(type, Long.class, Long.TYPE, Integer.class, Integer.TYPE, Short.class, Short.TYPE, Byte.class, Byte.TYPE)) {
            return SlashCommandOptionType.LONG;
        }
        else if (isInstanceOfAny(type, Boolean.class, Boolean.TYPE)) {
            return SlashCommandOptionType.BOOLEAN;
        }
        else if (isInstanceOf(type, User.class)) {
            return SlashCommandOptionType.USER;
        }
        else if (isInstanceOf(type, ServerChannel.class)) {
            return SlashCommandOptionType.CHANNEL;
        }
        else if (isInstanceOf(type, Role.class)) {
            return SlashCommandOptionType.ROLE;
        }
        else if (isInstanceOf(type, Mentionable.class)) {
            return SlashCommandOptionType.MENTIONABLE;
        }
        return SlashCommandOptionType.STRING;
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
    private void handleException(@NotNull CompletableFuture<?> future) {
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

    /**
     * Returns whether the given type is assignable from any of the given types.
     *
     * @param type the type to check.
     * @param types the types to check against.
     *
     * @return {@code true} if the type is assignable from any of the given types, {@code false} otherwise.
     */
    private static boolean isInstanceOfAny(@NotNull Class<?> type, Class<?> @NotNull... types) {
        for (Class<?> t : types) {
            if (t.isAssignableFrom(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether the given type is assignable from the given other type.
     *
     * @param type the type to check.
     * @param other the other type to check against.
     *
     * @return {@code true} if the type is assignable from the other type, {@code false} otherwise.
     */
    private static boolean isInstanceOf(@NotNull Class<?> type, @NotNull Class<?> other) {
        return other.isAssignableFrom(type);
    }

    /**
     * Creates a new choice with the given name and value for a command parameter.
     *
     * @param name the name of the choice.
     * @param value the value of the choice.
     *
     * @return the created {@link SlashCommandOptionChoice}.
     */
    private static SlashCommandOptionChoice createChoice(@NotNull String name, @NotNull String value) {
        return SlashCommandOptionChoice.create(name, value);
    }

    /**
     * Creates a new choice with the given name and value for a command parameter.
     *
     * @param name the name of the choice.
     * @param value the value of the choice.
     *
     * @return the created {@link SlashCommandOptionChoice}.
     */
    private static SlashCommandOptionChoice createChoice(@NotNull String name, long value) {
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
    private static String getSubcommandName(@NotNull String name) {
        if (StringUtils.containsWhitespace(name)) {
            String[] split = StringUtils.splitOnWhitespace(name);
            return split[split.length - 1];
        }
        return name;
    }
}
