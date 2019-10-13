package me.wilsonhu.ozzie.manager.json;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import me.wilsonhu.ozzie.OzzieManager;
import me.wilsonhu.ozzie.manager.json.configuration.ServerSettings;
import me.wilsonhu.ozzie.manager.plugin.Plugin;

public class JsonManager {
	private OzzieManager manager;
	
	public JsonManager(OzzieManager manager) {
		this.setOzzieManager(manager);
		getOzzieManager().getLogger().info("Json Manager started");
	}
	
	private void writeJson(String path, String filename, Object object) {
		try {
            Gson gson = new Gson();
            String json = gson.toJson(object);
            if(!new File(path).exists())new File(path).mkdirs();
            FileWriter fw = new FileWriter(path + File.separator + filename + ".json");
            fw.write(json);
            fw.flush();
            fw.close();
        } catch (Exception ex) {}
	}
	
	private <T> T readJson(String path, String filename, Type type) {
		Gson gson = new Gson();
        FileReader fileReader = null;
        BufferedReader buffered = null;
		try {
            fileReader = new FileReader(path + File.separator + filename + ".json");
            buffered = new BufferedReader(fileReader);
            Type t = type;
           	return gson.fromJson(fileReader, t);
        } catch (Exception ex) {
            ex.printStackTrace();
        }finally {
        	try {
				buffered.close();
				fileReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
		return null;
	}
	
	public void writeJsonPlugin(Plugin pl,String filename, Object object) {
		this.writeJson(this.getOzzieManager().getPluginLoader().getDirectory() + File.separator + pl.getName(), filename, object);
	}
	
	public <T> T readJsonPlugin(Plugin pl, String filename, Type type) {
		return readJson(this.getOzzieManager().getPluginLoader().getDirectory() + File.separator + pl.getName(), filename, type);
	}

	public void writeTokenList() {
        try {
            Gson gson = new Gson();
            String json = gson.toJson(this.getOzzieManager().getTokenManager().getTokenList());
            FileWriter fw = new FileWriter("tokenlist.json");
            fw.write(json);
            fw.flush();
            fw.close();
        } catch (Exception ex) {}
    }
	
	public void readTokenList() {
		try {
            Gson gson = new Gson();
            FileReader fileReader = new FileReader("tokenlist.json");
            BufferedReader buffered = new BufferedReader(fileReader);
            Type type = new TypeToken<HashMap<String, String>>(){}.getType();
            this.getOzzieManager().getTokenManager().setTokenList(gson.fromJson(fileReader, type));
            buffered.close();
            fileReader.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
	
	
	public void writeServerSettingsList() {
        try {
            Gson gson = new Gson();
            String json = gson.toJson(this.getOzzieManager().getServerSettingsManager().getServerSettingsList());
            FileWriter fw = new FileWriter("serversettingslist.json");
            fw.write(json);
            fw.flush();
            fw.close();
        } catch (Exception ex) {}
    }
	
	public void readServerSettingsList() {
		try {
            Gson gson = new Gson();
            File file = new File("serversettingslist.json");
            if(!file.exists())return;
            FileReader fileReader = new FileReader(file);
            BufferedReader buffered = new BufferedReader(fileReader);
            Type type = new TypeToken<HashMap<Long, ServerSettings>>(){}.getType();
            this.getOzzieManager().getServerSettingsManager().setServerSettingsList(gson.fromJson(fileReader, type));
            buffered.close();
            fileReader.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
	
	
	public void writeUserPermissionList() {
        try {
            Gson gson = new Gson();
            String json = gson.toJson(this.getOzzieManager().getPermissionManager().getUserPermissionList());
            FileWriter fw = new FileWriter("userpermissions.json");
            fw.write(json);
            fw.flush();
            fw.close();
        } catch (Exception ex) {}
    }
	
	public void readUserPermissionList() {
		try {
            Gson gson = new Gson();
            File file = new File("userpermissions.json");
            if(!file.exists())return;
            FileReader fileReader = new FileReader(file);
            BufferedReader buffered = new BufferedReader(fileReader);
            Type type = new TypeToken<HashMap<Long, ArrayList<String>>>(){}.getType();
            this.getOzzieManager().getPermissionManager().setUserPermissionList(gson.fromJson(fileReader, type));
            buffered.close();
            fileReader.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
	
	public OzzieManager getOzzieManager() {
		return manager;
	}

	public void setOzzieManager(OzzieManager manager) {
		this.manager = manager;
	}
	
}
