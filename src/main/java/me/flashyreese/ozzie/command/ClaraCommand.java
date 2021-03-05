package me.flashyreese.ozzie.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.flashyreese.ozzie.api.command.guild.DiscordCommand;
import me.flashyreese.ozzie.api.command.guild.DiscordCommandSource;
import me.flashyreese.ozzie.api.util.Tenor;

import java.util.List;
import java.util.Random;

public class ClaraCommand extends DiscordCommand {

    public String[] tags =
            new String[]{"Clara Oswald", "Clara Who", "Clara Oswin", "Oswin Oswald", "Clara Oswin Oswald", "Doctor Who Clara Oswald", "Doctor Who Clara Oswin Oswald", "Doctor Who Oswin Oswald", "Doctor Who Clara Oswald"};

    public ClaraCommand() {
        super("", "ozzie.clara.description", "ozzie.clara");
    }

    @Override
    public LiteralArgumentBuilder<DiscordCommandSource> getArgumentBuilder() {
        return this.literal("clara")
                .requires(this::hasPermission)
                .executes(this::clara);
    }

    private int clara(CommandContext<DiscordCommandSource> commandContext) {
        Random r = new Random();
        int randomNumber = r.nextInt(tags.length);
        List<String> results = new Tenor().getItems(tags[randomNumber]);
        int index = r.nextInt(results.size() - 1);
        String s = results.get(index);
        commandContext.getSource().getEvent().getChannel().sendMessage(s).queue();
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }
}
