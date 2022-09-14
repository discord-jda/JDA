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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.attribute.IAgeRestrictedChannel;
import net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer;
import net.dv8tion.jda.api.entities.channel.attribute.IWebhookContainer;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildChannel;
import net.dv8tion.jda.api.managers.channel.concrete.ForumChannelManager;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.requests.restaction.ForumPostAction;
import net.dv8tion.jda.api.utils.cache.SortedSnowflakeCacheView;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * A Forum Channel which contains {@link #createForumPost(String, MessageCreateData) Forum Posts}.
 * <br>Forum posts are simply {@link ThreadChannel ThreadChannels} of type {@link ChannelType#GUILD_PUBLIC_THREAD}.
 *
 * <p>The {@code CREATE POSTS} permission that is shown in the official Discord Client, is an alias for {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}.
 * {@link net.dv8tion.jda.api.Permission#CREATE_PUBLIC_THREADS Permission.CREATE_PUBLIC_THREADS} is ignored for creating forum posts.
 *
 * @see Guild#createForumChannel(String, Category)
 * @see #createForumPost(String, MessageCreateData)
 */
public interface ForumChannel extends StandardGuildChannel, IThreadContainer, IWebhookContainer, IAgeRestrictedChannel
{
    /**
     * The maximum length of a forum topic ({@value #MAX_FORUM_TOPIC_LENGTH})
     */
    int MAX_FORUM_TOPIC_LENGTH = 4096;
    /**
     * The maximum number of {@link ForumPostAction#setTags(Collection) tags} that can be applied to a forum post. ({@value #MAX_POST_TAGS})
     */
    int MAX_POST_TAGS = 5;

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

    @Nonnull
    SortedSnowflakeCacheView<ForumTag> getAvailableTagCache();

    @Nonnull
    default List<ForumTag> getAvailableTags()
    {
        return getAvailableTagCache().asList();
    }

    // TODO: Should this be in StandardGuildMessageChannel?

    /**
     * The slowmode set for this ForumChannel.
     * <br>If slowmode is set this returns an {@code int} between 1 and {@link net.dv8tion.jda.api.entities.channel.concrete.TextChannel#MAX_SLOWMODE TextChannel.MAX_SLOWMODE}.
     * <br>If not set this returns {@code 0}.
     *
     * <p>Note bots are unaffected by this.
     * <br>Having {@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE MESSAGE_MANAGE} or
     * {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} permission also
     * grants immunity to slowmode.
     *
     * @return The slowmode for this ForumChannel, between 1 and {@link net.dv8tion.jda.api.entities.channel.concrete.TextChannel#MAX_SLOWMODE TextChannel.MAX_SLOWMODE}, or {@code 0} if no slowmode is set.
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
     *         The name of the post (up to {@value Channel#MAX_NAME_LENGTH} characters)
     * @param  message
     *         The starting message of the post (see {@link net.dv8tion.jda.api.utils.messages.MessageCreateBuilder MessageCreateBuilder})
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the bot does not have {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND} in the channel
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If null is provided</li>
     *             <li>If the name is empty or longer than {@value Channel#MAX_NAME_LENGTH} characters</li>
     *         </ul>
     *
     * @return {@link ForumPostAction}
     */
    @Nonnull
    @Incubating
    @CheckReturnValue
    ForumPostAction createForumPost(@Nonnull String name, @Nonnull MessageCreateData message);
}
