package me.wilsonhu.ozzie.core.commands;


import java.awt.Color;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.manager.command.Command;
import me.wilsonhu.ozzie.manager.command.CommandCategory;
import me.wilsonhu.ozzie.manager.plugin.Plugin;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Plugins extends Command{

	public Plugins() {
		super(new String[] {"plugins", "plugin"}, "List Plugins", "%s | %s <Plugin Name>");
		this.setCategory(CommandCategory.INFORMATION);
		this.setPermission("ozzie.viewplugins");
	}

	@Override
	public void onCommand(String full, String split, MessageReceivedEvent event, Ozzie ozzie) throws Exception {
		if(full.equalsIgnoreCase(this.getNames()[0])) {
			String line = "";
			for(Plugin p: ozzie.getOzzieManager().getLoadedPluginList()) {
				line = line + "`" + p.getName() + "` ";
			}
			EmbedBuilder embed = new EmbedBuilder()
					.setColor(Color.orange)
					.setTitle("List of Plugins")
					.setDescription(line)
					.setFooter("Beta Work In Progress", null);
			event.getChannel().sendMessage(embed.build()).queue();
		}
	}
}
