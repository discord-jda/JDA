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

package net.dv8tion.jda.api.entities.channel.concrete;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelField;
import net.dv8tion.jda.api.entities.channel.ChannelFlag;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.attribute.IMemberContainer;
import net.dv8tion.jda.api.entities.channel.attribute.ISlowmodeChannel;
import net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion;
import net.dv8tion.jda.api.entities.channel.unions.IThreadContainerUnion;
import net.dv8tion.jda.api.managers.channel.concrete.ThreadChannelManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;
import net.dv8tion.jda.api.requests.restaction.pagination.ThreadMemberPaginationAction;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.FormattableFlags;
import java.util.Formatter;
import java.util.List;

/**
 * Represents Discord Message Threads of all kinds.
 * <br>These are also referred to as "posts" in the context of {@link ForumChannel Forum Channels}.
 *
 * <p>This includes all thread channel types, namely:
 * <ul>
 *     <li>{@link ChannelType#GUILD_PUBLIC_THREAD}</li>
 *     <li>{@link ChannelType#GUILD_PRIVATE_THREAD}</li>
 *     <li>{@link ChannelType#GUILD_NEWS_THREAD}</li>
 * </ul>
 *
 * <p>When a thread channel is {@link #isArchived() archived}, no new members can be added.
 * You can use the {@link #getManager() manager} to {@link ThreadChannelManager#setArchived(boolean) unarchive} the thread.
 *
 * @see Guild#getThreadChannels()
 * @see Guild#getThreadChannelById(long)
 * @see Guild#getThreadChannelCache()
 */
public interface ThreadChannel extends GuildMessageChannel, IMemberContainer, ISlowmodeChannel
{
    /**
     * Whether this thread is public or not.
     *
     * <p>Public threads can be read and joined by anyone with read access to its {@link net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer parent channel}.
     *
     * @return true if this thread is public, false otherwise.
     */
    default boolean isPublic()
    {
        ChannelType type = getType();
        return type == ChannelType.GUILD_PUBLIC_THREAD || type == ChannelType.GUILD_NEWS_THREAD;
    }

    /**
     * Gets the current number of messages present in this thread.
     * <br>Threads started from seed messages in the {@link net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer parent channel} will not count that seed message.
     * <br>This will be capped at 50 for threads created before <b>July 1, 2022</b>.
     *
     * @return The number of messages sent in this thread
     */
    int getMessageCount();

    /**
     * The total number of messages sent in this thread, including all deleted messages.
     * <br>This might be inaccurate for threads created before <b>July 1, 2022</b>.
     *
     * @return The total number of messages ever sent in this thread
     */
    int getTotalMessageCount();

    /**
     * Gets the current number of members that have joined this thread.
     * <br>This is capped at 50, meaning any additional members will not affect this count.
     *
     * @return The number of members that have joined this thread, capping at 50.
     */
    int getMemberCount();

    /**
     * Whether the currently logged in member has joined this thread.
     *
     * @return true if the self member has joined this thread, false otherwise.
     */
    default boolean isJoined()
    {
        return getSelfThreadMember() != null;
    }

    /**
     * Whether this thread is locked or not.
     *
     * <p>Locked threads cannot have new messages posted to them, or members join or leave them.
     * Threads can only be locked and unlocked by moderators.
     *
     * @return true if this thread is locked, false otherwise.
     *
     * @see    ChannelField#LOCKED
     */
    boolean isLocked();

    /**
     * Whether this thread is invitable.
     * <br>
     * A thread that is invitable can have non-moderators invite other non-moderators to it.
     * A thread that is not invitable can only have moderators invite others to it.
     *
     * <p>This property is exclusive to private threads.
     *
     * @throws UnsupportedOperationException
     *         If this thread is not a private thread.
     *
     * @return true if this thread is invitable, false otherwise.
     *
     * @see    ChannelField#INVITABLE
     */
    boolean isInvitable();

    /**
     * Whether this thread is a pinned forum post.
     *
     * @return True, if this is a pinned forum post.
     */
    default boolean isPinned()
    {
        return getFlags().contains(ChannelFlag.PINNED);
    }

    /**
     * Gets the {@link IThreadContainer parent channel} of this thread.
     *
     * @return The parent channel of this thread.
     *
     * @see    IThreadContainer#getThreadChannels()
     */
    @Nonnull
    IThreadContainerUnion getParentChannel();

