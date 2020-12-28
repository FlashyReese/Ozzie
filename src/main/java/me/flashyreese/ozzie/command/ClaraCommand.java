package me.flashyreese.ozzie.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.flashyreese.ozzie.api.command.Command;
import me.flashyreese.ozzie.api.command.CommandManager;
import me.flashyreese.ozzie.api.util.Tenor;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Random;

public class ClaraCommand extends Command {

    public String[] tags = new String[]{"Clara Oswald", "Clara Who", "Clara Oswin", "Oswin Oswald", "Clara Oswin Oswald", "Doctor Who Clara Oswald", "Doctor Who Clara Oswin Oswald", "Doctor Who Oswin Oswald", "Doctor Who Clara Oswald"};

    public ClaraCommand(String category, String description, String important) {
        super(category, description, important, "ozzie.clara");
    }

    @Override
    public LiteralArgumentBuilder<MessageReceivedEvent> getArgumentBuilder() {
        return CommandManager.literal("clara").requires(this::hasPermission).executes(commandContext -> {
            Random r = new Random();
            int randomNumber = r.nextInt(tags.length);
            List<String> results = new Tenor().getItems(tags[randomNumber]);
            int index = r.nextInt(results.size() - 1);
            String s = results.get(index);
            commandContext.getSource().getChannel().sendMessage(s).queue();
            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
        });
    }
}
