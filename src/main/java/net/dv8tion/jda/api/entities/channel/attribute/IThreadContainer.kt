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
package net.dv8tion.jda.api.entities.channel.attribute

import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.requests.restaction.ThreadChannelAction
import net.dv8tion.jda.api.requests.restaction.pagination.ThreadChannelPaginationAction
import net.dv8tion.jda.api.utils.MiscUtil
import net.dv8tion.jda.internal.utils.Helpers
import java.util.stream.Stream
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Abstraction of all channel types, which can contain or manage [ThreadChannels][ThreadChannel].
 *
 * @see ThreadChannel.getParentChannel
 * @see net.dv8tion.jda.api.entities.channel.unions.IThreadContainerUnion IThreadContainerUnion
 */
interface IThreadContainer : GuildChannel, IPermissionContainer {
    /**
     * The default [slowmode][ISlowmodeChannel.getSlowmode] for thread channels that is copied on thread creation.
     * <br></br>Users have to wait this amount of seconds before sending another message to the same thread.
     *
     * @return The default slowmode seconds for new threads, or `0` if unset
     */
    @JvmField
    val defaultThreadSlowmode: Int

    @get:Nonnull
    val threadChannels: List<ThreadChannel?>?
        /**
         * Finds all [ThreadChannels][ThreadChannel] whose parent is this channel.
         *
         *
         * These threads can also represent posts in [ForumChannels][net.dv8tion.jda.api.entities.channel.concrete.ForumChannel].
         *
         * @return Immutable list of all ThreadChannel children.
         */
        get() = getGuild().getThreadChannelCache().applyStream<List<ThreadChannel?>> { stream: Stream<ThreadChannel> ->
            stream.filter { thread: ThreadChannel -> thread.getParentChannel() === this }
                .collect(Helpers.toUnmodifiableList())
        }

    /**
     * Creates a new public [ThreadChannel] with the parent channel being this [IThreadContainer].
     *
     *
     * The resulting [ThreadChannel] may be either one of:
     *
     *  * [ChannelType.GUILD_PUBLIC_THREAD]
     *  * [ChannelType.GUILD_NEWS_THREAD]
     *
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
     * the returned [RestAction][net.dv8tion.jda.api.requests.RestAction] include the following:
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The channel could not be created due to a permission discrepancy
     *
     *  * [MAX_CHANNELS][net.dv8tion.jda.api.requests.ErrorResponse.MAX_CHANNELS]
     * <br></br>The maximum number of channels were exceeded
     *
     *  * [net.dv8tion.jda.api.requests.ErrorResponse.MAX_ACTIVE_THREADS]
     * <br></br>The maximum number of active threads has been reached, and no more may be created.
     *
     *
     * @param  name
     * The name of the new ThreadChannel (up to {@value Channel#MAX_NAME_LENGTH} characters)
     *
     * @throws IllegalArgumentException
     * If the provided name is null, blank, empty, or longer than {@value Channel#MAX_NAME_LENGTH} characters
     * @throws UnsupportedOperationException
     * If this is a forum channel.
     * You must use [createForumPost(...)][net.dv8tion.jda.api.entities.channel.concrete.ForumChannel.createForumPost] instead.
     * @throws InsufficientPermissionException
     *
     *  * If the bot does not have [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     *  * If the bot does not have [Permission.CREATE_PUBLIC_THREADS][net.dv8tion.jda.api.Permission.CREATE_PUBLIC_THREADS]
     *
     *
     * @return A specific [ThreadChannelAction] that may be used to configure the new ThreadChannel before its creation.
     */
    @Nonnull
    @CheckReturnValue
    fun createThreadChannel(@Nonnull name: String?): ThreadChannelAction? {
        return createThreadChannel(name, false)
    }

