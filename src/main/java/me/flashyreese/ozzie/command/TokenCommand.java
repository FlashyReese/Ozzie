package me.flashyreese.ozzie.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.flashyreese.ozzie.api.OzzieApi;
import me.flashyreese.ozzie.api.command.guild.DiscordCommand;
import me.flashyreese.ozzie.api.command.guild.DiscordCommandManager;
import me.flashyreese.ozzie.api.command.guild.DiscordCommandSource;
import me.flashyreese.ozzie.api.l10n.ParsableText;
import me.flashyreese.ozzie.api.l10n.TranslatableText;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class TokenCommand extends DiscordCommand {
    public TokenCommand() {
        super("", "ozzie.token.description", "ozzie.token");
    }

    @Override
    public LiteralArgumentBuilder<DiscordCommandSource> getArgumentBuilder() {
        return DiscordCommandManager.literal("token")
                .requires(this::hasPermission)
                .then(DiscordCommandManager.literal("add")
                        .requires(commandContext -> this.hasPermissionOf(commandContext, "add"))
                        .then(DiscordCommandManager.argument("tokenName", StringArgumentType.string())
                                .then(DiscordCommandManager.argument("token", StringArgumentType.string())
                                        .executes(this::add))))
                .then(DiscordCommandManager.literal("remove")
                        .requires(commandContext -> this.hasPermissionOf(commandContext, "remove"))
                        .then(DiscordCommandManager.argument("tokenName", StringArgumentType.string())
                                .executes(this::remove)))
                .then(DiscordCommandManager.literal("reload")
                        .requires(commandContext -> this.hasPermissionOf(commandContext, "reload"))
                        .executes(this::reload));
    }

    private int add(CommandContext<DiscordCommandSource> commandContext) {
        MessageReceivedEvent event = commandContext.getSource().getEvent();
        String tokenName =
                StringArgumentType.getString(commandContext, "tokenName");
        String token = StringArgumentType.getString(commandContext, "token");

        OzzieApi.INSTANCE.getTokenManager().addToken(tokenName, token);
        if (OzzieApi.INSTANCE.getTokenManager().containsKey(tokenName)) {
            event.getChannel()
                    .sendMessage(new ParsableText(new TranslatableText("ozzie.token.add.updated", commandContext), tokenName))
                    .queue();
        } else {
            event.getChannel()
                    .sendMessage(new ParsableText(new TranslatableText("ozzie.token.add.added", commandContext), tokenName))
                    .queue();
        }
        event.getMessage().delete().queue();
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private int remove(CommandContext<DiscordCommandSource> commandContext) {
        MessageReceivedEvent event = commandContext.getSource().getEvent();
        String tokenName = StringArgumentType.getString(commandContext, "tokenName");

        if (OzzieApi.INSTANCE.getTokenManager().containsKey(tokenName)) {
            OzzieApi.INSTANCE.getTokenManager().removeToken(tokenName);
            event.getChannel()
                    .sendMessage(new ParsableText(new TranslatableText("ozzie.token.remove.removed", commandContext), tokenName))
                    .queue();
        } else {
            event.getChannel()
                    .sendMessage(new ParsableText(new TranslatableText("ozzie.token.remove.not_exist", commandContext), tokenName))
                    .queue();
        }
        event.getMessage().delete().queue();
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private int reload(CommandContext<DiscordCommandSource> commandContext) {
        MessageReceivedEvent event = commandContext.getSource().getEvent();
        event.getChannel().sendMessage(new TranslatableText("ozzie.reload.reloading", commandContext)).queue();
        OzzieApi.INSTANCE.getTokenManager().loadSavedTokens();
        event.getChannel().sendMessage(new TranslatableText("ozzie.reload.complete", commandContext)).queue();
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }
}
