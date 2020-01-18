package me.wilsonhu.ozzie.core.command;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.commands.*;
import me.wilsonhu.ozzie.core.i18n.TranslatableText;
import me.wilsonhu.ozzie.core.plugin.PluginModule;
import me.wilsonhu.ozzie.schemas.ServerSchema;
import me.wilsonhu.ozzie.schemas.UserSchema;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;

public class CommandManager {

    private static final Logger log = LogManager.getLogger(CommandManager.class);
    private ArrayList<Command> commands;
    private ArrayList<Command> pluginCommands;
    private ArrayList<String> categoryList;
    private Ozzie ozzie;

    public CommandManager(Ozzie ozzie) {
        log.info("Building CommandManager...");
        this.setOzzie(ozzie);
        commands = new ArrayList<Command>();
        pluginCommands = new ArrayList<Command>();
        categoryList = new ArrayList<String>();
        commands.addAll(Arrays.asList(commands()));
        for(Command c: getCommands()) {
            String category = c.getAsCategory().toLowerCase().trim();
            if(!categoryList.contains(category)) {
                categoryList.add(category);
            }
        }
        log.info("CommandManager built!");
    }

    private Command[] commands(){
        return new Command[]{
                //Todo: Botprefix, Channel, permission, changelang
                new About(),
                new Clara(),
                new Evaluate(),
                new Help(),
                new InstallPlugin(),
                new Language(),
                new Ping(),
                new Reload(),
                new Restart(),
                new Shutdown(),
                new Token()
        };
    }

    public void onCommand(MessageReceivedEvent event, String full){
        if(event.getAuthor().isBot() || event.getAuthor().getIdLong() == event.getJDA().getSelfUser().getIdLong())return;
        if(full == null) {
            full = event.getMessage().getContentRaw();
        }
        full = full.trim();
        try{//Todo: Guild Splitting
            if(event.getChannelType().isGuild()){
                ServerSchema serverSchema = getOzzie().getConfigurationManager().getServerSettings(event.getGuild().getIdLong());
                if(serverSchema.isAllowedCommandTextChannel(event.getChannel().getIdLong())){
                    UserSchema userSchema = getOzzie().getConfigurationManager().getUserSettings(event.getAuthor().getIdLong());
                    if(!serverSchema.isAllowUserCustomCommandPrefix() || userSchema.getCustomCommandPrefix().equals("default")){
                        onCommand(event, full, serverSchema.getCustomCommandPrefix());
                    }else if(serverSchema.isAllowUserCustomCommandPrefix() && !userSchema.getCustomCommandPrefix().equals("default")){
                        onCommand(event, full, userSchema.getCustomCommandPrefix());
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void onCommand(MessageReceivedEvent event, String full, String customCommandPrefix) {
        if(!full.equals(customCommandPrefix)){
            if(event.getMessage().getContentRaw().startsWith(customCommandPrefix)){
                full = full.substring(customCommandPrefix.length());
            }
            String[] s;
            if (full.contains(" ")) {
                s = full.split(" ");
            }else{
                s = new String[]{full};
            }
            if(event.getMessage().getContentRaw().startsWith(customCommandPrefix)){
                this.onCommand(getCommands(), s, full, event);
                this.onCommand(getPluginCommands(), s, full, event);
            }
        }
    }

    public void onCommand(ArrayList<Command> list, String[] s, String full, MessageReceivedEvent event) {
        for (Command c: list){
            for (String name: c.getNames()){
                if (name.equalsIgnoreCase(s[0])){
                    if(getOzzie().getConfigurationManager().hasPermission(event.getGuild().getIdLong(), event.getAuthor().getIdLong(), c.getPermission())) {
                        String args = full.substring(name.length()).trim();
                        try{
                            c.onCommand(full, args, event, getOzzie());
                        }catch (Exception e){
                            event.getChannel().sendMessage(c.getHelpEmblem(event)).queue();
                            e.printStackTrace();
                        }
                        return;
                    }else {
                        event.getChannel().sendMessage(new TranslatableText("ozzie.insufficientperms", event).toString()).queue();
                    }
                }
            }
            //TODO: add Type of Chats
        }
    }

    public ArrayList<Command> getCommands()
    {
        return commands;
    }


    public ArrayList<Command> getPluginCommands()
    {
        return pluginCommands;
    }

    public ArrayList<String> getCategoryList(){
        return categoryList;
    }

    public Command getCommand(Class<?extends Command> leCommandClass)
    {
        for (Command c: getCommands())
        {
            if (c.getClass() == leCommandClass)
            {
                return c;
            }
        }
        return null;
    }

    public Command getCommandPlugin(Class<?extends Command> leCommandClass) {
        for (Command c: getPluginCommands())
        {
            if (c.getClass() == leCommandClass)
            {
                return c;
            }
        }
        return null;
    }

    public void addCommands(PluginModule pl) {
        for(Command cmd: pl.getPlugin().getCommands()) {
            if(cmd.getPermission().equalsIgnoreCase("ozzie.default")) {
                cmd.setPermission(String.format("%s.%s", pl.getSchema().getName(), "default").toLowerCase());
            }else {
                cmd.setPermission(String.format("%s.%s", pl.getSchema().getName(), cmd.getPermission()).toLowerCase());
            }
            this.getPluginCommands().add(cmd);
            log.info(String.format("[%s] Loading command %s", pl.getSchema().getName(), cmd.getNames()[0]));
        }
        for(Command c: getPluginCommands()) {
            String category = c.getAsCategory().toLowerCase().trim();
            if(!categoryList.contains(category)) {
                categoryList.add(category);
            }
        }
    }

    private void setOzzie(Ozzie ozzie){
        this.ozzie = ozzie;
    }

    private Ozzie getOzzie(){
        return ozzie;
    }
}