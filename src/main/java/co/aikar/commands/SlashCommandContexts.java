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

import co.aikar.commands.javacord.annotation.BotUser;
import co.aikar.commands.javacord.context.Member;
import co.aikar.commands.javacord.exception.JavacordInvalidCommandArgument;
import org.javacord.api.entity.user.User;
import org.jetbrains.annotations.NotNull;

public class SlashCommandContexts extends JavacordCommandContexts<SlashCommandEvent, SlashCommandExecutionContext> {

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    SlashCommandContexts(@NotNull AbstractJavacordCommandManager manager) {
        super(manager);

        /* Javacord-specific resolvers */
        registerIssuerOnlyContext(User.class, c -> {
            if (c.hasAnnotation(BotUser.class)) {
                return api.getYourself();
            }
            return c.issuer.getUser();
        });
        registerIssuerOnlyContext(Member.class, c -> {
            if (!c.issuer.isInServer()) {
                throw new JavacordInvalidCommandArgument(JavacordMessageKeys.SERVER_ONLY);
            }
            if (c.hasAnnotation(BotUser.class)) {
                return new Member(api.getYourself(), c.issuer.getServer().get());
            }
            return c.issuer.getMember();
        });
    }
}
