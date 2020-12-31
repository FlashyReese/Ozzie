package me.flashyreese.ozzie.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.flashyreese.ozzie.api.OzzieApi;
import me.flashyreese.ozzie.api.command.argument.RoleArgumentType;
import me.flashyreese.ozzie.api.command.argument.UserArgumentType;
import me.flashyreese.ozzie.api.command.guild.DiscordCommand;
import me.flashyreese.ozzie.api.command.guild.DiscordCommandManager;
import me.flashyreese.ozzie.api.command.guild.DiscordCommandSource;
import me.flashyreese.ozzie.api.database.mongodb.schema.RoleSchema;
import me.flashyreese.ozzie.api.database.mongodb.schema.UserSchema;
import me.flashyreese.ozzie.api.l10n.ParsableText;
import me.flashyreese.ozzie.api.l10n.TranslatableText;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.HashMap;
import java.util.Map;

public class PermissionCommand extends DiscordCommand {
    public PermissionCommand() {
        super("", "ozzie.permission.description", "ozzie.permission");
    }

    @Override
    public LiteralArgumentBuilder<DiscordCommandSource> getArgumentBuilder() {
        return DiscordCommandManager.literal("permission")
                .requires(this::hasPermission)
                .then(DiscordCommandManager.argument("role", RoleArgumentType.role())
                        .then(DiscordCommandManager.literal("clear")
                                .then(DiscordCommandManager.argument("permission", StringArgumentType.string())
                                        .executes(context -> {
                                            //
                                            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                                        }))
                                .then(DiscordCommandManager.literal("all")
                                        .executes(context -> {
                                            //use embed to confirm
                                            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                                        })))
                        .then(DiscordCommandManager.literal("set")
                                .then(DiscordCommandManager.argument("permission", StringArgumentType.string())
                                        .then(DiscordCommandManager.argument("state", BoolArgumentType.bool())
                                                .executes(this::setRolePermissionState)))))
                .then(DiscordCommandManager.argument("user", UserArgumentType.user())
                        .then(DiscordCommandManager.literal("clear")
                                .then(DiscordCommandManager.argument("permission", StringArgumentType.string())
                                        .executes(context -> {
                                            //
                                            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                                        }))
                                .then(DiscordCommandManager.literal("all")
                                        .executes(context -> {
                                            //use embed to confirm
                                            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                                        })))
                        .then(DiscordCommandManager.literal("set")
                                .then(DiscordCommandManager.argument("permission", StringArgumentType.string())
                                        .then(DiscordCommandManager.argument("state", BoolArgumentType.bool())
                                                .executes(this::setUserPermissionState)))));
    }

    private int setRolePermissionState(CommandContext<DiscordCommandSource> commandContext) throws CommandSyntaxException {
        MessageReceivedEvent event = commandContext.getSource().getEvent();
        Role role = RoleArgumentType.getRole(commandContext, "role");
        String permission =
                StringArgumentType.getString(commandContext, "permission");
        //todo: validate permission string, argument type word doesn't read asterisk
        boolean state = BoolArgumentType.getBool(commandContext, "state");
        try {
            RoleSchema roleSchema = OzzieApi.INSTANCE.getDatabaseHandler()
                    .retrieveRole(role.getIdLong());
            if (roleSchema.permissions().containsKey(permission)) {
                if (roleSchema.permissions().get(permission) == state) {
                    event.getChannel()
                            .sendMessage(new ParsableText(new TranslatableText("ozzie.permission.role.already_set_to", commandContext), permission, String.valueOf(state), role
                                    .getName()))
                            .queue();
                } else {
                    roleSchema.permissions().put(permission, state);
                    event.getChannel()
                            .sendMessage(new ParsableText(new TranslatableText("ozzie.permission.role.update_to", commandContext), permission, String.valueOf(state), role
                                    .getName()))
                            .queue();
                }
            } else {
                event.getChannel()
                        .sendMessage(new ParsableText(new TranslatableText("ozzie.permission.role.set_to", commandContext), permission, String.valueOf(state), role
                                .getName()))
                        .queue();
                roleSchema.permissions().put(permission, state);
            }
            OzzieApi.INSTANCE.getDatabaseHandler().updateRole(roleSchema);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return 0;
        }
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private int setUserPermissionState(CommandContext<DiscordCommandSource> commandContext) throws CommandSyntaxException {
        MessageReceivedEvent event = commandContext.getSource().getEvent();
        User user = UserArgumentType.getUser(commandContext, "user");
        String permission =
                StringArgumentType.getString(commandContext, "permission");
        //todo: validate permission string
        boolean state = BoolArgumentType.getBool(commandContext, "state");
        try {
            UserSchema userSchema = OzzieApi.INSTANCE.getDatabaseHandler()
                    .retrieveUser(user.getIdLong());
            if (!userSchema.getServerPermissionMap()
                    .containsKey(event.getGuild().getId())) {
                userSchema.getServerPermissionMap()
                        .put(event.getGuild().getId(), new HashMap<>());
            }

            Map<String, Boolean> permissionMap =
                    userSchema.getServerPermissionMap()
                            .get(event.getGuild().getId());

            if (permissionMap.containsKey(permission)) {
                if (permissionMap.get(permission) == state) {
                    event.getChannel()
                            .sendMessage(new ParsableText(new TranslatableText("ozzie.permission.user.already_set_to", commandContext), permission, String.valueOf(state), user
                                    .getName()))
                            .queue();
                } else {
                    permissionMap.put(permission, state);
                    event.getChannel()
                            .sendMessage(new ParsableText(new TranslatableText("ozzie.permission.user.update_to", commandContext), permission, String.valueOf(state), user
                                    .getName()))
                            .queue();
                }
            } else {
                event.getChannel()
                        .sendMessage(new ParsableText(new TranslatableText("ozzie.permission.user.set_to", commandContext), permission, String.valueOf(state), user
                                .getName()))
                        .queue();
                permissionMap.put(permission, state);
            }
            OzzieApi.INSTANCE.getDatabaseHandler().updateUser(userSchema);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return 0;
        }
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }
}
