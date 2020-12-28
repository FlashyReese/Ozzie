package me.flashyreese.ozzie.api.l10n;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vdurmont.semver4j.Semver;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.flashyreese.common.util.FileUtil;
import me.flashyreese.common.util.JarUtil;
import me.flashyreese.ozzie.api.plugin.OzziePlugin;
import me.flashyreese.ozzie.api.plugin.PluginLoader;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class L10nManager {
    private final Gson gson;
    private final File file;
    private final String jarLocaleDirectory;

    private final List<LocalizationContainer> localizationContainers = new ObjectArrayList<>();

    public L10nManager(Gson gson, File directory, String jarLocaleDirectory) {
        this.gson = gson;
        this.file = new File(directory + File.separator + "locale.json");
        this.jarLocaleDirectory = jarLocaleDirectory;
        this.localizationContainers.addAll(this.load());
    }

    public String translate(String key, String lang){
        key = key.trim();
        if (key.isEmpty() || key.startsWith(".") || key.endsWith(".") || !key.contains(".")) {
            return key;
        }

        String domain = key.substring(0, key.indexOf('.'));
        Optional<LocalizationContainer> optionalLocalizationContainer = this.localizationContainers.stream().filter(container -> container.getIdentifier().equalsIgnoreCase(domain)).findFirst();
        if (optionalLocalizationContainer.isPresent()){
            LocalizationContainer localizationContainer = optionalLocalizationContainer.get();
            if (localizationContainer.getLocales().containsKey(lang)){
                Map<String, String> langMap = localizationContainer.getLocales().get(lang);
                String name = key.substring(key.indexOf('.') + 1);
                return langMap.getOrDefault(name, key);
            }else{
                return key;
            }
        }else{
            return key;
        }
    }

    public void loadLocalizableContainerFromPluginEntryContainer(PluginLoader.PluginEntryContainer<OzziePlugin> pluginEntryContainer) throws Exception {
        Optional<LocalizationContainer> optionalLocalizableContainer = this.localizationContainers.stream().filter(container -> container.getIdentifier().equals(pluginEntryContainer.getPluginMetadata().getId())).findFirst();
        if (optionalLocalizableContainer.isPresent()) {
            LocalizationContainer localizationContainer = optionalLocalizableContainer.get();
            Semver pluginContainerSemver = new Semver(localizationContainer.getVersion());
            Semver pluginEntryContainerSemver = new Semver(pluginEntryContainer.getPluginMetadata().getVersion());
            if (!pluginContainerSemver.isEqualTo(pluginEntryContainerSemver)) {
                List<Locale> validLocales = this.searchValidLocales(pluginEntryContainer.getPluginFile());
                validLocales.forEach(locale -> {
                    try {
                        Map<String, String> translations = this.getTranslationsForLocale(pluginEntryContainer.getPluginFile(), locale);
                        localizationContainer.getLocales().put(locale.toString().toLowerCase(), translations);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }else{
            Map<String, Map<String, String>> locales = new Object2ObjectOpenHashMap<>();
            List<Locale> validLocales = this.searchValidLocales(pluginEntryContainer.getPluginFile());
            validLocales.forEach(locale -> {
                try {
                    Map<String, String> translations = this.getTranslationsForLocale(pluginEntryContainer.getPluginFile(), locale);
                    locales.put(locale.toString().toLowerCase(), translations);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            this.localizationContainers.add(new LocalizationContainer(pluginEntryContainer.getPluginMetadata().getId(), pluginEntryContainer.getPluginMetadata().getVersion(), locales));
        }
        this.writeChanges();
    }

    private List<Locale> searchValidLocales(File jarFile) throws Exception {
        List<Locale> validLocales = new ObjectArrayList<>();
        if (jarFile.isFile()) {
            final JarFile jar = new JarFile(jarFile);
            final Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                final JarEntry entry = entries.nextElement();
                final String name = entry.getName();
                if (name.startsWith(jarLocaleDirectory + "/") && name.endsWith(".json")) {
                    Locale locale = this.parseTag(JarUtil.getFileName(entry));
                    if (locale != null) {
                        validLocales.add(locale);
                    }
                }
            }
            jar.close();
            return validLocales;
        } else {
            final URL url = ClassLoader.getSystemClassLoader().getResource(jarLocaleDirectory);
            if (url != null) {
                try {
                    final File languages = new File(url.toURI());
                    for (File language : Objects.requireNonNull(languages.listFiles())) {
                        Locale locale = this.parseTag(FileUtil.getFileName(language));
                        if (locale != null) {
                            validLocales.add(locale);
                        }
                    }
                    return validLocales;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return validLocales;
    }

    private Map<String, String> getTranslationsForLocale(File jarFile, Locale locale) throws Exception {
        Map<String, String> translations = new Object2ObjectOpenHashMap<>();
        if (jarFile.isFile()) {
            final JarFile jar = new JarFile(jarFile);
            final Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                final JarEntry entry = entries.nextElement();
                final String name = entry.getName();
                if (name.startsWith(jarLocaleDirectory + "/") && name.endsWith(".json")) {
                    if (JarUtil.getFileName(entry).equalsIgnoreCase(locale.toString())) {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(jar.getInputStream(entry), StandardCharsets.UTF_8));
                        this.deserializeTranslations(translations, bufferedReader);
                        break;
                    }
                }
            }
            jar.close();
            return translations;
        } else {
            final URL url = ClassLoader.getSystemClassLoader().getResource(jarLocaleDirectory);
            if (url != null) {
                try {
                    final File languages = new File(url.toURI());
                    for (File language : Objects.requireNonNull(languages.listFiles())) {
                        if (FileUtil.getFileName(language).equalsIgnoreCase(locale.toString())) {
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(language), StandardCharsets.UTF_8));
                            this.deserializeTranslations(translations, bufferedReader);
                        }
                    }
                    return translations;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return null;
    }

    private void deserializeTranslations(Map<String, String> translations, BufferedReader bufferedReader) throws IOException {
        String json = bufferedReader.lines().collect(Collectors.joining());
        Map<String, String> map = gson.fromJson(json, new TypeToken<Object2ObjectOpenHashMap<String, String>>() {
        }.getType());
        bufferedReader.close();
        translations.putAll(map);
    }

    public Locale parseTag(String tag) {
        for (Locale locale : Locale.getAvailableLocales()) {
            if (locale.toString().equalsIgnoreCase(tag)) {
                return locale;
            }
        }
        return null;
    }

    public List<LocalizationContainer> load() {
        List<LocalizationContainer> localizationContainers;
        if (this.file.exists()) {
            try (FileReader reader = new FileReader(this.file)) {
                localizationContainers = this.gson.fromJson(reader, new TypeToken<ObjectArrayList<LocalizationContainer>>() {
                }.getType());
            } catch (IOException e) {
                throw new RuntimeException("Could not parse locales", e);
            }
        } else {
            localizationContainers = new ObjectArrayList<>();
            this.writeChanges();
        }
        return localizationContainers;
    }

    public void writeChanges() {
        try (FileWriter writer = new FileWriter(this.file)) {
            this.gson.toJson(this.localizationContainers, writer);
        } catch (IOException e) {
            throw new RuntimeException("Could not save locale file", e);
        }
    }

    public String getJarLocaleDirectory() {
        return jarLocaleDirectory;
    }

    public List<LocalizationContainer> getLocalizableContainers() {
        return localizationContainers;
    }

    public static class LocalizationContainer {
        private final String identifier;
        private final String version;
        private final Map<String, Map<String, String>> locales;

        public LocalizationContainer(String identifier, String version, Map<String, Map<String, String>> locales) {
            this.identifier = identifier;
            this.version = version;
            this.locales = locales;
        }

        public String getIdentifier() {
            return identifier;
        }

        public String getVersion() {
            return version;
        }

        public Map<String, Map<String, String>> getLocales() {
            return locales;
        }
    }
}
