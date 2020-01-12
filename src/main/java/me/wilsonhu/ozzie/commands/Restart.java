package me.wilsonhu.ozzie.commands;

import me.wilsonhu.ozzie.Application;
import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.core.command.Command;
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
    }

    @Override
    public void onCommand(String full, String split, MessageReceivedEvent event, Ozzie ozzie) throws Exception {
        if(!split.isEmpty()) {
            event.getChannel().sendMessage("Restarting with parameters: `" + split + "`").queue();
            log.info("Restarting with parameters: `" + split + "`");
            restartApplication(split, ozzie);
        }else {
            event.getChannel().sendMessage("Restarting").queue();
            log.info("Restarting");
            restartApplication("", ozzie);
        }
    }

    public void restartApplication(String args, Ozzie ozzie) throws IOException, URISyntaxException{
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                File currentJar = null;
                try {
                    currentJar = new File(Application.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                } catch (URISyntaxException e1) {
                    e1.printStackTrace();
                }
                if(!currentJar.getName().endsWith(".jar"))
                    return;
                ozzie.stop();
                String cmd = "java -jar " + currentJar.getPath() + " " + args;

                boolean isWindows = ozzie.getOperatingSystemName().toLowerCase().startsWith("windows");
                String line1 = isWindows ? "cmd.exe" : "/bin/sh";
                String line2 = isWindows ? "/c" : "-c";
                //osascript -e tell application "Terminal" to do script "java -jar test.jar"
                //Todo: Support Unix and macOS
                ProcessBuilder pb = new ProcessBuilder(line1, line2, cmd);
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