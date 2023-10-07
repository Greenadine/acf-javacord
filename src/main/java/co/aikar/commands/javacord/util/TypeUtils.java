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

public final class TypeUtils {

    /**
     * Returns whether the given type is assignable from any of the given types.
     *
     * @param type the type to check.
     * @param types the types to check against.
     *
     * @return {@code true} if the type is assignable from any of the given types, {@code false} otherwise.
     */
    public static boolean isInstanceOfAny(@NotNull Class<?> type, Class<?> @NotNull... types) {
        for (Class<?> t : types) {
            if (t.isAssignableFrom(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether the given type is assignable from the given other type.
     *
     * @param type the type to check.
     * @param other the other type to check against.
     *
     * @return {@code true} if the type is assignable from the other type, {@code false} otherwise.
     */
    public static boolean isInstanceOf(@NotNull Class<?> type, @NotNull Class<?> other) {
        return other.isAssignableFrom(type);
    }

    /**
     * Returns whether the given type is any (primitive) numeric type.
     *
     * @param type the type to check.
     *
     * @return {@code true} if the type is a numeric type, {@code false} otherwise.
     */
    public static boolean isNumericType(@NotNull Class<?> type) {
        return isInstanceOfAny(type,
                Long.class, Long.TYPE, Integer.class, Integer.TYPE, Short.class, Short.TYPE, Byte.class, Byte.TYPE,
                Double.class, Double.TYPE, Float.class, Float.TYPE);
    }
}
