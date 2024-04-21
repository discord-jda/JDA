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
package net.dv8tion.jda.api.entities.channel.concrete

import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.ChannelFlag
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.attribute.IMemberContainer
import net.dv8tion.jda.api.entities.channel.attribute.ISlowmodeChannel
import net.dv8tion.jda.api.entities.channel.forums.ForumTag
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion
import net.dv8tion.jda.api.entities.channel.unions.IThreadContainerUnion
import net.dv8tion.jda.api.managers.channel.ChannelManager
import net.dv8tion.jda.api.managers.channel.concrete.ThreadChannelManager
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.CacheRestAction
import net.dv8tion.jda.api.requests.restaction.pagination.ThreadMemberPaginationAction
import net.dv8tion.jda.api.utils.MiscUtil
import net.dv8tion.jda.internal.utils.Checks
import java.time.OffsetDateTime
import java.util.*
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Represents Discord Message Threads of all kinds.
 * <br></br>These are also referred to as "posts" in the context of [Forum Channels][ForumChannel].
 *
 *
 * This includes all thread channel types, namely:
 *
 *  * [ChannelType.GUILD_PUBLIC_THREAD]
 *  * [ChannelType.GUILD_PRIVATE_THREAD]
 *  * [ChannelType.GUILD_NEWS_THREAD]
 *
 *
 *
 * When a thread channel is [archived][.isArchived], no new members can be added.
 * You can use the [manager][.getManager] to [unarchive][ThreadChannelManager.setArchived] the thread.
 *
 * @see Guild.getThreadChannels
 * @see Guild.getThreadChannelById
 * @see Guild.getThreadChannelCache
 */
interface ThreadChannel : GuildMessageChannel, IMemberContainer, ISlowmodeChannel {
    val isPublic: Boolean
        /**
         * Whether this thread is public or not.
         *
         *
         * Public threads can be read and joined by anyone with read access to its [parent channel][net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer].
         *
         * @return true if this thread is public, false otherwise.
         */
        get() {
            val type = getType()
            return type == ChannelType.GUILD_PUBLIC_THREAD || type == ChannelType.GUILD_NEWS_THREAD
        }

    /**
     * Gets the current number of messages present in this thread.
     * <br></br>Threads started from seed messages in the [parent channel][net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer] will not count that seed message.
     * <br></br>This will be capped at 50 for threads created before **July 1, 2022**.
     *
     * @return The number of messages sent in this thread
     */
    val messageCount: Int

    /**
     * The total number of messages sent in this thread, including all deleted messages.
     * <br></br>This might be inaccurate for threads created before **July 1, 2022**.
     *
     * @return The total number of messages ever sent in this thread
     */
    @JvmField
    val totalMessageCount: Int

    /**
     * Gets the current number of members that have joined this thread.
     * <br></br>This is capped at 50, meaning any additional members will not affect this count.
     *
     * @return The number of members that have joined this thread, capping at 50.
     */
    val memberCount: Int
    val isJoined: Boolean
        /**
         * Whether the currently logged in member has joined this thread.
         *
         * @return true if the self member has joined this thread, false otherwise.
         */
        get() = selfThreadMember != null

    /**
     * Whether this thread is locked or not.
     *
     *
     * Locked threads cannot have new messages posted to them, or members join or leave them.
     * Threads can only be locked and unlocked by moderators.
     *
     * @return true if this thread is locked, false otherwise.
     *
     * @see ChannelField.LOCKED
     */
    @JvmField
    val isLocked: Boolean

    /**
     * Whether this thread is invitable.
     * <br></br>
     * A thread that is invitable can have non-moderators invite other non-moderators to it.
     * A thread that is not invitable can only have moderators invite others to it.
     *
     *
     * This property is exclusive to private threads.
     *
     * @throws UnsupportedOperationException
     * If this thread is not a private thread.
     *
     * @return true if this thread is invitable, false otherwise.
     *
     * @see ChannelField.INVITABLE
     */
    @JvmField
    val isInvitable: Boolean
    val isPinned: Boolean
        /**
         * Whether this thread is a pinned forum post.
         *
         * @return True, if this is a pinned forum post.
         */
        get() = flags.contains(ChannelFlag.PINNED)

    @JvmField
    @get:Nonnull
    val parentChannel: IThreadContainerUnion

