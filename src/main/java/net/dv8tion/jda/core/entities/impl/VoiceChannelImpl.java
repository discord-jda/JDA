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
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.dv8tion.jda.core.entities.impl;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.utils.MiscUtil;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class VoiceChannelImpl implements VoiceChannel
{
    private final String id;
    private final GuildImpl guild;

    private final HashMap<Member, PermissionOverride> memberOverrides = new HashMap<>();
    private final HashMap<Role, PermissionOverride> roleOverrides = new HashMap<>();
    private final HashMap<String, Member> connectedMembers = new HashMap<>();

    private String name;
    private int rawPosition;
    private int userLimit;
    private int bitrate;

    public VoiceChannelImpl(String id, Guild guild)
    {
        this.id = id;
        this.guild = (GuildImpl) guild;
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
    public List<Member> getMembers()
    {
        return Collections.unmodifiableList(new ArrayList<>(connectedMembers.values()));
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
        return rawPosition;
    }

    @Override
    public JDA getJDA()
    {
        return guild.getJDA();
    }

    @Override
    public PermissionOverride getOverrideForMember(Member member)
    {
        return memberOverrides.get(member);
    }

    @Override
    public PermissionOverride getOverrideForRole(Role role)
    {
        return roleOverrides.get(role);
    }

    @Override
    public List<PermissionOverride> getPermissionOverrides()
    {
        List<PermissionOverride> overrides = new ArrayList<>(memberOverrides.size() + roleOverrides.size());
        overrides.addAll(memberOverrides.values());
        overrides.addAll(roleOverrides.values());
        return Collections.unmodifiableList(overrides);
    }

    @Override
    public List<PermissionOverride> getMemberPermissionOverrides()
    {
        return Collections.unmodifiableList(new ArrayList<>(memberOverrides.values()));
    }

    @Override
    public List<PermissionOverride> getRolePermissionOverrides()
    {
        return Collections.unmodifiableList(new ArrayList<>(roleOverrides.values()));
    }

    @Override
    public String getId()
    {
        return id;
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

        OffsetDateTime thisTime = this.getCreationTime();
        OffsetDateTime chanTime = chan.getCreationTime();

        //We compare the provided channel's time to this's time instead of the reverse as one would expect due to how
        // discord deals with hierarchy. The more recent a channel was created, the lower its hierarchy ranking when
        // it shares the same position as another channel.
        return chanTime.compareTo(thisTime);
    }

    // -- Setters --

    public VoiceChannelImpl setName(String name)
    {
        this.name = name;
        return this;
    }

    public VoiceChannelImpl setRawPosition(int rawPosition)
    {
        this.rawPosition = rawPosition;
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

    // -- Map Getters --

    public HashMap<Member, PermissionOverride> getMemberOverrideMap()
    {
        return memberOverrides;
    }

    public HashMap<Role, PermissionOverride> getRoleOverrideMap()
    {
        return roleOverrides;
    }

    public HashMap<String, Member> getConnectedMembersMap()
    {
        return connectedMembers;
    }
}
