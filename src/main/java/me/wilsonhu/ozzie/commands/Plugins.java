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
package me.wilsonhu.ozzie.commands;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.core.command.Command;
import me.wilsonhu.ozzie.core.plugin.PluginModule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class Plugins extends Command {

    public Plugins() {
        super(new String[]{"plugins"}, "", "%s");
        this.setPermission("ozzie.developer");
        this.setCategory("developer");
    }

    @Override
    public void onCommand(String full, String[] args, MessageReceivedEvent event, Ozzie ozzie) throws Exception {
        int page = 0;
        if (full.equalsIgnoreCase(args[0])) {
            page = 1;
        } else {
            page = Integer.parseInt(args[0]);
        }
        double pagelist = ((double) ozzie.getPluginLoader().getConfiguredPlugins().size() / 10D);
        boolean isInt = pagelist == (int) pagelist;
        if (!isInt) {
            pagelist += 1;
        }
        if (page > pagelist) {
            event.getChannel().sendMessage("This page doesn't exist").queue();
            return;
        }
        event.getChannel().sendMessage(getPage(page, event, pagelist, ozzie)).queue();
    }

    public MessageEmbed getPage(int n, MessageReceivedEvent event, double pagen, Ozzie ozzie) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        StringBuilder line = new StringBuilder();
        int page = (n == 0 || n == 1 || n < 1) ? 1 : n;
        int i = 1;
        for (int k = 0; k < 10; k++) {
            int linetrackn = i + (10 * (page - 1));
            if (linetrackn > ozzie.getPluginLoader().getConfiguredPlugins().size()) {
                break;
            }
            PluginModule pluginModule = ozzie.getPluginLoader().getConfiguredPlugins().get(linetrackn - 1);
            if (pluginModule == null) break;
            //Todo: Just fucked the line below after modifying plugin Schema xd it's for the greater good
            //line.append(String.format("%s. [%s %s](%s) by %s\n", linetrackn, pluginModule.getSchema().getName(), pluginModule.getSchema().getVersion(), pluginModule.getSchema().getContact().get("homepage"), pluginModule.getSchema().getAuthors()[0]));
            //line = line + linetrackn + ". [" + au.getInfo().title.trim() + "](" + au.getInfo().uri + ") - `" + getLength(au.getDuration()) + "` - Requested by **" + au.getDJ().getName() + "**\n";
            if (i >= 10) break;
            i++;
        }
        if (line.length() == 0) {
            line = new StringBuilder("Queue List is empty tho");
        }
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Color.orange)
                .setTitle(String.format("%s - %s %s", ozzie.getBotName(), ozzie.getPluginLoader().getConfiguredPlugins().size(), ozzie.getPluginLoader().getConfiguredPlugins().size() > 1 ? "Plugins" : "Plugin"))
                .setDescription(line.toString())
                .setFooter("I can switch pages now, altho this is still buggy Page " + page + "/" + (int) pagen, null);
        return embed.build();
    }
}
