package me.wilsonhu.ozzie.core.commands;

import java.awt.Color;
import java.util.ArrayList;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.manager.command.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Help extends Command{
	
	public Help() {
		super(new String[]{"help"}, "Gives all the list of commands, or gives information about a specific command", "%s | %s <commandname>");
	}

	@Override
	public void onCommand(String full, String split, MessageReceivedEvent event, Ozzie ozzie) throws Exception {
		ArrayList<Command> allCommands = new ArrayList<Command>();
		allCommands.addAll(ozzie.getOzzieManager().getCommandManager().getCommands());
		allCommands.addAll(ozzie.getOzzieManager().getCommandManager().getPluginCommands());
		if(full.equalsIgnoreCase(this.getNames()[0])) {
			EmbedBuilder embed = new EmbedBuilder().setColor(Color.orange).setTitle("Following Commands for " + ozzie.getOzzieManager().getBotName());
			int adder = 0;
			for(String cc : ozzie.getOzzieManager().getCommandManager().getCategoryList()) {
				String name = cc.substring(0, 1).toUpperCase() + cc.substring(1).toLowerCase();
				String line = "";
				for(Command cmd: allCommands) {
					if(!cmd.isHidden()) {
						if(ozzie.getOzzieManager().getPermissionManager().hasPermission(event.getAuthor().getIdLong(), cmd.getPermission())) {
							if(cmd.getAsCategory().equalsIgnoreCase(cc)) {
								if(event.isFromGuild()) {
									line = line + String.format("`%s` ", cmd.getNames()[0]);
								}else if(event.isFromType(ChannelType.PRIVATE) && !cmd.isGuildOnly()){
									line = line + String.format("`%s` ", cmd.getNames()[0]);
								}
								adder++;
							}
						}
					}
				}
				if(!line.isEmpty()) {
					embed.addField(String.format("%s Commands", name), line, false);
				}
			}
			embed.setFooter("Total Allowed of Commands -> " + adder, null);
			event.getChannel().sendMessage(embed.build()).queue();
			return;
		}else {
			String cmdName = split.toLowerCase().trim();
			for(Command c: allCommands){
				if(cmdName.startsWith(c.getNames()[0].toLowerCase())){
					event.getChannel().sendMessage(c.getHelpEmblem()).queue();
				}
			}
		}
	}
	
	@Override
	public boolean isHidden()
	{
		return true;
	}

}
