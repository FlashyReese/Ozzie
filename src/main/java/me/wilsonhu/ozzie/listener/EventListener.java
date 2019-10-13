package me.wilsonhu.ozzie.listener;

import java.util.ArrayList;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.OzzieManager;
import me.wilsonhu.ozzie.manager.json.configuration.ServerSettings;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class EventListener extends ListenerAdapter {

	private OzzieManager manager;
	private Ozzie ozzie;
	
	public EventListener(OzzieManager manager, Ozzie ozzie) {
		setOzzieManager(manager);
		setOzzie(ozzie);
	}
	
	@Override
    public void onReady(ReadyEvent e){
		getOzzieManager().getJsonManager().readServerSettingsList();
		if(getOzzieManager().getServerSettingsManager().getServerSettingsList().isEmpty()) {
			for(Guild guild: getOzzie().getJDA().getGuilds()) {
				getOzzieManager().getServerSettingsManager().getServerSettingsList().put(guild.getIdLong(), new ServerSettings(guild, getOzzieManager()));
			}
			getOzzieManager().getJsonManager().writeServerSettingsList();
		}
		
		getOzzieManager().getJsonManager().readUserPermissionList();
		if(getOzzieManager().getPermissionManager().getUserPermissionList().isEmpty()) {
			for(Guild guild: getOzzie().getJDA().getGuilds()) {
				for(Member m : guild.getMembers()) {
					if(!getOzzieManager().getPermissionManager().getUserPermissionList().containsKey(m.getIdLong())) {
						ArrayList<String> defaultPerms = new ArrayList<String>();
						defaultPerms.add("ozzie.default");
						getOzzieManager().getPermissionManager().getUserPermissionList().put(m.getIdLong(), defaultPerms);
					}
				}
			}
			getOzzieManager().getJsonManager().writeUserPermissionList();
		}
	}
	
	@Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
		if (event.isFromType(ChannelType.PRIVATE))
        {
			System.out.println(String.format("[PM] %s: %s", event.getAuthor().getName(),
                    event.getMessage().getContentRaw()));
			getOzzieManager().getCommandManager().onCommand(event, null, this.getOzzie());
        }
        else
        {
            System.out.println(String.format("[%s][%s] %s: %s", event.getGuild().getName(),
                        event.getTextChannel().getName(), event.getMember().getEffectiveName(),
                        event.getMessage().getContentRaw()));
            getOzzieManager().getCommandManager().onCommand(event, null, this.getOzzie());
        }
		me.wilsonhu.ozzie.utilities.Activity act = new me.wilsonhu.ozzie.utilities.Activity(); 
		getOzzie().getJDA().getPresence().setActivity(Activity.playing(act.getRandomQuote()));
    }

	private OzzieManager getOzzieManager() {
		return manager;
	}

	private void setOzzieManager(OzzieManager manager) {
		this.manager = manager;
	}

	public Ozzie getOzzie() {
		return ozzie;
	}

	public void setOzzie(Ozzie ozzie) {
		this.ozzie = ozzie;
	}
}
