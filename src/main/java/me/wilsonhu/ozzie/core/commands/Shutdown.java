package me.wilsonhu.ozzie.core.commands;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.manager.command.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Shutdown extends Command{
	public Shutdown() {
		super(new String[] {"shutdown"}, "Stops bot", "%s");
		this.setCategory("developer");
		this.setPermission("ozzie.developer");
	}

	@Override
	public void onCommand(String full, String split, MessageReceivedEvent event, Ozzie ozzie) throws Exception {
		System.exit(0);
	}
}
