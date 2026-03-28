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

package net.dv8tion.jda.internal.entities.messages;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.messages.MessageSearchResponse;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

import javax.annotation.Nonnull;

public class MessageSearchResponseImpl {
    private MessageSearchResponseImpl() {}

    public static class NotReadyImpl implements MessageSearchResponse.NotReady {

        private final int documentsIndexed, retryAfter;

        public NotReadyImpl(int documentsIndexed, int retryAfter) {
            this.documentsIndexed = documentsIndexed;
            this.retryAfter = retryAfter;
        }

        @Override
        public boolean hasFailed() {
            return true;
        }

        @Nonnull
        @Override
        public NotReady asNotReady() {
            return this;
        }

        @Nonnull
        @Override
        public Results asResults() {
            throw new IllegalStateException("The message search has failed");
        }

        @Override
        public int getDocumentsIndexed() {
            return documentsIndexed;
        }

        @Override
        public int getRetryAfter() {
            return retryAfter;
        }
    }

    public static class ResultsImpl implements MessageSearchResponse.Results {
        private final List<Message> messages;
        private final boolean doingDeepHistoricalIndex;
        private final int totalResults;

        public ResultsImpl(List<Message> messages, boolean doingDeepHistoricalIndex, int totalResults) {
            this.messages = messages;
            this.doingDeepHistoricalIndex = doingDeepHistoricalIndex;
            this.totalResults = totalResults;
        }

        @Override
        public boolean hasFailed() {
            return false;
        }

        @Nonnull
        @Override
        public NotReady asNotReady() {
            throw new IllegalStateException("The message search has succeeded");
        }

        @Nonnull
        @Override
        public Results asResults() {
            return this;
        }

        @Nonnull
        @Override
        @Unmodifiable
        public List<Message> getMessages() {
            return messages;
        }

        @Override
        public boolean isDoingDeepHistoricalIndex() {
            return doingDeepHistoricalIndex;
        }

        @Override
        public int getTotalResults() {
            return totalResults;
        }
    }
}
