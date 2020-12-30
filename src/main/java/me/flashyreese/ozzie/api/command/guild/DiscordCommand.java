package me.flashyreese.ozzie.api.command.guild;

import me.flashyreese.common.permission.PermissionException;
import me.flashyreese.ozzie.api.OzzieApi;
import me.flashyreese.ozzie.api.command.Command;

import java.util.Map;

public abstract class DiscordCommand implements Command<DiscordCommandSource> {

    private final String category;
    private final String description;
    private final String permission;

    public DiscordCommand(String category, String description, String permission) {
        this.category = category;
        this.description = description;
        this.permission = permission;
    }

    protected boolean hasPermission(DiscordCommandSource commandContext) {
        Map<String, Boolean> permissions = commandContext.permissions();
        try {
            return OzzieApi.INSTANCE.getPermissionDispatcher().hasPermission(this.permission, permissions);
        } catch (PermissionException e) {
            e.printStackTrace();
        }
        return false;
    }

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
