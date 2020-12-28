package me.flashyreese.common.plugin.generic.basic;

import me.flashyreese.common.plugin.generic.GenericPluginSchema;

import java.util.List;

public class BasicPluginSchema extends GenericPluginSchema {
    private String name;
    private String description;
    private List<String> authors;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getAuthors() {
        return authors;
    }
}
