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

package net.dv8tion.jda.test.restaction;

import net.dv8tion.jda.internal.requests.CompletedRestAction;
import net.dv8tion.jda.test.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RestActionTest extends IntegrationTest
{
    @Test
    void testMapOperator()
    {
        assertThat(
            new CompletedRestAction<>(jda, "12345")
                .map(Integer::parseInt)
                .complete()
        ).isEqualTo(12345);
    }

    @Test
    void testFlatMapOperator()
    {
        assertThat(
            new CompletedRestAction<>(jda, "12345")
                .flatMap(value -> new CompletedRestAction<>(jda, Integer.parseInt(value)))
                .complete()
        ).isEqualTo(12345);

        assertThat(
            new CompletedRestAction<>(jda, "12345")
                .flatMap(
                    value -> value.startsWith("123"),
                    value -> new CompletedRestAction<>(jda, Integer.parseInt(value)))
                .complete()
        ).isEqualTo(12345);

        assertThatThrownBy(() ->
            new CompletedRestAction<>(jda, "12345")
                .flatMap(
                    value -> value.startsWith("wrong"),
                    value -> new CompletedRestAction<>(jda, Integer.parseInt(value)))
                .complete()
        ).isInstanceOf(CancellationException.class).hasMessage("FlatMap condition failed");

        assertThat(
            new CompletedRestAction<>(jda, "12345")
                .flatMap(
                    value -> value.startsWith("wrong"),
                    value -> new CompletedRestAction<>(jda, Integer.parseInt(value)))
                .submit()
        )
            .failsWithin(Duration.ZERO)
            .withThrowableThat()
            .havingRootCause()
            .isInstanceOf(CancellationException.class);
    }

    @Test
    void testDelayOperator()
    {
        when(scheduledExecutorService.schedule(any(Runnable.class), anyLong(), any()))
            .thenReturn(null);

        new CompletedRestAction<>(jda, "12345")
            .delay(Duration.ofSeconds(2), scheduledExecutorService)
            .queue();

        new CompletedRestAction<>(jda, "12345")
            .delay(3, TimeUnit.SECONDS, scheduledExecutorService)
            .queue();

        verify(scheduledExecutorService, times(1))
            .schedule(any(Runnable.class), eq(2000L), eq(TimeUnit.MILLISECONDS));

        verify(scheduledExecutorService, times(1))
            .schedule(any(Runnable.class), eq(3L), eq(TimeUnit.SECONDS));
    }

    @Test
    void testQueueAfter()
    {
        when(scheduledExecutorService.schedule(any(Runnable.class), anyLong(), any()))
            .thenReturn(null);

        new CompletedRestAction<>(jda, "12345")
            .queueAfter(2, TimeUnit.SECONDS, scheduledExecutorService);

        verify(scheduledExecutorService, times(1))
            .schedule(any(Runnable.class), eq(2L), eq(TimeUnit.SECONDS));
    }
}
