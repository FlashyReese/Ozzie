package me.flashyreese.ozzie.api.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.flashyreese.ozzie.api.OzzieApi;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CommandManager implements EventListener {
    private final List<CommandContainer> commandContainers = new ObjectArrayList<>();
    private final List<String> categories = new ObjectArrayList<>();

    private final CommandDispatcher<MessageReceivedEvent> dispatcher = new CommandDispatcher<>();

    public void registerCommand(Identifier identifier, Command command) {
        Optional<CommandContainer> optional = this.commandContainers.stream().filter(container -> container.getIdentifier().equals(identifier)).findFirst();
        if (!optional.isPresent()) {
            CommandContainer commandContainer = new CommandContainer(identifier, command);
            this.commandContainers.add(commandContainer);
            this.dispatcher.register(commandContainer.getCommand().getArgumentBuilder());
            this.createCategories(command);
        } else {
            OzzieApi.INSTANCE.getLogger().warn("Existing identifier \"{}\" skipping...", identifier.toString());
        }
    }

    public void unregisterCommand(Identifier identifier) {
        Optional<CommandContainer> optional = this.commandContainers.stream().filter(container -> container.getIdentifier().equals(identifier)).findFirst();
        if (optional.isPresent()) {
            CommandContainer commandContainer = optional.get();
            this.unregisterCommand(commandContainer);
            this.commandContainers.remove(commandContainer);
        } else {
            OzzieApi.INSTANCE.getLogger().warn("Non existing identifier \"{}\" skipping...", identifier.toString());
        }
    }

    public void unregisterCommand(Class<? extends Command> commandClass) {
        Optional<CommandContainer> optional = this.commandContainers.stream().filter(container -> container.getCommand().getClass() == commandClass).findFirst();
        if (optional.isPresent()) {
            CommandContainer commandContainer = optional.get();
            this.unregisterCommand(commandContainer);
            this.commandContainers.remove(commandContainer);
        } else {
            OzzieApi.INSTANCE.getLogger().warn("Non existing class \"{}\" skipping...", commandClass.toString());
        }
    }

    private void unregisterCommand(CommandContainer commandContainer) {
        this.dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(commandContainer.getCommand().getArgumentBuilder().getLiteral()));
    }

    private void createCategories(Command command) {
        String category = command.getCategory().toLowerCase().trim();
        if (!this.categories.contains(category)) {
            this.categories.add(category);
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

    public void onMessageReceived(MessageReceivedEvent event) throws Throwable {
        if (event.getAuthor().isBot() || event.getAuthor().getIdLong() == event.getJDA().getSelfUser().getIdLong()) {
            return;
        }
        ServerConfigurationSchema serverConfig = OzzieApi.INSTANCE.getDatabaseHandler().retrieveServerConfiguration(event.getGuild().getIdLong());
        String prefix = serverConfig.getCommandPrefix();
        if (prefix.isEmpty())
            prefix = OzzieApi.INSTANCE.getDefaultCommandPrefix();

        if (serverConfig.isAllowUserCommandPrefix()) {
            UserSchema user = OzzieApi.INSTANCE.getDatabaseHandler().retrieveUser(event.getAuthor().getIdLong());
            if (!user.getCommandPrefix().isEmpty()) {
                prefix = user.getCommandPrefix();
            }
        }

        String full = event.getMessage().getContentRaw();
        if (full.startsWith(prefix)) {
            full = full.replaceFirst(prefix, "");
            String finalFull = full;
            Optional<CommandNode<MessageReceivedEvent>> optional = this.dispatcher.getRoot().getChildren().stream().filter(child -> finalFull.startsWith(child.getName())).findFirst();
            if (optional.isPresent()){
                if (serverConfig.getAllowedCommandTextChannel() != null && serverConfig.getAllowedCommandTextChannel().contains(event.getChannel().getIdLong())) {
                    int executionState = this.executes(event, full);
                }
            }
        }
    }

    public int executes(MessageReceivedEvent commandSource, String command) throws CommandSyntaxException {
        return this.dispatcher.execute(command, commandSource);
    }

    public static Map<String, Boolean> permissionMap(MessageReceivedEvent event) {
        Map<String, Boolean> permissions = new Object2ObjectOpenHashMap<>();
        try {
            UserSchema userSchema = OzzieApi.INSTANCE.getDatabaseHandler().retrieveUser(event.getAuthor().getIdLong());
            if (userSchema.getServerPermissionMap().containsKey(event.getGuild().getId())) {
                permissions.putAll(userSchema.getServerPermissionMap().get(event.getGuild().getId()));
            }
            Member member = event.getGuild().getMemberById(event.getAuthor().getIdLong());
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

    public static LiteralArgumentBuilder<MessageReceivedEvent> literal(String literal) {
        return LiteralArgumentBuilder.literal(literal);
    }

    public static <T> RequiredArgumentBuilder<MessageReceivedEvent, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    public CommandDispatcher<MessageReceivedEvent> getDispatcher() {
        return this.dispatcher;
    }

    public List<CommandContainer> getCommandContainers() {
        return this.commandContainers;
    }

    public List<String> getCategories() {
        return this.categories;
    }

    public static class CommandContainer {
        private final Identifier identifier;
        private final Command command;

        public CommandContainer(Identifier identifier, Command command) {
            this.identifier = identifier;
            this.command = command;
        }

        public Identifier getIdentifier() {
            return identifier;
        }

        public Command getCommand() {
            return command;
        }
    }
}
