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
import co.aikar.commands.javacord.annotation.CommandOptions;
import co.aikar.commands.javacord.annotation.ServerCommand;
import co.aikar.commands.javacord.exception.SlashCommandRegistryException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.base.Preconditions;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandOptionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * A {@code SlashCommandNode} represents a part of a slash command.A node can any type of {@link SlashCommandNodeType}.
 * The type of the node determines how the node is handled.
 *
 * @since 0.5.0
 */
@SuppressWarnings("rawtypes,ConstantConditions")
@JsonPropertyOrder({"type", "name", "description", "properties", "children"})
public class SlashCommandNode {

    @NotNull
    final SlashCommandNodeType type;
    @NotNull
    final String name;
    @NotNull
    final String description;
    @NotNull
    final Map<String, Object> properties;
    @Nullable
    final SlashCommandNode parent;
    @NotNull
    final List<SlashCommandNode> children;

    private boolean registered;

    //region Constructors
    /**
     * Creates a new root command node from the given root command.
     *
     * @param rootCommand the root command to create the node from.
     * @param baseCommand the base command of the root command.
     *
     * @throws SlashCommandRegistryException if an error occurs while creating the node and its children.
     */
    SlashCommandNode(@NotNull RootCommand rootCommand, @NotNull SlashBaseCommand baseCommand) {
        this(SlashCommandNodeType.ROOT, rootCommand.getCommandName(), baseCommand.description, null);

        SlashCommandManager manager = (SlashCommandManager) rootCommand.getManager();
        Map<String, RegisteredCommand> subCommands = SlashCommandUtils.createNewSubcommandMap(rootCommand);

        // Process subcommands
        for (Map.Entry<String, RegisteredCommand> entry : subCommands.entrySet()) {
            if (SlashCommandUtils.isNotOwnSubcommand(name, entry.getKey())) {
                continue;
            }
            String subCommandName = SlashCommandUtils.getSubcommandName(entry.getKey());
            children.add(new SlashCommandNode(subCommandName, entry.getValue(), this));
            subCommands = SlashCommandUtils.splitRemoveFirstIf(subCommands, name);
        }

        // Process sub-scopes (subcommand groups)
        for (BaseCommand subScope : baseCommand.subScopes) {
            String name = manager.getAnnotations().getAnnotationValue(subScope.getClass(), Subcommand.class);
            children.add(new SlashCommandNode(name, (SlashBaseCommand) subScope, this, subCommands));
        }

        // Process command options
        Class<?> baseClass = baseCommand.getClass();
        if (baseClass.isAnnotationPresent(CommandOptions.class)) {
            CommandOptions options = baseClass.getAnnotation(CommandOptions.class);
            properties.put("default-enabled-for-everyone", options.defaultEnabledForEveryone());
            properties.put("default-enabled-for-permissions", options.defaultEnabledForPermissions());
            properties.put("default-disabled", options.defaultDisabled());
            properties.put("enabled-in-dms", options.enabledInDms());
            properties.put("is-nsfw", options.isNsfw());
        }

        // Determine whether the command should be registered globally or for a specific server
        DiscordApi api = manager.api;
        if (baseClass.isAnnotationPresent(ServerCommand.class)) {
            ServerCommand serverCommand = baseClass.getAnnotation(ServerCommand.class);

            Server server = null;
            // If an ID has been defined
            if (serverCommand.value() != 0L) {
                server = api.getServerById(serverCommand.value()).orElse(null);
            }
            // If a name has been defined
            else if (!serverCommand.name().isEmpty()) {
                server = api.getServersByName(serverCommand.name()).stream().findAny().orElse(null);
            }

            if (server != null) {
                properties.put("server", server.getId());
            } else {
                ACFUtil.sneaky(new SlashCommandRegistryException("Failed to find server for server command '" + name + "'."));
            }
        }
    }

