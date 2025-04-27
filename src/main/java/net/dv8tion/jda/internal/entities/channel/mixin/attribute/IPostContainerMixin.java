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

package net.dv8tion.jda.internal.entities.channel.mixin.attribute;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.attribute.IPostContainer;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.requests.restaction.ForumPostAction;
import net.dv8tion.jda.api.requests.restaction.ThreadChannelAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.internal.requests.restaction.ForumPostActionImpl;
import net.dv8tion.jda.internal.utils.cache.SortedSnowflakeCacheViewImpl;

import javax.annotation.Nonnull;

public interface IPostContainerMixin<T extends IPostContainerMixin<T>> extends IPostContainer, IThreadContainerMixin<T>
{
    @Nonnull
    @Override
    SortedSnowflakeCacheViewImpl<ForumTag> getAvailableTagCache();

    @Nonnull
    @Override
    default ForumPostAction createForumPost(@Nonnull String name, @Nonnull MessageCreateData message)
    {
        checkAttached();
        checkPermission(Permission.MESSAGE_SEND);
        return new ForumPostActionImpl(this, name, new MessageCreateBuilder().applyData(message));
    }

    @Nonnull
    @Override
    default ThreadChannelAction createThreadChannel(@Nonnull String name)
    {
        throw new UnsupportedOperationException("You cannot create threads without a message payload in forum/media channels! Use createForumPost(...) instead.");
    }

    @Nonnull
    @Override
    default ThreadChannelAction createThreadChannel(@Nonnull String name, @Nonnull String messageId)
    {
        throw new UnsupportedOperationException("You cannot create threads without a message payload in forum/media channels! Use createForumPost(...) instead.");
    }

    T setDefaultReaction(DataObject emoji);
    T setDefaultSortOrder(int defaultSortOrder);
    T setFlags(int flags);

    int getRawSortOrder();
    int getRawFlags();
}
