package me.wilsonhu.ozzie.core.commands;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.manager.command.Command;
import me.wilsonhu.ozzie.manager.command.CommandCategory;
import me.wilsonhu.ozzie.manager.command.CommandLevel;
import me.wilsonhu.ozzie.manager.plugin.Plugin;
import me.wilsonhu.ozzie.manager.plugin.PluginLoader;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Reload extends Command{
	public Reload() {
		super(new String[] {"reload"}, "reloading plugins without restarting", "%s");
		this.setCategory(CommandCategory.DEVELOPER);
		this.setLevel(CommandLevel.DEVELOPER);
		this.setPermission("ozzie.developer");
	}

	@Override
	public void onCommand(String full, String split, MessageReceivedEvent event, Ozzie ozzie) throws Exception {
		
		event.getChannel().sendMessage("Reloading").queue();
		this.removeListenersAndCommands(ozzie);
		this.reloadPluginLoader(ozzie);
		this.addListenersAndCommands(ozzie);
		event.getChannel().sendMessage("Reload Complete!").queue();
	}
	
	public void addListenersAndCommands(Ozzie ozzie) {
		for(Plugin pl: ozzie.getOzzieManager().getLoadedPluginList()) {
			ozzie.getOzzieManager().getLogger().info(String.format("Enabling %s %s", pl.getName(), pl.getVersion()));
			pl.onEnable(ozzie);
			ozzie.getJDA().addEventListener(pl);
			ozzie.getOzzieManager().getCommandManager().addCommands(pl);
		}
	}
	
	public void removeListenersAndCommands(Ozzie ozzie) {
		for(Plugin pl: ozzie.getOzzieManager().getLoadedPluginList()) {
			ozzie.getJDA().removeEventListener(pl);
			ozzie.getOzzieManager().getLogger().info(String.format("Disabling %s %s", pl.getName(), pl.getVersion()));
			pl.onDisable(ozzie);
		}
		ozzie.getOzzieManager().getCommandManager().getPluginCommands().clear();
		ozzie.getOzzieManager().getLoadedPluginList().clear();
	}
	
	public void reloadPluginLoader(Ozzie ozzie) {
		try {
			ozzie.getOzzieManager().setPluginLoader(new PluginLoader(ozzie.getOzzieManager(), "plugins", "config"));
			for (Plugin plugin: ozzie.getOzzieManager().getPluginLoader().getConfiguredPlugins()) {
				ozzie.getOzzieManager().getLoadedPluginList().add(plugin);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
