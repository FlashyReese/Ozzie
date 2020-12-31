package me.flashyreese.ozzie.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.flashyreese.ozzie.api.command.guild.DiscordCommandSource;
import me.flashyreese.ozzie.api.l10n.ParsableText;
import me.flashyreese.ozzie.api.l10n.TranslatableText;
import me.flashyreese.ozzie.api.command.guild.DiscordCommand;
import me.flashyreese.ozzie.api.command.guild.DiscordCommandManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;

public class PingCommand extends DiscordCommand {
    public PingCommand() {
        super("", "ozzie.ping.description", "ozzie.ping");
    }

    @Override
    public LiteralArgumentBuilder<DiscordCommandSource> getArgumentBuilder() {
        return DiscordCommandManager.literal("ping")
                .requires(this::hasPermission)
                .executes(this::ping);
    }

    private int ping(CommandContext<DiscordCommandSource> commandContext) {
        MessageReceivedEvent event = commandContext.getSource().getEvent();
        long start = System.currentTimeMillis();
        event.getChannel().sendTyping().queue(v -> {
            long ping = System.currentTimeMillis() - start;
            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(Color.ORANGE)
                    .addField(new ParsableText(new TranslatableText("ozzie.ping.latency_to", commandContext), event.getAuthor().getName()).toString(), "`" + ping + " ms`", false)
                    .addField(new TranslatableText("ozzie.ping.latency_to_websocket", commandContext).toString(), "`" + event.getJDA().getGatewayPing() + " ms`", false);

            event.getChannel().sendMessage(embed.build()).queue();
        });
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }
}
