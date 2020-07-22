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
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import com.vdurmont.semver4j.Semver;
import me.wilsonhu.ozzie.core.command.CommandManager;
import me.wilsonhu.ozzie.core.configuration.ConfigurationManager;
import me.wilsonhu.ozzie.core.i18n.I18nManager;
import me.wilsonhu.ozzie.core.plugin.Plugin;
import me.wilsonhu.ozzie.core.plugin.PluginLoader;
import me.wilsonhu.ozzie.core.plugin.PluginModule;
import me.wilsonhu.ozzie.core.runtimeoption.RuntimeOptionManager;
import me.wilsonhu.ozzie.core.token.TokenManager;
import me.wilsonhu.ozzie.handlers.PrimaryListener;
import me.wilsonhu.ozzie.runtimeoptions.DisablePlugins;
import me.wilsonhu.ozzie.utilities.ActivityHelper;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;

import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.stream.Collectors;

/**
 * Used to start new instances of {@link net.dv8tion.jda.api.JDA JDA}.
 *
 * <p> Default class for starting {@link me.wilsonhu.ozzie.Ozzie Ozzie} Core Modules and {@link net.dv8tion.jda.api.JDA JDA} instance.
 *
 * @author Yao Chung Hu
 *
 * @since  20.01.09
 */
public class Ozzie {

    private static final Logger log = LogManager.getLogger(Ozzie.class);

    private ShardManager shardManager;

    private String botName;
    private boolean running;
    private String defaultCommandPrefix;
    private String operatingSystemName;
    private File directory;

    private RuntimeOptionManager runtimeOptionManager;
    private TokenManager tokenManager;
    private ConfigurationManager configurationManager;
    private PluginLoader pluginLoader;
    private CommandManager commandManager;
    private I18nManager i18nManager;
    /*private RConServer RConServer;*/

    private EventWaiter eventWaiter;
    private static Ozzie ozzie;

    private final Semver CLIENT_VERSION = new Semver("0.5.1-SNAPSHOT+build-20201207",Semver.SemverType.STRICT);

    /**
     * Creates a Ozzie with the given parameters.
     * <br>This is the default constructor for Ozzie.
     *
     * @param args Ozzie parameters
     * @throws Exception If an error occur while performing a parameter method.
     */
    public Ozzie(String[] args) throws Exception {
        this.checkForUpdates();
        log.info("Building client...");
        this.setOperatingSystemName(System.getProperty("os.name").toLowerCase());
        this.setDirectory(new File(System.getProperty("user.dir")));
        //Todo: YAML Settings
        //this.getConfigurationManager().loadClientConfiguration();
        this.getRuntimeOptionManager().initRuntimeOptions(args, this);
        this.setBotName("Ozzie");
        this.setRunning(false);
        this.setDefaultCommandPrefix("-");
        this.setEventWaiter(new EventWaiter());
        log.info("Client built!");
    }

