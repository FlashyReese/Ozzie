package me.wilsonhu.ozzie;

import me.wilsonhu.ozzie.core.configuration.ConfigurationManager;
import me.wilsonhu.ozzie.core.parameter.ParameterManager;
import me.wilsonhu.ozzie.core.token.TokenManager;
import me.wilsonhu.ozzie.handlers.PrimaryListener;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.security.auth.login.LoginException;
import java.io.File;

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

    public void start() throws LoginException {
        if(isRunning()){
            log.warn("Client already running!");
        }else{
            log.info("Starting client...");
            this.setRunning(true);
            if(shardManager == null){
                log.info("Building ShardManager...");
                DefaultShardManagerBuilder shardManagerBuilder = new DefaultShardManagerBuilder();
                shardManagerBuilder.setAutoReconnect(true);
                shardManagerBuilder.setToken(getTokenManager().getToken("discord"));
                shardManager = shardManagerBuilder.build();
                shardManager.addEventListener(new PrimaryListener());
                log.info("ShardManager built!");
            }
            log.info("Client started!");
        }
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

    public void restart() throws LoginException {
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
        if(configurationManager == null) configurationManager = new ConfigurationManager();
        return configurationManager;
    }
}
