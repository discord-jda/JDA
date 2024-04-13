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

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MessagePollCreateData implements SerializableData
{
    private final MessagePoll.LayoutType layout;
    private final MessagePoll.Question question;
    private final List<MessagePoll.Answer> answers;
    private final Duration duration;
    private final boolean isMultiAnswer;

    public MessagePollCreateData(MessagePoll.LayoutType layout, MessagePoll.Question question, List<MessagePoll.Answer> answers, Duration duration, boolean isMultiAnswer)
    {
        this.layout = layout;
        this.question = question;
        this.answers = answers;
        this.duration = duration;
        this.isMultiAnswer = isMultiAnswer;
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
