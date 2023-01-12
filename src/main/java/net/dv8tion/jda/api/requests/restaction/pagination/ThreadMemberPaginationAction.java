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

import net.dv8tion.jda.api.entities.ThreadMember;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;

import javax.annotation.Nonnull;

/**
 * {@link PaginationAction} that paginates the thread members endpoint.
 * <br>Note that this implementation is not considered thread-safe as modifications to the cache are not done
 * with a lock. Calling methods on this class from multiple threads is not recommended.
 *
 * <p><b>Must provide not-null {@link ThreadChannel} to compile a valid
 * pagination route.</b>
 *
 * <p><b>Limits:</b><br>
 * Minimum - 1
 * <br>Maximum - 100
 *
 * <p><b>Example</b><br>
 * <pre>{@code
 * // Count all thread members who are bots
 * public static CompletableFuture<AtomicInteger> countBotMembers(ThreadChannel thread) {
 *     AtomicInteger count = new AtomicInteger();
 *     ThreadMemberPaginationAction members = thread.retrieveThreadMembers();
 *     return members.forEachAsync((threadMember) -> {
 *         if (threadMember.getUser().isBot())
 *             count.incrementAndGet();
 *         return true; // continues iterating if this returns true
 *     }).thenApply((v) -> count);
 * }
 * }</pre>
 *
 * @see ThreadChannel#retrieveThreadMembers()
 */
public interface ThreadMemberPaginationAction extends PaginationAction<ThreadMember, ThreadMemberPaginationAction>
{
    /**
     * The {@link ThreadChannel} this action fetches members for.
     *
     * @return The channel
     */
    @Nonnull
    ThreadChannel getThreadChannel();
}