    @get:Nonnull
    val parentMessageChannel: GuildMessageChannelUnion?
        /**
         * Gets the [parent channel][GuildMessageChannelUnion] of this thread, if it is a [TextChannel], [NewsChannel], or [VoiceChannel].
         * <br></br>This is a convenience method that will perform the cast if possible, throwing otherwise.
         *
         * @throws UnsupportedOperationException
         * If the parent channel is not a [GuildMessageChannel].
         *
         * @return The parent channel of this thread, as a [GuildMessageChannelUnion].
         */
        get() {
            if (parentChannel is GuildMessageChannel) return parentChannel as GuildMessageChannelUnion
            throw UnsupportedOperationException("Parent of this thread is not a MessageChannel. Parent: " + parentChannel)
        }

    @JvmField
    @get:Nonnull
    val appliedTags: List<ForumTag?>?

    /**
     * Attempts to get the [Message][net.dv8tion.jda.api.entities.Message] that this thread was started from.
     * <br></br>The parent message was posted in the [parent channel][.getParentChannel] and a thread was started on it.
     *
     *
     * The [Message.getMember()][Message.getMember] method will always return null for the resulting message.
     * To retrieve the member you can use `getGuild().retrieveMember(message.getAuthor())`.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The request was attempted after the account lost [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY]
     * in the [GuildMessageChannel].
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>The message has already been deleted.
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The request was attempted after the parent channel was deleted.
     *
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and the logged in account does not have
     *
     *  * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     *  * [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY]
     *
     * @throws UnsupportedOperationException
     * If the parent channel is not a [GuildMessageChannel].
     *
     * @return [RestAction][net.dv8tion.jda.api.requests.RestAction] - Type: Message
     * <br></br>The Message that started this thread
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveParentMessage(): RestAction<Message?>?

    /**
     * Attempts to get the [Message][net.dv8tion.jda.api.entities.Message] that was posted when this thread was created.
     * <br></br>Unlike [.retrieveParentMessage], the message was posted only inside the thread channel.
     * This is common for [ForumChannel] posts.
     *
     *
     * The [Message.getMember()][Message.getMember] method will always return null for the resulting message.
     * To retrieve the member you can use `getGuild().retrieveMember(message.getAuthor())`.
     *
     *
     * This is equivalent to `channel.retrieveMessageById(channel.getId())`.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The request was attempted after the account lost [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY]
     * in the [GuildMessageChannel].
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>The message has already been deleted or there was no starting message.
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The request was attempted after the parent channel was deleted.
     *
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and the logged in account does not have
     *
     *  * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     *  * [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY]
     *
     *
     * @return [RestAction][net.dv8tion.jda.api.requests.RestAction] - Type: Message
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveStartMessage(): RestAction<Message?>?
    val selfThreadMember: ThreadMember?
        /**
         * Gets the self member, as a member of this thread.
         *
         *
         * If the current account is not a member of this thread, this will return null.
         *
         * @return The self member of this thread, null if the current account is not a member of this thread.
         *
         * @see .isJoined
         */
        get() = getThreadMember(getJDA().getSelfUser())

    @JvmField
    @get:Nonnull
    val threadMembers: List<ThreadMember?>?

    /**
     * Gets a [ThreadMember] of this thread by their [Member].
     *
     *
     * Note that this operation relies on the [ThreadMember cache][.getThreadMembers] for this ThreadChannel.
     * As the cache is likely to be unpopulated, this method is likely to return null.
     *
     *
     * Use of [.retrieveThreadMember] is preferred instead, once it is released.
     *
     * @param  member
     * The member to get the [ThreadMember] for.
     *
     * @throws IllegalArgumentException
     * If the given member is null.
     *
     * @return The [ThreadMember] of this thread for the given member.
     *
     * @see .retrieveThreadMember
     */
    fun getThreadMember(@Nonnull member: Member): ThreadMember? {
        Checks.notNull(member, "Member")
        return getThreadMemberById(member.id)
    }

