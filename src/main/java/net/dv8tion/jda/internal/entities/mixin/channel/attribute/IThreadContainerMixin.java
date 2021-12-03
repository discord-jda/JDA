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

package net.dv8tion.jda.internal.entities.mixin.channel.attribute;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.IThreadContainer;
import net.dv8tion.jda.api.requests.restaction.ThreadChannelAction;
import net.dv8tion.jda.api.requests.restaction.pagination.ThreadChannelPaginationAction;
import net.dv8tion.jda.internal.entities.mixin.channel.middleman.GuildChannelMixin;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.requests.restaction.ThreadChannelActionImpl;
import net.dv8tion.jda.internal.requests.restaction.pagination.ThreadChannelPaginationActionImpl;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public interface IThreadContainerMixin<T extends IThreadContainerMixin<T>> extends IThreadContainer, GuildChannelMixin<T>
{
    // ---- Default implementations of interface ----
    @Nonnull
    @CheckReturnValue
    @Override
    default ThreadChannelAction createThreadChannel(String name, boolean isPrivate)
    {
        checkPermission(Permission.VIEW_CHANNEL);
        if (isPrivate)
        {
            if (!getGuild().getFeatures().contains("PRIVATE_THREADS"))
                throw new IllegalStateException("Can only use private threads in Guilds with the PRIVATE_THREADS feature");
            checkPermission(Permission.CREATE_PRIVATE_THREADS);
        }
        else
        {
            checkPermission(Permission.CREATE_PUBLIC_THREADS);
        }

        ChannelType threadType = isPrivate
            ? ChannelType.GUILD_PRIVATE_THREAD
            : getType() == ChannelType.TEXT
                ? ChannelType.GUILD_PUBLIC_THREAD
                : ChannelType.GUILD_NEWS_THREAD;

        return new ThreadChannelActionImpl(this, name, threadType);
    }

    @Nonnull
    @CheckReturnValue
    @Override
    default ThreadChannelAction createThreadChannel(String name, long messageId)
    {
        checkPermission(Permission.VIEW_CHANNEL);
        checkPermission(Permission.CREATE_PUBLIC_THREADS);

        return new ThreadChannelActionImpl(this, name, Long.toUnsignedString(messageId));
    }

    @Nonnull
    @CheckReturnValue
    @Override
    default ThreadChannelPaginationAction retrieveArchivedPublicThreadChannels()
    {
        checkPermission(Permission.MESSAGE_HISTORY);

        Route.CompiledRoute route = Route.Channels.LIST_PUBLIC_ARCHIVED_THREADS.compile(getId());
        return new ThreadChannelPaginationActionImpl(getJDA(), route, this);
    }

    @Nonnull
    @CheckReturnValue
    @Override
    default ThreadChannelPaginationAction retrieveArchivedPrivateThreadChannels()
    {
        checkPermission(Permission.MESSAGE_HISTORY);
        checkPermission(Permission.MANAGE_THREADS);

        Route.CompiledRoute route = Route.Channels.LIST_PRIVATE_ARCHIVED_THREADS.compile(getId());
        return new ThreadChannelPaginationActionImpl(getJDA(), route, this);
    }

    @Nonnull
    @CheckReturnValue
    @Override
    default ThreadChannelPaginationAction retrieveArchivedPrivateJoinedThreadChannels()
    {
        checkPermission(Permission.MESSAGE_HISTORY);

        Route.CompiledRoute route = Route.Channels.LIST_JOINED_PRIVATE_ARCHIVED_THREADS.compile(getId());
        return new ThreadChannelPaginationActionImpl(getJDA(), route, this);
    }
}
