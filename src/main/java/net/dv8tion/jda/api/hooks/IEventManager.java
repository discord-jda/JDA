/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

import javax.annotation.Nonnull;
import java.util.List;

/**
 * An interface for JDA's EventManager system.
 * <br>This should be registered in the {@link net.dv8tion.jda.api.JDABuilder JDABuilder}
 *
 * <p>JDA provides 2 implementations:
 * <ul>
 *     <li>{@link net.dv8tion.jda.api.hooks.InterfacedEventManager InterfacedEventManager}
 *     <br>Simple implementation that allows {@link net.dv8tion.jda.api.hooks.EventListener EventListener}
 *         instances as listeners.</li>
 *
 *     <li>{@link net.dv8tion.jda.api.hooks.AnnotatedEventManager AnnotatedEventManager}
 *     <br>An implementation that accepts any object and uses the {@link net.dv8tion.jda.api.hooks.SubscribeEvent SubscribeEvent}
 *         annotation to handle events.</li>
 * </ul>
 *
 * <p>The default event manager is {@link net.dv8tion.jda.api.hooks.InterfacedEventManager InterfacedEventManager}
 * <br>Use {@link net.dv8tion.jda.api.JDABuilder#setEventManager(IEventManager) JDABuilder.setEventManager(IEventManager)}
 * to set the preferred event manager implementation.
 * <br>You can only use one implementation per JDA instance!
 *
 * @see net.dv8tion.jda.api.hooks.InterfacedEventManager
 * @see net.dv8tion.jda.api.hooks.AnnotatedEventManager
 */
public interface IEventManager
{
    /**
     * Registers the specified listener
     * <br>Accepted types may be specified by implementations
     *
     * @param listener
     *        A listener object
     *
     * @throws java.lang.UnsupportedOperationException
     *         If the implementation does not support this method
     */
    void register(@Nonnull Object listener);

    /**
     * Removes the specified listener
     *
     * @param listener
     *        The listener object to remove
     *
     * @throws java.lang.UnsupportedOperationException
     *         If the implementation does not support this method
     */
    void unregister(@Nonnull Object listener);

    /**
     * Handles the provided {@link net.dv8tion.jda.api.events.GenericEvent GenericEvent}.
     * <br>How this is handled is specified by the implementation.
     *
     * <p>An implementation should not throw exceptions.
     *
     * @param event
     *        The event to handle
     */
    void handle(@Nonnull GenericEvent event);

    /**
     * The currently registered listeners
     *
     * @throws java.lang.UnsupportedOperationException
     *         If the implementation does not support this method
     *
     * @return A list of listeners that have already been registered
     */
    @Nonnull
    List<Object> getRegisteredListeners();
}
