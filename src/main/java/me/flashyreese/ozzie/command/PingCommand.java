package me.flashyreese.ozzie.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.flashyreese.ozzie.api.command.Command;
import me.flashyreese.ozzie.api.command.CommandManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class PingCommand extends Command {
    public PingCommand(String category, String description, String important) {
        super(category, description, important, "ozzie.ping");
    }

    @Override
    public LiteralArgumentBuilder<MessageReceivedEvent> getArgumentBuilder() {
        return CommandManager.literal("ping").requires(this::hasPermission).executes(commandContext -> {
            MessageReceivedEvent event = commandContext.getSource();
            long start = System.currentTimeMillis();
            event.getChannel().sendTyping().queue(v -> {
                long ping = System.currentTimeMillis() - start;
                EmbedBuilder embed = new EmbedBuilder()
                        .addField("Latency to " + event.getAuthor().getName(), "`" + ping + " ms`", false)
                        .addField("Latency to WebSocket", "`" + event.getJDA().getGatewayPing() + " ms`", false);

                event.getChannel().sendMessage(embed.build()).queue();
            });
            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
        });
    }
}
