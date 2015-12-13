package net.dv8tion.jda;

import net.dv8tion.jda.requests.RequestBuilder;
import net.dv8tion.jda.requests.RequestType;
import net.dv8tion.jda.requests.WebSocketClient;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;



public class JDA
{
    private String authToken = null;
    private WebSocketClient client;

    public JDA(String email, String password) throws IllegalArgumentException, LoginException
    {
        if (email == null || email.isEmpty() || password == null || password.isEmpty())
            throw new IllegalArgumentException("The provided email or password as empty / null.");

        Path saveFile = Paths.get("save.json");
        JSONObject configs = null;
        String gateway = null;
        if (Files.exists(saveFile))
        {
            configs = readJson(saveFile);
        }
        if (configs == null)
        {
            configs = new JSONObject().put("tokens", new JSONObject()).put("version", 1);
        }

        if (configs.getJSONObject("tokens").has(email))
        {
            try
            {
                authToken = configs.getJSONObject("tokens").getString(email);
                RequestBuilder rb = new RequestBuilder(this);
                rb.setType(RequestType.GET);
                rb.setUrl("https://discordapp.com/api/gateway");
                gateway = new JSONObject(rb.makeRequest()).getString("url");
            }
            catch (JSONException ex)
            {
                System.out.println("Save-file misformatted. Please delete it for recreation");
            }
            catch (Exception ex)
            {
                //ignore
            }
        }

        if (gateway == null)                                    //no token saved or invalid
        {
            RequestBuilder b = new RequestBuilder(this);
            b.setSendLoginHeaders(false);
            b.setData(new JSONObject().put("email", email).put("password", password).toString());
            b.setUrl("https://discordapp.com/api/auth/login");
            b.setType(RequestType.POST);

            String response = b.makeRequest();
            if (response == null)
                throw new LoginException("The provided email / password combination was incorrect. Please provide valid details.");

            authToken = new JSONObject(response).getString("token");
            configs.getJSONObject("tokens").put(email, authToken);

            RequestBuilder pb = new RequestBuilder(this);
            pb.setType(RequestType.GET);
            pb.setUrl("https://discordapp.com/api/gateway");

            gateway = new JSONObject(pb.makeRequest()).getString("url");
        }

        writeJson(saveFile, configs);
        client = new WebSocketClient(gateway, this);
    }

    public static void main(String[] args)
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

    private static JSONObject readJson(Path file)
    {
        try
        {
            return new JSONObject(StringUtils.join(Files.readAllLines(file, StandardCharsets.UTF_8), ""));
        }
        catch (IOException e)
        {
            System.out.println("Error reading save-file. Defaulting to standard");
            e.printStackTrace();
        }
        catch (JSONException e)
        {
            System.out.println("Save-file misformatted. Creating default one");
        }
        return null;
    }

    private static void writeJson(Path file, JSONObject object)
    {
        try
        {
            Files.write(file, Arrays.asList(object.toString(4).split("\n")), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
        catch (IOException e)
        {
            System.out.println("Error creating save-file");
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
