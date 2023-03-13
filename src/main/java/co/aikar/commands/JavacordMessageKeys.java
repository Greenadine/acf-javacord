/*
 * Copyright (c) 2021 Kevin Zuman (Greenadine)
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

import co.aikar.locales.MessageKey;
import co.aikar.locales.MessageKeyProvider;

import java.util.Locale;

/**
 * @since 0.1
 * @author Greenadine
 */
public enum JavacordMessageKeys implements MessageKeyProvider {
    OWNER_ONLY,
    SERVER_ONLY,
    PRIVATE_ONLY,
    GROUP_ONLY,
    TOO_MANY_USERS_WITH_NAME,
    COULD_NOT_FIND_USER,
    PLEASE_SPECIFY_USER,
    USER_IS_BOT,
    USER_NOT_MEMBER_OF_SERVER,
    USER_NOT_IN_VOICE_CHANNEL,
    COULD_NOT_FIND_CHANNEL,
    TOO_MANY_ROLES_WITH_NAME,
    COULD_NOT_FIND_ROLE,
    PLEASE_SPECIFY_ROLE,
    TOO_MANY_EMOJIS_WITH_NAME,
    COULD_NOT_FIND_EMOJI,
    PLEASE_SPECIFY_EMOJI,
    ;

    private final MessageKey key = MessageKey.of("acf-javacord." + this.name().toLowerCase(Locale.ENGLISH));

    public MessageKey getMessageKey() {
        return key;
    }
}
