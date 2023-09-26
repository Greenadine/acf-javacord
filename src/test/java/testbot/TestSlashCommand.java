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

import co.aikar.commands.BaseCommand;
import co.aikar.commands.SlashBaseCommand;
import co.aikar.commands.SlashCommandEvent;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.javacord.context.Member;

@SuppressWarnings("all")
@CommandAlias("slash")
@Description("Various test commands.")
public class TestSlashCommand extends SlashBaseCommand {

    /*
     * /slash test
     * NOTE: This is a test subcommand.
     */
    @Subcommand("test")
    @Description("Test subcommand.")
    public void onTest(SlashCommandEvent event, Member member) {
        event.newImmediateResponse()
                .setContent("Hello, " + member.getMentionTag() + "!")
                .respond();
    }

    /*
     * NOTE: This is a test subcommand group.
     * THESE ARE NOT YET SUPPORTED. These will for now be ignored.
     */
    @Subcommand("sub")
    @Description("Test subcommand group.")
    public class TestSubCommand extends BaseCommand {

        /*
         * /slash sub test
         * NOTE: This is a test subcommand of a subcommand group.
         * Because subcommand groups are currently not yet supported, these will for now also be ignored.
         */
        @Subcommand("test")
        @Description("Test subcommand.")
        public void onTest(SlashCommandEvent event) {
            event.newImmediateResponse()
                    .setContent("Test success!")
                    .respond();
        }
    }
}
