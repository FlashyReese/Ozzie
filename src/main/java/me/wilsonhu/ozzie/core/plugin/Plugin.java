package me.wilsonhu.ozzie.core.plugin;

import me.wilsonhu.ozzie.Ozzie;
import net.dv8tion.jda.api.hooks.ListenerAdapter;


public abstract class Plugin extends ListenerAdapter{

    public void onEnable(Ozzie ozzie) {}
    public void onDisable(Ozzie ozzie) {}
}