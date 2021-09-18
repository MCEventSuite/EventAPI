package dev.imabad.mceventsuite.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.imabad.mceventsuite.core.util.GsonUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class MyMCUUID {

    public static UUID getUUID(String username) {
        try {
            URL url = new URL("https://api.mymcuu.id/username/" + username);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("User-Agent", "Cubed! 2021");
            con.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            String response = content.toString();
            in.close();
            con.disconnect();
            return UUID.fromString(GsonUtils.getGson().fromJson(response, JsonObject.class).get("uuid").getAsString());
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }


}
