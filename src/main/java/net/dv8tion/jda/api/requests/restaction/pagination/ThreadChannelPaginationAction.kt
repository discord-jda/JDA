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
package net.dv8tion.jda.api.requests.restaction.pagination

import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.channel.unions.IThreadContainerUnion
import javax.annotation.Nonnull

/**
 * [PaginationAction] that paginates the thread members endpoint.
 * <br></br>Note that this implementation is not considered thread-safe as modifications to the cache are not done
 * with a lock. Calling methods on this class from multiple threads is not recommended.
 *
 *
 * **Must provide not-null [IThreadContainer] to compile a valid
 * pagination route.**
 *
 *
 * **Limits:**<br></br>
 * Minimum - 1
 * <br></br>Maximum - 100
 *
 *
 * **Example**<br></br>
 * <pre>`// Clean up all private threads older than 2 weeks
 * public static void cleanupPrivateThreads(TextChannel channel) {
 * // get 2-week offset
 * long 2WeekAgoTimestamp = System.currentTimeMillis() - (14 * 24 * 60 * 60 * 1000);
 * // get paginator
 * ThreadChannelPaginationAction threads = channel.retrieveArchivedPrivateThreadChannels();
 * // remove each thread older than 2 weeks
 * threads.forEachAsync((thread) ->
 * long threadArchiveTimestamp = thread.getTimeArchiveInfoLastModified().toInstant().toEpochMilli();
 * if (threadArchiveTimestamp < 2WeeksAgoTimestamp) {
 * thread.delete().reason("Cleaning up old private threads").queue();
 * }
 * );
 * }
`</pre> *
 *
 * @see IThreadContainer.retrieveArchivedPublicThreadChannels
 * @see IThreadContainer.retrieveArchivedPrivateThreadChannels
 * @see IThreadContainer.retrieveArchivedPrivateJoinedThreadChannels
 */
interface ThreadChannelPaginationAction : PaginationAction<ThreadChannel?, ThreadChannelPaginationAction?> {
    @get:Nonnull
    val channel: IThreadContainerUnion

    @get:Nonnull
    val guild: Guild?
        /**
         * The current target [Guild][net.dv8tion.jda.api.entities.Guild] for this ThreadChannelPaginationAction.
         *
         * @return The never-null target Guild
         */
        get() = channel.guild
}
