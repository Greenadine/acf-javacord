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

import co.aikar.commands.contexts.ContextResolver;
import co.aikar.commands.javacord.exception.JavacordInvalidCommandArgument;
import org.javacord.api.interaction.SlashCommandInteractionOption;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletionStage;

/**
 * @since 0.5.0
 */
@SuppressWarnings("rawtypes,unchecked")
public class SlashRegisteredCommand extends RegisteredCommand {

    SlashRegisteredCommand(@NotNull BaseCommand scope, @NotNull String command, @NotNull Method method, @NotNull String prefSubCommand) {
        super(scope, command, method, prefSubCommand);
    }

    @Override
    void invoke(CommandIssuer sender, List args, CommandOperationContext context) {
        invoke((SlashCommandEvent) sender, context);
    }

    void invoke(SlashCommandEvent event, CommandOperationContext context) {
        preCommand();

        try {
            manager.getCommandConditions().validateConditions(context);
            Map<String, Object> passedArgs = resolveContexts(event);
            if (passedArgs == null) {
                return;
            }

            Object obj = method.invoke(scope, passedArgs.values().toArray());
            if (obj instanceof CompletionStage) {
                CompletionStage<?> future = (CompletionStage<?>) obj;
                future.exceptionally((t) -> {
                    handleException(event, null, t);
                    return null;
                });
            }
        } catch (Exception ex) {
            handleException(event, null, ex);
        } finally {
            postCommand();
        }
    }

    Map<String, Object> resolveContexts(SlashCommandEvent event) {
        return resolveContexts(event, (String) null);
    }

    Map<String, Object> resolveContexts(SlashCommandEvent event, String name) {
        List<SlashCommandInteractionOption> args = new ArrayList<>(event.getInteraction().getArguments());
        Map<String, Object> passedArgs = new LinkedHashMap<>();
        int remainingRequired = requiredResolvers;

        for (int i = 0; i < parameters.length && (name == null || !passedArgs.containsKey(name)); i++) {
            boolean isLast = i == parameters.length - 1;
            final CommandParameter parameter = parameters[i];
            final String parameterName = parameter.getName();
            final Class<?> type = parameter.getType();
            final ContextResolver resolver = parameter.getResolver();
            SlashCommandExecutionContext context = ((SlashCommandManager) manager).createCommandContext(this, parameter, event, event.getInteraction().getArguments());
            boolean requiresInput = parameter.requiresInput();
            if (requiresInput && remainingRequired > 0) {
                remainingRequired--;
            }

            Set<String> parameterPermissions = parameter.getRequiredPermissions();
            if (args.isEmpty() && !(isLast && type == String[].class)) {
                // TODO: not sure if this is necessary for slash commands, works fine without it (for now)
//                if (allowOptional && parameter.getDefaultValue() != null) {
//                    args.add(parameter.getDefaultValue());
//                }
                if (parameter.isOptional()) {
                    Object value;
                    if (!parameter.isOptionalResolver() || !manager.hasPermission(event, parameterPermissions)) {
                        value = null;
                    } else {
                        value = resolver.getContext(context);
                    }

                    if (value == null && parameter.getClass().isPrimitive()) {
                        throw new IllegalStateException("Parameter " + parameterName + " is primitive and does not support @Optional.");
                    }

                    manager.getCommandConditions().validateConditions(context, value);
                    passedArgs.put(parameterName, value);
                    continue;
                }
                else if (requiresInput) {
                    scope.showSyntax(event, this);
                    return null;
                }
            } else {
                if (!manager.hasPermission(event, parameterPermissions)) {
                    event.sendMessage(MessageType.ERROR, MessageKeys.PERMISSION_DENIED_PARAMETER, "{param}", parameterName);
                    throw new JavacordInvalidCommandArgument();
                }
            }

            Object paramValue = resolver.getContext(context);
            manager.getCommandConditions().validateConditions(context, paramValue);
            passedArgs.put(parameterName, paramValue);
        }

        return passedArgs;
    }
}
