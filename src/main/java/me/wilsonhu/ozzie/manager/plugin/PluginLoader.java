package me.wilsonhu.ozzie.manager.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import me.wilsonhu.ozzie.OzzieManager;
import me.wilsonhu.ozzie.core.params.DisablePlugins;

public class PluginLoader {
	
	final ArrayList<Class<?>> pluginsRaw = new ArrayList<>(); 
	private OzzieManager manager;
	private String directory;
	
	public PluginLoader(OzzieManager manager, String dir, String config) throws ClassNotFoundException, IOException{
		this.setOzzieManager(manager);
		this.setDirectory(dir);
		if(!((DisablePlugins) manager.getParameterManager().getParam(DisablePlugins.class)).isPluginless()) {
			loadDirectory(dir, config);
			getOzzieManager().getLogger().info("Plugin Loader started");
		}
	}
	
	public void addPlugin(Class<?> clazz) {
		pluginsRaw.add(clazz);
	}
	
	public void loadClassAndConfig(String dir, String configname) throws ClassNotFoundException, IOException {
		loadClassAndConfig(new File(dir), configname);
	}
	
	public void loadClassAndConfig(File dir, String configname) throws IOException, ClassNotFoundException {
		final JarFile jf = new JarFile(dir);
		final JarEntry je = jf.getJarEntry(configname);
		if(je != null) {
			final BufferedReader br = new BufferedReader(new InputStreamReader(jf.getInputStream(je)));
			final HashMap<String, String> data = new HashMap<>();
			String in;
			while((in = br.readLine()) != null) {
				if(in.isEmpty() || in.startsWith("//")) {
					continue;
				}
				final String[] split = in.split("=");
				data.put(split[0], split[1]);
			}
			jf.close();
			br.close();
			Class<?> c = Class.forName(data.get("Main"), true, new URLClassLoader(new URL[]{dir.toURI().toURL()}));
			addPlugin(c);
		}
	}
	
	public void loadDirectory(String dird, String config) throws ClassNotFoundException, IOException {
		File dir = new File(dird);
		if(!dir.exists())dir.mkdirs();
		final File[] files = dir.listFiles();
		for(File f : files) {
			if(!f.getName().endsWith(".jar"))continue;
			loadClassAndConfig(f, config);
		}
	}
	
	public ArrayList<Plugin> getConfiguredPlugins() throws InstantiationException, IllegalAccessException{
		final ArrayList<Plugin> plugins = new ArrayList<Plugin>();
		for (Class<?> key : pluginsRaw) {
		    plugins.add(initAsPlugin(key));
		}
		return plugins;
	}
	
	public Plugin initAsPlugin(Class<?> group) throws InstantiationException, IllegalAccessException {
		Plugin plugin = (Plugin) group.newInstance();
		getOzzieManager().getLogger().info("[Plugin Loader] Found " + plugin.getName() + " " + plugin.getVersion() + " by " + plugin.getAuthor());
		return plugin;
	}

	private OzzieManager getOzzieManager() {
		return manager;
	}

	private void setOzzieManager(OzzieManager manager) {
		this.manager = manager;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}
}