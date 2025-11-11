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

package net.dv8tion.jda.test.compliance;

import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.UpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class EventConsistencyComplianceTest
{
    static SoftAssertions softly = new SoftAssertions();
    static Set<Class<? extends GenericEvent>> eventTypes;
    static Set<Class<? extends GenericEvent>> excludedTypes;

    @BeforeAll
    static void setup()
    {
        Reflections events = new Reflections("net.dv8tion.jda.api.events");
        eventTypes = events.getSubTypesOf(GenericEvent.class);
        excludedTypes = new HashSet<>(Arrays.asList(
            // Special casing
            UpdateEvent.class, Event.class, GenericEvent.class
        ));
    }

    @AfterEach
    void assertAllSoftly()
    {
        softly.assertAll();
    }

    @Test
    void testListenerAdapter()
    {
        Class<ListenerAdapter> adapter = ListenerAdapter.class;
        Set<String> found = new HashSet<>();

        found.add("onGenericEvent");
        found.add("onGenericUpdate");

        for (Class<? extends GenericEvent> type : eventTypes)
        {
            if (excludedTypes.contains(type))
                continue;
            String name = type.getSimpleName();
            String methodName = "on" + name.substring(0, name.length() - "Event".length());

            AtomicReference<Method> methodRef = new AtomicReference<>();
            softly.assertThatCode(() -> methodRef.set(adapter.getDeclaredMethod(methodName, type)))
                .as("Method for event " + type + " is missing!")
                .doesNotThrowAnyException();

            Method method = methodRef.get();
            if (method != null)
            {
                validateHookMethod(method);
                found.add(methodName);
            }
        }

        for (Method method : adapter.getDeclaredMethods())
        {
            int modifiers = method.getModifiers();
            boolean isHookMethod =  Modifier.isPublic(modifiers) && !Modifier.isFinal(modifiers);
            if (!isHookMethod || method.getAnnotation(Deprecated.class) != null)
                continue;

            softly.assertThat(found.contains(method.getName()))
                .as("Dangling method found in ListenerAdapter " + method.getName())
                .isTrue();
        }
    }

    private static void validateHookMethod(Method method)
    {
        int modifiers = method.getModifiers();
        softly.assertThat(modifiers)
            .as("Modifiers for method %s", method.getName())
            .isEqualTo(Modifier.PUBLIC);
        softly.assertThat(method.getReturnType())
            .as("Return type for method %s", method.getName())
            .isSameAs(Void.TYPE);
    }
}
