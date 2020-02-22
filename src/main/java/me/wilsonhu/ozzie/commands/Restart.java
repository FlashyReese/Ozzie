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
        if(args.length != 0) {
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
        }
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
                String cmd = "java -jar " + currentJar.getPath() + " " + args;
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