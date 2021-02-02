/*
 * Copyright © 2021 FlashyReese <reeszrbteam@gmail.com>
 *
 * This file is part of Ozzie.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.flashyreese.ozzie.api.command.guild;

import me.flashyreese.ozzie.api.permission.Permissible;
import me.flashyreese.ozzie.api.OzzieApi;
import me.flashyreese.ozzie.api.database.mongodb.schema.RoleSchema;
import me.flashyreese.ozzie.api.database.mongodb.schema.ServerConfigurationSchema;
import me.flashyreese.ozzie.api.database.mongodb.schema.UserSchema;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents Discord Command Source.
 *
 * @author FlashyReese
 * @version 0.9.0+build-20210105
 * @since 0.9.0+build-20210105
 */
public class DiscordCommandSource implements Permissible {

    private final UserSchema userSchema;
    private final ServerConfigurationSchema serverConfigurationSchema;
    private final Map<String, Boolean> permissions;
    private final MessageReceivedEvent event;

    /**
     * Creates a DiscordCommandSource for DiscordCommandManager.
     *
     * @param userSchema                User
     * @param serverConfigurationSchema ServerConfiguration
     * @param permissions               Map of permissions and it's states
     * @param event                     MessageReceivedEvent
     */
    public DiscordCommandSource(UserSchema userSchema, ServerConfigurationSchema serverConfigurationSchema, MessageReceivedEvent event) {
        this.userSchema = userSchema;
        this.serverConfigurationSchema = serverConfigurationSchema;
        this.permissions = this.createPermissionMap(userSchema, event);
        this.event = event;
    }

    /**
     * Creates a permission map by fetching all role/server-user permissions. Server-User permissions will have first priority then Server-Role permissions will have second priority.
     *
     * @param event MessageReceivedEvent
     * @return Map of permission and it's state
     */
    private Map<String, Boolean> createPermissionMap(UserSchema userSchema, MessageReceivedEvent event) {
        Map<String, Boolean> permissions = new HashMap<>();
        try {
            // Puts all permission-state from user's permission of specified server
            if (userSchema.getServerPermissionMap().containsKey(event.getGuild().getId())) {
                permissions.putAll(userSchema.getServerPermissionMap().get(event.getGuild().getId()));
            }

            // Puts all additional permission-state from user's roles without overwriting existing permission-state from user's permissions
            Member member = event.getMember();
            if (member != null) {
                for (Role role : member.getRoles()) {
                    RoleSchema roleSchema = OzzieApi.INSTANCE.getDatabaseHandler().retrieveRole(role.getIdLong());
                    roleSchema.permissions().forEach((key, aBoolean) -> {
                        if (!permissions.containsKey(key)) permissions.put(key, aBoolean);
                    });
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return permissions;
        }
        return permissions;
    }

    public UserSchema getUserSchema() {
        return userSchema;
    }

    public ServerConfigurationSchema getServerConfigurationSchema() {
        return serverConfigurationSchema;
    }

    public MessageReceivedEvent getEvent() {
        return event;
    }

    @Override
    public Map<String, Boolean> permissions() {
        return permissions;
    }
}
