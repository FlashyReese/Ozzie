package me.wilsonhu.ozzie.core.commands;

import java.awt.Color;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.manager.command.Command;
import me.wilsonhu.ozzie.manager.command.CommandCategory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Help extends Command{
	
	public Help() {
		super(new String[]{"help"}, "Gives all the list of commands, or gives information about a specific command", "%s | %s <commandname>");
	}

	@Override
	public void onCommand(String full, String split, MessageReceivedEvent event, Ozzie ozzie) throws Exception {
		//Improve to show only commands taht you have access to v: viewing your permission list v:
		if(full.equalsIgnoreCase(this.getNames()[0])) {
			EmbedBuilder embed = new EmbedBuilder().setColor(Color.orange).setTitle("Following Commands for " + ozzie.getOzzieManager().getBotName());
			for(CommandCategory cc : CommandCategory.values()) {
				String name = cc.name().substring(0, 1).toUpperCase() + cc.name().substring(1).toLowerCase();
				String line = "";
				for(Command c: ozzie.getOzzieManager().getCommandManager().getCommands()) {
					if(!c.isHidden()) {
						if(event.isFromType(ChannelType.PRIVATE) && !c.isGuildOnly()) {
							if(c.getCategory() == cc) {
								line = line + "`" + c.getNames()[0] + "` ";
							}
						}else if(event.isFromGuild()){
							if(c.getCategory() == cc) {
								line = line + "`" + c.getNames()[0] + "` ";
							}
						}
					}
				}
				if(line.isEmpty()) {
					line = "Work In Progress";
				}
				embed.addField(name + " Commands", line, false);
			}
			embed.setFooter("Total Amount of Commands -> " + ozzie.getOzzieManager().getCommandManager().getCommands().size(), null);
			event.getChannel().sendMessage(embed.build()).queue();
			return;
		}
		for(Command c: ozzie.getOzzieManager().getCommandManager().getCommands()){
			if(split.toLowerCase().startsWith(c.getNames()[0].toLowerCase())){
				event.getChannel().sendMessage(c.getHelpEmblem()).queue();
			}
		}
	}
	
	@Override
	public boolean isHidden()
	{
		return true;
	}

}
