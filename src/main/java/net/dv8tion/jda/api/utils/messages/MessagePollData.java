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

import net.dv8tion.jda.api.entities.messages.MessagePoll;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.utils.Helpers;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A poll that can be attached to a {@link MessageCreateRequest}.
 *
 * <p><b>Example</b><br>
 * <pre>{@code
 * channel.sendMessage("Hello guys! Check my poll:")
 *   .setPoll(
 *     MessagePollData.builder("Which programming language is better?")
 *       .addAnswer("Java", Emoji.fromFormatted("<:java:1006323566314274856>"))
 *       .addAnswer("Kotlin", Emoji.fromFormatted("<:kotlin:295940257797636096>"))
 *       .build())
 *   .queue()
 * }</pre>
 *
 * @see #builder(String)
 * @see MessageCreateBuilder#setPoll(MessagePollData)
 */
public class MessagePollData implements SerializableData
{
    private final MessagePoll.LayoutType layout;
    private final MessagePoll.Question question;
    private final List<MessagePoll.Answer> answers;
    private final Duration duration;
    private final boolean isMultiAnswer;

    public MessagePollData(MessagePoll.LayoutType layout, MessagePoll.Question question, List<MessagePoll.Answer> answers, Duration duration, boolean isMultiAnswer)
    {
        this.layout = layout;
        this.question = question;
        this.answers = answers;
        this.duration = duration;
        this.isMultiAnswer = isMultiAnswer;
    }

    /**
     * Creates a new {@link MessagePollBuilder}.
     *
     * <p>A poll must have at least one answer.
     *
     * @param  title
     *         The poll title (up to {@value MessagePoll#MAX_QUESTION_TEXT_LENGTH} characters)
     *
     * @throws IllegalArgumentException
     *         If the title is blank or longer than {@value MessagePoll#MAX_QUESTION_TEXT_LENGTH} characters
     *
     * @return {@link MessagePollBuilder}
     */
    @Nonnull
    public static MessagePollBuilder builder(@Nonnull String title)
    {
        return new MessagePollBuilder(title);
    }

    /**
     * Converts a {@link MessagePoll} to a sendable MessagePollData instance.
     * <br>This does not support {@link MessagePollBuilder#setDuration(Duration) duration}, which cannot be derived from an active poll.
     *
     * @param  poll
     *         The poll to copy
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return MessagePollData instance
     */
    @Nonnull
    public static MessagePollData from(@Nonnull MessagePoll poll)
    {
        return new MessagePollBuilder(poll).build();
    }

    @NotNull
    @Override
    public DataObject toData()
    {
        DataObject data = DataObject.empty();

        data.put("duration", TimeUnit.SECONDS.toHours(duration.getSeconds()));
        data.put("allow_multiselect", isMultiAnswer);
        data.put("layout_type", layout.getKey());

        data.put("question", DataObject.empty()
                .put("text", question.getText()));

        data.put("answers", answers.stream()
            .map(answer -> DataObject.empty()
                .put("answer_id", answer.getId())
                .put("poll_media", DataObject.empty()
                    .put("text", answer.getText())
                    .put("emoji", answer.getEmoji())))
            .collect(Helpers.toDataArray()));

        return data;
    }
}
