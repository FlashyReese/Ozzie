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
package me.wilsonhu.ozzie.schemas;

import me.wilsonhu.ozzie.Ozzie;

import java.util.Random;

public class UserSchema {

    private long userId;
    private String userLocale;
    private String customCommandPrefix;
    private String password;

    public UserSchema(long userId, Ozzie ozzie){
        setUserId(userId);
        setUserLocale("default");
        setCustomCommandPrefix(ozzie.getDefaultCommandPrefix());
        String alphabet = "abcdefghijklnmopqrstuvwxyzABCDEFGHIJKLNMOPQRSTUVWXYZ0123456789";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            password.append(alphabet.charAt(new Random().nextInt(alphabet.length())));
        }
        setPassword(password.toString());
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

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
