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

/**
 * JDA pushes {@link net.dv8tion.jda.core.events.Event Events} to the registered EventListeners.
 *
 * <p>Register an EventListener with either a {@link net.dv8tion.jda.core.JDA JDA} object
 * <br>or the {@link net.dv8tion.jda.core.JDABuilder JDABuilder}.
 *
 * <p><b>Examples: </b>
 * <br>
 * <code>
 *     JDA jda = new {@link net.dv8tion.jda.core.JDABuilder JDABuilder}(AccountType.BOT).{@link net.dv8tion.jda.core.JDABuilder#addEventListener(Object...) addEventListener(listeners)}.buildBlocking();<br>
 *     {@link net.dv8tion.jda.core.JDA#addEventListener(Object...) jda.addEventListener(listeners)};
 * </code>
 *
 * @see net.dv8tion.jda.core.hooks.ListenerAdapter
 * @see net.dv8tion.jda.core.hooks.InterfacedEventManager
 */
@FunctionalInterface
public interface EventListener
{

    /**
     * Handles any {@link net.dv8tion.jda.core.events.Event Event}.
     *
     * <p>To get specific events with Methods like {@code onMessageReceived(MessageReceivedEvent event)}
     * take a look at: {@link net.dv8tion.jda.core.hooks.ListenerAdapter ListenerAdapter}
     *
     * @param  event
     *         The Event to handle.
     */
    void onEvent(Event event);
}
