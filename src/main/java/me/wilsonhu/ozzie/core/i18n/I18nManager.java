package me.wilsonhu.ozzie.core.i18n;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.wilsonhu.ozzie.Application;
import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.core.configuration.ConfigurationManager;
import me.wilsonhu.ozzie.schemas.ServerSchema;
import me.wilsonhu.ozzie.schemas.UserSchema;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class I18nManager {

    private static final Logger log = LogManager.getLogger(I18nManager.class);
    private HashMap<String, String> pluginLocalizationControl = new HashMap<String, String>();
    private static Ozzie ozzie;
    public I18nManager(Ozzie ozzie) {
        log.info("Building I18nManager...");
        this.ozzie = ozzie;
        loadSavedSettings();
        try {
            loadOzzieLocale();
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("I18nManager built!");
    }

    private void loadOzzieLocale() throws IOException, URISyntaxException {
        final String path = "locale";
        final File jarFile = new File(Application.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        if(jarFile.getAbsolutePath().endsWith(".jar")) {
            final JarFile jar = new JarFile(jarFile);
            final Enumeration<JarEntry> entries = jar.entries();
            while(entries.hasMoreElements()) {
                final JarEntry entry = entries.nextElement();
                final String name = entry.getName();
                if (name.startsWith(path + "/") && name.endsWith(".json")) {
                    HashMap<String, String> locale = new Gson().fromJson(new BufferedReader(new InputStreamReader(jar.getInputStream(entry), "UTF-8")), new TypeToken<HashMap<String, String>>(){}.getType());
                    String localeName = name.replaceAll("locale/", "").replaceAll(".json", "");
                    getOzzie().getConfigurationManager().writeJson(ConfigurationManager.LOCALE_FOLDER + File.separator + localeName, "ozzie", locale);
                }
            }
            jar.close();
        } else {
            final URL url = Application.class.getResource("/" + path);
            if (url != null) {
                try {
                    final File apps = new File(url.toURI());
                    for (File app : Objects.requireNonNull(apps.listFiles())) {
                        String lang = app.getName().replaceAll(".json", "");
                        HashMap<String, String> locale = new Gson().fromJson(new BufferedReader(new InputStreamReader(new FileInputStream(app), "UTF-8")), new TypeToken<HashMap<String, String>>(){}.getType());
                        getOzzie().getConfigurationManager().writeJson(ConfigurationManager.LOCALE_FOLDER + File.separator + lang, "ozzie", locale);
                    }
                } catch (URISyntaxException ex) {

                }
            }
        }
    }

    private void loadSavedSettings(){
        if(!new File(ConfigurationManager.LOCALE_FOLDER + File.separator + "settings.json").exists()){
            return;
        }
        this.setPluginLocalizationControl(getOzzie().getConfigurationManager().readJson(ConfigurationManager.LOCALE_FOLDER , "settings", new TypeToken<HashMap<String, String>>(){}.getType()));
    }

    public void updateSavedSettings(){
        getOzzie().getConfigurationManager().writeJson(ConfigurationManager.LOCALE_FOLDER, "settings", getPluginLocalizationControl());
    }

    public Locale[] getAvailableLocales(){
        Locale[] locales = SimpleDateFormat.getAvailableLocales();
        Locale[] availableLocales = new Locale[]{};
        File dir = new File(ConfigurationManager.LOCALE_FOLDER);
        for (File file: Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()){
                for (Locale locale : locales) {
                    if(locale.toString().isEmpty())continue;
                    if(locale.toString().equals(file.getName())){
                        availableLocales = addItem(availableLocales, locale);
                    }
                }
            }
        }
        return availableLocales;
    }

    public String getLocaleDisplayName(String tag){
        for (Locale locale : getAvailableLocales()) {
            if(locale.toString().equals(tag)){
                return locale.getDisplayName();
            }
        }
        return tag;
    }

    private Locale[] addItem(Locale[] arr, Locale x){
        Locale[] newarr = new Locale[arr.length + 1];
        for (int i = 0; i < arr.length; i++) {
            newarr[i] = arr[i];
        }
        newarr[arr.length] = x;
        return newarr;
    }

    public static String translate(String key, String lang) {//Todo: Implement if key not found in HashMap that is not empty revert to default v:
        key = key.trim();
        if(key.isEmpty()){
            return "";
        }else if(key.startsWith(".") || key.endsWith(".") || !key.contains(".")){
            return key;
        }
        String[] splitted = key.split("\\.");
        String pluginId = splitted[0];
        String langKey = splitted[1];
        HashMap<String, String> locale = getOzzie().getConfigurationManager().readJson(ConfigurationManager.LOCALE_FOLDER + File.separator + lang, pluginId, new TypeToken<HashMap<String, String>>(){}.getType());
        if(locale.containsKey(langKey)){
            return locale.get(langKey);
        }
        return key;
    }

    public static String translate(String key, TranslatableText.TranslationType type, long id){
        if(type == TranslatableText.TranslationType.SERVER){
            ServerSchema schema = getOzzie().getConfigurationManager().getServerSettings(id);
            return translate(key, schema.getServerLocale());
        }else if(type == TranslatableText.TranslationType.USER){
            UserSchema schema = getOzzie().getConfigurationManager().getUserSettings(id);
            return translate(key, schema.getUserLocale());
        }
        return key;
    }

    public static String translate(String key, MessageReceivedEvent event){
        String lang = Locale.getDefault().toString();
        long server = event.getGuild().getIdLong();
        long user = event.getAuthor().getIdLong();
        ServerSchema serverSchema = getOzzie().getConfigurationManager().getServerSettings(server);
        UserSchema userSchema = getOzzie().getConfigurationManager().getUserSettings(user);
        if(serverSchema.isAllowUserLocale()){
            if(userSchema.getUserLocale().equals("default")){
                if(serverSchema.getServerLocale().equals("default")){
                    return translate(key, lang);
                }else{
                    return translate(key, serverSchema.getServerLocale());
                }
            }else{
                return translate(key, userSchema.getUserLocale());
            }
        }else{
            if(serverSchema.getServerLocale().equals("default")){
                return translate(key, lang);
            }else{
                return translate(key, serverSchema.getServerLocale());
            }
        }
    }

    public HashMap<String, String> getPluginLocalizationControl() {
        return pluginLocalizationControl;
    }

    public void setPluginLocalizationControl(HashMap<String, String> pluginLocalizationControl) {
        this.pluginLocalizationControl = pluginLocalizationControl;
    }

    public static Ozzie getOzzie(){
        return ozzie;
    }
}