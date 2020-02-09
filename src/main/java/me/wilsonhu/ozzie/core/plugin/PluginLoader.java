package me.wilsonhu.ozzie.core.plugin;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.core.configuration.ConfigurationManager;
import me.wilsonhu.ozzie.schemas.PluginSchema;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PluginLoader {

    private static final int SCHEMA_VERSION = 1;
    private static final Logger log = LogManager.getLogger(PluginLoader.class);

    final ArrayList<PluginModule> pluginsRaw = new ArrayList<PluginModule>();
    private File directory;
    private Ozzie ozzie;

    public PluginLoader(String dir, Ozzie ozzie) throws ClassNotFoundException, IOException{
        this.setDirectory(new File(dir + File.separator + "plugins"));
        this.ozzie = ozzie;
        loadDirectory(this.getDirectory().getAbsolutePath());
    }

    public void addPlugin(PluginSchema schema, Class<?> clazz) {
        pluginsRaw.add(new PluginModule(schema, clazz));
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
                addPlugin(pluginSchema, c);
                if(getOzzie().getI18nManager().getPluginLocalizationControl().containsKey(pluginSchema.getId())){
                    if(!getOzzie().getI18nManager().getPluginLocalizationControl().get(pluginSchema.getId()).equals(pluginSchema.getVersion())) {
                        getOzzie().getI18nManager().getPluginLocalizationControl().put(pluginSchema.getId(), pluginSchema.getVersion());
                        loadLocale(jf, pluginSchema);
                        getOzzie().getI18nManager().updateSavedSettings();
                    }
                }else{
                    getOzzie().getI18nManager().getPluginLocalizationControl().put(pluginSchema.getId(), pluginSchema.getVersion());
                    loadLocale(jf, pluginSchema);
                    getOzzie().getI18nManager().updateSavedSettings();
                }
            }else{
                log.warn("{}: Incompatible schema version: {} current version: {} ", pluginSchema.getName(), pluginSchema.getSchemaVersion(), SCHEMA_VERSION);
            }
        }
    }

    public void loadLocale(JarFile jf, PluginSchema schema) throws IOException {
        final Enumeration<JarEntry> entries = jf.entries();
        while(entries.hasMoreElements()) {
            final JarEntry entry = entries.nextElement();
            final String name = entry.getName();
            if (name.startsWith("locale/") && name.endsWith(".json")) {
                final BufferedReader br = new BufferedReader(new InputStreamReader(jf.getInputStream(entry), "UTF-8"));
                HashMap<String, String> locale = new Gson().fromJson(br, new TypeToken<HashMap<String, String>>(){}.getType());
                String localeName = name.replaceAll("locale/", "").replaceAll(".json", "");
                getOzzie().getConfigurationManager().writeJson(ConfigurationManager.LOCALE_FOLDER + File.separator + localeName, schema.getId(), locale);
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

    public ArrayList<PluginModule> getConfiguredPlugins() throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        final ArrayList<PluginModule> plugins = new ArrayList<PluginModule>();
        for (PluginModule pm: pluginsRaw) {
            Plugin pl = initAsPlugin(pm.getClazz());
            pl.setPluginSchema(pm.getSchema());
            PluginModule module = new  PluginModule(pl, pm.getSchema(), pm.getClazz());
            plugins.add(module);
        }
        return plugins;
    }

    public Plugin initAsPlugin(Class<?> group) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Plugin plugin = (Plugin) group.getDeclaredConstructor().newInstance();
        return plugin;
    }

    public File getDirectory() {
        return directory;
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }

    public Ozzie getOzzie(){
        return ozzie;
    }
}