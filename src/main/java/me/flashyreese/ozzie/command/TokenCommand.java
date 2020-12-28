package me.flashyreese.ozzie.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.flashyreese.ozzie.api.OzzieApi;
import me.flashyreese.ozzie.api.command.Command;
import me.flashyreese.ozzie.api.command.CommandManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class TokenCommand extends Command {
    public TokenCommand(String category, String description, String important) {
        super(category, description, important, "ozzie.token");
    }

    @Override
    public LiteralArgumentBuilder<MessageReceivedEvent> getArgumentBuilder() {
        return CommandManager.literal("token").requires(this::hasPermission)
                .then(CommandManager.literal("add")
                        .then(CommandManager.argument("tokenName", StringArgumentType.word())
                                .then(CommandManager.argument("token", StringArgumentType.word())
                                        .executes(commandContext -> {
                                            MessageReceivedEvent event = commandContext.getSource();
                                            String tokenName = StringArgumentType.getString(commandContext, "tokenName");
                                            String token = StringArgumentType.getString(commandContext, "token");

                                            if (OzzieApi.INSTANCE.getTokenManager().containsKey(tokenName)) {
                                                OzzieApi.INSTANCE.getTokenManager().addToken(tokenName, token);
                                                event.getChannel().sendMessage(String.format("The token `%s` has been updated.", tokenName)).queue();
                                            } else {
                                                event.getChannel().sendMessage(String.format("The token `%s` has been added.", tokenName)).queue();
                                            }
                                            event.getMessage().delete().queue();
                                            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                ).then(CommandManager.literal("remove")
                        .then(CommandManager.argument("tokenName", StringArgumentType.word())
                                .executes(commandContext -> {
                                    MessageReceivedEvent event = commandContext.getSource();
                                    String tokenName = StringArgumentType.getString(commandContext, "tokenName");

                                    if (OzzieApi.INSTANCE.getTokenManager().containsKey(tokenName)) {
                                        OzzieApi.INSTANCE.getTokenManager().removeToken(tokenName);
                                        event.getChannel().sendMessage(String.format("The token `%s` has been removed.", tokenName)).queue();
                                    } else {
                                        event.getChannel().sendMessage(String.format("The token `%s` does not exist.", tokenName)).queue();
                                    }
                                    event.getMessage().delete().queue();
                                    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                                })
                        )
                ).then(CommandManager.literal("reload")
                        .executes(commandContext -> {
                            MessageReceivedEvent event = commandContext.getSource();
                            event.getChannel().sendMessage("Reloading saved tokens...").queue();
                            OzzieApi.INSTANCE.getTokenManager().loadSavedTokens();
                            event.getChannel().sendMessage("Saved tokens reloaded.").queue();
                            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                        })
                );
    }
}
