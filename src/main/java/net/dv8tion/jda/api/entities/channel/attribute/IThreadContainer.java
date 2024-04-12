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

package net.dv8tion.jda.api.entities.channel.attribute;

import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.requests.restaction.ThreadChannelAction;
import net.dv8tion.jda.api.requests.restaction.pagination.ThreadChannelPaginationAction;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.internal.utils.Helpers;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.List;

/**
 * Abstraction of all channel types, which can contain or manage {@link ThreadChannel ThreadChannels}.
 *
 * @see ThreadChannel#getParentChannel()
 * @see net.dv8tion.jda.api.entities.channel.unions.IThreadContainerUnion IThreadContainerUnion
 */
public interface IThreadContainer extends GuildChannel, IPermissionContainer
{
    /**
     * The default {@link ISlowmodeChannel#getSlowmode() slowmode} for thread channels that is copied on thread creation.
     * <br>Users have to wait this amount of seconds before sending another message to the same thread.
     *
     * @return The default slowmode seconds for new threads, or {@code 0} if unset
     */
    int getDefaultThreadSlowmode();

    /**
     * Finds all {@link ThreadChannel ThreadChannels} whose parent is this channel.
     *
     * <p>These threads can also represent posts in {@link net.dv8tion.jda.api.entities.channel.concrete.ForumChannel ForumChannels}.
     *
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return Immutable list of all ThreadChannel children.
     */
    @Nonnull
    @Unmodifiable
    default List<ThreadChannel> getThreadChannels()
    {
        return getGuild().getThreadChannelCache().applyStream(stream ->
            stream.filter(thread -> thread.getParentChannel() == this)
                  .collect(Helpers.toUnmodifiableList())
        );
    }

    /**
     * Creates a new public {@link ThreadChannel} with the parent channel being this {@link IThreadContainer}.
     *
     * <p>The resulting {@link ThreadChannel ThreadChannel} may be either one of:
     * <ul>
     *     <li>{@link ChannelType#GUILD_PUBLIC_THREAD}</li>
     *     <li>{@link ChannelType#GUILD_NEWS_THREAD}</li>
     * </ul>
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The channel could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MAX_CHANNELS MAX_CHANNELS}
     *     <br>The maximum number of channels were exceeded</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MAX_ACTIVE_THREADS}
     *     <br>The maximum number of active threads has been reached, and no more may be created.</li>
     * </ul>
     *
     * @param  name
     *         The name of the new ThreadChannel (up to {@value Channel#MAX_NAME_LENGTH} characters)
     *
     * @throws IllegalArgumentException
     *         If the provided name is null, blank, empty, or longer than {@value Channel#MAX_NAME_LENGTH} characters
     * @throws UnsupportedOperationException
     *         If this is a forum channel.
     *         You must use {@link net.dv8tion.jda.api.entities.channel.concrete.ForumChannel#createForumPost(String, MessageCreateData) createForumPost(...)} instead.
     * @throws InsufficientPermissionException
     *         <ul>
     *             <li>If the bot does not have {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>If the bot does not have {@link net.dv8tion.jda.api.Permission#CREATE_PUBLIC_THREADS Permission.CREATE_PUBLIC_THREADS}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return A specific {@link ThreadChannelAction} that may be used to configure the new ThreadChannel before its creation.
     */
    @Nonnull
    @CheckReturnValue
    default ThreadChannelAction createThreadChannel(@Nonnull String name)
    {
        return createThreadChannel(name, false);
    }

