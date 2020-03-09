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
import me.wilsonhu.ozzie.core.i18n.TranslatableText;
import me.wilsonhu.ozzie.schemas.ServerSchema;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Objects;

public class Channel extends Command {

    public Channel() {
        super(new String[]{"channel"}, "", "syntax");
        this.setPermission("ozzie.channel");
    }

    @Override
    public void onCommand(String full, String[] args, MessageReceivedEvent event, Ozzie ozzie) throws Exception {
        if(full.equalsIgnoreCase(args[0])){
            event.getChannel().sendMessage(this.getHelpEmblem(event)).queue();
            return;
        }
        if(!event.getMessage().getMentionedChannels().isEmpty() && isCommand(args, "add")){
            ServerSchema serverSchema = ozzie.getConfigurationManager().getServerSettings(event.getGuild().getIdLong());
            for(TextChannel tc: event.getMessage().getMentionedChannels()){
                if(serverSchema.isAllowedCommandTextChannel(tc.getIdLong())){
                    event.getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.tcsallowalready", event), tc.getName()).toString()).queue();
                }else{
                    serverSchema.addCommandTextChannel(tc.getIdLong());
                    event.getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.tcsallow", event), tc.getName()).toString()).queue();
                }
            }
            ozzie.getConfigurationManager().updateServerSettings(serverSchema);
        }else if (!event.getMessage().getMentionedChannels().isEmpty() && isCommand(args, "remove")){
            ServerSchema serverSchema = ozzie.getConfigurationManager().getServerSettings(event.getGuild().getIdLong());
            for(TextChannel tc: event.getMessage().getMentionedChannels()){
                if(!serverSchema.isAllowedCommandTextChannel(tc.getIdLong())){
                    event.getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.tcsdisallowalready", event), tc.getName()).toString()).queue();
                }else{
                    serverSchema.removeCommandTextChannel(tc.getIdLong());
                    event.getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.tcsdisallow", event), tc.getName()).toString()).queue();
                }
            }
            ozzie.getConfigurationManager().updateServerSettings(serverSchema);
        }else if(isCommand(args, "list")){
            ServerSchema serverSchema = ozzie.getConfigurationManager().getServerSettings(event.getGuild().getIdLong());
            EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("List of Allowed Text Channels");
            StringBuilder line = new StringBuilder();
            for (long id: serverSchema.getAllowedCommandTextChannel()){
                if(event.getGuild().getTextChannelById(id) != null){
                    line.append(Objects.requireNonNull(event.getGuild().getTextChannelById(id)).getAsMention()).append("\n");
                }
            }
            embedBuilder.addField("Channels", line.toString(), false);
            event.getChannel().sendMessage(embedBuilder.build()).queue();
        }else{
            event.getChannel().sendMessage(this.getHelpEmblem(event)).queue();
        }
    }
}