    /**
     * Creates a new subcommand group node from the given subcommand group.
     *
     * @param scope the subcommand group to create the node from.
     */
    SlashCommandNode(@NotNull String name, @NotNull SlashBaseCommand scope, @NotNull SlashCommandNode parent, @NotNull Map<String, RegisteredCommand> subCommands) {
        this(SlashCommandNodeType.SUBCOMMAND_GROUP, name, scope.description, parent);
        SlashCommandUtils.check(!scope.subCommands.isEmpty(), "Subcommand groups must have at least one subcommand.");

        // Process subcommands
        ArrayList<String> subCommandNames = new ArrayList<>();
        for (Map.Entry<String, RegisteredCommand> entry : subCommands.entrySet()) {
            if (SlashCommandUtils.isNotOwnSubcommand(name, entry.getKey())) {
                continue;
            }
            String subCommandName = SlashCommandUtils.getSubcommandName(entry.getKey());
            if (subCommandNames.contains(subCommandName)) {
                throw new SlashCommandRegistryException("Found duplicate subcommand '" + scope.commandName + "' for subcommand group '" + scope.commandName + "'.");
            }
            children.add(new SlashCommandNode(subCommandName, entry.getValue(), this));
            subCommandNames.add(subCommandName);
        }
    }

    /**
     * Creates a new subcommand node from the given subcommand.
     *
     * @param name the name of the subcommand.
     * @param subCommand the subcommand to create the node from.
     * @param parent the parent node of the subcommand.
     *
     * @throws SlashCommandRegistryException if a required parameter follows an optional parameter.
     */
    private SlashCommandNode(@NotNull String name, @NotNull RegisteredCommand subCommand, @NotNull SlashCommandNode parent) {
        this(SlashCommandNodeType.SUBCOMMAND, name, subCommand.helpText, parent);

        // Process parameters
        CommandParameter previousParam = null;
        for (CommandParameter parameter : subCommand.parameters) {
            if (SlashCommandUtils.isIssuerOnlyParameter(parameter)) {
                continue;
            }
            if (!parameter.isOptional() && (previousParam != null && parameter.isOptional())) {
                throw new SlashCommandRegistryException("Required parameters can't come after optional parameters.");
            }
            previousParam = parameter;
            children.add(new SlashCommandNode(parameter, this));
        }
    }

    /**
     * Creates a new command parameter node from the given parameter.
     *
     * @param parameter the command parameter to create the node from.
     */
    private SlashCommandNode(@NotNull CommandParameter parameter, @NotNull SlashCommandNode parent) {
        this(SlashCommandNodeType.PARAMETER, parameter.getName(), parameter.getDescription(), parent);

        // Populate properties
        SlashCommandOptionType parameterType = SlashCommandUtils.getParameterType(parameter);
        properties.put("type", parameterType);
        properties.put("optional", parameter.isOptional());

        // Process flags
        SlashCommandUtils.processParameterFlags(parameter, properties, parameterType);

        // Process parameter choices
        SlashCommandUtils.processParameterChoices(parameter, properties);
    }

    /**
     * Creates a new base node.
     *
     * @param type the type of the node.
     * @param name the name of the node.
     * @param description the description of the node.
     *
     * @throws SlashCommandRegistryException if the name or description is too short or long (name: 1-32 characters; description: 1-100 characters).
     */
    private SlashCommandNode(@NotNull SlashCommandNodeType type, @NotNull String name, @NotNull String description, @Nullable SlashCommandNode parent) {
        SlashCommandUtils.check(name.length() <= 32, "Name has to be between 1 and 32 characters.");
        SlashCommandUtils.check(description.length() <= 100, "Description has to be between 1 and 100 characters.");

        this.type = type;
        this.name = name;
        this.description = description;
        this.properties = new HashMap<>();
        this.parent = parent;
        this.children = new ArrayList<>();
        this.registered = false;
    }

