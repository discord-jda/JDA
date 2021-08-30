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
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.requests.restaction.StageInstanceAction;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.internal.requests.restaction.StageInstanceActionImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class StageChannelImpl extends AbstractChannelImpl<StageChannel, StageChannelImpl> implements StageChannel
{
    private final TLongObjectMap<Member> connectedMembers = MiscUtil.newLongMap();
    private int bitrate;
    private String region;
    private StageInstance instance;

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

    @Nonnull
    @Override
    public List<Member> getMembers()
    {
        return Collections.unmodifiableList(new ArrayList<>(getConnectedMembersMap().valueCollection()));
    }

    @Nullable
    @Override
    public StageInstance getStageInstance()
    {
        return instance;
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

    public StageChannelImpl setBitrate(int bitrate)
    {
        this.bitrate = bitrate;
        return this;
    }

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

    public TLongObjectMap<Member> getConnectedMembersMap()
    {
        connectedMembers.transformValues((member) -> {
            // Load real member instance from cache to provided up-to-date cache information
            Member real = getGuild().getMemberById(member.getIdLong());
            return real != null ? real : member;
        });
        return connectedMembers;
    }
}
