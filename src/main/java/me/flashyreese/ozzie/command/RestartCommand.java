package me.flashyreese.ozzie.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.flashyreese.ozzie.api.OzzieApi;
import me.flashyreese.ozzie.api.command.guild.DiscordCommandSource;
import me.flashyreese.ozzie.api.command.guild.DiscordCommand;
import me.flashyreese.ozzie.api.command.guild.DiscordCommandManager;
import me.flashyreese.ozzie.api.l10n.ParsableText;
import me.flashyreese.ozzie.api.l10n.TranslatableText;

public class RestartCommand extends DiscordCommand {
    public RestartCommand() {
        super("", "ozzie.restart.description", "ozzie.restart");
    }

    @Override
    public LiteralArgumentBuilder<DiscordCommandSource> getArgumentBuilder() {
        return DiscordCommandManager.literal("restart")
                .requires(this::hasPermission)
                .executes(this::restartInstance);
    }

    private int restartInstance(CommandContext<DiscordCommandSource> commandContext) {
        commandContext.getSource().getEvent().getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.restart.restarting", commandContext), OzzieApi.INSTANCE.getBotName())).queue();
        OzzieApi.INSTANCE.restart();
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }
}
