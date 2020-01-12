package me.wilsonhu.ozzie.core.token;

import com.google.gson.reflect.TypeToken;
import me.wilsonhu.ozzie.Ozzie;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.HashMap;

public class TokenManager {
    private static final Logger log = LogManager.getLogger(TokenManager.class);

    private HashMap<String, String> tokens = new HashMap<String, String>();

    public TokenManager(Ozzie ozzie) {
        log.info("Building Token Manager...");
        loadSavedTokens(ozzie);
        log.info("Token Manager built!");
    }

    private void loadSavedTokens(Ozzie ozzie){
        if(!new File("tokens.json").exists()){
            return;
        }
        log.info("Loading saved tokens...");
        this.setTokens(ozzie.getConfigurationManager().readJson(ozzie.getDirectory().getAbsolutePath(), "tokens", new TypeToken<HashMap<String, String>>(){}.getType()));
        log.info("Loaded saved tokens!");
    }

    public String getToken(String key){
        return this.getTokens().get(key);
    }

    public void addToken(String key, String token, Ozzie ozzie){
        this.getTokens().put(key, token);
        log.info("Saving tokens...");
        ozzie.getConfigurationManager().writeJson(ozzie.getDirectory().getAbsolutePath(), "tokens", this.getTokens());
        log.info("Saved tokens!");
    }

    public void removeToken(String key, Ozzie ozzie){
        this.getTokens().remove(key);
        log.info("Saving tokens...");
        ozzie.getConfigurationManager().writeJson(ozzie.getDirectory().getAbsolutePath(), "tokens", this.getTokens());
        log.info("Saved tokens!");
    }

    private HashMap<String, String> getTokens() {
        return tokens;
    }

    private void setTokens(HashMap<String, String> tokens) {
        this.tokens = tokens;
    }
}
