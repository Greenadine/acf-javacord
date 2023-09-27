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
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.server.Server;
import org.jetbrains.annotations.NotNull;

/**
 * @since 0.1.0
 */
@SuppressWarnings("unchecked")
public abstract class JavacordCommandContexts<CE extends JavacordCommandEvent, CEC extends JavacordCommandExecutionContext<CE, CEC>>
        extends CommandContexts<CEC> {

    protected final DiscordApi api;

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    JavacordCommandContexts(@NotNull AbstractJavacordCommandManager manager) {
        super(manager);
        this.api = manager.getApi();

        /* Override ACF core's default resolvers to better fit Discord */

        /* Javacord-specific resolvers */
        registerIssuerOnlyContext(DiscordApi.class, c -> api);
        registerIssuerOnlyContext(JavacordCommandEvent.class, CommandExecutionContext::getIssuer);
        registerIssuerOnlyContext(MessageCommandEvent.class, c -> {
            if (!(c.issuer instanceof MessageCommandEvent)) {
                throw new JavacordInvalidCommandArgument("Command was not triggered by a message. Use SlashCommandEvent or CommandEvent instead.");
            }
            return (MessageCommandEvent) c.issuer;
        });
        registerIssuerOnlyContext(SlashCommandEvent.class, c -> {
            if (!(c.issuer instanceof SlashCommandEvent)) {
                throw new JavacordInvalidCommandArgument("Command was not triggered by a slash command. Use MessageCommandEvent or CommandEvent instead.");
            }
            return (SlashCommandEvent) c.issuer;
        });
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

    protected <T extends Number> T resolveBigNumber(@NotNull CEC c) {
        String arg = c.popFirstArg();
        try {
            T number = (T) ACFUtil.parseBigNumber(arg, c.hasFlag("suffixes"));
            this.validateMinMax(c, number);
            return number;
        } catch (NumberFormatException ex) {
            throw new JavacordInvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", arg);
        }
    }

    protected Number resolveNumber(@NotNull CEC c, @NotNull Number minValue, @NotNull Number maxValue) {
        String number = c.popFirstArg();
        try {
            return this.parseAndValidateNumber(number, c, minValue, maxValue).shortValue();
        } catch (NumberFormatException ex) {
            throw new JavacordInvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", number);
        }
    }

    @NotNull
    protected Number parseAndValidateNumber(String number, CEC c, Number minValue, Number maxValue) throws JavacordInvalidCommandArgument {
        Number val = ACFUtil.parseNumber(number, c.hasFlag("suffixes"));
        this.validateMinMax(c, val, minValue, maxValue);
        return val;
    }

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

    protected <T> void registerContext(Class<T> clazz1, Class<T> clazz2, ContextResolver<T, CEC> supplier) {
        registerContext(clazz1, supplier);
        registerContext(clazz2, supplier);
    }
}
