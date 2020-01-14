package me.wilsonhu.ozzie.core.configuration;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.schemas.ServerSchema;
import me.wilsonhu.ozzie.schemas.UserSchema;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class ConfigurationManager {

    private static final Logger log = LogManager.getLogger(ConfigurationManager.class);
    public static final String LOCALE_FOLDER = "locale";
    private static final String SETTINGS_FOLDER = "settings";
    private static final String SERVERS_SETTINGS_FOLDER = SETTINGS_FOLDER + File.separator + "servers";
    private static final String USERS_PERMISSIONS_SERVER_SETTINGS = "permissions";
    private static final String USERS_SETTINGS_FOLDER = SETTINGS_FOLDER + File.separator + "users";
    private static final String PLUGINS_SETTINGS_FOLDER = SETTINGS_FOLDER + File.separator + "plugins";
    private static final String SERVER_SETTINGS_FILE_NAME = "settings";

    private Ozzie ozzie;

    public ConfigurationManager(Ozzie ozzie){
        log.info("Building Configuration Manager...");
        this.ozzie = ozzie;
        log.info("Configuration Manager built!");
    }

    public ServerSchema getServerSettings(long id){
        if(!doesFileExist(SERVERS_SETTINGS_FOLDER + File.separator + id, SERVER_SETTINGS_FILE_NAME)){
            ServerSchema schema = new ServerSchema();
            long[] channel = new long[]{ozzie.getShardManager().getGuildById(id).getDefaultChannel().getIdLong()};
            long owner = ozzie.getShardManager().getGuildById(id).getOwnerIdLong();
            schema.setOwnerID(owner);
            schema.setAllowedCommandTextChannel(channel);
            schema.setCustomCommandPrefix(ozzie.getDefaultCommandPrefix());
            schema.setAllowUserCustomCommandPrefix(true);
            schema.setServerLocale("default");
            schema.setAllowUserLocale(true);
            updateServerSettings(id, schema);
        }
        ServerSchema schema = readJson(SERVERS_SETTINGS_FOLDER + File.separator + id, SERVER_SETTINGS_FILE_NAME, new TypeToken<ServerSchema>(){}.getType());
        if(Objects.requireNonNull(getOzzie().getShardManager().getGuildById(id)).getOwnerIdLong() != schema.getOwnerID()){
            schema.setOwnerID(Objects.requireNonNull(getOzzie().getShardManager().getGuildById(id)).getOwnerIdLong());
            log.info(String.format("Updated owner id from server(%s)", id));
        }
        return schema;
    }

    public void updateServerSettings(long id, ServerSchema schema){
        writeJson(SERVERS_SETTINGS_FOLDER + File.separator + id, SERVER_SETTINGS_FILE_NAME, schema);
    }

    public UserSchema getUserSettings(long id){
        if(!doesFileExist(USERS_SETTINGS_FOLDER, String.valueOf(id))){
            UserSchema schema = new UserSchema();
            schema.setUserLocale("default");
            schema.setCustomCommandPrefix("default");
            updateUserSettings(id, schema);
        }
        return readJson(USERS_SETTINGS_FOLDER, String.valueOf(id), new TypeToken<UserSchema>(){}.getType());
    }

    public void updateUserSettings(long id, UserSchema schema){
        writeJson(USERS_SETTINGS_FOLDER, String.valueOf(id), schema);
    }

    public ArrayList<String> getUserPermissions(long serverId, long userId){
        String[] permissions = readJson(SERVERS_SETTINGS_FOLDER + File.separator + serverId + File.separator + USERS_PERMISSIONS_SERVER_SETTINGS, String.valueOf(userId), new TypeToken<String[]>(){}.getType());
        return new ArrayList<String>(Arrays.asList(permissions));
    }

    public void updateUserPermissions(long serverId, long userId, String[] permissions){
        writeJson(SERVERS_SETTINGS_FOLDER + File.separator + serverId + File.separator + USERS_PERMISSIONS_SERVER_SETTINGS, String.valueOf(userId), permissions);
    }

    public boolean hasPermission(long serverID, long userID, String permission) {
        if(!doesFileExist(SERVERS_SETTINGS_FOLDER + File.separator + serverID + File.separator + USERS_PERMISSIONS_SERVER_SETTINGS, String.valueOf(userID))){
            updateUserPermissions(serverID, userID, new String[]{"ozzie.default"});
        }
        ArrayList<String> userPerms = getUserPermissions(serverID, userID);
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

    public <T> T getPluginSettings(String pluginId, Type schemaType){
        return readJson(PLUGINS_SETTINGS_FOLDER, pluginId, schemaType);
    }

    public void writePluginSettings(String pluginId, Object schema){
        writeJson(PLUGINS_SETTINGS_FOLDER, pluginId, schema);
    }

    public boolean doesFileExist(String path, String filename){
        File file = new File(path + File.separator + filename + ".json");
        return file.exists();
    }

    public void writeJson(String path, String filename, Object object) {
        try {
            Gson gson = new Gson();
            String json = gson.toJson(object);
            if(!new File(path).exists())new File(path).mkdirs();

            FileWriter fw = new FileWriter(new File(path + File.separator + filename + ".json"));
            fw.write(json);
            fw.flush();
            fw.close();
        } catch (Exception ex) {}
    }

    public <T> T readJson(String path, String filename, Type type) {
        Gson gson = new Gson();
        FileReader fileReader = null;
        BufferedReader buffered = null;
        try {
            fileReader = new FileReader(path + File.separator + filename + ".json");
            buffered = new BufferedReader(fileReader);
            Type t = type;
            return gson.fromJson(fileReader, t);
        } catch (Exception ex) {
            ex.printStackTrace();
        }finally {
            try {
                buffered.close();
                fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private Ozzie getOzzie(){
        return ozzie;
    }
}
