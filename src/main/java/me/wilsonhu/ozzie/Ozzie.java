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
package me.wilsonhu.ozzie;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
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
import me.wilsonhu.ozzie.utilities.ActivityHelper;
import net.dv8tion.jda.api.entities.Activity;
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
    /*private RConServer RConServer;*/

    private EventWaiter eventWaiter;

    private static Ozzie ozzie;

    public Ozzie(String[] args) throws Exception {
        log.info("Building client...");
        this.setOperatingSystemName(System.getProperty("os.name").toLowerCase());
        this.setDirectory(new File(System.getProperty("user.dir")));
        this.getParameterManager().runParameters(args, this);
        this.setBotName("Ozzie");
        this.setRunning(false);
        this.setDefaultCommandPrefix("-");
        this.setEventWaiter(new EventWaiter());
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
                if(getTokenManager().getToken("discord").isEmpty() || !getTokenManager().containsKey("discord")){
                    log.error("Couldn't find discordapi token!");
                    //Todo: Probably bad idea to read it, but sure why not
                    return;
                }
                shardManagerBuilder.setToken(getTokenManager().getToken("discord"));
                shardManager = shardManagerBuilder.build();
                getShardManager().addEventListener(new PrimaryListener(this));
                getShardManager().addEventListener(getEventWaiter());
                getShardManager().setActivity(Activity.playing(ActivityHelper.getRandomQuote()));//Fixme: Add me to a ExecutionService
                if(((DisablePlugins)getParameterManager().getParameter(DisablePlugins.class)).isPluginsDisabled()){
                    log.info("Plugins are disabled!");//Todo: Hmmm Implement a way to disable individually not using params tho
                }else{
                    loadPlugins();
                }
                log.info("ShardManager built!");
            }
            setOzzie(this);
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

    public EventWaiter getEventWaiter() {
        return eventWaiter;
    }

    public void setEventWaiter(EventWaiter eventWaiter) {
        this.eventWaiter = eventWaiter;
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

    public static Ozzie getOzzie(){
        return ozzie;
    }

    private static void setOzzie(Ozzie ozzie){
        Ozzie.ozzie = ozzie;
    }

    /*public RConServer getRConServer(){
        if (RConServer == null) RConServer = new RConServer(this, 27015);
        return RConServer;
    }*/
}
