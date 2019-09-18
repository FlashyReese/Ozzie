package me.wilsonhu.ozzie.manager.json.configuration;

import java.util.HashMap;

import me.wilsonhu.ozzie.OzzieManager;

public class ServerSettingsManager {
	
	private HashMap<Long, ServerSettings> serverSettingsList = new HashMap<Long, ServerSettings>();
	private OzzieManager manager;
	
	public ServerSettingsManager(OzzieManager manager) {
		setOzzieManager(manager);
		getOzzieManager().getLogger().info("Server Settings Manager started");
	}
	
	public HashMap<Long, ServerSettings> getServerSettingsList() {
		return serverSettingsList;
	}

	public void setServerSettingsList(HashMap<Long, ServerSettings> serverSettingsList) {
		this.serverSettingsList = serverSettingsList;
	}

	private OzzieManager getOzzieManager() {
		return manager;
	}

	private void setOzzieManager(OzzieManager manager) {
		this.manager = manager;
	}

}
