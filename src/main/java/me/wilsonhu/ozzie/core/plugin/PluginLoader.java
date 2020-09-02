/*
 * Copyright (C) 2019-2020 Yao Chung Hu / FlashyReese
 *
 * Ozzie is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Ozzie is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Ozzie.  If not, see http://www.gnu.org/licenses/
 *
 */
package me.wilsonhu.ozzie.core.plugin;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.core.AbstractManager;
import me.wilsonhu.ozzie.core.configuration.ConfigurationManager;
import me.wilsonhu.ozzie.schemas.PluginSchema;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class PluginLoader extends AbstractManager {

    public static final int SCHEMA_VERSION = 1;
    private static final Logger log = LogManager.getLogger(PluginLoader.class);

    final ArrayList<PluginModule> pluginsRaw = new ArrayList<PluginModule>();
    private File directory;

    public PluginLoader(String dir, Ozzie ozzie) throws ClassNotFoundException, IOException {
        super(ozzie);
        this.setDirectory(new File(dir + File.separator + "plugins"));
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
        if (je != null) {
            final BufferedReader br = new BufferedReader(new InputStreamReader(jf.getInputStream(je), StandardCharsets.UTF_8));
            JSONObject jsonObject = new JSONObject(br.lines().collect(Collectors.joining()));
            br.close();
            int pluginSchemaVersion = jsonObject.getInt("schemaVersion");
            String pluginName = jsonObject.getString("name");
            if (pluginSchemaVersion == SCHEMA_VERSION) {
                PluginSchema pluginSchema = new Gson().fromJson(jsonObject.toString(), new TypeToken<PluginSchema>() {
                }.getType());
                Class<?> c = Class.forName(pluginSchema.getEntrypoint(), true, new URLClassLoader(new URL[]{dir.toURI().toURL()}));
                addPlugin(pluginSchema, c);
                if (getOzzie().getI18nManager().getPluginLocalizationControl().containsKey(pluginSchema.getId())) {
                    if (!getOzzie().getI18nManager().getPluginLocalizationControl().get(pluginSchema.getId()).equals(pluginSchema.getVersion())) {
                        getOzzie().getI18nManager().getPluginLocalizationControl().put(pluginSchema.getId(), pluginSchema.getVersion());
                    }
                } else {
                    getOzzie().getI18nManager().getPluginLocalizationControl().put(pluginSchema.getId(), pluginSchema.getVersion());
                }
                loadLocale(jf, pluginSchema);
                getOzzie().getI18nManager().updateSavedSettings();
            } else {
                log.warn("{}: Incompatible schema version: {} current schema version: {}", pluginName, pluginSchemaVersion, SCHEMA_VERSION);
                log.info("Please update your client if plugin schema version is higher than current schema version!!!");
            }
        }
        jf.close();
    }

    public void loadLocale(JarFile jf, PluginSchema schema) throws IOException {
        final Enumeration<JarEntry> entries = jf.entries();
        while (entries.hasMoreElements()) {
            final JarEntry entry = entries.nextElement();
            final String name = entry.getName();
            if (name.startsWith("locale/") && name.endsWith(".json")) {
                final BufferedReader br = new BufferedReader(new InputStreamReader(jf.getInputStream(entry), StandardCharsets.UTF_8));
                HashMap<String, String> locale = new Gson().fromJson(br, new TypeToken<HashMap<String, String>>() {
                }.getType());
                String localeName = name.replaceAll("locale/", "").replaceAll(".json", "");
                getOzzie().getConfigurationManager().writeJson(ConfigurationManager.LOCALE_FOLDER + File.separator + localeName, schema.getId(), locale);
            }
        }
    }

    public void loadDirectory(String dird) throws ClassNotFoundException, IOException {
        File dir = new File(dird);
        if (!dir.exists()) dir.mkdirs();
        final File[] files = dir.listFiles();
        assert files != null;
        for (File f : files) {
            if (!f.getName().endsWith(".jar")) continue;
            loadClassAndConfig(f);
        }
    }

    public ArrayList<PluginModule> getConfiguredPlugins() throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        final ArrayList<PluginModule> plugins = new ArrayList<PluginModule>();
        for (PluginModule pm : pluginsRaw) {
            Plugin pl = initAsPlugin(pm.getClazz());
            pl.setPluginSchema(pm.getSchema());
            PluginModule module = new PluginModule(pl, pm.getSchema(), pm.getClazz());
            plugins.add(module);
        }
        return plugins;
    }

    public Plugin initAsPlugin(Class<?> group) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        return (Plugin) group.getDeclaredConstructor().newInstance();
    }

    public File getDirectory() {
        return directory;
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }

    @Override
    public @NotNull String getName() {
        return "Plugin Loader";
    }
}