    /**
     * Gets a [ThreadMember] of this thread by their [Member].
     *
     *
     * Note that this operation relies on the [ThreadMember cache][.getThreadMembers] for this ThreadChannel.
     * As the cache is likely to be unpopulated, this method is likely to return null.
     *
     *
     * Use of [.retrieveThreadMember] is preferred instead, once it is released.
     *
     * @param   user
     * The user to get the [ThreadMember] for.
     *
     * @throws IllegalArgumentException
     * If the given user is null.
     *
     * @return The [ThreadMember] of this thread for the given member.
     *
     * @see .retrieveThreadMember
     */
    fun getThreadMember(@Nonnull user: User): ThreadMember? {
        Checks.notNull(user, "User")
        return getThreadMemberById(user.id)
    }

    /**
     * Gets a [ThreadMember] of this thread by their [Member].
     *
     *
     * Note that this operation relies on the [ThreadMember cache][.getThreadMembers] for this ThreadChannel.
     * As the cache is likely to be unpopulated, this method is likely to return null.
     *
     *
     * Use of [.retrieveThreadMember] is preferred instead, once it is released.
     *
     * @param id
     * The ID of the member to get the [ThreadMember] for.
     *
     * @throws IllegalArgumentException
     * If the given id is null or empty.
     *
     * @return The [ThreadMember] of this thread for the given member.
     *
     * @see .retrieveThreadMember
     */
    fun getThreadMemberById(@Nonnull id: String?): ThreadMember? {
        return getThreadMemberById(MiscUtil.parseSnowflake(id))
    }

    /**
     * Gets a [ThreadMember] of this thread by their [Member].
     *
     *
     * Note that this operation relies on the [ThreadMember cache][.getThreadMembers] for this ThreadChannel.
     * As the cache is likely to be unpopulated, this method is likely to return null.
     *
     *
     * Use of [.retrieveThreadMember] is preferred instead, once it is released.
     *
     * @param id
     * The member to get the [ThreadMember] for.
     *
     * @return The [ThreadMember] of this thread for the given member.
     *
     * @see .retrieveThreadMember
     */
    fun getThreadMemberById(id: Long): ThreadMember?

    /**
     * Load the thread-member for the specified user.
     * <br></br>If the thread-member is already loaded it, will be retrieved from [.getThreadMemberById]
     * and immediately provided if the thread-member information is consistent. If the bot hasn't joined the thread,
     * [GatewayIntent.GUILD_MEMBERS][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS] is required to keep the cache updated.
     *
     * @param  member
     * The member to load the thread-member from
     *
     * @throws IllegalArgumentException
     * If provided with null
     *
     * @return [CacheRestAction] - Type: [ThreadMember]
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveThreadMember(@Nonnull member: Member): CacheRestAction<ThreadMember?>? {
        Checks.notNull(member, "Member")
        return retrieveThreadMemberById(member.idLong)
    }

    /**
     * Load the thread-member for the specified user.
     * <br></br>If the thread-member is already loaded, it will be retrieved from [.getThreadMemberById]
     * and immediately provided if the thread-member information is consistent. If the bot hasn't joined the thread,
     * [GatewayIntent.GUILD_MEMBERS][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS] is required to keep the cache updated.
     *
     * @param  user
     * The user to load the thread-member from
     *
     * @throws IllegalArgumentException
     * If provided with null
     *
     * @return [CacheRestAction] - Type: [ThreadMember]
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveThreadMember(@Nonnull user: User): CacheRestAction<ThreadMember?>? {
        Checks.notNull(user, "User")
        return retrieveThreadMemberById(user.idLong)
    }

    /**
     * Load the thread-member for the user with the specified id.
     * <br></br>If the thread-member is already loaded, it will be retrieved from [.getThreadMemberById]
     * and immediately provided if the thread-member information is consistent. If the bot hasn't joined the thread,
     * [GatewayIntent.GUILD_MEMBERS][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS] is required to keep the cache updated.
     *
     * @param  id
     * The user id to load the thread-member from
     *
     * @throws IllegalArgumentException
     * If the provided id is empty or null
     * @throws NumberFormatException
     * If the provided id is not a snowflake
     *
     * @return [CacheRestAction] - Type: [ThreadMember]
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveThreadMemberById(@Nonnull id: String?): CacheRestAction<ThreadMember?>? {
        return retrieveThreadMemberById(MiscUtil.parseSnowflake(id))
    }

    /**
     * Load the thread-member for the user with the specified id.
     * <br></br>If the thread-member is already loaded, it will be retrieved from [.getThreadMemberById]
     * and immediately provided if the thread-member information is consistent. If the bot hasn't joined the thread,
     * [GatewayIntent.GUILD_MEMBERS][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS] is required to keep the cache updated.
     *
     * @param  id
     * The user id to load the thread-member from
     *
     * @return [CacheRestAction] - Type: [ThreadMember]
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveThreadMemberById(id: Long): CacheRestAction<ThreadMember?>?

    /**
     * Retrieves the [ThreadMembers][ThreadMember] of this thread.
     *
     *
     * This requires the [net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS] intent to be enabled
     * in the [Application Dashboard](https://discord.com/developers/applications).
     *
     * @return [ThreadMemberPaginationAction]
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveThreadMembers(): ThreadMemberPaginationAction?
    val isOwner: Boolean
        /**
         * Whether the current account is the owner of this thread.
         *
         * @return true if the self account is the owner of this thread, false otherwise.
         */
        get() = getJDA().getSelfUser().getIdLong() == ownerIdLong

