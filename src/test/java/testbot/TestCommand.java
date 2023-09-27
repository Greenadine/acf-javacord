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
import co.aikar.commands.MessageCommandEvent;
import co.aikar.commands.annotation.*;
import co.aikar.commands.javacord.context.Member;
import co.aikar.commands.javacord.context.UnicodeEmoji;
import org.javacord.api.entity.channel.*;
import org.javacord.api.entity.emoji.CustomEmoji;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;

@CommandAlias("test")
@Description("Various test commands.")
public class TestCommand extends BaseCommand {

    @Default
    public void onCommand(MessageCommandEvent event) {
        event.reply("Test success!");
    }

    @CatchUnknown
    public void onUnknownSubcommand(MessageCommandEvent event) {
        event.reply("Unknown subcommand!");
    }

    @Subcommand("ping")
    public void onPing(MessageCommandEvent event) {
        event.reply("Testing latency...")
                .thenAcceptAsync(message -> {
                        double messageTimestamp = message.getCreationTimestamp().toEpochMilli();
                        double currentTimestamp = System.currentTimeMillis();
                        double ping = Math.abs(Math.round((currentTimestamp - messageTimestamp) / 100));

                        message.edit(String.format("My API latency is %.0fms.", ping));
                    });
    }

    @Subcommand("sudo")
    public void onSudo(MessageCommandEvent event, ServerTextChannel channel, String message) {
        event.deleteMessage().thenAccept(m -> channel.sendMessage(message)); // Delete original message
    }

    @Subcommand("user")
    public void onUser(MessageCommandEvent event, User user, String test) {
        if (user == null) {
            event.reply("No user specified");
        } else {
            event.reply(user.getName());
        }
        event.reply(test);
    }

    @Subcommand("member")
    public void onMember(MessageCommandEvent event, @Flags("other") Member member) {
        event.reply(member.getDisplayName());
    }

    @Subcommand("humanmember")
    public void onHumanmember(MessageCommandEvent event, @Flags("other,humanonly") Member member) {
        event.reply(member.getDisplayName());
    }

    @Subcommand("channel")
    public void onChannel(MessageCommandEvent event, Channel channel) {
        event.reply(channel.getIdAsString());
    }

    @Subcommand("textchannel")
    public void onTextChannel(MessageCommandEvent event, TextChannel channel) {
        event.reply(channel.getIdAsString());
    }

    @Subcommand("servertextchannel")
    public void onServertextchannel(MessageCommandEvent event, ServerTextChannel channel) {
        event.reply(channel.getMentionTag());
    }

    @Subcommand("voicechannel")
    public void onVoiceChannel(MessageCommandEvent event, VoiceChannel channel) {
        event.reply(channel.getIdAsString());
    }

    @Subcommand("servervoicechannel")
    public void onServervoicechannel(MessageCommandEvent event, ServerVoiceChannel channel) {
        event.reply(channel.getName());
    }

    @Subcommand("role")
    public void onRole(MessageCommandEvent event, Role role) {
        event.reply(role.getName());
    }

    @Subcommand("emoji")
    public void onEmoji(MessageCommandEvent event, Emoji emoji) {
        event.reply(emoji.getMentionTag());
    }

    @Subcommand("customemoji")
    public void onCustomEmoji(MessageCommandEvent event, CustomEmoji emoji) {
        event.reply(emoji.getMentionTag());
    }

    @Subcommand("unicodeemoji")
    public void onUnicodeEmoji(MessageCommandEvent event, UnicodeEmoji emoji) {
        event.reply(emoji.getMentionTag());
    }

    @Subcommand("perm")
    @CommandPermission("%perm-mod")
    public void onPerm(MessageCommandEvent event) {
        event.reply("Yes");
    }

    @Subcommand("integer")
    public void onInteger(MessageCommandEvent event, @Flags("min=1,max=5") Integer number) {
        event.reply(number + "");
    }
}
