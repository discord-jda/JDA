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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.StageInstance;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.channel.concrete.StageChannelManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.StageInstanceAction;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.channel.middleman.AbstractStandardGuildChannelImpl;
import net.dv8tion.jda.internal.entities.channel.mixin.concrete.StageChannelMixin;
import net.dv8tion.jda.internal.managers.channel.concrete.StageChannelManagerImpl;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.restaction.StageInstanceActionImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class StageChannelImpl extends AbstractStandardGuildChannelImpl<StageChannelImpl> implements
        StageChannel,
        StageChannelMixin<StageChannelImpl>
{
    private final TLongObjectMap<Member> connectedMembers = MiscUtil.newLongMap();

    private StageInstance instance;
    private String region;
    private int bitrate;
    private int userlimit;
    private int slowmode;
    private boolean ageRestricted;
    private long latestMessageId;

    public StageChannelImpl(long id, GuildImpl guild)
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
        return instance;
    }

    @Nonnull
    @Override
    public List<Member> getMembers()
    {
        return Collections.unmodifiableList(new ArrayList<>(connectedMembers.valueCollection()));
    }

    @Nonnull
    @Override
    public StageInstanceAction createStageInstance(@Nonnull String topic)
    {
        EnumSet<Permission> permissions = getGuild().getSelfMember().getPermissions(this);
        EnumSet<Permission> required = EnumSet.of(Permission.MANAGE_CHANNEL, Permission.VOICE_MUTE_OTHERS, Permission.VOICE_MOVE_OTHERS);
        for (Permission perm : required)
        {
            if (!permissions.contains(perm))
                throw new InsufficientPermissionException(this, perm, "You must be a stage moderator to create a stage instance! Missing Permission: " + perm);
        }

        return new StageInstanceActionImpl(this).setTopic(topic);
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
        return new StageChannelManagerImpl(this);
    }

    @Nonnull
    @Override
    public RestAction<Void> requestToSpeak()
    {
        Guild guild = getGuild();
        Route.CompiledRoute route = Route.Guilds.UPDATE_VOICE_STATE.compile(guild.getId(), "@me");
        DataObject body = DataObject.empty().put("channel_id", getId());
        // Stage moderators can bypass the request queue by just unsuppressing
        if (guild.getSelfMember().hasPermission(this, Permission.VOICE_MUTE_OTHERS))
            body.putNull("request_to_speak_timestamp").put("suppress", false);
        else
            body.put("request_to_speak_timestamp", OffsetDateTime.now().toString());

        if (!this.equals(guild.getSelfMember().getVoiceState().getChannel()))
            throw new IllegalStateException("Cannot request to speak without being connected to the stage channel!");
        return new RestActionImpl<>(getJDA(), route, body);
    }

    @Nonnull
    @Override
    public RestAction<Void> cancelRequestToSpeak()
    {
        Guild guild = getGuild();
        Route.CompiledRoute route = Route.Guilds.UPDATE_VOICE_STATE.compile(guild.getId(), "@me");
        DataObject body = DataObject.empty()
                .putNull("request_to_speak_timestamp")
                .put("suppress", true)
                .put("channel_id", getId());

        if (!this.equals(guild.getSelfMember().getVoiceState().getChannel()))
            throw new IllegalStateException("Cannot cancel request to speak without being connected to the stage channel!");
        return new RestActionImpl<>(getJDA(), route, body);
    }

    @Override
    public TLongObjectMap<Member> getConnectedMembersMap()
    {
        return connectedMembers;
    }

    @Override
    public StageChannelImpl setBitrate(int bitrate)
    {
        this.bitrate = bitrate;
        return this;
    }

    @Override
    public StageChannelImpl setUserLimit(int userlimit)
    {
        this.userlimit = userlimit;
        return this;
    }

    @Override
    public StageChannelImpl setRegion(String region)
    {
        this.region = region;
        return this;
    }

    public StageChannelImpl setStageInstance(StageInstance instance)
    {
        this.instance = instance;
        return this;
    }

    @Override
    public StageChannelImpl setNSFW(boolean ageRestricted)
    {
        this.ageRestricted = ageRestricted;
        return this;
    }

    @Override
    public StageChannelImpl setSlowmode(int slowmode)
    {
        this.slowmode = slowmode;
        return this;
    }

    @Override
    public StageChannelImpl setLatestMessageIdLong(long latestMessageId)
    {
        this.latestMessageId = latestMessageId;
        return this;
    }
}
