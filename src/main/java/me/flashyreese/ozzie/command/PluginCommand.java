package me.flashyreese.ozzie.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.vdurmont.semver4j.Semver;
import me.flashyreese.common.util.FileUtil;
import me.flashyreese.ozzie.api.OzzieApi;
import me.flashyreese.ozzie.api.l10n.ParsableText;
import me.flashyreese.ozzie.api.l10n.TranslatableText;
import me.flashyreese.ozzie.api.command.guild.DiscordCommandSource;
import me.flashyreese.ozzie.api.plugin.Plugin;
import me.flashyreese.ozzie.api.plugin.PluginLoader;
import me.flashyreese.ozzie.api.plugin.PluginMetadataV1;
import me.flashyreese.ozzie.api.util.ConfirmationMenu;
import me.flashyreese.ozzie.api.command.guild.DiscordCommand;
import me.flashyreese.ozzie.api.command.guild.DiscordCommandManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;

public class PluginCommand extends DiscordCommand {
    public PluginCommand() {
        super("", "ozzie.plugin.description", "ozzie.plugin");
    }

    @Override
    public LiteralArgumentBuilder<DiscordCommandSource> getArgumentBuilder() {
        return DiscordCommandManager.literal("plugin")
                .requires(this::hasPermission)
                .executes(commandContext -> com.mojang.brigadier.Command.SINGLE_SUCCESS)
                .then(DiscordCommandManager.literal("list")
                        .requires(commandContext -> this.hasPermissionOf(commandContext, "list"))
                        .executes(this::list))
                .then(DiscordCommandManager.literal("install")
                        .requires(commandContext -> this.hasPermissionOf(commandContext, "install"))
                        .executes(this::install)
                        .then(DiscordCommandManager.argument("url", StringArgumentType.greedyString())
                                .requires(commandContext -> this.hasPermissionOf(commandContext, "install.url"))
                                .executes(commandContext -> {
                                    //Todo:
                                    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                                })))
                .then(DiscordCommandManager.literal("reload")
                        .requires(commandContext -> this.hasPermissionOf(commandContext, "reload"))
                        .executes(this::reload));
    }

