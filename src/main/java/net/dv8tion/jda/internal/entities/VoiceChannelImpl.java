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
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.managers.channel.concrete.VoiceChannelManager;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.internal.entities.mixin.channel.attribute.ICategorizableChannelMixin;
import net.dv8tion.jda.internal.entities.mixin.channel.attribute.IInviteContainerMixin;
import net.dv8tion.jda.internal.entities.mixin.channel.attribute.IPositionableChannelMixin;
import net.dv8tion.jda.internal.entities.mixin.channel.middleman.AudioChannelMixin;
import net.dv8tion.jda.internal.managers.channel.concrete.VoiceChannelManagerImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VoiceChannelImpl extends AbstractGuildChannelImpl<VoiceChannelImpl> implements
        VoiceChannel,
        AudioChannelMixin<VoiceChannelImpl>,
        ICategorizableChannelMixin<VoiceChannelImpl>,
        IPositionableChannelMixin<VoiceChannelImpl>,
        IInviteContainerMixin<VoiceChannelImpl>
{
    private final TLongObjectMap<Member> connectedMembers = MiscUtil.newLongMap();
    private final TLongObjectMap<PermissionOverride> overrides = MiscUtil.newLongMap();

    private String region;
    private long parentCategoryId;
    private int bitrate;
    private int position;
    private int userLimit;

    public VoiceChannelImpl(long id, GuildImpl guild)
    {
        super(id, guild);
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
    public long getParentCategoryIdLong()
    {
        return parentCategoryId;
    }

    @Override
    public int getPositionRaw()
    {
        return position;
    }

    @Override
    public int getUserLimit()
    {
        return userLimit;
    }

    @Nonnull
    @Override
    public List<Member> getMembers()
    {
        return Collections.unmodifiableList(new ArrayList<>(connectedMembers.valueCollection()));
    }

    @Nonnull
    @Override
    public ChannelAction<VoiceChannel> createCopy(@Nonnull Guild guild)
    {
        Checks.notNull(guild, "Guild");
        //TODO-v5: .setRegion here?
        ChannelAction<VoiceChannel> action = guild.createVoiceChannel(name).setBitrate(bitrate).setUserlimit(userLimit);
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
    public VoiceChannelManager getManager()
    {
        return new VoiceChannelManagerImpl(this);
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
    public VoiceChannelImpl setParentCategory(long parentCategoryId)
    {
        this.parentCategoryId = parentCategoryId;
        return this;
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
    public VoiceChannelImpl setPosition(int position)
    {
        getGuild().getVoiceChannelsView().clearCachedLists();
        this.position = position;
        return this;
    }

    public VoiceChannelImpl setUserLimit(int userLimit)
    {
        this.userLimit = userLimit;
        return this;
    }
}
