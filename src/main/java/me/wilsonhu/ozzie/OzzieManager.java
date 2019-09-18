package me.wilsonhu.ozzie;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.darkmagician6.eventapi.EventManager;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import me.wilsonhu.ozzie.core.events.EventOzzieManager;
import me.wilsonhu.ozzie.core.params.DisablePlugins;
import me.wilsonhu.ozzie.core.params.ShardSize;
import me.wilsonhu.ozzie.manager.command.CommandManager;
import me.wilsonhu.ozzie.manager.json.JsonManager;
import me.wilsonhu.ozzie.manager.json.configuration.ServerSettingsManager;
import me.wilsonhu.ozzie.manager.parameter.ParameterManager;
import me.wilsonhu.ozzie.manager.plugin.Plugin;
import me.wilsonhu.ozzie.manager.plugin.PluginLoader;
import me.wilsonhu.ozzie.manager.shard.OzzieShardManager;
import me.wilsonhu.ozzie.manager.token.TokenManager;
import me.wilsonhu.ozzie.utilities.Logger;

public class OzzieManager {
	
	private File directory;
	private String botName;
	private String defaultBotPrefix;
	private String botToken;
	private String OS;
	
	private Logger logger;
	private ParameterManager parameterManager;
	private TokenManager tokenManager;
	private PluginLoader pluginLoader;
	private JsonManager jsonManager;
	private ServerSettingsManager serverSettingsManager;
	private CommandManager commandManager;
	private OzzieShardManager shardManager;
	
	
	private EventWaiter eventWaiter = new EventWaiter();

	
	private ArrayList<Plugin> loadedPluginList = new ArrayList<Plugin>();
	
	public OzzieManager(String[] args){
		this.setDefaultBotPrefix("-");
		this.setOS(System.getProperty("os.name").toLowerCase());
		this.setBotName("Ozzie");
		this.setDirectory(new File(System.getProperty("user.dir")));
		loadManagers(args);
	}
	
	public void loadManagers(String[] args) {
		this.setLogger(new Logger(this));
		this.setTokenManager(new TokenManager(this));
		this.setParameterManager(new ParameterManager(this, args));
		this.setServerSettingsManager(new ServerSettingsManager(this));
		this.setJsonManager(new JsonManager(this));
		this.setCommandManager(new CommandManager(this));
		try {
			this.setPluginLoader(new PluginLoader(this, "plugins", "config"));
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		init();
	}
	
	public void init() {
		if(!getTokenManager().getTokenList().containsKey("jda")) {
			getJsonManager().readTokenList();
			if(!getTokenManager().getTokenList().containsKey("jda")) {
				//Couldn't find saved? Manual entry with console
				return;
			}else {
				getLogger().info("Successfully set Discord API Token via tokenlist.json");
			}
		}
		setBotToken(getTokenManager().getTokenList().get("jda"));
		getJsonManager().writeTokenList();
		try {
			if(!((DisablePlugins) this.getParameterManager().getParam(DisablePlugins.class)).isPluginless()) {
				for (Plugin plugin: getPluginLoader().getConfiguredPlugins()) {
					this.getLoadedPluginList().add(plugin);
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		EventManager.call(new EventOzzieManager(this));
		if(((ShardSize) getParameterManager().getParam(ShardSize.class)).isShardless()) {
			new Ozzie(this).start();
		}else {
			this.setShardManager(new OzzieShardManager(this, ((ShardSize) getParameterManager().getParam(ShardSize.class)).getShardTotal()));
			this.getShardManager().startAllShards();
		}
		
	}

	public File getDirectory() {
		return directory;
	}

	public void setDirectory(File directory) {
		this.directory = directory;
	}

	public String getBotName() {
		return botName;
	}

	public void setBotName(String botName) {
		this.botName = botName;
	}

	public String getDefaultBotPrefix() {
		return defaultBotPrefix;
	}

	public void setDefaultBotPrefix(String defaultBotPrefix) {
		this.defaultBotPrefix = defaultBotPrefix;
	}

	public String getBotToken() {
		return botToken;
	}

	public void setBotToken(String botToken) {
		this.botToken = botToken;
	}

	public String getOS() {
		return OS;
	}

	public void setOS(String oS) {
		OS = oS;
	}
	
	public EventWaiter getEventWaiter() {
		return eventWaiter;
	}


	public Logger getLogger() {
		return logger;
	}


	public void setLogger(Logger logger) {
		this.logger = logger;
	}


	public ParameterManager getParameterManager() {
		return parameterManager;
	}


	public void setParameterManager(ParameterManager parameterManager) {
		this.parameterManager = parameterManager;
	}


	public TokenManager getTokenManager() {
		return tokenManager;
	}


	public void setTokenManager(TokenManager tokenManager) {
		this.tokenManager = tokenManager;
	}


	public PluginLoader getPluginLoader() {
		return pluginLoader;
	}


	public void setPluginLoader(PluginLoader pluginLoader) {
		this.pluginLoader = pluginLoader;
	}


	public JsonManager getJsonManager() {
		return jsonManager;
	}


	public void setJsonManager(JsonManager jsonManager) {
		this.jsonManager = jsonManager;
	}


	public ServerSettingsManager getServerSettingsManager() {
		return serverSettingsManager;
	}


	public void setServerSettingsManager(ServerSettingsManager serverSettingsManager) {
		this.serverSettingsManager = serverSettingsManager;
	}


	public CommandManager getCommandManager() {
		return commandManager;
	}


	public void setCommandManager(CommandManager commandManager) {
		this.commandManager = commandManager;
	}


	public OzzieShardManager getShardManager() {
		return shardManager;
	}

	public ArrayList<Plugin> getLoadedPluginList() {
		return loadedPluginList;
	}

	public void setShardManager(OzzieShardManager shardManager) {
		this.shardManager = shardManager;
	}
	
}
