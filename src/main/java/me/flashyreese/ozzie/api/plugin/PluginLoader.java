package me.flashyreese.ozzie.api.plugin;

import com.google.gson.Gson;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.flashyreese.common.util.FileUtil;
import me.flashyreese.ozzie.Application;
import me.flashyreese.ozzie.api.OzzieApi;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class PluginLoader implements me.flashyreese.common.plugin.PluginLoader {
    private final Gson gson;

    private final File directory;
    private final String jsonFileName;

    private final List<PluginEntryContainer<OzziePlugin>> pluginEntryContainers = new ObjectArrayList<>();

    public PluginLoader(Gson gson, File directory, String jsonFileName) {
        this.gson = gson;
        this.directory = directory;
        this.jsonFileName = jsonFileName;
    }

    private void loadPlugins() {
        this.pluginEntryContainers.clear();
        try {
            this.verifyPlugin(new File(Application.class.getProtectionDomain().getCodeSource().getLocation().toURI()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.searchDirectoryForPlugins(directory);
    }

    @Override
    public void searchDirectoryForPlugins(File directory) {
        FileUtil.createDirectoryIfNotExist(directory);
        Arrays.stream(directory.listFiles()).filter(f -> f.isFile() && f.getName().endsWith(".jar")).forEach(file -> {
            try {
                this.verifyPlugin(file);
            } catch (Exception e) {
                OzzieApi.INSTANCE.getLogger().error("Invalid Plugin: {} with stacktrace of {}", file.getName(), e);
                e.printStackTrace();
            }
        });
    }

    @Override
    public void verifyPlugin(File pluginFile) throws IOException, URISyntaxException {
        PluginMetadataV1 pluginSchema = null;
        BufferedReader bufferedReader;
        if (pluginFile.isFile()) {
            final JarFile pluginJarFile = new JarFile(pluginFile);
            final JarEntry pluginJarFileJarEntry = pluginJarFile.getJarEntry(String.format("%s.json", this.jsonFileName));
            if (pluginJarFileJarEntry == null) {
                pluginJarFile.close();
                //Todo: Generate metadata and load them as lib
                OzzieApi.INSTANCE.getLogger().error("Invalid Plugin: {}", pluginFile.getName());
                return;
            }
            bufferedReader = new BufferedReader(new InputStreamReader(pluginJarFile.getInputStream(pluginJarFileJarEntry), StandardCharsets.UTF_8));
            pluginSchema = this.gson.fromJson(bufferedReader.lines().collect(Collectors.joining()), PluginMetadataV1.class);
            bufferedReader.close();
            pluginJarFile.close();
        } else {
            final URL url = ClassLoader.getSystemClassLoader().getResource(String.format("%s.json", this.jsonFileName));
            if (url != null) {
                final File jsonSchema = new File(url.toURI());
                bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(jsonSchema), StandardCharsets.UTF_8));
                pluginSchema = this.gson.fromJson(bufferedReader.lines().collect(Collectors.joining()), PluginMetadataV1.class);
                bufferedReader.close();
            }
        }
        if (pluginSchema == null)
            return;

        if (pluginSchema.getSchemaVersion() == 1) {
            List<OzziePlugin> plugins = new ObjectArrayList<>();
            pluginSchema.getEntryPoint().entrySet().stream().filter(stringListEntry -> stringListEntry.getKey().equals("main"))
                    .forEach(stringListEntry -> stringListEntry.getValue().forEach(entryPoint -> {
                        try {
                            OzziePlugin plugin = this.createEntryPointInstance(entryPoint, pluginFile, OzziePlugin.class);
                            plugins.add(plugin);
                        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException | ClassNotFoundException | IOException e) {
                            e.printStackTrace();
                            //print invalid class path or couldn't load
                        }
                    }));
            //Todo: valid if id exists before adding, split into plugin with version resolving.
            this.pluginEntryContainers.add(new PluginEntryContainer<>(pluginSchema, pluginFile, plugins));
        } else {
            //Todo: using old or new plugin format, will try load not guaranteed.
            OzzieApi.INSTANCE.getLogger().info("Skipping {}", pluginFile.getName());
        }
    }

    public <T> T createEntryPointInstance(String entryPoint, File pluginFile, Class<T> tClass) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException, ClassNotFoundException {
        URLClassLoader classLoader = new URLClassLoader(new URL[]{pluginFile.toURI().toURL()});
        @SuppressWarnings("unchecked")
        Class<T> entryPointInstance = (Class<T>) Class.forName(entryPoint, true, classLoader);
        classLoader.close();
        return entryPointInstance.getDeclaredConstructor().newInstance();
    }

    public <T> List<PluginEntryContainer<T>> getEntryPointContainer(String entryName, Class<T> classPath) {
        List<PluginEntryContainer<T>> containers = new ObjectArrayList<>();
        this.getPluginEntryContainers().forEach(pluginEntryContainer -> {
            List<T> entryPoints = new ObjectArrayList<>();
            pluginEntryContainer.getPluginMetadata().getEntryPoint().entrySet().stream().filter(entry -> entry.getKey().equals(entryName))
                    .forEach(entryPointContainers -> entryPointContainers.getValue()
                            .forEach(entryPoint -> {
                                try {
                                    T entryPointInstance = this.createEntryPointInstance(entryPoint, pluginEntryContainer.getPluginFile(), classPath);
                                    entryPoints.add(entryPointInstance);
                                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException | ClassNotFoundException | IOException e) {
                                    e.printStackTrace();
                                    //print invalid class path or couldn't load
                                }
                            }));
            containers.add(new PluginEntryContainer<>(pluginEntryContainer.getPluginMetadata(), pluginEntryContainer.getPluginFile(), entryPoints));
        });
        return containers;
    }

    public void registerPlugins() {
        this.loadPlugins();
        this.getPluginEntryContainers().forEach(entry -> entry.getEntryPoints().forEach(plugin -> {
            OzzieApi.INSTANCE.getLogger().info("Initializing {} {}...", entry.getPluginMetadata().getName(), entry.getPluginMetadata().getVersion());
            plugin.initializePlugin();
            try {
                OzzieApi.INSTANCE.getL10nManager().loadLocalizableContainerFromPluginEntryContainer(entry);
            } catch (Exception e) {
                e.printStackTrace();
            }
            OzzieApi.INSTANCE.getLogger().info("Initialized {} {}...", entry.getPluginMetadata().getName(), entry.getPluginMetadata().getVersion());
        }));
    }

    public void unregisterPlugins() {
        this.getPluginEntryContainers().forEach(entry -> entry.getEntryPoints().forEach(plugin -> {
            OzzieApi.INSTANCE.getLogger().info("Terminating {} {}...", entry.getPluginMetadata().getName(), entry.getPluginMetadata().getVersion());
            plugin.terminatePlugin();
            OzzieApi.INSTANCE.getLogger().info("Terminated {} {}...", entry.getPluginMetadata().getName(), entry.getPluginMetadata().getVersion());
        }));
        this.getPluginEntryContainers().clear();
    }

    public File getDirectory() {
        return directory;
    }

    public String getJsonFileName() {
        return jsonFileName;
    }

    public List<PluginEntryContainer<OzziePlugin>> getPluginEntryContainers() {
        return pluginEntryContainers;
    }

    public static class PluginEntryContainer<T> {
        private final PluginMetadataV1 pluginMetadata;
        private final File pluginFile;
        private final List<T> entryPoints;

        public PluginEntryContainer(PluginMetadataV1 pluginMetadata, File pluginFile, List<T> entryPoints) {
            this.pluginMetadata = pluginMetadata;
            this.pluginFile = pluginFile;
            this.entryPoints = entryPoints;
        }

        public PluginMetadataV1 getPluginMetadata() {
            return pluginMetadata;
        }

        public File getPluginFile() {
            return pluginFile;
        }

        public List<T> getEntryPoints() {
            return entryPoints;
        }
    }
}
