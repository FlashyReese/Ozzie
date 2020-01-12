package me.wilsonhu.ozzie.commands;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.core.command.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class InstallPlugin extends Command {

    private static final Logger log = LogManager.getLogger(InstallPlugin.class);

    public InstallPlugin() {
        super(new String[] {"installplugin"}, "Installs plugin via direct upload or URL", "%s\n%s <URL>");
        this.setCategory("developer");
        this.setPermission("ozzie.developer");
    }

    @Override
    public void onCommand(String full, String split, MessageReceivedEvent event, Ozzie ozzie) throws Exception {
        if(!event.getMessage().getAttachments().isEmpty()) {
            if(event.getMessage().getAttachments().get(0).getFileName().endsWith(".jar")) {
                Random r = new Random();
                int code = r.nextInt(9999);
                event.getChannel().sendMessage("Installing Plugin: `" + event.getMessage().getAttachments().get(0).getFileName() + "`. To confirm action please enter: `"+ code + "`.").queue();
                ozzie.getEventWaiter().waitForEvent(MessageReceivedEvent.class,
                        e -> e.getAuthor().equals(event.getAuthor())
                                && e.getChannel().equals(event.getChannel())
                                && !e.getMessage().equals(event.getMessage()),
                        e -> pinConfirm(ozzie, event, e, code),
                        1, TimeUnit.MINUTES, () -> event.getChannel().sendMessage("Sorry, you took too long.").queue());

            }
        }else {
            //Please attach a file
        }
    }

    public void pinConfirm(Ozzie ozzie, MessageReceivedEvent event, MessageReceivedEvent e, int code) {
        if(code == Integer.parseInt(e.getMessage().getContentRaw())) {
            File file = null;
            try {
                file = new File(ozzie.getPluginLoader().getDirectory() + File.separator + event.getMessage().getAttachments().get(0).getFileName());
            } catch (Exception ex){
                ex.printStackTrace();
            }
            assert file != null;
            if(event.getMessage().getAttachments().get(0).downloadToFile(file).isDone()) {
                event.getChannel().sendMessage("Plugin Installed: `" + event.getMessage().getAttachments().get(0).getFileName() + "`").queue();
                log.info("Plugin Installed: " + event.getMessage().getAttachments().get(0).getFileName());
            }
        }else {
            event.getChannel().sendMessage("Unsuccessful Verification").queue();
        }
    }
}
