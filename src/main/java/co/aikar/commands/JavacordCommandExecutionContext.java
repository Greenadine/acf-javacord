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

import java.util.List;
import java.util.Map;

/**
 * @since 0.1.0
 */
public abstract class JavacordCommandExecutionContext<CE extends JavacordCommandEvent, CEC extends CommandExecutionContext<CEC, CE>> extends CommandExecutionContext<CEC, CE> {

    JavacordCommandExecutionContext(RegisteredCommand cmd, CommandParameter param, CE event, List<String> args, int index, Map<String, Object> passedArgs) {
        super(cmd, param, event, args, index, passedArgs);
    }
}