    /**
     * Creates a new {@link ThreadChannel} with the parent channel being this {@link IThreadContainer}.
     *
     * <p>The resulting {@link ThreadChannel ThreadChannel} may be one of:
     * <ul>
     *     <li>{@link ChannelType#GUILD_PUBLIC_THREAD}</li>
     *     <li>{@link ChannelType#GUILD_NEWS_THREAD}</li>
     *     <li>{@link ChannelType#GUILD_PRIVATE_THREAD}</li>
     * </ul>
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The channel could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MAX_CHANNELS MAX_CHANNELS}
     *     <br>The maximum number of channels were exceeded</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MAX_ACTIVE_THREADS}
     *     <br>The maximum number of active threads has been reached, and no more may be created.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS}
     *     <br>Due to missing private thread permissions.</li>
     * </ul>
     *
     * @param  name
     *         The name of the new ThreadChannel (up to {@value Channel#MAX_NAME_LENGTH} characters).
     * @param  isPrivate
     *         The public/private status of the new ThreadChannel. If true, the new ThreadChannel will be private.
     *
     * @throws IllegalArgumentException
     *         If the provided name is null, blank, empty, or longer than {@value Channel#MAX_NAME_LENGTH} characters.
     * @throws IllegalStateException
     *         If the guild does have the feature flag {@code "PRIVATE_THREADS"} enabled.
     * @throws UnsupportedOperationException
     *         If this is a forum channel.
     *         You must use {@link net.dv8tion.jda.api.entities.channel.concrete.ForumChannel#createForumPost(String, MessageCreateData) createForumPost(...)} instead.
     * @throws InsufficientPermissionException
     *         <ul>
     *             <li>If the bot does not have {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>If the thread is {@code private}, and the bot does not have {@link net.dv8tion.jda.api.Permission#CREATE_PRIVATE_THREADS Permission.CREATE_PRIVATE_THREADS}</li>
     *             <li>If the thread is not {@code private}, and the bot does not have {@link net.dv8tion.jda.api.Permission#CREATE_PUBLIC_THREADS Permission.CREATE_PUBLIC_THREADS}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return A specific {@link ThreadChannelAction} that may be used to configure the new ThreadChannel before its creation.
     */
    @Nonnull
    @CheckReturnValue
    ThreadChannelAction createThreadChannel(@Nonnull String name, boolean isPrivate);

    /**
     * Creates a new, public {@link ThreadChannel} with the parent channel being this {@link IThreadContainer}.
     * <br>The starting message will copy the message for the provided id, and will be of type {@link MessageType#THREAD_STARTER_MESSAGE MessageType.THREAD_STARTER_MESSAGE}.
     *
     * <p>The resulting {@link ThreadChannel ThreadChannel} may be one of:
     * <ul>
     *     <li>{@link ChannelType#GUILD_PUBLIC_THREAD}</li>
     *     <li>{@link ChannelType#GUILD_NEWS_THREAD}</li>
     * </ul>
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The channel could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MAX_CHANNELS MAX_CHANNELS}
     *     <br>The maximum number of channels were exceeded</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#THREAD_WITH_THIS_MESSAGE_ALREADY_EXISTS}
     *     <br>This message has already been used to create a thread</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MAX_ACTIVE_THREADS}
     *     <br>The maximum number of active threads has been reached, and no more may be created.</li>
     * </ul>
     *
     * @param  name
     *         The name of the new ThreadChannel (up to {@value Channel#MAX_NAME_LENGTH} characters)
     * @param  messageId
     *         The ID of the message from which this ThreadChannel will be spawned.
     *
     * @throws IllegalArgumentException
     *         If the provided name is null, blank, empty, or longer than {@value Channel#MAX_NAME_LENGTH} characters
     * @throws UnsupportedOperationException
     *         If this is a forum channel.
     *         You must use {@link net.dv8tion.jda.api.entities.channel.concrete.ForumChannel#createForumPost(String, MessageCreateData) createForumPost(...)} instead.
     * @throws InsufficientPermissionException
     *         If the bot does not have {@link net.dv8tion.jda.api.Permission#CREATE_PUBLIC_THREADS Permission.CREATE_PUBLIC_THREADS} in this channel
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return A specific {@link ThreadChannelAction} that may be used to configure the new ThreadChannel before its creation.
     */
    @Nonnull
    @CheckReturnValue
    ThreadChannelAction createThreadChannel(@Nonnull String name, long messageId);


