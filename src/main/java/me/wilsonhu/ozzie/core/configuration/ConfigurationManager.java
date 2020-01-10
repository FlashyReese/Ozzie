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
import java.util.Objects;

public class ConfigurationManager {

    private static final Logger log = LogManager.getLogger(ConfigurationManager.class);
    private static final String LOCALE_FOLDER = "locale";
    private static final String SETTINGS_FOLDER = "settings";
    private static final String SERVERS_SETTINGS_FOLDER = SETTINGS_FOLDER + File.separator + "servers";
    private static final String USERS_PERMISSIONS_SERVER_SETTINGS = SERVERS_SETTINGS_FOLDER + File.separator + "permissions";
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
        return readJson(USERS_SETTINGS_FOLDER, String.valueOf(id), new TypeToken<UserSchema>(){}.getType());
    }

    public void updateUserSettings(long id, UserSchema schema){
        writeJson(USERS_SETTINGS_FOLDER, String.valueOf(id), schema);
    }

    public void writeJson(String path, String filename, Object object) {
        try {
            Gson gson = new Gson();
            String json = gson.toJson(object);
            if(!new File(path).exists())new File(path).mkdirs();
            FileWriter fw = new FileWriter(path + File.separator + filename + ".json");
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
