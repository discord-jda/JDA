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

package net.dv8tion.jda.internal.requests;

import net.dv8tion.jda.api.utils.MiscUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Helper class for the rate limiting logic specific to member chunking.
 *
 * <p>When the {@link WebSocketSendingThread} loop attempts to send a chunk request,
 * it will:
 * <ol>
 *     <li>
 *         Check if there is a chunk request in-flight for the specified guild
 *         <ol>
 *             <li>If there is, it will try again on the next loop</li>
 *             <li>
 *                 If not, it will check for a possible rate limit and:
 *                 <ol>
 *                     <li>If there is, it will try again on the next loop</li>
 *                     <li>If not, it sends the WS message</li>
 *                 </ol>
 *             </li>
 *         </ol>
 *     </li>
 * </ul>
 */
public class WebSocketChunkingRateLimiter
{
    private final ReentrantLock queueLock;
    /** Guild ID -> Retry timestamp */
    private final Map<Long, Long> rateLimits = new HashMap<>();
    /** Guild ID */
    private final Set<Long> awaitingResponses = new HashSet<>();

    WebSocketChunkingRateLimiter(ReentrantLock queueLock)
    {
        this.queueLock = queueLock;
    }

    public void onRateLimit(long guildId, long retryAfter)
    {
        MiscUtil.locked(queueLock, () ->
        {
            rateLimits.put(guildId, System.currentTimeMillis() + retryAfter);
            awaitingResponses.remove(guildId);
        });
    }

    public void onChunkReceived(long guildId)
    {
        MiscUtil.locked(queueLock, () ->
        {
            awaitingResponses.remove(guildId);
        });
    }

    /** Caller ({@link WebSocketSendingThread}) for methods below already holds the lock, but it doesn't hurt to be sure */
    boolean isRateLimited(long guildId)
    {
        return MiscUtil.locked(queueLock, () ->
        {
            final Long retryAt = rateLimits.get(guildId);
            return retryAt != null && retryAt > System.currentTimeMillis();
        });
    }

    void setAwaitingResponse(long guildId)
    {
        MiscUtil.locked(queueLock, () ->
        {
            awaitingResponses.add(guildId);
        });
    }

    boolean isAwaitingResponse(long guildId)
    {
        return MiscUtil.locked(queueLock, () -> awaitingResponses.contains(guildId));
    }
}
