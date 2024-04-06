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

package net.dv8tion.jda.test;

import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.requests.Requester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mock;

import javax.annotation.Nonnull;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

public class IntegrationTest
{
    protected Random random = new Random(4242);
    @Mock
    protected JDAImpl jda;
    @Mock
    protected Requester requester;
    @Mock
    protected ScheduledExecutorService scheduledExecutorService;

    private AutoCloseable closeable;
    private int expectedRequestCount;

    @BeforeEach
    protected final void setup()
    {
        expectedRequestCount = 0;
        closeable = openMocks(this);
        when(jda.getRequester()).thenReturn(requester);
    }

    @AfterEach
    protected final void teardown(TestInfo testInfo) throws Exception
    {
        verify(
            requester,
            times(expectedRequestCount).description("Requests sent by " + testInfo.getDisplayName())
        ).request(any());
        closeable.close();
    }

    @Nonnull
    protected DataObject normalizeRequestBody(@Nonnull DataObject body)
    {
        return body;
    }

    protected RestActionAssertions assertThatNextRequest()
    {
        expectedRequestCount += 1;
        return RestActionAssertions.assertThatNextAction(requester)
                .withNormalizedBody(this::normalizeRequestBody);
    }
}
