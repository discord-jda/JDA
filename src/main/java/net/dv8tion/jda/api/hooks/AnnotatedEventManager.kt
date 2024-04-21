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
import net.dv8tion.jda.internal.utils.ClassWalker
import net.dv8tion.jda.internal.utils.JDALogger
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.function.Consumer
import java.util.function.Function
import javax.annotation.Nonnull

/**
 * Implementation for [IEventManager][net.dv8tion.jda.api.hooks.IEventManager]
 * which checks for [SubscribeEvent][net.dv8tion.jda.api.hooks.SubscribeEvent] annotations on both
 * **static** and **member** methods.
 *
 *
 * Listeners for this manager do <u>not</u> need to implement [EventListener][net.dv8tion.jda.api.hooks.EventListener]
 * <br></br>Example
 * <pre>`
 * public class Foo
 * {
 * @SubscribeEvent
 * public void onMsg(MessageReceivedEvent event)
 * {
 * System.out.printf("%s: %s\n", event.getAuthor().getName(), event.getMessage().getContentDisplay());
 * }
 * }
`</pre> *
 *
 * @see net.dv8tion.jda.api.hooks.InterfacedEventManager
 *
 * @see net.dv8tion.jda.api.hooks.IEventManager
 *
 * @see net.dv8tion.jda.api.hooks.SubscribeEvent
 */
class AnnotatedEventManager : IEventManager {
    private val listeners: MutableSet<Any?> = ConcurrentHashMap.newKeySet()
    private val methods: MutableMap<Class<*>, MutableMap<Any?, MutableList<Method>>> = ConcurrentHashMap()
    override fun register(@Nonnull listener: Any) {
        if (listener.javaClass.isArray) {
            for (o in listener as Array<Any>) register(o)
            return
        }
        if (listeners.add(listener)) {
            registerListenerMethods(listener)
        }
    }

    override fun unregister(@Nonnull listener: Any?) {
        if (listener!!.javaClass.isArray) {
            for (o in (listener as Array<Any?>?)!!) unregister(o)
            return
        }
        if (listeners.remove(listener)) {
            updateMethods()
        }
    }

    @get:Nonnull
    override val registeredListeners: List<Any?>
        get() = Collections.unmodifiableList(ArrayList(listeners))

    override fun handle(@Nonnull event: GenericEvent) {
        for (eventClass in ClassWalker.walk(event.javaClass)) {
            val listeners: Map<Any?, MutableList<Method>>? = methods[eventClass]
            listeners?.forEach { (key: Any?, value: List<Method>) ->
                value.forEach(
                    Consumer { method: Method ->
                        try {
                            method.setAccessible(true)
                            method.invoke(key, event)
                        } catch (e1: IllegalAccessException) {
                            JDAImpl.LOG.error("Couldn't access annotated EventListener method", e1)
                        } catch (e1: InvocationTargetException) {
                            JDAImpl.LOG.error("Couldn't access annotated EventListener method", e1)
                        } catch (throwable: Throwable) {
                            JDAImpl.LOG.error("One of the EventListeners had an uncaught exception", throwable)
                            if (throwable is Error) throw throwable
                        }
                    })
            }
        }
    }

    private fun updateMethods() {
        methods.clear()
        for (listener in listeners) {
            registerListenerMethods(listener)
        }
    }

    private fun registerListenerMethods(listener: Any?) {
        val isClass = listener is Class<*>
        val c = if (isClass) listener as Class<*>? else listener!!.javaClass
        val allMethods = c!!.getDeclaredMethods()
        for (m in allMethods) {
            if (!m.isAnnotationPresent(SubscribeEvent::class.java)) continue
            //Skip member methods if listener is a Class
            if (isClass && !Modifier.isStatic(m.modifiers)) continue
            val parameterTypes = m.parameterTypes
            if (parameterTypes.size != 1 || !GenericEvent::class.java.isAssignableFrom(parameterTypes[0])) {
                LOGGER.warn(
                    "Method '{}' annotated with @{} must have at most 1 parameter, which implements GenericEvent",
                    m,
                    SubscribeEvent::class.java.getSimpleName()
                )
                continue
            }
            val eventClass = parameterTypes[0]
            methods.computeIfAbsent(
                eventClass,
                Function<Class<*>, MutableMap<Any?, MutableList<Method>>> { k: Class<*>? -> ConcurrentHashMap<Any?, List<Method>>() })
                .computeIfAbsent(listener) { k: Any? -> CopyOnWriteArrayList() }
                .add(m)
        }
    }

    companion object {
        private val LOGGER = JDALogger.getLog(AnnotatedEventManager::class.java)
    }
}
