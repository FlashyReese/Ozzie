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
import me.wilsonhu.ozzie.core.command.CommandType;
import me.wilsonhu.ozzie.core.i18n.ParsableText;
import me.wilsonhu.ozzie.core.i18n.TranslatableText;
import me.wilsonhu.ozzie.schemas.ServerUserPermissionSchema;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.ArrayList;

public class Help extends Command {

    public Help() {
        super(new String[]{"help"}, "ozzie.helpdesc", "%s | %s <commandname>");
        this.setCommandTypes(CommandType.SERVER, CommandType.USER);
    }

    @Override
    public void onCommand(String full, String[] args, MessageReceivedEvent event, Ozzie ozzie) throws Exception {
        ServerUserPermissionSchema serverUserPermissionSchema = ozzie.getConfigurationManager().getMongoDBConnectionFactory().retrieveServerUserPermission(event.getGuild().getIdLong(), event.getAuthor().getIdLong());
        ArrayList<Command> allCommands = new ArrayList<Command>();
        allCommands.addAll(ozzie.getCommandManager().getCommands());
        allCommands.addAll(ozzie.getCommandManager().getPluginCommands());
        if(full.equalsIgnoreCase(args[0])) {
            EmbedBuilder embed = new EmbedBuilder().setColor(Color.orange).setTitle(new ParsableText(new TranslatableText("ozzie.helpfollowing", event), ozzie.getBotName()).toString());
            int adder = 0;
            for(String cc : ozzie.getCommandManager().getCategoryList()) {
                String name = cc.substring(0, 1).toUpperCase() + cc.substring(1).toLowerCase();
                String line = "";
                for(Command cmd: allCommands) {
                    if(!cmd.isHidden()) {
                        if(ozzie.getConfigurationManager().hasPermission(cmd.getPermission(), serverUserPermissionSchema)) {
                            if(cmd.getAsCategory().equalsIgnoreCase(cc)) {
                                if(event.isFromGuild()) {
                                    line = line + String.format("`%s` ", cmd.getNames()[0]);
                                }else if(event.isFromType(ChannelType.PRIVATE) /*&& !cmd.isGuildOnly()*/){
                                    line = line + String.format("`%s` ", cmd.getNames()[0]);
                                }
                                adder++;
                            }
                        }
                    }
                }
                if(!line.isEmpty()) {
                    embed.addField(new ParsableText(new TranslatableText("ozzie.cmds", event), name).toString(), line, false);
                }
            }
            embed.setFooter(new ParsableText(new TranslatableText("ozzie.helptotal", event), Integer.toString(adder)).toString(), null);
            event.getChannel().sendMessage(embed.build()).queue();
        }else{
            String cmdName = args[0];
            for(Command c: allCommands){
                if(cmdName.startsWith(c.getNames()[0].toLowerCase())){
                    event.getChannel().sendMessage(c.getHelpEmblem(event)).queue();
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