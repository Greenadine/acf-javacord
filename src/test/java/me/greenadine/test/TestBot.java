package me.greenadine.test;

import co.aikar.commands.JavacordCommandManager;
import co.aikar.commands.JavacordOptions;
import co.aikar.commands.Locales;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Scanner;

public class TestBot {

    // Bot
    public static DiscordApi api;

    public static JavacordCommandManager commandManager;

    public static String defaultPrefix;

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

        registerCommands();

        api.updateActivity(ActivityType.WATCHING, defaultPrefix + "help"); // Set activity
        System.out.println("Online!");
    }

    private static void registerCommands() {
        commandManager = new JavacordOptions().configProvider(new TestCommandConfig()).create(api); // Create manager
        defaultPrefix = ((TestCommandConfig)commandManager.getConfigProvider()).getCommandPrefixes().get(0); // Get command prefix

        registerCommandReplacements();

        commandManager.addSupportedLanguage(Locales.DUTCH);
        commandManager.addSupportedLanguage(Locale.GERMAN);
        commandManager.addSupportedLanguage(Locale.FRENCH);
        commandManager.getLocales().setDefaultLocale(Locale.ENGLISH);

        // Register commands
        commandManager.registerCommand(new TestCommand());
    }

    /**
     * Register all command replacements.
     */
    private static void registerCommandReplacements() {
        commandManager.getCommandReplacements().addReplacements(
                // Perms
                "perm-admin", "manage-server", // Principal/Vice-Principal
                "perm-mod", "view-audit-log", // Teacher
                "perm-helper", "kick-members", // Assistant
                "perm-trusted", "use-external-emojis"
        );
    }
}
