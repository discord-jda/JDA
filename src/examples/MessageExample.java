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
package examples;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.MessageReceivedEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;
import org.json.JSONException;
import org.json.JSONObject;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class MessageExample extends ListenerAdapter
{
    /**
     * Used for the internal test bot.
     *
     * @param args
     */
    public static void main(String[] args)
    {
        JSONObject config = getConfig();
        try
        {
            JDA api = new JDA(config.getString("email"), config.getString("password"));
            api.getEventManager().register(new MessageExample());
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
            return new JSONObject(new String(Files.readAllBytes(Paths.get(config.getPath())), "UTF-8"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        User author = event.getMessage().getAuthor();
        TextChannel channel = event.getMessage().getChannel();
        Guild guild = channel.getGuild();
        List<User> mentions = event.getMessage().getMentionedUsers();

        StringBuilder builder = new StringBuilder();
        builder.append("[")
            .append(guild.getName())
            .append("]{")
            .append(channel.getName())
            .append("} ")
            .append(author.getUsername())
            .append(": ")
            .append(event.getMessage().getContent());
        System.out.println(builder.toString());

        builder = new StringBuilder();
        for (User u : mentions)
        {
            builder.append(u.getUsername()).append(", ");
        }
        String mentionsMessage = builder.toString();
        if (!mentionsMessage.isEmpty())
        {
            mentionsMessage = mentionsMessage.substring(0, mentionsMessage.length() - 2);
            System.out.println("The follow users were mentioned: " + mentionsMessage);
        }
        System.out.println("Users in channel " + channel.getName() + ": " + channel.getUsers().stream().map(User::getUsername).reduce((s1, s2) -> s1 + ", " + s2).get());
        if (author.getUsername().equalsIgnoreCase("kantenkugel") || author.getUsername().equalsIgnoreCase("dv8fromtheworld"))
        {
            channel.sendMessage("Hi");
        }
    }
}
