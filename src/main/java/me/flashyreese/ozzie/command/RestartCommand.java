package me.flashyreese.ozzie.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.flashyreese.ozzie.api.OzzieApi;
import me.flashyreese.ozzie.api.command.Command;
import me.flashyreese.ozzie.api.command.CommandManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.URISyntaxException;

public class RestartCommand extends Command {
    public RestartCommand(String category, String description, String important) {
        super(category, description, important, "ozzie.restart");
    }

    @Override
    public LiteralArgumentBuilder<MessageReceivedEvent> getArgumentBuilder() {
        return CommandManager.literal("restart").requires(this::hasPermission).executes(commandContext -> {
            commandContext.getSource().getChannel().sendMessage("Restarting").queue();
            OzzieApi.INSTANCE.stop();
            try {
                OzzieApi.INSTANCE.start();
            } catch (LoginException | URISyntaxException | IOException e) {
                e.printStackTrace();
            }
            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
        });
    }
}
