package net.dv8tion.jda;

import net.dv8tion.jda.requests.RequestBuilder;
import net.dv8tion.jda.requests.RequestType;
import net.dv8tion.jda.requests.WebSocketClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;



public class JDA
{
    private String authToken;
    private WebSocketClient client;

    public JDA(String email, String password)
    {
        RequestBuilder b = new RequestBuilder();
        b.setSendLoginHeaders(false);
        b.setData(new JSONObject().put("email", email).put("password", password).toString());
        b.setUrl("https://discordapp.com/api/auth/login");
        b.setType(RequestType.POST);

        String response = b.makeRequest();
        if (response != null)
        {
            authToken = new JSONObject(response).getString("token");
        } else
        {
            System.out.println("Login incorrect or rejected");
            System.exit(0);
        }
        System.out.println("Token: " + authToken);
        RequestBuilder.setAuthToken(authToken);

        RequestBuilder pb = new RequestBuilder();
        pb.setType(RequestType.GET);
        pb.setUrl("https://discordapp.com/api/gateway");

        String gateway = new JSONObject(pb.makeRequest()).getString("url");

        client = new WebSocketClient(gateway, this);
    }

    public static void main(String[] args) throws InterruptedException
    {
        JSONObject config = getConfig();
        new JDA(config.getString("email"), config.getString("password"));
    }

    private static JSONObject getConfig()
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

    public String getAuthToken()
    {
        return authToken;
    }

    public WebSocketClient getClient()
    {
        return client;
    }
}
