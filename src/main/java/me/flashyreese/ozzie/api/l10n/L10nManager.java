package me.flashyreese.ozzie.api.l10n;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.vdurmont.semver4j.Semver;
import me.flashyreese.common.util.FileUtil;
import me.flashyreese.common.util.JarUtil;
import me.flashyreese.ozzie.api.plugin.Plugin;
import me.flashyreese.ozzie.api.plugin.PluginLoader;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * Represents the Localization Manager.
 *
 * @author FlashyReese
 * @version 0.9.0+build-20210105
 * @since 0.9.0+build-20210105
 */
public class L10nManager {
    private final Gson gson;
    private final File file;
    private final String jarLocaleDirectory;

    private final List<LocalizationContainer> localizationContainers = new ArrayList<>();

    public L10nManager(Gson gson, File directory, String jarLocaleDirectory) {
        this.gson = gson;
        this.file = new File(directory + File.separator + "locale.json");
        this.jarLocaleDirectory = jarLocaleDirectory;
        this.localizationContainers.addAll(this.loadLocalizationContainers());
    }

    /**
     * Retrieve domain and look for domain in localization containers.
     *
     * @param key  Localization key, Ex. "hello.world".
     * @param lang Language Code
     * @return Translated text or key if key isn't found.
     */
    public String translate(String key, String lang) {
        // Checks if key is not empty, does not starts/ends with period and contains period.
        key = key.trim();
        if (key.isEmpty() || key.startsWith(".") || key.endsWith(".") || !key.contains(".")) {
            return key;
        }

        // Get the domain of the key, Ex. "hello.world" -> "hello". Checks if localization containers contains domain.
        String domain = key.substring(0, key.indexOf('.'));
        Optional<LocalizationContainer> optionalLocalizationContainer = this.localizationContainers.stream().filter(container -> container.getIdentifier().equalsIgnoreCase(domain)).findFirst();
        if (optionalLocalizationContainer.isPresent()) {
            LocalizationContainer localizationContainer = optionalLocalizationContainer.get();

            // Check if localization container contains lang
            if (localizationContainer.getLocales().containsKey(lang)) {
                Map<String, String> langMap = localizationContainer.getLocales().get(lang);
                String name = key.substring(key.indexOf('.') + 1);

                // Return value if key exist or default to key
                return langMap.getOrDefault(name, key);
            } else {
                return key;
            }
        } else {
            return key;
        }
    }

