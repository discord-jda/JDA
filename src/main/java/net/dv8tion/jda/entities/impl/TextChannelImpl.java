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
package net.dv8tion.jda.entities.impl;

import net.dv8tion.jda.MessageBuilder;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.*;
import net.dv8tion.jda.handle.EntityBuilder;
import net.dv8tion.jda.requests.RequestBuilder;
import net.dv8tion.jda.requests.RequestType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TextChannelImpl implements TextChannel
{
    private final String id;
    private final Guild guild;
    private final JDAImpl api;
    private String name;
    private String topic;
    private int position;
    private Map<User, PermissionOverride> userPermissionOverrides = new HashMap<>();
    private Map<Role, PermissionOverride> rolePermissionOverrides = new HashMap<>();

    public TextChannelImpl(String id, Guild guild, JDAImpl api)
    {
        this.id = id;
        this.guild = guild;
        this.api = api;
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
    public String getTopic()
    {
        return topic;
    }

    @Override
    public Guild getGuild()
    {
        return guild;
    }

    @Override
    public List<User> getUsers()
    {
        List<User> users = getGuild().getUsers().stream().filter(user -> checkPermission(user, Permission.MESSAGE_READ)).collect(Collectors.toList());
        return Collections.unmodifiableList(users);
    }

    @Override
    public int getPosition()
    {
        return position;
    }

    @Override
    public Message sendMessage(String text)
    {
        return sendMessage(new MessageBuilder().appendString(text).build());
    }

    @Override
    public Message sendMessage(Message msg)
    {
        JSONArray mentions = new JSONArray();
        for (User user : msg.getMentionedUsers())
        {
            mentions.put(user.getId());
        }
        RequestBuilder rb = new RequestBuilder(api);
        rb.setType(RequestType.POST);
        rb.setUrl("https://discordapp.com/api/channels/" + getId() + "/messages");
        rb.setData(new JSONObject().put("content", msg.getContent()).put("mentions", mentions).toString());
        JSONObject response = new JSONObject(rb.makeRequest());
        try
        {
            return new EntityBuilder(api).createMessage(response);
        }
        catch (JSONException ex)
        {
            //sending failed
            return null;
        }
    }

    @Override
    public boolean checkPermission(User user, Permission perm)
    {
        //Do we have all permissions possible? (Owner or user has MANAGE_ROLES permission)
        //If we have all permissions possible, then we will be able to see this room.
        if (getGuild().getOwnerId().equals(user.getId())
                || getGuild().getPublicRole().hasPermission(Permission.MANAGE_ROLES)
                || getGuild().getRolesForUser(user).stream().anyMatch(role -> role.hasPermission(Permission.MANAGE_ROLES)))
        {
            return true;
        }

        //Default global permission of @everyone in this guild
        int permission = ((RoleImpl) getGuild().getPublicRole()).getPermissions();
        //override with channel-specific overrides of @everyone
        PermissionOverride override = rolePermissionOverrides.get(getGuild().getPublicRole());
        if (override != null)
        {
            permission = rolePermissionOverrides.get(getGuild().getPublicRole()).apply(permission);
        }

        //handle role-overrides of this user in this channel
        List<Role> rolesOfUser = getGuild().getRolesForUser(user);
        override = null;
        for (Role role : rolesOfUser)
        {
            PermissionOverride po = rolePermissionOverrides.get(role);
            override = (po == null) ? override : ((override == null) ? po : po.after(override));
        }
        if (override != null)
        {
            permission = override.apply(permission);
        }

        //handle user-specific overrides
        PermissionOverride useroverride = userPermissionOverrides.get(user);
        if (useroverride != null)
        {
            permission = useroverride.apply(permission);
        }
        return (permission & (1 << perm.getOffset())) > 0;
    }

    public TextChannelImpl setName(String name)
    {
        this.name = name;
        return this;
    }

    public TextChannelImpl setTopic(String topic)
    {
        this.topic = topic;
        return this;
    }

    public TextChannelImpl setPosition(int position)
    {
        this.position = position;
        return this;
    }

    public Map<User, PermissionOverride> getUserPermissionOverrides()
    {
        return userPermissionOverrides;
    }

    public Map<Role, PermissionOverride> getRolePermissionOverrides()
    {
        return rolePermissionOverrides;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof TextChannel))
            return false;
        TextChannel oTChannel = (TextChannel) o;
        return this == oTChannel || this.getId().equals(oTChannel.getId());
    }

    @Override
    public int hashCode()
    {
        return getId().hashCode();
    }
}
