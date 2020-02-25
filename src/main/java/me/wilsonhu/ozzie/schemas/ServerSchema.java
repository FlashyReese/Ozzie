package me.wilsonhu.ozzie.schemas;

import me.wilsonhu.ozzie.utilities.Helper;

public class ServerSchema {
    private long ownerID;
    private String serverLocale;
    private String customCommandPrefix;
    private long[] allowedCommandTextChannel;
    private boolean allowUserLocale;
    private boolean allowUserCustomCommandPrefix;

    public long getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(long ownerID) {
        this.ownerID = ownerID;
    }

    public String getServerLocale() {
        return serverLocale;
    }

    public void setServerLocale(String serverLocale) {
        this.serverLocale = serverLocale;
    }

    public String getCustomCommandPrefix() {
        return customCommandPrefix;
    }

    public void setCustomCommandPrefix(String customCommandPrefix) {
        this.customCommandPrefix = customCommandPrefix;
    }

    public long[] getAllowedCommandTextChannel() {//Not going to lie you are fucking cancer to deal with
        return allowedCommandTextChannel;
    }

    public boolean isAllowedCommandTextChannel(long textChannelId){
        for(long id: getAllowedCommandTextChannel()){
            if(id == textChannelId)return true;
        }
        return false;
    }

    public void addCommandTextChannel(long textChannelId){
        setAllowedCommandTextChannel(Helper.appendArray(getAllowedCommandTextChannel(), textChannelId));
    }

    public void removeCommandTextChannel(long textChannelId){
        setAllowedCommandTextChannel(Helper.unappendArray(getAllowedCommandTextChannel(), textChannelId));
    }

    public void setAllowedCommandTextChannel(long[] allowedCommandTextChannel) {
        this.allowedCommandTextChannel = allowedCommandTextChannel;
    }

    public boolean isAllowUserLocale() {
        return allowUserLocale;
    }

    public void setAllowUserLocale(boolean allowUserLocale) {
        this.allowUserLocale = allowUserLocale;
    }

    public boolean isAllowUserCustomCommandPrefix() {
        return allowUserCustomCommandPrefix;
    }

    public void setAllowUserCustomCommandPrefix(boolean allowUserCustomCommandPrefix) {
        this.allowUserCustomCommandPrefix = allowUserCustomCommandPrefix;
    }
}