    private int list(CommandContext<DiscordCommandSource> commandContext){
        MessageReceivedEvent event = commandContext.getSource().getEvent();
        //Todo: Navigate-able Embed
        for (PluginLoader.PluginEntryContainer<Plugin> entryContainer : OzzieApi.INSTANCE.getPluginLoader()
                .getPluginEntryContainers()) {
            event.getChannel().sendMessage(entryContainer.getPluginMetadata().getName()).queue();
        }
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private int install(CommandContext<DiscordCommandSource> commandContext){
        MessageReceivedEvent event = commandContext.getSource().getEvent();
        if (event.getMessage().getAttachments().isEmpty()) {
            return 0;
        }// should manage download here instead
        event.getMessage().getAttachments().forEach(attachment -> {
            try {
                this.verifyPlugin(commandContext, attachment);
            } catch (ExecutionException | InterruptedException | IOException e) {
                e.printStackTrace();
            }
        });
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    public int reload(CommandContext<DiscordCommandSource> commandContext){
        MessageReceivedEvent event = commandContext.getSource().getEvent();
        event.getChannel().sendMessage(new TranslatableText("ozzie.plugin.reload.reloading", commandContext)).queue();
        OzzieApi.INSTANCE.getPluginLoader().unregisterPlugins();
        OzzieApi.INSTANCE.getPluginLoader().registerPlugins();
        event.getChannel().sendMessage(new TranslatableText("ozzie.plugin.reload.complete", commandContext)).queue();
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private void verifyPlugin(CommandContext<DiscordCommandSource> commandContext, Message.Attachment attachment) throws ExecutionException, InterruptedException, IOException {
        MessageReceivedEvent event = commandContext.getSource().getEvent();
        if (attachment.getFileName().toLowerCase().endsWith(".jar")) {
            File temporalDirectory =
                    new File(OzzieApi.INSTANCE.getPluginLoader().getDirectory() + File.separator + "temp");
            FileUtil.createDirectoryIfNotExist(temporalDirectory);
            File newAttachmentFile =
                    new File(temporalDirectory.getAbsolutePath() + File.separator + attachment.getFileName());
            newAttachmentFile = attachment.downloadToFile(newAttachmentFile).get();

            final JarFile pluginJarFile = new JarFile(newAttachmentFile);
            final JarEntry pluginJarFileJarEntry =
                    pluginJarFile.getJarEntry(String.format("%s.json", OzzieApi.INSTANCE.getPluginLoader()
                            .getJsonFileName()));
            if (pluginJarFileJarEntry == null) {
                pluginJarFile.close();
                FileUtil.removeFileDirectory(temporalDirectory);
                event.getChannel().sendMessage(new TranslatableText("ozzie.plugin.install.missing_metadata", commandContext)).queue();
                return;
            }
            BufferedReader bufferedReader =
                    new BufferedReader(new InputStreamReader(pluginJarFile.getInputStream(pluginJarFileJarEntry), StandardCharsets.UTF_8));
            PluginMetadataV1 pluginMetadata = OzzieApi.INSTANCE.getGson()
                    .fromJson(bufferedReader.lines().collect(Collectors.joining()), PluginMetadataV1.class);
            bufferedReader.close();
            if (pluginMetadata.getSchemaVersion() == 1) {
                PluginLoader.PluginEntryContainer<Plugin> existingPluginEntryContainer =
                        OzzieApi.INSTANCE.getPluginLoader()
                                .getPluginEntryContainers()
                                .stream()
                                .filter(pluginEntryContainer -> pluginEntryContainer.getPluginMetadata()
                                        .getId()
                                        .equals(pluginMetadata.getId()))
                                .findFirst()
                                .orElse(null);

                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setColor(Color.orange)
                        .setTitle(new TranslatableText("ozzie.plugin.install.details", commandContext).toString())
                        .addField(new TranslatableText("ozzie.plugin.install.schema_version", commandContext).toString(), Integer.toString(pluginMetadata.getSchemaVersion()), true)
                        .addField(new TranslatableText("ozzie.plugin.install.identifier", commandContext).toString(), pluginMetadata.getId(), true)
                        .addBlankField(true)
                        .addField(new TranslatableText("ozzie.plugin.install.name", commandContext).toString(), pluginMetadata.getName(), true)
                        .addField(new TranslatableText("ozzie.plugin.install.version", commandContext).toString(), pluginMetadata.getVersion(), true)
                        .addField(new TranslatableText("ozzie.plugin.install.description", commandContext).toString(), pluginMetadata.getDescription(), false)
                        .setFooter(new TranslatableText("ozzie.plugin.install.continue_installation", commandContext).toString());

                ConfirmationMenu.Builder builder = new ConfirmationMenu.Builder();
                builder.allowTextInput(true)
                        .setEventWaiter(OzzieApi.INSTANCE.getEventWaiter())
                        .setTimeout(1, TimeUnit.MINUTES)
                        .setUsers(event.getAuthor());

                if (existingPluginEntryContainer != null) {
                    Semver existingSemver = new Semver(existingPluginEntryContainer.getPluginMetadata().getVersion());
                    Semver newSemver = new Semver(pluginMetadata.getVersion());
                    //resolve versioning
                    if (existingSemver.isEqualTo(newSemver)) {
                        pluginJarFile.close();
                        FileUtil.removeFileDirectory(temporalDirectory);
                        event.getChannel().sendMessage(new TranslatableText("ozzie.plugin.install.identical_version", commandContext)).queue();
                        return;
                    } else if (existingSemver.isEquivalentTo(newSemver)) {
                        embedBuilder.setDescription(new ParsableText(new TranslatableText("ozzie.plugin.install.replace", commandContext), pluginMetadata.getVersion(), existingPluginEntryContainer
                                .getPluginMetadata()
                                .getVersion()));
                    } else if (existingSemver.isGreaterThan(newSemver)) {
                        embedBuilder.setDescription(new ParsableText(new TranslatableText("ozzie.plugin.install.older_than", commandContext), pluginMetadata.getVersion(), existingPluginEntryContainer
                                .getPluginMetadata()
                                .getVersion()));
                    } else if (existingSemver.isLowerThan(newSemver)) {
                        embedBuilder.setDescription(new ParsableText(new TranslatableText("ozzie.plugin.install.newer_than", commandContext), pluginMetadata.getVersion(), existingPluginEntryContainer
                                .getPluginMetadata()
                                .getVersion()));
                    }
                    File finalNewAttachmentFile = newAttachmentFile;
                    builder.setConfirm((msg) ->
                    {
                        existingPluginEntryContainer.getEntryPoints().forEach(Plugin::terminatePlugin);
                        existingPluginEntryContainer.getEntryPoints().clear();
                        if (existingPluginEntryContainer.getPluginFile().delete()) {
                            OzzieApi.INSTANCE.getPluginLoader()
                                    .getPluginEntryContainers()
                                    .remove(existingPluginEntryContainer);
                            this.renameFileAndVerifyPlugin(pluginJarFile, attachment, event, finalNewAttachmentFile);
                        } else {
                            event.getChannel().sendMessage(new TranslatableText("ozzie.plugin.install.error", commandContext)).queue();
                        }
                    });
                } else {
                    File finalNewAttachmentFile1 = newAttachmentFile;
                    builder.setConfirm((msg) -> this.renameFileAndVerifyPlugin(pluginJarFile, attachment, event, finalNewAttachmentFile1));
                }
                builder.setCustomEmbed(embedBuilder.build());
                builder.build().display(event.getChannel());
            } else {
                //print invalid schemaVersion
                event.getChannel().sendMessage(new TranslatableText("ozzie.plugin.install.incompatible_schema_version", commandContext)).queue();
            }
        } else {
            event.getChannel().sendMessage("Invalid File Format").queue();
        }
    }

    private void renameFileAndVerifyPlugin(JarFile pluginJarFile, Message.Attachment attachment, MessageReceivedEvent event, File finalNewAttachmentFile1) {
        File officialPluginLocation = new File(OzzieApi.INSTANCE.getPluginLoader()
                .getDirectory() + File.separator + attachment.getFileName());
        try {
            pluginJarFile.close();
            Files.move(finalNewAttachmentFile1.toPath(), officialPluginLocation.toPath(), ATOMIC_MOVE);
            OzzieApi.INSTANCE.getPluginLoader().verifyPlugin(officialPluginLocation);
            PluginLoader.PluginEntryContainer<Plugin> entryContainer = OzzieApi.INSTANCE.getPluginLoader()
                    .getPluginEntryContainers()
                    .stream()
                    .filter(pluginEntryContainer -> pluginEntryContainer.getPluginFile()
                            .getAbsolutePath()
                            .equals(officialPluginLocation.getAbsolutePath()))
                    .findFirst()
                    .orElse(null);
            if (entryContainer != null) {
                entryContainer.getEntryPoints().forEach(Plugin::initializePlugin);
                event.getChannel().sendMessage("Installation Complete!").queue();
                return;
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        event.getChannel().sendMessage("something went wrong :c").queue();
    }
}
