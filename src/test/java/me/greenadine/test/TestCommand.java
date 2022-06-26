package me.greenadine.test;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.JavacordCommandEvent;
import co.aikar.commands.annotation.*;
import co.aikar.commands.javacord.contexts.Member;
import co.aikar.commands.javacord.contexts.UnicodeEmoji;
import co.aikar.commands.javacord.util.TestActionRowBuilder;
import org.javacord.api.entity.channel.*;
import org.javacord.api.entity.emoji.CustomEmoji;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;

import java.util.ArrayList;
import java.util.Collection;

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
    public void onSudo(JavacordCommandEvent event, ServerTextChannel channel, String message) {
        event.deleteMessage().thenAccept(m -> channel.sendMessage(message)); // Delete original message
    }

    @Subcommand("user")
    public void onUser(JavacordCommandEvent event, User user, String test) {
        if (user == null) {
            event.reply("No user specified");
        } else {
            event.reply(user.getName());
        }

        event.reply(test);
    }

    @Subcommand("member")
    public void onMember(JavacordCommandEvent event, @Flags("other") Member member) {
        event.reply(member.getDisplayName());
    }

    @Subcommand("humanmember")
    public void onHumanmember(JavacordCommandEvent event, @Flags("other,humanonly") Member member) {
        event.reply(member.getDisplayName());
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

    @Subcommand("actionrow")
    public void onActionrow(JavacordCommandEvent event) {
        TestActionRowBuilder testActionRowBuilder = new TestActionRowBuilder();
        Collection<Button> buttonsRow1 = new ArrayList<>();
        Collection<Button> buttonsRow2 = new ArrayList<>();
        Collection<Button> buttonsRow3 = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            buttonsRow1.add(Button.primary("button_" + (i + 1), "Button " + (i + 1)));
        }
        for (int i = 0; i < 3; i++) {
            buttonsRow2.add(Button.primary("button_" + (i + 1), "Button " + (i + 1)));
        }
        for (int i = 0; i < 4; i++) {
            buttonsRow3.add(Button.primary("button_" + (i + 1), "Button " + (i + 1)));
        }

        testActionRowBuilder.addComponentsToRow(0, buttonsRow1);
        testActionRowBuilder.addComponentsToRow(1, buttonsRow2);
        testActionRowBuilder.addComponentsToRow(2, buttonsRow3);

        testActionRowBuilder.build().setContent("Test").send(event.getChannel());
    }

    @Subcommand("perm")
    @CommandPermission("%perm-mod")
    public void onPerm(JavacordCommandEvent event) {
        event.reply("Yes");
    }

    @Subcommand("integer")
    public void onInteger(JavacordCommandEvent event, @Flags("min=1,max=5") Integer number) {
        event.reply(number + "");
    }
}
