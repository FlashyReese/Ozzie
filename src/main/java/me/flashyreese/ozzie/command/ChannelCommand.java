package me.flashyreese.ozzie.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.flashyreese.ozzie.api.OzzieApi;
import me.flashyreese.ozzie.api.command.Command;
import me.flashyreese.ozzie.api.command.CommandManager;
import me.flashyreese.ozzie.api.database.mongodb.schema.ServerConfigurationSchema;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ChannelCommand extends Command {
    public ChannelCommand(String category, String description, String important) {
        super(category, description, important, "ozzie.channel");
    }

    @Override
    public LiteralArgumentBuilder<MessageReceivedEvent> getArgumentBuilder() {
        return CommandManager.literal("channel").requires(this::hasPermission)
                .then(CommandManager.literal("add")
                        .then(CommandManager.argument("channels", StringArgumentType.greedyString())
                                .executes(commandContext -> {
                                    MessageReceivedEvent event = commandContext.getSource();
                                    try {
                                        ServerConfigurationSchema serverConfigurationSchema = OzzieApi.INSTANCE.getDatabaseHandler().retrieveServerConfiguration(event.getGuild().getIdLong());
                                        for (TextChannel tc : event.getMessage().getMentionedChannels()) {
                                            if (serverConfigurationSchema.getAllowedCommandTextChannel().contains(tc.getIdLong())) {
                                                event.getChannel().sendMessage("Already allowed").queue();
                                            } else {
                                                serverConfigurationSchema.getAllowedCommandTextChannel().add(tc.getIdLong());
                                                event.getChannel().sendMessage("Added").queue();
                                            }
                                        }
                                        OzzieApi.INSTANCE.getDatabaseHandler().updateServerConfiguration(serverConfigurationSchema);
                                    } catch (Throwable throwable) {
                                        throwable.printStackTrace();
                                        return 0;
                                    }
                                    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                                })
                        )
                ).then(CommandManager.literal("remove")
                        .then(CommandManager.argument("channels", StringArgumentType.greedyString())
                                .executes(commandContext -> {
                                    MessageReceivedEvent event = commandContext.getSource();
                                    try {
                                        ServerConfigurationSchema serverConfigurationSchema = OzzieApi.INSTANCE.getDatabaseHandler().retrieveServerConfiguration(event.getGuild().getIdLong());
                                        for (TextChannel tc : event.getMessage().getMentionedChannels()) {
                                            if (!serverConfigurationSchema.getAllowedCommandTextChannel().contains(tc.getIdLong())) {
                                                event.getChannel().sendMessage("Doesn't exist").queue();
                                            } else {
                                                serverConfigurationSchema.getAllowedCommandTextChannel().remove(tc.getIdLong());
                                                event.getChannel().sendMessage("Removed").queue();
                                            }
                                        }
                                        OzzieApi.INSTANCE.getDatabaseHandler().updateServerConfiguration(serverConfigurationSchema);
                                    } catch (Throwable throwable) {
                                        throwable.printStackTrace();
                                        return 0;
                                    }
                                    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                                })
                        )
                ).then(CommandManager.literal("list").executes(commandContext -> {
                    MessageReceivedEvent event = commandContext.getSource();
                    try {
                        ServerConfigurationSchema serverConfigurationSchema = OzzieApi.INSTANCE.getDatabaseHandler().retrieveServerConfiguration(event.getGuild().getIdLong());
                        EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("List of Allowed Text Channels");
                        StringBuilder line = new StringBuilder();
                        for (long id : serverConfigurationSchema.getAllowedCommandTextChannel()) {
                            if (event.getGuild().getTextChannelById(id) != null) {
                                line.append(event.getGuild().getTextChannelById(id).getAsMention()).append(" ");
                            }
                        }
                        embedBuilder.addField("Channels", line.toString(), false);
                        event.getChannel().sendMessage(embedBuilder.build()).queue();
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                        return 0;
                    }
                    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                }));
    }
}
