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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.events.channel.voice.VoiceChannelCreateEvent;
import net.dv8tion.jda.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;

import org.json.JSONException;
import org.json.JSONObject;

public class ChannelListenerExample extends ListenerAdapter
{
    public static void main(String[] args)
    {
        JSONObject config = getConfig();
        try
        {
            JDA api = new JDA(config.getString("email"), config.getString("password"));
            api.getEventManager().register(new ChannelListenerExample());
        }
        catch (IllegalArgumentException e)
        {
            System.out.println("The config was not populated. Please enter an email and password.");
        }
        catch (LoginException e)
        {
            System.out.println("The provided email / password combination was incorrect. Please provide valid details.");
        }
    }

    @Override
    public void onTextChannelCreate(TextChannelCreateEvent event)
    {
        System.out.println("A TextChannel named: " + event.getChannel().getName() + " was created in guild: " + event.getGuild().getName());
    }

    @Override
    public void onTextChannelDelete(TextChannelDeleteEvent event)
    {
        System.out.println("A TextChannel named: " + event.getChannel().getName() + " was deleted from guild: " + event.getGuild().getName());
    }

    @Override
    public void onVoiceChannelCreate(VoiceChannelCreateEvent event)
    {
        System.out.println("A VoiceChannel named: " + event.getChannel().getName() + " was created in guild: " + event.getGuild().getName());
    }

    @Override
    public void onVoiceChannelDelete(VoiceChannelDeleteEvent event)
    {
        System.out.println("A VoiceChannel named: " + event.getChannel().getName() + " was deleted from guild: " + event.getGuild().getName());
    }

    //Simple config system to make life easier. THIS IS NOT REQUIRED FOR JDA.
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
}
