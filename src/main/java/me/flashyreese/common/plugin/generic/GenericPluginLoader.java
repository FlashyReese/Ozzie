package me.flashyreese.common.plugin.generic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.flashyreese.common.plugin.Plugin;
import me.flashyreese.common.plugin.PluginLoader;
import me.flashyreese.common.util.FileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class GenericPluginLoader<T extends Plugin, U extends GenericPluginSchema> implements PluginLoader {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final String jsonFileName;
    private final Class<U> pluginSchemaPersistentClass;

    private final List<GenericPluginEntryContainer<T, U>> plugins = new ObjectArrayList<>();

    @SuppressWarnings("unchecked")
    public GenericPluginLoader(File directory, String jsonFileName) {
        this.jsonFileName = jsonFileName;
        this.pluginSchemaPersistentClass = (Class<U>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
        this.searchDirectoryForPlugins(directory);
    }

    @Override
    public void searchDirectoryForPlugins(File directory) {
        FileUtil.createDirectoryIfNotExist(directory);
        Arrays.stream(directory.listFiles()).filter(f -> f.isFile() && f.getName().endsWith(".jar")).forEach(file -> {
            try {
                this.verifyPlugin(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void verifyPlugin(File pluginFile) throws IOException {
        final JarFile pluginJarFile = new JarFile(pluginFile);
        final JarEntry pluginJarFileJarEntry = pluginJarFile.getJarEntry(String.format("%s.json", this.jsonFileName));
        try {
            U pluginSchema = this.gson.fromJson(new BufferedReader(new InputStreamReader(pluginJarFile.getInputStream(pluginJarFileJarEntry), StandardCharsets.UTF_8)).lines().collect(Collectors.joining()), this.pluginSchemaPersistentClass);
            T plugin = this.createPluginInstance(pluginSchema.getEntryPoint(), pluginFile);

            this.plugins.add(new GenericPluginEntryContainer<>(plugin, pluginSchema));
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException | ClassNotFoundException | MalformedURLException e) {
            e.printStackTrace();
        }
        pluginJarFile.close();
    }

    public T createPluginInstance(String entryPoint, File pluginFile) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, MalformedURLException, ClassNotFoundException {
        @SuppressWarnings("unchecked")
        Class<T> basicPluginClass = (Class<T>) Class.forName(entryPoint, true, new URLClassLoader(new URL[]{pluginFile.toURI().toURL()}));
        return basicPluginClass.getDeclaredConstructor().newInstance();
    }

    public List<GenericPluginEntryContainer<T, U>> getPlugins() {
        return this.plugins;
    }
}