    @JsonCreator
    SlashCommandNode(@JsonProperty("type") @NotNull SlashCommandNodeType type,
                     @JsonProperty("name") @NotNull String name,
                     @JsonProperty("description") @NotNull String description,
                     @JsonProperty("properties") @NotNull Map<String, Object> properties,
                     @JsonProperty("parent") @Nullable SlashCommandNode parent,
                     @JsonProperty("children") @NotNull List<SlashCommandNode> children) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.properties = properties;
        this.parent = parent;
        this.children = children;
        this.registered = false;
    }
    //endregion

    /**
     * Registers a root node as a slash command.
     *
     * @param manager the slash command manager.
     *
     * @return a {@link CompletableFuture} that completes with a {@link SlashCommandRegistry} when the command has been registered.
     */
    @NotNull
    CompletableFuture<SlashCommandRegistry> register(@NotNull SlashCommandManager manager) {
        if (registered) {
            throw new SlashCommandRegistryException("This command has already been registered.");
        }
        Preconditions.checkState(type == SlashCommandNodeType.ROOT, "Cannot register non-root node.");

        if (properties.containsKey("server")) {
            long serverId = (long) properties.get("server");

            return CompletableFuture.supplyAsync(() -> {
                SlashCommand command;
                try {
                    command = toBuilder().createForServer(manager.api, serverId).join();
                } catch (CompletionException | CancellationException thrown) {
                    throw new SlashCommandRegistryException("Failed to register slash command '" + name + "'.", thrown);
                }
                registered = true;
                return new SlashCommandRegistry(command.getId(), this);
            });
        }
        return CompletableFuture.supplyAsync(() -> {
            SlashCommand command;
            try {
                command = toBuilder().createGlobal(manager.api)
                        .join();
            } catch (CompletionException | CancellationException thrown) {
                throw new SlashCommandRegistryException("Failed to register slash command '" + name + "'.", thrown);
            }
            registered = true;
            return new SlashCommandRegistry(command.getId(), this);
        });
    }

    /**
     * Compares this node to another node.
     *
     * @param other the other node.
     *
     * @return {@code true} if the nodes are equal, {@code false} otherwise.
     */
    boolean isEqual(@NotNull SlashCommandNode other) {
        // Compare node type
        if (this.type != other.type) {
            return false;
        }

        // Compare node name & description
        if (!this.name.equals(other.name)
                || !this.description.equals(other.description)) {
            return false;
        }

        // Compare properties
        if (this.properties.size() != other.properties.size()) {
            return false;
        }
        if (!this.properties.isEmpty()) {
            for (Map.Entry<String, Object> entry : this.properties.entrySet()) {
                if (!other.properties.containsKey(entry.getKey())) {
                    return false;
                }
                if (!other.properties.get(entry.getKey()).equals(entry.getValue())) {
                    return false;
                }
            }
        }

        // Compare children
        if (this.children.size() != other.children.size()) {
            return false;
        }
        if (!this.children.isEmpty()) {
            for (int i = 0; i < this.children.size(); i++) {
                if (!this.children.get(i).isEqual(other.children.get(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Converts this node to a {@link SlashCommandBuilder}.
     *
     * @return the converted {@code SlashCommandBuilder}.
     */
    SlashCommandBuilder toBuilder() {
        Preconditions.checkState(type == SlashCommandNodeType.ROOT, "Cannot convert non-root node to SlashCommandBuilder.");
        SlashCommandBuilder builder = new SlashCommandBuilder()
                .setName(name)
                .setDescription(description);

        // Process command options
        builder.setEnabledInDms((boolean) properties.getOrDefault("enabled-in-dms", true));
        builder.setNsfw((boolean) properties.getOrDefault("is-nsfw", false));
        if (properties.containsKey("default-enabled-for-everyone")) {
            builder.setDefaultEnabledForEveryone();
        }
        if (properties.containsKey("default-enabled-for-permissions")) {
            builder.setDefaultEnabledForPermissions((PermissionType[]) properties.get("default-enabled-for-permissions"));
        }
        if (properties.containsKey("default-disabled")) {
            builder.setDefaultDisabled();
        }

        // Process children
        for (SlashCommandNode child : children) {
            switch (child.type) {
                case SUBCOMMAND_GROUP:
                    builder.addOption(SlashCommandUtils.createSubcommandGroupOption(child));
                    break;
                case SUBCOMMAND:
                    builder.addOption(SlashCommandUtils.createSubcommandOption(child));
                    break;
            }
        }
        return builder;
    }
}
