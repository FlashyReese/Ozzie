package me.wilsonhu.ozzie.commands;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.core.command.Command;
import me.wilsonhu.ozzie.core.plugin.PluginLoader;
import me.wilsonhu.ozzie.schemas.PluginSchema;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class InstallPlugin extends Command {

    private static final Logger log = LogManager.getLogger(InstallPlugin.class);

    public InstallPlugin() {
        super(new String[] {"installplugin"}, "Installs plugin via direct upload or URL", "%s\n%s <URL>");
        this.setCategory("developer");
        this.setPermission("ozzie.developer");
    }

    @Override
    public void onCommand(String full, String[] args, MessageReceivedEvent event, Ozzie ozzie) throws Exception {
        if(!event.getMessage().getAttachments().isEmpty()) {
            if(event.getMessage().getAttachments().get(0).getFileName().endsWith(".jar")) {
                Random r = new Random();
                int code = r.nextInt(9999);
                event.getChannel().sendMessage("Installing Plugin: `" + event.getMessage().getAttachments().get(0).getFileName() + "`. To confirm action please enter: `"+ code + "`.").queue();
                ozzie.getEventWaiter().waitForEvent(MessageReceivedEvent.class,
                        e -> e.getAuthor().equals(event.getAuthor())
                                && e.getChannel().equals(event.getChannel())
                                && !e.getMessage().equals(event.getMessage()),
                        e -> pinConfirm(ozzie, event, e, code),
                        1, TimeUnit.MINUTES, () -> event.getChannel().sendMessage("Sorry, you took too long.").queue());

            }
        }else {
            //Please attach a file TODO: Adapt wget for this v:
        }
    }

    public void pinConfirm(Ozzie ozzie, MessageReceivedEvent event, MessageReceivedEvent e, int code) {
        if(code == Integer.parseInt(e.getMessage().getContentRaw())) {
            if(!event.getMessage().getAttachments().isEmpty()){
                try {
                    downloadFile(ozzie, event);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            
        }else {
            event.getChannel().sendMessage("Unsuccessful Verification").queue();
        }
    }

    public void downloadFile(Ozzie ozzie, MessageReceivedEvent event) throws Exception{
        File newFile = new File(ozzie.getPluginLoader().getDirectory() + File.separator + event.getMessage().getAttachments().get(0).getFileName());
        if(event.getMessage().getAttachments().get(0).getFileName().toLowerCase().endsWith(".jar")){
            CompletableFuture<File> downloadFile = event.getMessage().getAttachments().get(0).downloadToFile(newFile);
            newFile = downloadFile.get();
            final JarFile jf = new JarFile(newFile);
            final JarEntry je = jf.getJarEntry("ozzie.plugin.json");
            if(je != null) {
                final BufferedReader br = new BufferedReader(new InputStreamReader(jf.getInputStream(je), StandardCharsets.UTF_8));
                PluginSchema pluginSchema = new Gson().fromJson(br, new TypeToken<PluginSchema>() {}.getType());//Saved for future lang references v:
                br.close();
                jf.close();
                if (pluginSchema.getSchemaVersion() != PluginLoader.SCHEMA_VERSION) {//todo: auto reload? or reload based on setting?
                    event.getChannel().sendMessage("Incompatible Plugin Version: Plugin may be using a newer schema, or plugin may be using an outdated schema!").queue();
                }
                File renamedFile = new File(ozzie.getPluginLoader().getDirectory() + File.separator + pluginSchema.getId() + ".jar");
                if(renamedFile.exists()){
                    boolean success = newFile.delete();
                    if(!success){
                        event.getChannel().sendMessage("Something went wrong! :')").queue();
                    }
                    downloadFile = event.getMessage().getAttachments().get(0).downloadToFile(renamedFile);
                    downloadFile.get();
                    if(downloadFile.isDone()){
                        event.getChannel().sendMessage(String.format("Plugin Installed: `%s %s` - `%s`", pluginSchema.getName(), pluginSchema.getVersion(), renamedFile.getName())).queue();
                    }
                }
                boolean success = newFile.renameTo(renamedFile);
                if(success){
                    event.getChannel().sendMessage(String.format("Plugin Installed: `%s %s` - `%s`", pluginSchema.getName(), pluginSchema.getVersion(), renamedFile.getName())).queue();
                }
            }else{
                event.getChannel().sendMessage("File not a valid Plugin").queue();
            }
            jf.close();
        }else{
            event.getChannel().sendMessage("Unsupported File Type as Plugin").queue();
        }
    }
}