    /**
     * Gets the {@link GuildMessageChannelUnion parent channel} of this thread, if it is a {@link TextChannel}, {@link NewsChannel}, or {@link VoiceChannel}.
     * <br>This is a convenience method that will perform the cast if possible, throwing otherwise.
     *
     * @throws UnsupportedOperationException
     *         If the parent channel is not a {@link GuildMessageChannel}.
     *
     * @return The parent channel of this thread, as a {@link GuildMessageChannelUnion}.
     */
    @Nonnull
    default GuildMessageChannelUnion getParentMessageChannel()
    {
        if (getParentChannel() instanceof GuildMessageChannel)
            return (GuildMessageChannelUnion) getParentChannel();

        throw new UnsupportedOperationException("Parent of this thread is not a MessageChannel. Parent: " + getParentChannel());
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.channel.forums.ForumTag forum tags} applied to this thread.
     * <br>This will be an empty list if the thread was not created in a {@link net.dv8tion.jda.api.entities.channel.concrete.ForumChannel ForumChannel}.
     *
     * @return Immutable {@link List} of {@link net.dv8tion.jda.api.entities.channel.forums.ForumTag ForumTags} applied to this post
     */
    @Nonnull
    List<ForumTag> getAppliedTags();

    /**
     * Attempts to get the {@link net.dv8tion.jda.api.entities.Message Message} that this thread was started from.
     * <br>The parent message was posted in the {@link #getParentChannel() parent channel} and a thread was started on it.
     *
     * <p>The {@link Message#getMember() Message.getMember()} method will always return null for the resulting message.
     * To retrieve the member you can use {@code getGuild().retrieveMember(message.getAuthor())}.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}
     *         in the {@link GuildMessageChannel GuildMessageChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The message has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the parent channel was deleted.</li>
     * </ul>
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel GuildMessageChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     * @throws UnsupportedOperationException
     *         If the parent channel is not a {@link GuildMessageChannel GuildMessageChannel}.
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: Message
     *         <br>The Message that started this thread
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Message> retrieveParentMessage();

    /**
     * Attempts to get the {@link net.dv8tion.jda.api.entities.Message Message} that was posted when this thread was created.
     * <br>Unlike {@link #retrieveParentMessage()}, the message was posted only inside the thread channel.
     * This is common for {@link ForumChannel} posts.
     *
     * <p>The {@link Message#getMember() Message.getMember()} method will always return null for the resulting message.
     * To retrieve the member you can use {@code getGuild().retrieveMember(message.getAuthor())}.
     *
     * <p>This is equivalent to {@code channel.retrieveMessageById(channel.getId())}.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}
     *         in the {@link GuildMessageChannel GuildMessageChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The message has already been deleted or there was no starting message.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the parent channel was deleted.</li>
     * </ul>
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel GuildMessageChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: Message
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Message> retrieveStartMessage();

    /**
     * Gets the self member, as a member of this thread.
     *
     * <p>If the current account is not a member of this thread, this will return null.
     *
     * @return The self member of this thread, null if the current account is not a member of this thread.
     *
     * @see    #isJoined()
     */
    @Nullable
    default ThreadMember getSelfThreadMember()
    {
        return getThreadMember(getJDA().getSelfUser());
    }


    /**
     * Gets a List of all cached {@link ThreadMember members} of this thread.
     *
     * <p>The thread owner is not included in this list, unless the current account is the owner.
     * Any updates to this cache are lost when JDA is shutdown, and this list is not sent to JDA on startup.
     * For this reason, {@link #retrieveThreadMembers()} should be used instead in most cases.
     *
     * <p>The cache this method relies on is empty until JDA sees a member join via a {@link net.dv8tion.jda.api.events.thread.member.ThreadMemberJoinEvent}.
     * <br>If the current account is a member of this ThreadChannel, this cache will contain the current account, even after a restart.
     * <br>In order for this cache to be updated, the following requirements must be met:
     * <ul>
     *     <li>the {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MEMBERS} intent must be enabled.</li>
     *     <li>the bot must be able to join the thread (either via the {@link net.dv8tion.jda.api.Permission#MANAGE_THREADS MANAGE_THREADS} permission, or a public thread)</li>
     *     <li>the bot must have be online to receive the update</li>
     * </ul>
     *
     * @return List of all {@link ThreadMember members} of this thread. This list may be empty, but not null.
     *
     * @see    #retrieveThreadMembers()
     */
    @Nonnull
    List<ThreadMember> getThreadMembers();

