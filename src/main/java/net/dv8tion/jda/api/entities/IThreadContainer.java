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

package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.requests.restaction.ThreadChannelAction;
import net.dv8tion.jda.api.requests.restaction.pagination.ThreadChannelPaginationAction;
import net.dv8tion.jda.api.utils.MiscUtil;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public interface IThreadContainer extends GuildChannel, IPermissionContainer
{
    /**
     * Finds all {@link ThreadChannel ThreadChannels} whose parent is this channel.
     *
     * @return a list of all ThreadChannel children.
     */
    default List<ThreadChannel> getThreadChannels()
    {
        return Collections.unmodifiableList(
                getGuild().getThreadChannels()
                    .stream()
                    .filter(thread -> thread.getParentChannel() == this)
                    .collect(Collectors.toList())
                );
    }


    /**
     * Creates a new, public {@link ThreadChannel} with the parent channel being this {@link IThreadContainer}.
     * This requires the bot to have the {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL} and {@link net.dv8tion.jda.api.Permission#CREATE_PUBLIC_THREADS} permissions.
     *
     * The resulting {@link net.dv8tion.jda.api.entities.ThreadChannel ThreadChannel} may be either one of:
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
     *
     * </ul>
     *
     * @param name
     *        The name of the new ThreadChannel
     *
     * @return A specific {@link ThreadChannelAction} that may be used to configure the new ThreadChannel before its creation.
     */
    @Nonnull
    @CheckReturnValue
    default ThreadChannelAction createThreadChannel(String name)
    {
        return createThreadChannel(name, false);
    }

    /**
     * Creates a new {@link ThreadChannel} with the parent channel being this {@link IThreadContainer}.
     * This requires the bot to have the {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL} and {@link net.dv8tion.jda.api.Permission#CREATE_PUBLIC_THREADS} permissions.
     *
     * The resulting {@link net.dv8tion.jda.api.entities.ThreadChannel ThreadChannel} may be one of:
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
     *
     * </ul>
     *
     * @param  name
     *         The name of the new ThreadChannel
     * @param  isPrivate
     *         The public/private status of the new ThreadChannel. If true, the new ThreadChannel will be private.
     *
     * @throws InsufficientPermissionException
     *         if the ThreadChannel is set to private, and the logged in account does not have {@link net.dv8tion.jda.api.Permission#CREATE_PRIVATE_THREADS}.
     *
     * @return A specific {@link ThreadChannelAction} that may be used to configure the new ThreadChannel before its creation.
     */
    @Nonnull
    @CheckReturnValue
    ThreadChannelAction createThreadChannel(String name, boolean isPrivate);


    /**
     * Creates a new, public {@link ThreadChannel} with the parent channel being this {@link IThreadContainer}.
     * This ThreadChannel will be spawned from the given messageID, and will consequently share its ID with the message.
     * This requires the bot to have {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL} and {@link net.dv8tion.jda.api.Permission#CREATE_PUBLIC_THREADS} permissions.
     *
     * The resulting {@link net.dv8tion.jda.api.entities.ThreadChannel ThreadChannel} may be one of:
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
     *
     * </ul>
     *
     * @param name
     *        The name of the new ThreadChannel
     * @param messageId
     *        The ID of the message from which this ThreadChannel will be spawned.
     *
     * @return A specific {@link ThreadChannelAction} that may be used to configure the new ThreadChannel before its creation.
     */
    @Nonnull
    @CheckReturnValue
    ThreadChannelAction createThreadChannel(String name, long messageId);


    /**
     * Creates a new, public {@link ThreadChannel} with the parent channel being this {@link IThreadContainer}.
     * This ThreadChannel will be spawned from the given messageID, and will consequently share its ID with the message.
     * This requires the bot to have {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL} and {@link net.dv8tion.jda.api.Permission#CREATE_PUBLIC_THREADS} permissions.
     *
     * The resulting {@link net.dv8tion.jda.api.entities.ThreadChannel ThreadChannel} may be one of:
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
     *
     * </ul>
     *
     * @param name
     *        The name of the new ThreadChannel
     * @param messageId
     *        The ID of the message from which this ThreadChannel will be spawned.
     *
     * @return A specific {@link ThreadChannelAction} that may be used to configure the new ThreadChannel before its creation.
     */
    @Nonnull
    @CheckReturnValue
    default ThreadChannelAction createThreadChannel(String name, String messageId)
    {
        return createThreadChannel(name, MiscUtil.parseSnowflake(messageId));
    }

    //TODO-v5: Docs
    @Nonnull
    @CheckReturnValue
    ThreadChannelPaginationAction retrieveArchivedPublicThreadChannels();

    //TODO-v5: Docs
    @Nonnull
    @CheckReturnValue
    ThreadChannelPaginationAction retrieveArchivedPrivateThreadChannels();

    //TODO-v5: Docs
    @Nonnull
    @CheckReturnValue
    ThreadChannelPaginationAction retrieveArchivedPrivateJoinedThreadChannels();
}
