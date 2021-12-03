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

import net.dv8tion.jda.api.requests.restaction.ThreadChannelAction;
import net.dv8tion.jda.api.requests.restaction.pagination.ThreadChannelPaginationAction;
import net.dv8tion.jda.api.utils.MiscUtil;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

public interface IThreadContainer extends GuildChannel, IPermissionContainer
{
    //TODO-v5 - Docs
    default List<ThreadChannel> getThreadChannels()
    {
        return getGuild().getThreadChannels()
            .stream()
            .filter(thread -> thread.getParentChannel() == this)
            .collect(Collectors.toList());
    }

    //TODO-v5: Docs
    @Nonnull
    @CheckReturnValue
    default ThreadChannelAction createThreadChannel(String name)
    {
        return createThreadChannel(name, false);
    }

    //TODO-v5: Docs
    @Nonnull
    @CheckReturnValue
    ThreadChannelAction createThreadChannel(String name, boolean isPrivate);


    //TODO-v5: Docs
    @Nonnull
    @CheckReturnValue
    ThreadChannelAction createThreadChannel(String name, long messageId);

    //TODO-v5: Docs
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
