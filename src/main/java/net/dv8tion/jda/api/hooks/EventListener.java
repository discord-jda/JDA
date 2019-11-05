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

/**
 * JDA pushes {@link net.dv8tion.jda.api.events.GenericEvent GenericEvents} to the registered EventListeners.
 *
 * <p>Register an EventListener with either a {@link net.dv8tion.jda.api.JDA JDA} object
 * <br>or the {@link net.dv8tion.jda.api.JDABuilder JDABuilder}.
 *
 * <p><b>Examples: </b>
 * <br>
 * <code>
 *     JDA jda = new {@link net.dv8tion.jda.api.JDABuilder JDABuilder}("token").{@link net.dv8tion.jda.api.JDABuilder#addEventListeners(Object...) addEventListeners(listeners)}.{@link net.dv8tion.jda.api.JDABuilder#build() build()};<br>
 *     {@link net.dv8tion.jda.api.JDA#addEventListener(Object...) jda.addEventListener(listeners)};
 * </code>
 *
 * @see net.dv8tion.jda.api.hooks.ListenerAdapter
 * @see net.dv8tion.jda.api.hooks.InterfacedEventManager
 */
@FunctionalInterface
public interface EventListener
{
    /**
     * Handles any {@link net.dv8tion.jda.api.events.GenericEvent GenericEvent}.
     *
     * <p>To get specific events with Methods like {@code onMessageReceived(MessageReceivedEvent event)}
     * take a look at: {@link net.dv8tion.jda.api.hooks.ListenerAdapter ListenerAdapter}
     *
     * @param  event
     *         The Event to handle.
     */
    void onEvent(@Nonnull GenericEvent event);
}
