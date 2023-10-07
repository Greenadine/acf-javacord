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

import co.aikar.commands.contexts.IssuerOnlyContextResolver;
import co.aikar.commands.javacord.annotation.Choices;
import co.aikar.commands.javacord.exception.SlashCommandRegistryException;
import co.aikar.commands.javacord.util.StringUtils;
import co.aikar.commands.javacord.util.TypeUtils;
import org.javacord.api.entity.Mentionable;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionBuilder;
import org.javacord.api.interaction.SlashCommandOptionType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings("rawtypes")
public final class SlashCommandUtils {

    /**
     * Creates a subcommand group {@link SlashCommandOption option} from the given subcommand group {@link SlashCommandNode node}.
     *
     * @param node the subcommand group node to create the option from.
     *
     * @return the created {@code SlashCommandOption}.
     */
    static SlashCommandOption createSubcommandGroupOption(@NotNull SlashCommandNode node) {
        SlashCommandOptionBuilder builder = createOptionBuilder(node, SlashCommandOptionType.SUB_COMMAND_GROUP);

        // Process all children (subcommands and subcommand groups)
        for (SlashCommandNode child : node.children) {
            if (child.type != SlashCommandNodeType.SUBCOMMAND_GROUP) {
                continue;
            }
            builder.addOption(createSubcommandGroupOption(child));
        }
        return builder.build();
    }

    /**
     * Creates a subcommand {@link SlashCommandOption option} from the given sucommand {@link SlashCommandNode node}.
     *
     * @param node the subcommand node to create the option from.
     *
     * @return the created {@code SlashCommandOption}.
     */
    static SlashCommandOption createSubcommandOption(@NotNull SlashCommandNode node) {
        SlashCommandOptionBuilder builder = createOptionBuilder(node, SlashCommandOptionType.SUB_COMMAND);

        // Process parameters
        for (SlashCommandNode child : node.children) {
            builder.addOption(createParameterOption(child));
        }
        return builder.build();
    }

    /**
     * Creates a command parameter {@link SlashCommandOption option} from the given command parameter {@link SlashCommandNode node}.
     *
     * @param node the command parameter node to create the option from.
     *
     * @return the created {@code SlashCommandOption}.
     */
    @SuppressWarnings("unchecked")
    static SlashCommandOption createParameterOption(@NotNull SlashCommandNode node) {
        SlashCommandOptionType parameterType = (SlashCommandOptionType) node.properties.get("type");
        SlashCommandOptionBuilder builder = createOptionBuilder(node, parameterType);

        // Process flags
        switch (parameterType) {
            case STRING:
                if (node.properties.containsKey("autocomplete")) {
                    builder.setAutocompletable(true);
                }
                if (node.properties.containsKey("minlen")) {
                    builder.setMaxLength((long) node.properties.get("minlen"));
                }
                if (node.properties.containsKey("maxlen")) {
                    builder.setMaxLength((long) node.properties.get("maxlen"));
                }
            case LONG:
            case DECIMAL:
                if (node.properties.containsKey("autocomplete")) {
                    builder.setAutocompletable(true);
                }
        }
        if (node.properties.containsKey("channel-types")) {
            builder.setChannelTypes((List<ChannelType>) node.properties.get("channel-types"));
        }

        // Process parameter choice
        if (node.properties.containsKey("choices")) {
            Map<String, Object> choices = (Map<String, Object>) node.properties.get("choices");
            for (Map.Entry<String, Object> entry : choices.entrySet()) {
                if (entry.getValue() instanceof Integer) {
                    builder.addChoice(entry.getKey(), (int) entry.getValue());
                } else {
                    builder.addChoice(entry.getKey(), (String) entry.getValue());
                }
            }
        }
        return builder.build();
    }

    /**
     * Creates a base {@link SlashCommandOptionBuilder} from the given node with the given {@link SlashCommandOptionType type}.
     *
     * @param node the node to create the option from.
     * @param type the type of the option.
     *
     * @return the created {@code SlashCommandOptionBuilder}.
     */
    static SlashCommandOptionBuilder createOptionBuilder(@NotNull SlashCommandNode node, @NotNull SlashCommandOptionType type) {
        return new SlashCommandOptionBuilder()
                .setType(type)
                .setName(node.name)
                .setDescription(node.description);
    }

    /**
     * Creates a new subcommand map from the given {@link RootCommand command}.
     *
     * @param command the command to create the subcommand map from.
     *
     * @return the created subcommand map.
     */
    static Map<String, RegisteredCommand> createNewSubcommandMap(@NotNull RootCommand command) {
        Map<String, RegisteredCommand> subcommands = new HashMap<>();

        for (RegisteredCommand registeredCommand : command.getSubCommands().values()) {
            subcommands.put(registeredCommand.command, registeredCommand);
        }
        return subcommands;
    }

