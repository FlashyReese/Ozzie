package me.wilsonhu.ozzie.commands;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.core.command.Command;
import me.wilsonhu.ozzie.utilities.Tenor;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Random;

public class Clara extends Command {

    public String[] tags = new String[] {"Clara Oswald", "Clara Who", "Clara Oswin", "Oswin Oswald", "Clara Oswin Oswald"};

    public Clara() {
        super(new String[]{"clara"}, ":p much love clara", "%s");
    }

    @Override
    public void onCommand(String full, String[] args, MessageReceivedEvent event, Ozzie ozzie){
        Tenor tenor = new Tenor();
        Random r=new Random();
        int randomNumber=r.nextInt(tags.length);
        int index = r.nextInt(tenor.getItems(tags[randomNumber], ozzie).size()-1);
        String s = tenor.getItems(tags[randomNumber], ozzie).get(index);
        event.getChannel().sendMessage(s).queue();
    }
}