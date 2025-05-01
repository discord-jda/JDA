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

import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.requests.Requester;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.test.assertions.restaction.RestActionAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mock;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

public class IntegrationTest
{
    protected Random random = new Random();
    @Mock
    protected JDAImpl jda;
    @Mock
    protected Requester requester;
    @Mock
    protected ScheduledExecutorService scheduledExecutorService;

    private AutoCloseable closeable;
    private int expectedRequestCount;

    private TestInfo testInfo;

    @BeforeEach
    protected final void setup(TestInfo info)
    {
        testInfo = info;

        random.setSeed(4242);
        expectedRequestCount = 0;
        closeable = openMocks(this);
        when(jda.getRequester()).thenReturn(requester);
        when(jda.getEntityBuilder()).thenReturn(new EntityBuilder(jda));
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

    @CheckReturnValue
    protected RestActionAssertions assertThatRequestFrom(@Nonnull RestAction<?> action)
    {
        expectedRequestCount += 1;
        return RestActionAssertions.assertThatNextAction(requester, action)
                .withNormalizedBody(this::normalizeRequestBody);
    }

    protected void assertThatNoRequestsWereSent()
    {
        verify(requester, never()).request(any());
    }

    protected <T> void whenSuccess(RestActionImpl<T> action, DataArray array, Consumer<T> assertion)
    {
        Response response = mock();
        Request<T> request = mock();

        when(response.isOk()).thenReturn(true);
        when(response.getArray()).thenReturn(array);

        doNothing().when(request).onSuccess(assertArg(assertion));

        action.handleResponse(response, request);

        verify(request, times(1)).onSuccess(any());
    }

    protected String randomSnowflake()
    {
        return Long.toUnsignedString(random.nextLong());
    }

    protected void withCacheFlags(EnumSet<CacheFlag> flags)
    {
        when(jda.getCacheFlags()).thenReturn(flags);
    }

    protected DataObject getSampleObject()
    {
        Class<?> currentClass = testInfo.getTestClass().orElseThrow(AssertionError::new);
        Method testMethod = testInfo.getTestMethod().orElseThrow(AssertionError::new);
        String fileName = currentClass.getSimpleName() + "_" + testMethod.getName() + ".json";

        try (InputStream stream = currentClass.getResourceAsStream(fileName))
        {
            assertThat(stream).as("Loading sample from resource file '%s'", fileName).isNotNull();
            return DataObject.fromJson(stream);
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }
}
