package me.flashyreese.common.plugin.generic;

import me.flashyreese.common.plugin.Plugin;

public class GenericPluginEntryContainer<T extends Plugin, U extends GenericPluginSchema> {
    private final T plugin;
    private final U pluginSchema;

    public GenericPluginEntryContainer(T plugin, U pluginSchema) {
        this.plugin = plugin;
        this.pluginSchema = pluginSchema;
    }

    public T getPlugin() {
        return plugin;
    }

    public U getPluginSchema() {
        return pluginSchema;
    }
}