/**
 *    Copyright 2015-2016 Austin Keener & Michael Ritter
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
package net.dv8tion.jda.entities.impl;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.Region;
import net.dv8tion.jda.entities.*;
import net.dv8tion.jda.handle.EntityBuilder;
import net.dv8tion.jda.managers.ChannelManager;
import net.dv8tion.jda.managers.GuildManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class GuildImpl implements Guild
{
    private final String id;
    private String name;
    private String iconId;
    private String afkChannelId;
    private String ownerId;
    private int afkTimeout;
    private Region region;
    private final Map<String, TextChannel> textChannels = new HashMap<>();
    private final Map<String, VoiceChannel> voiceChannels = new HashMap<>();
    private final Map<String, Role> roles = new HashMap<>();
    private Role publicRole;
    private final Map<User, List<Role>> userRoles = new HashMap<>();
    private final JDAImpl api;

    public GuildImpl(JDAImpl api, String id)
    {
        this.api = api;
        this.id = id;
    }

    @Override
    public JDA getJDA()
    {
        return api;
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getIconId()
    {
        return iconId;
    }

    @Override
    public String getIconUrl()
    {
        return "https://cdn.discordapp.com/icons/" + getId() + "/" + getIconId() + ".jpg";
    }

    @Override
    public String getAfkChannelId()
    {
        return afkChannelId;
    }

    @Override
    public String getOwnerId()
    {
        return ownerId;
    }

    @Override
    public int getAfkTimeout()
    {
        return afkTimeout;
    }

    @Override
    public Region getRegion()
    {
        return region;
    }

    @Override
    public List<User> getUsers()
    {
        List<User> list = new ArrayList<>();
        list.addAll(userRoles.keySet());
        return Collections.unmodifiableList(list);
    }

    @Override
    public List<TextChannel> getTextChannels()
    {
        List<TextChannel> list = new ArrayList<>();
        list.addAll(textChannels.values());
        return Collections.unmodifiableList(list);
    }

    @Override
    public ChannelManager createTextChannel(String name)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("TextChannel name must not be null");
        }
        JSONObject response = api.getRequester().post("https://discordapp.com/api/guilds/" + getId() + "/channels", new JSONObject().put("name", name).put("type", "text"));
        if (response == null || !response.has("id"))
        {
            //error creating guild
            throw new RuntimeException("Creating a new TextChannel failed. Reason: " + (response == null ? "Unknown" : response.toString()));
        }
        else
        {
            TextChannel channel = new EntityBuilder(api).createTextChannel(response, getId());
            return new ChannelManager(channel);
        }
    }

    @Override
    public List<VoiceChannel> getVoiceChannels()
    {
        List<VoiceChannel> list = new ArrayList<>();
        list.addAll(voiceChannels.values());
        return Collections.unmodifiableList(list);
    }

    @Override
    public ChannelManager createVoiceChannel(String name)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("VoiceChannel name must not be null");
        }
        JSONObject response = api.getRequester().post("https://discordapp.com/api/guilds/" + getId() + "/channels", new JSONObject().put("name", name).put("type", "voice"));
        if (response == null || !response.has("id"))
        {
            //error creating guild
            throw new RuntimeException("Creating a new VoiceChannel failed. Reason: " + (response == null ? "Unknown" : response.toString()));
        }
        else
        {
            VoiceChannel channel = new EntityBuilder(api).createVoiceChannel(response, getId());
            return new ChannelManager(channel);
        }
    }

    @Override
    public List<Role> getRoles()
    {
        List<Role> list = new ArrayList<>();
        list.addAll(roles.values());
        return Collections.unmodifiableList(list);
    }

    @Override
    public List<Role> getRolesForUser(User user)
    {
        return userRoles.get(user) == null ? new LinkedList<>() : Collections.unmodifiableList(userRoles.get(user));
    }

    @Override
    public Role getPublicRole()
    {
        return publicRole;
    }

    @Override
    public GuildManager getManager()
    {
        return new GuildManager(this);
    }

    @Override
    public List<User> getBans()
    {
        List<User> bans = new LinkedList<>();
        JSONArray bannedArr = api.getRequester().getA("https://discordapp.com/api/guilds/" + getId() + "/bans");
        for (int i = 0; i < bannedArr.length(); i++)
        {
            JSONObject userObj = bannedArr.getJSONObject(i).getJSONObject("user");
            User u = api.getUserById(userObj.getString("id"));
            if (u != null)
            {
                bans.add(u);
            }
            else
            {
                //Create user here, instead of using the EntityBuilder (don't want to add users to registry)
                bans.add(new UserImpl(userObj.getString("id"), api)
                        .setUserName(userObj.getString("username"))
                        .setDiscriminator(userObj.get("discriminator").toString())
                        .setAvatarId(userObj.isNull("avatar") ? null : userObj.getString("avatar")));
            }
        }
        return bans;
    }

    public Map<String, Role> getRolesMap()
    {
        return roles;
    }

    public Map<User, List<Role>> getUserRoles()
    {
        return userRoles;
    }

    public GuildImpl setName(String name)
    {
        this.name = name;
        return this;
    }

    public GuildImpl setIconId(String iconId)
    {
        this.iconId = iconId;
        return this;
    }

    public GuildImpl setOwnerId(String ownerId)
    {
        this.ownerId = ownerId;
        return this;
    }

    public GuildImpl setAfkTimeout(int afkTimeout)
    {
        this.afkTimeout = afkTimeout;
        return this;
    }

    public GuildImpl setAfkChannelId(String channelId)
    {
        this.afkChannelId = channelId;
        return this;
    }

    public GuildImpl setRegion(Region region)
    {
        this.region = region;
        return this;
    }

    public GuildImpl setPublicRole(Role role)
    {
        this.publicRole = role;
        return this;
    }

    public Map<String, TextChannel> getTextChannelsMap()
    {
        return textChannels;
    }

    public Map<String, VoiceChannel> getVoiceChannelsMap()
    {
        return voiceChannels;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof Guild))
            return false;
        Guild oGuild = (Guild) o;
        return this == oGuild || this.getId().equals(oGuild.getId());
    }

    @Override
    public int hashCode()
    {
        return getId().hashCode();
    }
}
