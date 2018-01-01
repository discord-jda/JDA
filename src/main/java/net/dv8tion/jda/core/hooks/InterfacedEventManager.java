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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * An {@link net.dv8tion.jda.core.hooks.IEventManager IEventManager} implementation
 * that uses the {@link net.dv8tion.jda.core.hooks.EventListener EventListener} interface for
 * event listeners.
 *
 * <p>This only accepts listeners that implement {@link net.dv8tion.jda.core.hooks.EventListener EventListener}
 * <br>An adapter implementation is {@link net.dv8tion.jda.core.hooks.ListenerAdapter ListenerAdapter} which
 * provides methods for each individual {@link net.dv8tion.jda.core.events.Event}.
 *
 * <p><b>This is the default IEventManager used by JDA</b>
 *
 * @see net.dv8tion.jda.core.hooks.AnnotatedEventManager
 * @see net.dv8tion.jda.core.hooks.IEventManager
 */
public class InterfacedEventManager implements IEventManager
{
    private final CopyOnWriteArrayList<EventListener> listeners = new CopyOnWriteArrayList<>();

    public InterfacedEventManager()
    {

    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException
     *         If the provided listener does not implement {@link net.dv8tion.jda.core.hooks.EventListener EventListener}
     */
    @Override
    public void register(Object listener)
    {
        if (!(listener instanceof EventListener))
        {
            throw new IllegalArgumentException("Listener must implement EventListener");
        }
        listeners.add(((EventListener) listener));
    }

    @Override
    public void unregister(Object listener)
    {
        listeners.remove(listener);
    }

    @Override
    public List<Object> getRegisteredListeners()
    {
        return Collections.unmodifiableList(new LinkedList<>(listeners));
    }

    @Override
    public void handle(Event event)
    {
        for (EventListener listener : listeners)
        {
            try
            {
                listener.onEvent(event);
            }
            catch (Throwable throwable)
            {
                JDAImpl.LOG.error("One of the EventListeners had an uncaught exception", throwable);
            }
        }
    }
}
