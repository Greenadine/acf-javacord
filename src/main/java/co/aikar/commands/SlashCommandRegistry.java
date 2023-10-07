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

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

public class SlashCommandRegistry {

    private final long id;
    private final SlashCommandNode root;

    public SlashCommandRegistry(long id, @NotNull SlashCommandNode root) {
        this.id = id;
        this.root = root;
    }

    /**
     * Gets the ID of this registry.
     *
     * @return the registry's ID.
     */
    public long getId() {
        return id;
    }

    /**
     * Checks if this registry is different from another registry.
     *
     * @param other the other registry.
     *
     * @return {@code true} if the registries are different, {@code false} otherwise.
     */
    public boolean isDifferent(@NotNull SlashCommandRegistry other) {
        return !this.root.isEqual(other.root);
    }
}
