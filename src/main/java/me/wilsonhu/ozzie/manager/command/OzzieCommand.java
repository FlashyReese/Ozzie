package me.wilsonhu.ozzie.manager.command;

import java.awt.Color;

import com.jagrosh.jdautilities.command.Command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public abstract class OzzieCommand extends Command{
	
	protected String important;
	
	public OzzieCommand(String name, String[] alias,String description, String syntax, boolean guildOnly)
	{
		this.name = name;
		this.aliases = alias;
		this.help = description;
		this.arguments = syntax;
		this.important = "Nothing is important at this point";
		this.guildOnly = guildOnly;
	}
	
	public MessageEmbed getHelpEmblem() {
        EmbedBuilder embed = new EmbedBuilder()
        		.setColor(Color.red)
        		.setTitle(this.toTitleCase(getName()) + " Command")
                .setDescription(this.getHelp())
                //.addField("Permission Level: ", Character.toUpperCase(getLevel().name().toLowerCase().charAt(0)) + getLevel().name().substring(1).toLowerCase(), false)
                .addField("Usage: ", this.getArguments(), false);
        
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
}

