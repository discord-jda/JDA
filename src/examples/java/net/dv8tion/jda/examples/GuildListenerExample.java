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
package net.dv8tion.jda.examples;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.events.guild.GuildJoinEvent;
import net.dv8tion.jda.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.events.guild.GuildRoleCreateEvent;
import net.dv8tion.jda.events.guild.GuildRoleDeleteEvent;
import net.dv8tion.jda.events.guild.member.GuildMemberBanEvent;
import net.dv8tion.jda.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.events.guild.member.GuildMemberUnbanEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;
import org.json.JSONException;
import org.json.JSONObject;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GuildListenerExample extends ListenerAdapter
{
    /**
     * Used for the internal test bot.
     *
     * @param args not used
     */
    public static void main(String[] args)
    {
        JSONObject config = getConfig();
        try
        {
            JDA api = JDA.newInstance();
            api.addEventListener(new GuildListenerExample());
            api.login(config.getString("email"), config.getString("password"));
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
    public void onGuildJoin(GuildJoinEvent event)
    {
        System.out.println("I joined a new Guild! Guild Name: " + event.getGuild().getName());
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event)
    {
        System.out.println("I left a guild... Hope I wasn't kicked.  Guild Name: " + event.getGuild().getName());
    }

    @Override
    public void onGuildMemberBan(GuildMemberBanEvent event)
    {
        System.out.println("Didn't like " + event.getUser().getUsername() + " anyways.");
    }

    @Override
    public void onGuildMemberUnban(GuildMemberUnbanEvent event)
    {
        System.out.println(event.getUserName() + " was unbanned.  Better keep an eye on them...");
    }

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event)
    {
        System.out.println(event.getUser().getUsername() + " was just given the following roles: ");
        for (Role r : event.getRoles())
        {
            System.out.println(r.getName());
        }
    }

    @Override
    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event)
    {
        System.out.println(event.getUser().getUsername() + " just had the following roles removed: ");
        for (Role r : event.getRoles())
        {
            System.out.println(r.getName());
        }
    }

    @Override
    public void onGuildRoleCreate(GuildRoleCreateEvent event)
    {
        System.out.println("The " + event.getGuild().getName() + " Guild just created a new role called: " + event.getRole().getName());
    }

    @Override
    public void onGuildRoleDelete(GuildRoleDeleteEvent event)
    {
        System.out.println("The following role was deleted from the " + event.getGuild().getName() + " guild: " + event.getRole().getName());
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