    /**
     * Gets the ID of the owner of this thread as a long.
     *
     * @return the ID of the member who created this thread as a long.
     */
    @JvmField
    val ownerIdLong: Long

    @get:Nonnull
    val ownerId: String?
        /**
         * Gets the [User] of the owner of this thread as a String.
         *
         * @return The [User] of the member who created this thread as a String.
         */
        get() = java.lang.Long.toUnsignedString(ownerIdLong)

    /**
     * Gets the [Member] that created and owns this thread.
     * <br></br>This will be null if the member is not cached,
     * and so it is recommended to [retrieve this member from the guild][Guild.retrieveMemberById]
     * using [the owner&#39;d ID][.getOwnerIdLong].
     *
     * @return The [Member] of the member who created this thread.
     *
     * @see .getThreadMemberById
     * @see Guild.retrieveMemberById
     */
    fun getOwner(): Member? {
        return getGuild().getMemberById(ownerIdLong)
    }

    val ownerThreadMember: ThreadMember?
        /**
         * Gets the owner of this thread as a [ThreadMember].
         * <br></br>This will be null if the member is not cached, and so it is recommended to retrieve the owner instead.
         *
         *
         * This method relies on the [.getThreadMembers] cache,
         * and so it is recommended to [retrieve the ThreadMember][.retrieveThreadMemberById]
         * by [their ID][.getOwnerIdLong] instead.
         *
         * @return The owner of this thread as a [ThreadMember].
         *
         * @see .getThreadMemberById
         */
        get() = getThreadMemberById(ownerIdLong)

    /**
     * Whether this thread has been archived.
     *
     *
     * This method will consider locked channels to also be archived.
     *
     *
     * Archived threads are not deleted threads, but are considered inactive.
     * They are not shown to clients in the channels list, but can still be navigated to and read.
     * ThreadChannels may be unarchived as long as there is space for a new active thread.
     *
     * @return true if this thread has been archived, false otherwise.
     *
     * @see .isLocked
     * @see ThreadChannelManager.setArchived
     * @see .getAutoArchiveDuration
     * @see ChannelField.ARCHIVED
     */
    @JvmField
    val isArchived: Boolean

    @JvmField
    @get:Nonnull
    val timeArchiveInfoLastModified: OffsetDateTime?

    @JvmField
    @get:Nonnull
    val autoArchiveDuration: AutoArchiveDuration?

    /**
     * The timestamp when this thread was created.
     * <br></br>**This will only be valid for threads created after 2022-01-09.
     * Otherwise, this will return the timestamp of creation based on the [thread&#39;s id.][.getIdLong]**
     *
     * @return The timestamp when this thread was created
     */
    @Nonnull
    override fun getTimeCreated(): OffsetDateTime

