package me.flashyreese.ozzie.api.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.flashyreese.common.permission.PermissionException;
import me.flashyreese.ozzie.api.OzzieApi;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Map;

public abstract class Command {

    private final String category;
    private final String description;
    private final String important;
    private final String permission;

    public Command(String category, String description, String important, String permission) {
        this.category = category;
        this.description = description;
        this.important = important;
        this.permission = permission;
    }

    public abstract LiteralArgumentBuilder<MessageReceivedEvent> getArgumentBuilder();

    protected boolean hasPermission(MessageReceivedEvent event) {
        Map<String, Boolean> permissions = CommandManager.permissionMap(event);
        try {
            return OzzieApi.INSTANCE.getPermissionDispatcher().hasPermission(permission, permissions);
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

    public String getImportant() {
        return important;
    }

    public String getPermission() {
        return permission;
    }
}
