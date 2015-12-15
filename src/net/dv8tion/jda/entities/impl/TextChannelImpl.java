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
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TextChannelImpl implements TextChannel
{
    private final String id;
    private final Guild guild;
    private String name;
    private String topic;
    private int position;
    private Map<User, PermissionOverride> userPermissionOverrides = new HashMap<>();
    private Map<Role, PermissionOverride> rolePermissionOverrides = new HashMap<>();

    public TextChannelImpl(String id, Guild guild)
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
        throw new UnsupportedOperationException("Until permissions is finished, getting the Users in a Channel is not supported");
    }

    @Override
    public int getPosition()
    {
        return position;
    }

    @Override
    public boolean checkPermission(User user, Permission perm)
    {
        //Default global permission of @everyone in this guild
        int permission = ((RoleImpl) getGuild().getPublicRole()).getPermissions();
        //override with channel-specific overrides of @everyone
        permission = rolePermissionOverrides.get(getGuild().getPublicRole()).apply(permission);

        //handle role-overrides of this user in this channel
        List<Role> rolesOfUser = getGuild().getRolesForUser(user);
        Optional<PermissionOverride> reduced = rolesOfUser.stream().map(role -> rolePermissionOverrides.get(role)).reduce(PermissionOverride::after);
        if (reduced.isPresent())
        {
            permission = reduced.get().apply(permission);
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
        return null;
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
