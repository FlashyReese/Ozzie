package me.wilsonhu.ozzie.manager.json.configuration;

import java.util.ArrayList;

import me.wilsonhu.ozzie.OzzieManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

public class ServerSettings {
	
	private boolean whiteListMode;
	private String customBotPrefix;
	private long botLoggerChannel;
	private ArrayList<Long> allowedCommandTextChannels;
	private ArrayList<Long> blacklistedUsers;
	private ArrayList<Long> whitelistedUsers;
	
	public ServerSettings(Guild guild, OzzieManager manager) {
		setWhiteListMode(false);
		setCustomBotPrefix(manager.getDefaultBotPrefix());
		setBotLoggerChannel(0);
		setAllowedCommandTextChannels(new ArrayList<Long>());
		for(TextChannel tc: guild.getTextChannels()) {
			getAllowedCommandTextChannels().add(tc.getIdLong());
		}
		setBlacklistedUsers(new ArrayList<Long>());
		setWhitelistedUsers(new ArrayList<Long>());
	}
	
	public boolean isWhiteListMode() {
		return whiteListMode;
	}
	public void setWhiteListMode(boolean whiteListMode) {
		this.whiteListMode = whiteListMode;
	}
	public String getCustomBotPrefix() {
		return customBotPrefix;
	}
	public void setCustomBotPrefix(String customBotPrefix) {
		this.customBotPrefix = customBotPrefix;
	}
	public long getBotLoggerChannel() {
		return botLoggerChannel;
	}
	public void setBotLoggerChannel(long botLoggerChannel) {
		this.botLoggerChannel = botLoggerChannel;
	}
	public ArrayList<Long> getAllowedCommandTextChannels() {
		return allowedCommandTextChannels;
	}
	public void setAllowedCommandTextChannels(ArrayList<Long> allowedCommandTextChannels) {
		this.allowedCommandTextChannels = allowedCommandTextChannels;
	}
	public ArrayList<Long> getBlacklistedUsers() {
		return blacklistedUsers;
	}
	public void setBlacklistedUsers(ArrayList<Long> blacklistedUsers) {
		this.blacklistedUsers = blacklistedUsers;
	}
	public ArrayList<Long> getWhitelistedUsers() {
		return whitelistedUsers;
	}
	public void setWhitelistedUsers(ArrayList<Long> whitelistedUsers) {
		this.whitelistedUsers = whitelistedUsers;
	}
	
	
}
