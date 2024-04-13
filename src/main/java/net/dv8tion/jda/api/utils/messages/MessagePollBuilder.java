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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MessagePollBuilder
{
    private MessagePoll.LayoutType layout = MessagePoll.LayoutType.DEFAULT;
    private String title;
    private Map<Long, MessagePoll.Answer> answers = new LinkedHashMap<>();
    private Duration duration = Duration.ofHours(24);
    private boolean isMultiAnswer;

    public MessagePollBuilder(@Nonnull String title)
    {
        this.setTitle(title);
    }

    @Nonnull
    public MessagePollBuilder setLayout(@Nonnull MessagePoll.LayoutType layout)
    {
        Checks.notNull(layout, "Layout");

        this.layout = layout;
        return this;
    }

    @Nonnull
    public MessagePollBuilder setTitle(@Nonnull String title)
    {
        Checks.notBlank(title, "Title");
        title = title.trim();
        Checks.notLonger(title, MessagePoll.MAX_QUESTION_TEXT_LENGTH, "Poll question title");

        this.title = title;
        return this;
    }

    @Nonnull
    public MessagePollBuilder setDuration(@Nonnull Duration duration)
    {
        Checks.notNull(duration, "Duration");
        Checks.positive(duration.toHours(), "Duration");
        Checks.check(duration.toHours() <= MessagePoll.MAX_DURATION_HOURS, "Poll duration may not be longer than 7 days. Provided: %d hours", duration.toHours());

        this.duration = duration;
        return this;
    }

    @Nonnull
    public MessagePollBuilder setDuration(long duration, @Nonnull TimeUnit unit)
    {
        Checks.notNull(unit, "TimeUnit");
        this.duration = Duration.ofHours(unit.toHours(duration));
        return this;
    }

    @Nonnull
    public MessagePollBuilder setMultiAnswer(boolean multiAnswer)
    {
        isMultiAnswer = multiAnswer;
        return this;
    }

    @Nonnull
    public MessagePollBuilder addAnswer(@Nonnull String title)
    {
        return addAnswer(this.answers.size(), title, null);
    }

    @Nonnull
    public MessagePollBuilder addAnswer(@Nonnull String title, @Nullable Emoji emoji)
    {
        return addAnswer(this.answers.size(), title, emoji);
    }

    @Nonnull
    public MessagePollBuilder addAnswer(long id, @Nonnull String title)
    {
        return addAnswer(id, title, null);
    }

    @Nonnull
    public MessagePollBuilder addAnswer(long id, @Nonnull String title, @Nullable Emoji emoji)
    {
        Checks.notBlank(title, "Answer title");
        title = title.trim();
        Checks.notLonger(title, MessagePoll.MAX_ANSWER_TEXT_LENGTH, "Poll answer title");
        if (!this.answers.containsKey(id))
            Checks.check(this.answers.size() < MessagePoll.MAX_ANSWERS, "Poll cannot have more than %d answers", MessagePoll.MAX_ANSWERS);

        this.answers.put(id, new MessagePoll.Answer(id, title, (EmojiUnion) emoji, 0, false));
        return this;
    }

    @Nonnull
    public MessagePollData build()
    {
        return new MessagePollData(
            layout,
            new MessagePoll.Question(title, null),
            new ArrayList<>(answers.values()),
            duration,
            isMultiAnswer
        );
    }
}
