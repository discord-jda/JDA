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

package net.dv8tion.jda.test.assertions.events;

import net.dv8tion.jda.internal.JDAImpl;
import org.junit.jupiter.api.function.ThrowingConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.*;

public class EventFiredAssertions<T>
{
    private final Class<T> eventType;
    private final JDAImpl jda;
    private final List<ThrowingConsumer<T>> assertions = new ArrayList<>();

    public EventFiredAssertions(Class<T> eventType, JDAImpl jda)
    {
        this.eventType = eventType;
        this.jda = jda;
    }

    public <V> EventFiredAssertions<T> hasGetterWithValueEqualTo(Function<T, V> getter, V value)
    {
        assertions.add(event -> assertThat(getter.apply(event)).isEqualTo(value));
        return this;
    }

    public void isFiredBy(Runnable runnable)
    {
        doNothing().when(jda).handleEvent(assertArg(arg -> {
            assertThat(arg).isInstanceOf(eventType);
            T casted = eventType.cast(arg);
            for (ThrowingConsumer<T> assertion : assertions)
                assertion.accept(casted);
        }));

        runnable.run();

        verify(jda, times(1)).handleEvent(any());
    }
}