    /**
     * Starts the instance of Ozzie using the {@link net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder DefaultShardManagerBuilder} and it's core modules, also loading found plugins.
     * <br>This method can throw an exception on the first time running the application, caused by empty configurations.</br>
     * @throws LoginException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    public void start() throws LoginException, IOException, ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        if(isRunning()){
            log.warn("Client already running!");
        }else{
            log.info("Starting client...");
            this.setRunning(true);
            if(shardManager == null){
                log.info("Building ShardManager...");
                DefaultShardManagerBuilder shardManagerBuilder;
                if(getTokenManager().getToken("discord") == null || getTokenManager().getToken("discord").isEmpty() || !getTokenManager().containsKey("discord")){
                    log.error("Couldn't find discordapi token!");
                    BufferedReader bufferedReader;
                    bufferedReader = new BufferedReader(new InputStreamReader(System.in));
                    while (getTokenManager().getToken("discord") == null || getTokenManager().getToken("discord").isEmpty() || !getTokenManager().containsKey("discord")){
                        log.info("Please input a discord token: ");
                        String token = bufferedReader.readLine();
                        getTokenManager().addToken("discord", token.trim());
                        //Todo: Add precheck for getOzzie().getTokenManager().getToken("mongodb") dsn also add a param check
                    }
                    bufferedReader.close();
                }
                shardManagerBuilder = DefaultShardManagerBuilder.createDefault(getTokenManager().getToken("discord"));
                shardManagerBuilder.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MESSAGE_TYPING, GatewayIntent.DIRECT_MESSAGE_TYPING);
                shardManagerBuilder.setAutoReconnect(true);
                shardManagerBuilder.setAudioSendFactory(new NativeAudioSendFactory());
                shardManager = shardManagerBuilder.build();
                getShardManager().addEventListener(new PrimaryListener(this));
                getShardManager().addEventListener(getEventWaiter());
                getShardManager().setActivity(/*Activity.streaming("Just Chatting","https://twitch.tv/theflashyreese"));*/Activity.playing(ActivityHelper.getRandomQuote()));//Fixme: Add me to a ExecutionService
                if(((DisablePlugins) getRuntimeOptionManager().getRuntimeOption(DisablePlugins.class)).isPluginsDisabled()){
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

    /**
     * Loads plugins into core components.
     *
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    private void loadPlugins() throws InstantiationException, IllegalAccessException, IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException   {
        for(PluginModule pluginModule : getPluginLoader().getConfiguredPlugins()){
            Plugin currentPlugin = pluginModule.getPlugin();
            log.info(String.format("Enabling [%s]%s %s", pluginModule.getSchema().getId(), pluginModule.getSchema().getName(), pluginModule.getSchema().getVersion()));
            currentPlugin.onEnable(this);
            getShardManager().addEventListener(currentPlugin);
            getCommandManager().addCommands(pluginModule);
        }
    }

    /**
     * Unloads plugins from core components.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private void unloadPlugins() throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        for (PluginModule pluginModule : getPluginLoader().getConfiguredPlugins()) {
            Plugin currentPlugin = pluginModule.getPlugin();
            log.info(String.format("Disabling [%s]%s %s", pluginModule.getSchema().getId(), pluginModule.getSchema().getName(), pluginModule.getSchema().getVersion()));
            currentPlugin.onDisable(this);
            getShardManager().removeEventListener(currentPlugin);
        }
    }

    /**
     * Reloads plugins this method is only used in {@link me.wilsonhu.ozzie.commands.Reload Reload}.
     *
     * <br>Useful for loading new plugins without restarting Ozzie</br>.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    public void pluginsReload() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        log.warn("Reloading Plugins!");
        unloadPlugins();
        getCommandManager().getPluginCommands().clear();
        pluginLoader = new PluginLoader(getDirectory().getAbsolutePath(), this);
        loadPlugins();
        log.info("Plugins reloaded!");
    }

    /**
     * Shuts down Ozzie calling {@link Ozzie#unloadPlugins() unloadPlugins()} and {@link ShardManager#shutdown()}.
     *
     * <br>This method is used on {@link me.wilsonhu.ozzie.commands.Shutdown Shutdown} and the shutdown hook.</br>
     *
     * @throws NoSuchMethodException
     * @throws IOException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws ClassNotFoundException
     */
    public void stop() throws NoSuchMethodException, IOException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        if(isRunning()){
            log.info("Stopping client...");
            unloadPlugins();
            this.getShardManager().shutdown();
            this.setRunning(false);
            shardManager = null;
            log.info("Client stopped!");
        }else{
            log.warn("Client not running!");
        }
    }

    /**
     * Soft restarts Ozzie calling on {@link Ozzie#stop() stop()} and {@link Ozzie#start()}.
     *
     * <br>This does not create a new instance of Ozzie.</br>
     *
     * @throws LoginException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IOException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    public void restart() throws LoginException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, NoSuchMethodException, InvocationTargetException {
        if (isRunning()){
            log.info("Restarting client...");
            this.stop();
            this.setEventWaiter(new EventWaiter());
            this.start();
            log.info("Client restarted!");
        }else{
            log.warn("Client not running!");
        }
    }

    /**
     * Checks for updates with a url provided from the repository.
     *
     * @throws IOException
     */
    public void checkForUpdates() throws IOException{
        log.info("Checking for updates...");
        URL url = new URL("https://api.github.com/repos/FlashyReese/Ozzie/releases");
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String json = in.lines().collect(Collectors.joining());
        in.close();
        JSONArray jsonArray = new JSONArray(json);
        Semver latest = new Semver(jsonArray.getJSONObject(0).getString("tag_name"), Semver.SemverType.STRICT);
        if(getClientVersion().isLowerThan(latest)){
            log.warn("*** Error, this build is outdated ***");
            log.warn("*** Please download a new build from https://github.com/FlashyReese/Ozzie/releases ***");
        }else{
            log.info("Up to date!");
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


    public RuntimeOptionManager getRuntimeOptionManager() {
        if(runtimeOptionManager == null) runtimeOptionManager = new RuntimeOptionManager();
        return runtimeOptionManager;
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

    public Semver getClientVersion(){
        return CLIENT_VERSION;
    }
}
