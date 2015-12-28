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

import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.utils.PermissionUtil;

import java.util.*;

public class VoiceChannelImpl implements VoiceChannel
{
    private final String id;
    private final Guild guild;
    private String name;
    private int position;
    private List<User> connectedUsers = new ArrayList<>();
    private final Map<User, PermissionOverride> userPermissionOverrides = new HashMap<>();
    private final Map<Role, PermissionOverride> rolePermissionOverrides = new HashMap<>();

    public VoiceChannelImpl(String id, Guild guild)
    {
        this.id = id;
        this.guild = guild;
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
    public Guild getGuild()
    {
        return guild;
    }

    @Override
    public int getPosition()
    {
        return position;
    }

    @Override
    public List<User> getUsers()
    {
        return Collections.unmodifiableList(connectedUsers);
    }

    @Override
    public boolean checkPermission(User user, Permission perm)
    {
        return PermissionUtil.checkPermission(this, user, perm);
    }

    public VoiceChannelImpl setName(String name)
    {
        this.name = name;
        return this;
    }

    public VoiceChannelImpl setPosition(int position)
    {
        this.position = position;
        return this;
    }

    public VoiceChannelImpl setUsers(List<User> connectedUsers)
    {
        this.connectedUsers = connectedUsers;
        return this;
    }

    public List<User> getUsersModifiable()
    {
        return connectedUsers;
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
        if (!(o instanceof VoiceChannel))
            return false;
        VoiceChannel oVChannel = (VoiceChannel) o;
        return this == oVChannel || this.getId().equals(oVChannel.getId());
    }

    @Override
    public int hashCode()
    {
        return getId().hashCode();
    }
}
