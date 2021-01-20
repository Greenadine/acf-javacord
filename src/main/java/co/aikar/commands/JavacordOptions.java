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

import org.javacord.api.DiscordApi;
import org.jetbrains.annotations.NotNull;

public class JavacordOptions {
    co.aikar.commands.CommandConfig defaultConfig = new co.aikar.commands.JavacordCommandConfig();
    co.aikar.commands.CommandConfigProvider configProvider = null;
    co.aikar.commands.CommandPermissionResolver permissionResolver = new co.aikar.commands.JavacordCommandPermissionResolver();

    public JavacordOptions() {}

    public JavacordOptions defaultConfig(@NotNull co.aikar.commands.CommandConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
        return this;
    }

    public JavacordOptions configProvider(@NotNull co.aikar.commands.CommandConfigProvider configProvider) {
        this.configProvider = configProvider;
        return this;
    }

    public JavacordOptions permissionResolver(@NotNull co.aikar.commands.CommandPermissionResolver permissionResolver) {
        this.permissionResolver = permissionResolver;
        return this;
    }

    public co.aikar.commands.JavacordCommandManager create(DiscordApi api) { return new co.aikar.commands.JavacordCommandManager(api, this); }
}
