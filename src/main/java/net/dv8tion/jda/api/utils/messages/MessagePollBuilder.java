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

package net.dv8tion.jda.api.utils.messages;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.entities.messages.MessagePoll;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Builder for {@link MessagePollData}
 *
 * @see MessageCreateBuilder#setPoll(MessagePollData)
 */
public class MessagePollBuilder
{
    private final List<MessagePoll.Answer> answers = new ArrayList<>(MessagePoll.MAX_ANSWERS);
    private MessagePoll.LayoutType layout = MessagePoll.LayoutType.DEFAULT;
    private String title;
    private Duration duration = Duration.ofHours(24);
    private boolean isMultiAnswer;

    /**
     * Create a new builder instance
     *
     * @param  title
     *         The poll title (up to {@link MessagePoll#MAX_QUESTION_TEXT_LENGTH} characters)
     *
     * @throws IllegalArgumentException
     *         If the title is blank or longer than {@link MessagePoll#MAX_QUESTION_TEXT_LENGTH} characters
     */
    public MessagePollBuilder(@Nonnull String title)
    {
        this.setTitle(title);
    }

    /**
     * Creates a new builder, initialized by the provided {@link MessagePoll} instance.
     *
     * @param poll
     *        The poll to copy from
     *
     * @throws IllegalArgumentException
     *         If null is provided
     */
    public MessagePollBuilder(@Nonnull MessagePoll poll)
    {
        Checks.notNull(poll, "Poll");
        this.title = poll.getQuestion().getText();
        this.isMultiAnswer = poll.isMultiAnswer();
        this.layout = poll.getLayout();
        for (MessagePoll.Answer answer : poll.getAnswers())
            addAnswer(answer.getText(), answer.getEmoji());
    }

    /**
     * They poll layout.
     *
     * @param  layout
     *         The layout
     *
     * @throws IllegalArgumentException
     *         If null or {@link net.dv8tion.jda.api.entities.messages.MessagePoll.LayoutType#UNKNOWN UNKNOWN} is provided
     *
     * @return The updated builder
     */
    @Nonnull
    public MessagePollBuilder setLayout(@Nonnull MessagePoll.LayoutType layout)
    {
        Checks.notNull(layout, "Layout");
        Checks.check(layout != MessagePoll.LayoutType.UNKNOWN, "Layout cannot be UNKNOWN");

        this.layout = layout;
        return this;
    }

    /**
     * Change the title for this poll.
     *
     * @param  title
     *         The poll title (up to {@link MessagePoll#MAX_QUESTION_TEXT_LENGTH} characters)
     *
     * @throws IllegalArgumentException
     *         If the title is blank or longer than {@link MessagePoll#MAX_QUESTION_TEXT_LENGTH} characters
     *
     * @return The updated builder
     */
    @Nonnull
    public MessagePollBuilder setTitle(@Nonnull String title)
    {
        Checks.notBlank(title, "Title");
        title = title.trim();
        Checks.notLonger(title, MessagePoll.MAX_QUESTION_TEXT_LENGTH, "Title");

        this.title = title;
        return this;
    }

    /**
     * Change the duration for this poll.
     * <br>Default: {@code 1} day
     *
     * <p>The poll will automatically expire after this duration.
     *
     * @param  duration
     *         The duration of this poll (in hours resolution)
     *
     * @throws IllegalArgumentException
     *         If the duration is null, less than 1 hour, or longer than {@value  MessagePoll#MAX_DURATION_HOURS} hours (7 days)
     *
     * @return The updated builder
     */
    @Nonnull
    public MessagePollBuilder setDuration(@Nonnull Duration duration)
    {
        Checks.notNull(duration, "Duration");
        Checks.positive(duration.toHours(), "Duration");
        Checks.notLonger(duration, Duration.ofHours(MessagePoll.MAX_DURATION_HOURS), TimeUnit.HOURS, "Duration");

        this.duration = duration;
        return this;
    }

    /**
     * Change the duration for this poll.
     * <br>Default: {@code 1} day
     *
     * <p>The poll will automatically expire after this duration.
     *
     * @param  duration
     *         The duration of this poll (in hours resolution)
     * @param  unit
     *         The time unit for the duration
     *
     * @throws IllegalArgumentException
     *         If the time unit is null or the duration is not between 1 and {@value  MessagePoll#MAX_DURATION_HOURS} hours (7 days) long
     *
     * @return The updated builder
     */
    @Nonnull
    public MessagePollBuilder setDuration(long duration, @Nonnull TimeUnit unit)
    {
        Checks.notNull(unit, "TimeUnit");
        return setDuration(Duration.ofHours(unit.toHours(duration)));
    }

    /**
     * Whether this poll allows selecting multiple answers.
     * <br>Default: {@code false}
     *
     * @param  multiAnswer
     *         True, if this poll should allow multiple answers
     *
     * @return The updated builder
     */
    @Nonnull
    public MessagePollBuilder setMultiAnswer(boolean multiAnswer)
    {
        isMultiAnswer = multiAnswer;
        return this;
    }

    /**
     * Add an answer to this poll.
     *
     * @param  title
     *         The answer title
     *
     * @throws IllegalArgumentException
     *         If the title is null, blank, or longer than {@value MessagePoll#MAX_ANSWER_TEXT_LENGTH} characters
     *
     * @return The updated builder
     */
    @Nonnull
    public MessagePollBuilder addAnswer(@Nonnull String title)
    {
        return addAnswer(title, null);
    }

    /**
     * Add an answer to this poll.
     *
     * @param  title
     *         The answer title
     * @param  emoji
     *         Optional emoji to show next to the answer text
     *
     * @throws IllegalArgumentException
     *         If the title is null, blank, or longer than {@value MessagePoll#MAX_ANSWER_TEXT_LENGTH} characters
     *
     * @return The updated builder
     */
    @Nonnull
    public MessagePollBuilder addAnswer(@Nonnull String title, @Nullable Emoji emoji)
    {
        Checks.notBlank(title, "Answer title");
        title = title.trim();
        Checks.notLonger(title, MessagePoll.MAX_ANSWER_TEXT_LENGTH, "Answer title");
        Checks.check(this.answers.size() < MessagePoll.MAX_ANSWERS, "Poll cannot have more than %d answers", MessagePoll.MAX_ANSWERS);

        this.answers.add(new MessagePoll.Answer(this.answers.size() + 1, title, (EmojiUnion) emoji, 0, false));
        return this;
    }

    /**
     * Build the poll data.
     *
     * @throws IllegalStateException
     *         If no answers have been added to the builder
     *
     * @return {@link MessagePollData}
     */
    @Nonnull
    public MessagePollData build()
    {
        if (answers.isEmpty())
            throw new IllegalStateException("Cannot build a poll without answers");
        return new MessagePollData(
            layout,
            new MessagePoll.Question(title, null),
            new ArrayList<>(answers),
            duration,
            isMultiAnswer
        );
    }
}
