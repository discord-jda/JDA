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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.managers.ChannelManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.utils.cache.CacheView;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.utils.Helpers;
import org.jetbrains.annotations.NotNull;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class GuildThreadImpl implements GuildThread
{
    private final long id;
    private final ChannelType type;
    private final JDAImpl api;
    private final CacheView.SimpleCacheView<GuildThreadMember> threadMembers = new CacheView.SimpleCacheView<>(GuildThreadMember.class, null);

    private GuildImpl guild;
    private String name;
    private AutoArchiveDuration autoArchiveDuration;
    private boolean locked;
    private boolean archived;
    private long parentChannelId;
    private long archiveTimestamp;
    private long ownerId;
    private long lastMessageId;
    private int messageCount;
    private int memberCount;
    private int slowmode;

    public GuildThreadImpl(long id, ChannelType type, GuildImpl guild)
    {
        this.id = id;
        this.type = type;
        this.api = guild.getJDA();
        this.guild = guild;
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @NotNull
    @Override
    public String getName()
    {
        return name;
    }

    @NotNull
    @Override
    public ChannelType getType()
    {
        return type;
    }

    @NotNull
    @Override
    public JDA getJDA()
    {
        return api;
    }

    @NotNull
    @Override
    public Guild getGuild()
    {
        GuildImpl realGuild = (GuildImpl) api.getGuildById(guild.getIdLong());
        if (realGuild != null)
            guild = realGuild;
        return guild;
    }

    @NotNull
    @Override
    public ChannelManager<? extends GuildChannel> getManager()
    {
        return null;
    }

    @Override
    @Nonnull
    public BaseGuildMessageChannel getParentChannel()
    {
        //noinspection ConstantConditions
        return (BaseGuildMessageChannel) guild.getGuildChannelById(parentChannelId);
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
    @Nullable
    public List<GuildThreadMember> getThreadMembers()
    {
        return threadMembers.asList();
    }

    @Override
    public GuildThreadMember getThreadMemberById(long id)
    {
        return threadMembers.get(id);
    }

    @Override
    public long getOwnerIdLong()
    {
        return ownerId;
    }

    @Override
    public boolean isLocked()
    {
        return locked;
    }

    @Override
    public boolean isArchived()
    {
        return archived;
    }

    @Override
    public OffsetDateTime getTimeArchive()
    {
        return Helpers.toOffset(archiveTimestamp);
    }

    @Override
    @Nonnull
    public AutoArchiveDuration getAutoArchiveDuration()
    {
        return autoArchiveDuration;
    }

    @Override
    public int getSlowmode()
    {
        return slowmode;
    }

    @NotNull
    @Override
    public List<Member> getMembers()
    {
        return threadMembers.stream().map(GuildThreadMember::getMember).collect(Collectors.toList());
    }

    @Override
    public long getLatestMessageIdLong()
    {
        return lastMessageId;
    }

    @NotNull
    @Override
    public AuditableRestAction<Void> delete()
    {
        return null;
    }

    @Override
    public boolean canTalk()
    {
        return false;
    }

    @Override
    public boolean canTalk(@NotNull Member member)
    {
        return false;
    }

    @NotNull
    @Override
    public RestAction<Void> removeReactionById(@NotNull String messageId, @NotNull String unicode, @NotNull User user)
    {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public RestAction<Void> deleteMessagesByIds(@NotNull Collection<String> messageIds)
    {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public RestAction<Void> clearReactionsById(@NotNull String messageId)
    {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public RestAction<Void> clearReactionsById(@NotNull String messageId, @NotNull String unicode)
    {
        throw new NotImplementedException();
    }

    //TODO-Threads: CompareTo doesn't really make sense here given that there is no sorting of threads. What do?
    @Override
    public int compareTo(@NotNull GuildChannel o)
    {
        return 0;
    }

    // ====== Non-API getters =====

    public long getArchiveTimestamp()
    {
        return archiveTimestamp;
    }

    // ====== Setters ======

    public GuildThreadImpl setName(String name)
    {
        this.name = name;
        return this;
    }

    public GuildThreadImpl setAutoArchiveDuration(AutoArchiveDuration autoArchiveDuration)
    {
        this.autoArchiveDuration = autoArchiveDuration;
        return this;
    }

    public GuildThreadImpl setParentChannelId(long parentChannelId)
    {
        this.parentChannelId = parentChannelId;
        return this;
    }

    public GuildThreadImpl setLocked(boolean locked)
    {
        this.locked = locked;
        return this;
    }

    public GuildThreadImpl setArchived(boolean archived)
    {
        this.archived = archived;
        return this;
    }

    public GuildThreadImpl setArchiveTimestamp(long archiveTimestamp)
    {
        this.archiveTimestamp = archiveTimestamp;
        return this;
    }

    public GuildThreadImpl setOwnerId(long ownerId)
    {
        this.ownerId = ownerId;
        return this;
    }

    public GuildThreadImpl setMessageCount(int messageCount)
    {
        this.messageCount = messageCount;
        return this;
    }

    public GuildThreadImpl setMemberCount(int memberCount)
    {
        this.memberCount = memberCount;
        return this;
    }

    public GuildThreadImpl setLastMessageId(long lastMessageId)
    {
        this.lastMessageId = lastMessageId;
        return this;
    }

    public GuildThreadImpl setSlowmode(int slowmode)
    {
        this.slowmode = slowmode;
        return this;
    }
}