    /**
     * Creates a new, public {@link ThreadChannel} with the parent channel being this {@link IThreadContainer}.
     * <br>The starting message will copy the message for the provided id, and will be of type {@link MessageType#THREAD_STARTER_MESSAGE MessageType.THREAD_STARTER_MESSAGE}.
     *
     * <p>The resulting {@link ThreadChannel ThreadChannel} may be one of:
     * <ul>
     *     <li>{@link ChannelType#GUILD_PUBLIC_THREAD}</li>
     *     <li>{@link ChannelType#GUILD_NEWS_THREAD}</li>
     * </ul>
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The channel could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MAX_CHANNELS MAX_CHANNELS}
     *     <br>The maximum number of channels were exceeded</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#THREAD_WITH_THIS_MESSAGE_ALREADY_EXISTS}
     *     <br>This message has already been used to create a thread</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MAX_ACTIVE_THREADS}
     *     <br>The maximum number of active threads has been reached, and no more may be created.</li>
     * </ul>
     *
     * @param  name
     *         The name of the new ThreadChannel (up to {@value Channel#MAX_NAME_LENGTH} characters)
     * @param  messageId
     *         The ID of the message from which this ThreadChannel will be spawned.
     *
     * @throws IllegalArgumentException
     *         If the provided name is null, blank, empty, or longer than {@value Channel#MAX_NAME_LENGTH} characters.
     *         Or the message id is not a valid snowflake.
     * @throws UnsupportedOperationException
     *         If this is a forum channel.
     *         You must use {@link net.dv8tion.jda.api.entities.channel.concrete.ForumChannel#createForumPost(String, MessageCreateData) createForumPost(...)} instead.
     * @throws InsufficientPermissionException
     *         If the bot does not have {@link net.dv8tion.jda.api.Permission#CREATE_PUBLIC_THREADS Permission.CREATE_PUBLIC_THREADS} in this channel
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return A specific {@link ThreadChannelAction} that may be used to configure the new ThreadChannel before its creation.
     */
    @Nonnull
    @CheckReturnValue
    default ThreadChannelAction createThreadChannel(@Nonnull String name, @Nonnull String messageId)
    {
        return createThreadChannel(name, MiscUtil.parseSnowflake(messageId));
    }

    /**
     * Retrieves the archived public {@link ThreadChannel ThreadChannels} for this channel.
     * <br>This will iterate over all previously opened public threads, that have been archived.
     *
     * <p>You can use {@link #retrieveArchivedPrivateThreadChannels()}, to get all <em>private</em> archived threads.
     *
     * <p>These threads can also represent posts in {@link net.dv8tion.jda.api.entities.channel.concrete.ForumChannel ForumChannels}.
     *
     * @throws InsufficientPermissionException
     *         If the bot does not have {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY} in this channel
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return {@link ThreadChannelPaginationAction} to iterate over all public archived ThreadChannels
     */
    @Nonnull
    @CheckReturnValue
    ThreadChannelPaginationAction retrieveArchivedPublicThreadChannels();

    /**
     * Retrieves the archived private {@link ThreadChannel ThreadChannels} for this channel.
     * <br>This will iterate over all previously opened private threads, that have been archived.
     * This is a moderator restricted method, since private threads are only visible to members with {@link net.dv8tion.jda.api.Permission#MANAGE_THREADS Permission.MANAGE_THREADS}.
     *
     * <p>You can use {@link #retrieveArchivedPublicThreadChannels()}, to get all <em>public</em> archived threads.
     *
     * <p>Note that {@link net.dv8tion.jda.api.entities.channel.concrete.ForumChannel ForumChannels} cannot have private threads.
     *
     * @throws InsufficientPermissionException
     *         If the bot does not have {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}
     *         or {@link net.dv8tion.jda.api.Permission#MANAGE_THREADS Permission.MANAGE_THREADS} in this channel
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return {@link ThreadChannelPaginationAction} to iterate over all private archived ThreadChannels
     */
    @Nonnull
    @CheckReturnValue
    ThreadChannelPaginationAction retrieveArchivedPrivateThreadChannels();

    /**
     * Retrieves the archived private {@link ThreadChannel ThreadChannels} for this channel, that the bot has previously joined or been added to.
     * <br>Unlike {@link #retrieveArchivedPrivateThreadChannels()}, this only checks for threads which the bot has joined, and thus does not require permissions to manage threads.
     *
     * <p>You can use {@link #retrieveArchivedPrivateThreadChannels()}, to get all <em>private</em> archived threads.
     *
     * <p>Note that {@link net.dv8tion.jda.api.entities.channel.concrete.ForumChannel ForumChannels} cannot have private threads.
     *
     * @throws InsufficientPermissionException
     *         If the bot does not have {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY} in this channel
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return {@link ThreadChannelPaginationAction} to iterate over all joined private archived ThreadChannels
     */
    @Nonnull
    @CheckReturnValue
    ThreadChannelPaginationAction retrieveArchivedPrivateJoinedThreadChannels();
}
