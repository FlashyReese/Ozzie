package me.wilsonhu.ozzie;

import me.wilsonhu.ozzie.core.command.CommandManager;
import me.wilsonhu.ozzie.core.configuration.ConfigurationManager;
import me.wilsonhu.ozzie.core.i18n.I18nManager;
import me.wilsonhu.ozzie.core.parameter.ParameterManager;
import me.wilsonhu.ozzie.core.plugin.Plugin;
import me.wilsonhu.ozzie.core.plugin.PluginLoader;
import me.wilsonhu.ozzie.core.plugin.PluginModule;
import me.wilsonhu.ozzie.core.token.TokenManager;
import me.wilsonhu.ozzie.handlers.PrimaryListener;
import me.wilsonhu.ozzie.parameters.DisablePlugins;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class Ozzie {

    private static final Logger log = LogManager.getLogger(Ozzie.class);

    private ShardManager shardManager;

    private String botName;
    private boolean running;
    private String defaultCommandPrefix;
    private String operatingSystemName;
    private File directory;

    private ParameterManager parameterManager;
    private TokenManager tokenManager;
    private ConfigurationManager configurationManager;
    private PluginLoader pluginLoader;
    private CommandManager commandManager;
    private I18nManager i18nManager;

    public Ozzie(String[] args) throws Exception {
        log.info("Building client...");
        this.setOperatingSystemName(System.getProperty("os.name").toLowerCase());
        this.setDirectory(new File(System.getProperty("user.dir")));
        this.getParameterManager().runParameters(args, this);
        this.setBotName("Ozzie");
        this.setRunning(false);
        this.setDefaultCommandPrefix("-");
        log.info("Client built!");
    }

    public void start() throws LoginException, IOException, ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        if(isRunning()){
            log.warn("Client already running!");
        }else{
            log.info("Starting client...");
            this.setRunning(true);
            if(shardManager == null){
                log.info("Building ShardManager...");
                DefaultShardManagerBuilder shardManagerBuilder = new DefaultShardManagerBuilder();
                shardManagerBuilder.setAutoReconnect(true);
                if(getTokenManager().getToken("discord").isEmpty()){//broken
                    log.error("Couldn't find discordapi token!");
                    return;
                }
                shardManagerBuilder.setToken(getTokenManager().getToken("discord"));
                shardManager = shardManagerBuilder.build();
                getShardManager().addEventListener(new PrimaryListener(this));
                if(((DisablePlugins)getParameterManager().getParameter(DisablePlugins.class)).isPluginsDisabled()){
                    log.info("Plugins are disabled!");
                }else{
                    loadPlugins();
                }
                log.info("ShardManager built!");
            }
            log.info("Client started!");
        }
    }

    private void loadPlugins() throws InstantiationException, IllegalAccessException, IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException   {
        for(PluginModule pluginModule : getPluginLoader().getConfiguredPlugins()){
            Plugin currentPlugin = pluginModule.getPlugin();
            log.info(String.format("Enabling [%s]%s %s", pluginModule.getSchema().getId(), pluginModule.getSchema().getName(), pluginModule.getSchema().getVersion()));
            currentPlugin.onEnable(this);
            getShardManager().addEventListener(currentPlugin);
            getCommandManager().addCommands(pluginModule);
        }
    }

    public void pluginsReload() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        log.warn("Reloading Plugins!");
        for (PluginModule pluginModule : getPluginLoader().getConfiguredPlugins()) {
            Plugin currentPlugin = pluginModule.getPlugin();
            log.info(String.format("Disabling [%s]%s %s", pluginModule.getSchema().getId(), pluginModule.getSchema().getName(), pluginModule.getSchema().getVersion()));
            currentPlugin.onDisable(this);
            getShardManager().removeEventListener(currentPlugin);
        }
        getCommandManager().getPluginCommands().clear();
        pluginLoader = new PluginLoader(getDirectory().getAbsolutePath(), this);
        loadPlugins();
        log.info("Plugins reloaded!");
    }

    public void stop(){
        if(isRunning()){
            log.info("Stopping client...");
            this.setRunning(false);
            this.getShardManager().shutdown();
            shardManager = null;
            log.info("Client stopped!");
        }else{
            log.warn("Client not running!");
        }
    }

    public void restart() throws LoginException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, NoSuchMethodException, InvocationTargetException {
        if (isRunning()){
            log.info("Restarting client...");
            this.stop();
            this.start();
            log.info("Client restarted!");
        }else{
            log.warn("Client not running!");
        }
    }

    public ShardManager getShardManager(){
        return shardManager;
    }

    public String getBotName() {
        return botName;
    }

    public void setBotName(String botName) {
        this.botName = botName;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public String getDefaultCommandPrefix() {
        return defaultCommandPrefix;
    }

    public void setDefaultCommandPrefix(String defaultCommandPrefix) {
        this.defaultCommandPrefix = defaultCommandPrefix;
    }

    public String getOperatingSystemName() {
        return operatingSystemName;
    }

    public void setOperatingSystemName(String operatingSystemName) {
        this.operatingSystemName = operatingSystemName;
    }

    public File getDirectory() {
        return directory;
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }

    public ParameterManager getParameterManager() {
        if(parameterManager == null) parameterManager = new ParameterManager();
        return parameterManager;
    }

    public TokenManager getTokenManager() {
        if(tokenManager == null) tokenManager = new TokenManager(this);
        return tokenManager;
    }

    public ConfigurationManager getConfigurationManager() {
        if(configurationManager == null) configurationManager = new ConfigurationManager(this);
        return configurationManager;
    }

    public PluginLoader getPluginLoader() throws IOException, ClassNotFoundException {
        if(pluginLoader == null) pluginLoader = new PluginLoader(getDirectory().getAbsolutePath(), this);
        return pluginLoader;
    }

    public CommandManager getCommandManager(){
        if(commandManager == null) commandManager = new CommandManager(this);
        return commandManager;
    }

    public I18nManager getI18nManager(){
        if (i18nManager == null) i18nManager = new I18nManager(this);
        return i18nManager;
    }
}
