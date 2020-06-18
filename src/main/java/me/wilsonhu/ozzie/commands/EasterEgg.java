package me.wilsonhu.ozzie.commands;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.core.command.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class EasterEgg extends Command {
    public EasterEgg() {
        super(new String[]{"maria","valeria"}, "ozzie.easteregg1", "eggeaster");
    }

    @Override
    public void onCommand(String full, String[] args, MessageReceivedEvent event, Ozzie ozzie) throws Exception {
        event.getChannel().sendMessage("Flashy :heart: Maria").queue();
    }

    @Override
    public boolean isHidden() {
        return true;
    }
}
