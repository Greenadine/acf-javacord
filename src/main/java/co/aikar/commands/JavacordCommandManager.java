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

import co.aikar.commands.apachecommonslang.ApacheCommonsExceptionUtil;
import org.javacord.api.AccountType;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JavacordCommandManager extends CommandManager<
        MessageCreateEvent,
        JavacordCommandEvent,
        String,
        MessageFormatter<String>,
        JavacordCommandExecutionContext,
        JavacordConditionContext
        > {

    private final DiscordApi api;
    protected JavacordCommandCompletions completions;
    protected JavacordCommandContexts contexts;
    protected JavacordLocales locales;
    protected Map<String, JavacordRootCommand> commands = new HashMap<>();
    private Logger logger;
    private CommandConfig defaultConfig;
    private CommandConfigProvider configProvider;
    private CommandPermissionResolver permissionResolver;
    private long botOwner = 0L;

    public JavacordCommandManager(DiscordApi api) { this(api, null); }

    public JavacordCommandManager(DiscordApi api, JavacordOptions options) {
        if (options == null) {
            options = new JavacordOptions();
        }
        this.api = api;
        this.permissionResolver = options.permissionResolver;
        this.defaultConfig = options.defaultConfig != null ? new JavacordCommandConfig() : options.defaultConfig;
        this.configProvider = options.configProvider;
        this.defaultFormatter = new JavacordMessageFormatter();
        this.completions = new JavacordCommandCompletions(this);
        this.logger = Logger.getLogger(this.getClass().getSimpleName());

        initializeBotOwner();
        api.addMessageCreateListener(new JavacordListener(this));

        getCommandConditions().addCondition("owneronly", context -> {
            if (context.getIssuer().getEvent().getMessageAuthor().getId() != getBotOwnerId()) {
                throw new ConditionFailedException(JavacordMessageKeys.OWNER_ONLY);
            }
        });

        getCommandConditions().addCondition("serveronly", context -> {
            if (context.getIssuer().getEvent().getChannel().getType() != ChannelType.SERVER_TEXT_CHANNEL) {
                throw new ConditionFailedException(JavacordMessageKeys.SERVER_ONLY);
            }
        });

        getCommandConditions().addCondition("privateonly", context -> {
            if (context.getIssuer().getEvent().getChannel().getType() != ChannelType.PRIVATE_CHANNEL) {
                throw new ConditionFailedException(JavacordMessageKeys.PRIVATE_ONLY);
            }
        });

        getCommandConditions().addCondition("grouponly", context -> {
            if (context.getIssuer().getEvent().getChannel().getType() != ChannelType.GROUP_CHANNEL) {
                throw new ConditionFailedException(JavacordMessageKeys.GROUP_ONLY);
            }
        });
    }

    public static JavacordOptions options() { return new JavacordOptions(); }

    void initializeBotOwner() {
        if (botOwner == 0L) {
            if (api.getAccountType() == AccountType.BOT) {
                botOwner = api.getApplicationInfo().join().getOwnerId();
            } else {
                botOwner = api.getYourself().getId();
            }
        }
    }

    public long getBotOwnerId() {
        return botOwner;
    }

    public DiscordApi getApi() {
        return api;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public CommandConfig getDefaultConfig() {
        return defaultConfig;
    }

    public void setDefaultConfig(CommandConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
    }

    public CommandConfigProvider getConfigProvider() {
        return configProvider;
    }

    public void setConfigProvider(CommandConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    public CommandPermissionResolver getPermissionResolver() {
        return permissionResolver;
    }

    public void setPermissionResolver(CommandPermissionResolver permissionResolver) {
        this.permissionResolver = permissionResolver;
    }

    @Override
    public CommandContexts<?> getCommandContexts() {
        if (this.contexts == null) {
            this.contexts = new JavacordCommandContexts(this);
        }
        return this.contexts;
    }

    @Override
    public CommandCompletions<?> getCommandCompletions() {
        return completions;
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

    public void unregisterCommand(BaseCommand command) {
        for (Map.Entry<String, RootCommand> entry : command.registeredCommands.entrySet()) {
            String javacordCommandName = entry.getKey().toLowerCase(Locale.ENGLISH);
            JavacordRootCommand javacordCommand = (JavacordRootCommand) entry.getValue();
            javacordCommand.getSubCommands().values().removeAll(command.subCommands.values());
        }
    }

    @Override
    public boolean hasRegisteredCommands() {
        return !this.commands.isEmpty();
    }

    public boolean isCommandIssuer(Class<?> type) {
        return JavacordCommandEvent.class.isAssignableFrom(type);
    }

    @Override
    public JavacordCommandEvent getCommandIssuer(Object issuer) {
        if (!(issuer instanceof MessageCreateEvent)) {
            throw new IllegalArgumentException(issuer.getClass().getName() + " is not a MessageCreateEvent.");
        }
        return new JavacordCommandEvent(this, (MessageCreateEvent) issuer);
    }

    @Override
    public RootCommand createRootCommand(String cmd) {
        return new JavacordRootCommand(this, cmd);
    }

    @Override
    public Collection<RootCommand> getRegisteredRootCommands() {
        return Collections.unmodifiableCollection(commands.values());
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
    public CommandExecutionContext createCommandContext(RegisteredCommand command, CommandParameter parameter, CommandIssuer sender, List<String> args, int i, Map<String, Object> passedArgs) {
        return new JavacordCommandExecutionContext(command, parameter, (JavacordCommandEvent) sender, args, i, passedArgs);
    }

    @Override
    public CommandCompletionContext createCompletionContext(RegisteredCommand command, CommandIssuer sender, String input, String config, String[] args) {
        // Not really going to be used;
        //noinspection unchecked
        return new CommandCompletionContext(command, sender, input, config, args);
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

    void dispatchEvent(MessageCreateEvent event) {
        Message message = event.getMessage();
        String msg = message.getContent();

        CommandConfig config = getCommandConfig(event);

        String prefixFound = null;
        for (String prefix : config.getCommandPrefixes()) {
            if (msg.startsWith(prefix)) {
                prefixFound = prefix;
                break;
            }
        }
        if (prefixFound == null) {
            return;
        }

        String[] args = ACFPatterns.SPACE.split(msg.substring(prefixFound.length()), -1);
        if (args.length == 0) {
            return;
        }
        String cmd = args[0].toLowerCase(Locale.ENGLISH);
        JavacordRootCommand rootCommand = this.commands.get(cmd);
        if (rootCommand == null) {
            return;
        }
        if (args.length > 1) {
            args = Arrays.copyOfRange(args, 1, args.length);
        } else {
            args = new String[0];
        }
        rootCommand.execute(this.getCommandIssuer(event), cmd, args);
    }

    private CommandConfig getCommandConfig(MessageCreateEvent event) {
        CommandConfig config = this.defaultConfig;
        if (this.configProvider != null) {
            CommandConfig provider = this.configProvider.provide(event);
            if (provider != null) {
                config = provider;
            }
        }
        return config;
    }

    @Override
    public String getCommandPrefix(CommandIssuer issuer) {
        MessageCreateEvent event = ((JavacordCommandEvent) issuer).getEvent();
        CommandConfig commandConfig = getCommandConfig(event);
        List<String> prefixes = commandConfig.getCommandPrefixes();
        return prefixes.isEmpty() ? "" : prefixes.get(0);
    }
}
