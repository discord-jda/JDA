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

import net.dv8tion.jda.annotations.Incubating;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.attribute.IAgeRestrictedChannel;
import net.dv8tion.jda.api.managers.channel.concrete.ForumChannelManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ForumChannel extends StandardGuildChannel, IThreadContainer, IAgeRestrictedChannel
{
    @Nonnull
    @Override
    default ChannelType getType()
    {
        return ChannelType.FORUM;
    }

    @Nonnull
    @Override
    ForumChannelManager getManager();

    @Nonnull
    @Override
    ChannelAction<ForumChannel> createCopy(@Nonnull Guild guild);

    @Nonnull
    @Override
    default ChannelAction<ForumChannel> createCopy()
    {
        return createCopy(getGuild());
    }

    // TODO: Should this be in StandardGuildMessageChannel?

    /**
     * The slowmode set for this ForumChannel.
     * <br>If slowmode is set this returns an {@code int} between 1 and {@link net.dv8tion.jda.api.entities.TextChannel#MAX_SLOWMODE TextChannel.MAX_SLOWMODE}.
     * <br>If not set this returns {@code 0}.
     *
     * <p>Note bots are unaffected by this.
     * <br>Having {@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE MESSAGE_MANAGE} or
     * {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} permission also
     * grants immunity to slowmode.
     *
     * @return The slowmode for this ForumChannel, between 1 and {@link net.dv8tion.jda.api.entities.TextChannel#MAX_SLOWMODE TextChannel.MAX_SLOWMODE}, or {@code 0} if no slowmode is set.
     */
    int getSlowmode();

    // TODO: Should this be in StandardGuildChannel?

    /**
     * The topic set for this channel, this is referred to as <em>Guidelines</em> in the official Discord client.
     * <br>If no topic has been set, this returns null.
     *
     * @return Possibly-null String containing the topic of this channel.
     */
    @Nullable
    String getTopic();

// TODO-message-rework: Specializing by combining message send with thread create into a unified interface
//
// Note: This requires changes coming with the message-rework. Specifically, you need a nice way to create a complete message and pass it to the method.
// In addition, we need a comprehensive abstraction for all the message specific setters, coming with the MessageSend interface in the message rework.
//
// There is a temporary implementation available, which will most definitely be replaced in a future release.

    /**
     * Creates a new forum post (thread) in this forum.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>If the forum channel was deleted</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#REQUEST_ENTITY_TOO_LARGE REQUEST_ENTITY_TOO_LARGE}
     *     <br>If the total sum of uploaded bytes exceeds the guild's {@link Guild#getMaxFileSize() upload limit}</li>
     * </ul>
     *
     * @param  name
     *         The name of the post
     * @param  upload
     *         A file attachment to upload as the post content
     * @param  uploads
     *         Additional files to attach to the post
     *
     * @return {@link RestAction} - Type: {@link ThreadChannel}
     */
    @Nonnull
    @Incubating
    @CheckReturnValue
    RestAction<ThreadChannel> createForumPost(@Nonnull String name, @Nonnull FileUpload upload, @Nonnull FileUpload... uploads);

    /**
     * Creates a new forum post (thread) in this forum.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>If the forum channel was deleted</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#REQUEST_ENTITY_TOO_LARGE REQUEST_ENTITY_TOO_LARGE}
     *     <br>If the total sum of uploaded bytes exceeds the guild's {@link Guild#getMaxFileSize() upload limit}</li>
     * </ul>
     *
     * @param  name
     *         The name of the post
     * @param  message
     *         The post message content
     * @param  uploads
     *         Additional files to attach to the post
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If null is provided</li>
     *             <li>If the name is empty or longer than 100 characters</li>
     *         </ul>
     *
     * @return {@link RestAction} - Type: {@link ThreadChannel}
     */
    @Nonnull
    @Incubating
    @CheckReturnValue
    RestAction<ThreadChannel> createForumPost(@Nonnull String name, @Nonnull Message message, @Nonnull FileUpload... uploads);
}
