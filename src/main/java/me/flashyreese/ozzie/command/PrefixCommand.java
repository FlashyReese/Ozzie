package me.flashyreese.ozzie.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.flashyreese.ozzie.api.OzzieApi;
import me.flashyreese.ozzie.api.command.guild.DiscordCommandSource;
import me.flashyreese.ozzie.api.database.mongodb.schema.ServerConfigurationSchema;
import me.flashyreese.ozzie.api.database.mongodb.schema.UserSchema;
import me.flashyreese.ozzie.api.l10n.ParsableText;
import me.flashyreese.ozzie.api.l10n.TranslatableText;
import me.flashyreese.ozzie.api.command.guild.DiscordCommand;
import me.flashyreese.ozzie.api.command.guild.DiscordCommandManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class PrefixCommand extends DiscordCommand {
    public PrefixCommand() {
        super("ozzie.help.uncategorized", "ozzie.prefix.description", "ozzie.prefix");
    }

    @Override
    public LiteralArgumentBuilder<DiscordCommandSource> getArgumentBuilder() {
        return DiscordCommandManager.literal("prefix")
                .requires(this::hasPermission)
                .executes(this::prefix)
                .then(DiscordCommandManager.literal("set")
                        .requires(commandContext -> this.hasPermissionOf(commandContext, "set"))
                        .then(DiscordCommandManager.argument("prefix", StringArgumentType.string())
                                .executes(this::setUserPrefix)))
                .then(DiscordCommandManager.literal("clear")
                        .requires(commandContext -> this.hasPermissionOf(commandContext, "clear"))
                        .executes(this::clearUserPrefix))
                .then(DiscordCommandManager.literal("server")
                        .requires(commandContext -> this.hasPermissionOf(commandContext, "server"))
                        .then(DiscordCommandManager.literal("set")
                                .requires(commandContext -> this.hasPermissionOf(commandContext, "server.set"))
                                .then(DiscordCommandManager.argument("prefix", StringArgumentType.string())
                                        .executes(this::setServerPrefix)))
                        .then(DiscordCommandManager.literal("clear")
                                .requires(commandContext -> this.hasPermissionOf(commandContext, "server.clear"))
                                .executes(this::clearServerPrefix)));
    }

    private int prefix(CommandContext<DiscordCommandSource> commandContext){
        MessageReceivedEvent event = commandContext.getSource().getEvent();
        ServerConfigurationSchema serverConfigurationSchema = commandContext.getSource().getServerConfigurationSchema();
        if (serverConfigurationSchema.isAllowUserCommandPrefix()) {
            UserSchema userSchema = commandContext.getSource().getUserSchema();
            if (!userSchema.getCommandPrefix().isEmpty()) {
                event.getChannel().sendMessage(
                        new ParsableText(
                                new TranslatableText("ozzie.prefix.user.current_prefix", commandContext),
                                event.getAuthor().getAsMention(),
                                userSchema.getCommandPrefix())
                ).queue();
            } else {
                event.getChannel().sendMessage(
                        new ParsableText(
                                new TranslatableText("ozzie.prefix.user.no_prefix_set", commandContext),
                                event.getAuthor().getAsMention())
                ).queue();
            }
        } else {
            if (!serverConfigurationSchema.getCommandPrefix().isEmpty()) {
                event.getChannel().sendMessage(
                        new ParsableText(
                                new TranslatableText("ozzie.prefix.server.current_prefix", commandContext),
                                event.getGuild().getName(),
                                serverConfigurationSchema.getCommandPrefix())
                ).queue();
            } else {
                event.getChannel().sendMessage(
                        new ParsableText(
                                new TranslatableText("ozzie.prefix.server.no_prefix_set", commandContext),
                                event.getGuild().getName())
                ).queue();
            }
        }
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private int setUserPrefix(CommandContext<DiscordCommandSource> commandContext){
        MessageReceivedEvent event = commandContext.getSource().getEvent();
        String prefix = StringArgumentType.getString(commandContext, "prefix");//fixme: validate prefix length

        UserSchema userSchema = commandContext.getSource().getUserSchema();

        if (userSchema.getCommandPrefix().equals(prefix)) {
            event.getChannel()
                    .sendMessage(new ParsableText(new TranslatableText("ozzie.prefix.user.already_set_to", commandContext), event.getAuthor()
                            .getAsMention(), prefix))
                    .queue();
        } else {
            userSchema.setCommandPrefix(prefix);
            try {
                OzzieApi.INSTANCE.getDatabaseHandler().updateUser(userSchema);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            event.getChannel()
                    .sendMessage(new ParsableText(new TranslatableText("ozzie.prefix.user.set_to", commandContext), event.getAuthor()
                            .getAsMention(), prefix))
                    .queue();
        }

        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private int clearUserPrefix(CommandContext<DiscordCommandSource> commandContext) {
        MessageReceivedEvent event = commandContext.getSource().getEvent();

        UserSchema userSchema = commandContext.getSource().getUserSchema();

        if (userSchema.getLocale().isEmpty()) {
            event.getChannel()
                    .sendMessage(new ParsableText(new TranslatableText("ozzie.prefix.user.already_clear", commandContext), event.getAuthor()
                            .getAsMention()))
                    .queue();
        } else {
            userSchema.setCommandPrefix("");
            try {
                OzzieApi.INSTANCE.getDatabaseHandler().updateUser(userSchema);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            event.getChannel()
                    .sendMessage(new ParsableText(new TranslatableText("ozzie.prefix.user.clear", commandContext), event.getAuthor()
                            .getAsMention()))
                    .queue();
        }
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private int setServerPrefix(CommandContext<DiscordCommandSource> commandContext) {
        MessageReceivedEvent event = commandContext.getSource().getEvent();
        String prefix = StringArgumentType.getString(commandContext, "prefix");//fixme: validate prefix length

        ServerConfigurationSchema serverConfigurationSchema = commandContext.getSource().getServerConfigurationSchema();

        if (serverConfigurationSchema.getCommandPrefix().equals(prefix)) {
            event.getChannel()
                    .sendMessage(new ParsableText(new TranslatableText("ozzie.prefix.server.already_set_to", commandContext), event
                            .getGuild()
                            .getName(), prefix))
                    .queue();
        } else {
            serverConfigurationSchema.setCommandPrefix(prefix);
            try {
                OzzieApi.INSTANCE.getDatabaseHandler().updateServerConfiguration(serverConfigurationSchema);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            event.getChannel()
                    .sendMessage(new ParsableText(new TranslatableText("ozzie.prefix.server.set_to", commandContext), event.getGuild()
                            .getName(), prefix))
                    .queue();
        }

        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private int clearServerPrefix(CommandContext<DiscordCommandSource> context) {
        MessageReceivedEvent event = context.getSource().getEvent();

        ServerConfigurationSchema serverConfigurationSchema = context.getSource().getServerConfigurationSchema();

        if (serverConfigurationSchema.getCommandPrefix().isEmpty()) {
            event.getChannel()
                    .sendMessage(new ParsableText(new TranslatableText("ozzie.prefix.server.already_clear", context), event
                            .getGuild()
                            .getName()))
                    .queue();
        } else {
            serverConfigurationSchema.setCommandPrefix("");
            try {
                OzzieApi.INSTANCE.getDatabaseHandler().updateServerConfiguration(serverConfigurationSchema);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            event.getChannel()
                    .sendMessage(new ParsableText(new TranslatableText("ozzie.prefix.server.clear", context), event.getGuild()
                            .getName()))
                    .queue();
        }
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }
}
