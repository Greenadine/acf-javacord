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

package testbot;

import co.aikar.commands.SlashBaseCommand;
import co.aikar.commands.SlashCommandEvent;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;

@SuppressWarnings("all")
@CommandAlias("slash")
@Description("Various test commands.")
public class TestSlashCommand extends SlashBaseCommand {

    @Subcommand("ping")
    @Description("Test bot latency.")
    public void onPing(SlashCommandEvent event) {
        double startMillis = System.currentTimeMillis();
        event.newImmediateResponse()
                .setContent("Testing latency...")
                .respond()
                .thenAcceptAsync(updater -> {
                        double currentMillis = System.currentTimeMillis();
                        double ping = Math.abs(Math.round((currentMillis - startMillis) / 100));

                        updater.setContent(String.format("My API latency is %.0fms.", ping))
                                .update();
                    });
    }
}