    /**
     * Gets a {@link ThreadMember} of this thread by their {@link Member}.
     *
     * <p>Note that this operation relies on the {@link #getThreadMembers() ThreadMember cache} for this ThreadChannel.
     * As the cache is likely to be unpopulated, this method is likely to return null.
     *
     * <p>Use of {@link #retrieveThreadMember(Member)} is preferred instead, once it is released.
     *
     * @param  member
     *         The member to get the {@link ThreadMember} for.
     *
     * @throws IllegalArgumentException
     *         If the given member is null.
     *
     * @return The {@link ThreadMember} of this thread for the given member.
     *
     * @see    #retrieveThreadMember(Member)
     */
    @Nullable
    default ThreadMember getThreadMember(@Nonnull Member member)
    {
        Checks.notNull(member, "Member");
        return getThreadMemberById(member.getId());
    }

    /**
     * Gets a {@link ThreadMember} of this thread by their {@link Member}.
     *
     * <p>Note that this operation relies on the {@link #getThreadMembers() ThreadMember cache} for this ThreadChannel.
     * As the cache is likely to be unpopulated, this method is likely to return null.
     *
     * <p>Use of {@link #retrieveThreadMember(Member)} is preferred instead, once it is released.
     *
     * @param   user
     *         The user to get the {@link ThreadMember} for.
     *
     * @throws IllegalArgumentException
     *         If the given user is null.
     *
     * @return The {@link ThreadMember} of this thread for the given member.
     *
     * @see    #retrieveThreadMember(Member)
     */
    @Nullable
    default ThreadMember getThreadMember(@Nonnull User user)
    {
        Checks.notNull(user, "User");
        return getThreadMemberById(user.getId());
    }

    /**
     * Gets a {@link ThreadMember} of this thread by their {@link Member}.
     *
     * <p>Note that this operation relies on the {@link #getThreadMembers() ThreadMember cache} for this ThreadChannel.
     * As the cache is likely to be unpopulated, this method is likely to return null.
     *
     * <p>Use of {@link #retrieveThreadMember(Member)} is preferred instead, once it is released.
     *
     * @param id
     *        The ID of the member to get the {@link ThreadMember} for.
     *
     * @throws IllegalArgumentException
     *         If the given id is null or empty.
     *
     * @return The {@link ThreadMember} of this thread for the given member.
     *
     * @see    #retrieveThreadMember(Member)
     */
    @Nullable
    default ThreadMember getThreadMemberById(@Nonnull String id)
    {
        return getThreadMemberById(MiscUtil.parseSnowflake(id));
    }

    /**
     * Gets a {@link ThreadMember} of this thread by their {@link Member}.
     *
     * <p>Note that this operation relies on the {@link #getThreadMembers() ThreadMember cache} for this ThreadChannel.
     * As the cache is likely to be unpopulated, this method is likely to return null.
     *
     * <p>Use of {@link #retrieveThreadMember(Member)} is preferred instead, once it is released.
     *
     * @param id
     *        The member to get the {@link ThreadMember} for.
     *
     * @return The {@link ThreadMember} of this thread for the given member.
     *
     * @see    #retrieveThreadMember(Member)
     */
    @Nullable
    ThreadMember getThreadMemberById(long id);

    /**
     * Load the thread-member for the specified user.
     * <br>If the thread-member is already loaded it, will be retrieved from {@link #getThreadMemberById(long)}
     * and immediately provided if the thread-member information is consistent. If the bot hasn't joined the thread,
     * {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MEMBERS GatewayIntent.GUILD_MEMBERS} is required to keep the cache updated.
     *
     * @param  member
     *         The member to load the thread-member from
     *
     * @throws IllegalArgumentException
     *         If provided with null
     *
     * @return {@link CacheRestAction} - Type: {@link ThreadMember}
     */
    @Nonnull
    @CheckReturnValue
    default CacheRestAction<ThreadMember> retrieveThreadMember(@Nonnull Member member)
    {
        Checks.notNull(member, "Member");
        return retrieveThreadMemberById(member.getIdLong());
    }

