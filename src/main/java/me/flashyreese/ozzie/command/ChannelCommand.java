package me.flashyreese.ozzie.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.flashyreese.ozzie.api.OzzieApi;
import me.flashyreese.ozzie.api.command.guild.DiscordCommandSource;
import me.flashyreese.ozzie.api.database.mongodb.schema.ServerConfigurationSchema;
import me.flashyreese.ozzie.api.l10n.ParsableText;
import me.flashyreese.ozzie.api.l10n.TranslatableText;
import me.flashyreese.ozzie.api.command.guild.DiscordCommand;
import me.flashyreese.ozzie.api.command.guild.DiscordCommandManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ChannelCommand extends DiscordCommand {
    public ChannelCommand() {
        super("", "ozzie.channel.description", "ozzie.channel");
    }

    @Override
    public LiteralArgumentBuilder<DiscordCommandSource> getArgumentBuilder() {
        return DiscordCommandManager.literal("channel")
                .requires(this::hasPermission)
                .then(DiscordCommandManager.literal("add")
                        .requires(commandContext -> this.hasPermissionOf(commandContext, "add"))
                        .executes(context -> {
                            //Embed selector
                            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                        })
                        .then(DiscordCommandManager.argument("channels", StringArgumentType.greedyString())
                                .executes(this::addMentioned)))
                .then(DiscordCommandManager.literal("remove")
                        .requires(commandContext -> this.hasPermissionOf(commandContext, "remove"))
                        .executes(context -> {
                            //Embed selector
                            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                        })
                        .then(DiscordCommandManager.argument("channels", StringArgumentType.greedyString())
                                .executes(this::removeMentioned)))
                .then(DiscordCommandManager.literal("list")
                        .requires(commandContext -> this.hasPermissionOf(commandContext, "list"))
                        .executes(this::list));
    }

    private int addMentioned(CommandContext<DiscordCommandSource> commandContext) {
        MessageReceivedEvent event = commandContext.getSource().getEvent();
        try {
            StringBuilder builder = new StringBuilder();
            ServerConfigurationSchema serverConfigurationSchema = commandContext.getSource().getServerConfigurationSchema();
            for (TextChannel tc : event.getMessage().getMentionedChannels()) {
                if (serverConfigurationSchema.getAllowedCommandTextChannel().contains(tc.getIdLong())) {
                    builder.append(new ParsableText(new TranslatableText("ozzie.channel.already_add", commandContext), tc.getAsMention())).append("\n");
                } else {
                    serverConfigurationSchema.getAllowedCommandTextChannel().add(tc.getIdLong());
                    builder.append(new ParsableText(new TranslatableText("ozzie.channel.add", commandContext), tc.getAsMention())).append("\n");
                }
            }
            OzzieApi.INSTANCE.getDatabaseHandler().updateServerConfiguration(serverConfigurationSchema);
            event.getChannel().sendMessage(builder.toString()).queue();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return 0;
        }
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private int removeMentioned(CommandContext<DiscordCommandSource> commandContext) {
        MessageReceivedEvent event = commandContext.getSource().getEvent();
        try {
            StringBuilder builder = new StringBuilder();
            ServerConfigurationSchema serverConfigurationSchema = commandContext.getSource().getServerConfigurationSchema();
            for (TextChannel tc : event.getMessage().getMentionedChannels()) {
                if (!serverConfigurationSchema.getAllowedCommandTextChannel().contains(tc.getIdLong())) {
                    builder.append(new ParsableText(new TranslatableText("ozzie.channel.remove_invalid", commandContext), tc.getAsMention())).append("\n");
                } else {
                    serverConfigurationSchema.getAllowedCommandTextChannel().remove(tc.getIdLong());
                    builder.append(new ParsableText(new TranslatableText("ozzie.channel.remove", commandContext), tc.getAsMention())).append("\n");
                }
            }
            OzzieApi.INSTANCE.getDatabaseHandler().updateServerConfiguration(serverConfigurationSchema);
            event.getChannel().sendMessage(builder.toString()).queue();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return 0;
        }
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private int list(CommandContext<DiscordCommandSource> commandContext) {
        MessageReceivedEvent event = commandContext.getSource().getEvent();
        try {
            ServerConfigurationSchema serverConfigurationSchema = commandContext.getSource().getServerConfigurationSchema();
            EmbedBuilder embedBuilder = new EmbedBuilder().setTitle(new TranslatableText("ozzie.channel.list.allowed", commandContext).toString());
            StringBuilder line = new StringBuilder();
            for (long id : serverConfigurationSchema.getAllowedCommandTextChannel()) {
                TextChannel textChannel = event.getGuild().getTextChannelById(id);
                if (textChannel != null) {
                    line.append(textChannel.getAsMention()).append(" ");
                }
            }
            embedBuilder.addField(new TranslatableText("ozzie.channel.list.channels", commandContext).toString(), line.toString(), false);
            event.getChannel().sendMessage(embedBuilder.build()).queue();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return 0;
        }
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }
}
