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