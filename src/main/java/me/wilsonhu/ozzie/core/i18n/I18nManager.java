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
import java.nio.charset.StandardCharsets;
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
    private Ozzie ozzie;
    public I18nManager(Ozzie ozzie) {
        log.info("Building I18nManager...");
        setOzzie(ozzie);
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
                    HashMap<String, String> locale = new Gson().fromJson(new BufferedReader(new InputStreamReader(jar.getInputStream(entry), StandardCharsets.UTF_8)), new TypeToken<HashMap<String, String>>(){}.getType());
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
                        HashMap<String, String> locale = new Gson().fromJson(new BufferedReader(new InputStreamReader(new FileInputStream(app), StandardCharsets.UTF_8)), new TypeToken<HashMap<String, String>>(){}.getType());
                        getOzzie().getConfigurationManager().writeJson(ConfigurationManager.LOCALE_FOLDER + File.separator + lang, "ozzie", locale);
                    }
                } catch (URISyntaxException ex) {
                    ex.printStackTrace();
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

    public String translate(String key, String lang) {//Todo: Implement if key not found in HashMap that is not empty revert to default v: using a toggle switch cause this is already implemented
        //Todo: Dumb idea, calling keys within keys new a special notation for that v: like parsabletext?
        // ex. ozzie.salute = "Hello {ozzie.whatever#en_US}" ozzie.whatever = "balls" in en_US o "bolas" in es_MX result as {ozzie.whatever#en_US} = "Hello balls"  {ozzie.whatever#es_MX} = "Hello bolas"
        key = key.trim();
        if(key.isEmpty()){
            return "";
        }else if(key.startsWith(".") || key.endsWith(".") || !key.contains(".")){
            return key;
        }
        String[] splitted = key.split("\\.");
        String pluginId = splitted[0];
        String langKey = splitted[1];
        if(!getOzzie().getConfigurationManager().doesDirectoryExist(ConfigurationManager.LOCALE_FOLDER + File.separator + lang)){
            lang = Locale.getDefault().toString();
        }
        HashMap<String, String> locale = getOzzie().getConfigurationManager().readJson(ConfigurationManager.LOCALE_FOLDER + File.separator + lang, pluginId, new TypeToken<HashMap<String, String>>(){}.getType());
        if(locale.containsKey(langKey)){
            return locale.get(langKey);
        }
        return key;
    }

    public String translate(String key, TranslatableText.TranslationType type, long id){
        if(type == TranslatableText.TranslationType.SERVER){
            ServerSchema schema = getOzzie().getConfigurationManager().getServerSettings(id);
            return translate(key, schema.getServerLocale());
        }else if(type == TranslatableText.TranslationType.USER){
            UserSchema schema = getOzzie().getConfigurationManager().getUserSettings(id);
            return translate(key, schema.getUserLocale());
        }
        return key;
    }

    public String translate(String key, MessageReceivedEvent event){
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

    private Ozzie getOzzie(){
        return ozzie;
    }

    private void setOzzie(Ozzie ozzie){
        this.ozzie = ozzie;
    }
}