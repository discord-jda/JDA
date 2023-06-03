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
package net.dv8tion.jda.api.hooks;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.utils.ClassWalker;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.slf4j.Logger;

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
    private static final Logger LOGGER = JDALogger.getLog(AnnotatedEventManager.class);
    private final Set<Object> listeners = ConcurrentHashMap.newKeySet();
    private final Map<Class<?>, Map<Object, List<Method>>> methods = new ConcurrentHashMap<>();

    @Override
    public void register(@Nonnull Object listener)
    {
        if (listener.getClass().isArray())
        {
            for (Object o : ((Object[]) listener))
                register(o);
            return;
        }

        if (listeners.add(listener))
        {
            registerListenerMethods(listener);
        }
    }

    @Override
    public void unregister(@Nonnull Object listener)
    {
        if (listener.getClass().isArray())
        {
            for (Object o : ((Object[]) listener))
                unregister(o);
            return;
        }

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
    public void handle(@Nonnull GenericEvent event)
    {
        for (Class<?> eventClass : ClassWalker.walk(event.getClass()))
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
                        if (throwable instanceof Error)
                            throw (Error) throwable;
                    }
                }));
            }
        }
    }

    private void updateMethods()
    {
        methods.clear();
        for (Object listener : listeners)
        {
            registerListenerMethods(listener);
        }
    }

    private void registerListenerMethods(Object listener)
    {
        boolean isClass = listener instanceof Class;
        Class<?> c = isClass ? (Class<?>) listener : listener.getClass();
        Method[] allMethods = c.getDeclaredMethods();
        for (Method m : allMethods)
        {
            if (!m.isAnnotationPresent(SubscribeEvent.class))
                continue;
            //Skip member methods if listener is a Class
            if (isClass && !Modifier.isStatic(m.getModifiers()))
                continue;

            final Class<?>[] parameterTypes = m.getParameterTypes();
            if (parameterTypes.length != 1 || !GenericEvent.class.isAssignableFrom(parameterTypes[0]))
            {
                LOGGER.warn("Method '{}' annotated with @{} must have at most 1 parameter, which implements GenericEvent", m, SubscribeEvent.class.getSimpleName());
                continue;
            }

            Class<?> eventClass = parameterTypes[0];
            methods.computeIfAbsent(eventClass, k -> new ConcurrentHashMap<>())
                    .computeIfAbsent(listener, k -> new CopyOnWriteArrayList<>())
                    .add(m);
        }
    }
}
