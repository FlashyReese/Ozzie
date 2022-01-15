package me.flashyreese.ozzie.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.flashyreese.ozzie.api.OzzieApi;
import me.flashyreese.ozzie.api.command.guild.DiscordCommand;
import me.flashyreese.ozzie.api.command.guild.DiscordCommandManager;
import me.flashyreese.ozzie.api.command.guild.DiscordCommandSource;
import me.flashyreese.ozzie.api.l10n.ParsableText;
import me.flashyreese.ozzie.api.l10n.TranslatableText;

public class ClientCommand extends DiscordCommand {
    public ClientCommand() {
        super("ozzie.help.category.super_commands", "ozzie.client.description", "ozzie.client");
    }

    @Override
    public LiteralArgumentBuilder<DiscordCommandSource> getArgumentBuilder() {
        return DiscordCommandManager.literal("client")
                .requires(this::hasPermission)
                .then(DiscordCommandManager.literal("stop")
                        .requires(commandContext -> this.hasPermissionOf(commandContext, "stop"))
                        .executes(this::stopInstance))
                .then(DiscordCommandManager.literal("restart")
                        .requires(commandContext -> this.hasPermissionOf(commandContext, "restart"))
                        .executes(this::restartInstance));
    }

    private int stopInstance(CommandContext<DiscordCommandSource> context) {
        OzzieApi.INSTANCE.stop();
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private int restartInstance(CommandContext<DiscordCommandSource> commandContext) {
        commandContext.getSource().getEvent().getChannel().sendMessage(new ParsableText(new TranslatableText("ozzie.client.restart.restarting", commandContext), OzzieApi.INSTANCE.getBotName())).queue();
        OzzieApi.INSTANCE.restart();
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }
}
