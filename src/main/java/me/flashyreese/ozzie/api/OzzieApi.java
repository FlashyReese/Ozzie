package me.flashyreese.ozzie.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.vdurmont.semver4j.Semver;
import me.flashyreese.common.util.JarUtil;
import me.flashyreese.ozzie.Ozzie;
import me.flashyreese.ozzie.api.command.guild.DiscordCommandManager;
import me.flashyreese.ozzie.api.database.DatabaseHandler;
import me.flashyreese.ozzie.api.l10n.L10nManager;
import me.flashyreese.ozzie.api.permission.PermissionDispatcher;
import me.flashyreese.ozzie.api.plugin.PluginLoader;
import me.flashyreese.ozzie.api.token.TokenManager;
import me.flashyreese.ozzie.api.util.ActivityHelper;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.stream.Collectors;

public class OzzieApi {
    public static OzzieApi INSTANCE;
    private final Logger logger = LogManager.getLogger(Ozzie.class);
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final Semver version;
    private final String botName;
    private final String defaultCommandPrefix;
    private boolean running;
    private final File directory;

    private ShardManager shardManager;
    private final DiscordCommandManager commandManager;
    private final TokenManager tokenManager;
    private final PluginLoader pluginLoader;
    private final PermissionDispatcher permissionDispatcher;
    private final DatabaseHandler databaseHandler;
    private final L10nManager l10nManager;
    private final EventWaiter eventWaiter;

    public OzzieApi(String[] arguments) throws IOException, URISyntaxException {
        OzzieApi.INSTANCE = this;
        this.version = new Semver(JsonParser.parseString(JarUtil.readTextFile("ozzie.plugin.json")).getAsJsonObject().get("version").getAsString());
        this.checkForUpdates();
        this.logger.info("Building instance...");
        this.botName = "Ozzie";
        this.defaultCommandPrefix = "-";
        this.directory = new File(System.getProperty("user.dir"));
        this.tokenManager = new TokenManager(this.gson, this.directory);
        this.commandManager = new DiscordCommandManager();
        this.pluginLoader = new PluginLoader(this.gson, new File(this.directory.getAbsolutePath() + File.separator + "plugins"), "ozzie.plugin");
        this.permissionDispatcher = new PermissionDispatcher();
        this.databaseHandler = new DatabaseHandler();
        this.l10nManager = new L10nManager(this.gson, this.directory, "assets/lang");
        this.eventWaiter = new EventWaiter();
        this.logger.info("Instance built!");
    }

    public synchronized void start() throws LoginException, URISyntaxException, IOException {
        if (!this.running) {
            this.logger.info("Starting instance...");
            if (shardManager == null) {
                DefaultShardManagerBuilder shardManagerBuilder;
                shardManagerBuilder = DefaultShardManagerBuilder.createDefault(this.tokenManager.getToken("discord"));
                shardManagerBuilder.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MESSAGE_TYPING, GatewayIntent.DIRECT_MESSAGE_TYPING);
                shardManagerBuilder.setAutoReconnect(true);
                this.shardManager = shardManagerBuilder.build();
                this.shardManager.addEventListener(this.commandManager);
                this.shardManager.addEventListener(this.eventWaiter);
                this.shardManager.setActivity(Activity.playing(ActivityHelper.getRandomQuote()));
                this.pluginLoader.registerPlugins();
            }
            this.running = true;
            this.logger.info("Instance started!");
        } else {
            this.logger.error("Instance already running!");
        }
    }

    public synchronized void stop() {
        if (this.running) {
            this.logger.info("Stopping instance...");
            this.pluginLoader.unregisterPlugins();
            this.pluginLoader.getPluginEntryContainers().clear();
            this.shardManager.shutdown();
            this.running = false;
            this.logger.info("Instance stopped!");
        } else {
            this.logger.warn("Instance not running!");
        }
    }

    public synchronized void restart() {
        if (this.running) {
            this.logger.info("Restarting instance...");
            this.pluginLoader.unregisterPlugins();
            this.pluginLoader.getPluginEntryContainers().clear();
            this.shardManager.restart();
            this.pluginLoader.registerPlugins();
            this.logger.info("Restarted instance...");
        } else {
            this.logger.warn("Instance not running!");
        }
    }

    /**
     * Checks for updates with a url provided from the repository.
     *
     * @throws IOException if no connection with Internet or GitHub down
     */
    public void checkForUpdates() throws IOException {
        this.logger.info("Checking for updates...");
        URL url = new URL("https://api.github.com/repos/FlashyReese/Ozzie/releases");
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String json = in.lines().collect(Collectors.joining());
        in.close();
        JsonArray jsonArray = JsonParser.parseString(json).getAsJsonArray();
        Semver latest = new Semver(jsonArray.get(0).getAsJsonObject().get("tag_name").getAsString(), Semver.SemverType.STRICT);
        if (this.version.isLowerThan(latest)) {
            this.logger.warn("*** Error, this build is outdated ***");
            this.logger.warn("*** Please download a new build from https://github.com/FlashyReese/Ozzie/releases ***");
        } else {
            this.logger.info("Up to date!");
        }
    }

    public Logger getLogger() {
        return logger;
    }

    public Gson getGson() {
        return gson;
    }

    public Semver getVersion() {
        return version;
    }

    public String getBotName() {
        return botName;
    }

    public String getDefaultCommandPrefix() {
        return defaultCommandPrefix;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public File getDirectory() {
        return directory;
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

    public DiscordCommandManager getCommandManager() {
        return commandManager;
    }

    public TokenManager getTokenManager() {
        return tokenManager;
    }

    public PluginLoader getPluginLoader() {
        return pluginLoader;
    }

    public PermissionDispatcher getPermissionDispatcher() {
        return permissionDispatcher;
    }

    public DatabaseHandler getDatabaseHandler() {
        return databaseHandler;
    }

    public L10nManager getL10nManager() {
        return l10nManager;
    }

    public EventWaiter getEventWaiter() {
        return eventWaiter;
    }
}
