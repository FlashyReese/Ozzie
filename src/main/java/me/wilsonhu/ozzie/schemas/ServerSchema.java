package me.wilsonhu.ozzie.schemas;

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

    public long[] getAllowedCommandTextChannel() {
        return allowedCommandTextChannel;
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
