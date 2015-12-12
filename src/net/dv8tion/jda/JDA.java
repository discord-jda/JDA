package net.dv8tion.jda;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import net.dv8tion.jda.requests.RequestBuilder;
import net.dv8tion.jda.requests.RequestType;
import net.dv8tion.jda.requests.WebSocketClient;

import org.json.JSONException;
import org.json.JSONObject;



public class JDA
{
    public static void main(String[] args) throws InterruptedException
    {
        JSONObject auth = getAuth();

        String token = null;
        String gateway = null;

        RequestBuilder b = new RequestBuilder();
        b.setSendLoginHeaders(false);
        b.setData(new JSONObject().put("email", auth.getString("email")).put("password", auth.getString("password")).toString());
        b.setUrl("https://discordapp.com/api/auth/login");
        b.setType(RequestType.POST);

        token = new JSONObject(b.makeRequest()).getString("token");
        System.out.println("Token: " + token);
        RequestBuilder.setAuthToken(token);

        RequestBuilder pb = new RequestBuilder();
        pb.setType(RequestType.GET);
        pb.setUrl("https://discordapp.com/api/gateway");

        gateway = new JSONObject(pb.makeRequest()).getString("url");

        WebSocketClient client = new WebSocketClient(gateway);
        while (!client.isConnected())
        {
            Thread.sleep(50);
        }

        JSONObject connectObj = new JSONObject();
        connectObj
            .put("op", 2)
            .put("d", new JSONObject()
                      .put("token", token)
                      .put("properties", new JSONObject()
                                .put("$os", "Windows")
                                .put("$browser", "Java Discord API")
                                .put("$device", System.getProperty("os.name"))
                                .put("$referring_domain", "t.co")
                                .put("$referrer", "")
                        )
                      .put("v", 3));
        System.out.println(connectObj.toString(4));
        client.send(connectObj.toString());
    }

    private static JSONObject getAuth()
    {
        File config = new File("config.json");
        if (!config.exists())
        {
            try
            {
                Files.write(Paths.get(config.getPath()),
                        new JSONObject()
                                .put("email", "")
                                .put("password", "")
                                .toString(4).getBytes());
                System.out.println("config.json created. Populate with login information.");
                System.exit(0);
            }
            catch (JSONException | IOException e)
            {
                e.printStackTrace();
            }
        }
        try
        {
            JSONObject auth = new JSONObject(new String(Files.readAllBytes(Paths.get(config.getPath())), "UTF-8"));
            if (auth.getString("email") == null || auth.getString("email").isEmpty()
                    || auth.getString("password") == null || auth.getString("password").isEmpty())
            {
                System.out.println("Config not properly populated!");
            }
            return auth;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
