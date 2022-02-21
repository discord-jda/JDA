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

package net.dv8tion.jda.internal.entities;

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.channel.concrete.StageChannelManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.requests.restaction.StageInstanceAction;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.mixin.channel.attribute.ICategorizableChannelMixin;
import net.dv8tion.jda.internal.entities.mixin.channel.attribute.IInviteContainerMixin;
import net.dv8tion.jda.internal.entities.mixin.channel.attribute.IPositionableChannelMixin;
import net.dv8tion.jda.internal.entities.mixin.channel.middleman.AudioChannelMixin;
import net.dv8tion.jda.internal.managers.channel.concrete.StageChannelManagerImpl;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.requests.restaction.StageInstanceActionImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class StageChannelImpl extends AbstractGuildChannelImpl<StageChannelImpl> implements 
        StageChannel, 
        AudioChannelMixin<StageChannelImpl>,
        ICategorizableChannelMixin<StageChannelImpl>,
        IPositionableChannelMixin<StageChannelImpl>,
        IInviteContainerMixin<StageChannelImpl>
{
    private final TLongObjectMap<Member> connectedMembers = MiscUtil.newLongMap();
    private final TLongObjectMap<PermissionOverride> overrides = MiscUtil.newLongMap();

    private StageInstance instance;
    private String region;
    private long parentCategoryId;
    private int bitrate;
    private int position;

    public StageChannelImpl(long id, GuildImpl guild)
    {
        super(id, guild);
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

    @Nullable
    @Override
    public String getRegionRaw()
    {
        return region;
    }

    @Override
    public long getParentCategoryIdLong()
    {
        return parentCategoryId;
    }

    @Override
    public int getPositionRaw()
    {
        return position;
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

    @Nonnull
    @Override
    public ChannelAction<StageChannel> createCopy(@Nonnull Guild guild)
    {
        Checks.notNull(guild, "Guild");
        //TODO-v5: .setRegion here?
        ChannelAction<StageChannel> action = guild.createStageChannel(name).setBitrate(bitrate);
        if (guild.equals(getGuild()))
        {
            Category parent = getParentCategory();
            if (parent != null)
                action.setParent(parent);
            for (PermissionOverride o : overrides.valueCollection())
            {
                if (o.isMemberOverride())
                    action.addMemberPermissionOverride(o.getIdLong(), o.getAllowedRaw(), o.getDeniedRaw());
                else
                    action.addRolePermissionOverride(o.getIdLong(), o.getAllowedRaw(), o.getDeniedRaw());
            }
        }
        return action;
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
    public TLongObjectMap<PermissionOverride> getPermissionOverrideMap()
    {
        return overrides;
    }

    @Override
    public TLongObjectMap<Member> getConnectedMembersMap()
    {
        return connectedMembers;
    }

    @Override
    public StageChannelImpl setParentCategory(long parentCategoryId)
    {
        this.parentCategoryId = parentCategoryId;
        return this;
    }

    @Override
    public StageChannelImpl setPosition(int position)
    {
        getGuild().getStageChannelsView().clearCachedLists();
        this.position = position;
        return this;
    }

    @Override
    public StageChannelImpl setBitrate(int bitrate)
    {
        this.bitrate = bitrate;
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
}
