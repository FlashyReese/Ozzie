package me.wilsonhu.ozzie.commands;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.core.command.Command;
import me.wilsonhu.ozzie.core.command.CommandType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.PrintWriter;

public class Shutdown extends Command {
    public Shutdown() {
        super(new String[] {"shutdown"}, "Stops bot", "%s");
        this.setCategory("developer");
        this.setPermission("ozzie.developer");
        this.setCommandTypes(CommandType.SERVER, CommandType.USER, CommandType.RCON);
    }

    @Override
    public void onCommand(String full, String[] args, MessageReceivedEvent event, Ozzie ozzie) throws Exception {
        ozzie.stop();
        System.exit(0);
    }

    @Override
    public void onCommand(String full, String split, PrintWriter writer, Ozzie ozzie) throws Exception {
        ozzie.stop();
        System.exit(0);
    }
}