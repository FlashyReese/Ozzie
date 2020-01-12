package me.wilsonhu.ozzie.commands;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.core.command.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Reload extends Command {
    public Reload() {
        super(new String[] {"reload"}, "reloading plugins without restarting", "%s");
        this.setCategory("developer");
        this.setPermission("ozzie.developer");
    }

    @Override
    public void onCommand(String full, String split, MessageReceivedEvent event, Ozzie ozzie) throws Exception {

        event.getChannel().sendMessage("Reloading").queue();
        ozzie.pluginsReload();
        event.getChannel().sendMessage("Reload Complete!").queue();
    }
}