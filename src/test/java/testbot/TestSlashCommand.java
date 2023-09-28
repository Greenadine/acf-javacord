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
import co.aikar.commands.annotation.*;
import co.aikar.commands.javacord.annotation.Choices;
import co.aikar.commands.javacord.annotation.Issuer;
import co.aikar.commands.javacord.context.Member;
import org.javacord.api.entity.channel.*;
import org.javacord.api.entity.user.User;

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

    @Subcommand("string")
    @Description("Send a message as the bot.")
    public void onString(SlashCommandEvent event,
                       @Description("The message to send.") String message) {
        event.newImmediateResponse()
                .setContent(message)
                .respond();
    }

    @Subcommand("stringchoice")
    @Description("Pick a choice.")
    public void onStringchoice(SlashCommandEvent event,
                         @Description("Your choice.") @Choices("test1=one,test2=two,test3=three") String message) {
        event.newImmediateResponse()
                .setContent(message) // Will send "one" if "test1" is selected, "two" if "test2" is selected, etc.
                .respond();
    }

    @Subcommand("long")
    @Description("Send a long as the bot.")
    public void onLong(SlashCommandEvent event,
                       @Description("The long to send.") long l) {
        event.newImmediateResponse()
                .setContent("Long: " + l)
                .respond();
    }

    @Subcommand("longchoice")
    @Description("Send a long as the bot.")
    public void onLongchoice(SlashCommandEvent event,
                       @Description("Your choice.") @Choices("test1=1.0,test2=2,test3=3") long l) {
        event.newImmediateResponse()
                .setContent("Long: " + l) // Will be 1 if "test1" is selected, 2 if "test2" is selected, etc.
                .respond();
    }

    @Subcommand("int")
    @Description("Send a integer as the bot.")
    public void onInt(SlashCommandEvent event,
                       @Description("The integer to send.") Integer i) {
        event.newImmediateResponse()
                .setContent("Integer: " + i)
                .respond();
    }

    @Subcommand("double")
    @Description("Send a double as the bot.")
    public void onDouble(SlashCommandEvent event,
                       @Description("The double to send.") double d) {
        event.newImmediateResponse()
                .setContent("Double: " + d)
                .respond();
    }

    @Subcommand("char")
    @Description("Send a character as the bot.")
    public void onChar(SlashCommandEvent event,
                         @Description("The double to send.") char c) {
        event.newImmediateResponse()
                .setContent("Double: " + c)
                .respond();
    }

    @Subcommand("float")
    @Description("Send a float as the bot.")
    public void onFloat(SlashCommandEvent event,
                         @Description("The float to send.") Float f) {
        event.newImmediateResponse()
                .setContent("Float: " + f)
                .respond();
    }

    @Subcommand("user")
    @Description("Shows the username of a user.")
    public void onUser(SlashCommandEvent event,
                         @Description("The user.") @Issuer User user) {
        event.newImmediateResponse()
                .setContent("Your username is " + user.getName())
                .respond();
    }

    @Subcommand("member")
    @Description("Shows the display name of a member.")
    public void onMember(SlashCommandEvent event,
                         @Description("The member.") Member member) {
        event.newImmediateResponse()
                .setContent("Their display name is " + member.getDisplayName())
                .respond();
    }

    @Subcommand("channel")
    @Description("Get the name of a channel.")
    public void onChannel(SlashCommandEvent event,
                          @Description("The channel to get the name of.") ServerChannel channel) {
        event.newImmediateResponse()
                .setContent("Server channel name: " + channel.getName())
                .respond();
    }

    @Subcommand("textchannel")
    @Description("Get the name of a text channel.")
    public void onTextchannel(SlashCommandEvent event,
                          @Description("The text channel to get the name of.") ServerTextChannel channel) {
        event.newImmediateResponse()
                .setContent("Server text channel name: " + channel.getName())
                .respond();
    }

    @Subcommand("voicechannel")
    @Description("Get the name of a voice channel.")
    public void onVoicechannel(SlashCommandEvent event,
                              @Description("The voice channel to get the name of.") ServerTextChannel channel) {
        event.newImmediateResponse()
                .setContent("Server voice channel name: " + channel.getName())
                .respond();
    }

    @Subcommand("stagevoicechannel")
    @Description("Get the name of a stage voice channel.")
    public void onStagevoicechannel(SlashCommandEvent event,
                               @Description("The stage channel to get the name of.") ServerStageVoiceChannel channel) {
        event.newImmediateResponse()
                .setContent("Server stage voice channel name: " + channel.getName())
                .respond();
    }

    @Subcommand("forumchannel")
    @Description("Get the name of a forum channel.")
    public void onForumChannel(SlashCommandEvent event,
                               @Description("The forum channel to get the name of.") ServerForumChannel channel) {
        event.newImmediateResponse()
                .setContent("Server forum channel name: " + channel.getName())
                .respond();
    }

    @Subcommand("threadchannel")
    @Description("Get the name of a thread channel.")
    public void onThreadchannel(SlashCommandEvent event,
                               @Description("The thread channel to get the name of.") ServerThreadChannel channel) {
        event.newImmediateResponse()
                .setContent("Server thread channel name: " + channel.getName())
                .respond();
    }
}