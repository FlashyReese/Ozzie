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
import java.util.*;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class PluginLoader {
    private final Gson gson;

    private final File directory;
    private final String jsonFileName;

    private final List<PluginEntryContainer<Plugin>> pluginEntryContainers = new ArrayList<>();
    private final Map<Integer, Function<File, PluginEntryContainer<Plugin>>> schemaVersionMetadata = new HashMap<>();

    public PluginLoader(Gson gson, File directory, String jsonFileName) {
        this.gson = gson;
        this.directory = directory;
        this.jsonFileName = jsonFileName;
        this.loadSchematicVersionMetas();
    }

    private void loadSchematicVersionMetas(){
        this.schemaVersionMetadata.put(1, pluginFile -> {
            PluginMetadataV1 pluginMetadata = null;
            try {
                BufferedReader bufferedReader;
                if (pluginFile.isFile()) {
                    final JarFile pluginJarFile = new JarFile(pluginFile);
                    final JarEntry pluginJarFileJarEntry = pluginJarFile.getJarEntry(String.format("%s.json", jsonFileName));
                    if (pluginJarFileJarEntry == null) {
                        pluginJarFile.close();
                        OzzieApi.INSTANCE.getLogger().error("Invalid Plugin: {}", pluginFile.getName());
                        return null;
                    }
                    bufferedReader = new BufferedReader(new InputStreamReader(pluginJarFile.getInputStream(pluginJarFileJarEntry), StandardCharsets.UTF_8));
                    pluginMetadata = gson.fromJson(bufferedReader.lines().collect(Collectors.joining()), PluginMetadataV1.class);
                    bufferedReader.close();
                    pluginJarFile.close();
                } else {
                    final URL url = ClassLoader.getSystemClassLoader().getResource(String.format("%s.json", jsonFileName));
                    if (url != null) {
                        final File jsonSchema = new File(url.toURI());
                        bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(jsonSchema), StandardCharsets.UTF_8));
                        pluginMetadata = gson.fromJson(bufferedReader.lines().collect(Collectors.joining()), PluginMetadataV1.class);
                        bufferedReader.close();
                    }
                }
                if (pluginMetadata.getId() == null) {
                    OzzieApi.INSTANCE.getLogger().error("Missing identifier for {}", pluginFile.getName());
                    return null;
                }

                PluginMetadataV1 finalPluginMetadata = pluginMetadata;
                Optional<PluginEntryContainer<Plugin>> optional = pluginEntryContainers.stream().filter(container -> container.getPluginMetadata().asV1().getId().equals(finalPluginMetadata.getId())).findFirst();

                if (optional.isPresent()) {
                    PluginEntryContainer<Plugin> existingPluginEntryContainer = optional.get();
                    Semver existingSemver = new Semver(existingPluginEntryContainer.getPluginMetadata().asV1().getVersion());
                    Semver currentSemver = new Semver(pluginMetadata.getVersion());
                    if (existingSemver.isEquivalentTo(currentSemver)) {
                        OzzieApi.INSTANCE.getLogger().warn("Skipping {} {} as {} {} is already loaded.", pluginMetadata.getId(), pluginMetadata.getVersion(), existingPluginEntryContainer.getPluginMetadata().asV1().getId(), existingPluginEntryContainer.getPluginMetadata().asV1().getVersion());
                        return null;
                    } else if (existingSemver.isGreaterThan(currentSemver)) {
                        OzzieApi.INSTANCE.getLogger().warn("Skipping {} {} as a newer {} {} is already loaded.", pluginMetadata.getId(), pluginMetadata.getVersion(), existingPluginEntryContainer.getPluginMetadata().asV1().getId(), existingPluginEntryContainer.getPluginMetadata().asV1().getVersion());
                        return null;
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return new PluginEntryContainer<>(pluginMetadata, pluginFile);
        });
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

    public <T> List<EntryPointContainer<T>> getEntryPointContainers(PluginEntryContainer<Plugin> pluginEntryContainer){
        List<EntryPointContainer<T>> entryPoints = new ArrayList<>();
        pluginEntryContainer.getPluginMetadata().asV1()
                .getEntryPoint()
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().equals("main"))
                .forEach(entryPointContainers -> entryPointContainers.getValue()
                        .forEach(entryPoint -> {
                            try {
                                EntryPointContainer<T> entryPointInstance = this.createEntryPointInstance(entryPoint, pluginEntryContainer.getPluginFile());
                                entryPoints.add(entryPointInstance);
                            } catch (NoSuchMethodException | MalformedURLException | IllegalAccessException | InvocationTargetException | InstantiationException | ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        }));
        return entryPoints;
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
        SchematicVersionMetadata schematicVersionMetadata = null;
        BufferedReader bufferedReader;
        if (pluginFile.isFile()) {
            final JarFile pluginJarFile = new JarFile(pluginFile);
            final JarEntry pluginJarFileJarEntry = pluginJarFile.getJarEntry(String.format("%s.json", this.jsonFileName));
            if (pluginJarFileJarEntry == null) {
                pluginJarFile.close();
                OzzieApi.INSTANCE.getLogger().error("Invalid Plugin: {}", pluginFile.getName());
                return;
            }
            bufferedReader = new BufferedReader(new InputStreamReader(pluginJarFile.getInputStream(pluginJarFileJarEntry), StandardCharsets.UTF_8));
            schematicVersionMetadata = this.gson.fromJson(bufferedReader.lines().collect(Collectors.joining()), SchematicVersionMetadata.class);
            bufferedReader.close();
            pluginJarFile.close();
        } else {
            final URL url = ClassLoader.getSystemClassLoader().getResource(String.format("%s.json", this.jsonFileName));
            if (url != null) {
                final File jsonSchema = new File(url.toURI());
                bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(jsonSchema), StandardCharsets.UTF_8));
                schematicVersionMetadata = this.gson.fromJson(bufferedReader.lines().collect(Collectors.joining()), SchematicVersionMetadata.class);
                bufferedReader.close();
            }
        }

        if (schematicVersionMetadata == null)
            return;

        Function<File, PluginEntryContainer<Plugin>> function = this.schemaVersionMetadata.getOrDefault(schematicVersionMetadata.getSchemaVersion(), null);

        if (function != null){
            PluginEntryContainer<Plugin> pluginEntryContainer = function.apply(pluginFile);
            this.pluginEntryContainers.add(pluginEntryContainer);
        }else{
            OzzieApi.INSTANCE.getLogger().info("Skipping {}", pluginFile.getName());
        }
    }

    public <T> EntryPointContainer<T> createEntryPointInstance(String entryPoint, File pluginFile) throws NoSuchMethodException, MalformedURLException, IllegalAccessException, InvocationTargetException, InstantiationException, ClassNotFoundException {
        URLClassLoader classLoader = new URLClassLoader(new URL[]{pluginFile.toURI().toURL()});
        @SuppressWarnings("unchecked")
        Class<T> entryPointInstance = (Class<T>) Class.forName(entryPoint, true, classLoader);
        return new EntryPointContainer<>(classLoader, entryPointInstance.getDeclaredConstructor().newInstance());
    }

    public <T> List<PluginEntryContainer<T>> getPluginEntryContainer(String entryName) {
        List<PluginEntryContainer<T>> containers = new ArrayList<>();
        this.pluginEntryContainers.forEach(pluginEntryContainer -> containers.add(new PluginEntryContainer<>(pluginEntryContainer.getPluginMetadata().asV1(), pluginEntryContainer.getPluginFile(), this.getEntryPointContainers(pluginEntryContainer))));
        return containers;
    }

    public void registerPlugins() {
        this.loadPlugins();
        this.getPluginEntryContainers().forEach(this::initializePlugin);
    }

    public void registerPlugin(PluginEntryContainer<Plugin> entry){
        if (!this.pluginEntryContainers.contains(entry)){
            this.initializePlugin(entry);
            this.pluginEntryContainers.add(entry);
        }else{
            OzzieApi.INSTANCE.getLogger().warn("Plugin {} {} already exist in plugin entry containers list...", entry.getPluginMetadata().asV1().getName(), entry.getPluginMetadata().asV1().getVersion());
        }
    }

    public void initializePlugin(PluginEntryContainer<Plugin> pluginEntryContainer){
        pluginEntryContainer.getEntryPoints().addAll(this.getEntryPointContainers(pluginEntryContainer));
        pluginEntryContainer.getEntryPoints().forEach(plugin -> {
            OzzieApi.INSTANCE.getLogger().info("Initializing {} {}...", pluginEntryContainer.getPluginMetadata().asV1().getName(), pluginEntryContainer.getPluginMetadata().asV1().getVersion());
            try {
                plugin.getEntryPointInstance().initializePlugin();
                OzzieApi.INSTANCE.getL10nManager().loadLocalizableContainerFromPluginEntryContainer(pluginEntryContainer);
            } catch (Throwable e) {
                try {
                    plugin.getUrlClassLoader().close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                OzzieApi.INSTANCE.getLogger().error("Fail to initialize {} {} with " + e, pluginEntryContainer.getPluginMetadata().asV1().getName(), pluginEntryContainer.getPluginMetadata().asV1().getVersion());
                e.printStackTrace();
                return;
            }
            OzzieApi.INSTANCE.getLogger().info("Initialized {} {}...", pluginEntryContainer.getPluginMetadata().asV1().getName(), pluginEntryContainer.getPluginMetadata().asV1().getVersion());
        });
    }

    public void unregisterPlugins() {
        this.pluginEntryContainers.forEach(this::unregisterPlugin);
        this.pluginEntryContainers.clear();
    }

    public void unregisterPlugin(PluginEntryContainer<Plugin> pluginEntryContainer){
        if (this.pluginEntryContainers.contains(pluginEntryContainer)){
            pluginEntryContainer.getEntryPoints().forEach(plugin -> {
                OzzieApi.INSTANCE.getLogger().info("Terminating {} {}...", pluginEntryContainer.getPluginMetadata().asV1().getName(), pluginEntryContainer.getPluginMetadata().asV1().getVersion());
                try {
                    plugin.getEntryPointInstance().terminatePlugin();
                    plugin.getUrlClassLoader().close();
                } catch (Throwable e) {
                    OzzieApi.INSTANCE.getLogger().error("Fail to terminate {} {} with " + e, pluginEntryContainer.getPluginMetadata().asV1().getName(), pluginEntryContainer.getPluginMetadata().asV1().getVersion());
                    e.printStackTrace();
                    return;
                }
                OzzieApi.INSTANCE.getLogger().info("Terminated {} {}...", pluginEntryContainer.getPluginMetadata().asV1().getName(), pluginEntryContainer.getPluginMetadata().asV1().getVersion());
            });
            pluginEntryContainer.getEntryPoints().clear();
        }else{
            OzzieApi.INSTANCE.getLogger().warn("Plugin {} {} does not exist in plugin entry containers list...", pluginEntryContainer.getPluginMetadata().asV1().getName(), pluginEntryContainer.getPluginMetadata().asV1().getVersion());
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
        private final SchematicVersionMetadata pluginMetadata;
        private final File pluginFile;
        private List<EntryPointContainer<T>> entryPoints = new ArrayList<>();

        public PluginEntryContainer(SchematicVersionMetadata pluginMetadata, File pluginFile){
            this.pluginMetadata = pluginMetadata;
            this.pluginFile = pluginFile;
        }

        public PluginEntryContainer(SchematicVersionMetadata pluginMetadata, File pluginFile, List<EntryPointContainer<T>> entryPoints) {
            this.pluginMetadata = pluginMetadata;
            this.pluginFile = pluginFile;
            this.entryPoints = entryPoints;
        }

        public SchematicVersionMetadata getPluginMetadata() {
            return pluginMetadata;
        }

        public File getPluginFile() {
            return pluginFile;
        }

        public List<EntryPointContainer<T>> getEntryPoints() {
            return entryPoints;
        }
    }

    public static class EntryPointContainer<T> {
        private final URLClassLoader urlClassLoader;
        private final T entryPointInstance;

        public EntryPointContainer(URLClassLoader urlClassLoader, T entryPointInstance) {
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