    /**
     * Creates a new [ThreadChannel] with the parent channel being this [IThreadContainer].
     *
     *
     * The resulting [ThreadChannel] may be one of:
     *
     *  * [ChannelType.GUILD_PUBLIC_THREAD]
     *  * [ChannelType.GUILD_NEWS_THREAD]
     *  * [ChannelType.GUILD_PRIVATE_THREAD]
     *
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
     * the returned [RestAction][net.dv8tion.jda.api.requests.RestAction] include the following:
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The channel could not be created due to a permission discrepancy
     *
     *  * [MAX_CHANNELS][net.dv8tion.jda.api.requests.ErrorResponse.MAX_CHANNELS]
     * <br></br>The maximum number of channels were exceeded
     *
     *  * [net.dv8tion.jda.api.requests.ErrorResponse.MAX_ACTIVE_THREADS]
     * <br></br>The maximum number of active threads has been reached, and no more may be created.
     *
     *  * [net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>Due to missing private thread permissions.
     *
     *
     * @param  name
     * The name of the new ThreadChannel (up to {@value Channel#MAX_NAME_LENGTH} characters).
     * @param  isPrivate
     * The public/private status of the new ThreadChannel. If true, the new ThreadChannel will be private.
     *
     * @throws IllegalArgumentException
     * If the provided name is null, blank, empty, or longer than {@value Channel#MAX_NAME_LENGTH} characters.
     * @throws IllegalStateException
     * If the guild does have the feature flag `"PRIVATE_THREADS"` enabled.
     * @throws UnsupportedOperationException
     * If this is a forum channel.
     * You must use [createForumPost(...)][net.dv8tion.jda.api.entities.channel.concrete.ForumChannel.createForumPost] instead.
     * @throws InsufficientPermissionException
     *
     *  * If the bot does not have [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     *  * If the thread is `private`, and the bot does not have [Permission.CREATE_PRIVATE_THREADS][net.dv8tion.jda.api.Permission.CREATE_PRIVATE_THREADS]
     *  * If the thread is not `private`, and the bot does not have [Permission.CREATE_PUBLIC_THREADS][net.dv8tion.jda.api.Permission.CREATE_PUBLIC_THREADS]
     *
     *
     * @return A specific [ThreadChannelAction] that may be used to configure the new ThreadChannel before its creation.
     */
    @Nonnull
    @CheckReturnValue
    fun createThreadChannel(@Nonnull name: String?, isPrivate: Boolean): ThreadChannelAction?

    /**
     * Creates a new, public [ThreadChannel] with the parent channel being this [IThreadContainer].
     * <br></br>The starting message will copy the message for the provided id, and will be of type [MessageType.THREAD_STARTER_MESSAGE].
     *
     *
     * The resulting [ThreadChannel] may be one of:
     *
     *  * [ChannelType.GUILD_PUBLIC_THREAD]
     *  * [ChannelType.GUILD_NEWS_THREAD]
     *
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
     * the returned [RestAction][net.dv8tion.jda.api.requests.RestAction] include the following:
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The channel could not be created due to a permission discrepancy
     *
     *  * [MAX_CHANNELS][net.dv8tion.jda.api.requests.ErrorResponse.MAX_CHANNELS]
     * <br></br>The maximum number of channels were exceeded
     *
     *  * [net.dv8tion.jda.api.requests.ErrorResponse.THREAD_WITH_THIS_MESSAGE_ALREADY_EXISTS]
     * <br></br>This message has already been used to create a thread
     *
     *  * [net.dv8tion.jda.api.requests.ErrorResponse.MAX_ACTIVE_THREADS]
     * <br></br>The maximum number of active threads has been reached, and no more may be created.
     *
     *
     * @param  name
     * The name of the new ThreadChannel (up to {@value Channel#MAX_NAME_LENGTH} characters)
     * @param  messageId
     * The ID of the message from which this ThreadChannel will be spawned.
     *
     * @throws IllegalArgumentException
     * If the provided name is null, blank, empty, or longer than {@value Channel#MAX_NAME_LENGTH} characters
     * @throws UnsupportedOperationException
     * If this is a forum channel.
     * You must use [createForumPost(...)][net.dv8tion.jda.api.entities.channel.concrete.ForumChannel.createForumPost] instead.
     * @throws InsufficientPermissionException
     * If the bot does not have [Permission.CREATE_PUBLIC_THREADS][net.dv8tion.jda.api.Permission.CREATE_PUBLIC_THREADS] in this channel
     *
     * @return A specific [ThreadChannelAction] that may be used to configure the new ThreadChannel before its creation.
     */
    @Nonnull
    @CheckReturnValue
    fun createThreadChannel(@Nonnull name: String?, messageId: Long): ThreadChannelAction?

