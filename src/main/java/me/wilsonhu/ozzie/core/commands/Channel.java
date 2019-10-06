package me.wilsonhu.ozzie.core.commands;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.manager.command.Command;
import me.wilsonhu.ozzie.manager.command.CommandCategory;
import me.wilsonhu.ozzie.manager.command.CommandLevel;
import me.wilsonhu.ozzie.manager.json.configuration.ServerSettings;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Channel extends Command{
	
	public Channel() {
		super(new String[]{"channel"}, "desc", "%s add (Guild ID) <Mentioned TextChannels>\n%s remove (Guild ID) <Mentioned TextChannel>");
		this.setCategory(CommandCategory.SETTINGS);
		this.setLevel(CommandLevel.ADMINISTRATOR);
		this.setGuildOnly(true);//Probably can fix this to make it work with PMs
	}

	@Override
	public void onCommand(String full, String argss, MessageReceivedEvent event, Ozzie ozzie){
		try {
			String[] args = argss.split(" ");
			String args1 = args[0];
			Guild guild = event.getGuild();
			System.out.println(args.length + args[0] + args[1]);
			if(args.length <= 3) {
				guild = ozzie.getJDA().getGuildById(args[1]);
			}
			ServerSettings ss = ozzie.getOzzieManager().getServerSettingsManager().getServerSettingsList().get(guild.getIdLong());
			if(args1.equalsIgnoreCase("add")) {
				TextChannel tc = event.getMessage().getMentionedChannels().get(0);
				if(ss.getAllowedCommandTextChannels().contains(tc.getIdLong())) {
					event.getChannel().sendMessage(tc.getAsMention() + " is already on the allowed command channels").queue();
					return;
				}
				ss.getAllowedCommandTextChannels().add(tc.getIdLong());
				event.getChannel().sendMessage(tc.getAsMention() + " has been added to the allowed command channels").queue();
			}else if(args1.equalsIgnoreCase("remove")) {
				TextChannel tc = event.getMessage().getMentionedChannels().get(0);
				if(!ss.getAllowedCommandTextChannels().contains(tc.getIdLong())) {
					event.getChannel().sendMessage(tc.getAsMention() + " is not on the allowed command channels").queue();
					return;
				}
				ss.getAllowedCommandTextChannels().remove(tc.getIdLong());
				event.getChannel().sendMessage(tc.getAsMention() + " has been removed from the allowed command channels").queue();
			}	
			ozzie.getOzzieManager().getServerSettingsManager().getServerSettingsList().replace(guild.getIdLong(), ss);
			ozzie.getOzzieManager().getJsonManager().writeServerSettingsList();
		}catch(Exception e) {
			e.printStackTrace();
			event.getChannel().sendMessage(this.getHelpEmblem()).queue();
		}
	}

}