    /**
     * Joins this thread, adding the current account to the member list of this thread.
     *
     *
     * Note that joining threads is not a requirement of getting events about the thread.
     *
     * <br></br>This will have no effect if the current account is already a member of this thread.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after access was lost to this ThreadChannel (either by losing access to the parent of a public ThreadChannel,
     * or losing [MANAGE_THREADS][net.dv8tion.jda.api.Permission.MANAGE_THREADS] to a private channel).
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The request was attempted after the channel was deleted.
     *
     *
     * @throws IllegalStateException
     * If this thread is archived.
     *
     * @return [RestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun join(): RestAction<Void?>?

    /**
     * Leaves this thread, removing the current account from the member list of this thread.
     * <br></br>This will have no effect if the current account is not a member of this thread.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed.
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The request was attempted after the channel was deleted.
     *
     *
     * @throws IllegalStateException
     * If this thread is archived.
     *
     * @return [RestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun leave(): RestAction<Void?>?

    /**
     * Adds a member to this thread.
     * <br></br>This will have no effect if the member is already a member of this thread.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>This can be caused by any of the following:
     *
     *  * The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild],
     * typically due to being kicked or removed.
     *  * The user supplied is not a member of this ThreadChannel's [Guild][net.dv8tion.jda.api.entities.Guild]
     *  * The thread is not [invitable][.isInvitable], and the current account does not have the [MANAGE_THREADS][net.dv8tion.jda.api.Permission.MANAGE_THREADS] permission.
     *
     * <br></br>
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The request was attempted after the channel was deleted.
     *
     *  * [UNKNOWN_USER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_USER]
     * <br></br>The provided User ID does not belong to a user.
     *
     *  * [INVALID_FORM_BODY][net.dv8tion.jda.api.requests.ErrorResponse.INVALID_FORM_BODY]
     * <br></br>The provided User ID is not a valid snowflake.
     *
     *
     *
     * @param  id
     * The id of the member to add.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *
     *  * If this is a [private thread][.isPublic] or not [.isInvitable],
     * and the bot does not have [MANAGE_THREADS][net.dv8tion.jda.api.Permission.MANAGE_THREADS] permission and is not the [.getOwner].
     *  * If the bot does not have [MESSAGE_SEND_IN_THREADS][net.dv8tion.jda.api.Permission.MESSAGE_SEND_IN_THREADS] permission in the parent channel.
     *
     * @throws IllegalStateException
     * If this thread is archived.
     *
     * @return [RestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun addThreadMemberById(id: Long): RestAction<Void?>?

    /**
     * Adds a member to this thread.
     *
     * <br></br>This will have no effect if the member is already a member of this thread.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>This can be caused by any of the following:
     *
     *  * The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild],
     * typically due to being kicked or removed.
     *  * The user supplied is not a member of this ThreadChannel's [Guild][net.dv8tion.jda.api.entities.Guild]
     *  * The thread is not [invitable][.isInvitable], and the current account does not have the [MANAGE_THREADS][net.dv8tion.jda.api.Permission.MANAGE_THREADS] permission.
     *
     * <br></br>
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The request was attempted after the channel was deleted.
     *
     *  * [UNKNOWN_USER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_USER]
     * <br></br>The provided User ID does not belong to a user.
     *
     *  * [INVALID_FORM_BODY][net.dv8tion.jda.api.requests.ErrorResponse.INVALID_FORM_BODY]
     * <br></br>The provided User ID is not a valid snowflake.
     *
     *
     * @param  id
     * The id of the member to add.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *
     *  * If this is a [private thread][.isPublic] or not [.isInvitable],
     * and the bot does not have [MANAGE_THREADS][net.dv8tion.jda.api.Permission.MANAGE_THREADS] permission and is not the [.getOwner].
     *  * If the bot does not have [MESSAGE_SEND_IN_THREADS][net.dv8tion.jda.api.Permission.MESSAGE_SEND_IN_THREADS] permission in the parent channel.
     *
     * @throws IllegalStateException
     * If this thread is locked or archived
     * @throws IllegalArgumentException
     * If the provided id is not a valid snowflake.
     *
     * @return [RestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun addThreadMemberById(@Nonnull id: String?): RestAction<Void?>? {
        return addThreadMemberById(MiscUtil.parseSnowflake(id))
    }

    /**
     * Adds a member to this thread.
     * <br></br>This will have no effect if the member is already a member of this thread.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>This can be caused by any of the following:
     *
     *  * The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild],
     * typically due to being kicked or removed.
     *  * The user supplied is not a member of this ThreadChannel's [Guild][net.dv8tion.jda.api.entities.Guild]
     *  * The thread is not [invitable][.isInvitable], and the current account does not have the [MANAGE_THREADS][net.dv8tion.jda.api.Permission.MANAGE_THREADS] permission.
     *
     * <br></br>
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The request was attempted after the channel was deleted.
     *
     *
     * @param  user
     * The [User] to add.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *
     *  * If this is a [private thread][.isPublic] or not [.isInvitable],
     * and the bot does not have [MANAGE_THREADS][net.dv8tion.jda.api.Permission.MANAGE_THREADS] permission and is not the [.getOwner].
     *  * If the bot does not have [MESSAGE_SEND_IN_THREADS][net.dv8tion.jda.api.Permission.MESSAGE_SEND_IN_THREADS] permission in the parent channel.
     *
     * @throws IllegalStateException
     * If this thread is locked or archived.
     * @throws IllegalArgumentException
     * If the provided user is null.
     *
     * @return [RestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun addThreadMember(@Nonnull user: User): RestAction<Void?>? {
        Checks.notNull(user, "User")
        return addThreadMemberById(user.idLong)
    }

    /**
     * Adds a member to this thread.
     * <br></br>This will have no effect if the member is already a member of this thread.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>This can be caused by any of the following:
     *
     *  * The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild],
     * typically due to being kicked or removed.
     *  * The user supplied is not a member of this ThreadChannel's [Guild][net.dv8tion.jda.api.entities.Guild]
     *  * The thread is not [invitable][.isInvitable], and the current account does not have the [MANAGE_THREADS][net.dv8tion.jda.api.Permission.MANAGE_THREADS] permission.
     *
     * <br></br>
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The request was attempted after the channel was deleted.
     *
     *
     * @param  member
     * The [Member] to add.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *
     *  * If this is a [private thread][.isPublic] or not [.isInvitable],
     * and the bot does not have [MANAGE_THREADS][net.dv8tion.jda.api.Permission.MANAGE_THREADS] permission and is not the [.getOwner].
     *  * If the bot does not have [MESSAGE_SEND_IN_THREADS][net.dv8tion.jda.api.Permission.MESSAGE_SEND_IN_THREADS] permission in the parent channel.
     *
     * @throws IllegalStateException
     * If this thread is locked or archived.
     * @throws IllegalArgumentException
     * If the provided member is null.
     *
     * @return [RestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun addThreadMember(@Nonnull member: Member): RestAction<Void?>? {
        Checks.notNull(member, "Member")
        return addThreadMemberById(member.idLong)
    }

    /**
     * Removes a member from this thread.
     *
     *
     * Removing members from threads **requires the [net.dv8tion.jda.api.Permission.MANAGE_THREADS] permission** *unless* the thread is private **and** owned by the current account.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or the bot losing permissions to perform this action.
     * <br></br>This can also be caused if the user supplied is not a member of this ThreadChannel's [Guild][net.dv8tion.jda.api.entities.Guild]
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The request was attempted after the channel was deleted.
     *
     *  * [UNKNOWN_USER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_USER]
     * <br></br>The provided User ID does not belong to a user.
     *
     *  * [INVALID_FORM_BODY][net.dv8tion.jda.api.requests.ErrorResponse.INVALID_FORM_BODY]
     * <br></br>The provided User ID is not a valid snowflake.
     *
     *
     *
     * @param  id
     * The id of the member to remove from this thread.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the account does not have the [net.dv8tion.jda.api.Permission.MANAGE_THREADS] permission,
     * and this is not a private thread channel this account owns.
     *
     * @return [RestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun removeThreadMemberById(id: Long): RestAction<Void?>?

    /**
     * Removes a member from this thread.
     *
     *
     * Removing members from threads **requires the [net.dv8tion.jda.api.Permission.MANAGE_THREADS] permission** *unless* the thread is private **and** owned by the current account.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or the bot losing permissions to perform this action.
     * <br></br>This can also be caused if the user supplied is not a member of this ThreadChannel's [Guild][net.dv8tion.jda.api.entities.Guild]
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The request was attempted after the channel was deleted.
     *
     *  * [UNKNOWN_USER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_USER]
     * <br></br>The provided User ID does not belong to a user.
     *
     *  * [INVALID_FORM_BODY][net.dv8tion.jda.api.requests.ErrorResponse.INVALID_FORM_BODY]
     * <br></br>The provided User ID is not a valid snowflake.
     *
     *
     *
     * @param  id
     * The id of the member to remove from this thread.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the account does not have the [net.dv8tion.jda.api.Permission.MANAGE_THREADS] permission,
     * and this is not a private thread channel this account owns.
     * @throws IllegalArgumentException
     * If the provided id is not a valid snowflake.
     *
     * @return [RestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun removeThreadMemberById(@Nonnull id: String?): RestAction<Void?>? {
        return removeThreadMemberById(MiscUtil.parseSnowflake(id))
    }

    /**
     * Removes a member from this thread.
     *
     *
     * Removing members from threads **requires the [net.dv8tion.jda.api.Permission.MANAGE_THREADS] permission** *unless* the thread is private **and** owned by the current account.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or the bot losing permissions to perform this action.
     * <br></br>This can also be caused if the user supplied is not a member of this ThreadChannel's [Guild][net.dv8tion.jda.api.entities.Guild]
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The request was attempted after the channel was deleted.
     *
     *
     *
     * @param  user
     * The user to remove from this thread.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the account does not have the [net.dv8tion.jda.api.Permission.MANAGE_THREADS] permission,
     * and this is not a private thread channel this account owns.
     * @throws IllegalArgumentException
     * If the provided user is null.
     *
     * @return [RestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun removeThreadMember(@Nonnull user: User): RestAction<Void?>? {
        Checks.notNull(user, "User")
        return removeThreadMemberById(user.id)
    }

    /**
     * Removes a member from this thread.
     *
     * Removing members from threads **requires the [net.dv8tion.jda.api.Permission.MANAGE_THREADS] permission** *unless* the thread is private **and** owned by the current account.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or the bot losing permissions to perform this action.
     * <br></br>This can also be caused if the member supplied is not a member of this ThreadChannel's [Guild][net.dv8tion.jda.api.entities.Guild]
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The request was attempted after the channel was deleted.
     *
     *
     *
     *
     * Removing members from public threads or private threads this account does not own **requires the [net.dv8tion.jda.api.Permission.MANAGE_THREADS] permission**.
     *
     * @param member
     * The member to remove from this thread.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the account does not have the [net.dv8tion.jda.api.Permission.MANAGE_THREADS] permission, and this isn't a private thread channel this account owns.
     * @throws IllegalArgumentException
     * If the provided member is null.
     *
     * @return [RestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun removeThreadMember(@Nonnull member: Member): RestAction<Void?>? {
        Checks.notNull(member, "Member")
        return removeThreadMemberById(member.idLong)
    }

    @get:Nonnull
    abstract override val manager: ChannelManager<*, *>?
    override fun formatTo(formatter: Formatter, flags: Int, width: Int, precision: Int) {
        val leftJustified = flags and FormattableFlags.LEFT_JUSTIFY == FormattableFlags.LEFT_JUSTIFY
        val upper = flags and FormattableFlags.UPPERCASE == FormattableFlags.UPPERCASE
        val alt = flags and FormattableFlags.ALTERNATE == FormattableFlags.ALTERNATE
        val out: String
        out = if (alt) "#" + (if (upper) getName().uppercase(formatter.locale()) else getName()) else asMention
        MiscUtil.appendTo(formatter, width, precision, leftJustified, out)
    }

    /**
     * The values permitted for the auto archive duration of a [ThreadChannel].
     *
     *
     * This is the time before an idle thread will be automatically hidden.
     *
     *
     * Sending a message to the thread will reset the timer.
     *
     * @see ChannelField.AUTO_ARCHIVE_DURATION
     */
    enum class AutoArchiveDuration(
        /**
         * The number of minutes before an idle thread will be automatically hidden.
         *
         * @return The number of minutes
         */
        @JvmField val minutes: Int
    ) {
        TIME_1_HOUR(60),
        TIME_24_HOURS(1440),
        TIME_3_DAYS(4320),
        TIME_1_WEEK(10080);

        companion object {
            /**
             * Provides the corresponding enum constant for the provided number of minutes.
             *
             * @param  minutes
             * The number of minutes. (must be one of the valid values)
             *
             * @throws IllegalArgumentException
             * If the provided minutes is not a valid value.
             *
             * @return The corresponding enum constant.
             */
            @JvmStatic
            @Nonnull
            fun fromKey(minutes: Int): AutoArchiveDuration {
                for (duration in entries) {
                    if (duration.minutes == minutes) return duration
                }
                throw IllegalArgumentException("Provided key was not recognized. Minutes: $minutes")
            }
        }
    }
}
