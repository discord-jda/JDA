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

import net.dv8tion.jda.core.events.Event;

import java.util.List;

/**
 * An interface for JDA's EventManager system.
 * <br>This should be registered in the {@link net.dv8tion.jda.core.JDABuilder JDABuilder}
 *
 * <p>JDA provides 2 implementations:
 * <ul>
 *     <li>{@link net.dv8tion.jda.core.hooks.InterfacedEventManager InterfacedEventManager}
 *     <br>Simple implementation that allows {@link net.dv8tion.jda.core.hooks.EventListener EventListener}
 *         instances as listeners.</li>
 *
 *     <li>{@link net.dv8tion.jda.core.hooks.AnnotatedEventManager AnnotatedEventManager}
 *     <br>An implementation that accepts any object and uses the {@link net.dv8tion.jda.core.hooks.SubscribeEvent SubscribeEvent}
 *         annotation to handle events.</li>
 * </ul>
 *
 * <p>The default event manager is {@link net.dv8tion.jda.core.hooks.InterfacedEventManager InterfacedEventManager}
 * <br>Use {@link net.dv8tion.jda.core.JDABuilder#setEventManager(IEventManager) JDABuilder.setEventManager(IEventManager)}
 * to set the preferred event manager implementation.
 * <br>You can only use one implementation per JDA instance!
 *
 * @see net.dv8tion.jda.core.hooks.InterfacedEventManager
 * @see net.dv8tion.jda.core.hooks.AnnotatedEventManager
 */
public interface IEventManager
{

    /**
     * Registers the specified listener
     * <br>Accepted types may be specified by implementations
     *
     * @param listener
     *        A listener object
     */
    void register(Object listener);

    /**
     * Removes the specified listener
     *
     * @param listener
     *        The listener object to remove
     */
    void unregister(Object listener);

    /**
     * Handles the provided {@link net.dv8tion.jda.core.events.Event Event}.
     * How this is handled is specified by the implementation.
     *
     * @param event
     *        The event to handle
     */
    void handle(Event event);

    /**
     * The currently registered listeners
     *
     * @return An immutable list of listeners
     *         that have already been registered
     */
    List<Object> getRegisteredListeners();
}
