package me.wilsonhu.ozzie.core.configuration;

import com.google.gson.Gson;
import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.schemas.ServerSchema;
import me.wilsonhu.ozzie.schemas.ServerUserPermissionSchema;
import me.wilsonhu.ozzie.schemas.UserSchema;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

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
    private MongoDBHandler mongoDBHandler;

    public ConfigurationManager(Ozzie ozzie){
        log.info("Building Configuration Manager...");
        setOzzie(ozzie);
        setMongoDBHandler(new MongoDBHandler(ozzie));
        log.info("Configuration Manager built!");
    }

    public ServerSchema getServerSettings(long id){
        return getMongoDBHandler().retrieveServer(id);
    }

    public void updateServerSettings(ServerSchema schema){
        getMongoDBHandler().updateServer(schema);
    }

    public UserSchema getUserSettings(long id){
        return getMongoDBHandler().retrieveUser(id);
    }

    public void updateUserSettings(UserSchema schema){
        getMongoDBHandler().updateUser(schema);
    }

    public ArrayList<String> getUserPermissions(long serverId, long userId){
        return getMongoDBHandler().retrieveServerUserPermission(serverId, userId).getPermissions();
    }

    public void updateUserPermissions(long serverId, long userId, String[] permissions){
        ServerUserPermissionSchema serverUserPermissionSchema = new ServerUserPermissionSchema(serverId, userId);
        for(String perm: permissions){
            if(!serverUserPermissionSchema.getPermissions().contains(perm)){
                serverUserPermissionSchema.getPermissions().add(perm);
            }
        }
        getMongoDBHandler().updateServerUserPermission(serverUserPermissionSchema);
    }

    public boolean hasPermission(long serverID, long userID, String permission) {
        if(!doesFileExist(SERVERS_SETTINGS_FOLDER + File.separator + serverID + File.separator + USERS_PERMISSIONS_SERVER_SETTINGS, String.valueOf(userID))){
            updateUserPermissions(serverID, userID, new String[]{"*.default"});
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

    public MongoDBHandler getMongoDBHandler() {
        return mongoDBHandler;
    }

    public void setMongoDBHandler(MongoDBHandler mongoDBHandler) {
        this.mongoDBHandler = mongoDBHandler;
    }

}
