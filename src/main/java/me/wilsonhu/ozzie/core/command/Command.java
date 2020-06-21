/*
 * Copyright (C) 2019-2020 Yao Chung Hu / FlashyReese
 *
 * Ozzie is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Ozzie is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Ozzie.  If not, see http://www.gnu.org/licenses/
 *
 */
package me.wilsonhu.ozzie.core.command;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.core.i18n.ParsableText;
import me.wilsonhu.ozzie.core.i18n.TranslatableText;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.Arrays;
import java.util.stream.Collectors;

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
        this.setCommandTypes(CommandType.SERVER, CommandType.USER, CommandType.CONSOLE);
    }

    public abstract void onCommand(String full, String[] args, MessageReceivedEvent event, Ozzie ozzie) throws Exception;

    public MessageEmbed getHelpEmblem(MessageReceivedEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Color.red)
                .setTitle(new ParsableText(new TranslatableText("ozzie.cmd", event), this.toTitleCase(getNames()[0])).toString())
                .setDescription(new TranslatableText(getDescription(), event).toString())
                .addField(new TranslatableText("ozzie.perm", event).toString(), this.getPermission(), false)
                .addField(new TranslatableText("ozzie.usage", event).toString(), getSyntax().replaceAll("%s", getNames()[0]), false);
        embed.addField(new TranslatableText("ozzie.important", event).toString(), getImportant(), false);
        return embed.build();
    }

    public boolean isCommand(String[] args, String... evaluateArgs) {
        if (args.length >= evaluateArgs.length) {
            for (int i = 0; i < evaluateArgs.length; i++) {
                if (args[i].equalsIgnoreCase(evaluateArgs[i])) {
                    if (evaluateArgs.length == i + 1) {
                        return true;
                    }
                } else {
                    return false;
                }
            }
        }
        return false;
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

    public boolean isCommandType(CommandType type){
        return Arrays.stream(getCommandTypes()).collect(Collectors.toList()).contains(type);
    }
}