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

package co.aikar.commands.javacord.util;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public final class StringUtils {

    private static final Pattern SPACE_MATCHER = Pattern.compile(".*\\s.*");
    private static final Pattern SPACE_SPLITTER = Pattern.compile("\\s+");
    private static final Pattern EQUALS_MATCHER = Pattern.compile(".*=.*");
    private static final Pattern EQUALS_SPLITTER = Pattern.compile("=");

    /**
     * Checks if the given string contains whitespace.
     *
     * @param str the string to check.
     *
     * @return {@code true} if the given string contains whitespace, {@code false} otherwise.
     */
    public static boolean containsWhitespace(@NotNull String str) {
        return SPACE_MATCHER.matcher(str).matches();
    }

    /**
     * Splits the given string on whitespace.
     *
     * @param str the string to split.
     *
     * @return the split string.
     */
    public static String[] splitOnWhitespace(@NotNull String str) {
        return SPACE_SPLITTER.split(str);
    }

    /**
     * Checks if the given string contains the '=' character.
     *
     * @param str the string to check
     *
     * @return {@code true} if the given string contains the '=' character, {@code false} otherwise
     */
    public static boolean containsEquals(@NotNull String str) {
        return EQUALS_MATCHER.matcher(str).matches();
    }

    /**
     * Splits the given string on the '=' character.
     *
     * @param str the string to split.
     *
     * @return the split string.
     */
    public static String[] splitOnEquals(@NotNull String str) {
        return EQUALS_SPLITTER.split(str);
    }
}
