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
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer;
import net.dv8tion.jda.api.entities.channel.unions.IThreadContainerUnion;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.ThreadChannelAction;
import net.dv8tion.jda.api.requests.restaction.pagination.ThreadChannelPaginationAction;
import net.dv8tion.jda.internal.entities.channel.mixin.middleman.GuildChannelMixin;
import net.dv8tion.jda.internal.requests.restaction.ThreadChannelActionImpl;
import net.dv8tion.jda.internal.requests.restaction.pagination.ThreadChannelPaginationActionImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;

public interface IThreadContainerMixin<T extends IThreadContainerMixin<T>> extends
        IThreadContainer,
        IThreadContainerUnion,
        GuildChannelMixin<T>
{
    // ---- Default implementations of interface ----
    @Nonnull
    @Override
    default ThreadChannelAction createThreadChannel(@Nonnull String name, boolean isPrivate)
    {
        Checks.notNull(name, "Name");
        name = name.trim();
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, 100, "Name");

        checkAttached();
        Checks.checkAccess(getGuild().getSelfMember(), this);
        if (isPrivate)
            checkPermission(Permission.CREATE_PRIVATE_THREADS);
        else
            checkPermission(Permission.CREATE_PUBLIC_THREADS);

        ChannelType threadType = isPrivate
            ? ChannelType.GUILD_PRIVATE_THREAD
            : getType() == ChannelType.TEXT
                ? ChannelType.GUILD_PUBLIC_THREAD
                : ChannelType.GUILD_NEWS_THREAD;

        return new ThreadChannelActionImpl(this, name, threadType);
    }

    @Nonnull
    @Override
    default ThreadChannelAction createThreadChannel(@Nonnull String name, long messageId)
    {
        Checks.notNull(name, "Name");
        name = name.trim();
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, 100, "Name");

        checkAttached();
        Checks.checkAccess(getGuild().getSelfMember(), this);
        checkPermission(Permission.CREATE_PUBLIC_THREADS);

        return new ThreadChannelActionImpl(this, name, Long.toUnsignedString(messageId));
    }

    @Nonnull
    @Override
    default ThreadChannelPaginationAction retrieveArchivedPublicThreadChannels()
    {
        checkAttached();
        Checks.checkAccess(getGuild().getSelfMember(), this);
        checkPermission(Permission.MESSAGE_HISTORY);

        Route.CompiledRoute route = Route.Channels.LIST_PUBLIC_ARCHIVED_THREADS.compile(getId());
        return new ThreadChannelPaginationActionImpl(getJDA(), route, this, false);
    }

    @Nonnull
    @Override
    default ThreadChannelPaginationAction retrieveArchivedPrivateThreadChannels()
    {
        checkAttached();
        Checks.checkAccess(getGuild().getSelfMember(), this);
        checkPermission(Permission.MESSAGE_HISTORY);
        checkPermission(Permission.MANAGE_THREADS);

        Route.CompiledRoute route = Route.Channels.LIST_PRIVATE_ARCHIVED_THREADS.compile(getId());
        return new ThreadChannelPaginationActionImpl(getJDA(), route, this, false);
    }

    @Nonnull
    @Override
    default ThreadChannelPaginationAction retrieveArchivedPrivateJoinedThreadChannels()
    {
        checkAttached();
        Checks.checkAccess(getGuild().getSelfMember(), this);
        checkPermission(Permission.MESSAGE_HISTORY);

        Route.CompiledRoute route = Route.Channels.LIST_JOINED_PRIVATE_ARCHIVED_THREADS.compile(getId());
        return new ThreadChannelPaginationActionImpl(getJDA(), route, this, true);
    }

    T setDefaultThreadSlowmode(int slowmode);
}
