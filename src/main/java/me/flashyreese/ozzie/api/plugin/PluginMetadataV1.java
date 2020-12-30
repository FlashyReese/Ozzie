package me.flashyreese.ozzie.api.plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PluginMetadataV1 extends SchematicVersion {

    private String id;
    private String name;
    private String description;
    private Map<String, String> authors = new HashMap<>();
    private Map<String, String> contacts = new HashMap<>();
    private String license;
    private String version;
    private Map<String, List<String>> entryPoint = new HashMap<>();

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, String> getAuthors() {
        return authors;
    }

    public Map<String, String> getContacts() {
        return contacts;
    }

    public String getLicense() {
        return license;
    }

    public String getVersion() {
        return version;
    }

    public Map<String, List<String>> getEntryPoint() {
        return entryPoint;
    }
}
