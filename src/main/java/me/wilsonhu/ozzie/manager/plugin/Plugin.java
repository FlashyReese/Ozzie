package me.wilsonhu.ozzie.manager.plugin;

import java.util.ArrayList;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.manager.command.Command;
import net.dv8tion.jda.api.hooks.ListenerAdapter;


public abstract class Plugin extends ListenerAdapter{
	
	private String name;
	private String author;
	private String version;
	
	private ArrayList<Command> commands = new ArrayList<Command>();
	
	public Plugin(String name, String version, String author) {
		this.setName(name);
		this.setVersion(version);
		this.setAuthor(author);
	}
	
	public void onEnable(Ozzie ozzie) {}
	public void onDisable(Ozzie ozzie) {}	
	
	public ArrayList<Command> getCommands() {
		return commands;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
}