/**
 *    Copyright 2015 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda;

import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.SelfInfo;
import net.dv8tion.jda.entities.User;
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
import java.util.HashMap;
import java.util.Map;


/**
 * Represents the core of the Discord API. All functionality is connected through this.
 */
public class JDA
{
    private final Map<String, User> userMap = new HashMap<>();
    private final Map<String, Guild> guildMap = new HashMap<>();
    private SelfInfo selfInfo = null;
    private String authToken = null;
    private WebSocketClient client;

    /**
     * Creates a new instance of the Discord API wrapper and attempts to login to Discord.
     * Upon successful auth with Discord, a token is returned and stored in token.json.
     *
     * @param email
     *          The email of the account attempting to log in.
     * @param password
     *          The password of the account attempting to log in.
     * @throws IllegalArgumentException
     *          Thrown if this email or password provided are empty or null.
     * @throws LoginException
     *          Thrown if the email-password combination fails the auth check with the Discord servers.
     */
    public JDA(String email, String password) throws IllegalArgumentException, LoginException
    {
        if (email == null || email.isEmpty() || password == null || password.isEmpty())
            throw new IllegalArgumentException("The provided email or password as empty / null.");

        Path tokenFile = Paths.get("tokens.json");
        JSONObject configs = null;
        String gateway = null;
        if (Files.exists(tokenFile))
        {
            configs = readJson(tokenFile);
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
                System.out.println("Token-file misformatted. Please delete it for recreation");
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

        writeJson(tokenFile, configs);
        client = new WebSocketClient(gateway, this);
    }

    /**
     * Used for the internal test bot. Will be removed.
     * @param args
     */
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

    /**
     * Takes a provided json file, reads all lines and constructs a {@link org.json.JSONObject JSONObject} from it.
     *
     * @param file
     *          The json file to read.
     * @return
     *      The {@link org.json.JSONObject JSONObject} representation of the json in the file.
     */
    private static JSONObject readJson(Path file)
    {
        try
        {
            return new JSONObject(StringUtils.join(Files.readAllLines(file, StandardCharsets.UTF_8), ""));
        }
        catch (IOException e)
        {
            System.out.println("Error reading token-file. Defaulting to standard");
            e.printStackTrace();
        }
        catch (JSONException e)
        {
            System.out.println("Token-file misformatted. Creating default one");
        }
        return null;
    }

    /**
     * Writes the json representation of the provided {@link org.json.JSONObject JSONObject} to the provided file.
     *
     * @param file
     *          The file which will have the json representation of object written into.
     * @param object
     *          The {@link org.json.JSONObject JSONObject} to write to file.
     */
    private static void writeJson(Path file, JSONObject object)
    {
        try
        {
            Files.write(file, Arrays.asList(object.toString(4).split("\n")), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
        catch (IOException e)
        {
            System.out.println("Error creating token-file");
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

    public Map<String, User> getUserMap()
    {
        return userMap;
    }

    public Map<String, Guild> getGuildMap()
    {
        return guildMap;
    }

    /**
     * Returns the currently logged in account represented by {@link net.dv8tion.jda.entities.SelfInfo SelfInfo}.<br>
     * Account settings <b>cannot</b> be modified using this object. If you wish to modify account settings please
     *   use the {@link net.dv8tion.jda.AccountManager AccountManager}.
     *
     * @return
     *      The currently logged in account.
     */
    public SelfInfo getSelfInfo()
    {
        return selfInfo;
    }
    public void setSelfInfo(SelfInfo selfInfo)
    {
        this.selfInfo = selfInfo;
    }
}