    /**
     * Load the thread-member for the specified user.
     * <br>If the thread-member is already loaded, it will be retrieved from {@link #getThreadMemberById(long)}
     * and immediately provided if the thread-member information is consistent. If the bot hasn't joined the thread,
     * {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MEMBERS GatewayIntent.GUILD_MEMBERS} is required to keep the cache updated.
     *
     * @param  user
     *         The user to load the thread-member from
     *
     * @throws IllegalArgumentException
     *         If provided with null
     *
     * @return {@link CacheRestAction} - Type: {@link ThreadMember}
     */
    @Nonnull
    @CheckReturnValue
    default CacheRestAction<ThreadMember> retrieveThreadMember(@Nonnull User user)
    {
        Checks.notNull(user, "User");
        return retrieveThreadMemberById(user.getIdLong());
    }

    /**
     * Load the thread-member for the user with the specified id.
     * <br>If the thread-member is already loaded, it will be retrieved from {@link #getThreadMemberById(long)}
     * and immediately provided if the thread-member information is consistent. If the bot hasn't joined the thread,
     * {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MEMBERS GatewayIntent.GUILD_MEMBERS} is required to keep the cache updated.
     *
     * @param  id
     *         The user id to load the thread-member from
     *
     * @throws IllegalArgumentException
     *         If the provided id is empty or null
     * @throws NumberFormatException
     *         If the provided id is not a snowflake
     *
     * @return {@link CacheRestAction} - Type: {@link ThreadMember}
     */
    @Nonnull
    @CheckReturnValue
    default CacheRestAction<ThreadMember> retrieveThreadMemberById(@Nonnull String id)
    {
        return retrieveThreadMemberById(MiscUtil.parseSnowflake(id));
    }

    /**
     * Load the thread-member for the user with the specified id.
     * <br>If the thread-member is already loaded, it will be retrieved from {@link #getThreadMemberById(long)}
     * and immediately provided if the thread-member information is consistent. If the bot hasn't joined the thread,
     * {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MEMBERS GatewayIntent.GUILD_MEMBERS} is required to keep the cache updated.
     *
     * @param  id
     *         The user id to load the thread-member from
     *
     * @return {@link CacheRestAction} - Type: {@link ThreadMember}
     */
    @Nonnull
    @CheckReturnValue
    CacheRestAction<ThreadMember> retrieveThreadMemberById(long id);

    /**
     * Retrieves the {@link ThreadMember ThreadMembers} of this thread.
     *
     * <p>This requires the {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MEMBERS} intent to be enabled
     * in the <a href="https://discord.com/developers/applications" target="_blank">Application Dashboard</a>.
     *
     * @return {@link ThreadMemberPaginationAction}
     */
    @Nonnull
    @CheckReturnValue
    ThreadMemberPaginationAction retrieveThreadMembers();

    /**
     * Whether the current account is the owner of this thread.
     *
     * @return true if the self account is the owner of this thread, false otherwise.
     */
    default boolean isOwner()
    {
        return getJDA().getSelfUser().getIdLong() == getOwnerIdLong();
    }

    /**
     * Gets the ID of the owner of this thread as a long.
     *
     * @return the ID of the member who created this thread as a long.
     */
    long getOwnerIdLong();

    /**
     * Gets the {@link User} of the owner of this thread as a String.
     *
     * @return The {@link User} of the member who created this thread as a String.
     */
    @Nonnull
    default String getOwnerId()
    {
        return Long.toUnsignedString(getOwnerIdLong());
    }

    /**
     * Gets the {@link Member} that created and owns this thread.
     * <br>This will be null if the member is not cached,
     * and so it is recommended to {@link Guild#retrieveMemberById(long) retrieve this member from the guild}
     * using {@link #getOwnerIdLong() the owner'd ID}.
     *
     * @return The {@link Member} of the member who created this thread.
     *
     * @see    #getThreadMemberById(long)
     * @see    Guild#retrieveMemberById(long)
     */
    @Nullable
    default Member getOwner()
    {
        return getGuild().getMemberById(getOwnerIdLong());
    }

    /**
     * Gets the owner of this thread as a {@link ThreadMember}.
     * <br>This will be null if the member is not cached, and so it is recommended to retrieve the owner instead.
     *
     * <p>This method relies on the {@link #getThreadMembers()} cache,
     * and so it is recommended to {@link #retrieveThreadMemberById(long) retrieve the ThreadMember}
     * by {@link #getOwnerIdLong() their ID} instead.
     *
     * @return The owner of this thread as a {@link ThreadMember}.
     *
     * @see    #getThreadMemberById(long)
     */
    @Nullable
    default ThreadMember getOwnerThreadMember()
    {
        return getThreadMemberById(getOwnerIdLong());
    }

