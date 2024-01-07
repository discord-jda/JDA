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

import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.ThreadMember;
import net.dv8tion.jda.api.entities.channel.ChannelFlag;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer;
import net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.entities.channel.unions.IThreadContainerUnion;
import net.dv8tion.jda.api.managers.channel.concrete.ThreadChannelManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;
import net.dv8tion.jda.api.requests.restaction.pagination.ThreadMemberPaginationAction;
import net.dv8tion.jda.api.utils.TimeUtil;
import net.dv8tion.jda.api.utils.cache.CacheView;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.channel.middleman.AbstractGuildChannelImpl;
import net.dv8tion.jda.internal.entities.channel.mixin.attribute.ISlowmodeChannelMixin;
import net.dv8tion.jda.internal.entities.channel.mixin.middleman.GuildMessageChannelMixin;
import net.dv8tion.jda.internal.managers.channel.concrete.ThreadChannelManagerImpl;
import net.dv8tion.jda.internal.requests.DeferredRestAction;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.restaction.pagination.ThreadMemberPaginationActionImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.LongStream;

public class ThreadChannelImpl extends AbstractGuildChannelImpl<ThreadChannelImpl> implements
        ThreadChannel,
        GuildMessageChannelMixin<ThreadChannelImpl>,
        ISlowmodeChannelMixin<ThreadChannelImpl>
{
    private final ChannelType type;
    private final CacheView.SimpleCacheView<ThreadMember> threadMembers = new CacheView.SimpleCacheView<>(ThreadMember.class, null);

    private TLongSet appliedTags = new TLongHashSet(ForumChannel.MAX_POST_TAGS);
    private AutoArchiveDuration autoArchiveDuration;
    private IThreadContainerUnion parentChannel;
    private boolean locked;
    private boolean archived;
    private boolean invitable;
    private long archiveTimestamp;
    private long creationTimestamp;
    private long ownerId;
    private long latestMessageId;
    private int messageCount;
    private int totalMessageCount;
    private int memberCount;
    private int slowmode;
    private int flags;

    public ThreadChannelImpl(long id, GuildImpl guild, ChannelType type)
    {
        super(id, guild);
        this.type = type;
    }

    @Nonnull
    @Override
    public EnumSet<ChannelFlag> getFlags()
    {
        return ChannelFlag.fromRaw(flags);
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
    public int getTotalMessageCount()
    {
        return totalMessageCount;
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
        if (type == ChannelType.GUILD_PRIVATE_THREAD && threadMembers.get(member.getIdLong()) == null)
            return member.hasPermission(getParentChannel(), Permission.MANAGE_THREADS, Permission.MESSAGE_SEND_IN_THREADS);
        return member.hasPermission(getParentChannel(), Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND_IN_THREADS);
    }

    @Nonnull
    @Override
    public List<Member> getMembers()
    {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public IThreadContainerUnion getParentChannel()
    {
        IThreadContainer realChannel = getGuild().getChannelById(IThreadContainer.class, parentChannel.getIdLong());
        if (realChannel != null)
            parentChannel = (IThreadContainerUnion) realChannel;
        return parentChannel;
    }

    @Nonnull
    @Override
    public List<ForumTag> getAppliedTags()
    {
        IThreadContainerUnion parent = getParentChannel();
        if (parent.getType() != ChannelType.FORUM)
            return Collections.emptyList();
        return parent.asForumChannel()
                .getAvailableTagCache()
                .stream()
                .filter(tag -> this.appliedTags.contains(tag.getIdLong()))
                .collect(Helpers.toUnmodifiableList());
    }

    @Nonnull
    @Override
    public RestAction<Message> retrieveParentMessage()
    {
        return this.getParentMessageChannel().retrieveMessageById(this.getIdLong());
    }

    @Nonnull
    @Override
    public RestAction<Message> retrieveStartMessage()
    {
        return retrieveMessageById(getId());
    }

    @Nonnull
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

    @Nonnull
    @Override
    public CacheRestAction<ThreadMember> retrieveThreadMemberById(long id)
    {
        JDAImpl jda = (JDAImpl) getJDA();
        return new DeferredRestAction<>(jda, ThreadMember.class,
                () -> getThreadMemberById(id),
                () -> {
                    Route.CompiledRoute route = Route.Channels.GET_THREAD_MEMBER.compile(getId(), Long.toUnsignedString(id)).withQueryParams("with_member", "true");
                    return new RestActionImpl<>(jda, route, (resp, req) ->
                        jda.getEntityBuilder().createThreadMember(getGuild(), this, resp.getObject()));
                });
    }

    @Nonnull
    @Override
    public ThreadMemberPaginationAction retrieveThreadMembers()
    {
        return new ThreadMemberPaginationActionImpl(this);
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

    @Nonnull
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

    @Nonnull
    @Override
    public OffsetDateTime getTimeCreated()
    {
        return creationTimestamp == 0 ? TimeUtil.getTimeCreated(getIdLong()) : Helpers.toOffset(creationTimestamp);
    }

    @Override
    public int getSlowmode()
    {
        return slowmode;
    }

    @Nonnull
    @Override
    public RestAction<Void> join()
    {
        checkUnarchived();

        Route.CompiledRoute route = Route.Channels.JOIN_THREAD.compile(getId());
        return new RestActionImpl<>(api, route);
    }

    @Nonnull
    @Override
    public RestAction<Void> leave()
    {
        checkUnarchived();

        Route.CompiledRoute route = Route.Channels.LEAVE_THREAD.compile(getId());
        return new RestActionImpl<>(api, route);
    }

    @Nonnull
    @Override
    public RestAction<Void> addThreadMemberById(long id)
    {
        checkUnarchived();
        checkInvitable();
        checkPermission(Permission.MESSAGE_SEND_IN_THREADS);

        Route.CompiledRoute route = Route.Channels.ADD_THREAD_MEMBER.compile(getId(), Long.toUnsignedString(id));
        return new RestActionImpl<>(api, route);
    }

    @Nonnull
    @Override
    public RestAction<Void> removeThreadMemberById(long id)
    {
        checkUnarchived();

        boolean privateThreadOwner = type == ChannelType.GUILD_PRIVATE_THREAD && ownerId == api.getSelfUser().getIdLong();
        if (!privateThreadOwner)
            checkPermission(Permission.MANAGE_THREADS);

        Route.CompiledRoute route = Route.Channels.REMOVE_THREAD_MEMBER.compile(getId(), Long.toUnsignedString(id));
        return new RestActionImpl<>(api, route);
    }

    @Nonnull
    @Override
    public ThreadChannelManager getManager()
    {
        return new ThreadChannelManagerImpl(this);
    }

    @Override
    public void checkCanManage()
    {
        if (isOwner())
            return;

        checkPermission(Permission.MANAGE_THREADS);
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

    public ThreadChannelImpl setParentChannel(IThreadContainer channel)
    {
        this.parentChannel = (IThreadContainerUnion) channel;
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

    public ThreadChannelImpl setCreationTimestamp(long creationTimestamp)
    {
        this.creationTimestamp = creationTimestamp;
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

    public ThreadChannelImpl setTotalMessageCount(int messageCount)
    {
        this.totalMessageCount = Math.max(messageCount, this.messageCount); // If this is 0 we use the older count
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

    public ThreadChannelImpl setAppliedTags(LongStream tags)
    {
        TLongSet set = new TLongHashSet(ForumChannel.MAX_POST_TAGS);
        tags.forEach(set::add);
        this.appliedTags = set;
        return this;
    }

    public ThreadChannelImpl setFlags(int flags)
    {
        this.flags = flags;
        return this;
    }

    public long getArchiveTimestamp()
    {
        return archiveTimestamp;
    }

    public TLongSet getAppliedTagsSet()
    {
        return appliedTags;
    }


    public int getRawFlags()
    {
        return flags;
    }

    private void checkUnarchived()
    {
        if (archived)
            throw new IllegalStateException("Cannot modify a ThreadChannel while it is archived!");
    }

    private void checkInvitable()
    {
        if (ownerId == api.getSelfUser().getIdLong()) return;

        if (!isPublic() && !isInvitable())
            checkPermission(Permission.MANAGE_THREADS);
    }
}
