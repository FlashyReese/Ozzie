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
package me.wilsonhu.ozzie.core.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.wilsonhu.ozzie.Application;
import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.core.database.mongodb.MongoDBConnectionFactory;
import me.wilsonhu.ozzie.schemas.ServerSchema;
import me.wilsonhu.ozzie.schemas.ServerUserPermissionSchema;
import me.wilsonhu.ozzie.schemas.UserSchema;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ConfigurationManager {

    private static final Logger log = LogManager.getLogger(ConfigurationManager.class);
    public static final String LOCALE_FOLDER = "locale";
    private static final String SETTINGS_FOLDER = "settings";
    private static final String PLUGINS_SETTINGS_FOLDER = SETTINGS_FOLDER + File.separator + "plugins";

    private ClientConfiguration clientConfiguration;
    private Ozzie ozzie;
    private MongoDBConnectionFactory mongoDBConnectionFactory;

    public ConfigurationManager(Ozzie ozzie){
        log.info("Building Configuration Manager...");
        setOzzie(ozzie);
        log.info("Configuration Manager built!");
    }


    public void loadClientConfiguration() throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        File clientConfigurationFile = new File("OzzieClientConfiguration.yml");
        if(clientConfigurationFile.exists()){
            ClientConfiguration config = objectMapper.readValue(clientConfigurationFile, ClientConfiguration.class);
            this.setClientConfiguration(config);
        }else{
            final String path = "config";
            final File jarFile = new File(Application.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if(jarFile.getAbsolutePath().endsWith(".jar")) {
                final JarFile jar = new JarFile(jarFile);
                final Enumeration<JarEntry> entries = jar.entries();
                while(entries.hasMoreElements()) {
                    final JarEntry entry = entries.nextElement();
                    final String name = entry.getName();
                    if (name.startsWith(path + "/") && name.endsWith(".yml")) {
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
    }

    public ClientConfiguration getClientConfiguration(){
        return clientConfiguration;
    }

    public void setClientConfiguration(ClientConfiguration clientConfiguration){
        this.clientConfiguration = clientConfiguration;
    }

    public ServerSchema getServerSettings(long id){
        return getMongoDBConnectionFactory().retrieveServer(id);
    }

    public void updateServerSettings(ServerSchema schema){
        getMongoDBConnectionFactory().updateServer(schema);
    }

    public UserSchema getUserSettings(long id){
        return getMongoDBConnectionFactory().retrieveUser(id);
    }

    public void updateUserSettings(UserSchema schema){
        getMongoDBConnectionFactory().updateUser(schema);
    }

    public ArrayList<String> getUserPermissions(long serverId, long userId){
        return getMongoDBConnectionFactory().retrieveServerUserPermission(serverId, userId).getPermissions();
    }

    public void updateUserPermissions(long serverId, long userId, ArrayList<String> permissions){
        ServerUserPermissionSchema serverUserPermissionSchema = new ServerUserPermissionSchema(serverId, userId);
        serverUserPermissionSchema.setPermissions(permissions);
        getMongoDBConnectionFactory().updateServerUserPermission(serverUserPermissionSchema);
    }

    public boolean hasPermission(long serverID, long userID, String permission) {
        ArrayList<String> userPerms = getUserPermissions(serverID, userID);
        return hasPermission(permission, userPerms);
    }

    public boolean hasPermission(String permission, ServerUserPermissionSchema serverUserPermissionSchema) {
        ArrayList<String> userPerms = serverUserPermissionSchema.getPermissions();
        return hasPermission(permission, userPerms);
    }

    private boolean hasPermission(String permission, ArrayList<String> userPerms) {
        if(userPerms.contains(permission)) {
            return true;
        }
        if(userPerms.contains("*")) {
            return true;
        }
        if(userPerms.contains("*"+permission.substring(permission.lastIndexOf("."), permission.length()))) {
            return true;
        }
        if(userPerms.contains(permission.substring(0, permission.lastIndexOf(".") + 1) + "*")) {
            return true;
        }
        return false;
    }

    public boolean isOwner(long serverID, long userID){
        if(!hasPermission(serverID, userID, "*.developer"))return false;
        ServerSchema serverSchema = getServerSettings(serverID);
        return userID == serverSchema.getOwnerID();
    }

    public <T> T getPluginSettings(String pluginId, Type schemaType){
        return readJson(PLUGINS_SETTINGS_FOLDER, pluginId, schemaType);
    }

    public void writePluginSettings(String pluginId, Object schema){
        writeJson(PLUGINS_SETTINGS_FOLDER, pluginId, schema);
    }

    public boolean doesFileExist(String path, String filename){
        return new File(path + File.separator + filename + ".json").isFile();
    }

    public boolean doesDirectoryExist(String path){
        return new File(path).isDirectory();
    }

    public void writeJson(String path, String filename, Object object) {
        Writer fw = null;
        try {
            Gson gson = new Gson();
            String json = gson.toJson(object);
            if(!new File(path).exists())new File(path).mkdirs();
            fw = new OutputStreamWriter(new FileOutputStream(new File(path + File.separator + filename + ".json")), StandardCharsets.UTF_8);
            fw.write(json);
            fw.flush();
            fw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }finally {
            try {
                assert fw != null;
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public <T> T readJson(String path, String filename, Type type) {
        Gson gson = new Gson();
        BufferedReader buffered = null;
        try {
            buffered = new BufferedReader(new InputStreamReader(new FileInputStream(path + File.separator + filename + ".json"), StandardCharsets.UTF_8));
            return gson.fromJson(buffered, type);
        } catch (Exception ex) {
            ex.printStackTrace();
        }finally {
            try {
                assert buffered != null;
                buffered.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }



    private void setOzzie(Ozzie ozzie){
        this.ozzie = ozzie;
    }

    private Ozzie getOzzie(){
        return ozzie;
    }

    public MongoDBConnectionFactory getMongoDBConnectionFactory() {
        if (mongoDBConnectionFactory == null) setMongoDBConnectionFactory(new MongoDBConnectionFactory(getOzzie()));
        return mongoDBConnectionFactory;
    }

    public void setMongoDBConnectionFactory(MongoDBConnectionFactory mongoDBConnectionFactory) {
        this.mongoDBConnectionFactory = mongoDBConnectionFactory;
    }

}
