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
import net.dv8tion.jda.utils.PermissionUtil;
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
    private final Map<User, PermissionOverride> userPermissionOverrides = new HashMap<>();
    private final Map<Role, PermissionOverride> rolePermissionOverrides = new HashMap<>();

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
        try
        {
            JSONObject response = api.getRequester().post("https://discordapp.com/api/channels/" + getId() + "/messages",
                    new JSONObject().put("content", msg.getContent()).put("tts", msg.isTTS()));
            return new EntityBuilder(api).createMessage(response);
        }
        catch (JSONException ex)
        {
            ex.printStackTrace();
            //sending failed
            return null;
        }
    }

    @Override
    public boolean checkPermission(User user, Permission perm)
    {
        return PermissionUtil.checkPermission(this, user, perm);
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
