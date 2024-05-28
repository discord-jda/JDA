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

import net.dv8tion.jda.api.requests.Method;
import net.dv8tion.jda.internal.requests.restaction.pagination.PollVotersPaginationActionImpl;
import net.dv8tion.jda.test.IntegrationTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

public class PollVotersPaginationTest extends IntegrationTest
{
    private PollVotersPaginationActionImpl newAction()
    {
        return new PollVotersPaginationActionImpl(jda, "381886978205155338", "1228092239079804968", 5);
    }

    @Test
    void testDefaults()
    {
        assertThatRequestFrom(newAction())
            .hasMethod(Method.GET)
            .hasCompiledRoute("channels/381886978205155338/polls/1228092239079804968/answers/5?limit=1000&after=0")
            .whenQueueCalled();
    }

    @Test
    void testSkipTo()
    {
        long randomId = random.nextLong();
        assertThatRequestFrom(newAction().skipTo(randomId))
            .hasMethod(Method.GET)
            .hasQueryParams("limit", "1000", "after", Long.toUnsignedString(randomId))
            .whenQueueCalled();
    }

    @Test
    void testOrder()
    {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> newAction().reverse())
            .withMessage("Cannot use PaginationOrder.BACKWARD for this pagination endpoint.");
    }
}
