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
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.utils.messages.MessagePollBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Poll sent with messages.
 *
 * @see Message#getPoll()
 * @see Message#endPoll()
 */
public interface MessagePoll
{
    /** Maximum length of a {@link MessagePollBuilder#setTitle(String) poll question title} ({@value}) */
    int MAX_QUESTION_TEXT_LENGTH = 300;
    /** Maximum length of a {@link MessagePollBuilder#addAnswer(String)} poll answer title} ({@value}) */
    int MAX_ANSWER_TEXT_LENGTH = 55;
    /** Maximum amount of {@link MessagePollBuilder#addAnswer(String) poll answers} ({@value}) */
    int MAX_ANSWERS = 10;
    /** Maximum {@link MessagePollBuilder#setDuration(Duration) duration} of poll ({@value}) */
    long MAX_DURATION_HOURS = 7 * 24;

    /**
     * The layout of the poll.
     *
     * @return The poll layout, or {@link LayoutType#UNKNOWN} if unknown
     */
    @Nonnull
    LayoutType getLayout();

    /**
     * The poll question, representing the title.
     *
     * @return {@link Question}
     */
    @Nonnull
    Question getQuestion();

    /**
     * The poll answers.
     *
     * <p>Each answer also has the current {@link Answer#getVotes() votes}.
     * The votes might not be finalized and might be incorrect before the poll has expired,
     * see {@link #isFinalizedVotes()}.
     *
     * @return Immutable {@link List} of {@link Answer}
     */
    @Nonnull
    List<Answer> getAnswers();

    /**
     * The time when this poll will automatically expire.
     *
     * <p>The author of the poll can always expire the poll manually, using {@link Message#endPoll()}.
     *
     * @return {@link OffsetDateTime} representing the time when the poll expires automatically, or null if it never expires
     */
    @Nullable
    OffsetDateTime getTimeExpiresAt();

    /**
     * Whether this poll allows multiple answers to be selected.
     *
     * @return True, if this poll allows multi selection
     */
    boolean isMultiAnswer();

    /**
     * Whether this poll is finalized and recounted.
     *
     * <p>The votes for answers might be inaccurate due to eventual consistency, until this is true.
     * Finalization does not mean the votes cannot change anymore, use {@link #isExpired()} to check if a poll has ended.
     *
     * @return True, if the votes have been precisely counted
     */
    boolean isFinalizedVotes();

    /**
     * Whether this poll has passed its {@link #getTimeExpiresAt() expiration time}.
     *
     * @return True, if this poll is expired.
     */
    default boolean isExpired()
    {
        return getTimeExpiresAt().isBefore(OffsetDateTime.now());
    }

    /**
     * The question for a poll.
     */
    class Question
    {
        private final String text;
        private final EmojiUnion emoji;

        public Question(String text, Emoji emoji)
        {
            this.text = text;
            this.emoji = (EmojiUnion) emoji;
        }

        /**
         * The poll question title.
         *
         * <p>Shown above all answers.
         *
         * @return The question title
         */
        @Nonnull
        public String getText()
        {
            return text;
        }

        /**
         * Possible emoji related to the poll question.
         *
         * @return Possibly-null emoji
         */
        @Nullable
        public EmojiUnion getEmoji()
        {
            return emoji;
        }
    }

    /**
     * One of the answers for a poll.
     *
     * <p>Provides the current {@link #getVotes()} and whether you have voted for it.
     */
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

        /**
         * The id of this answer.
         *
         * @return The answer id.
         */
        public long getId()
        {
            return id;
        }

        /**
         * The text content of the answer.
         *
         * @return The answer label.
         */
        @Nonnull
        public String getText()
        {
            return text;
        }

        /**
         * The emoji assigned to this answer.
         *
         * @return {@link EmojiUnion}
         */
        @Nullable
        public EmojiUnion getEmoji()
        {
            return emoji;
        }

        /**
         * The number of votes this answer has received so far.
         *
         * <p>This might not be {@link #isFinalizedVotes() finalized}.
         *
         * @return The current number of votes
         */
        public int getVotes()
        {
            return votes;
        }

        /**
         * Whether the answer was voted for by the currently logged in account.
         *
         * @return True, if the bot has voted for this.
         */
        public boolean isSelfVoted()
        {
            return selfVoted;
        }
    }

    /**
     * The poll layout.
     *
     * <p>Currently always {@link #DEFAULT}.
     */
    enum LayoutType
    {
        DEFAULT(1),
        UNKNOWN(-1);

        private final int key;

        LayoutType(int key)
        {
            this.key = key;
        }

        /**
         * The raw API key used to identify this layout.
         *
         * @return The API key
         */
        public int getKey()
        {
            return key;
        }

        /**
         * Resolves the provided raw API key to the layout enum constant.
         *
         * @param  key
         *         The API key
         *
         * @return The layout type or {@link #UNKNOWN}
         */
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