    /**
     * Whether this thread has been archived.
     *
     * <p>This method will consider locked channels to also be archived.
     *
     * <p>Archived threads are not deleted threads, but are considered inactive.
     * They are not shown to clients in the channels list, but can still be navigated to and read.
     * ThreadChannels may be unarchived as long as there is space for a new active thread.
     *
     * @return true if this thread has been archived, false otherwise.
     *
     * @see    #isLocked()
     * @see    ThreadChannelManager#setArchived(boolean)
     * @see    #getAutoArchiveDuration()
     * @see    ChannelField#ARCHIVED
     */
    boolean isArchived();

    /**
     * The last time the archive info of this thread was updated.
     *
     * <p>This timestamp will be updated when any of the following happen:
     * <ul>
     *     <li>The channel is archived</li>
     *     <li>The channel is unarchived</li>
     *     <li>The AUTO_ARCHIVE_DURATION is changed.</li>
     * </ul>
     *
     * @return the time of the last archive info update.
     *
     * @see    ChannelField#ARCHIVED_TIMESTAMP
     */
    @Nonnull
    OffsetDateTime getTimeArchiveInfoLastModified();

    /**
     * The inactivity timeout of this thread.
     *
     * <p>If a message is not sent within this amount of time, the thread will be automatically hidden.
     *
     * <p>A thread archived this way can be unarchived by any member.
     *
     * @return The inactivity timeframe until a thread is automatically hidden.
     *
     * @see    ChannelField#AUTO_ARCHIVE_DURATION
     */
    @Nonnull
    AutoArchiveDuration getAutoArchiveDuration();

    /**
     * The timestamp when this thread was created.
     * <br><b>This will only be valid for threads created after 2022-01-09.
     * Otherwise, this will return the timestamp of creation based on the {@link #getIdLong() thread's id.}</b>
     *
     * @return The timestamp when this thread was created
     */
    @Nonnull
    OffsetDateTime getTimeCreated();

