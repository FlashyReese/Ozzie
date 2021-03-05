package me.flashyreese.ozzie.api.plugin;

public class SchematicVersionMetadata {
    private int schemaVersion;

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public PluginMetadataV1 asV1(){
        return (PluginMetadataV1) this;
    }
}
