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
import co.aikar.commands.contexts.OptionalContextResolver;
import co.aikar.commands.javacord.exception.JavacordInvalidCommandArgument;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.server.Server;
import org.jetbrains.annotations.NotNull;

/**
 * @since 0.1.0
 */
public abstract class JavacordCommandContexts<CE extends JavacordCommandEvent, CEC extends JavacordCommandExecutionContext<CE, CEC>>
        extends CommandContexts<CEC> {

    protected final DiscordApi api;

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    JavacordCommandContexts(@NotNull AbstractJavacordCommandManager manager) {
        super(manager);
        this.api = manager.getApi();

        /* Javacord-specific resolvers */
        registerIssuerOnlyContext(DiscordApi.class, c -> api);
        registerIssuerOnlyContext(JavacordCommandEvent.class, CommandExecutionContext::getIssuer);
        registerIssuerOnlyContext(ChannelType.class, c -> c.issuer.getChannel().getType());
        registerIssuerOnlyContext(Server.class, c -> {
            if (!c.issuer.isInServer()) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.SERVER_ONLY);
            } else {
                return c.issuer.getServer().get(); // No need for 'isPresent()' due to 'MessageCreateEvent#isServerMessage()'
            }
        });
    }

    /* Utility methods */

    protected void validateMinMax(CEC c, Number val) throws JavacordInvalidCommandArgument {
        this.validateMinMax(c, val, null, null);
    }

    protected void validateMinMax(CEC c, Number val, Number minValue, Number maxValue) throws JavacordInvalidCommandArgument {
        minValue = c.getFlagValue("min", minValue);
        maxValue = c.getFlagValue("max", maxValue);
        if (maxValue != null && val.doubleValue() > maxValue.doubleValue()) {
            throw new JavacordInvalidCommandArgument(MessageKeys.PLEASE_SPECIFY_AT_MOST, "{max}", String.valueOf(maxValue));
        } else if (minValue != null && val.doubleValue() < minValue.doubleValue()) {
            throw new JavacordInvalidCommandArgument(MessageKeys.PLEASE_SPECIFY_AT_LEAST, "{min}", String.valueOf(minValue));
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> void registerContext(Class<? extends T> clazz1, Class<? extends T> clazz2, ContextResolver<? extends T, CEC> supplier) {
        registerContext((Class<T>) clazz1, (ContextResolver<T, CEC> ) supplier);
        registerContext((Class<T>) clazz2, (ContextResolver<T, CEC> ) supplier);
    }

    protected <T> void registerOptionalContext(Class<T> clazz1, Class<T> clazz2, OptionalContextResolver<T, CEC> supplier) {
        registerOptionalContext(clazz1, supplier);
        registerOptionalContext(clazz2, supplier);
    }
}
