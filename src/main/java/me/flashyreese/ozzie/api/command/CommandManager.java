/*
 * Copyright © 2021 FlashyReese <reeszrbteam@gmail.com>
 *
 * This file is part of Ozzie.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.flashyreese.ozzie.api.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import me.flashyreese.ozzie.api.OzzieApi;
import me.flashyreese.ozzie.api.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents an abstract Command Manager.
 *
 * @author FlashyReese
 * @version 0.9.0+build-20210105
 * @since 0.9.0+build-20210105
 */
public abstract class CommandManager<S, Y extends Command<S>> {
    private final List<CommandContainer<S, Y>> commandContainers = new ArrayList<>();

    private final CommandDispatcher<S> dispatcher = new CommandDispatcher<>();

    /**
     * Registers Command to the Command Dispatcher and Command Containers.
     *
     * @param identifier Identifier
     * @param command    Command Object that extends to Command
     * @return Success
     */
    public boolean registerCommand(Identifier identifier, Y command) {
        Optional<CommandContainer<S, Y>> optional = this.commandContainers.stream().filter(container -> container.getIdentifier().equals(identifier)).findFirst();
        if (!optional.isPresent()) {
            CommandContainer<S, Y> commandContainer = new CommandContainer<S, Y>(identifier, command);

            Optional<CommandNode<S>> optionalSCommandNode = this.dispatcher.getRoot().getChildren().stream().filter(child -> child.getName().equals(command.getArgumentBuilder().getLiteral())).findFirst();
            if (!optionalSCommandNode.isPresent()) {
                this.dispatcher.register(commandContainer.getCommand().getArgumentBuilder());
                this.commandContainers.add(commandContainer);
                return true;
            }
        }
        OzzieApi.INSTANCE.getLogger().warn("Existing identifier \"{}\" skipping...", identifier.toString());
        return false;
    }

    /**
     * Unregisters Command from the Command Dispatcher and Command Containers.
     *
     * @param identifier Identifier
     * @return Success
     */
    public boolean unregisterCommand(Identifier identifier) {
        Optional<CommandContainer<S, Y>> optional = this.commandContainers.stream().filter(container -> container.getIdentifier().equals(identifier)).findFirst();
        if (optional.isPresent()) {
            CommandContainer<S, Y> commandContainer = optional.get();
            this.unregisterCommand(commandContainer);
            this.commandContainers.remove(commandContainer);
            return true;
        } else {
            OzzieApi.INSTANCE.getLogger().warn("Non existing identifier \"{}\" skipping...", identifier.toString());
            return false;
        }
    }

    /**
     * Unregisters Command from the Command Dispatcher and Command Containers.
     *
     * @param commandClass Class of the Command
     * @return Success
     */
    public boolean unregisterCommand(Class<? extends Y> commandClass) {
        Optional<CommandContainer<S, Y>> optional = this.commandContainers.stream().filter(container -> container.getCommand().getClass() == commandClass).findFirst();
        if (optional.isPresent()) {
            CommandContainer<S, Y> commandContainer = optional.get();
            this.unregisterCommand(commandContainer);
            this.commandContainers.remove(commandContainer);
            return true;
        } else {
            OzzieApi.INSTANCE.getLogger().warn("Non existing class \"{}\" skipping...", commandClass.toString());
            return false;
        }
    }

    /**
     * Unregisters Command Container from Command Containers if exist.
     *
     * @param commandContainer Command Container
     */
    private void unregisterCommand(CommandContainer<S, Y> commandContainer) {
        this.dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(commandContainer.getCommand().getArgumentBuilder().getLiteral()));
    }

    /**
     * Executes registered Dispatcher Command from Command Source.
     *
     * @param commandSource Command Source
     * @param command Command
     * @return Success(1)
     * @throws CommandSyntaxException if invalid command, syntax or insufficient permissions
     */
    public int executes(S commandSource, String command) throws CommandSyntaxException {
        return this.dispatcher.execute(command, commandSource);
    }

    public CommandDispatcher<S> getDispatcher() {
        return this.dispatcher;
    }

    public List<CommandContainer<S, Y>> getCommandContainers() {
        return this.commandContainers;
    }

    public static class CommandContainer<S, Y extends Command<S>> {
        private final Identifier identifier;
        private final Y command;

        public CommandContainer(Identifier identifier, Y command) {
            this.identifier = identifier;
            this.command = command;
        }

        public Identifier getIdentifier() {
            return identifier;
        }

        public Y getCommand() {
            return command;
        }
    }

}
