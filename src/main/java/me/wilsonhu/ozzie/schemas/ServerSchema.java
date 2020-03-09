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

import java.util.ArrayList;
import java.util.Objects;

public class ServerSchema {


    private long serverId;
    private long ownerID;
    private String serverLocale;
    private String customCommandPrefix;
    private ArrayList<Long> allowedCommandTextChannel;
    private boolean allowUserLocale;
    private boolean allowUserCustomCommandPrefix;

    public ServerSchema(long serverId, Ozzie ozzie){
        setServerId(serverId);
        long owner = Objects.requireNonNull(ozzie.getShardManager().getGuildById(serverId)).getOwnerIdLong();
        setOwnerID(owner);
        setAllowedCommandTextChannel(new ArrayList<Long>());
        getAllowedCommandTextChannel().add(Objects.requireNonNull(Objects.requireNonNull(ozzie.getShardManager().getGuildById(serverId)).getDefaultChannel()).getIdLong());
        setCustomCommandPrefix(ozzie.getDefaultCommandPrefix());
        setAllowUserCustomCommandPrefix(true);
        setServerLocale("default");
        setAllowUserLocale(true);
    }


    public long getServerId() {
        return serverId;
    }

    public void setServerId(long serverId) {
        this.serverId = serverId;
    }

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

    public ArrayList<Long> getAllowedCommandTextChannel() {//Not going to lie you are fucking cancer to deal with
        return allowedCommandTextChannel;
    }

    public boolean isAllowedCommandTextChannel(long textChannelId){
        for(long id: getAllowedCommandTextChannel()){
            if(id == textChannelId)return true;
        }
        return false;
    }

    public void addCommandTextChannel(long textChannelId){
        getAllowedCommandTextChannel().add(textChannelId);
    }

    public void removeCommandTextChannel(long textChannelId){
        getAllowedCommandTextChannel().remove(textChannelId);
    }

    public void setAllowedCommandTextChannel(ArrayList<Long> allowedCommandTextChannel) {
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
