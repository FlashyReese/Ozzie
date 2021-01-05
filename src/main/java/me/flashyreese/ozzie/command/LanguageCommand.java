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

import java.util.Locale;

public class LanguageCommand extends DiscordCommand {
    public LanguageCommand() {
        super("", "ozzie.language.description", "ozzie.language");
    }

    @Override
    public LiteralArgumentBuilder<DiscordCommandSource> getArgumentBuilder() {
        return DiscordCommandManager.literal("language")
                .requires(this::hasPermission)
                .executes(this::language)
                .then(DiscordCommandManager.literal("set")
                        .requires(commandContext -> this.hasPermissionOf(commandContext, "set"))
                        .executes(context -> {
                            //embed chooser
                            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                        })
                        .then(DiscordCommandManager.argument("lang", StringArgumentType.word())
                                .executes(this::setUserLanguage)))
                .then(DiscordCommandManager.literal("clear")
                        .requires(commandContext -> this.hasPermissionOf(commandContext, "clear"))
                        .executes(this::clearUserLanguage))
                .then(DiscordCommandManager.literal("server")
                        .requires(commandContext -> this.hasPermissionOf(commandContext, "server"))
                        .then(DiscordCommandManager.literal("set")
                                .requires(commandContext -> this.hasPermissionOf(commandContext, "server.set"))
                                .executes(context -> {
                                    //Embed chooser
                                    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                                })
                                .then(DiscordCommandManager.argument("lang", StringArgumentType.word())
                                        .executes(this::setServerLanguage)))
                        .then(DiscordCommandManager.literal("clear")
                                .requires(commandContext -> this.hasPermissionOf(commandContext, "server.clear"))
                                .executes(this::clearServerLanguage)));
    }

