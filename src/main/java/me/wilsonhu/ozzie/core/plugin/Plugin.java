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
package me.wilsonhu.ozzie.core.plugin;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.core.command.Command;
import me.wilsonhu.ozzie.schemas.PluginSchema;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;


public abstract class Plugin extends ListenerAdapter {

    private ArrayList<Command> commands = new ArrayList<>();
    private PluginSchema pluginSchema;

    public void onEnable(Ozzie ozzie) {
    }

    public void onDisable(Ozzie ozzie) {
    }

    public ArrayList<Command> getCommands() {
        return this.commands;
    }


    public PluginSchema getPluginSchema() {
        return this.pluginSchema;
    }

    public void setPluginSchema(PluginSchema pluginSchema) {
        this.pluginSchema = pluginSchema;
    }

}