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

package net.dv8tion.jda.api.entities.messages;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Base interface for message search responses.
 *
 * @see Guild#searchMessages()
 */
public interface MessageSearchResponse {
    /**
     * Whether the message search has failed, currently,
     * it can only "gracefully" fail when the message index is not yet available.
     *
     * <p>If this returns {@code true}, you muse use {@link #asNotReady()}
     * and retry the request according to the returned {@linkplain NotReady#getRetryAfter() delay}.
     *
     * <p>If this returns {@code false}, you must use {@link #asResults()} to read the results.
     *
     * @return {@code true} if the request failed, {@code false} otherwise
     */
    boolean hasFailed();

    /**
     * Returns this instance as {@link NotReady NotReady}, if it is one.
     *
     * @throws IllegalStateException
     *         If the search has succeeded
     *
     * @return The {@link NotReady NotReady} state
     */
    @Nonnull
    NotReady asNotReady();

    /**
     * Returns this instance as {@link Results Results}, if it is one.
     *
     * @throws IllegalStateException
     *         If the search is not ready yet
     *
     * @return The {@link Results Results} state
     */
    @Nonnull
    Results asResults();

    /**
     * Represents a response indicating the search index is not yet ready.
     *
     * @see #asNotReady()
     */
    interface NotReady extends MessageSearchResponse {
        /**
         * The number of documents that has been indexed thus far.
         *
         * @return Number of currently indexed documents
         */
        int getDocumentsIndexed();

        /**
         * The delay (in seconds) after which you should retry the message search.
         * <br>If the value is {@code 0}, you should retry the request after a short delay.
         *
         * @return Delay (in seconds) before retrying the request
         */
        int getRetryAfter();
    }

    /**
     * Represents a response with the found messages.
     *
     * @see #asResults()
     */
    interface Results extends MessageSearchResponse {
        /**
         * The messages satisfying the search query.
         *
         * <p>The returned messages will be missing reactions, member objects (unless cached)
         * and the containing thread's members will only contain the {@linkplain net.dv8tion.jda.api.entities.Guild#getSelfMember() current member},
         * if it has joined the thread.
         *
         * @return The matching messages
         */
        @Nonnull
        @Unmodifiable
        List<Message> getMessages();

        /**
         * Whether the guild is still being indexed.
         *
         * @return {@code true} if the indexing is still being done
         */
        boolean isDoingDeepHistoricalIndex();

        /**
         * The total number of messages. May not be accurate as messages are created/deleted.
         *
         * @return The total results
         */
        int getTotalResults();
    }
}
