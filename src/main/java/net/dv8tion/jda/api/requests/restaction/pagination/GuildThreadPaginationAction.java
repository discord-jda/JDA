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

package net.dv8tion.jda.api.requests.restaction.pagination;

import net.dv8tion.jda.api.entities.*;

import javax.annotation.Nonnull;

/**
 * {@link PaginationAction PaginationAction} that paginates the thread members endpoint.
 * <br>Note that this implementation is not considered thread-safe as modifications to the cache are not done
 * with a lock. Calling methods on this class from multiple threads is not recommended.
 *
 * //TODO-threads: This should reference the ThreadParentChannel instead.
 * <p><b>Must provide not-null {@link BaseGuildMessageChannel BaseGuildMessageChannel} to compile a valid
 * pagination route.</b>
 *
 * <h2>Limits:</h2>
 * Minimum - 1
 * <br>Maximum - 100
 *
 * <h1>Example</h1>
 * <pre>{@code
 * // Clean up all private threads older than 2 weeks
 * public static void cleanupPrivateThreads(TextChannel channel) {
 *     // get 2-week offset
 *     long 2WeekAgoTimestamp = System.currentTimeMillis() - (14 * 24 * 60 * 60 * 1000);
 *     // get paginator
 *     GuildThreadPaginationAction threads = channel.retrieveArchivedPrivateThreads();
 *     // remove each thread older than 2 weeks
 *     threads.forEachAsync((thread) ->
 *         long threadArchiveTimestamp = thread.getTimeArchive().toInstant().toEpochMilli();
 *         if (threadArchiveTimestamp < 2WeeksAgoTimestamp) {
 *            thread.delete().reason("Cleaning up old private threads").queue();
 *         }
 *     );
 * }
 * }</pre>
 *
 * @since  3.1
 *
 * //TODO-threads: Use ThreadParentChannel
 * @see    BaseGuildMessageChannel#retrieveArchivedPublicThreads()
 * @see    BaseGuildMessageChannel#retrieveArchivedPrivateThreads()
 * @see    BaseGuildMessageChannel#retrieveArchivedPrivateJoinedThreads()
 */
public interface GuildThreadPaginationAction extends PaginationAction<GuildThread, GuildThreadPaginationAction>
{
    //TODO-v5: Docs
    //TODO-threads: Replace with ThreadParentChannel
    @Nonnull
    BaseGuildMessageChannel getChannel();

    /**
     * The current target {@link net.dv8tion.jda.api.entities.Guild Guild} for this GuildThreadPaginationAction.
     *
     * @return The never-null target Guild
     */
    @Nonnull
    default Guild getGuild()
    {
        return getChannel().getGuild();
    }
}
