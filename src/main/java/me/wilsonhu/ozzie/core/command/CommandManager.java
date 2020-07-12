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
package me.wilsonhu.ozzie.core.command;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.commands.Shutdown;
import me.wilsonhu.ozzie.commands.*;
import me.wilsonhu.ozzie.core.i18n.TranslatableText;
import me.wilsonhu.ozzie.core.plugin.PluginModule;
import me.wilsonhu.ozzie.schemas.ServerSchema;
import me.wilsonhu.ozzie.schemas.UserSchema;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;

public class CommandManager {//Fixme: Rewrite this due to mongo implementations I need something faster, and more optimized. Instead of non-returnable method of onCommand from Command.class, I do returnable json format like REST response to do stuff, will need to rewrite most of the plugins but just small changes
    //Fixme: Due to using eventwaiter, I can't make it like a rest response but I can rewrite mongodb handler for faster access
    private static final Logger log = LogManager.getLogger(CommandManager.class);
    private ArrayList<Command> commands;
    private ArrayList<Command> pluginCommands;
    private ArrayList<String> categoryList;
    private Ozzie ozzie;

    public CommandManager(Ozzie ozzie) {
        log.info("Building CommandManager...");
        this.setOzzie(ozzie);
        commands = new ArrayList<Command>();
        pluginCommands = new ArrayList<Command>();
        categoryList = new ArrayList<String>();
        commands.addAll(Arrays.asList(commands()));
        for(Command c: getCommands()) {
            String category = c.getAsCategory().toLowerCase().trim();
            if(!categoryList.contains(category)) {
                categoryList.add(category);
            }
        }
        log.info("CommandManager built!");
    }

    private Command[] commands(){
        return new Command[]{
                //Todo: Permission(Might need to fix permission system xd get it up to LuckPerms standards)
                new About(),
                new Channel(),
                new Clara(),
                new Evaluate(),
                new Help(),
                new InstallPlugin(),
                new Language(),
                new Permission(),
                new Ping(),
                new Plugins(),
                new Prefix(),
                new Reload(),
                new Restart(),
                new Shutdown(),
                new SystemInformation(),
                new Token()
        };
    }

    public void onMessageReceived(MessageReceivedEvent event, String full){
        if(event.getAuthor().isBot() || event.getAuthor().getIdLong() == event.getJDA().getSelfUser().getIdLong()){
            return;
        }
        if(full == null || full.isEmpty()){
            full = event.getMessage().getContentRaw();
        }
        full = full.replaceAll("\\s{2,}", " ").trim();//Trim ends and replace all more than 2 spaces with single space
        try{
            if(event.getChannelType().isGuild()){
                ServerSchema serverSchema = getOzzie().getConfigurationManager().getServerSettings(event.getGuild().getIdLong());
                if(serverSchema.isAllowedCommandTextChannel(event.getChannel().getIdLong())){
                    UserSchema userSchema = getOzzie().getConfigurationManager().getUserSettings(event.getAuthor().getIdLong());
                    if(!serverSchema.isAllowUserCustomCommandPrefix() || userSchema.getCustomCommandPrefix().equals("default")){
                        onCommandPrefix(event, full, serverSchema.getCustomCommandPrefix());
                    }else if(serverSchema.isAllowUserCustomCommandPrefix() && !userSchema.getCustomCommandPrefix().equals("default")){
                        onCommandPrefix(event, full, userSchema.getCustomCommandPrefix());
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    private void onCommandPrefix(MessageReceivedEvent event, String full, String customCommandPrefix) {
        if(!full.equals(customCommandPrefix)){
            if(event.getMessage().getContentRaw().startsWith(customCommandPrefix)){
                full = full.substring(customCommandPrefix.length());
            }
            String[] s;
            if (full.contains(" ")) {
                String[] args = new String[full.split(" ").length-1];
                if (full.split(" ").length - 1 >= 0)
                    System.arraycopy(full.split(" "), 1, args, 0, full.split(" ").length - 1);
                s = args;
            }else{
                s = new String[]{full};
            }
            if(event.getMessage().getContentRaw().startsWith(customCommandPrefix)){
                this.onCommandValidator(getCommands(), s, full, event);
                this.onCommandValidator(getPluginCommands(), s, full, event);
            }
        }
    }

    public void onCommandValidator(ArrayList<Command> list, String[] s, String full, MessageReceivedEvent event) {
        for (Command c: list){
            for (String name: c.getNames()){
                if (full.toLowerCase().startsWith(name.toLowerCase())){
                    if(getOzzie().getConfigurationManager().hasPermission(event.getGuild().getIdLong(), event.getAuthor().getIdLong(), c.getPermission())) {
                        try{
                            c.onCommand(full, s, event, getOzzie());
                        }catch (Exception e){
                            event.getChannel().sendMessage(c.getHelpEmblem(event)).queue();
                            e.printStackTrace();
                        }
                        return;
                    }else {
                        event.getChannel().sendMessage(new TranslatableText("ozzie.insufficientperms", event).toString()).queue();
                    }
                }
            }
        }
    }

    public ArrayList<Command> getCommands()
    {
        return commands;
    }


    public ArrayList<Command> getPluginCommands()
    {
        return pluginCommands;
    }

    public ArrayList<String> getCategoryList(){
        return categoryList;
    }

    public Command getCommand(Class<?extends Command> leCommandClass)
    {
        for (Command c: getCommands())
        {
            if (c.getClass() == leCommandClass)
            {
                return c;
            }
        }
        return null;
    }

    public Command getCommandPlugin(Class<?extends Command> leCommandClass) {
        for (Command c: getPluginCommands())
        {
            if (c.getClass() == leCommandClass)
            {
                return c;
            }
        }
        return null;
    }

    public void addCommands(PluginModule pl) {
        for(Command cmd: pl.getPlugin().getCommands()) {
            if(cmd.getPermission().equalsIgnoreCase("ozzie.default")) {
                cmd.setPermission(String.format("%s.%s", pl.getSchema().getName(), "default").toLowerCase());
            }else {
                cmd.setPermission(String.format("%s.%s", pl.getSchema().getName(), cmd.getPermission()).toLowerCase());
            }
            this.getPluginCommands().add(cmd);
            log.info(String.format("[%s] Loading command %s", pl.getSchema().getName(), cmd.getNames()[0]));
        }
        for(Command c: getPluginCommands()) {
            String category = c.getAsCategory().toLowerCase().trim();
            if(!categoryList.contains(category)) {
                categoryList.add(category);
            }
        }
    }

    private void setOzzie(Ozzie ozzie){
        this.ozzie = ozzie;
    }

    private Ozzie getOzzie(){
        return ozzie;
    }
}