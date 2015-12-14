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
package net.dv8tion.jda.handle;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.OnlineStatus;
import net.dv8tion.jda.Region;
import net.dv8tion.jda.entities.*;
import net.dv8tion.jda.entities.impl.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EntityBuilder
{
    private final JDA api;

    public EntityBuilder(JDA api)
    {
        this.api = api;
    }

    protected Guild createGuild(JSONObject guild)
    {
        String id = guild.getString("id");
        GuildImpl guildObj = ((GuildImpl) api.getGuildMap().get(id));
        if (guildObj == null)
        {
            guildObj = new GuildImpl(id);
            api.getGuildMap().put(id, guildObj);
        }
        guildObj.setIconId(guild.isNull("icon") ? null : guild.getString("icon"));
        guildObj.setRegion(Region.fromKey(guild.getString("region")));
        guildObj.setName(guild.getString("name"));
        guildObj.setOwnerId(guild.getString("owner_id"));
        guildObj.setAfkTimeout(guild.getInt("afk_timeout"));
        guildObj.setAfkChannelId(guild.isNull("afk_channel_id") ? null : guild.getString("afk_channel_id"));

        JSONArray channels = guild.getJSONArray("channels");
        for (int i = 0; i < channels.length(); i++)
        {
            JSONObject channel = channels.getJSONObject(i);
            String type = channel.getString("type");
            if (type.equalsIgnoreCase("text"))
            {
                createTextChannel(channel, guildObj.getId());
            }
            else if (type.equalsIgnoreCase("voice"))
            {
                createVoiceChannel(channel, guildObj.getId());
            }
        }

        JSONArray roles = guild.getJSONArray("roles");
        for (int i = 0; i < roles.length(); i++)
        {
            Role role = createRole(roles.getJSONObject(i), guildObj.getId());
            guildObj.getRolesMap().put(role.getId(), role);
        }

        JSONArray members = guild.getJSONArray("members");
        Map<String, Role> rolesMap = guildObj.getRolesMap();
        Map<User, List<Role>> userRoles = guildObj.getUserRoles();
        for (int i = 0; i < members.length(); i++)
        {
            JSONObject member = members.getJSONObject(i);
            User user = createUser(member.getJSONObject("user"));
            userRoles.put(user, new ArrayList<>());
            JSONArray roleArr = member.getJSONArray("roles");
            for (int j = 0; j < roleArr.length(); j++)
            {
                String roleId = roleArr.getString(j);
                userRoles.get(user).add(rolesMap.get(roleId));
            }
        }
        JSONArray presences = guild.getJSONArray("presences");
        for (int i = 0; i < presences.length(); i++)
        {
            JSONObject presence = presences.getJSONObject(i);
            UserImpl user = ((UserImpl) api.getUserMap().get(presence.getJSONObject("user").getString("id")));
            user.setCurrentGameId(presence.isNull("game_id") ? -1 : presence.getInt("game_id"));
            user.setOnlineStatus(OnlineStatus.fromKey(presence.getString("status")));
        }
        return guildObj;
    }

    protected TextChannel createTextChannel(JSONObject json, String guildId)
    {
        String id = json.getString("id");
        GuildImpl guild = ((GuildImpl) api.getGuildMap().get(guildId));
        TextChannelImpl channel = (TextChannelImpl) guild.getTextChannelsMap().get(id);
        if (channel == null)
        {
            channel = new TextChannelImpl(id, guild);
            guild.getTextChannelsMap().put(id, channel);
        }
        return channel.setName(json.getString("name")).setTopic(json.isNull("topic") ? "" : json.getString("topic")).setPosition(json.getInt("position"));
    }

    protected TextChannel createVoiceChannel(JSONObject json, String guildId)
    {
        return null;
    }

    protected PrivateChannel createPrivateChannel(JSONObject privatechat)
    {
        UserImpl user = ((UserImpl) api.getUserMap().get(privatechat.getJSONObject("recipient").getString("id")));
        if (user == null)   //The API can give us private channels connected to Users that we can no longer communicate with.
            return null;    //As such, can't and shouldn't attempt to create a PrivateChannel.

        PrivateChannelImpl priv = new PrivateChannelImpl(privatechat.getString("id"), user);
        user.setPrivateChannel(priv);
        return priv;
    }

    protected Role createRole(JSONObject roleJson, String guildId)
    {
        String id = roleJson.getString("id");
        GuildImpl guild = ((GuildImpl) api.getGuildMap().get(guildId));
        RoleImpl role = ((RoleImpl) guild.getRolesMap().get(id));
        if (role == null)
        {
            role = new RoleImpl(id);
        }
        role.setName(roleJson.getString("name")).setPosition(roleJson.getInt("position")).setPermissions(roleJson.getInt("permissions"))
                .setManaged(roleJson.getBoolean("managed")).setHoist(roleJson.getBoolean("hoist")).setColor(roleJson.getInt("color"));
        return role;
    }

    protected User createUser(JSONObject user)
    {
        String id = user.getString("id");
        UserImpl userObj = ((UserImpl) api.getUserMap().get(id));
        if (userObj == null)
        {
            userObj = new UserImpl(id);
            api.getUserMap().put(id, userObj);
        }
        userObj.setUserName(user.getString("username"));
        userObj.setDiscriminator(user.get("discriminator").toString());
        userObj.setAvatarId(user.isNull("avatar") ? null : user.getString("avatar"));
        return userObj;
    }

    protected SelfInfo createSelfInfo(JSONObject self)
    {
        SelfInfoImpl selfInfo = ((SelfInfoImpl) api.getSelfInfo());
        if (selfInfo == null)
        {
            selfInfo = new SelfInfoImpl(self.getString("id"), self.getString("email"));
            api.setSelfInfo(selfInfo);
        }
        selfInfo.setVerified(self.getBoolean("verified"));
        selfInfo.setUserName(self.getString("username"));
        selfInfo.setDiscriminator(self.getString("discriminator"));
        selfInfo.setAvatarId(self.isNull("avatar") ? null : self.getString("avatar"));
        return selfInfo;
    }
}
