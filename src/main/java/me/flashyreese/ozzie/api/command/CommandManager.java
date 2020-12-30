package me.flashyreese.ozzie.api.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.flashyreese.ozzie.api.OzzieApi;
import me.flashyreese.ozzie.api.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class CommandManager<S, Y extends Command<S>> {
    private final List<CommandContainer<S, Y>> commandContainers = new ArrayList<>();

    private final CommandDispatcher<S> dispatcher = new CommandDispatcher<>();

    public void registerCommand(Identifier identifier, Y command) {
        Optional<CommandContainer<S, Y>> optional = this.commandContainers.stream().filter(container -> container.getIdentifier().equals(identifier)).findFirst();
        if (!optional.isPresent()) {
            CommandContainer<S, Y> commandContainer = new CommandContainer<S, Y>(identifier, command);
            this.commandContainers.add(commandContainer);
            this.dispatcher.register(commandContainer.getCommand().getArgumentBuilder());
        } else {
            OzzieApi.INSTANCE.getLogger().warn("Existing identifier \"{}\" skipping...", identifier.toString());
        }
    }

    public void unregisterCommand(Identifier identifier) {
        Optional<CommandContainer<S, Y>> optional = this.commandContainers.stream().filter(container -> container.getIdentifier().equals(identifier)).findFirst();
        if (optional.isPresent()) {
            CommandContainer<S, Y> commandContainer = optional.get();
            this.unregisterCommand(commandContainer);
            this.commandContainers.remove(commandContainer);
        } else {
            OzzieApi.INSTANCE.getLogger().warn("Non existing identifier \"{}\" skipping...", identifier.toString());
        }
    }

    public void unregisterCommand(Class<? extends Y> commandClass) {
        Optional<CommandContainer<S, Y>> optional = this.commandContainers.stream().filter(container -> container.getCommand().getClass() == commandClass).findFirst();
        if (optional.isPresent()) {
            CommandContainer<S, Y> commandContainer = optional.get();
            this.unregisterCommand(commandContainer);
            this.commandContainers.remove(commandContainer);
        } else {
            OzzieApi.INSTANCE.getLogger().warn("Non existing class \"{}\" skipping...", commandClass.toString());
        }
    }

    private void unregisterCommand(CommandContainer<S, Y> commandContainer) {
        this.dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(commandContainer.getCommand().getArgumentBuilder().getLiteral()));
    }

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
