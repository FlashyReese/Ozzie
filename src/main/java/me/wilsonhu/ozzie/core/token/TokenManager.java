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
    private Ozzie ozzie;

    public TokenManager(Ozzie ozzie) {
        log.info("Building Token Manager...");
        setOzzie(ozzie);
        loadSavedTokens();
        log.info("Token Manager built!");
    }

    public void loadSavedTokens(){
        if(!new File("tokens.json").exists()){
            return;
        }
        log.info("Loading saved tokens...");
        this.setTokens(getOzzie().getConfigurationManager().readJson(getOzzie().getDirectory().getAbsolutePath(), "tokens", new TypeToken<HashMap<String, String>>(){}.getType()));
        log.info("Loaded saved tokens!");
    }

    public String getToken(String key){
        return this.getTokens().get(key);
    }

    public boolean containsKey(String key){
        return getTokens().containsKey(key);
    }

    private Ozzie getOzzie(){return this.ozzie;}

    public void addToken(String key, String token){
        this.getTokens().put(key, token);
        log.info("Saving tokens...");
        getOzzie().getConfigurationManager().writeJson(getOzzie().getDirectory().getAbsolutePath(), "tokens", this.getTokens());
        log.info("Saved tokens!");
    }

    public void removeToken(String key){
        this.getTokens().remove(key);
        log.info("Saving tokens...");
        getOzzie().getConfigurationManager().writeJson(getOzzie().getDirectory().getAbsolutePath(), "tokens", this.getTokens());
        log.info("Saved tokens!");
    }

    private HashMap<String, String> getTokens() {
        return tokens;
    }

    private void setTokens(HashMap<String, String> tokens) {
        this.tokens = tokens;
    }

    private void setOzzie(Ozzie ozzie){
        this.ozzie = ozzie;
    }
}