    /**
     * Removes the first word of the given subcommand map if it matches the given search key for all entries.
     *
     * @param subcommands the subcommand map to remove the first word from.
     * @param searchKey the search key to remove the first word for.
     *
     * @return the subcommand map with the first word removed from all entries.
     */
    static Map<String, RegisteredCommand> splitRemoveFirstIf(@NotNull Map<String, RegisteredCommand> subcommands, @NotNull String searchKey) {
        Map<String, RegisteredCommand> result = new HashMap<>();

        for (Map.Entry<String, RegisteredCommand> entry : subcommands.entrySet()) {
            String[] split = StringUtils.splitOnWhitespace(entry.getKey());
            if (!split[0].equals(searchKey)) {
                result.put(entry.getKey(), entry.getValue());
                continue;
            }
            String newKey = ACFUtil.join(Arrays.copyOfRange(split, 1, split.length), " ");
            result.put(newKey, entry.getValue());
        }
        return result;
    }

    /**
     * Gets the correct name of the subcommand from the given string.
     *
     * @param name the string to get the subcommand name from.
     *
     * @return the correct name of the subcommand.
     */
    @NotNull
    static String getSubcommandName(@NotNull String name) {
        if (StringUtils.containsWhitespace(name)) {
            String[] split = StringUtils.splitOnWhitespace(name);
            return ACFUtil.join(Arrays.copyOfRange(split, 1, split.length), " ");
        }
        return name;
    }

    /**
     * Checks whether a given subcommand should be registered as a subcommand of its node.
     *
     * @param nodeName the name of the node.
     * @param name the name of the subcommand.
     *
     * @return {@code true} if the subcommand should be registered as a subcommand of its node, {@code false} otherwise.
     */
    static boolean isNotOwnSubcommand(@NotNull String nodeName, @NotNull String name) {
        String[] split = StringUtils.splitOnWhitespace(name);
        if (!split[0].equals(nodeName)) {
            return true;
        }
        split = Arrays.copyOfRange(split, 1, split.length);
        return split.length != 1;
    }

    /**
     * Returns whether the given command parameter's type is registered as an issuer-only parameter,
     * meaning it should be ignored while registering the command as a slash command.
     *
     * @param parameter the parameter to check.
     *
     * @return {@code true} if the parameter is an issuer-only parameter, {@code false} otherwise.
     */
    static boolean isIssuerOnlyParameter(@NotNull CommandParameter parameter) {
        Map contextMap = parameter.getManager().getCommandContexts().contextMap;
        if (!contextMap.containsKey(parameter.getType())) {
            return false;
        }
        return contextMap.get(parameter.getType()) instanceof IssuerOnlyContextResolver;
    }

    /**
     * Gets the appropriate {@link SlashCommandOptionType} for the given {@link CommandParameter}.
     *
     * @param parameter the parameter to get the type for.
     *
     * @return the appropriate {@link SlashCommandOptionType} for the parameter.
     */
    @NotNull
    static SlashCommandOptionType getParameterType(@NotNull CommandParameter parameter) {
        Class<?> type = parameter.getType();

        if (TypeUtils.isInstanceOfAny(type, Double.class, Double.TYPE, Float.class, Float.TYPE)) {
            return SlashCommandOptionType.DECIMAL;
        }
        else if (TypeUtils.isInstanceOfAny(type, Long.class, Long.TYPE, Integer.class, Integer.TYPE, Short.class, Short.TYPE, Byte.class, Byte.TYPE)) {
            return SlashCommandOptionType.LONG;
        }
        else if (TypeUtils.isInstanceOfAny(type, Boolean.class, Boolean.TYPE)) {
            return SlashCommandOptionType.BOOLEAN;
        }
        else if (TypeUtils.isInstanceOf(type, User.class)) {
            return SlashCommandOptionType.USER;
        }
        else if (TypeUtils.isInstanceOf(type, ServerChannel.class)) {
            return SlashCommandOptionType.CHANNEL;
        }
        else if (TypeUtils.isInstanceOf(type, Role.class)) {
            return SlashCommandOptionType.ROLE;
        }
        else if (TypeUtils.isInstanceOf(type, Mentionable.class)) {
            return SlashCommandOptionType.MENTIONABLE;
        }
        return SlashCommandOptionType.STRING;
    }

