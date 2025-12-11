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

import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

import javax.annotation.Nonnull;

public interface MessageSearchResponse {
    /**
     * Whether the message search has failed, currently,
     * it can only "gracefully" fail when the message index is not yet available.
     *
     * <p>If this returns {@code true}, you muse use {@link #asFailure()}
     * and retry the request according to the returned {@linkplain Failure#getRetryAfter() delay}.
     *
     * <p>If this returns {@code false}, you must use {@link #asBody()} to read the results.
     *
     * @return {@code true} if the request failed, {@code false} otherwise
     */
    boolean hasFailed();

    @Nonnull
    Failure asFailure();

    @Nonnull
    Body asBody();

    interface Failure extends MessageSearchResponse {
        int getDocumentsIndexed();

        /**
         * The delay (in seconds) after which you should retry the message search.
         * <br>If the value is {@code 0}, you should retry the request after a short delay.
         *
         * @return Delay (in seconds) before retrying the request
         */
        int getRetryAfter();
    }

    // TODO there is also "threads" and "members", have yet to see what they could be useful for
    interface Body extends MessageSearchResponse {
        @Nonnull
        @Unmodifiable
        List<Message> getMessages();

        boolean isDoingDeepHistoricalIndex();

        /**
         * The total number of messages. May not be accurate.
         *
         * @return The total results
         */
        int getTotalResults();
    }
}
