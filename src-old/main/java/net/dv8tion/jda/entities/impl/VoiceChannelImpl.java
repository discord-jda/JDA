/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
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
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.*;
import net.dv8tion.jda.exceptions.PermissionException;
import net.dv8tion.jda.managers.ChannelManager;
import net.dv8tion.jda.managers.PermissionOverrideManager;
import net.dv8tion.jda.utils.InviteUtil;
import net.dv8tion.jda.utils.MiscUtil;
import net.dv8tion.jda.utils.PermissionUtil;

import java.time.OffsetDateTime;
import java.util.*;

public class VoiceChannelImpl implements VoiceChannel
{
    private final String id;
    private final Guild guild;
    private String name;
    private int position;
    private int userLimit;
    private int bitrate;
    private List<User> connectedUsers = new ArrayList<>();
    private final Map<User, PermissionOverride> userPermissionOverrides = new HashMap<>();
    private final Map<Role, PermissionOverride> rolePermissionOverrides = new HashMap<>();

    private ChannelManager manager = null;

    public VoiceChannelImpl(String id, Guild guild)
    {
        this.id = id;
        this.guild = guild;
    }

    @Override
    public JDA getJDA()
    {
        return guild.getJDA();
    }

    @Override
    public PermissionOverride getOverrideForUser(User user)
    {
        return userPermissionOverrides.get(user);
    }

    @Override
    public PermissionOverride getOverrideForRole(Role role)
    {
        return rolePermissionOverrides.get(role);
    }

    @Override
    public List<PermissionOverride> getPermissionOverrides()
    {
        List<PermissionOverride> overrides = new LinkedList<>();
        overrides.addAll(userPermissionOverrides.values());
        overrides.addAll(rolePermissionOverrides.values());
        return Collections.unmodifiableList(overrides);
    }

    @Override
    public List<PermissionOverride> getUserPermissionOverrides()
    {
        return Collections.unmodifiableList(new LinkedList<PermissionOverride>(userPermissionOverrides.values()));
    }

    @Override
    public List<PermissionOverride> getRolePermissionOverrides()
    {
        return Collections.unmodifiableList(new LinkedList<PermissionOverride>(rolePermissionOverrides.values()));
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
        List<VoiceChannel> channels = guild.getVoiceChannels();
        for (int i = 0; i < channels.size(); i++)
        {
            if (channels.get(i) == this)
                return i;
        }
        throw new RuntimeException("Somehow when determining position we never found the VoiceChannel in the Guild's channels? wtf?");
    }

    @Override
    public int getPositionRaw()
    {
        return position;
    }

    @Override
    public List<User> getUsers()
    {
        return Collections.unmodifiableList(new LinkedList<>(connectedUsers));
    }

    @Override
    public boolean checkPermission(User user, Permission perm)
    {
        return PermissionUtil.checkPermission(user, perm, this);
    }

    @Override
    public synchronized ChannelManager getManager()
    {
        if (manager == null)
            manager = new ChannelManager(this);
        return manager;
    }

    @Override
    public PermissionOverrideManager createPermissionOverride(User user)
    {
        if (!checkPermission(getJDA().getSelfInfo(), Permission.MANAGE_PERMISSIONS))
        {
            throw new PermissionException(Permission.MANAGE_PERMISSIONS);
        }
        if (!getGuild().getUsers().contains(user))
        {
            throw new IllegalArgumentException("Given user is not member of this Guild");
        }
        PermissionOverrideImpl override = new PermissionOverrideImpl(this, user, null);
        //hacky way of putting entity to server without using requester here
        override.setAllow(1 << Permission.MANAGE_PERMISSIONS.getOffset()).setDeny(0);
        PermissionOverrideManager manager = override.getManager();
        manager.reset(Permission.MANAGE_PERMISSIONS).update();
        return manager;
    }

    @Override
    public PermissionOverrideManager createPermissionOverride(Role role)
    {
        if (!checkPermission(getJDA().getSelfInfo(), Permission.MANAGE_PERMISSIONS))
        {
            throw new PermissionException(Permission.MANAGE_PERMISSIONS);
        }
        if (!getGuild().getRoles().contains(role))
        {
            throw new IllegalArgumentException("Given role does not exist in this Guild");
        }
        PermissionOverrideImpl override = new PermissionOverrideImpl(this, null, role);
        //hacky way of putting entity to server without using requester here
        override.setAllow(1 << Permission.MANAGE_PERMISSIONS.getOffset()).setDeny(0);
        PermissionOverrideManager manager = override.getManager();
        manager.reset(Permission.MANAGE_PERMISSIONS).update();
        return manager;
    }

    @Override
    public List<InviteUtil.AdvancedInvite> getInvites()
    {
        return InviteUtil.getInvites(this);
    }

    @Override
    public int getUserLimit()
    {
        return userLimit;
    }

    @Override
    public int getBitrate()
    {
        return bitrate;
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

    public VoiceChannelImpl setUserLimit(int userLimit)
    {
        this.userLimit = userLimit;
        return this;
    }

    public VoiceChannelImpl setBitrate(int bitrate)
    {
        this.bitrate = bitrate;
        return this;
    }

    public List<User> getUsersModifiable()
    {
        return connectedUsers;
    }

    public Map<User, PermissionOverride> getUserPermissionOverridesMap()
    {
        return userPermissionOverrides;
    }

    public Map<Role, PermissionOverride> getRolePermissionOverridesMap()
    {
        return rolePermissionOverrides;
    }

    @Override
    public String getTopic()
    {
        return null;
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

    @Override
    public String toString()
    {
        return "VC:" + getName() + '(' + getId() + ')';
    }

    @Override
    public int compareTo(VoiceChannel chan)
    {
        if (this == chan)
            return 0;

        if (this.getGuild() != chan.getGuild())
            throw new IllegalArgumentException("Cannot compare VoiceChannels that aren't from the same guild!");

        if (this.getPositionRaw() != chan.getPositionRaw())
            return chan.getPositionRaw() - this.getPositionRaw();

        OffsetDateTime thisTime = MiscUtil.getCreationTime(this);
        OffsetDateTime chanTime = MiscUtil.getCreationTime(chan);

        //We compare the provided channel's time to this's time instead of the reverse as one would expect due to how
        // discord deals with hierarchy. The more recent a channel was created, the lower its hierarchy ranking when
        // it shares the same position as another channel.
        return chanTime.compareTo(thisTime);
    }
}