    /**
     * Creates a new, public [ThreadChannel] with the parent channel being this [IThreadContainer].
     * <br></br>The starting message will copy the message for the provided id, and will be of type [MessageType.THREAD_STARTER_MESSAGE].
     *
     *
     * The resulting [ThreadChannel] may be one of:
     *
     *  * [ChannelType.GUILD_PUBLIC_THREAD]
     *  * [ChannelType.GUILD_NEWS_THREAD]
     *
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
     * the returned [RestAction][net.dv8tion.jda.api.requests.RestAction] include the following:
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The channel could not be created due to a permission discrepancy
     *
     *  * [MAX_CHANNELS][net.dv8tion.jda.api.requests.ErrorResponse.MAX_CHANNELS]
     * <br></br>The maximum number of channels were exceeded
     *
     *  * [net.dv8tion.jda.api.requests.ErrorResponse.THREAD_WITH_THIS_MESSAGE_ALREADY_EXISTS]
     * <br></br>This message has already been used to create a thread
     *
     *  * [net.dv8tion.jda.api.requests.ErrorResponse.MAX_ACTIVE_THREADS]
     * <br></br>The maximum number of active threads has been reached, and no more may be created.
     *
     *
     * @param  name
     * The name of the new ThreadChannel (up to {@value Channel#MAX_NAME_LENGTH} characters)
     * @param  messageId
     * The ID of the message from which this ThreadChannel will be spawned.
     *
     * @throws IllegalArgumentException
     * If the provided name is null, blank, empty, or longer than {@value Channel#MAX_NAME_LENGTH} characters.
     * Or the message id is not a valid snowflake.
     * @throws UnsupportedOperationException
     * If this is a forum channel.
     * You must use [createForumPost(...)][net.dv8tion.jda.api.entities.channel.concrete.ForumChannel.createForumPost] instead.
     * @throws InsufficientPermissionException
     * If the bot does not have [Permission.CREATE_PUBLIC_THREADS][net.dv8tion.jda.api.Permission.CREATE_PUBLIC_THREADS] in this channel
     *
     * @return A specific [ThreadChannelAction] that may be used to configure the new ThreadChannel before its creation.
     */
    @Nonnull
    @CheckReturnValue
    fun createThreadChannel(@Nonnull name: String?, @Nonnull messageId: String?): ThreadChannelAction? {
        return createThreadChannel(name, MiscUtil.parseSnowflake(messageId))
    }

    /**
     * Retrieves the archived public [ThreadChannels][ThreadChannel] for this channel.
     * <br></br>This will iterate over all previously opened public threads, that have been archived.
     *
     *
     * You can use [.retrieveArchivedPrivateThreadChannels], to get all *private* archived threads.
     *
     *
     * These threads can also represent posts in [ForumChannels][net.dv8tion.jda.api.entities.channel.concrete.ForumChannel].
     *
     * @throws InsufficientPermissionException
     * If the bot does not have [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY] in this channel
     *
     * @return [ThreadChannelPaginationAction] to iterate over all public archived ThreadChannels
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveArchivedPublicThreadChannels(): ThreadChannelPaginationAction?

    /**
     * Retrieves the archived private [ThreadChannels][ThreadChannel] for this channel.
     * <br></br>This will iterate over all previously opened private threads, that have been archived.
     * This is a moderator restricted method, since private threads are only visible to members with [Permission.MANAGE_THREADS][net.dv8tion.jda.api.Permission.MANAGE_THREADS].
     *
     *
     * You can use [.retrieveArchivedPublicThreadChannels], to get all *public* archived threads.
     *
     *
     * Note that [ForumChannels][net.dv8tion.jda.api.entities.channel.concrete.ForumChannel] cannot have private threads.
     *
     * @throws InsufficientPermissionException
     * If the bot does not have [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY]
     * or [Permission.MANAGE_THREADS][net.dv8tion.jda.api.Permission.MANAGE_THREADS] in this channel
     *
     * @return [ThreadChannelPaginationAction] to iterate over all private archived ThreadChannels
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveArchivedPrivateThreadChannels(): ThreadChannelPaginationAction?

    /**
     * Retrieves the archived private [ThreadChannels][ThreadChannel] for this channel, that the bot has previously joined or been added to.
     * <br></br>Unlike [.retrieveArchivedPrivateThreadChannels], this only checks for threads which the bot has joined, and thus does not require permissions to manage threads.
     *
     *
     * You can use [.retrieveArchivedPrivateThreadChannels], to get all *private* archived threads.
     *
     *
     * Note that [ForumChannels][net.dv8tion.jda.api.entities.channel.concrete.ForumChannel] cannot have private threads.
     *
     * @throws InsufficientPermissionException
     * If the bot does not have [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY] in this channel
     *
     * @return [ThreadChannelPaginationAction] to iterate over all joined private archived ThreadChannels
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveArchivedPrivateJoinedThreadChannels(): ThreadChannelPaginationAction?
}
