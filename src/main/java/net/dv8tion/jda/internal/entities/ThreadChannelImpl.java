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

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.managers.channel.concrete.ThreadChannelManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.cache.CacheView;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.mixin.channel.middleman.GuildMessageChannelMixin;
import net.dv8tion.jda.internal.managers.channel.concrete.ThreadChannelManagerImpl;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ThreadChannelImpl extends AbstractGuildChannelImpl<ThreadChannelImpl> implements 
        ThreadChannel, 
        GuildMessageChannelMixin<ThreadChannelImpl>
{
    private final ChannelType type;
    private final CacheView.SimpleCacheView<ThreadMember> threadMembers = new CacheView.SimpleCacheView<>(ThreadMember.class, null);

    private AutoArchiveDuration autoArchiveDuration;
    private boolean locked;
    private boolean archived;
    private boolean invitable;
    private long parentChannelId;
    private long archiveTimestamp;
    private long ownerId;
    private long latestMessageId;
    private int messageCount;
    private int memberCount;
    private int slowmode;

    public ThreadChannelImpl(long id, GuildImpl guild, ChannelType type)
    {
        super(id, guild);
        this.type = type;
    }

    @Nonnull
    @Override
    public ChannelType getType()
    {
        return type;
    }

    @Override
    public long getLatestMessageIdLong()
    {
        return latestMessageId;
    }

    @Override
    public int getMessageCount()
    {
        return messageCount;
    }

    @Override
    public int getMemberCount()
    {
        return memberCount;
    }

    @Override
    public boolean isLocked()
    {
        return locked;
    }

    @Override
    public boolean canTalk(@Nonnull Member member)
    {
        Checks.notNull(member, "Member");
        return member.hasPermission(getParentChannel(), Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND_IN_THREADS);
    }

    @Nonnull
    @Override
    public List<Member> getMembers()
    {
        return null;
    }

    @Nonnull
    @Override
    @SuppressWarnings("ConstantConditions")
    public IThreadContainer getParentChannel()
    {
        return (IThreadContainer) guild.getGuildChannelById(parentChannelId);
    }

    @Override
    public IPermissionContainer getPermissionContainer()
    {
        return getParentChannel();
    }

    @Nonnull
    @Override
    public List<ThreadMember> getThreadMembers()
    {
        return threadMembers.asList();
    }

    @Nullable
    @Override
    public ThreadMember getThreadMemberById(long id)
    {
        return threadMembers.get(id);
    }

    @Override
    public RestAction<List<ThreadMember>> retrieveThreadMembers()
    {
        //TODO-threads: This interacts with GUILD_MEMBERS in some way. Need to test and document how.

        Route.CompiledRoute route = Route.Channels.LIST_THREAD_MEMBERS.compile(getId());
        return new RestActionImpl<>(getJDA(), route, (response, request) ->
        {
            EntityBuilder builder = api.getEntityBuilder();
            List<ThreadMember> threadMembers = new LinkedList<>();
            DataArray memberArr = response.getArray();

            for (int i = 0; i < memberArr.length(); i++)
            {
                final DataObject object = memberArr.getObject(i);
                //Very possible this is gonna break because we don't get user/member info with threadmembers
                //TODO revisit the @Nonnull annotations in ThreadMember due to the lack of user/member info. Might be a time to introduce RestX entities?
                threadMembers.add(builder.createThreadMember((GuildImpl) this.getGuild(), this, object));
            }
            return Collections.unmodifiableList(threadMembers);
        });
    }

    @Override
    public long getOwnerIdLong()
    {
        return ownerId;
    }

    @Override
    public boolean isArchived()
    {
        return archived;
    }

    @Override
    public boolean isInvitable()
    {
        if (type != ChannelType.GUILD_PRIVATE_THREAD)
            throw new UnsupportedOperationException("Only private threads support the concept of invitable.");

        return invitable;
    }

    @Override
    public OffsetDateTime getTimeArchiveInfoLastModified()
    {
        return Helpers.toOffset(archiveTimestamp);
    }

    @Nonnull
    @Override
    public AutoArchiveDuration getAutoArchiveDuration()
    {
        return autoArchiveDuration;
    }

    @Override
    public int getSlowmode()
    {
        return slowmode;
    }

    @Override
    public RestAction<Void> join()
    {
        checkUnarchived();

        Route.CompiledRoute route = Route.Channels.JOIN_THREAD.compile(getId());
        return new RestActionImpl<>(api, route);
    }

    @Override
    public RestAction<Void> leave()
    {
        checkUnarchived();

        Route.CompiledRoute route = Route.Channels.LEAVE_THREAD.compile(getId());
        return new RestActionImpl<>(api, route);
    }

    @Override
    public RestAction<Void> addThreadMemberById(long id)
    {
        checkUnarchived();

        Route.CompiledRoute route = Route.Channels.ADD_THREAD_MEMBER.compile(getId(), Long.toUnsignedString(id));
        return new RestActionImpl<>(api, route);
    }

    @Override
    public RestAction<Void> removeThreadMemberById(long id)
    {
        checkUnarchived();

        boolean privateThreadOwner = type == ChannelType.GUILD_PRIVATE_THREAD && ownerId == api.getSelfUser().getIdLong();
        if (!privateThreadOwner) {
            checkPermission(Permission.MANAGE_THREADS);
        }

        Route.CompiledRoute route = Route.Channels.REMOVE_THREAD_MEMBER.compile(getId(), Long.toUnsignedString(id));
        return new RestActionImpl<>(api, route);
    }

    @Nonnull
    @Override
    public ThreadChannelManager getManager()
    {
        return new ThreadChannelManagerImpl(this);
    }

    public CacheView.SimpleCacheView<ThreadMember> getThreadMemberView()
    {
        return threadMembers;
    }

    @Override
    public ThreadChannelImpl setLatestMessageIdLong(long latestMessageId)
    {
        this.latestMessageId = latestMessageId;
        return this;
    }

    public ThreadChannelImpl setAutoArchiveDuration(AutoArchiveDuration autoArchiveDuration)
    {
        this.autoArchiveDuration = autoArchiveDuration;
        return this;
    }

    public ThreadChannelImpl setParentChannelId(long parentChannelId)
    {
        this.parentChannelId = parentChannelId;
        return this;
    }

    public ThreadChannelImpl setLocked(boolean locked)
    {
        this.locked = locked;
        return this;
    }

    public ThreadChannelImpl setArchived(boolean archived)
    {
        this.archived = archived;
        return this;
    }

    public ThreadChannelImpl setInvitable(boolean invitable)
    {
        this.invitable = invitable;
        return this;
    }

    public ThreadChannelImpl setArchiveTimestamp(long archiveTimestamp)
    {
        this.archiveTimestamp = archiveTimestamp;
        return this;
    }

    public ThreadChannelImpl setOwnerId(long ownerId)
    {
        this.ownerId = ownerId;
        return this;
    }

    public ThreadChannelImpl setMessageCount(int messageCount)
    {
        this.messageCount = messageCount;
        return this;
    }

    public ThreadChannelImpl setMemberCount(int memberCount)
    {
        this.memberCount = memberCount;
        return this;
    }

    public ThreadChannelImpl setSlowmode(int slowmode)
    {
        this.slowmode = slowmode;
        return this;
    }

    public long getArchiveTimestamp()
    {
        return archiveTimestamp;
    }


    // -- Object overrides --

    @Override
    public String toString()
    {
        return "ThC:" + getName() + '(' + id + ')';
    }

    private void checkUnarchived()
    {
        if (archived) {
            throw new IllegalStateException("Cannot modify a ThreadChannel while it is archived!");
        }
    }
}
