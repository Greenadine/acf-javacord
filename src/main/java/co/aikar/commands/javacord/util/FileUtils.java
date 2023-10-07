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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public final class FileUtils {

    /**
     * Attempts to create the file with the given path, if it does not exist already.
     *
     * @param path the path of the file.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean createFileIfRequired(@NotNull String path) {
        File file = new File(path);

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        if (!file.exists()) {
            try {
                file.createNewFile();
                return true;
            } catch (IOException ex) {
                System.out.println("Failed to create file '" + path + "'. Cause: IOException.");
                //noinspection all
                ex.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Writes an empty JSON map to the file with the given path.
     * Creates the file if it does not exist yet.
     *
     * @param path the path of the file.
     *
     * @return {@code true} if the file didn't exist and was created, {@code false} otherwise.
     */
    public static boolean writeEmptyJsonMap(@NotNull String path) {
        File file = new File(path);

        if (createFileIfRequired(path)) {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                writer.write("{ }");
                writer.close();
            } catch (IOException ex) {
                System.out.println("Failed to write empty JSON map to file '" + file.getName() + "'. Cause: IOException.");
                //noinspection all
                ex.printStackTrace();
            }
            return true;
        }
        return false;
    }
}
