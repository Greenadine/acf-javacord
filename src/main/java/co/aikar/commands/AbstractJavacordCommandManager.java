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

import co.aikar.commands.apachecommonslang.ApacheCommonsExceptionUtil;
import org.javacord.api.DiscordApi;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Super class for Javacord command managers.
 *
 * @param <CE> the {@link JavacordCommandEvent} type.
 * @param <CEC> the {@link CommandExecutionContext} type.
 * @param <CC> the {@link ConditionContext} type.
 *
 * @since 0.5.0
 */
public abstract class AbstractJavacordCommandManager<
            CE extends JavacordCommandEvent,
            CEC extends JavacordCommandExecutionContext<CE, CEC>,
            CC extends JavacordConditionContext<CE>>
        extends CommandManager<
                    CE,
                    CE,
                    Object,
                    JavacordMessageFormatter,
                    CEC,
                    CC> {

    protected final DiscordApi api;

    protected Map<String, RootCommand> commands = new HashMap<>();
    protected JavacordCommandContexts<CE, CEC> contexts;
    protected JavacordCommandCompletions completions;
    protected CommandConfig defaultConfig;
    protected CommandConfigProvider configProvider;
    protected PermissionResolver permissionResolver;
    protected JavacordLocales locales;
    protected Logger logger;
    private long botOwner = 0L;

    protected AbstractJavacordCommandManager(@NotNull DiscordApi api) {
        this(api, new JavacordOptions());
    }

    protected AbstractJavacordCommandManager(@NotNull DiscordApi api, @NotNull JavacordOptions options) {
        this.api = api;
        this.completions = new JavacordCommandCompletions(this);
        this.defaultConfig = options.defaultConfig != null ? new JavacordCommandConfig() : options.defaultConfig;
        this.permissionResolver = options.permissionResolver;

        initializeBotOwner();
        registerCommandConditions();
    }

    @Override
    public void registerCommand(BaseCommand command) {
        command.onRegister(this);

        for (Map.Entry<String, RootCommand> entry : command.registeredCommands.entrySet()) {
            String commandName = entry.getKey().toLowerCase(Locale.ENGLISH);
            JavacordRootCommand cmd = (JavacordRootCommand) entry.getValue();
            if (!cmd.isRegistered) {
                cmd.isRegistered = true;
                commands.put(commandName, cmd);
            }
        }
    }

    @Override
    public CommandContexts<?> getCommandContexts() {
        return contexts;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public CommandCompletionContext createCompletionContext(RegisteredCommand command, CommandIssuer sender, String input, String config, String[] args) {
        // Not really going to be used;
        //noinspection unchecked
        return new CommandCompletionContext(command, sender, input, config, args);
    }

    @Override
    public Locales getLocales() {
        if (this.locales == null) {
            this.locales = new JavacordLocales(this);
            this.locales.loadLanguages();
        }
        return this.locales;
    }

    @Override
    public void log(LogLevel level, String message, Throwable throwable) {
        Level logLevel = level == LogLevel.INFO ? Level.INFO : Level.SEVERE;
        logger.log(logLevel, LogLevel.LOG_PREFIX + message);
        if (throwable != null) {
            for (String line : ACFPatterns.NEWLINE.split(ApacheCommonsExceptionUtil.getFullStackTrace(throwable))) {
                logger.log(logLevel, LogLevel.LOG_PREFIX + line);
            }
        }
    }

    /**
     * Gets the {@link DiscordApi} instance.
     *
     * @return the {@code DiscordApi} instance.
     */
    public DiscordApi getApi() {
        return api;
    }

    /**
     * Gets the ID of the bot owner.
     *
     * @return the ID of the bot owner.
     */
    public long getBotOwnerId() {
        return botOwner;
    }

    /**
     * Gets the {@link Logger}.
     *
     * @return the {@code Logger}.
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Sets the {@link Logger}.
     *
     * @param logger the {@code Logger}.
     */
    public void setLogger(@NotNull Logger logger) {
        this.logger = logger;
    }

    /**
     * Gets the default command configuration.
     *
     * @return the default command configuration.
     */
    public CommandConfig getDefaultConfig() {
        return defaultConfig;
    }

    /**
     * Sets the default command configuration.
     *
     * @param defaultConfig the default command configuration.
     */
    public void setDefaultConfig(@NotNull CommandConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
    }

    /**
     * Gets the command configuration provider.
     *
     * @return the command configuration provider.
     */
    public CommandConfigProvider getConfigProvider() {
        return configProvider;
    }

    /**
     * Sets the command configuration provider.
     *
     * @param configProvider the command configuration provider.
     */
    public void setConfigProvider(@NotNull CommandConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    /**
     * Gets the permission resolver.
     *
     * @return the permission resolver.
     */
    public PermissionResolver getPermissionResolver() {
        return permissionResolver;
    }

    /**
     * Sets the permission resolver.
     *
     * @param permissionResolver the permission resolver.
     */
    public void setPermissionResolver(@NotNull PermissionResolver permissionResolver) {
        this.permissionResolver = permissionResolver;
    }

    /**
     * Retrieves the ID of the bot owner.
     */
    void initializeBotOwner() {
        if (botOwner == 0L) {
            botOwner = api.getOwnerId().orElse(-1L);
        }
    }

    /**
     * Register Javacord-specific command conditions.
     */
    void registerCommandConditions() {
        getCommandConditions().addCondition("owneronly", context -> {
            if (context.getIssuer().getUser().getId() != getBotOwnerId()) {
                throw new ConditionFailedException(JavacordMessageKeys.OWNER_ONLY);
            }
        });

        getCommandConditions().addCondition("serveronly", context -> {
            if (!context.getIssuer().isInServer()) {
                throw new ConditionFailedException(JavacordMessageKeys.SERVER_ONLY);
            }
        });

        getCommandConditions().addCondition("privateonly", context -> {
            if (!context.getIssuer().isInPrivate()) {
                throw new ConditionFailedException(JavacordMessageKeys.PRIVATE_ONLY);
            }
        });
    }

    /**
     * Executes a root command.
     *
     * @param event the event that triggered the command.
     * @param cmd the invoked command's name.
     * @param args the invoked command arguments.
     */
    protected void executeRootCommand(@NotNull Object event, @NotNull String cmd, @NotNull String[] args) {
        RootCommand rootCommand = this.commands.get(cmd);
        if (rootCommand == null) {
            return;
        }
        rootCommand.execute(this.getCommandIssuer(event), cmd, args);
    }
}
