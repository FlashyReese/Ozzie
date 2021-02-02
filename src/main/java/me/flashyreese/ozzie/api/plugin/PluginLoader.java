package me.flashyreese.ozzie.api.plugin;

import com.google.gson.Gson;
import com.vdurmont.semver4j.Semver;
import me.flashyreese.common.util.FileUtil;
import me.flashyreese.ozzie.Application;
import me.flashyreese.ozzie.api.OzzieApi;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class PluginLoader {
    private final Gson gson;

    private final File directory;
    private final String jsonFileName;

    private final List<PluginEntryContainer<Plugin>> pluginEntryContainers = new ArrayList<>();

    public PluginLoader(Gson gson, File directory, String jsonFileName) {
        this.gson = gson;
        this.directory = directory;
        this.jsonFileName = jsonFileName;
    }

    private void loadPlugins() {
        this.pluginEntryContainers.clear();
        this.searchDirectoryForPlugins(directory);
        try {
            this.verifyPlugin(new File(Application.class.getProtectionDomain().getCodeSource().getLocation().toURI()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    public void verifyPlugin(File pluginFile) throws IOException, URISyntaxException {
        PluginMetadataV1 pluginMetadata = null;
        BufferedReader bufferedReader;
        // Todo: Instead of deserializing PluginMetadataV1, deserialize SchematicVersion check version, have a Function Map to deserialize accordingly
        // Also
        if (pluginFile.isFile()) {
            final JarFile pluginJarFile = new JarFile(pluginFile);
            final JarEntry pluginJarFileJarEntry = pluginJarFile.getJarEntry(String.format("%s.json", this.jsonFileName));
            if (pluginJarFileJarEntry == null) {
                pluginJarFile.close();
                OzzieApi.INSTANCE.getLogger().error("Invalid Plugin: {}", pluginFile.getName());
                return;
            }
            bufferedReader = new BufferedReader(new InputStreamReader(pluginJarFile.getInputStream(pluginJarFileJarEntry), StandardCharsets.UTF_8));
            pluginMetadata = this.gson.fromJson(bufferedReader.lines().collect(Collectors.joining()), PluginMetadataV1.class);
            bufferedReader.close();
            pluginJarFile.close();
        } else {
            final URL url = ClassLoader.getSystemClassLoader().getResource(String.format("%s.json", this.jsonFileName));
            if (url != null) {
                final File jsonSchema = new File(url.toURI());
                bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(jsonSchema), StandardCharsets.UTF_8));
                pluginMetadata = this.gson.fromJson(bufferedReader.lines().collect(Collectors.joining()), PluginMetadataV1.class);
                bufferedReader.close();
            }
        }
        if (pluginMetadata == null)
            return;

        if (pluginMetadata.getSchemaVersion() == 1) {
            if (pluginMetadata.getId() == null) {
                OzzieApi.INSTANCE.getLogger().error("Missing identifier for {}", pluginFile.getName());
                return;
            }

            PluginMetadataV1 finalPluginMetadata = pluginMetadata;
            Optional<PluginEntryContainer<Plugin>> optional = this.pluginEntryContainers.stream().filter(container -> container.getPluginMetadata().getId().equals(finalPluginMetadata.getId())).findFirst();

            if (optional.isPresent()) {
                PluginEntryContainer<Plugin> existingPluginEntryContainer = optional.get();
                Semver existingSemver = new Semver(existingPluginEntryContainer.getPluginMetadata().getVersion());
                Semver currentSemver = new Semver(pluginMetadata.getVersion());
                if (existingSemver.isEquivalentTo(currentSemver)) {
                    OzzieApi.INSTANCE.getLogger().warn("Skipping {} {} as {} {} is already loaded.", pluginMetadata.getId(), pluginMetadata.getVersion(), existingPluginEntryContainer.getPluginMetadata().getId(), existingPluginEntryContainer.getPluginMetadata().getVersion());
                    return;
                } else if (existingSemver.isGreaterThan(currentSemver)) {
                    OzzieApi.INSTANCE.getLogger().warn("Skipping {} {} as a newer {} {} is already loaded.", pluginMetadata.getId(), pluginMetadata.getVersion(), existingPluginEntryContainer.getPluginMetadata().getId(), existingPluginEntryContainer.getPluginMetadata().getVersion());
                    return;
                }
            }

            List<PluginClassLoaderContainer<Plugin>> plugins = new ArrayList<>();
            pluginMetadata.getEntryPoint()
                    .entrySet()
                    .stream()
                    .filter(stringListEntry -> stringListEntry.getKey().equals("main"))
                    .forEach(stringListEntry -> stringListEntry.getValue().forEach(entryPoint -> {
                        PluginClassLoaderContainer<Plugin> plugin = null;
                        try {
                            plugin = this.createEntryPointInstance(entryPoint, pluginFile, Plugin.class);
                        } catch (NoSuchMethodException | MalformedURLException | IllegalAccessException | InvocationTargetException | InstantiationException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        plugins.add(plugin);
                    }));
            this.pluginEntryContainers.add(new PluginEntryContainer<>(pluginMetadata, pluginFile, plugins));
        } else {
            OzzieApi.INSTANCE.getLogger().info("Skipping {}", pluginFile.getName());
        }
    }

    public <T> PluginClassLoaderContainer<T> createEntryPointInstance(String entryPoint, File pluginFile, Class<T> tClass) throws NoSuchMethodException, MalformedURLException, IllegalAccessException, InvocationTargetException, InstantiationException, ClassNotFoundException {
        URLClassLoader classLoader = new URLClassLoader(new URL[]{pluginFile.toURI().toURL()});
        @SuppressWarnings("unchecked")
        Class<T> entryPointInstance = (Class<T>) Class.forName(entryPoint, true, classLoader);
        return new PluginClassLoaderContainer<>(classLoader, entryPointInstance.getDeclaredConstructor().newInstance());
    }

    public <T> List<PluginEntryContainer<T>> getEntryPointContainer(String entryName, Class<T> classPath) {
        List<PluginEntryContainer<T>> containers = new ArrayList<>();
        this.pluginEntryContainers.forEach(pluginEntryContainer -> {
            List<PluginClassLoaderContainer<T>> entryPoints = new ArrayList<>();
            pluginEntryContainer.getPluginMetadata()
                    .getEntryPoint()
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getKey().equals(entryName))
                    .forEach(entryPointContainers -> entryPointContainers.getValue()
                            .forEach(entryPoint -> {
                                try {
                                    PluginClassLoaderContainer<T> entryPointInstance = this.createEntryPointInstance(entryPoint, pluginEntryContainer.getPluginFile(), classPath);
                                    entryPoints.add(entryPointInstance);
                                } catch (NoSuchMethodException | MalformedURLException | IllegalAccessException | InvocationTargetException | InstantiationException | ClassNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }));
            containers.add(new PluginEntryContainer<>(pluginEntryContainer.getPluginMetadata(), pluginEntryContainer.getPluginFile(), entryPoints));
        });
        return containers;
    }

    public void registerPlugins() {
        this.loadPlugins();
        this.getPluginEntryContainers().forEach(this::loadPlugin);
    }

    public void registerPlugin(PluginEntryContainer<Plugin> entry){
        if (!this.pluginEntryContainers.contains(entry)){
            this.loadPlugin(entry);
            this.pluginEntryContainers.add(entry);
        }else{
            OzzieApi.INSTANCE.getLogger().warn("Plugin {} {} already exist in plugin entry containers list...", entry.getPluginMetadata().getName(), entry.getPluginMetadata().getVersion());
        }
    }

    public void loadPlugin(PluginEntryContainer<Plugin> entry){
        entry.getEntryPoints().forEach(plugin -> {
            OzzieApi.INSTANCE.getLogger().info("Initializing {} {}...", entry.getPluginMetadata().getName(), entry.getPluginMetadata().getVersion());
            try {
                plugin.getEntryPointInstance().initializePlugin();
                OzzieApi.INSTANCE.getL10nManager().loadLocalizableContainerFromPluginEntryContainer(entry);
            } catch (Throwable e) {
                try {
                    plugin.getUrlClassLoader().close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                OzzieApi.INSTANCE.getLogger().error("Fail to initialize {} {} with " + e, entry.getPluginMetadata().getName(), entry.getPluginMetadata().getVersion());
                e.printStackTrace();
                return;
            }
            OzzieApi.INSTANCE.getLogger().info("Initialized {} {}...", entry.getPluginMetadata().getName(), entry.getPluginMetadata().getVersion());
        });
    }


    public void unregisterPlugins() {
        this.pluginEntryContainers.forEach(this::unregisterPlugin);
        this.pluginEntryContainers.clear();
    }

    public void unregisterPlugin(PluginEntryContainer<Plugin> pluginEntryContainer){
        if (this.pluginEntryContainers.contains(pluginEntryContainer)){
            pluginEntryContainer.getEntryPoints().forEach(plugin -> {
                OzzieApi.INSTANCE.getLogger().info("Terminating {} {}...", pluginEntryContainer.getPluginMetadata().getName(), pluginEntryContainer.getPluginMetadata().getVersion());
                try {
                    plugin.getEntryPointInstance().terminatePlugin();
                    plugin.getUrlClassLoader().close();
                } catch (Throwable e) {
                    OzzieApi.INSTANCE.getLogger().error("Fail to terminate {} {} with " + e, pluginEntryContainer.getPluginMetadata().getName(), pluginEntryContainer.getPluginMetadata().getVersion());
                    e.printStackTrace();
                    return;
                }
                OzzieApi.INSTANCE.getLogger().info("Terminated {} {}...", pluginEntryContainer.getPluginMetadata().getName(), pluginEntryContainer.getPluginMetadata().getVersion());
            });
            pluginEntryContainer.getEntryPoints().clear();
        }else{
            OzzieApi.INSTANCE.getLogger().warn("Plugin {} {} does not exist in plugin entry containers list...", pluginEntryContainer.getPluginMetadata().getName(), pluginEntryContainer.getPluginMetadata().getVersion());
        }
    }

    public File getDirectory() {
        return directory;
    }

    public String getJsonFileName() {
        return jsonFileName;
    }

    public List<PluginEntryContainer<Plugin>> getPluginEntryContainers() {
        return pluginEntryContainers;
    }

    public static class PluginEntryContainer<T> {
        private final PluginMetadataV1 pluginMetadata;
        private final File pluginFile;
        // Todo: Remove final for entrypoints
        private final List<PluginClassLoaderContainer<T>> entryPoints;

        // Todo: Create constructor without list of entrypoints

        public PluginEntryContainer(PluginMetadataV1 pluginMetadata, File pluginFile, List<PluginClassLoaderContainer<T>> entryPoints) {
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

        public List<PluginClassLoaderContainer<T>> getEntryPoints() {
            return entryPoints;
        }
    }

    public static class PluginClassLoaderContainer<T> {
        private final URLClassLoader urlClassLoader;
        private final T entryPointInstance;

        public PluginClassLoaderContainer(URLClassLoader urlClassLoader, T entryPointInstance) {
            this.urlClassLoader = urlClassLoader;
            this.entryPointInstance = entryPointInstance;
        }

        public URLClassLoader getUrlClassLoader() {
            return urlClassLoader;
        }

        public T getEntryPointInstance() {
            return entryPointInstance;
        }
    }
}
