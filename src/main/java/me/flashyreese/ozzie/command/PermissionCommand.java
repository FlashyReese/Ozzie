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
import me.flashyreese.ozzie.api.command.guild.DiscordCommandSource;
import me.flashyreese.ozzie.api.database.mongodb.schema.RoleSchema;
import me.flashyreese.ozzie.api.database.mongodb.schema.UserSchema;
import me.flashyreese.ozzie.api.l10n.ParsableText;
import me.flashyreese.ozzie.api.l10n.TranslatableText;
import me.flashyreese.ozzie.api.util.ConfirmationMenu;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
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
        return this.literal("permission")
                .requires(this::hasPermission)
                .then(this.argument("role", RoleArgumentType.role())
                        .then(this.literal("clear")
                                .then(this.argument("permission", StringArgumentType.string())
                                        .executes(this::clearRolePermissionState))
                                .then(this.literal("all")
                                        .executes(this::clearRoleAllPermission)))
                        .then(this.literal("set")
                                .then(this.argument("permission", StringArgumentType.string())
                                        .then(this.argument("state", BoolArgumentType.bool())
                                                .executes(this::setRolePermissionState)))))
                .then(this.argument("user", UserArgumentType.user())
                        .then(this.literal("clear")
                                .then(this.argument("permission", StringArgumentType.string())
                                        .executes(this::clearUserPermissionState))
                                .then(this.literal("all")
                                        .executes(this::clearUserAllPermission)))
                        .then(this.literal("set")
                                .then(this.argument("permission", StringArgumentType.string())
                                        .then(this.argument("state", BoolArgumentType.bool())
                                                .executes(this::setUserPermissionState)))));
    }

    private int clearRoleAllPermission(CommandContext<DiscordCommandSource> commandContext) {
        try {
            Role role = RoleArgumentType.getRole(commandContext, "role");
            RoleSchema roleSchema = OzzieApi.INSTANCE.getDatabaseHandler().retrieveRole(role.getIdLong());
            MessageEmbed messageEmbed = new EmbedBuilder()
                    .setDescription(new ParsableText(new TranslatableText("ozzie.permission.role.clear_confirm", commandContext), role.getAsMention()))
                    .build();
            ConfirmationMenu confirmationMenu = new ConfirmationMenu.Builder()
                    .setEventWaiter(OzzieApi.INSTANCE.getEventWaiter())
                    .setCustomEmbed(messageEmbed)
                    .setConfirm(message -> {
                        roleSchema.getPermissions().clear();
                        try {
                            OzzieApi.INSTANCE.getDatabaseHandler().updateRole(roleSchema);
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }
                        commandContext.getSource().getEvent().getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.permission.role.clear_all", commandContext), role.getAsMention())).queue();
                    })
                    .build();
            confirmationMenu.display(commandContext.getSource().getEvent().getChannel());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private int clearUserAllPermission(CommandContext<DiscordCommandSource> commandContext) {
        try {
            User user = UserArgumentType.getUser(commandContext, "user");
            UserSchema userSchema = OzzieApi.INSTANCE.getDatabaseHandler().retrieveUser(user.getIdLong());

            if (userSchema.getServerPermissionMap().containsKey(commandContext.getSource().getEvent().getGuild().getId())) {
                Map<String, Boolean> userPermissionMap = userSchema.getServerPermissionMap().get(commandContext.getSource().getEvent().getGuild().getId());
                MessageEmbed messageEmbed = new EmbedBuilder()
                        .setDescription(new ParsableText(new TranslatableText("ozzie.permission.user.clear_confirm", commandContext), user.getAsMention()))
                        .build();
                ConfirmationMenu confirmationMenu = new ConfirmationMenu.Builder()
                        .setEventWaiter(OzzieApi.INSTANCE.getEventWaiter())
                        .setCustomEmbed(messageEmbed)
                        .setConfirm(message -> {
                            userPermissionMap.clear();
                            try {
                                OzzieApi.INSTANCE.getDatabaseHandler().updateUser(userSchema);
                            } catch (Throwable throwable) {
                                throwable.printStackTrace();
                            }
                            commandContext.getSource().getEvent().getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.permission.user.clear_all", commandContext), user.getAsMention())).queue();
                        })
                        .build();
                confirmationMenu.display(commandContext.getSource().getEvent().getChannel());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private int clearRolePermissionState(CommandContext<DiscordCommandSource> commandContext) {
        try {
            Role role = RoleArgumentType.getRole(commandContext, "role");
            String permission = StringArgumentType.getString(commandContext, "permission");
            RoleSchema roleSchema = OzzieApi.INSTANCE.getDatabaseHandler().retrieveRole(role.getIdLong());

            if (roleSchema.getPermissions().containsKey(permission)) {
                roleSchema.getPermissions().remove(permission);
                commandContext.getSource().getEvent().getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.permission.role.clear", commandContext), permission, role.getName())).queue();
            } else {
                commandContext.getSource().getEvent().getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.permission.role.not_exist", commandContext), permission, role.getName())).queue();
            }
            OzzieApi.INSTANCE.getDatabaseHandler().updateRole(roleSchema);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private int clearUserPermissionState(CommandContext<DiscordCommandSource> commandContext) {
        MessageReceivedEvent event = commandContext.getSource().getEvent();
        try {
            User user = UserArgumentType.getUser(commandContext, "user");
            String permission = StringArgumentType.getString(commandContext, "permission");
            UserSchema userSchema = OzzieApi.INSTANCE.getDatabaseHandler().retrieveUser(user.getIdLong());

            if (userSchema.getServerPermissionMap().containsKey(event.getGuild().getId())) {
                Map<String, Boolean> userPermissionMap = userSchema.getServerPermissionMap().get(event.getGuild().getId());

                if (userPermissionMap.containsKey(permission)) {
                    userPermissionMap.remove(permission);
                    commandContext.getSource().getEvent().getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.permission.user.clear", commandContext), permission, user.getName())).queue();
                } else {
                    commandContext.getSource().getEvent().getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.permission.user.not_exist", commandContext), permission, user.getName())).queue();
                }
            } else {
                commandContext.getSource().getEvent().getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.permission.user.not_exist", commandContext), permission, user.getName())).queue();
            }

            OzzieApi.INSTANCE.getDatabaseHandler().updateUser(userSchema);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
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
