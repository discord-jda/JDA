/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.hooks;

import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.events.Event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class AnnotatedEventManager implements IEventManager
{
    private final Set<Object> listeners = new HashSet<>();
    private final Map<Class<? extends Event>, Map<Object, Method>> methods = new HashMap<>();

    @Override
    public void register(Object listener)
    {
        if (listeners.add(listener))
        {
            updateMethods();
        }
    }

    @Override
    public void unregister(Object listener)
    {
        if (listeners.remove(listener))
        {
            updateMethods();
        }
    }

    @Override
    public List<Object> getRegisteredListeners()
    {
        return Collections.unmodifiableList(new LinkedList<>(listeners));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handle(Event event)
    {
        Class<? extends Event> eventClass = event.getClass();
        do
        {
            Map<Object, Method> listeners = methods.get(eventClass);
            if (listeners != null)
            {
                listeners.entrySet().forEach(e -> {
                    try
                    {
                        e.getValue().setAccessible(true);
                        e.getValue().invoke(e.getKey(), event);
                    }
                    catch (IllegalAccessException | InvocationTargetException e1)
                    {
                        JDAImpl.LOG.log(e1);
                    }
                    catch (Throwable throwable)
                    {
                        JDAImpl.LOG.fatal("One of the EventListeners had an uncaught exception");
                        JDAImpl.LOG.log(throwable);
                    }
                });
            }
            eventClass = eventClass == Event.class ? null : (Class<? extends Event>) eventClass.getSuperclass();
        }
        while (eventClass != null);
    }

    private void updateMethods()
    {
        methods.clear();
        for (Object listener : listeners)
        {
            Class<?> c = listener.getClass();
            Method[] allMethods = c.getDeclaredMethods();
            for (Method m : allMethods) {
                if (!m.isAnnotationPresent(SubscribeEvent.class))
                {
                    continue;
                }
                Class<?>[] pType  = m.getParameterTypes();
                if (pType.length == 1 && Event.class.isAssignableFrom(pType[0]))
                {
                    @SuppressWarnings("unchecked")
                    Class<? extends Event> eventClass = (Class<? extends Event>) pType[0];
                    if (!methods.containsKey(eventClass))
                    {
                        methods.put(eventClass, new HashMap<>());
                    }
                    methods.get(eventClass).put(listener, m);
                }
            }
        }
    }
}
