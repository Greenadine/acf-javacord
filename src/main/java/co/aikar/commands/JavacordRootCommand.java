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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 0.1.0
 * @see RootCommand
 */
@SuppressWarnings("rawtypes")
public class JavacordRootCommand implements RootCommand {

    protected final String name;
    boolean isRegistered = false;
    protected final AbstractJavacordCommandManager<?, ?, ?> manager;
    protected BaseCommand defCommand;
    protected final SetMultimap<String, RegisteredCommand> subCommands = HashMultimap.create();
    protected final List<BaseCommand> children = new ArrayList<>();

    JavacordRootCommand(@NotNull AbstractJavacordCommandManager<?, ?, ?> manager, @NotNull String name) {
        this.manager = manager;
        this.name = name;
    }

    @Override
    public void addChild(BaseCommand command) {
        if (defCommand == null || !command.subCommands.get(BaseCommand.DEFAULT).isEmpty()) {
            defCommand = command;
        }
        addChildShared(children, subCommands, command);
    }

    @Override
    public AbstractJavacordCommandManager<?, ?, ?> getManager() {
        return manager;
    }

    @Override
    public SetMultimap<String, RegisteredCommand> getSubCommands() {
        return subCommands;
    }

    @Override
    public List<BaseCommand> getChildren() {
        return children;
    }

    @Override
    public String getCommandName() {
        return name;
    }
}
