package me.wilsonhu.ozzie.manager.command;

import java.awt.Color;

import me.wilsonhu.ozzie.Ozzie;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class Command {
	private String[] names;
	private String description;
	private String syntax;
	private String important;
	private String permission;
	private String category;
	private boolean guildOnly;

	public Command(String[] names, String description, String syntax)
	{
		this.names = names;
		this.description = description;
		this.syntax = syntax;
		this.important = "Nothing is important at this point";
		this.setCategory(CommandCategory.DEFAULT);
		this.setGuildOnly(false);
		this.setPermission("ozzie.default");
	}
	
	public abstract void onCommand(String full, String split, MessageReceivedEvent event, Ozzie ozzie) throws Exception;
	
	public String[] getNames()
	{
		return names;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	public String getSyntax()
	{
		return syntax;
	}
	
	public boolean isHidden()
	{
		return false;
	}
	
	public MessageEmbed getHelpEmblem() {
        EmbedBuilder embed = new EmbedBuilder()
        		.setColor(Color.red)
        		.setTitle(this.toTitleCase(getNames()[0]) + " Command")
                .setDescription(getDescription())
                .addField("Permission Level: ", this.getPermission(), false)
                .addField("Usage: ", getSyntax().replaceAll("%s", getNames()[0]), false);
        embed.addField("Important", getImportant(), false);
        return embed.build();
    }
	
	private String toTitleCase(String input) {
	    StringBuilder titleCase = new StringBuilder();
	    boolean nextTitleCase = true;

	    for (char c : input.toCharArray()) {
	        if (Character.isSpaceChar(c)) {
	            nextTitleCase = true;
	        } else if (nextTitleCase) {
	            c = Character.toTitleCase(c);
	            nextTitleCase = false;
	        }

	        titleCase.append(c);
	    }

	    return titleCase.toString();
	}
	
	public String getImportant() {
		return important;
	}

	public void setImportant(String important) {
		this.important = important;
	}

	@Deprecated
	public CommandCategory getCategory() {
		return CommandCategory.DEPRECATED;
	}
	
	public String getAsCategory() {
		return this.category;
	}
	
	@Deprecated
	public void setCategory(CommandCategory category) {
		this.category = category.name();
	}
	
	public void setCategory(String category) {
		this.category = category.trim();
	}

	public boolean isGuildOnly() {
		return guildOnly;
	}

	public void setGuildOnly(boolean guildOnly) {
		this.guildOnly = guildOnly;
	}

	public String getPermission() {
		return permission;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}
	
}