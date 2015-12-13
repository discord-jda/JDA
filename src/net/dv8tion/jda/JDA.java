package net.dv8tion.jda;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.requests.RequestBuilder;
import net.dv8tion.jda.requests.RequestType;
import net.dv8tion.jda.requests.WebSocketClient;

import org.json.JSONException;
import org.json.JSONObject;



public class JDA
{
    private String authToken;
    private WebSocketClient client;

    public JDA(String email, String password) throws IllegalArgumentException, LoginException
    {
        if (email == null || email.isEmpty() || password == null || password.isEmpty())
            throw new IllegalArgumentException("The provided email or password as empty / null.");

        RequestBuilder b = new RequestBuilder();
        b.setSendLoginHeaders(false);
        b.setData(new JSONObject().put("email", email).put("password", password).toString());
        b.setUrl("https://discordapp.com/api/auth/login");
        b.setType(RequestType.POST);

        String response = b.makeRequest();
        if (response == null)
            throw new LoginException("The provided email / password combination was incorrect. Please provide valid details.");

        authToken = new JSONObject(response).getString("token");
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
        try
        {
            new JDA(config.getString("email"), config.getString("password"));
        }
        catch (IllegalArgumentException e)
        {
            System.out.println("The config was not populated. Please enter an email and password.");
        }
        catch (LoginException e)
        {
            System.out.println("The provided email / password combination was incorrect. Please provide valid details.");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            //TODO: Do NOT let this make it to main.  When someone auto generates the Catch list JSONException should not
            //       auto generate with IllegalArgumentException and LoginException.
        }
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
