package me.greenadine.test;

import co.aikar.commands.CommandConfig;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TestCommandConfig implements CommandConfig {

    @NotNull
    private final List<String> commandPrefixes = new CopyOnWriteArrayList<>(new String[] {"j!"});

    @NotNull
    @Override
    public List<String> getCommandPrefixes() {
        return commandPrefixes;
    }
}
