package me.flashyreese.ozzie.api.database.mongodb.schema;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;

public class UserSchema {
    private long userIdentifier;
    private String locale;
    private String commandPrefix;
    private Map<String, Map<String, Boolean>> serverPermissionMap = new Object2ObjectOpenHashMap<>();

    public UserSchema(){

    }

    public UserSchema(long userIdentifier) {
        this.userIdentifier = userIdentifier;
        this.locale = "";
        this.commandPrefix = "";
    }

    public long getUserIdentifier() {
        return userIdentifier;
    }

    public void setUserIdentifier(long userIdentifier) {
        this.userIdentifier = userIdentifier;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getCommandPrefix() {
        return commandPrefix;
    }

    public void setCommandPrefix(String commandPrefix) {
        this.commandPrefix = commandPrefix;
    }

    public Map<String, Map<String, Boolean>> getServerPermissionMap() {
        return serverPermissionMap;
    }

    public void setServerPermissionMap(Map<String, Map<String, Boolean>> serverPermissionMap) {
        this.serverPermissionMap = serverPermissionMap;
    }
}
