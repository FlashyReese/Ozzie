package me.wilsonhu.ozzie.schemas;

import java.util.HashMap;

public class PluginSchema {
    private int schemaVersion;
    private String id;
    private String version;
    private String name;
    private String description;
    private String[] authors;
    private HashMap<String, String> contact;
    private String entrypoint;

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public String getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String[] getAuthors() {
        return authors;
    }

    public HashMap<String, String> getContact() {
        return contact;
    }

    public String getEntrypoint() {
        return entrypoint;
    }

}
