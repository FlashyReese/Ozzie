package me.wilsonhu.ozzie.commands;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.core.command.Command;
import me.wilsonhu.ozzie.core.command.CommandType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.PrintWriter;

public class Reload extends Command {
    public Reload() {
        super(new String[] {"reload"}, "reloading plugins without restarting", "%s");
        this.setCategory("developer");
        this.setPermission("ozzie.developer");
        this.setCommandTypes(CommandType.SERVER, CommandType.USER, CommandType.RCON);
    }

    @Override
    public void onCommand(String full, String[] args, MessageReceivedEvent event, Ozzie ozzie) throws Exception {
        event.getChannel().sendMessage("Reloading").queue();
        ozzie.pluginsReload();
        event.getChannel().sendMessage("Reload Complete!").queue();
    }

    @Override
    public void onCommand(String full, String split, PrintWriter writer, Ozzie ozzie) throws Exception {
        writer.println("Reloading");
        ozzie.pluginsReload();
        writer.println("Reload Complete!");
    }
}