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

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.ThreadMember;
import net.dv8tion.jda.api.entities.channel.ChannelFlag;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.entities.channel.unions.IThreadContainerUnion;
import net.dv8tion.jda.api.managers.channel.concrete.ThreadChannelManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;
import net.dv8tion.jda.api.requests.restaction.pagination.ThreadMemberPaginationAction;
import net.dv8tion.jda.api.utils.TimeUtil;
import net.dv8tion.jda.internal.entities.channel.middleman.AbstractGuildChannelImpl;
import net.dv8tion.jda.internal.entities.channel.mixin.attribute.IInteractionPermissionMixin;
import net.dv8tion.jda.internal.entities.channel.mixin.concrete.ThreadChannelMixin;
import net.dv8tion.jda.internal.entities.detached.DetachedGuildImpl;
import net.dv8tion.jda.internal.interactions.ChannelInteractionPermissions;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.List;

public class DetachedThreadChannelImpl extends AbstractGuildChannelImpl<DetachedThreadChannelImpl>
    implements
        ThreadChannel,
        ThreadChannelMixin<DetachedThreadChannelImpl>,
        IInteractionPermissionMixin<DetachedThreadChannelImpl>
{
    private final ChannelType type;
    private ChannelInteractionPermissions interactionPermissions;

    private AutoArchiveDuration autoArchiveDuration;
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

    public DetachedThreadChannelImpl(long id, DetachedGuildImpl guild, ChannelType type)
    {
        super(id, guild);
        this.type = type;
    }

    @Override
    public boolean isDetached()
    {
        return true;
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
    public IThreadContainerUnion getParentChannel()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public List<ForumTag> getAppliedTags()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public RestAction<Message> retrieveParentMessage()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public RestAction<Message> retrieveStartMessage()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public IPermissionContainer getPermissionContainer()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public List<ThreadMember> getThreadMembers()
    {
        throw detachedException();
    }

    @Nullable
    @Override
    public ThreadMember getThreadMemberById(long id)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public CacheRestAction<ThreadMember> retrieveThreadMemberById(long id)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public ThreadMemberPaginationAction retrieveThreadMembers()
    {
        throw detachedException();
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
        throw detachedException();
    }

    @Nonnull
    @Override
    public RestAction<Void> leave()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public RestAction<Void> addThreadMemberById(long id)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public RestAction<Void> removeThreadMemberById(long id)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public ThreadChannelManager getManager()
    {
        throw detachedException();
    }

    @Override
    public void checkCanManage()
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
    public DetachedThreadChannelImpl setLatestMessageIdLong(long latestMessageId)
    {
        this.latestMessageId = latestMessageId;
        return this;
    }

    @Override
    public DetachedThreadChannelImpl setAutoArchiveDuration(AutoArchiveDuration autoArchiveDuration)
    {
        this.autoArchiveDuration = autoArchiveDuration;
        return this;
    }

    @Override
    public DetachedThreadChannelImpl setLocked(boolean locked)
    {
        this.locked = locked;
        return this;
    }

    @Override
    public DetachedThreadChannelImpl setArchived(boolean archived)
    {
        this.archived = archived;
        return this;
    }

    @Override
    public DetachedThreadChannelImpl setInvitable(boolean invitable)
    {
        this.invitable = invitable;
        return this;
    }

    @Override
    public DetachedThreadChannelImpl setArchiveTimestamp(long archiveTimestamp)
    {
        this.archiveTimestamp = archiveTimestamp;
        return this;
    }

    @Override
    public DetachedThreadChannelImpl setCreationTimestamp(long creationTimestamp)
    {
        this.creationTimestamp = creationTimestamp;
        return this;
    }

    @Override
    public DetachedThreadChannelImpl setOwnerId(long ownerId)
    {
        this.ownerId = ownerId;
        return this;
    }

    @Override
    public DetachedThreadChannelImpl setMessageCount(int messageCount)
    {
        this.messageCount = messageCount;
        return this;
    }

    @Override
    public DetachedThreadChannelImpl setTotalMessageCount(int messageCount)
    {
        this.totalMessageCount = Math.max(messageCount, this.messageCount); // If this is 0 we use the older count
        return this;
    }

    @Override
    public DetachedThreadChannelImpl setMemberCount(int memberCount)
    {
        this.memberCount = memberCount;
        return this;
    }

    @Override
    public DetachedThreadChannelImpl setSlowmode(int slowmode)
    {
        this.slowmode = slowmode;
        return this;
    }

    @Override
    public DetachedThreadChannelImpl setFlags(int flags)
    {
        this.flags = flags;
        return this;
    }

    @Nonnull
    @Override
    public DetachedThreadChannelImpl setInteractionPermissions(@Nonnull ChannelInteractionPermissions interactionPermissions)
    {
        this.interactionPermissions = interactionPermissions;
        return this;
    }
}
