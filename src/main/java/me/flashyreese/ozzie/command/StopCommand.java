package me.flashyreese.ozzie.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.flashyreese.ozzie.api.OzzieApi;
import me.flashyreese.ozzie.api.command.guild.DiscordCommandSource;
import me.flashyreese.ozzie.api.command.guild.DiscordCommand;
import me.flashyreese.ozzie.api.command.guild.DiscordCommandManager;

public class StopCommand extends DiscordCommand {
    public StopCommand() {
        super("", "ozzie.stop.description", "ozzie.stop");
    }

    @Override
    public LiteralArgumentBuilder<DiscordCommandSource> getArgumentBuilder() {
        return DiscordCommandManager.literal("stop")
                .requires(this::hasPermission)
                .executes(this::stopInstance);
    }

    private int stopInstance(CommandContext<DiscordCommandSource> context) {
        OzzieApi.INSTANCE.stop();
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }
}
