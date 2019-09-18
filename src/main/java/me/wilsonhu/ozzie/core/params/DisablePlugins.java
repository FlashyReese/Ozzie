package me.wilsonhu.ozzie.core.params;

import me.wilsonhu.ozzie.OzzieManager;
import me.wilsonhu.ozzie.manager.parameter.Param;

public class DisablePlugins extends Param{

	private boolean pluginless = false;
	
	public DisablePlugins() {
		super("disableplugins", "bottoken");
	}

	@Override
	public void onCommand(String full, String[] split, OzzieManager yucenia) throws Exception {
		setPluginless(true);
		yucenia.getLogger().warn("Disabling Plugin Loader");
	}

	public boolean isPluginless() {
		return pluginless;
	}

	public void setPluginless(boolean pluginless) {
		this.pluginless = pluginless;
	}
}
