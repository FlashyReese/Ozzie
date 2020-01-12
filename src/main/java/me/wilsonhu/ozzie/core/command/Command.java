package me.wilsonhu.ozzie.core.command;

import me.wilsonhu.ozzie.Ozzie;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;

public abstract class Command {
    private String[] names;
    private String description;
    private String syntax;
    private String important;
    private String permission;
    private String category;
    private CommandType[] commandTypes;

    public Command(String[] names, String description, String syntax)
    {
        this.names = names;
        this.description = description;
        this.syntax = syntax;
        this.important = "Nothing is important at this point";
        this.setPermission("ozzie.default");
        this.setCategory("Default");
        this.setCommandTypes(CommandType.SERVER, CommandType.USER);
    }

    public abstract void onCommand(String full, String split, MessageReceivedEvent event, Ozzie ozzie) throws Exception;

    public MessageEmbed getHelpEmblem(MessageReceivedEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Color.red)
                .setTitle(this.toTitleCase(getNames()[0]) + " Command")
                .setDescription(getDescription())
                .addField("Permission: ", this.getPermission(), false)
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

    public String getImportant() {
        return important;
    }

    public void setImportant(String important) {
        this.important = important;
    }

    public String getAsCategory() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category = category.trim();
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public CommandType[] getCommandTypes() {
        return commandTypes;
    }

    public void setCommandTypes(CommandType... commandTypes) {
        this.commandTypes = commandTypes;
    }
}