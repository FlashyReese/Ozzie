/*
 * Copyright (C) 2019-2020 Yao Chung Hu / FlashyReese
 *
 * Ozzie is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Ozzie is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Ozzie.  If not, see http://www.gnu.org/licenses/
 *
 */
package me.wilsonhu.ozzie.commands;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.core.command.Command;
import me.wilsonhu.ozzie.core.plugin.PluginLoader;
import me.wilsonhu.ozzie.core.plugin.PluginModule;
import me.wilsonhu.ozzie.schemas.PluginSchema;
import me.wilsonhu.ozzie.utilities.ConfirmationMenu;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class InstallPlugin extends Command {

    private PluginSchema currentSchema;
    private static final Logger log = LogManager.getLogger(InstallPlugin.class);

    public InstallPlugin() {
        super(new String[] {"installplugin", "installplugins"}, "Installs plugins via direct upload or URL", "%s\n%s <URL>");
        this.setCategory("developer");
        this.setPermission("ozzie.developer");
    }

    @Override
    public void onCommand(String full, String[] args, MessageReceivedEvent event, Ozzie ozzie) throws Exception {

        if (!event.getMessage().getAttachments().isEmpty()) {
            boolean isValid = false;
            StringBuilder stringBuilder = new StringBuilder();
            Message.Attachment attachment = event.getMessage().getAttachments().get(0);
            if (attachment.getFileName().toLowerCase().endsWith(".jar")) {
                File newFile = new File(ozzie.getPluginLoader().getDirectory() + File.separator + System.currentTimeMillis() + attachment.getFileName());
                CompletableFuture<File> downloadFile = attachment.downloadToFile(newFile);
                newFile = downloadFile.get();
                final JarFile jf = new JarFile(newFile);
                final JarEntry je = jf.getJarEntry("ozzie.plugin.json");
                if (je != null) {
                    final BufferedReader br = new BufferedReader(new InputStreamReader(jf.getInputStream(je), StandardCharsets.UTF_8));
                    JSONObject jsonObject = new JSONObject(br.lines().collect(Collectors.joining()));
                    br.close();
                    int pluginSchemaVersion = jsonObject.getInt("schemaVersion");
                    jf.close();
                    if (pluginSchemaVersion == PluginLoader.SCHEMA_VERSION) {
                        PluginSchema pluginSchema = new Gson().fromJson(jsonObject.toString(), new TypeToken<PluginSchema>(){}.getType());
                        isValid = true;
                        currentSchema = pluginSchema;
                        //Todo: semantic versioning checking for installed plugins when installing updated plugins
                        for(PluginModule pluginModule : ozzie.getPluginLoader().getConfiguredPlugins()){
                            if(pluginModule.getSchema().getId().equalsIgnoreCase(pluginSchema.getId())){
                                
                            }
                        }
                        boolean success = newFile.delete();
                        if (!success) {
                            event.getChannel().sendMessage("Something went wrong :'(").queue();
                            return;
                        }
                    }else{
                        stringBuilder.append(String.format("%s %s `%s`", jsonObject.getString("name"), jsonObject.getString("version"), "Incompatible Plugin Schema Version"));
                    }
                } else {
                    stringBuilder.append(String.format("%s `%s`", attachment.getFileName(), "Invalid Plugin"));
                }
                jf.close();
            } else {
                stringBuilder.append(String.format("%s `%s`", attachment.getFileName(), "Unsupported File Type"));
            }
            if (isValid) {


                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setColor(Color.orange)
                        .setTitle("Plugin Info")
                        .addField("Schema Version", Integer.toString(currentSchema.getSchemaVersion()), true)
                        .addField("Identifier", currentSchema.getId(), true)
                        .addBlankField(true)
                        .addField("Name", currentSchema.getName(), true)
                        .addField("Version", currentSchema.getVersion(), true)
                        .addField("Description", currentSchema.getDescription(), false)

                        .setFooter("Continue with installation?");
                if(currentSchema.getContact().size() > 0){
                    StringBuilder contactString = new StringBuilder();
                    for (Map.Entry<String, String> entry: currentSchema.getContact().entrySet()){
                        contactString.append(String.format("[%s](%s) ", entry.getKey(), entry.getValue()));
                    }
                    embedBuilder.addField("Contact", contactString.toString(), true);
                }
                if(currentSchema.getAuthors().size() > 0){
                    StringBuilder authorsString = new StringBuilder();
                    for (Map.Entry<String, String> entry: currentSchema.getAuthors().entrySet()){
                        authorsString.append(String.format("[%s](%s) ", entry.getKey(), entry.getValue()));
                    }
                    if(currentSchema.getAuthors().size() > 1){
                        embedBuilder.addField("Authors", authorsString.toString(), false);
                    }else{
                        embedBuilder.addField("Author", authorsString.toString(), false);
                    }
                }
                ConfirmationMenu.Builder builder = new ConfirmationMenu.Builder();
                builder.allowTextInput(true)
                        .useCustomEmbed(true)
                        .setCustomEmbed(embedBuilder.build())
                        .setEventWaiter(ozzie.getEventWaiter())
                        .setTimeout(1, TimeUnit.MINUTES)
                        .setConfirm((msg) ->
                        {
                            try {
                                installPlugin(ozzie, currentSchema, attachment, event);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        })
                        .setUsers(event.getAuthor());
                builder.build().display(event.getChannel());
            } else {
                event.getChannel().sendMessage(stringBuilder.toString()).queue();
            }
        }
    }
    public void installPlugin(Ozzie ozzie, PluginSchema pluginSchema, Message.Attachment attachment, MessageReceivedEvent event) throws Exception {
        File newFile = new File(ozzie.getPluginLoader().getDirectory() + File.separator + pluginSchema.getId() + ".jar");
        if(newFile.exists()){
            boolean success = newFile.delete();
            if(!success){
                event.getChannel().sendMessage("Something went wrong! :')").queue();//Todo: weirdly I can delete it but I can modify it I think
            }
        }
        CompletableFuture<File> downloadFile = event.getMessage().getAttachments().get(0).downloadToFile(newFile);
        newFile = downloadFile.get();
        if(downloadFile.isDone()){
            final JarFile jf = new JarFile(newFile);
            final JarEntry je = jf.getJarEntry("ozzie.plugin.json");
            if(je != null) {
                event.getChannel().sendMessage("Installation Complete!").queue();
            }else{
                event.getChannel().sendMessage("Something went wrong! :')").queue();
                newFile.delete();
            }
        }
    }
}
