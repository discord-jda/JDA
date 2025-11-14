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

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.internal.utils.EntityString;

import java.time.OffsetDateTime;

import javax.annotation.Nonnull;

/**
 * {@link PaginationAction PaginationAction} that paginates the pinned messages in a {@link MessageChannel}.
 * <br>Note that this implementation is not considered thread-safe as modifications to the cache are not done
 * with a lock. Calling methods on this class from multiple threads is not recommended.
 *
 * <p><b>Limits:</b><br>
 * Minimum - 1
 * <br>Maximum - 50
 *
 * @see MessageChannel#retrievePinnedMessages()
 */
public interface PinnedMessagePaginationAction
        extends PaginationAction<PinnedMessagePaginationAction.PinnedMessage, PinnedMessagePaginationAction> {
    /**
     * A pinned message instance.
     *
     * @see #getTimePinned()
     * @see #getMessage()
     */
    class PinnedMessage {
        private final OffsetDateTime pinnedAt;
        private final Message message;

        public PinnedMessage(OffsetDateTime pinnedAt, Message message) {
            this.pinnedAt = pinnedAt;
            this.message = message;
        }

        /**
         * The time when this message was pinned.
         *
         * @return The pin timestamp
         */
        @Nonnull
        public OffsetDateTime getTimePinned() {
            return pinnedAt;
        }

        /**
         * The message that was pinned.
         *
         * @return The pinned message instance
         */
        @Nonnull
        public Message getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return new EntityString(this)
                    .addMetadata("pinned_at", pinnedAt)
                    .addMetadata("message", message)
                    .toString();
        }
    }
}
