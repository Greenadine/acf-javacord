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

package co.aikar.commands;

import co.aikar.locales.MessageKey;
import co.aikar.locales.MessageKeyProvider;

/**
 * @since 0.1
 * @author Greenadine
 */
public class JavacordInvalidCommandArgument extends InvalidCommandArgument {

    public JavacordInvalidCommandArgument() {
        this((String)null);
    }

    public JavacordInvalidCommandArgument(String message) {
        super(message, false);
    }

    public JavacordInvalidCommandArgument(MessageKeyProvider key) {
        super(key, false);
    }

    public JavacordInvalidCommandArgument(MessageKeyProvider key, String... replacements) {
        super(key.getMessageKey(), false, replacements);
    }

    public JavacordInvalidCommandArgument(String message, Object... replacements) {
        this(String.format(message, replacements));
    }

    public JavacordInvalidCommandArgument(String message, String... replacements) {
        this(String.format(message, (Object[]) replacements));
    }

    public JavacordInvalidCommandArgument(MessageKey key, String... replacements) {
        super(key, false, replacements);
    }
}
