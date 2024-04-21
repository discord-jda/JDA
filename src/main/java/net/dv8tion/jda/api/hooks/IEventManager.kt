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
package net.dv8tion.jda.api.hooks

import net.dv8tion.jda.api.events.GenericEvent
import javax.annotation.Nonnull

/**
 * An interface for JDA's EventManager system.
 * <br></br>This should be registered in the [JDABuilder][net.dv8tion.jda.api.JDABuilder]
 *
 *
 * JDA provides 2 implementations:
 *
 *  * [InterfacedEventManager][net.dv8tion.jda.api.hooks.InterfacedEventManager]
 * <br></br>Simple implementation that allows [EventListener][net.dv8tion.jda.api.hooks.EventListener]
 * instances as listeners.
 *
 *  * [AnnotatedEventManager][net.dv8tion.jda.api.hooks.AnnotatedEventManager]
 * <br></br>An implementation that accepts any object and uses the [SubscribeEvent][net.dv8tion.jda.api.hooks.SubscribeEvent]
 * annotation to handle events.
 *
 *
 *
 * The default event manager is [InterfacedEventManager][net.dv8tion.jda.api.hooks.InterfacedEventManager]
 * <br></br>Use [JDABuilder.setEventManager(IEventManager)][net.dv8tion.jda.api.JDABuilder.setEventManager]
 * to set the preferred event manager implementation.
 * <br></br>You can only use one implementation per JDA instance!
 *
 * @see net.dv8tion.jda.api.hooks.InterfacedEventManager
 *
 * @see net.dv8tion.jda.api.hooks.AnnotatedEventManager
 */
interface IEventManager {
    /**
     * Registers the specified listener
     * <br></br>Accepted types may be specified by implementations
     *
     * @param listener
     * A listener object
     *
     * @throws java.lang.UnsupportedOperationException
     * If the implementation does not support this method
     */
    fun register(@Nonnull listener: Any)

    /**
     * Removes the specified listener
     *
     * @param listener
     * The listener object to remove
     *
     * @throws java.lang.UnsupportedOperationException
     * If the implementation does not support this method
     */
    fun unregister(@Nonnull listener: Any?)

    /**
     * Handles the provided [GenericEvent][net.dv8tion.jda.api.events.GenericEvent].
     * <br></br>How this is handled is specified by the implementation.
     *
     *
     * An implementation should not throw exceptions.
     *
     * @param event
     * The event to handle
     */
    fun handle(@Nonnull event: GenericEvent)

    @JvmField
    @get:Nonnull
    val registeredListeners: List<Any?>
}
