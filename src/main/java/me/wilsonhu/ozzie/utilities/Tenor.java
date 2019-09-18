package me.wilsonhu.ozzie.utilities;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import me.wilsonhu.ozzie.OzzieManager;

public class Tenor {
    
    public ArrayList<String> getItems(String search, OzzieManager ozzie){
    	ArrayList<String> items = new ArrayList<String>();
    	Thread t = new Thread() {
            @Override
            public void run() {

                String searchTerm = "";
				try {
					searchTerm = URLEncoder.encode(search, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
                JSONObject searchResult = getSearchResults(searchTerm, 50, ozzie);
                
                for(int i = 0; i < 50; i++) {
                	JSONObject results = searchResult.getJSONArray("results").getJSONObject(i);
                	items.add(results.getString("itemurl"));
                }

            }
        };
    	t.start();
    	try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	return items;
    }

    /**
     * Get Search Result GIFs
     * @param ozzie 
     */
    public JSONObject getSearchResults(String searchTerm, int limit, OzzieManager ozzie) {

        // make search request - using default locale of EN_US
    	String token = ozzie.getTokenManager().getTokenList().get("tenor");
    	
        final String url = String.format("https://api.tenor.com/v1/search?q=%1$s&key=%2$s&limit=%3$s",
                searchTerm, token, limit);
        try {
            return get(url);
        } catch (IOException | JSONException ignored) {
        }
        return null;
    }

    /**
     * Construct and run a GET request
     */
    private JSONObject get(String url) throws IOException, JSONException {
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
        return new JSONObject("");
    }

    /**
     * Parse the response into JSONObject
     */
    private JSONObject parser(HttpURLConnection connection) throws JSONException {
        char[] buffer = new char[1024 * 4];
        int n;
        InputStream stream = null;
        try {
            stream = new BufferedInputStream(connection.getInputStream());
            InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
            StringWriter writer = new StringWriter();
            while (-1 != (n = reader.read(buffer))) {
                writer.write(buffer, 0, n);
            }
            return new JSONObject(writer.toString());
        } catch (IOException ignored) {
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignored) {
                }
            }
        }
        return new JSONObject("");
    }
}