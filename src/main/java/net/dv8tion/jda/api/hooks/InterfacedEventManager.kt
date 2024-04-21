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
import net.dv8tion.jda.internal.JDAImpl
import net.dv8tion.jda.internal.utils.JDALogger
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import javax.annotation.Nonnull

/**
 * An [IEventManager][net.dv8tion.jda.api.hooks.IEventManager] implementation
 * that uses the [EventListener][net.dv8tion.jda.api.hooks.EventListener] interface for
 * event listeners.
 *
 *
 * This only accepts listeners that implement [EventListener][net.dv8tion.jda.api.hooks.EventListener]
 * <br></br>An adapter implementation is [ListenerAdapter][net.dv8tion.jda.api.hooks.ListenerAdapter] which
 * provides methods for each individual [net.dv8tion.jda.api.events.Event].
 *
 *
 * **This is the default IEventManager used by JDA**
 *
 * @see net.dv8tion.jda.api.hooks.AnnotatedEventManager
 *
 * @see net.dv8tion.jda.api.hooks.IEventManager
 */
class InterfacedEventManager : IEventManager {
    private val listeners = CopyOnWriteArrayList<EventListener?>()

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException
     * If the provided listener does not implement [EventListener][net.dv8tion.jda.api.hooks.EventListener]
     */
    override fun register(@Nonnull listener: Any) {
        require(listener is EventListener) { "Listener must implement EventListener" }
        listeners.add(listener)
    }

    override fun unregister(@Nonnull listener: Any?) {
        if (listener !is EventListener) {
            JDALogger.getLog(javaClass).warn(
                "Trying to remove a listener that does not implement EventListener: {}",
                if (listener == null) "null" else listener.javaClass.getName()
            )
        }
        listeners.remove(listener)
    }

    @get:Nonnull
    override val registeredListeners: List<Any?>
        get() = Collections.unmodifiableList<Any?>(ArrayList(listeners))

    override fun handle(@Nonnull event: GenericEvent) {
        for (listener in listeners) {
            try {
                listener!!.onEvent(event)
            } catch (throwable: Throwable) {
                JDAImpl.LOG.error("One of the EventListeners had an uncaught exception", throwable)
                if (throwable is Error) throw throwable
            }
        }
    }
}
