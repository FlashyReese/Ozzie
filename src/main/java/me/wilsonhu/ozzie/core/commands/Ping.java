package me.wilsonhu.ozzie.core.commands;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.manager.command.Command;
import me.wilsonhu.ozzie.manager.command.CommandCategory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Ping extends Command{
	
	public Ping() {
		super(new String[]{"ping"}, "Requests bot's ping to server", "%s");
		this.setCategory(CommandCategory.INFORMATION);
	}

	@Override
	public void onCommand(String full, String split, MessageReceivedEvent event, Ozzie ozzie) throws Exception {
		long start = System.currentTimeMillis();
		event.getChannel().sendTyping().queue(v -> {
			long ping = System.currentTimeMillis() - start;
			EmbedBuilder embed = new EmbedBuilder()
					.addField("Latency to " + event.getAuthor().getName(), "`" + ping + " ms`", false)
					.addField("Latency to WebSocket", "`" + event.getJDA().getGatewayPing() + " ms`", false);
					
			event.getChannel().sendMessage(embed.build()).queue();
		});
	}

}