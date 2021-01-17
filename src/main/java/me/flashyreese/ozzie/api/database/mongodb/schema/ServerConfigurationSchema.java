/*
 * Copyright © 2021 FlashyReese <reeszrbteam@gmail.com>
 *
 * This file is part of Ozzie.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.flashyreese.ozzie.api.database.mongodb.schema;

import me.flashyreese.ozzie.api.OzzieApi;

import java.util.ArrayList;
import java.util.List;

/**
 * Serialization/Deserialization template for Server Configuration
 *
 * @author FlashyReese
 * @version 0.9.0+build-20210105
 * @since 0.9.0+build-20210105
 */
public class ServerConfigurationSchema {
    private long serverIdentifier;
    private List<Long> owners = new ArrayList<>();
    private String commandPrefix = "";
    private String locale = "";
    private boolean allowUserLocale;
    private boolean allowUserCommandPrefix;
    private List<Long> allowedCommandTextChannel = new ArrayList<>();

    public ServerConfigurationSchema() {

    }

    public ServerConfigurationSchema(long serverIdentifier) {
        this.serverIdentifier = serverIdentifier;
        this.owners.add(OzzieApi.INSTANCE.getShardManager().getGuildById(serverIdentifier).getOwnerIdLong());
        this.allowUserLocale = true;
        this.allowUserCommandPrefix = true;
        this.allowedCommandTextChannel.add(OzzieApi.INSTANCE.getShardManager().getGuildById(serverIdentifier).getDefaultChannel().getIdLong());
    }

    public long getServerIdentifier() {
        return serverIdentifier;
    }

    public void setServerIdentifier(long serverIdentifier) {
        this.serverIdentifier = serverIdentifier;
    }

    public List<Long> getOwners() {
        return owners;
    }

    public void setOwners(List<Long> owners) {
        this.owners = owners;
    }

    public String getCommandPrefix() {
        return commandPrefix;
    }

    public void setCommandPrefix(String commandPrefix) {
        this.commandPrefix = commandPrefix;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public boolean isAllowUserLocale() {
        return allowUserLocale;
    }

    public void setAllowUserLocale(boolean allowUserLocale) {
        this.allowUserLocale = allowUserLocale;
    }

    public boolean isAllowUserCommandPrefix() {
        return allowUserCommandPrefix;
    }

    public void setAllowUserCommandPrefix(boolean allowUserCommandPrefix) {
        this.allowUserCommandPrefix = allowUserCommandPrefix;
    }

    public List<Long> getAllowedCommandTextChannel() {
        return allowedCommandTextChannel;
    }

    public void setAllowedCommandTextChannel(List<Long> allowedCommandTextChannel) {
        this.allowedCommandTextChannel = allowedCommandTextChannel;
    }
}
