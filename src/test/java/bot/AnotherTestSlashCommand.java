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

import co.aikar.commands.BaseCommand;
import co.aikar.commands.SlashCommandEvent;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;

@CommandAlias("purpose")
@Description("Test slash command.")
public class AnotherTestSlashCommand extends BaseCommand {

    @Default
    public void onCommand(SlashCommandEvent event) {
        event.respond("I ruin your sanity.");
    }
}
