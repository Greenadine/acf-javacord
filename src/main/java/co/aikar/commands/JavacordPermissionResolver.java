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

import org.javacord.api.entity.permission.PermissionType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 * @since 0.5.0
 * @author Greenadine
 */
public class JavacordPermissionResolver implements PermissionResolver {
    private final Map<String, Long> discordPermissionValues;

    public JavacordPermissionResolver() {
        discordPermissionValues = new HashMap<>();
        for (PermissionType permission : PermissionType.values()) {
            discordPermissionValues.put(permission.name().toLowerCase(Locale.ENGLISH).replaceAll("_", "-"), permission.getValue());
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean hasPermission(@NotNull AbstractJavacordCommandManager manager, @NotNull JavacordCommandEvent event, @NotNull String permission) {
        // Explicitly return true if the user is the bots' owner. They are always allowed.
        if (manager.getBotOwnerId() == event.getUser().getId()) {
            return true;
        }

        Long permissionValue = discordPermissionValues.get(permission);
        if (permissionValue == null) {
            return false;
        }

        if (!event.getChannel().asServerTextChannel().isPresent()) return false;

        PermissionType permissionType = getPermission(permissionValue);

        if (permissionType == null) return false;

        return event.getChannel().asServerTextChannel().get().getServer().hasAnyPermission(event.getUser(), permissionType, PermissionType.ADMINISTRATOR);
    }

    private PermissionType getPermission(long value) {
        for (PermissionType type : PermissionType.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }

        return null;
    }
}
