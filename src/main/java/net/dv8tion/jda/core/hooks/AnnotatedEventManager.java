/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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
package net.dv8tion.jda.core.hooks;

import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.Event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Implementation for {@link net.dv8tion.jda.core.hooks.IEventManager IEventManager}
 * which checks for {@link net.dv8tion.jda.core.hooks.SubscribeEvent SubscribeEvent} annotations on both
 * <b>static</b> and <b>member</b> methods.
 *
 * <p>Listeners for this manager do <u>not</u> need to implement {@link net.dv8tion.jda.core.hooks.EventListener EventListener}
 * <br>Example
 * <pre><code>
 *     public class Foo
 *     {
 *        {@literal @SubscribeEvent}
 *         public void onMsg(MessageReceivedEvent event)
 *         {
 *             System.out.printf("%s: %s\n", event.getAuthor().getName(), event.getMessage().getContentDisplay());
 *         }
 *     }
 * </code></pre>
 *
 * @see net.dv8tion.jda.core.hooks.InterfacedEventManager
 * @see net.dv8tion.jda.core.hooks.IEventManager
 * @see net.dv8tion.jda.core.hooks.SubscribeEvent
 */
public class AnnotatedEventManager implements IEventManager
{
    private final Set<Object> listeners = new HashSet<>();
    private final Map<Class<? extends Event>, Map<Object, List<Method>>> methods = new HashMap<>();

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
            Map<Object, List<Method>> listeners = methods.get(eventClass);
            if (listeners != null)
            {
                listeners.entrySet().forEach(e -> e.getValue().forEach(method ->
                {
                    try
                    {
                        method.setAccessible(true);
                        method.invoke(e.getKey(), event);
                    }
                    catch (IllegalAccessException | InvocationTargetException e1)
                    {
                        JDAImpl.LOG.error("Couldn't access annotated eventlistener method", e1);
                    }
                    catch (Throwable throwable)
                    {
                        JDAImpl.LOG.error("One of the EventListeners had an uncaught exception", throwable);
                    }
                }));
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
                if (pType.length == 1 && Event.class.isAssignableFrom(pType[0]))
                {
                    @SuppressWarnings("unchecked")
                    Class<? extends Event> eventClass = (Class<? extends Event>) pType[0];
                    if (!methods.containsKey(eventClass))
                    {
                        methods.put(eventClass, new HashMap<>());
                    }

                    if (!methods.get(eventClass).containsKey(listener))
                    {
                        methods.get(eventClass).put(listener, new ArrayList<>());
                    }

                    methods.get(eventClass).get(listener).add(m);
                }
            }
        }
    }
}
