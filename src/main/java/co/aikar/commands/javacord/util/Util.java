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

public final class Util {

    /**
     * Returns the root cause of the given {@link Throwable}.
     *
     * @param thrown the {@link Throwable} to get the root cause of.
     *
     * @return the root cause of the given {@link Throwable}.
     */
    public static Throwable getRootCause(@NotNull Throwable thrown) {
        while (thrown.getCause() != null) {
            if (thrown.getCause().getClass() == Exception.class
                    || thrown.getCause().getClass() == Error.class
                    || thrown.getCause().getClass() == Throwable.class
                    || thrown.getCause().getClass() == RuntimeException.class) {
                break;
            }
            thrown = thrown.getCause();
        }
        return thrown;
    }
}
