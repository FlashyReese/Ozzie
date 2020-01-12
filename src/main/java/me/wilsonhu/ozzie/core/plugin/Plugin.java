package me.wilsonhu.ozzie.core.plugin;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.core.command.Command;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;


public abstract class Plugin extends ListenerAdapter{

    private ArrayList<Command> commands = new ArrayList<Command>();

    public Plugin(){
        //LoadConfig
    }

    public void onEnable(Ozzie ozzie) {}
    public void onDisable(Ozzie ozzie) {}

    public ArrayList<Command> getCommands() {
        return commands;
    }
}