package me.flashyreese.common.plugin;

import java.io.File;

public interface PluginLoader {
    void searchDirectoryForPlugins(File directory);

    void verifyPlugin(File file) throws Exception;
}
