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
package me.flashyreese.ozzie.api.token;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.flashyreese.ozzie.api.OzzieApi;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class TokenManager {
    private final Gson gson;
    private final File file;
    private Map<String, String> tokens = new Object2ObjectOpenHashMap<>();

    public TokenManager(Gson gson, File directory) {
        this.gson = gson;
        this.file = new File(directory.getAbsolutePath() + File.separator + "tokens.json");
        this.loadSavedTokens();
    }

    public void loadSavedTokens() {
        OzzieApi.INSTANCE.getLogger().info("Loading saved tokens...");
        this.tokens = this.load();
        OzzieApi.INSTANCE.getLogger().info("Loaded saved tokens!");
    }

    public String getToken(String key) {
        return this.getTokens().get(key);
    }

    public boolean containsKey(String key) {
        return this.getTokens().containsKey(key);
    }

    public void addToken(String key, String token) {
        this.getTokens().put(key, token);
        this.writeChanges();
    }

    public void removeToken(String key) {
        this.getTokens().remove(key);
        this.writeChanges();
    }

    public Map<String, String> load() {
        Map<String, String> config;

        if (this.file.exists()) {
            try (FileReader reader = new FileReader(this.file)) {
                config = this.gson.fromJson(reader, new TypeToken<Object2ObjectOpenHashMap<String, String>>() {
                }.getType());
            } catch (IOException e) {
                throw new RuntimeException("Could not parse tokens", e);
            }
        } else {
            config = new Object2ObjectOpenHashMap<>();
            this.writeChanges();
        }

        return config;
    }

    public void writeChanges() {
        OzzieApi.INSTANCE.getLogger().info("Saving tokens...");
        try (FileWriter writer = new FileWriter(this.file)) {
            this.gson.toJson(this.tokens, writer);
        } catch (IOException e) {
            throw new RuntimeException("Could not save tokens file", e);
        }
        OzzieApi.INSTANCE.getLogger().info("Saved tokens!");
    }

    private Map<String, String> getTokens() {
        return this.tokens;
    }
}
