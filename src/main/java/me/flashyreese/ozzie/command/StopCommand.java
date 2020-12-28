package me.flashyreese.ozzie.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.flashyreese.ozzie.api.OzzieApi;
import me.flashyreese.ozzie.api.command.Command;
import me.flashyreese.ozzie.api.command.CommandManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class StopCommand extends Command {
    public StopCommand(String category, String description, String important) {
        super(category, description, important, "ozzie.stop");
    }

    @Override
    public LiteralArgumentBuilder<MessageReceivedEvent> getArgumentBuilder() {
        return CommandManager.literal("stop").requires(this::hasPermission).executes(commandContext -> {
            OzzieApi.INSTANCE.stop();
            System.exit(0);
            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
        });
    }
}
