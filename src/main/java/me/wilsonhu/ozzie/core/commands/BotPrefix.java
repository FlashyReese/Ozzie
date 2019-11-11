package me.wilsonhu.ozzie.core.commands;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.manager.command.Command;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class BotPrefix extends Command{
	public BotPrefix() {
		super(new String[]{"botprefix"}, "prefix", "%s (Guild ID) <Prefix>");
		this.setCategory("settings");
		this.setGuildOnly(true);//Probably can fix this to make it work with PMs
		this.setPermission("ozzie.changebotprefix");
	}

	@Override
	public void onCommand(String full, String split, MessageReceivedEvent event, Ozzie ozzie) throws Exception {
		long guildID = event.getGuild().getIdLong();
		String prefix = "";
		if(split.contains(" ") && split != full) {
			guildID = Long.parseLong(split.split(" ")[0]);
			prefix = split.split(" ")[1];
		}else {
			prefix = split;
		}
		ozzie.getOzzieManager().getServerSettingsManager().getServerSettingsList().get(guildID).setCustomBotPrefix(prefix);
		
		Guild g = ozzie.getJDA().getGuildById(guildID);
		event.getChannel().sendMessage(g.getName() + "'s Command Prefix has been set to `" + ozzie.getOzzieManager().getServerSettingsManager().getServerSettingsList().get(guildID).getCustomBotPrefix() + "`.").queue();
		ozzie.getOzzieManager().getJsonManager().writeServerSettingsList();
	}
}
