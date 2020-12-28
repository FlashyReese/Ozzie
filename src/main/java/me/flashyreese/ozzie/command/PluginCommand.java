package me.flashyreese.ozzie.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.vdurmont.semver4j.Semver;
import me.flashyreese.common.util.FileUtil;
import me.flashyreese.ozzie.api.OzzieApi;
import me.flashyreese.ozzie.api.command.Command;
import me.flashyreese.ozzie.api.command.CommandManager;
import me.flashyreese.ozzie.api.plugin.OzziePlugin;
import me.flashyreese.ozzie.api.plugin.PluginLoader;
import me.flashyreese.ozzie.api.plugin.PluginMetadataV1;
import me.flashyreese.ozzie.api.util.ConfirmationMenu;
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

public class PluginCommand extends Command {
    public PluginCommand(String category, String description, String important) {
        super(category, description, important, "ozzie.plugin");
    }

    @Override
    public LiteralArgumentBuilder<MessageReceivedEvent> getArgumentBuilder() {
        return CommandManager.literal("plugin").requires(this::hasPermission).executes(commandContext -> com.mojang.brigadier.Command.SINGLE_SUCCESS)
                .then(CommandManager.literal("list")
                        .executes(commandContext -> {
                            MessageReceivedEvent event = commandContext.getSource();
                            //Todo: Navigate-able Embed
                            for (PluginLoader.PluginEntryContainer<OzziePlugin> entryContainer : OzzieApi.INSTANCE.getPluginLoader().getPluginEntryContainers()) {
                                event.getChannel().sendMessage(entryContainer.getPluginMetadata().getName()).queue();
                            }
                            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                        })
                ).then(CommandManager.literal("install")
                        .executes(commandContext -> {
                            MessageReceivedEvent event = commandContext.getSource();
                            if (event.getMessage().getAttachments().isEmpty()) {
                                return 0;
                            }
                            event.getMessage().getAttachments().forEach(attachment -> {
                                try {
                                    this.verifyPlugin(commandContext, attachment);
                                } catch (ExecutionException | InterruptedException | IOException e) {
                                    e.printStackTrace();
                                }
                            });
                            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                        }).then(CommandManager.argument("url", StringArgumentType.greedyString())
                                .executes(commandContext -> {
                                    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                                })
                        )
                ).then(CommandManager.literal("reload")
                        .executes(commandContext -> {
                            MessageReceivedEvent event = commandContext.getSource();
                            event.getChannel().sendMessage("Reloading").queue();
                            OzzieApi.INSTANCE.getPluginLoader().unregisterPlugins();
                            OzzieApi.INSTANCE.getPluginLoader().registerPlugins();
                            event.getChannel().sendMessage("Reload Complete!").queue();
                            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                        })
                );
    }


