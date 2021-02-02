/*
 * Copyright © 2021 FlashyReese <reeszrbteam@gmail.com>
 *
 * This file is part of Ozzie.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.flashyreese.ozzie.api.command.guild;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import me.flashyreese.ozzie.api.OzzieApi;
import me.flashyreese.ozzie.api.command.CommandManager;
import me.flashyreese.ozzie.api.database.mongodb.schema.ServerConfigurationSchema;
import me.flashyreese.ozzie.api.database.mongodb.schema.UserSchema;
import me.flashyreese.ozzie.api.util.Identifier;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents Discord Command Manager.
 *
 * @author FlashyReese
 * @version 0.9.0+build-20210105
 * @since 0.9.0+build-20210105
 */
public final class DiscordCommandManager extends CommandManager<DiscordCommandSource, DiscordCommand> implements EventListener {

    private final List<String> categories = new ArrayList<>();

    /**
     * Registers Command to the Command Dispatcher, Command Containers and creates a category for command.
     *
     * @param identifier Identifier
     * @param command    Discord Command Object
     * @return Success
     */
    @Override
    public boolean registerCommand(Identifier identifier, DiscordCommand command) {
        if (super.registerCommand(identifier, command)) {
            this.createCategories(command);
            return true;
        }
        return false;
    }

    /**
     * Event Listener for Discord MessageReceivedEvent.
     *
     * @param event GenericEvent
     */
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

    /**
     * Checks if MessageReceivedEvent is a command, if so process and execute command.
     *
     * @param event MessageReceivedEvent
     * @throws Throwable if unable to fetch server/user configurations or invalid command, syntax or insufficient permissions
     */
    private void onMessageReceived(MessageReceivedEvent event) throws Throwable {
        // Verify message author is not a bot or itself
        if (event.getAuthor().isBot() || event.getAuthor().getIdLong() == event.getJDA().getSelfUser().getIdLong()) {
            return;
        }
        // Retrieve server settings and user settings/permissions
        ServerConfigurationSchema serverConfig = OzzieApi.INSTANCE.getDatabaseHandler().retrieveServerConfiguration(event.getGuild().getIdLong());
        UserSchema user = OzzieApi.INSTANCE.getDatabaseHandler().retrieveUser(event.getAuthor().getIdLong());

        // Check prefix for command using server settings and user settings
        String prefix = serverConfig.getCommandPrefix();
        if (prefix.isEmpty())
            prefix = OzzieApi.INSTANCE.getDefaultCommandPrefix();

        if (serverConfig.isAllowUserCommandPrefix()) {
            if (!user.getCommandPrefix().isEmpty()) {
                prefix = user.getCommandPrefix();
            }
        }

        // Check if message starts with prefix
        String full = event.getMessage().getContentRaw();
        if (full.startsWith(prefix)) {
            full = full.replaceFirst(prefix, "");
            String finalFull = full;
            // Check if received command is a valid command
            Optional<CommandNode<DiscordCommandSource>> optional = this.getDispatcher().getRoot().getChildren().stream().filter(child -> finalFull.startsWith(child.getName())).findFirst();
            if (optional.isPresent()) {
                // Verify if command can be executed in text channel
                if (serverConfig.getAllowedCommandTextChannel() != null && serverConfig.getAllowedCommandTextChannel().contains(event.getChannel().getIdLong())) {
                    // Creates a CommandSource and executes command
                    this.executes(new DiscordCommandSource(user, serverConfig, event), full);
                }
            }
        }
    }

    /**
     * Creates a category for a Discord Command if category does not exist.
     *
     * @param command Discord Command
     */
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
