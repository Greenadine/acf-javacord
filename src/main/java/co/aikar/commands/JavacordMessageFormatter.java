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

/**
 * Message formatter for Javacord.
 *
 * @since 0.1.0
 * @author Greenadine
 */
public class JavacordMessageFormatter extends MessageFormatter<Object> {

    public JavacordMessageFormatter() {
        // Javacord does not support coloring messages outside of embed fields.
        // We pass three empty strings to remove color coded messages from appearing.
        super("", "", "");
    }

    @Override
    String format(Object color, String message) {
        return message;
    }
}
