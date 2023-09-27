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
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.javacord.annotation.CommandParameter;
import co.aikar.commands.javacord.context.Member;

@SuppressWarnings("all")
@CommandAlias("slash")
@Description("Various test commands.")
public class TestSlashCommand extends SlashBaseCommand {

    /*
     * Command signature: /slash test <channel>
     */
    @Subcommand("test1")
    @Description("Test subcommand.")
    public void onTest(SlashCommandEvent event, Member member) {
        event.newImmediateResponse()
                .setContent("Hello, " + member.getMentionTag() + "!")
                .respond();
    }

    /*
     * Command signature: /slash ban <user> [reason]
     */
    @Subcommand("ban")
    @Description("'Ban' a user.")
    public void onBan(SlashCommandEvent event, Member member, @CommandParameter("The reason the member was banned.") @Optional String reason) {
        if (reason == null) {
            reason = "No reason provided.";
        }

        event.newImmediateResponse()
                .setContent("Banned " + member.getMentionTag() + " for " + reason + ". Not really, but let's pretend.")
                .respond();
    }

    /*
     * This is a test subcommand group.
     */
    @Subcommand("group")
    @Description("Test subcommand group.")
    public class TestSubCommandGroup extends BaseCommand {

        /*
         * Command signature: /slash sub test
         * This is a test subcommand of a subcommand group.
         */
        @Subcommand("test2")
        @Description("Test subcommand.")
        public void onTest(SlashCommandEvent event) {
            event.newImmediateResponse()
                    .setContent("Test 2 success!")
                    .respond();
        }

//        @Subcommand("anothergroup")
//        public class TestSubSubCommandGroup extends BaseCommand {
//            @Subcommand("test3")
//            @Description("Test subcommand.")
//            public void onTest(SlashCommandEvent event) {
//                event.newImmediateResponse()
//                        .setContent("Test 3 success!")
//                        .respond();
//            }
//        }
    }
}
