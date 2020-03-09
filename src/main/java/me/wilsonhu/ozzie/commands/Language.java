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

import com.jagrosh.jdautilities.menu.OrderedMenu;
import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.core.command.Command;
import me.wilsonhu.ozzie.core.i18n.ParsableText;
import me.wilsonhu.ozzie.core.i18n.TranslatableText;
import me.wilsonhu.ozzie.schemas.ServerSchema;
import me.wilsonhu.ozzie.schemas.UserSchema;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Language extends Command {

    public Language() {
        super(new String[]{"language"}, "", "%s");
    }

    @Override
    public void onCommand(String full, String[] args, MessageReceivedEvent event, Ozzie ozzie) throws Exception {//Fixme: Definitely can make this look nicer xd
        if(full.equalsIgnoreCase(args[0])){
            ServerSchema serverSchema = ozzie.getConfigurationManager().getServerSettings(event.getGuild().getIdLong());
            if(serverSchema.isAllowUserLocale()){
                UserSchema userSchema = ozzie.getConfigurationManager().getUserSettings(event.getAuthor().getIdLong());
                if(!userSchema.getUserLocale().equals("default")){
                    event.getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.currentlocaleuser", event), event.getAuthor().getName(), ozzie.getI18nManager().getLocaleDisplayName(userSchema.getUserLocale())).toString()).queue();
                }else{
                    defaultServerLocaleCheck(event, ozzie, serverSchema);
                }
            }else{
                defaultServerLocaleCheck(event, ozzie, serverSchema);
            }
        }else if(isCommand(args, "set", "allowuser") && (event.getAuthor().getIdLong() == event.getGuild().getOwnerIdLong() || ozzie.getConfigurationManager().hasPermission(event.getGuild().getIdLong(), event.getAuthor().getIdLong(), "ozzie.developer"))){
            ServerSchema serverSchema = ozzie.getConfigurationManager().getServerSettings(event.getGuild().getIdLong());
            boolean prefix = Boolean.parseBoolean(args[2]);
            if(serverSchema.isAllowUserLocale() == prefix){
                event.getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.servercustomlocalealready", event), (serverSchema.isAllowUserLocale() ? new TranslatableText("ozzie.true").toString() : new TranslatableText("ozzie.false", event).toString())).toString()).queue();
            }else{
                serverSchema.setAllowUserLocale(prefix);
                event.getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.servercustomlocale", event), (serverSchema.isAllowUserLocale() ? new TranslatableText("ozzie.true").toString() : new TranslatableText("ozzie.false", event).toString())).toString()).queue();
                ozzie.getConfigurationManager().updateServerSettings(serverSchema);
            }
        }else if(isCommand(args, "set", "server") && (event.getAuthor().getIdLong() == event.getGuild().getOwnerIdLong() || ozzie.getConfigurationManager().hasPermission(event.getGuild().getIdLong(), event.getAuthor().getIdLong(), "ozzie.developer"))){
                OrderedMenu.Builder builder = new OrderedMenu.Builder();//Fixme: Gonna be careful with this since it only allows up to 10 items as the locales grow, it will likely be a problem
            builder.allowTextInput(true)
                    .useNumbers()
                    .useCancelButton(true)
                    .setText(new ParsableText(new TranslatableText("ozzie.setserverlocaletext", event), event.getGuild().getName()).toString())
                    .setEventWaiter(ozzie.getEventWaiter())
                    .setTimeout(1, TimeUnit.MINUTES)
                    .setColor(Objects.requireNonNull(event.getMember()).getColor())
                    .addChoices(getLocaleChoices(ozzie))
                    .setSelection((msg,i) ->
                    {
                        setServerLocale(ozzie, event, i);
                    })
                    .setCancel((msg) -> {})
                    .setUsers(event.getAuthor());
            builder.build().display(event.getChannel());
        }else if(isCommand(args, "set")){
            ServerSchema serverSchema = ozzie.getConfigurationManager().getServerSettings(event.getGuild().getIdLong());
            if(serverSchema.isAllowUserLocale()){
                OrderedMenu.Builder builder = new OrderedMenu.Builder();
                builder.allowTextInput(true)
                        .useNumbers()
                        .useCancelButton(true)
                        .setText(new ParsableText(new TranslatableText("ozzie.setuserlocaletext", event), event.getAuthor().getName()).toString())
                        .setEventWaiter(ozzie.getEventWaiter())
                        .setTimeout(1, TimeUnit.MINUTES)
                        .setColor(Objects.requireNonNull(event.getMember()).getColor())
                        .addChoices(getLocaleChoices(ozzie))
                        .setSelection((msg,i) ->
                        {
                            setUserLocale(ozzie, event, i);
                        })
                        .setCancel((msg) -> {})
                        .setUsers(event.getAuthor());
                builder.build().display(event.getChannel());
            }else{
                event.getChannel().sendMessage(new TranslatableText("ozzie.serverlocaledisabled", event).toString()).queue();
            }
        }
    }

    private void defaultServerLocaleCheck(MessageReceivedEvent event, Ozzie ozzie, ServerSchema serverSchema) {
        if(!serverSchema.getServerLocale().equals("default")){
            event.getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.currentlocaleserver", event), ozzie.getI18nManager().getLocaleDisplayName(serverSchema.getServerLocale())).toString()).queue();
        }else{
            event.getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.currentlocale", event), ozzie.getI18nManager().getLocaleDisplayName(Locale.getDefault().toString())).toString()).queue();
        }
    }

    public String[] getLocaleChoices(Ozzie ozzie){
        Locale[] locales = ozzie.getI18nManager().getAvailableLocales();
        String[] choices = new String[locales.length];
        for(int i = 0; i < locales.length; i++){
            choices[i] = locales[i].getDisplayName();
        }
        return choices;
    }

    public void setServerLocale(Ozzie ozzie, MessageReceivedEvent event, int selection){
        Locale[] locales = ozzie.getI18nManager().getAvailableLocales();
        ServerSchema serverSchema = ozzie.getConfigurationManager().getServerSettings(event.getGuild().getIdLong());
        String locale = locales[selection-1].toString();
        if(serverSchema.getServerLocale().equalsIgnoreCase(locale)){
            event.getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.serversetlocalealready", event), ozzie.getI18nManager().getLocaleDisplayName(serverSchema.getServerLocale())).toString()).queue();
        }else{
            serverSchema.setServerLocale(locale);
            event.getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.serversetlocale", event), ozzie.getI18nManager().getLocaleDisplayName(serverSchema.getServerLocale())).toString()).queue();
            ozzie.getConfigurationManager().updateServerSettings(serverSchema);
        }
    }

    public void setUserLocale(Ozzie ozzie, MessageReceivedEvent event, int selection){
        Locale[] locales = ozzie.getI18nManager().getAvailableLocales();
        UserSchema userSchema = ozzie.getConfigurationManager().getUserSettings(event.getAuthor().getIdLong());
        String locale = locales[selection-1].toString();
        if(userSchema.getUserLocale().equalsIgnoreCase(locale)){
            event.getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.usersetlocalealready", event), event.getAuthor().getName(), ozzie.getI18nManager().getLocaleDisplayName(userSchema.getUserLocale())).toString()).queue();
        }else{
            userSchema.setUserLocale(locale);
            event.getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.usersetlocale", event), event.getAuthor().getName(), ozzie.getI18nManager().getLocaleDisplayName(userSchema.getUserLocale())).toString()).queue();
            ozzie.getConfigurationManager().updateUserSettings(userSchema);
        }
    }
}
