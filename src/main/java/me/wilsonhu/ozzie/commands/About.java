package me.wilsonhu.ozzie.commands;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.core.command.Command;
import me.wilsonhu.ozzie.core.i18n.ParsableText;
import me.wilsonhu.ozzie.core.i18n.TranslatableText;
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
    }

    @Override
    public void onCommand(String full, String[] split, MessageReceivedEvent event, Ozzie ozzie) throws Exception  {
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
            }
        }
        long millis = ManagementFactory.getRuntimeMXBean().getUptime();
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.red)
                .setAuthor(new ParsableText(new TranslatableText("ozzie.aboutbot", event), ozzie.getBotName()).toString(), "https://cutt.ly/Ozzie", event.getJDA().getSelfUser().getAvatarUrl())
                .setThumbnail(event.getJDA().getSelfUser().getAvatarUrl())
                .setDescription(new ParsableText(new TranslatableText("ozzie.aboutinfo", event), ozzie.getBotName(), (ozzie.getConfigurationManager().getServerSettings(event.getGuild().getIdLong()).isAllowUserCustomCommandPrefix() ? ozzie.getConfigurationManager().getUserSettings(event.getAuthor().getIdLong()).getCustomCommandPrefix() : ozzie.getConfigurationManager().getServerSettings(event.getGuild().getIdLong()).getCustomCommandPrefix()), "https://cutt.ly/Ozzie").toString())
                .addField(new TranslatableText("ozzie.leaddev", event).toString(), dev, true)
                .addField(new TranslatableText("ozzie.codev", event).toString(), codev, true)
                //.addBlankField(true)
                .addField(new TranslatableText("ozzie.uptime", event).toString(), String.format(
                        "%d days, %02d hrs, %02d min",
                        days, hours % 24, minutes % 60
                ), false)//Todo: Figure a way to mirage a custom datetime format per region using settings. orrrrr... using custom cron format
                //.addField("Version", ozzie.getOzzieManager().getBotVersion().getVersion(), false)//Fixme: Build date
                //.addBlankField(true)
                .addField(new TranslatableText("ozzie.svs", event).toString(), ozzie.getShardManager().getGuilds().size() + "", true)
                .addField(new TranslatableText("ozzie.vcs", event).toString(), ozzie.getShardManager().getVoiceChannels().size() + "", true)
                .addField(new TranslatableText("ozzie.tcs", event).toString(), ozzie.getShardManager().getTextChannels().size() + "", true)
                .addField(new TranslatableText("ozzie.onlineusers", event).toString(), users + "", true)
                .addField(new TranslatableText("ozzie.shard", event).toString(), ozzie.getShardManager().getShardsRunning() + "/" + ozzie.getShardManager().getShardsTotal(), true)//Fixme: Queue Shards, too lazy atm xd
                //.addField("System Information", "```markdown\n" + si.getHardware().toString() + "```", false)//Todo: Seperate this for only dev
                .setFooter(new TranslatableText("ozzie.bydev", event).toString(), null);
        event.getChannel().sendMessage(embed.build()).queue();
    }
}