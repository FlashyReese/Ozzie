package me.flashyreese.ozzie.api.plugin;

import me.flashyreese.common.plugin.Plugin;

public interface OzziePlugin extends Plugin {

    void initializePlugin();

    void terminatePlugin();
}
