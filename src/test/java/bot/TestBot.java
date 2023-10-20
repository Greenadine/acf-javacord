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

package bot;

import co.aikar.commands.*;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class TestBot {

    // Bot
    public static DiscordApi api;

    public static JavacordCommandManager COMMAND_MANAGER;

    public static String DEFAULT_PREFIX;

    @SuppressWarnings("CallToPrintStackTrace")
    public static void main(String[] args) {
        String token;

        // Retrieve the bot token from file.
        try {
            Scanner scanner = new Scanner(new File("token.txt"));
            token = scanner.nextLine();
            scanner.close();
        } catch (IOException ex) {
            System.out.println("Failed to read token from file. Cause: IOException.");
            ex.printStackTrace();
            return;
        }

        // Connect to bot user.
        api = new DiscordApiBuilder().setToken(token).setAllIntents().login().join();

//        api.getGlobalSlashCommands().join().forEach(SlashCommand::delete);
        registerCommands();

        api.updateActivity(ActivityType.PLAYING, "with Kevin's sanity"); // Set activity
        System.out.println("Online!");
    }

    /**
     * Registers commands.
     */
    private static void registerCommands() {
        COMMAND_MANAGER = new JavacordOptions().messageConfigProvider(new TestCommandConfig()).createManager(api); // Create command manager
        DEFAULT_PREFIX = ((TestCommandConfig) COMMAND_MANAGER.getMessageCommandManager().getConfigProvider()).getCommandPrefixes().get(0); // Get command prefix

        registerCommandReplacements();

        // Register commands
//        COMMAND_MANAGER.registerMessageCommand(new TestCommand());
        COMMAND_MANAGER.registerSlashCommand(new TestSlashCommand());
    }

    /**
     * Register all command replacements.
     */
    private static void registerCommandReplacements() {
        COMMAND_MANAGER.addReplacements(
                // Perms
                "perm-admin", "manage-server", // Principal/Vice-Principal
                "perm-mod", "view-audit-log", // Teacher
                "perm-helper", "kick-members", // Assistant
                "perm-trusted", "use-external-emojis"
        );
    }
}
