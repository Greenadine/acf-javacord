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

import org.javacord.api.DiscordApi;
import org.jetbrains.annotations.NotNull;

/**
 * @since 0.1.0
 */
public class JavacordOptions {
    CommandConfig defaultConfig = new JavacordCommandConfig();
    CommandConfigProvider messageConfigProvider = null;
    CommandConfigProvider slashConfigProvider = null;
    JavacordPermissionResolver permissionResolver = new JavacordPermissionResolver();

    public JavacordOptions() {}

    public JavacordOptions defaultConfig(@NotNull CommandConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
        return this;
    }

    public JavacordOptions messageConfigProvider(@NotNull CommandConfigProvider configProvider) {
        this.messageConfigProvider = configProvider;
        return this;
    }

    public JavacordOptions slashConfigProvider(@NotNull CommandConfigProvider configProvider) {
        slashConfigProvider = configProvider;
        return this;
    }

    public JavacordOptions permissionResolver(@NotNull JavacordPermissionResolver permissionResolver) {
        this.permissionResolver = permissionResolver;
        return this;
    }

    public JavacordCommandManager createManager(@NotNull DiscordApi api) {
        return new JavacordCommandManager(api, this);
    }

    public MessageCommandManager createMessageManager(@NotNull DiscordApi api) {
        return new MessageCommandManager(api, this);
    }

    public SlashCommandManager createSlashManager(@NotNull DiscordApi api) {
        return new SlashCommandManager(api, this);
    }
}
