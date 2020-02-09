package me.wilsonhu.ozzie.schemas;

public class UserSchema {

    private String userLocale;
    private String customCommandPrefix;
    private String password;

    public String getUserLocale() {
        return userLocale;
    }

    public void setUserLocale(String userLocale) {
        this.userLocale = userLocale;
    }

    public String getCustomCommandPrefix() {
        return customCommandPrefix;
    }

    public void setCustomCommandPrefix(String customCommandPrefix) {
        this.customCommandPrefix = customCommandPrefix;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