    /**
     * Load localizable container from plugin entry container, compare plugin versions and replaces localization per plugin identifier accordingly.
     *
     * @param pluginEntryContainer Plugin Entry Container
     * @throws Exception IO
     */
    public void loadLocalizableContainerFromPluginEntryContainer(PluginLoader.PluginEntryContainer<Plugin> pluginEntryContainer) throws Exception {
        // Check if localization containers contains plugin entry container identifier
        Optional<LocalizationContainer> optionalLocalizableContainer = this.localizationContainers.stream().filter(container -> container.getIdentifier().equals(pluginEntryContainer.getPluginMetadata().asV1().getId())).findFirst();
        if (optionalLocalizableContainer.isPresent()) {
            LocalizationContainer localizationContainer = optionalLocalizableContainer.get();

            // Compare existing version to plugin entry container version
            Semver pluginContainerSemver = new Semver(localizationContainer.getVersion());
            Semver pluginEntryContainerSemver = new Semver(pluginEntryContainer.getPluginMetadata().asV1().getVersion());
            if (!pluginContainerSemver.isEqualTo(pluginEntryContainerSemver)) {
                // Search for list of locales available in plugin
                List<Locale> validLocales = this.searchValidLocales(pluginEntryContainer.getPluginFile());
                validLocales.forEach(locale -> {
                    try {
                        // Fetch translations map with locale
                        Map<String, String> translations = this.getTranslationsForLocale(pluginEntryContainer.getPluginFile(), locale);
                        localizationContainer.getLocales().put(locale.toString().toLowerCase(), translations);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        } else {
            // Create new localization container
            Map<String, Map<String, String>> locales = new HashMap<>();
            List<Locale> validLocales = this.searchValidLocales(pluginEntryContainer.getPluginFile());
            validLocales.forEach(locale -> {
                try {
                    Map<String, String> translations = this.getTranslationsForLocale(pluginEntryContainer.getPluginFile(), locale);
                    locales.put(locale.toString().toLowerCase(), translations);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            this.localizationContainers.add(new LocalizationContainer(pluginEntryContainer.getPluginMetadata().asV1().getId(), pluginEntryContainer.getPluginMetadata().asV1().getVersion(), locales));
        }
        this.writeChanges();
    }

    /**
     * Retrieves a list of valid locales from a file (in this case jar file).
     *
     * @param jarFile File
     * @return List of valid locales.
     * @throws Exception IO
     */
    private List<Locale> searchValidLocales(File jarFile) throws Exception {
        List<Locale> validLocales = new ArrayList<>();
        // Checks if jarFile is actually file
        if (jarFile.isFile()) {
            // Iterate through all entries in JarFile
            final JarFile jar = new JarFile(jarFile);
            final Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                final JarEntry entry = entries.nextElement();
                final String name = entry.getName();
                // Check if file is in localization directory and is a JSON file
                if (name.startsWith(this.jarLocaleDirectory + "/") && name.endsWith(".json")) {
                    // Validate file name as language code
                    Locale locale = this.parseTag(JarUtil.getFileName(entry));
                    if (locale != null) {
                        validLocales.add(locale);
                    }
                }
            }
            jar.close();
        } else {
            // If file is not a file, then get the resource using the System Class Loader
            final URL url = ClassLoader.getSystemClassLoader().getResource(this.jarLocaleDirectory);
            if (url != null) {
                // Iterate list of files in resource location
                final File languages = new File(url.toURI());
                for (File language : Objects.requireNonNull(languages.listFiles())) {
                    // Validate file name as language code
                    Locale locale = this.parseTag(FileUtil.getFileName(language));
                    if (locale != null) {
                        validLocales.add(locale);
                    }
                }
            }
        }
        return validLocales;
    }

    /**
     * Retrieves a map of a specific locale from a file.
     *
     * @param jarFile File
     * @param locale  Locale
     * @return Map of translated strings
     * @throws Exception IO Error
     */
    private Map<String, String> getTranslationsForLocale(File jarFile, Locale locale) throws Exception {
        Map<String, String> translations = new HashMap<>();
        // Checks if jarFile is actually file
        if (jarFile.isFile()) {
            // Iterate through all entries in JarFile
            final JarFile jar = new JarFile(jarFile);
            final Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                final JarEntry entry = entries.nextElement();
                final String name = entry.getName();
                // Check if file is in localization directory and is a JSON file
                if (name.startsWith(this.jarLocaleDirectory + "/") && name.endsWith(".json")) {
                    // Check if file name is a locale
                    if (JarUtil.getFileName(entry).equalsIgnoreCase(locale.toString())) {
                        translations.putAll(this.deserializeTranslations(jar.getInputStream(entry)));
                        break;
                    }
                }
            }
            jar.close();
        } else {
            // If file is not a file, then get the resource using the System Class Loader
            final URL url = ClassLoader.getSystemClassLoader().getResource(this.jarLocaleDirectory);
            if (url != null) {
                // Iterate list of files in resource location
                final File languages = new File(url.toURI());
                for (File language : Objects.requireNonNull(languages.listFiles())) {
                    // Check if file name is a locale
                    if (FileUtil.getFileName(language).equalsIgnoreCase(locale.toString())) {
                        translations.putAll(this.deserializeTranslations(new FileInputStream(language)));
                        break;
                    }
                }
            }
        }
        return translations;
    }

    /**
     * Deserializes translation JSON to locale map.
     *
     * @param in InputStream
     * @return Map of translations
     * @throws IOException         IO Error
     * @throws JsonSyntaxException Deserialization Error
     */
    private Map<String, String> deserializeTranslations(InputStream in) throws IOException, JsonSyntaxException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        String json = bufferedReader.lines().collect(Collectors.joining());
        Map<String, String> map = this.gson.fromJson(json, new TypeToken<HashMap<String, String>>() {
        }.getType());
        bufferedReader.close();
        return map;
    }

    public Locale parseTag(String tag) {
        for (Locale locale : Locale.getAvailableLocales()) {
            if (locale.toString().equalsIgnoreCase(tag)) {
                return locale;
            }
        }
        return null;
    }

    /**
     * Loads existing Localization Containers saved in file.
     *
     * @return List of Localization Container
     */
    public List<LocalizationContainer> loadLocalizationContainers() {
        List<LocalizationContainer> localizationContainers;
        if (this.file.exists()) {
            try (FileReader reader = new FileReader(this.file)) {
                localizationContainers = this.gson.fromJson(reader, new TypeToken<ArrayList<LocalizationContainer>>() {
                }.getType());
            } catch (IOException e) {
                throw new RuntimeException("Could not parse locales", e);
            }
        } else {
            localizationContainers = new ArrayList<>();
            this.writeChanges();
        }
        return localizationContainers;
    }

    /**
     * Write all Localization Containers changes to file.
     */
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
