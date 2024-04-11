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

package net.dv8tion.jda.internal.entities.channel.concrete.detached;

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.StageInstance;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;
import net.dv8tion.jda.api.managers.channel.concrete.StageChannelManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.StageInstanceAction;
import net.dv8tion.jda.internal.entities.channel.middleman.AbstractStandardGuildChannelImpl;
import net.dv8tion.jda.internal.entities.channel.mixin.attribute.IInteractionPermissionMixin;
import net.dv8tion.jda.internal.entities.channel.mixin.concrete.StageChannelMixin;
import net.dv8tion.jda.internal.entities.detached.DetachedGuildImpl;
import net.dv8tion.jda.internal.interactions.ChannelInteractionPermissions;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class DetachedStageChannelImpl extends AbstractStandardGuildChannelImpl<DetachedStageChannelImpl>
    implements
        StageChannel,
        StageChannelMixin<DetachedStageChannelImpl>,
        IInteractionPermissionMixin<DetachedStageChannelImpl>
{
    private ChannelInteractionPermissions interactionPermissions;

    private String region;
    private int bitrate;
    private int userlimit;
    private int slowmode;
    private boolean ageRestricted;
    private long latestMessageId;

    public DetachedStageChannelImpl(long id, DetachedGuildImpl guild)
    {
        super(id, guild);
    }

    @Override
    public boolean isDetached()
    {
        return true;
    }
    
    @Nonnull
    @Override
    public ChannelType getType()
    {
        return ChannelType.STAGE;
    }

    @Override
    public int getBitrate()
    {
        return bitrate;
    }

    @Override
    public int getUserLimit()
    {
        return userlimit;
    }

    @Nullable
    @Override
    public String getRegionRaw()
    {
        return region;
    }

    @Nullable
    @Override
    public StageInstance getStageInstance()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public List<Member> getMembers()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public StageInstanceAction createStageInstance(@Nonnull String topic)
    {
        throw detachedException();
    }

    @Override
    public int getSlowmode()
    {
        return slowmode;
    }

    @Override
    public boolean isNSFW()
    {
        return ageRestricted;
    }

    @Override
    public boolean canTalk(@Nonnull Member member)
    {
        Checks.notNull(member, "Member");
        return member.hasPermission(this, Permission.MESSAGE_SEND);
    }

    @Override
    public long getLatestMessageIdLong()
    {
        return latestMessageId;
    }

    @Nonnull
    @Override
    public StageChannelManager getManager()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public RestAction<Void> requestToSpeak()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public RestAction<Void> cancelRequestToSpeak()
    {
        throw detachedException();
    }

    @Override
    public TLongObjectMap<Member> getConnectedMembersMap()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public ChannelInteractionPermissions getInteractionPermissions()
    {
        return interactionPermissions;
    }

    @Override
    public DetachedStageChannelImpl setBitrate(int bitrate)
    {
        this.bitrate = bitrate;
        return this;
    }

    @Override
    public DetachedStageChannelImpl setUserLimit(int userlimit)
    {
        this.userlimit = userlimit;
        return this;
    }

    @Override
    public DetachedStageChannelImpl setRegion(String region)
    {
        this.region = region;
        return this;
    }

    @Override
    public DetachedStageChannelImpl setNSFW(boolean ageRestricted)
    {
        this.ageRestricted = ageRestricted;
        return this;
    }

    @Override
    public DetachedStageChannelImpl setSlowmode(int slowmode)
    {
        this.slowmode = slowmode;
        return this;
    }

    @Override
    public DetachedStageChannelImpl setLatestMessageIdLong(long latestMessageId)
    {
        this.latestMessageId = latestMessageId;
        return this;
    }

    @Nonnull
    @Override
    public DetachedStageChannelImpl setInteractionPermissions(@Nonnull ChannelInteractionPermissions interactionPermissions)
    {
        this.interactionPermissions = interactionPermissions;
        return this;
    }
}
