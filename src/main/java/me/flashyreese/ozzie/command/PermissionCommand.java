package me.flashyreese.ozzie.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.flashyreese.ozzie.api.OzzieApi;
import me.flashyreese.ozzie.api.command.Command;
import me.flashyreese.ozzie.api.command.CommandManager;
import me.flashyreese.ozzie.api.command.argument.RoleArgumentType;
import me.flashyreese.ozzie.api.command.argument.UserArgumentType;
import me.flashyreese.ozzie.api.database.mongodb.schema.RoleSchema;
import me.flashyreese.ozzie.api.database.mongodb.schema.UserSchema;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Map;

public class PermissionCommand extends Command {
    public PermissionCommand(String category, String description, String important) {
        super(category, description, important, "ozzie.permission");
    }

    @Override
    public LiteralArgumentBuilder<MessageReceivedEvent> getArgumentBuilder() {
        return CommandManager.literal("permission").requires(this::hasPermission)
                .then(CommandManager.argument("role", RoleArgumentType.role())
                        .then(CommandManager.literal("set")
                                .then(CommandManager.argument("permission", StringArgumentType.word())
                                        .then(CommandManager.argument("state", BoolArgumentType.bool()).executes(commandContext -> {
                                            MessageReceivedEvent event = commandContext.getSource();
                                            Role role = RoleArgumentType.getRole(commandContext, "role");
                                            String permission = StringArgumentType.getString(commandContext, "permission");
                                            //todo: validate permission string, argument type word doesn't read asterisk
                                            boolean state = BoolArgumentType.getBool(commandContext, "state");
                                            try {
                                                RoleSchema roleSchema = OzzieApi.INSTANCE.getDatabaseHandler().retrieveRole(role.getIdLong());
                                                if (roleSchema.permissions().containsKey(permission)){
                                                    if (roleSchema.permissions().get(permission) == state){
                                                        event.getChannel().sendMessage(String.format("Permission `%s` already set to `%s` for `%s`", permission, state, role.getName())).queue();
                                                    }else{
                                                        roleSchema.permissions().put(permission, state);
                                                        event.getChannel().sendMessage(String.format("Permission `%s` updated to `%s` for `%s`", permission, state, role.getName())).queue();
                                                    }
                                                }else{
                                                    event.getChannel().sendMessage(String.format("Permission `%s` with state of `%s` added to `%s`", permission, state, role.getName())).queue();
                                                    roleSchema.permissions().put(permission, state);
                                                }
                                                OzzieApi.INSTANCE.getDatabaseHandler().updateRole(roleSchema);
                                            } catch (Throwable throwable) {
                                                throwable.printStackTrace();
                                                return 0;
                                            }
                                            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                                        }))
                                )
                        ))
                .then(CommandManager.argument("user", UserArgumentType.user())
                        .then(CommandManager.literal("set")
                                .then(CommandManager.argument("permission", StringArgumentType.word())
                                        .then(CommandManager.argument("state", BoolArgumentType.bool()).executes(commandContext -> {
                                            MessageReceivedEvent event = commandContext.getSource();
                                            User user = UserArgumentType.getUser(commandContext, "user");
                                            String permission = StringArgumentType.getString(commandContext, "permission");
                                            //todo: validate permission string
                                            boolean state = BoolArgumentType.getBool(commandContext, "state");
                                            try {
                                                UserSchema userSchema = OzzieApi.INSTANCE.getDatabaseHandler().retrieveUser(user.getIdLong());
                                                if (!userSchema.getServerPermissionMap().containsKey(event.getGuild().getId())){
                                                    userSchema.getServerPermissionMap().put(event.getGuild().getId(), new Object2ObjectOpenHashMap<>());
                                                }

                                                Map<String, Boolean> permissionMap = userSchema.getServerPermissionMap().get(event.getGuild().getId());

                                                if (permissionMap.containsKey(permission)){
                                                    if (permissionMap.get(permission) == state){
                                                        event.getChannel().sendMessage(String.format("Permission `%s` already set to `%s` for `%s`", permission, state, user.getName())).queue();
                                                    }else{
                                                        permissionMap.put(permission, state);
                                                        event.getChannel().sendMessage(String.format("Permission `%s` updated to `%s` for `%s`", permission, state, user.getName())).queue();
                                                    }
                                                }else{
                                                    event.getChannel().sendMessage(String.format("Permission `%s` with state of `%s` added to `%s`", permission, state, user.getName())).queue();
                                                    permissionMap.put(permission, state);
                                                }
                                                OzzieApi.INSTANCE.getDatabaseHandler().updateUser(userSchema);
                                            } catch (Throwable throwable) {
                                                throwable.printStackTrace();
                                                return 0;
                                            }
                                            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                                        }))
                                )
                        )
                );
    }
}
