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
 * JDA pushes [GenericEvents][net.dv8tion.jda.api.events.GenericEvent] to the registered EventListeners.
 *
 *
 * Register an EventListener with either a [JDA][net.dv8tion.jda.api.JDA] object
 * <br></br>or the [JDABuilder][net.dv8tion.jda.api.JDABuilder].
 *
 *
 * **Examples: **
 * <br></br>
 * `
 * JDA jda = [JDABuilder][net.dv8tion.jda.api.JDABuilder].createDefault("token").[addEventListeners(listeners)][net.dv8tion.jda.api.JDABuilder.addEventListeners].[build()][net.dv8tion.jda.api.JDABuilder.build];<br></br>
 * [jda.addEventListener(listeners)][net.dv8tion.jda.api.JDA.addEventListener];
` *
 *
 * @see net.dv8tion.jda.api.hooks.ListenerAdapter
 *
 * @see net.dv8tion.jda.api.hooks.InterfacedEventManager
 */
fun interface EventListener {
    /**
     * Handles any [GenericEvent][net.dv8tion.jda.api.events.GenericEvent].
     *
     *
     * To get specific events with Methods like `onMessageReceived(MessageReceivedEvent event)`
     * take a look at: [ListenerAdapter][net.dv8tion.jda.api.hooks.ListenerAdapter]
     *
     * @param  event
     * The Event to handle.
     */
    fun onEvent(@Nonnull event: GenericEvent)
}
