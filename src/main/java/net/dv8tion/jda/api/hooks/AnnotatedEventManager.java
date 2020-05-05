/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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
package net.dv8tion.jda.api.hooks;

import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.internal.JDAImpl;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Implementation for {@link net.dv8tion.jda.api.hooks.IEventManager IEventManager}
 * which checks for {@link net.dv8tion.jda.api.hooks.SubscribeEvent SubscribeEvent} annotations on both
 * <b>static</b> and <b>member</b> methods.
 *
 * <p>Listeners for this manager do <u>not</u> need to implement {@link net.dv8tion.jda.api.hooks.EventListener EventListener}
 * <br>Example
 * <pre><code>
 * public class Foo
 * {
 *    {@literal @SubscribeEvent}
 *     public void onMsg(MessageReceivedEvent event)
 *     {
 *         System.out.printf("%s: %s\n", event.getAuthor().getName(), event.getMessage().getContentDisplay());
 *     }
 * }
 * </code></pre>
 *
 * @see net.dv8tion.jda.api.hooks.InterfacedEventManager
 * @see net.dv8tion.jda.api.hooks.IEventManager
 * @see net.dv8tion.jda.api.hooks.SubscribeEvent
 */
public class AnnotatedEventManager implements IEventManager
{
    private final Set<Object> listeners = ConcurrentHashMap.newKeySet();
    private final Map<Class<?>, Map<Object, List<Method>>> methods = new ConcurrentHashMap<>();

    @Override
    public void register(@Nonnull Object listener)
    {
        if (listeners.add(listener))
        {
            updateMethods();
        }
    }

    @Override
    public void unregister(@Nonnull Object listener)
    {
        if (listeners.remove(listener))
        {
            updateMethods();
        }
    }

    @Nonnull
    @Override
    public List<Object> getRegisteredListeners()
    {
        return Collections.unmodifiableList(new ArrayList<>(listeners));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handle(@Nonnull GenericEvent event)
    {
        Class<?> eventClass = event.getClass();
        do
        {
            Map<Object, List<Method>> listeners = methods.get(eventClass);
            if (listeners != null)
            {
                listeners.forEach((key, value) -> value.forEach(method ->
                {
                    try
                    {
                        method.setAccessible(true);
                        method.invoke(key, event);
                    }
                    catch (IllegalAccessException | InvocationTargetException e1)
                    {
                        JDAImpl.LOG.error("Couldn't access annotated EventListener method", e1);
                    }
                    catch (Throwable throwable)
                    {
                        JDAImpl.LOG.error("One of the EventListeners had an uncaught exception", throwable);
                    }
                }));
            }
            eventClass = eventClass == Event.class ? null : (Class<? extends GenericEvent>) eventClass.getSuperclass();
        }
        while (eventClass != null);
    }

    private void updateMethods()
    {
        methods.clear();
        for (Object listener : listeners)
        {
            boolean isClass = listener instanceof Class;
            Class<?> c = isClass ? (Class) listener : listener.getClass();
            Method[] allMethods = c.getDeclaredMethods();
            for (Method m : allMethods)
            {
                if (!m.isAnnotationPresent(SubscribeEvent.class) || (isClass && !Modifier.isStatic(m.getModifiers())))
                {
                    continue;
                }
                Class<?>[] pType  = m.getParameterTypes();
                if (pType.length == 1 && GenericEvent.class.isAssignableFrom(pType[0]))
                {
                    Class<?> eventClass = pType[0];
                    if (!methods.containsKey(eventClass))
                    {
                        methods.put(eventClass, new ConcurrentHashMap<>());
                    }

                    if (!methods.get(eventClass).containsKey(listener))
                    {
                        methods.get(eventClass).put(listener, new CopyOnWriteArrayList<>());
                    }

                    methods.get(eventClass).get(listener).add(m);
                }
            }
        }
    }
}
