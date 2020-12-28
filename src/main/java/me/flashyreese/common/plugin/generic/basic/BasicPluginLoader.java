package me.flashyreese.common.plugin.generic.basic;

import me.flashyreese.common.plugin.generic.GenericPluginLoader;

import java.io.File;

public class BasicPluginLoader extends GenericPluginLoader<BasicPlugin, BasicPluginSchema> {
    public BasicPluginLoader(File directory, String jsonFileName) {
        super(directory, jsonFileName);
    }
}
