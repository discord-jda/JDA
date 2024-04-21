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

package net.dv8tion.jda.test.entities.message;

import net.dv8tion.jda.api.entities.messages.MessagePoll;
import net.dv8tion.jda.api.utils.messages.MessagePollBuilder;
import net.dv8tion.jda.test.ChecksHelper;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static net.dv8tion.jda.test.ChecksHelper.*;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

public class MessagePollDataTest
{
    @Test
    void testInvalidInputs()
    {
        assertStringChecks("Title", MessagePollBuilder::new)
            .checksNotNull()
            .checksNotBlank()
            .checksNotLonger(300);

        MessagePollBuilder builder = new MessagePollBuilder("test title");

        assertEnumChecks("Layout", builder::setLayout)
            .checksNotNull()
            .checkIsNot(MessagePoll.LayoutType.UNKNOWN);

        assertDurationChecks("Duration", builder::setDuration)
            .checksNotNull()
            .checksPositive()
            .checksNotLonger(Duration.ofHours(7 * 24), TimeUnit.HOURS);

        ChecksHelper.<TimeUnit>assertChecks("TimeUnit", (unit) -> builder.setDuration(1, unit))
            .checksNotNull();

        assertLongChecks("Duration", (duration) -> builder.setDuration(duration, TimeUnit.SECONDS))
            .checksPositive()
            .throwsFor(TimeUnit.DAYS.toSeconds(8), "Duration may not be longer than 168 hours (7 days). Provided: 192 hours (8 days)");

        assertStringChecks("Answer title", builder::addAnswer)
            .checksNotNull()
            .checksNotBlank()
            .checksNotLonger(55);

        assertThatIllegalStateException()
            .isThrownBy(builder::build)
            .withMessage("Cannot build a poll without answers");

        for (int i = 0; i < 10; i++)
            builder.addAnswer("Answer " + i);

        assertThatIllegalArgumentException()
            .isThrownBy(() -> builder.addAnswer("Answer " + 10))
            .withMessage("Poll cannot have more than 10 answers");
    }
}
