package me.wilsonhu.ozzie.commands;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.core.command.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import oshi.SystemInfo;

import java.awt.*;
import java.lang.management.ManagementFactory;

public class About extends Command {

    public About() {
        super(new String[] {"about"}, "Information about the bot", "%s");
        this.setCategory("information");
        this.setPermission("ozzie.info");
    }

    @Override
    public void onCommand(String full, String split, MessageReceivedEvent event, Ozzie ozzie) throws Exception  {
        long users = 0;
        for(Guild guild: ozzie.getShardManager().getGuilds()){
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
                .setAuthor("About " + ozzie.getBotName(), "https://cutt.ly/Ozzie", event.getJDA().getSelfUser().getAvatarUrl())
                .setThumbnail(event.getJDA().getSelfUser().getAvatarUrl())
                .setDescription("Howdy, I am " + ozzie.getBotName() + ", some call me Clara or Oswin. I'm currently being worked on :heart:. Born on 2019-09-17. Try using `"
                        + (ozzie.getConfigurationManager().getServerSettings(event.getGuild().getIdLong()).isAllowUserCustomCommandPrefix() ? ozzie.getConfigurationManager().getUserSettings(event.getAuthor().getIdLong()).getCustomCommandPrefix() : ozzie.getConfigurationManager().getServerSettings(event.getGuild().getIdLong()).getCustomCommandPrefix())
                        +"help` to start using my features! \n **[Add Me To A Server](https://cutt.ly/Ozzie)** !!")
                .addField("Lead Developer", dev, true)
                .addField("Co-Developer", codev, true)
                //.addBlankField(true)
                .addField("Uptime", String.format(
                        "%d days, %02d hrs, %02d min",
                        days, hours % 24, minutes % 60
                ), false)
                //.addField("Version", ozzie.getOzzieManager().getBotVersion().getVersion(), false)
                //.addBlankField(true)
                .addField("Guilds", ozzie.getShardManager().getGuilds().size() + "", true)
                .addField("Voice Channels", ozzie.getShardManager().getVoiceChannels().size() + "", true)
                .addField("Text Channels", ozzie.getShardManager().getTextChannels().size() + "", true)
                .addField("Online Users", users + "", true)
                .addField("Shard", ozzie.getShardManager().getShardsRunning() + "(" + ozzie.getShardManager().getShardsQueued() + "...)/" + ozzie.getShardManager().getShardsTotal(), true)
                .addField("System Information", "```markdown\n" + si.getHardware().toString() + "```", false)
                .setFooter("by FlashyReese", null);
        event.getChannel().sendMessage(embed.build()).queue();
    }
}