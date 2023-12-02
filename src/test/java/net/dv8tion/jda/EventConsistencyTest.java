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

package net.dv8tion.jda;

import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.UpdateEvent;
import net.dv8tion.jda.api.events.self.SelfUpdateDiscriminatorEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class EventConsistencyTest
{
    static Set<Class<? extends GenericEvent>> eventTypes;
    static Set<Class<? extends GenericEvent>> excludedTypes;

    @BeforeAll
    static void setup()
    {
        Reflections events = new Reflections("net.dv8tion.jda.api.events");
        eventTypes = events.getSubTypesOf(GenericEvent.class);
        excludedTypes = new HashSet<>(Arrays.asList(
            // Special casing
            UpdateEvent.class, Event.class, GenericEvent.class,
            // Deprecated / Removed / Unused
            SelfUpdateDiscriminatorEvent.class
        ));
    }

    @Test
    void testListenerAdapter()
    {
        Class<ListenerAdapter> adapter = ListenerAdapter.class;
        Set<String> found = new HashSet<>();

        for (Class<? extends GenericEvent> type : eventTypes)
        {
            if (excludedTypes.contains(type))
                continue;
            String name = type.getSimpleName();
            String methodName = "on" + name.substring(0, name.length() - "Event".length());
            Assertions.assertDoesNotThrow(() -> adapter.getDeclaredMethod(methodName, type), "Method for event " + type + " is missing!");
            found.add(methodName);
        }

        for (Method method : adapter.getDeclaredMethods())
        {
            if (!method.isAccessible() || method.getAnnotation(Deprecated.class) != null)
                continue;
            Assertions.assertTrue(found.contains(method.getName()), "Dangling method found in ListenerAdapter " + method.getName());
        }
    }
}
