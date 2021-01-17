/*
 * Copyright © 2021 FlashyReese <reeszrbteam@gmail.com>
 *
 * This file is part of Ozzie.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.flashyreese.ozzie.api.command.guild;

import me.flashyreese.common.permission.PermissionException;
import me.flashyreese.ozzie.api.OzzieApi;
import me.flashyreese.ozzie.api.command.Command;

import java.util.Map;

/**
 * Represents an abstract Discord Command.
 *
 * @author FlashyReese
 * @version 0.9.0+build-20210105
 * @since 0.9.0+build-20210105
 */
public abstract class DiscordCommand implements Command<DiscordCommandSource> {

    private final String category;
    private final String description;
    private final String permission;

    /**
     * Default constructor for Discord Command.
     *
     * @param category Command Category
     * @param description Command Description
     * @param permission Command Permission
     */
    public DiscordCommand(String category, String description, String permission) {
        this.category = category;
        this.description = description;
        this.permission = permission;
    }

    /**
     * Checks if user has sufficient permission for this command permission.
     *
     * @param commandContext Command Context
     * @return Sufficient permissions
     */
    protected boolean hasPermission(DiscordCommandSource commandContext) {
        Map<String, Boolean> permissions = commandContext.permissions();
        try {
            return OzzieApi.INSTANCE.getPermissionDispatcher().hasPermission(this.permission, permissions);
        } catch (PermissionException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Checks if user has sufficient permission for a specific permission.
     *
     * @param commandContext Command Context
     * @return Sufficient permissions
     */
    protected boolean hasPermissionOf(DiscordCommandSource commandContext, String subPermission) {
        Map<String, Boolean> permissions = commandContext.permissions();
        try {
            return OzzieApi.INSTANCE.getPermissionDispatcher().hasPermission(String.format("%s.%s", this.permission, subPermission), permissions);
        } catch (PermissionException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isHidden() {
        return false;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public String getPermission() {
        return permission;
    }
}