    private int language(CommandContext<DiscordCommandSource> commandContext) {
        MessageReceivedEvent event = commandContext.getSource().getEvent();
        ServerConfigurationSchema serverConfigurationSchema = commandContext.getSource().getServerConfigurationSchema();
        if (serverConfigurationSchema.isAllowUserLocale()) {
            UserSchema userSchema = commandContext.getSource().getUserSchema();
            if (!userSchema.getLocale().isEmpty()) {
                event.getChannel().sendMessage(
                        new ParsableText(
                                new TranslatableText("ozzie.language.user.current_language", commandContext),
                                event.getAuthor().getAsMention(),
                                OzzieApi.INSTANCE.getL10nManager().parseTag(userSchema.getLocale()).getDisplayName())
                ).queue();
            } else {
                event.getChannel().sendMessage(
                        new ParsableText(
                                new TranslatableText("ozzie.language.user.no_language_set", commandContext),
                                event.getAuthor().getAsMention())
                ).queue();
            }
        } else {
            if (!serverConfigurationSchema.getLocale().isEmpty()) {
                event.getChannel().sendMessage(
                        new ParsableText(
                                new TranslatableText("ozzie.language.server.current_language", commandContext),
                                event.getGuild().getName(),
                                OzzieApi.INSTANCE.getL10nManager().parseTag(serverConfigurationSchema.getLocale()).getDisplayName())
                ).queue();
            } else {
                event.getChannel().sendMessage(
                        new ParsableText(
                                new TranslatableText("ozzie.language.server.no_language_set", commandContext),
                                event.getGuild().getName())
                ).queue();
            }
        }
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private int setUserLanguage(CommandContext<DiscordCommandSource> commandContext) {
        MessageReceivedEvent event = commandContext.getSource().getEvent();
        String lang = StringArgumentType.getString(commandContext, "lang");
        Locale locale = OzzieApi.INSTANCE.getL10nManager().parseTag(lang);//fixme: validate lang

        if (locale != null) {
            UserSchema userSchema = commandContext.getSource().getUserSchema();

            if (userSchema.getLocale().equalsIgnoreCase(lang)) {
                event.getChannel()
                        .sendMessage(new ParsableText(new TranslatableText("ozzie.language.user.already_set_to", commandContext), event.getAuthor()
                                .getAsMention(), lang))
                        .queue();
            } else {
                userSchema.setLocale(locale.toString().toLowerCase());
                try {
                    OzzieApi.INSTANCE.getDatabaseHandler().updateUser(userSchema);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                event.getChannel()
                        .sendMessage(new ParsableText(new TranslatableText("ozzie.language.user.set_to", commandContext), event.getAuthor()
                                .getAsMention(), lang))
                        .queue();
            }
        } else {
            event.getChannel()
                    .sendMessage(new ParsableText(new TranslatableText("ozzie.language.invalid_lang", commandContext), lang))
                    .queue();
        }

        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private int clearUserLanguage(CommandContext<DiscordCommandSource> commandContext) {
        MessageReceivedEvent event = commandContext.getSource().getEvent();

        UserSchema userSchema = commandContext.getSource().getUserSchema();

        if (userSchema.getLocale().isEmpty()) {
            event.getChannel()
                    .sendMessage(new ParsableText(new TranslatableText("ozzie.language.user.already_clear", commandContext), event.getAuthor()
                            .getAsMention()))
                    .queue();
        } else {
            userSchema.setLocale("");
            try {
                OzzieApi.INSTANCE.getDatabaseHandler().updateUser(userSchema);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            event.getChannel()
                    .sendMessage(new ParsableText(new TranslatableText("ozzie.language.user.clear", commandContext), event.getAuthor()
                            .getAsMention()))
                    .queue();
        }
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private int setServerLanguage(CommandContext<DiscordCommandSource> commandContext) {
        MessageReceivedEvent event = commandContext.getSource().getEvent();
        String lang = StringArgumentType.getString(commandContext, "lang");
        Locale locale = OzzieApi.INSTANCE.getL10nManager().parseTag(lang);//fixme: validate lang

        if (locale != null) {
            ServerConfigurationSchema serverConfigurationSchema = commandContext.getSource().getServerConfigurationSchema();

            if (serverConfigurationSchema.getLocale().equalsIgnoreCase(lang)) {
                event.getChannel()
                        .sendMessage(new ParsableText(new TranslatableText("ozzie.language.server.already_set_to", commandContext), event
                                .getGuild()
                                .getName(), lang))
                        .queue();
            } else {
                serverConfigurationSchema.setLocale(locale.toString().toLowerCase());
                try {
                    OzzieApi.INSTANCE.getDatabaseHandler().updateServerConfiguration(serverConfigurationSchema);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                event.getChannel()
                        .sendMessage(new ParsableText(new TranslatableText("ozzie.language.server.set_to", commandContext), event.getGuild()
                                .getName(), lang))
                        .queue();
            }
        } else {
            event.getChannel()
                    .sendMessage(new ParsableText(new TranslatableText("ozzie.language.invalid_lang", commandContext), lang))
                    .queue();
        }

        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private int clearServerLanguage(CommandContext<DiscordCommandSource> commandContext) {
        MessageReceivedEvent event = commandContext.getSource().getEvent();

        ServerConfigurationSchema serverConfigurationSchema = commandContext.getSource().getServerConfigurationSchema();

        if (serverConfigurationSchema.getLocale().isEmpty()) {
            event.getChannel()
                    .sendMessage(new ParsableText(new TranslatableText("ozzie.language.server.already_clear", commandContext), event
                            .getGuild()
                            .getName()))
                    .queue();
        } else {
            serverConfigurationSchema.setLocale("");
            try {
                OzzieApi.INSTANCE.getDatabaseHandler().updateServerConfiguration(serverConfigurationSchema);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            event.getChannel()
                    .sendMessage(new ParsableText(new TranslatableText("ozzie.language.server.clear", commandContext), event.getGuild()
                            .getName()))
                    .queue();
        }
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }
}
