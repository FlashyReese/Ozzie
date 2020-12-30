package me.flashyreese.ozzie.api.command.guild;

import me.flashyreese.common.permission.Permissible;
import me.flashyreese.ozzie.api.database.mongodb.schema.ServerConfigurationSchema;
import me.flashyreese.ozzie.api.database.mongodb.schema.UserSchema;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Map;

public class DiscordCommandSource implements Permissible {


    private final UserSchema userSchema;
    private final ServerConfigurationSchema serverConfigurationSchema;
    private final Map<String, Boolean> permissions;
    private final MessageReceivedEvent event;

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
