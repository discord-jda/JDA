/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.core.entities.impl;

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.requests.restaction.ChannelAction;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.MiscUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VoiceChannelImpl extends AbstractChannelImpl<VoiceChannelImpl> implements VoiceChannel
{
    private final TLongObjectMap<Member> connectedMembers = MiscUtil.newLongMap();
    private int userLimit;
    private int bitrate;

    public VoiceChannelImpl(long id, GuildImpl guild)
    {
        super(id, guild);
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
    public ChannelType getType()
    {
        return ChannelType.VOICE;
    }

    @Override
    public List<Member> getMembers()
    {
        return Collections.unmodifiableList(new ArrayList<>(connectedMembers.valueCollection()));
    }

    @Override
    public int getPosition()
    {
        List<VoiceChannel> channels = getGuild().getVoiceChannels();
        for (int i = 0; i < channels.size(); i++)
        {
            if (channels.get(i) == this)
                return i;
        }
        throw new AssertionError("Somehow when determining position we never found the VoiceChannel in the Guild's channels? wtf?");
    }

    @Override
    public ChannelAction createCopy(Guild guild)
    {
        Checks.notNull(guild, "Guild");
        ChannelAction action = guild.getController().createVoiceChannel(name).setBitrate(bitrate).setUserlimit(userLimit);
        if (guild.equals(getGuild()))
        {
            Category parent = getParent();
            if (parent != null)
                action.setParent(parent);
            for (PermissionOverride o : overrides.valueCollection())
            {
                if (o.isMemberOverride())
                    action.addPermissionOverride(o.getMember(), o.getAllowedRaw(), o.getDeniedRaw());
                else
                    action.addPermissionOverride(o.getRole(), o.getAllowedRaw(), o.getDeniedRaw());
            }
        }
        return action;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof VoiceChannel))
            return false;
        VoiceChannel oVChannel = (VoiceChannel) o;
        return this == oVChannel || this.getIdLong() == oVChannel.getIdLong();
    }

    @Override
    public String toString()
    {
        return "VC:" + getName() + '(' + id + ')';
    }

    @Override
    public int compareTo(VoiceChannel chan)
    {
        Checks.notNull(chan, "Other VoiceChannel");
        if (this == chan)
            return 0;
        Checks.check(getGuild().equals(chan.getGuild()), "Cannot compare VoiceChannels that aren't from the same guild!");
        if (this.getPositionRaw() == chan.getPositionRaw())
            return Long.compare(id, chan.getIdLong());
        return Integer.compare(rawPosition, chan.getPositionRaw());
    }

    // -- Setters --

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

    public TLongObjectMap<Member> getConnectedMembersMap()
    {
        return connectedMembers;
    }
}
