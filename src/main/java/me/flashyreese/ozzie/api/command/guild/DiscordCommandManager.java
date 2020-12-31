package me.flashyreese.ozzie.api.command.guild;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import me.flashyreese.ozzie.api.OzzieApi;
import me.flashyreese.ozzie.api.command.CommandManager;
import me.flashyreese.ozzie.api.database.mongodb.schema.RoleSchema;
import me.flashyreese.ozzie.api.database.mongodb.schema.ServerConfigurationSchema;
import me.flashyreese.ozzie.api.database.mongodb.schema.UserSchema;
import me.flashyreese.ozzie.api.util.Identifier;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class DiscordCommandManager extends CommandManager<DiscordCommandSource, DiscordCommand> implements EventListener {

    private final List<String> categories = new ArrayList<>();

    @Override
    public void registerCommand(Identifier identifier, DiscordCommand command) {
        Optional<CommandContainer<DiscordCommandSource, DiscordCommand>> optional = this.getCommandContainers().stream().filter(container -> container.getIdentifier().equals(identifier)).findFirst();
        if (!optional.isPresent()) {
            CommandContainer<DiscordCommandSource, DiscordCommand> commandContainer = new CommandContainer<>(identifier, command);
            this.getCommandContainers().add(commandContainer);
            this.getDispatcher().register(commandContainer.getCommand().getArgumentBuilder());
            this.createCategories(commandContainer.getCommand());
        } else {
            OzzieApi.INSTANCE.getLogger().warn("Existing identifier \"{}\" skipping...", identifier.toString());
        }
    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof MessageReceivedEvent) {
            MessageReceivedEvent messageReceivedEvent = (MessageReceivedEvent) event;
            try {
                this.onMessageReceived(messageReceivedEvent);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private void onMessageReceived(MessageReceivedEvent event) throws Throwable {
        if (event.getAuthor().isBot() || event.getAuthor().getIdLong() == event.getJDA().getSelfUser().getIdLong()) {
            return;
        }
        ServerConfigurationSchema serverConfig = OzzieApi.INSTANCE.getDatabaseHandler().retrieveServerConfiguration(event.getGuild().getIdLong());
        UserSchema user = OzzieApi.INSTANCE.getDatabaseHandler().retrieveUser(event.getAuthor().getIdLong());
        String prefix = serverConfig.getCommandPrefix();
        if (prefix.isEmpty())
            prefix = OzzieApi.INSTANCE.getDefaultCommandPrefix();

        if (serverConfig.isAllowUserCommandPrefix()) {
            if (!user.getCommandPrefix().isEmpty()) {
                prefix = user.getCommandPrefix();
            }
        }

        String full = event.getMessage().getContentRaw();
        if (full.startsWith(prefix)) {
            full = full.replaceFirst(prefix, "");
            String finalFull = full;
            Optional<CommandNode<DiscordCommandSource>> optional = this.getDispatcher().getRoot().getChildren().stream().filter(child -> finalFull.startsWith(child.getName())).findFirst();
            if (optional.isPresent()) {
                if (serverConfig.getAllowedCommandTextChannel() != null && serverConfig.getAllowedCommandTextChannel().contains(event.getChannel().getIdLong())) {
                    this.executes(new DiscordCommandSource(user, serverConfig, this.permissionMap(event), event), full);
                }
            }
        }
    }

    private Map<String, Boolean> permissionMap(MessageReceivedEvent event) {
        Map<String, Boolean> permissions = new HashMap<>();
        try {
            UserSchema userSchema = OzzieApi.INSTANCE.getDatabaseHandler().retrieveUser(event.getAuthor().getIdLong());
            if (userSchema.getServerPermissionMap().containsKey(event.getGuild().getId())) {
                permissions.putAll(userSchema.getServerPermissionMap().get(event.getGuild().getId()));
            }

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

    private void createCategories(DiscordCommand command) {
        String category = command.getCategory().toLowerCase().trim();
        if (!this.categories.contains(category)) {
            this.categories.add(category);
        }
    }

    public static LiteralArgumentBuilder<DiscordCommandSource> literal(String literal) {
        return LiteralArgumentBuilder.literal(literal);
    }

    public static <T> RequiredArgumentBuilder<DiscordCommandSource, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    public List<String> getCategories() {
        return this.categories;
    }
}