    //Move this into
    public void verifyPlugin(CommandContext<MessageReceivedEvent> commandContext, Message.Attachment attachment) throws ExecutionException, InterruptedException, IOException {
        MessageReceivedEvent event = commandContext.getSource();
        if (attachment.getFileName().toLowerCase().endsWith(".jar")) {
            File temporalDirectory = new File(OzzieApi.INSTANCE.getPluginLoader().getDirectory() + File.separator + "temp");
            FileUtil.createDirectoryIfNotExist(temporalDirectory);
            File newAttachmentFile = new File(temporalDirectory.getAbsolutePath() + File.separator + attachment.getFileName());
            newAttachmentFile = attachment.downloadToFile(newAttachmentFile).get();

            final JarFile pluginJarFile = new JarFile(newAttachmentFile);
            final JarEntry pluginJarFileJarEntry = pluginJarFile.getJarEntry(String.format("%s.json", OzzieApi.INSTANCE.getPluginLoader().getJsonFileName()));
            if (pluginJarFileJarEntry == null) {
                pluginJarFile.close();
                FileUtil.removeFileDirectory(temporalDirectory);
                //Todo: Generate metadata and load them as lib
                event.getChannel().sendMessage("Missing metadata file").queue();
                return;
            }
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(pluginJarFile.getInputStream(pluginJarFileJarEntry), StandardCharsets.UTF_8));
            PluginMetadataV1 pluginSchema = OzzieApi.INSTANCE.getGson().fromJson(bufferedReader.lines().collect(Collectors.joining()), PluginMetadataV1.class);
            bufferedReader.close();
            if (pluginSchema.getSchemaVersion() == 1) {
                PluginLoader.PluginEntryContainer<OzziePlugin> existingPluginEntryContainer = OzzieApi.INSTANCE.getPluginLoader().getPluginEntryContainers().stream().filter(pluginEntryContainer -> pluginEntryContainer.getPluginMetadata().getId().equals(pluginSchema.getId())).findFirst().orElse(null);

                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setColor(Color.orange)
                        .setTitle("Plugin Details")
                        .addField("Schema Version", Integer.toString(pluginSchema.getSchemaVersion()), true)
                        .addField("Identifier", pluginSchema.getId(), true)
                        .addBlankField(true)
                        .addField("Name", pluginSchema.getName(), true)
                        .addField("Version", pluginSchema.getVersion(), true)
                        .addField("Description", pluginSchema.getDescription(), false)
                        .setFooter("Continue with installation?");

                ConfirmationMenu.Builder builder = new ConfirmationMenu.Builder();
                builder.allowTextInput(true)
                        .setEventWaiter(OzzieApi.INSTANCE.getEventWaiter())
                        .setTimeout(1, TimeUnit.MINUTES)
                        .setUsers(event.getAuthor());

                if (existingPluginEntryContainer != null) {
                    Semver existingSemver = new Semver(existingPluginEntryContainer.getPluginMetadata().getVersion());
                    Semver newSemver = new Semver(pluginSchema.getVersion());
                    //resolve versioning
                    if (existingSemver.isEqualTo(newSemver)) {
                        pluginJarFile.close();
                        FileUtil.removeFileDirectory(temporalDirectory);
                        event.getChannel().sendMessage("Same version already installed").queue();
                        return;
                    } else if (existingSemver.isEquivalentTo(newSemver)) {
                        embedBuilder.setDescription(String.format("`%s` will be replacing `%s`", pluginSchema.getVersion(), existingPluginEntryContainer.getPluginMetadata().getVersion()));
                    } else if (existingSemver.isGreaterThan(newSemver)) {
                        embedBuilder.setDescription(String.format("`%s` is older than `%s`", pluginSchema.getVersion(), existingPluginEntryContainer.getPluginMetadata().getVersion()));
                    } else if (existingSemver.isLowerThan(newSemver)) {
                        embedBuilder.setDescription(String.format("`%s` is newer than `%s`", pluginSchema.getVersion(), existingPluginEntryContainer.getPluginMetadata().getVersion()));
                    }
                    File finalNewAttachmentFile = newAttachmentFile;
                    builder.setConfirm((msg) ->
                    {
                        existingPluginEntryContainer.getEntryPoints().forEach(OzziePlugin::terminatePlugin);
                        existingPluginEntryContainer.getEntryPoints().clear();
                        if (existingPluginEntryContainer.getPluginFile().delete()) {
                            OzzieApi.INSTANCE.getPluginLoader().getPluginEntryContainers().remove(existingPluginEntryContainer);
                            this.renameFileAndVerifyPlugin(pluginJarFile, attachment, event, finalNewAttachmentFile);
                        } else {
                            event.getChannel().sendMessage("something went wrong :c").queue();
                        }
                    });
                } else {
                    File finalNewAttachmentFile1 = newAttachmentFile;
                    builder.setConfirm((msg) -> this.renameFileAndVerifyPlugin(pluginJarFile, attachment, event, finalNewAttachmentFile1));
                }
                builder.setCustomEmbed(embedBuilder.build());
                builder.build().display(event.getChannel());
            } else {
                //Todo: using old or new plugin format, will try load not guaranteed.
                event.getChannel().sendMessage("using old or new plugin format, will try load not guaranteed.").queue();
            }
        } else {
            event.getChannel().sendMessage("Invalid File Format").queue();
        }
    }

    private void renameFileAndVerifyPlugin(JarFile pluginJarFile, Message.Attachment attachment, MessageReceivedEvent event, File finalNewAttachmentFile1) {
        File officialPluginLocation = new File(OzzieApi.INSTANCE.getPluginLoader().getDirectory() + File.separator + attachment.getFileName());
        try {
            pluginJarFile.close();
            Files.move(finalNewAttachmentFile1.toPath(), officialPluginLocation.toPath(), ATOMIC_MOVE);
            OzzieApi.INSTANCE.getPluginLoader().verifyPlugin(officialPluginLocation);
            PluginLoader.PluginEntryContainer<OzziePlugin> entryContainer = OzzieApi.INSTANCE.getPluginLoader().getPluginEntryContainers().stream().filter(pluginEntryContainer -> pluginEntryContainer.getPluginFile().getAbsolutePath().equals(officialPluginLocation.getAbsolutePath())).findFirst().orElse(null);
            if (entryContainer != null) {
                entryContainer.getEntryPoints().forEach(OzziePlugin::initializePlugin);
                event.getChannel().sendMessage("Installation Complete!").queue();
                return;
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        event.getChannel().sendMessage("something went wrong :c").queue();
    }
}