    /**
     * Joins this thread, adding the current account to the member list of this thread.
     *
     * <p>Note that joining threads is not a requirement of getting events about the thread.
     *
     * <br>This will have no effect if the current account is already a member of this thread.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after access was lost to this ThreadChannel (either by losing access to the parent of a public ThreadChannel,
     *         or losing {@link net.dv8tion.jda.api.Permission#MANAGE_THREADS MANAGE_THREADS} to a private channel).</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @throws IllegalStateException
     *         If this thread is locked or archived.
     *
     * @return {@link RestAction}
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Void> join();

    /**
     * Leaves this thread, removing the current account from the member list of this thread.
     * <br>This will have no effect if the current account is not a member of this thread.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @throws IllegalStateException
     *         If this thread is locked or archived.
     *
     * @return {@link RestAction}
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Void> leave();

    //TODO-v5: re-document this method as permission checks are included in the impl.
    //this is probably also affected by private threads that are not invitable
    /**
     * Adds a member to this thread.
     * <br>This will have no effect if the member is already a member of this thread.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>This can be caused by any of the following:
     *     <ul>
     *         <li>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild},
     *             typically due to being kicked or removed.</li>
     *         <li>The user supplied is not a member of this ThreadChannel's {@link net.dv8tion.jda.api.entities.Guild Guild}</li>
     *         <li>The thread is not {@link #isInvitable() invitable}, and the current account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_THREADS MANAGE_THREADS} permission.</li>
     *     </ul>
     *     <br>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_USER UNKNOWN_USER}
     *     <br>The provided User ID does not belong to a user.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#INVALID_FORM_BODY INVALID_FORM_BODY}
     *     <br>The provided User ID is not a valid snowflake.</li>
     *
     * </ul>
     *
     * @param  id
     *         The id of the member to add.
     *
     * @throws IllegalStateException
     *         If this thread is locked or archived.
     *
     * @return {@link RestAction}
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Void> addThreadMemberById(long id);

    /**
     * Adds a member to this thread.
     *
     * <br>This will have no effect if the member is already a member of this thread.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>This can be caused by any of the following:
     *     <ul>
     *         <li>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild},
     *             typically due to being kicked or removed.</li>
     *         <li>The user supplied is not a member of this ThreadChannel's {@link net.dv8tion.jda.api.entities.Guild Guild}</li>
     *         <li>The thread is not {@link #isInvitable() invitable}, and the current account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_THREADS MANAGE_THREADS} permission.</li>
     *     </ul>
     *     <br>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_USER UNKNOWN_USER}
     *     <br>The provided User ID does not belong to a user.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#INVALID_FORM_BODY INVALID_FORM_BODY}
     *     <br>The provided User ID is not a valid snowflake.</li>
     * </ul>
     *
     * @param  id
     *         The id of the member to add.
     *
     * @throws IllegalStateException
     *         If this thread is locked or archived
     * @throws IllegalArgumentException
     *         If the provided id is not a valid snowflake.
     *
     * @return {@link RestAction}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> addThreadMemberById(@Nonnull String id)
    {
        return addThreadMemberById(MiscUtil.parseSnowflake(id));
    }

    /**
     * Adds a member to this thread.
     * <br>This will have no effect if the member is already a member of this thread.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>This can be caused by any of the following:
     *     <ul>
     *         <li>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild},
     *             typically due to being kicked or removed.</li>
     *         <li>The user supplied is not a member of this ThreadChannel's {@link net.dv8tion.jda.api.entities.Guild Guild}</li>
     *         <li>The thread is not {@link #isInvitable() invitable}, and the current account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_THREADS MANAGE_THREADS} permission.</li>
     *     </ul>
     *     <br>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  user
     *         The {@link User} to add.
     *
     * @throws IllegalStateException
     *         If this thread is locked or archived.
     * @throws IllegalArgumentException
     *         If the provided user is null.
     *
     * @return {@link RestAction}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> addThreadMember(@Nonnull User user)
    {
        Checks.notNull(user, "User");
        return addThreadMemberById(user.getIdLong());
    }

    /**
     * Adds a member to this thread.
     * <br>This will have no effect if the member is already a member of this thread.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>This can be caused by any of the following:
     *     <ul>
     *         <li>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild},
     *             typically due to being kicked or removed.</li>
     *         <li>The user supplied is not a member of this ThreadChannel's {@link net.dv8tion.jda.api.entities.Guild Guild}</li>
     *         <li>The thread is not {@link #isInvitable() invitable}, and the current account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_THREADS MANAGE_THREADS} permission.</li>
     *     </ul>
     *     <br>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  member
     *         The {@link Member} to add.
     *
     * @throws IllegalStateException
     *         If this thread is locked or archived.
     * @throws IllegalArgumentException
     *         If the provided member is null.
     *
     * @return {@link RestAction}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> addThreadMember(@Nonnull Member member)
    {
        Checks.notNull(member, "Member");
        return addThreadMemberById(member.getIdLong());
    }

    /**
     * Removes a member from this thread.
     *
     * <p>Removing members from threads <b>requires the {@link net.dv8tion.jda.api.Permission#MANAGE_THREADS} permission</b> <i>unless</i> the thread is private <b>and</b> owned by the current account.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or the bot losing permissions to perform this action.
     *         <br>This can also be caused if the user supplied is not a member of this ThreadChannel's {@link net.dv8tion.jda.api.entities.Guild Guild}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_USER UNKNOWN_USER}
     *     <br>The provided User ID does not belong to a user.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#INVALID_FORM_BODY INVALID_FORM_BODY}
     *     <br>The provided User ID is not a valid snowflake.</li>
     *
     * </ul>
     *
     * @param  id
     *         The id of the member to remove from this thread.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_THREADS} permission,
     *         and this is not a private thread channel this account owns.
     *
     * @return {@link RestAction}
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Void> removeThreadMemberById(long id);

    /**
     * Removes a member from this thread.
     *
     * <p>Removing members from threads <b>requires the {@link net.dv8tion.jda.api.Permission#MANAGE_THREADS} permission</b> <i>unless</i> the thread is private <b>and</b> owned by the current account.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or the bot losing permissions to perform this action.
     *         <br>This can also be caused if the user supplied is not a member of this ThreadChannel's {@link net.dv8tion.jda.api.entities.Guild Guild}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_USER UNKNOWN_USER}
     *     <br>The provided User ID does not belong to a user.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#INVALID_FORM_BODY INVALID_FORM_BODY}
     *     <br>The provided User ID is not a valid snowflake.</li>
     *
     * </ul>
     *
     * @param  id
     *         The id of the member to remove from this thread.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_THREADS} permission,
     *         and this is not a private thread channel this account owns.
     * @throws IllegalArgumentException
     *         If the provided id is not a valid snowflake.
     *
     * @return {@link RestAction}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> removeThreadMemberById(@Nonnull String id)
    {
        return removeThreadMemberById(MiscUtil.parseSnowflake(id));
    }

    /**
     * Removes a member from this thread.
     *
     * <p>Removing members from threads <b>requires the {@link net.dv8tion.jda.api.Permission#MANAGE_THREADS} permission</b> <i>unless</i> the thread is private <b>and</b> owned by the current account.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or the bot losing permissions to perform this action.
     *         <br>This can also be caused if the user supplied is not a member of this ThreadChannel's {@link net.dv8tion.jda.api.entities.Guild Guild}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     *
     * </ul>
     *
     * @param  user
     *         The user to remove from this thread.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_THREADS} permission,
     *         and this is not a private thread channel this account owns.
     * @throws IllegalArgumentException
     *         If the provided user is null.
     *
     * @return {@link RestAction}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> removeThreadMember(@Nonnull User user)
    {
        Checks.notNull(user, "User");
        return removeThreadMemberById(user.getId());
    }

    /**
     * Removes a member from this thread.
     * <p>Removing members from threads <b>requires the {@link net.dv8tion.jda.api.Permission#MANAGE_THREADS} permission</b> <i>unless</i> the thread is private <b>and</b> owned by the current account.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or the bot losing permissions to perform this action.
     *         <br>This can also be caused if the member supplied is not a member of this ThreadChannel's {@link net.dv8tion.jda.api.entities.Guild Guild}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     *
     * </ul>
     *
     * <p>Removing members from public threads or private threads this account does not own <b>requires the {@link net.dv8tion.jda.api.Permission#MANAGE_THREADS} permission</b>.
     *
     * @param member
     *        The member to remove from this thread.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_THREADS} permission, and this isn't a private thread channel this account owns.
     * @throws IllegalArgumentException
     *         If the provided member is null.
     *
     * @return {@link RestAction}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> removeThreadMember(@Nonnull Member member)
    {
        Checks.notNull(member, "Member");
        return removeThreadMemberById(member.getIdLong());
    }

    @Override
    @Nonnull
    ThreadChannelManager getManager();

    @Override
    default void formatTo(Formatter formatter, int flags, int width, int precision)
    {
        boolean leftJustified = (flags & FormattableFlags.LEFT_JUSTIFY) == FormattableFlags.LEFT_JUSTIFY;
        boolean upper = (flags & FormattableFlags.UPPERCASE) == FormattableFlags.UPPERCASE;
        boolean alt = (flags & FormattableFlags.ALTERNATE) == FormattableFlags.ALTERNATE;
        String out;

        if (alt)
            out = "#" + (upper ? getName().toUpperCase(formatter.locale()) : getName());
        else
            out = getAsMention();

        MiscUtil.appendTo(formatter, width, precision, leftJustified, out);
    }

    /**
     * The values permitted for the auto archive duration of a {@link ThreadChannel}.
     *
     * <p>This is the time before an idle thread will be automatically hidden.
     *
     * <p>Sending a message to the thread will reset the timer.
     *
     * @see ChannelField#AUTO_ARCHIVE_DURATION
     */
    enum AutoArchiveDuration
    {
        TIME_1_HOUR(60),
        TIME_24_HOURS(1440),
        TIME_3_DAYS(4320),
        TIME_1_WEEK(10080);

        private final int minutes;

        AutoArchiveDuration(int minutes)
        {
            this.minutes = minutes;
        }

        /**
         * The number of minutes before an idle thread will be automatically hidden.
         *
         * @return The number of minutes
         */
        public int getMinutes()
        {
            return minutes;
        }

        /**
         * Provides the corresponding enum constant for the provided number of minutes.
         *
         * @param  minutes
         *         The number of minutes. (must be one of the valid values)
         *
         * @throws IllegalArgumentException
         *         If the provided minutes is not a valid value.
         *
         * @return The corresponding enum constant.
         */
        @Nonnull
        public static AutoArchiveDuration fromKey(int minutes)
        {
            for (AutoArchiveDuration duration : values())
            {
                if (duration.getMinutes() == minutes)
                    return duration;
            }
            throw new IllegalArgumentException("Provided key was not recognized. Minutes: " + minutes);
        }
    }
}
