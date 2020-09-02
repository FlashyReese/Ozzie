/*
 * Copyright (C) 2019-2020 Yao Chung Hu / FlashyReese
 *
 * Ozzie is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Ozzie is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Ozzie.  If not, see http://www.gnu.org/licenses/
 *
 */
package me.wilsonhu.ozzie.commands;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.core.command.Command;
import me.wilsonhu.ozzie.core.i18n.ParsableText;
import me.wilsonhu.ozzie.core.i18n.ParsableTranslatableText;
import me.wilsonhu.ozzie.core.i18n.TranslatableText;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.lang.management.ManagementFactory;

public class About extends Command {

    public About() {
        super(new String[]{"about"}, "Information about the bot", "%s");
        this.setCategory("information");
    }

    @Override
    public void onCommand(String full, String[] split, MessageReceivedEvent event, Ozzie ozzie) throws Exception {
        long users = 0;
        for (Guild guild : event.getJDA().getGuildCache()) {
            guild.loadMembers();
            for (Member member : guild.getMemberCache()) {
                if (member.getOnlineStatus().equals(OnlineStatus.ONLINE) || member.getOnlineStatus().equals(OnlineStatus.IDLE) || member.getOnlineStatus().equals(OnlineStatus.DO_NOT_DISTURB)) {
                    users += 1;
                }
            }
        }
        String dev = "FlashyReese";
        String codev = "Anyone?";
        if (event.isFromGuild()) {
            for (Member m : event.getGuild().getMembers()) {
                if (m.getUser().getIdLong() == 141594071033577472L) {
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
                        new TranslatableText("ozzie.uptimeformat", event).toString(),
                        days, hours % 24, minutes % 60
                ), false)//Todo: Figure a way to mirage a custom datetime format per region using settings. orrrrr... using custom cron format
                .addField("Version", ozzie.getClientVersion().toString(), false)
                //.addBlankField(true)
                .addField(new TranslatableText("ozzie.svs", event).toString(), ozzie.getShardManager().getGuilds().size() + "", true)
                .addField(new TranslatableText("ozzie.vcs", event).toString(), ozzie.getShardManager().getVoiceChannels().size() + "", true)
                .addField(new TranslatableText("ozzie.tcs", event).toString(), ozzie.getShardManager().getTextChannels().size() + "", true)
                .addField(new TranslatableText("ozzie.onlineusers", event).toString(), users + "", true)
                .addField(new TranslatableText("ozzie.shard", event).toString(), String.format("%s", ozzie.getShardManager().getShardsTotal() == 1 ? new TranslatableText("ozzie.singleinstance", event).toString() : String.format("%s/%s %s", ozzie.getShardManager().getShardsRunning(), ozzie.getShardManager().getShardsTotal(), ozzie.getShardManager().getShardsQueued() == 0 ? "" : new ParsableTranslatableText(event, "ozzie.queuedshards", Integer.toString(ozzie.getShardManager().getShardsQueued())))), true)
                .setFooter(new TranslatableText("ozzie.bydev", event).toString(), null);
        event.getChannel().sendMessage(embed.build()).queue();
    }
}