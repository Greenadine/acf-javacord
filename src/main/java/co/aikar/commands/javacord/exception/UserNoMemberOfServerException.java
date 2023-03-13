/*
 * Copyright (c) 2022 Kevin Zuman (Greenadine)
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

package co.aikar.commands.javacord.exception;

import co.aikar.commands.javacord.contexts.Member;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

/**
 * Thrown when an instance of a {@link Member} is being constructed where the provided
 * {@link User} is not part of the specified {@link Server}.
 *
 * @since 0.1
 * @author Greenadine
 */
public class UserNoMemberOfServerException extends RuntimeException {

    public UserNoMemberOfServerException(final User user, final Server server) {
        super(String.format("User '%s' (ID: %d) is no member of server '%s' (ID: %d).", user.getDiscriminatedName(), user.getId(), server.getName(), server.getId()));
    }
}
