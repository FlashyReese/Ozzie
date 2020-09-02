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
import me.wilsonhu.ozzie.core.AbstractManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class TokenManager extends AbstractManager {
    private Map<String, String> tokens = new HashMap<>();

    public TokenManager(Ozzie ozzie) {
        super(ozzie);
        this.info("Building Token Manager...");
        this.loadSavedTokens();
        this.info("Token Manager built!");
    }

    public void loadSavedTokens() {
        if (!new File("tokens.json").exists()) {
            return;
        }
        this.info("Loading saved tokens...");
        this.setTokens(getOzzie().getConfigurationManager().readJson(getOzzie().getDirectory().getAbsolutePath(), "tokens", new TypeToken<HashMap<String, String>>() {
        }.getType()));
        this.info("Loaded saved tokens!");
    }

    public String getToken(String key) {
        return this.getTokens().get(key);
    }

    public boolean containsKey(String key) {
        return this.getTokens().containsKey(key);
    }

    public void addToken(String key, String token) {
        this.getTokens().put(key, token);
        this.info("Saving tokens...");
        this.getOzzie().getConfigurationManager().writeJson(getOzzie().getDirectory().getAbsolutePath(), "tokens", this.getTokens());
        this.info("Saved tokens!");
    }

    public void removeToken(String key) {
        this.getTokens().remove(key);
        this.info("Saving tokens...");
        this.getOzzie().getConfigurationManager().writeJson(getOzzie().getDirectory().getAbsolutePath(), "tokens", this.getTokens());
        this.info("Saved tokens!");
    }

    private Map<String, String> getTokens() {
        return this.tokens;
    }

    private void setTokens(Map<String, String> tokens) {
        this.tokens = tokens;
    }

    @Override
    public @NotNull String getName() {
        return "Token Manager";
    }
}
