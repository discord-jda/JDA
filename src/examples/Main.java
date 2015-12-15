/**
 * Created by Michael Ritter on 15.12.2015.
 */
package examples;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.events.MessageCreateEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;
import org.json.JSONException;
import org.json.JSONObject;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main extends ListenerAdapter
{
    /**
     * Used for the internal test bot. Will be removed.
     *
     * @param args
     */
    public static void main(String[] args)
    {
        JSONObject config = getConfig();
        try
        {
            JDA api = new JDA(config.getString("email"), config.getString("password"));
            api.getEventManager().register(new Main());
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

    @Override
    public void onMessageCreate(MessageCreateEvent event)
    {
        System.out.println("[" + event.getMessage().getChannel().getName() + ']' +
                event.getMessage().getAuthor().getUsername() + ": " + event.getMessage().getContent());
    }
}
