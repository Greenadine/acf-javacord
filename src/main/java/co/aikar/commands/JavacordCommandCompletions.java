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

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class JavacordCommandCompletions extends CommandCompletions<CommandCompletionContext<?>> {
    private boolean initialized;

    public JavacordCommandCompletions(CommandManager manager) {
        super(manager);
        this.initialized = true;
    }

    @Override
    public CommandCompletionHandler registerCompletion(String id, CommandCompletionHandler<CommandCompletionContext<?>> handler) {
        if (initialized) {
            throw new UnsupportedOperationException("Javacord doesn't support command completions.");
        }
        return null;
    }

    @Override
    public CommandCompletionHandler registerAsyncCompletion(String id, AsyncCommandCompletionHandler<CommandCompletionContext<?>> handler) {
        if (initialized) {
            throw new UnsupportedOperationException("Javacord doesn't support command completions.");
        }
        return null;
    }

    @NotNull
    @Override
    List<String> of(RegisteredCommand command, CommandIssuer issuer, String[] args, boolean isAsync) {
        return Collections.emptyList();
    }

    @Override
    List<String> getCompletionValues(RegisteredCommand command, CommandIssuer issuer, String completion, String[] args, boolean isAsync) {
        return Collections.emptyList();
    }
}
