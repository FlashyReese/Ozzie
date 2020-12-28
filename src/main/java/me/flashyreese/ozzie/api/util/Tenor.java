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
package me.flashyreese.ozzie.api.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.flashyreese.ozzie.api.OzzieApi;

import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Tenor {

    public List<String> getItems(String search) {
        List<String> items = new ObjectArrayList<>();
        Thread t = new Thread(() -> {

            String searchTerm = "";
            try {
                searchTerm = URLEncoder.encode(search, "UTF-8").replaceAll("\\+", "%20");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            JsonObject searchResult = getSearchResults(searchTerm, 50);

            for (int i = 0; i < 50; i++) {

                JsonObject results = searchResult.getAsJsonArray("results").get(i).getAsJsonObject();
                items.add(results.get("itemurl").getAsString());
            }

        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return items;
    }


    public JsonObject getSearchResults(String searchTerm, int limit) {

        // make search request - using default locale of EN_US
        String token = OzzieApi.INSTANCE.getTokenManager().getToken("tenor");

        final String url = String.format("https://api.tenor.com/v1/search?q=%1$s&key=%2$s&limit=%3$s",
                searchTerm, token, limit);
        try {
            return get(url);
        } catch (IOException ignored) {
        }
        return null;
    }


    private JsonObject get(String url) throws IOException {
        HttpURLConnection connection = null;
        try {
            // Get request
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            // Handle failure
            int statusCode = connection.getResponseCode();
            if (statusCode != HttpURLConnection.HTTP_OK && statusCode != HttpURLConnection.HTTP_CREATED) {
                String error = String.format("HTTP Code: '%1$s' from '%2$s'", statusCode, url);
                throw new ConnectException(error);
            }

            // Parse response
            return parser(connection);
        } catch (Exception ignored) {
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return JsonParser.parseString("").getAsJsonObject();
    }


    private JsonObject parser(HttpURLConnection connection) {
        char[] buffer = new char[1024 * 4];
        int n;
        InputStream stream = null;
        try {
            stream = new BufferedInputStream(connection.getInputStream());
            InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            StringWriter writer = new StringWriter();
            while (-1 != (n = reader.read(buffer))) {
                writer.write(buffer, 0, n);
            }
            return JsonParser.parseString(writer.toString()).getAsJsonObject();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return JsonParser.parseString("").getAsJsonObject();
    }
}