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
import org.mockito.Mock;

import javax.annotation.Nonnull;
import java.util.concurrent.ScheduledExecutorService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class IntegrationTest
{
    @Mock
    protected JDAImpl jda;
    @Mock
    protected Requester requester;
    @Mock
    protected ScheduledExecutorService scheduledExecutorService;

    private AutoCloseable closeable;

    @BeforeEach
    protected final void setup()
    {
        closeable = openMocks(this);
        when(jda.getRequester()).thenReturn(requester);
    }

    @AfterEach
    protected final void teardown() throws Exception
    {
        closeable.close();
    }

    @Nonnull
    protected DataObject normalizeRequestBody(@Nonnull DataObject body)
    {
        return body;
    }

    protected void assertNextRequestBodyEquals(DataObject expectedBody)
    {
        doNothing().when(requester).request(assertArg(request -> {
            assertThat(request.getRawBody())
                    .isNotNull()
                    .isInstanceOf(DataObject.class);
            DataObject body = normalizeRequestBody((DataObject) request.getRawBody());

            assertThat(body)
                .withRepresentation(new PrettyRepresentation())
                .isEqualTo(expectedBody);
        }));
    }
}
