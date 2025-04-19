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

import net.dv8tion.jda.api.entities.messages.MessagePoll;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;
import java.util.List;

public class MessagePollImpl implements MessagePoll
{
    private final LayoutType layout;
    private final Question question;
    private final List<Answer> answers;
    private final OffsetDateTime expiresAt;
    private final boolean isMultiAnswer;
    private final boolean isFinalizedVotes;

    public MessagePollImpl(LayoutType layout, Question question, List<Answer> answers, OffsetDateTime expiresAt, boolean isMultiAnswer, boolean isFinalizedVotes)
    {
        this.layout = layout;
        this.question = question;
        this.answers = answers;
        this.expiresAt = expiresAt;
        this.isMultiAnswer = isMultiAnswer;
        this.isFinalizedVotes = isFinalizedVotes;
    }

    @Nonnull
    @Override
    public LayoutType getLayout()
    {
        return layout;
    }

    @Nonnull
    @Override
    public Question getQuestion()
    {
        return question;
    }

    @Nonnull
    @Override
    public List<Answer> getAnswers()
    {
        return answers;
    }

    @Nonnull
    @Override
    public OffsetDateTime getTimeExpiresAt()
    {
        return expiresAt;
    }

    @Override
    public boolean isMultiAnswer()
    {
        return isMultiAnswer;
    }

    @Override
    public boolean isFinalizedVotes()
    {
        return isFinalizedVotes;
    }
}
