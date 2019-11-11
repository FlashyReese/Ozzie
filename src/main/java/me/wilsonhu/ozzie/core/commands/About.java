package me.wilsonhu.ozzie.core.commands;


import java.awt.Color;
import java.lang.management.ManagementFactory;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.manager.command.Command;
import me.wilsonhu.ozzie.utilities.SystemInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class About extends Command{
	
	public About() {
		super(new String[] {"about"}, "tells you about the bot", "%s");
		this.setCategory("information");
		this.setPermission("ozzie.info");
	}

	@Override
	public void onCommand(String full, String split, MessageReceivedEvent event, Ozzie ozzie) throws Exception  {
		long users = 0;
		for(Guild guild: ozzie.getJDA().getGuilds()){
			for(Member member: guild.getMembers()){
				if(member.getOnlineStatus().equals(OnlineStatus.ONLINE) || member.getOnlineStatus().equals(OnlineStatus.IDLE) || member.getOnlineStatus().equals(OnlineStatus.DO_NOT_DISTURB)){
					users+=1;
				}
			}
		}
		String dev = "FlashyReese";
		String codev = "Anyone?";
		SystemInfo si = new SystemInfo();
		if(event.isFromGuild()) {
			for(Member m: event.getGuild().getMembers()){
				if(m.getUser().getIdLong() == 141594071033577472L){
					dev = m.getAsMention();
				}
				//if(m.getUser().getIdLong() == Long.parseLong(FlashyBot.getInstance().getDEVIDS()[1])){
					codev = "Looking for one";
				//}
			}
		}
		long millis = ManagementFactory.getRuntimeMXBean().getUptime();
		long seconds = millis / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		long days = hours / 24;
		EmbedBuilder embed = new EmbedBuilder();
		embed.setColor(Color.red)
		.setAuthor("About " + ozzie.getOzzieManager().getBotName(), "https://cutt.ly/Ozzie", event.getJDA().getSelfUser().getAvatarUrl())
		.setThumbnail(event.getJDA().getSelfUser().getAvatarUrl())
		.setDescription("Howdy, I am " + ozzie.getOzzieManager().getBotName() + ", some call me Clara or Oswin. I'm currently being worked on :heart:. Born on 2019-09-17. Try using `"+ ozzie.getOzzieManager().getServerSettingsManager().getServerSettingsList().get(event.getGuild().getIdLong()).getCustomBotPrefix() +"help` to start using my features! \n **[Add Me To A Server](https://cutt.ly/Ozzie)** !!")
		.addField("Lead Developer", dev, true)
		.addField("Co-Developer", codev, true)
		//.addBlankField(true)
		.addField("Uptime", String.format(
				"%d days, %02d hrs, %02d min",
				days, hours % 24, minutes % 60
			), false)
		//.addField("Version", ozzie.getOzzieManager().getBotVersion().getVersion(), false)
		//.addBlankField(true)
		.addField("Guilds", ozzie.getJDA().getGuilds().size() + "", true)
		.addField("Voice Channels", ozzie.getJDA().getVoiceChannels().size() + "", true)
		.addField("Text Channels", ozzie.getJDA().getTextChannels().size() + "", true)
		.addField("Online Users", users + "", true)
		.addField("Shard", ozzie.getJDA().getShardInfo() == null? "Single Instance" : ozzie.getJDA().getShardInfo() + "", true)
		.addField("SystemInfo", "```prolong\n" + si.Info() + "```", false)
		.setFooter("Last Update on 16th of September 2019 at 8:21 PM ", null);
		
		event.getChannel().sendMessage(embed.build()).queue();
	}
}
