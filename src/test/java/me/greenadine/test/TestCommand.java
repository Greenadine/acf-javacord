package me.greenadine.test;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.JavacordCommandEvent;
import co.aikar.commands.annotation.*;
import co.aikar.commands.javacord.contexts.Member;
import co.aikar.commands.javacord.contexts.UnicodeEmoji;
import org.javacord.api.entity.channel.*;
import org.javacord.api.entity.emoji.CustomEmoji;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;

@CommandAlias("test")
@Description("Various test commands.")
public class TestCommand extends BaseCommand {

    @Default
    public void onCommand(JavacordCommandEvent event) {
        event.reply("Test success!");
    }

    @CatchUnknown
    public void onUnknownSubcommand(JavacordCommandEvent event) {
        event.reply("Unknown subcommand!");
    }

    @Subcommand("ping")
    public void onPing(JavacordCommandEvent event) {
        event.reply("Testing latency...").thenAcceptAsync(message -> {
            double messageTimestamp = message.getCreationTimestamp().toEpochMilli();
            double currentTimestamp = System.currentTimeMillis();
            double ping = Math.abs(Math.round((currentTimestamp - messageTimestamp) / 100));

            message.edit(String.format("My API latency is %.0fms.", ping));
        });
    }

    @Subcommand("sudo")
    public void onSudo(JavacordCommandEvent event, @Require ServerTextChannel channel, String message) {
        event.deleteMessage(); // Delete original message
        channel.sendMessage(message);
    }

    @Subcommand("user")
    public void onUser(JavacordCommandEvent event, @Flags("other") User user) {
        event.reply(user.getName());
    }

    @Subcommand("member")
    public void onMember(JavacordCommandEvent event, @Flags("other") Member member) {
        event.reply(member.getName());
    }

    @Subcommand("channel")
    public void onChannel(JavacordCommandEvent event, Channel channel) {
        event.reply(channel.getIdAsString());
    }

    @Subcommand("textchannel")
    public void onTextChannel(JavacordCommandEvent event, TextChannel channel) {
        event.reply(channel.getIdAsString());
    }

    @Subcommand("servertextchannel")
    public void onServertextchannel(JavacordCommandEvent event, ServerTextChannel channel) {
        event.reply(channel.getMentionTag());
    }

    @Subcommand("voicechannel")
    public void onVoiceChannel(JavacordCommandEvent event, VoiceChannel channel) {
        event.reply(channel.getIdAsString());
    }

    @Subcommand("servervoicechannel")
    public void onServervoicechannel(JavacordCommandEvent event, ServerVoiceChannel channel) {
        event.reply(channel.getName());
    }

    @Subcommand("role")
    public void onRole(JavacordCommandEvent event, Role role) {
        event.reply(role.getName());
    }

    @Subcommand("emoji")
    public void onEmoji(JavacordCommandEvent event, Emoji emoji) {
        event.reply(emoji.getMentionTag());
    }

    @Subcommand("customemoji")
    public void onCustomEmoji(JavacordCommandEvent event, CustomEmoji emoji) {
        event.reply(emoji.getMentionTag());
    }

    @Subcommand("unicodeemoji")
    public void onUnicodeEmoji(JavacordCommandEvent event, UnicodeEmoji emoji) {
        event.reply(emoji.getMentionTag());
    }
}
