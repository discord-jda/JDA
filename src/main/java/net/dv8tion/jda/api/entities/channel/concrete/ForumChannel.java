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
import java.util.Collection;
import java.util.Collections;

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
     * The topic set for this channel.
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

    @Nonnull
    @Incubating
    @CheckReturnValue
    default RestAction<ThreadChannel> createForumPost(@Nonnull String name, @Nonnull Message message, @Nonnull FileUpload... uploads)
    {
        return createForumPost(name, message, Collections.emptyList(), uploads);
    }

    @Nonnull
    @Incubating
    @CheckReturnValue
    RestAction<ThreadChannel> createForumPost(@Nonnull String name, @Nonnull Message message, @Nonnull Collection<String> tags, @Nonnull FileUpload... uploads);
}
