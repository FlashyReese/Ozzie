/*
 * Copyright © 2021 FlashyReese <reeszrbteam@gmail.com>
 *
 * This file is part of Ozzie.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.flashyreese.ozzie.api.command.guild;

import me.flashyreese.common.permission.Permissible;
import me.flashyreese.ozzie.api.database.mongodb.schema.ServerConfigurationSchema;
import me.flashyreese.ozzie.api.database.mongodb.schema.UserSchema;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

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
     * @param userSchema User
     * @param serverConfigurationSchema ServerConfiguration
     * @param permissions Map of permissions and it's states
     * @param event MessageReceivedEvent
     */
    public DiscordCommandSource(UserSchema userSchema, ServerConfigurationSchema serverConfigurationSchema, Map<String, Boolean> permissions, MessageReceivedEvent event) {
        this.userSchema = userSchema;
        this.serverConfigurationSchema = serverConfigurationSchema;
        this.permissions = permissions;
        this.event = event;
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
