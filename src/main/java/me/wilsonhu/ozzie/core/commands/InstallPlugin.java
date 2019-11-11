package me.wilsonhu.ozzie.core.commands;

import java.io.File;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.manager.command.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class InstallPlugin extends Command{
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
				ozzie.getOzzieManager().getEventWaiter().waitForEvent(MessageReceivedEvent.class, 
						e -> e.getAuthor().equals(event.getAuthor()) 
						&& e.getChannel().equals(event.getChannel()) 
						&& !e.getMessage().equals(event.getMessage()),
						e -> pinConfirm(ozzie, event, e, code),
						1, TimeUnit.MINUTES, () -> event.getChannel().sendMessage("Sorry, you took too long.").queue());
				
			}
		}else {
			
		}
	}
	
	public void pinConfirm(Ozzie ozzie, MessageReceivedEvent event, MessageReceivedEvent e, int code) {
		if(code == Integer.parseInt(e.getMessage().getContentRaw())) {
			File file = new File(ozzie.getOzzieManager().getPluginLoader().getDirectory() + File.separator + event.getMessage().getAttachments().get(0).getFileName());
			if(event.getMessage().getAttachments().get(0).downloadToFile(file) != null) {
				event.getChannel().sendMessage("Plugin Installed: `" + event.getMessage().getAttachments().get(0).getFileName() + "`").queue();
				ozzie.getOzzieManager().getLogger().info("Plugin Installed: " + event.getMessage().getAttachments().get(0).getFileName());
			}
		}else {
			event.getChannel().sendMessage("Unsuccessful Verification").queue();
		}
	}
}
