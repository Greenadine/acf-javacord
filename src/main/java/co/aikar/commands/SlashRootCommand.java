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

import org.jetbrains.annotations.NotNull;

/**
 * @since 0.5.0
 */
public class SlashRootCommand extends JavacordRootCommand {

    SlashRootCommand(@NotNull SlashCommandManager manager, @NotNull String name) {
        super(manager, name);
    }

    @Override
    public BaseCommand execute(CommandIssuer sender, String commandLabel, String[] args) {
        CommandRouter router = this.getManager().getRouter();
        CommandRouter.RouteSearch search = router.routeCommand(this, commandLabel, args, false);
        BaseCommand defCommand = this.getDefCommand();
        if (search != null) {
            CommandRouter.CommandRouteResult result = router.matchCommand(search, false);
            if (result != null) {
                BaseCommand scope = result.cmd.scope;
                scope.execute(sender, result);
                return scope;
            }

            RegisteredCommand firstElement = ACFUtil.getFirstElement(search.commands);
            if (firstElement != null) {
                defCommand = firstElement.scope;
            }
        }
        return defCommand;
    }
}
