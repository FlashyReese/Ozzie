package me.wilsonhu.ozzie.parameters;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.core.parameter.Parameter;

public class DisablePlugins extends Parameter {

    private boolean isPluginsDisabled = false;

    public DisablePlugins() {
        super("disableplugins", "disableplugins");
    }

    @Override
    public void onRun(String full, String split, Ozzie ozzie) throws Exception {
        isPluginsDisabled = true;
    }

    public boolean isPluginsDisabled(){
        return isPluginsDisabled;
    }
}
