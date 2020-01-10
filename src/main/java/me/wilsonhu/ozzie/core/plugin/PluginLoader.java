package me.wilsonhu.ozzie.core.plugin;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.wilsonhu.ozzie.schemas.PluginSchema;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PluginLoader {

    private static final int SCHEMA_VERSION = 1;
    private static final Logger log = LogManager.getLogger(PluginLoader.class);

    final ArrayList<Class<?>> pluginsRaw = new ArrayList<>();
    private File directory;

    public PluginLoader(String dir) throws ClassNotFoundException, IOException{
        this.setDirectory(new File(dir));
        loadDirectory(dir);
    }

    public void addPlugin(Class<?> clazz) {
        pluginsRaw.add(clazz);
    }

    public void loadClassAndConfig(String dir) throws ClassNotFoundException, IOException {
        loadClassAndConfig(new File(dir));
    }

    public void loadClassAndConfig(File dir) throws IOException, ClassNotFoundException {
        final JarFile jf = new JarFile(dir);
        final JarEntry je = jf.getJarEntry("ozzie.plugin.json");
        if(je != null) {
            final BufferedReader br = new BufferedReader(new InputStreamReader(jf.getInputStream(je), "UTF-8"));
            PluginSchema pluginSchema = new Gson().fromJson(br, new TypeToken<PluginSchema>(){}.getType());//Saved for future lang references v:
            if(pluginSchema.getSchemaVersion() == SCHEMA_VERSION){
                Class<?> c = Class.forName(pluginSchema.getEntrypoint(), true, new URLClassLoader(new URL[]{dir.toURI().toURL()}));
                addPlugin(c);
            }else{
                log.warn("{}: Incompatible schema version: {} current version: {} ", pluginSchema.getName(), pluginSchema.getSchemaVersion(), SCHEMA_VERSION);
            }
        }
    }

    public void loadDirectory(String dird) throws ClassNotFoundException, IOException {
        File dir = new File(dird);
        if(!dir.exists())dir.mkdirs();
        final File[] files = dir.listFiles();
        for(File f : files) {
            if(!f.getName().endsWith(".jar"))continue;
            loadClassAndConfig(f);
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
        return plugin;
    }

    public File getDirectory() {
        return directory;
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }
}