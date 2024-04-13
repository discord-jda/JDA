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

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.List;

public interface MessagePoll
{
    @Nonnull
    LayoutType getLayout();

    @Nonnull
    Question getQuestion();

    @Nonnull
    List<Answer> getAnswers();

    @Nonnull
    OffsetDateTime getTimeExpiresAt();

    boolean isMultiAnswer();

    boolean isFinalizedVotes();


    class Question
    {
        private final String text;
        private final EmojiUnion emoji;

        public Question(String text, Emoji emoji)
        {
            this.text = text;
            this.emoji = (EmojiUnion) emoji;
        }

        @Nonnull
        public String getText()
        {
            return text;
        }

        @Nullable
        public EmojiUnion getEmoji()
        {
            return emoji;
        }
    }

    class Answer
    {
        private final long id;
        private final String text;
        private final EmojiUnion emoji;
        private final int votes;
        private final boolean selfVoted;

        public Answer(long id, String text, EmojiUnion emoji, int votes, boolean selfVoted)
        {
            this.id = id;
            this.text = text;
            this.emoji = emoji;
            this.votes = votes;
            this.selfVoted = selfVoted;
        }

        public long getId()
        {
            return id;
        }

        @Nonnull
        public String getText()
        {
            return text;
        }

        @Nullable
        public EmojiUnion getEmoji()
        {
            return emoji;
        }

        public int getVotes()
        {
            return votes;
        }

        public boolean isSelfVoted()
        {
            return selfVoted;
        }
    }

    enum LayoutType
    {
        DEFAULT(1),
        UNKNOWN(-1);

        private final int key;

        LayoutType(int key)
        {
            this.key = key;
        }

        public int getKey()
        {
            return key;
        }

        public static LayoutType fromKey(int key)
        {
            for (LayoutType type : values())
            {
                if (type.key == key)
                    return type;
            }
            return UNKNOWN;
        }
    }
}
