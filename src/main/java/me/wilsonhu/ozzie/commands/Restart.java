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

import me.wilsonhu.ozzie.Application;
import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.core.command.Command;
import me.wilsonhu.ozzie.core.command.CommandType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class Restart extends Command {

    private static final Logger log = LogManager.getLogger(Restart.class);

    public Restart() {
        super(new String[] {"restart"}, "Restarts bot really helpful for loading plugins", "%s\n%s <Custom Parameters>");
        this.setCategory("developer");
        this.setPermission("ozzie.developer");
        this.setCommandTypes(CommandType.SERVER, CommandType.USER, CommandType.RCON);
    }

    @Override
    public void onCommand(String full, String[] args, MessageReceivedEvent event, Ozzie ozzie) throws Exception {
        event.getChannel().sendMessage("Restarting").queue();
        ozzie.restart();//Fixme: Doesn't take in params v: temporal built in fix behaves weird on Linux Systemctl service
        /*if(args.length != 0) {
            StringBuilder programArguments = new StringBuilder();
            for(int i = 1; i < args.length; i++){
                programArguments.append(args[i]).append(" ");
            }
            programArguments = new StringBuilder(programArguments.toString().trim());
            event.getChannel().sendMessage("Restarting with parameters: `" + programArguments.toString() + "`").queue();
            log.info("Restarting with parameters: `" + programArguments.toString() + "`");
            restartApplication(programArguments.toString(), ozzie);
        }else {
            event.getChannel().sendMessage("Restarting").queue();
            log.info("Restarting");
            restartApplication("", ozzie);
        }*/
    }

    /*@Override
    public void onCommand(String full, String split, PrintWriter writer, Ozzie ozzie) throws Exception {
        if(!split.isEmpty()) {
            writer.println("Restarting with parameters: `" + split + "`");
            log.info("Restarting with parameters: `" + split + "`");
            restartApplication(split, ozzie);
        }else {
            writer.println("Restarting");
            log.info("Restarting");
            restartApplication("", ozzie);
        }
    }*/

    public void restartApplication(String args, Ozzie ozzie) throws IOException, URISyntaxException{
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                File currentJar = null;
                try {
                    currentJar = new File(Application.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                } catch (URISyntaxException e1) {
                    e1.printStackTrace();
                }
                assert currentJar != null;
                if(!currentJar.getName().endsWith(".jar"))
                    return;
                ozzie.stop();
                String cmd = "java -jar " + currentJar.getPath() + " " + args;//Fixme: Hmmm odd behaviour in Linux may be due to users
                String os_name = ozzie.getOperatingSystemName().toLowerCase();
                String[] cmd_exec = new String[]{};
                if (os_name.contains("win")){
                    cmd_exec = new String[]{"cmd.exe", "/c", cmd};
                }else if (os_name.contains("mac")){
                    cmd_exec = new String[]{"/usr/bin/open", "-a", cmd};
                }else if (os_name.contains("nix") || os_name.contains("nux")){
                    cmd_exec = new String[]{"/bin/bash", cmd};
                }else if (os_name.contains("sunos")){
                    cmd_exec = new String[]{"/bin/bash", cmd};
                }
                ProcessBuilder pb = new ProcessBuilder(cmd_exec);
                pb.redirectErrorStream(true).inheritIO();
                try {
                    pb.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                log.info("Exitting...");
            }
        });
        System.exit(0);
    }
}