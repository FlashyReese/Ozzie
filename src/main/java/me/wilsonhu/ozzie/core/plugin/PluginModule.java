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

import me.wilsonhu.ozzie.schemas.PluginSchema;

public class PluginModule{

    private Plugin plugin;
    private PluginSchema schema;
    private Class<?> clazz;

    public PluginModule(Plugin plugin, PluginSchema schema, Class<?> clazz){
        this.setPlugin(plugin);
        this.setSchema(schema);
        this.setClazz(clazz);
    }

    public PluginModule(PluginSchema schema, Class<?> clazz){
        this.setSchema(schema);
        this.setClazz(clazz);
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    public PluginSchema getSchema() {
        return schema;
    }

    public void setSchema(PluginSchema schema) {
        this.schema = schema;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

}