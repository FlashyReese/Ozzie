package me.wilsonhu.ozzie.manager.command;

import java.util.ArrayList;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.OzzieManager;
import me.wilsonhu.ozzie.core.commands.*;
import me.wilsonhu.ozzie.manager.json.configuration.ServerSettings;
import me.wilsonhu.ozzie.manager.plugin.Plugin;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandManager
{
	
	private ArrayList<Command> commands;
	private OzzieManager manager;
	
	public CommandManager(OzzieManager manager)
	{
		this.setOzzieManager(manager);
		commands = new ArrayList<Command>();
		for (Command c: commands())
		{
			commands.add(c);
		}
	}
	
	private Command[] commands(){
		return new Command[]{
				new About(),
				new Eval(),
				new Help(),
				new Ping(),
				new Plugins(),
				new User(),
				new Reload(),
				new Restart(),
				new InstallPlugin(),
				new BotPrefix(),
				new Channel(),
				new ManagePlugins(),
				new Clara(),
				new Token()
		};
	}
	
	public void onCommand(MessageReceivedEvent event, String full, Ozzie ozzie){
		if(event.getAuthor().isBot() || event.getMessage().getAuthor().getId() == event.getJDA().getSelfUser().getId() || event.getMessage().getContentRaw() == null) {
			return;
		}
		if(full == null) {
			full = event.getMessage().getContentRaw();
		}
		full = full.trim();
		if(event.isFromType(ChannelType.PRIVATE)) {
			try {
				if (full == getOzzieManager().getDefaultBotPrefix()){
					return;
				}
				full = full.substring(getOzzieManager().getDefaultBotPrefix().length());
				String[] s;
				if(full.contains(" ")) {
					s = full.split(" ");
				}else {
					s = new String[] {full};
				}
				for(Command cmd: getCommands()) {
					for(String name: cmd.getNames()) {
						if(name.equalsIgnoreCase(s[0])) {
							String args = full.substring(name.length()).trim();
							if(!cmd.isGuildOnly()) {
								try
								{
									cmd.onCommand(full, args, event, ozzie);
								}
								catch (Exception e)
								{
									event.getChannel().sendMessage(cmd.getHelpEmblem()).queue();
									e.printStackTrace();
								}
								return;
							}
						}
					}
				}
			}catch(Exception e) {
				e.printStackTrace();
				event.getChannel().sendMessage("Sorry I think I'm broken").queue();
			}
			return;
		}else if(event.isFromGuild()) {
			try{
				Guild guild = event.getGuild();
				if(!getOzzieManager().getServerSettingsManager().getServerSettingsList().containsKey(guild.getIdLong())) {
					getOzzieManager().getServerSettingsManager().getServerSettingsList().put(guild.getIdLong(), new ServerSettings(guild, getOzzieManager()));
				}
				ServerSettings ss = getOzzieManager().getServerSettingsManager().getServerSettingsList().get(guild.getIdLong());
				if (full == ss.getCustomBotPrefix()){
					return;
				}
				if(event.getMessage().getContentRaw().startsWith(ss.getCustomBotPrefix())){
					full = full.substring(ss.getCustomBotPrefix().length());
				}
				String[] s;
				if (full.contains(" ")) {
					s = full.split(" ");
				}else{
					s = new String[]{full};
				}
				if(event.getMessage().getContentRaw().startsWith(ss.getCustomBotPrefix()) && ss.getAllowedCommandTextChannels().contains(event.getChannel().getIdLong())){
					if(ss.isWhiteListMode()) {
						if(ss.getWhitelistedUsers().contains(event.getAuthor().getIdLong())) {
							for (Command c: getCommands()){
								for (String name: c.getNames()){
									if (name.equalsIgnoreCase(s[0])){
										String args = full.substring(name.length()).trim();
											try{
												c.onCommand(full, args, event, ozzie);
											}catch (Exception e){
												event.getChannel().sendMessage(c.getHelpEmblem()).queue();
												e.printStackTrace();
											}
											return;
									}
								}
								
							}
							return;
						}
						event.getChannel().sendMessage("Not Whitelisted").queue();
					}else if(!ss.isWhiteListMode() && !ss.getBlacklistedUsers().contains(event.getAuthor().getIdLong())) {
						for (Command c: getCommands()){
							for (String name: c.getNames()){
								if (name.equalsIgnoreCase(s[0])){
									String args = full.substring(name.length()).trim();
										try{
											c.onCommand(full, args, event, ozzie);
										}catch (Exception e){
											event.getChannel().sendMessage(c.getHelpEmblem()).queue();
											e.printStackTrace();
										}
										return;
								}
							}
							
						}
						return;
					}else {
						event.getChannel().sendMessage("Blacklisted").queue();
					}
	            }
				
				
			}catch (Exception e){
				e.printStackTrace();
				event.getChannel().sendMessage("Sorry I think I'm broken").queue();
			}
		}
		
	}
	
	public ArrayList<Command> getCommands()
	{
		return commands;
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
	
	public boolean performUserCheck(Command c, MessageReceivedEvent event){
		if(event.isFromType(ChannelType.PRIVATE)){
			return true;
		}
		return (((c.getLevel() == CommandLevel.OWNER || c.getLevel() == CommandLevel.ADMINISTRATOR || c.getLevel() == CommandLevel.DEFAULT) && event.getGuild().getOwner().getUser().getIdLong() == event.getAuthor().getIdLong()) || ((c.getLevel() == CommandLevel.DEVELOPER || c.getLevel() == CommandLevel.OWNER || c.getLevel() == CommandLevel.ADMINISTRATOR || c.getLevel() == CommandLevel.DEFAULT)) || ((c.getLevel() == CommandLevel.ADMINISTRATOR || c.getLevel() == CommandLevel.DEFAULT) && event.getGuild().getMemberById(event.getAuthor().getIdLong()).hasPermission(Permission.ADMINISTRATOR)) || (c.getLevel() == CommandLevel.DEFAULT));
	}

	public void addCommands(Plugin pl) {
		for(Command cmd: pl.getCommands()) {
			this.getCommands().add(cmd);
			this.getOzzieManager().getLogger().info(String.format("[%s] Loading command %s", pl.getName(), cmd.getNames()[0]));
		}
	}
	
	public OzzieManager getOzzieManager() {
		return manager;
	}

	public void setOzzieManager(OzzieManager manager) {
		this.manager = manager;
	}
	
}