    /**
     * Processes the given {@link CommandParameter}'s flags and adds them to the given properties map.
     *
     * @param parameter the parameter to process the flags for.
     * @param properties the properties map to add the flags to.
     * @param parameterOptionType the {@link SlashCommandOptionType} of the parameter.
     */
    static void processParameterFlags(@NotNull CommandParameter parameter, @NotNull Map<String, Object> properties, @NotNull SlashCommandOptionType parameterOptionType) {
        //noinspection unchecked
        Map<String, String> flags = parameter.getFlags();
        switch (parameterOptionType) {
            case STRING:
                if (flags.containsKey("autocomplete")) {
                    properties.put("autocomplete", true);
                }
                if (flags.containsKey("minlen")) {
                    properties.put("minlen", Long.parseLong(flags.get("minlen")));
                }
                if (flags.containsKey("maxlen")) {
                    properties.put("maxlen", Long.parseLong(flags.get("maxlen")));
                }
                break;
            case LONG:
            case DECIMAL:
                if (flags.containsKey("autocomplete")) {
                    properties.put("autocomplete", true);
                }
                break;
            case CHANNEL:
                Class<?> paramType = parameter.getType();

                List<ChannelType> channelTypes = new ArrayList<>();
                switch (paramType.getSimpleName()) {
                    case "ServerChannel":
                        // Add all server channel types
                        for (ChannelType channelType : ChannelType.values()) {
                            if (channelType.name().startsWith("SERVER_")) {
                                channelTypes.add(channelType);
                            }
                        }
                        channelTypes.add(ChannelType.CHANNEL_CATEGORY);
                        break;
                    case "ServerTextChannel":
                        channelTypes.add(ChannelType.SERVER_TEXT_CHANNEL);
                        break;
                    case "ServerVoiceChannel":
                        channelTypes.add(ChannelType.SERVER_VOICE_CHANNEL);
                        break;
                    case "ServerForumChannel":
                        channelTypes.add(ChannelType.SERVER_FORUM_CHANNEL);
                        break;
                    case "ServerThreadChannel":
                        channelTypes.add(ChannelType.SERVER_PRIVATE_THREAD);
                        channelTypes.add(ChannelType.SERVER_PUBLIC_THREAD);
                        channelTypes.add(ChannelType.SERVER_NEWS_THREAD);
                        break;
                    case "ServerStageVoiceChannel":
                        channelTypes.add(ChannelType.SERVER_STAGE_VOICE_CHANNEL);
                        break;
                    case "ChannelCategory":
                        channelTypes.add(ChannelType.CHANNEL_CATEGORY);
                        break;
                }
                properties.put("channel-types", channelTypes);
                break;
        }
    }

    /**
     * Processes the given {@link CommandParameter}'s choices and adds them to the given properties map.
     *
     * @param parameter the parameter to process the choices for.
     * @param properties the properties map to add the choices to.
     */
    static void processParameterChoices(@NotNull CommandParameter parameter, @NotNull Map<String, Object> properties) {
        if (parameter.getParameter().isAnnotationPresent(Choices.class)) {
            Map<String, Object> choices = new HashMap<>();

            String choicesValue = parameter.getParameter().getAnnotation(Choices.class).value();
            for (String choice : choicesValue.split(",")) {
                String choiceName;
                String choiceValue;
                if (StringUtils.containsEquals(choice)) {
                    String[] choiceSplit = StringUtils.splitOnEquals(choice);
                    if (choiceSplit.length != 2) {
                        throw new SlashCommandRegistryException("Invalid choice: " + choice);
                    }
                    choiceName = choiceSplit[0];
                    choiceValue = choiceSplit[1];
                } else {
                    choiceName = choice;
                    choiceValue = choice;
                }

                if (TypeUtils.isNumericType(parameter.getParameter().getType())) {
                    try {
                        int longValue = Integer.parseInt(choiceValue);
                        choices.put(choiceName, longValue);
                    } catch (NumberFormatException ex) {
                        throw new SlashCommandRegistryException("Invalid choice value: " + choiceValue);
                    }
                }
                choices.put(choiceName, choiceValue);
            }
            properties.put("choices", choices);
        }
    }

    /**
     * Checks whether the given {@link CommandParameter} is a valid parameter for a slash command, and
     * throws a {@link SlashCommandRegistryException} if it isn't.
     *
     * @param expression the expression to check.
     * @param message the message to throw if the expression is {@code false}.
     */
    static void check(boolean expression, @NotNull String message) {
        if (!expression) {
            throw new SlashCommandRegistryException(message);
        }
    }
}
