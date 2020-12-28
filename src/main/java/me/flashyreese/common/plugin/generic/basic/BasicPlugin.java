package me.flashyreese.common.plugin.generic.basic;

import me.flashyreese.common.plugin.Plugin;

public interface BasicPlugin extends Plugin {
    void initializePlugin();

    void terminatePlugin();
}
