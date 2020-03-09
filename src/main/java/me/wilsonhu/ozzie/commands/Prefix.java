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
import me.wilsonhu.ozzie.schemas.UserSchema;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Prefix extends Command {
    public Prefix() {
        super(new String[]{"prefix"}, "description", "syntax");
    }

    @Override
    public void onCommand(String full, String[] args, MessageReceivedEvent event, Ozzie ozzie) throws Exception {
        if(full.equalsIgnoreCase(args[0])){
            ServerSchema serverSchema = ozzie.getConfigurationManager().getServerSettings(event.getGuild().getIdLong());
            if(serverSchema.isAllowUserCustomCommandPrefix()){
                UserSchema userSchema = ozzie.getConfigurationManager().getUserSettings(event.getAuthor().getIdLong());
                if(!userSchema.getCustomCommandPrefix().equals(ozzie.getDefaultCommandPrefix())){
                    event.getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.currentprefixuser", event), event.getAuthor().getName(), userSchema.getCustomCommandPrefix()).toString()).queue();
                }else{
                    defaultServerPrefixCheck(event, ozzie, serverSchema);
                }
            }else{
                defaultServerPrefixCheck(event, ozzie, serverSchema);
            }
        }else if(isCommand(args, "set", "allowuser")){
            ServerSchema serverSchema = ozzie.getConfigurationManager().getServerSettings(event.getGuild().getIdLong());
            boolean prefix = Boolean.parseBoolean(args[2]);
            if(serverSchema.isAllowUserCustomCommandPrefix() == prefix){
                event.getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.servercustomprefixalready", event), (serverSchema.isAllowUserCustomCommandPrefix() ? new TranslatableText("ozzie.true").toString() : new TranslatableText("ozzie.false", event).toString())).toString()).queue();
            }else{
                serverSchema.setAllowUserCustomCommandPrefix(prefix);
                event.getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.servercustomprefix", event), (serverSchema.isAllowUserCustomCommandPrefix() ? new TranslatableText("ozzie.true").toString() : new TranslatableText("ozzie.false", event).toString())).toString()).queue();
                ozzie.getConfigurationManager().updateServerSettings(serverSchema);
            }
        }else if(isCommand(args, "set", "server")  && (event.getAuthor().getIdLong() == event.getGuild().getOwnerIdLong() || ozzie.getConfigurationManager().hasPermission(event.getGuild().getIdLong(), event.getAuthor().getIdLong(), "ozzie.developer"))){
            ServerSchema serverSchema = ozzie.getConfigurationManager().getServerSettings(event.getGuild().getIdLong());
            String prefix = args[2];
            if(serverSchema.getCustomCommandPrefix().equalsIgnoreCase(prefix)){
                event.getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.serversetprefixalready", event), serverSchema.getServerLocale()).toString()).queue();
            }else{
                serverSchema.setCustomCommandPrefix(prefix);
                event.getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.serversetprefix", event), serverSchema.getServerLocale()).toString()).queue();
                ozzie.getConfigurationManager().updateServerSettings(serverSchema);
            }
        }else if(isCommand(args, "set")){
            ServerSchema serverSchema = ozzie.getConfigurationManager().getServerSettings(event.getGuild().getIdLong());
            if(serverSchema.isAllowUserCustomCommandPrefix()){
                UserSchema userSchema = ozzie.getConfigurationManager().getUserSettings(event.getAuthor().getIdLong());
                String prefix = args[1];
                if(userSchema.getCustomCommandPrefix().equalsIgnoreCase(prefix)){
                    event.getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.usersetcustomprefixalready", event), event.getAuthor().getName(), userSchema.getCustomCommandPrefix()).toString()).queue();
                }else{
                    userSchema.setCustomCommandPrefix(prefix);
                    event.getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.usersetcustomprefix", event), event.getAuthor().getName(), userSchema.getCustomCommandPrefix()).toString()).queue();
                    ozzie.getConfigurationManager().updateUserSettings(userSchema);
                }
            }else{
                event.getChannel().sendMessage(new TranslatableText("ozzie.servercustomprefixdisabled", event).toString()).queue();
            }
        }
    }

    private void defaultServerPrefixCheck(MessageReceivedEvent event, Ozzie ozzie, ServerSchema serverSchema) {
        if(!serverSchema.getCustomCommandPrefix().equals(ozzie.getDefaultCommandPrefix())){
            event.getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.currentprefixserver", event), serverSchema.getCustomCommandPrefix()).toString()).queue();
        }else{
            event.getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.currentprefix", event), serverSchema.getCustomCommandPrefix()).toString()).queue();
        }
    }
}
