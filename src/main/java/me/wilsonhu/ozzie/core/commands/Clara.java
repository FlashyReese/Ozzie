package me.wilsonhu.ozzie.core.commands;

import java.util.Random;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.manager.command.Command;
import me.wilsonhu.ozzie.manager.command.CommandLevel;
import me.wilsonhu.ozzie.utilities.Tenor;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Clara extends Command{
	
	public String[] tags = new String[] {"Clara Oswald", "Clara Who", "Clara Oswin", "Oswin Oswald", "Clara Oswin Oswald"};
	
	public Clara() {
		super(new String[]{"clara"}, "", "%s");
		this.setLevel(CommandLevel.DEVELOPER);
	}

	@Override
	public void onCommand(String full, String argss, MessageReceivedEvent event, Ozzie ozzie){
		Tenor tenor = new Tenor();
		Random r=new Random();
        int randomNumber=r.nextInt(tags.length);
        int index = r.nextInt(tenor.getItems(tags[randomNumber], ozzie.getOzzieManager()).size()-1);
		String s = tenor.getItems(tags[randomNumber], ozzie.getOzzieManager()).get(index);
		event.getChannel().sendMessage(s).queue();
	}
}
