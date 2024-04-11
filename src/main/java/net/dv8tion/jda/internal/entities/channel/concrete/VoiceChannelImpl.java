/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.internal.entities.channel.concrete;

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.managers.channel.concrete.VoiceChannelManager;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.channel.middleman.AbstractStandardGuildChannelImpl;
import net.dv8tion.jda.internal.entities.channel.mixin.concrete.VoiceChannelMixin;
import net.dv8tion.jda.internal.managers.channel.concrete.VoiceChannelManagerImpl;
import net.dv8tion.jda.internal.requests.restaction.AuditableRestActionImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VoiceChannelImpl extends AbstractStandardGuildChannelImpl<VoiceChannelImpl> implements
        VoiceChannel,
        VoiceChannelMixin<VoiceChannelImpl>
{
    private final TLongObjectMap<Member> connectedMembers = MiscUtil.newLongMap();

    private String region;
    private String status = "";
    private long latestMessageId;
    private int bitrate;
    private int userLimit;
    private int slowmode;
    private boolean nsfw;

    public VoiceChannelImpl(long id, GuildImpl guild)
    {
        super(id, guild);
    }

    @Override
    public boolean isDetached()
    {
        return false;
    }

    @Nonnull
    @Override
    public GuildImpl getGuild()
    {
        return (GuildImpl) super.getGuild();
    }

    @Nonnull
    @Override
    public ChannelType getType()
    {
        return ChannelType.VOICE;
    }

    @Override
    public int getBitrate()
    {
        return bitrate;
    }

    @Nullable
    @Override
    public String getRegionRaw()
    {
        return region;
    }

    @Override
    public int getUserLimit()
    {
        return userLimit;
    }

    @Override
    public boolean isNSFW()
    {
        return nsfw;
    }

    @Override
    public int getSlowmode()
    {
        return slowmode;
    }

    @Override
    public long getLatestMessageIdLong()
    {
        return latestMessageId;
    }

    @Nonnull
    @Override
    public List<Member> getMembers()
    {
        return Collections.unmodifiableList(new ArrayList<>(connectedMembers.valueCollection()));
    }

    @Nonnull
    @Override
    public VoiceChannelManager getManager()
    {
        return new VoiceChannelManagerImpl(this);
    }

    @Nonnull
    @Override
    public String getStatus()
    {
        return status;
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> modifyStatus(@Nonnull String status)
    {
        Checks.notLonger(status, MAX_STATUS_LENGTH, "Voice Status");
        checkCanAccess();
        if (this.equals(getGuild().getSelfMember().getVoiceState().getChannel()))
            checkPermission(Permission.VOICE_SET_STATUS);
        else
            checkCanManage();

        Route.CompiledRoute route = Route.Channels.SET_STATUS.compile(getId());
        DataObject body = DataObject.empty().put("status", status);
        return new AuditableRestActionImpl<>(api, route, body);
    }

    @Override
    public TLongObjectMap<Member> getConnectedMembersMap()
    {
        return connectedMembers;
    }

    @Override
    public VoiceChannelImpl setBitrate(int bitrate)
    {
        this.bitrate = bitrate;
        return this;
    }

    @Override
    public VoiceChannelImpl setRegion(String region)
    {
        this.region = region;
        return this;
    }

    @Override
    public VoiceChannelImpl setUserLimit(int userLimit)
    {
        this.userLimit = userLimit;
        return this;
    }

    @Override
    public VoiceChannelImpl setNSFW(boolean nsfw)
    {
        this.nsfw = nsfw;
        return this;
    }

    @Override
    public VoiceChannelImpl setSlowmode(int slowmode)
    {
        this.slowmode = slowmode;
        return this;
    }

    @Override
    public VoiceChannelImpl setLatestMessageIdLong(long latestMessageId)
    {
        this.latestMessageId = latestMessageId;
        return this;
    }

    @Override
    public VoiceChannelImpl setStatus(String status)
    {
        this.status = status;
        return this;
    }
}
