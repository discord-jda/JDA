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
package net.dv8tion.jda.core.events;

import net.dv8tion.jda.core.JDA;

/**
 * Top-level event type
 * <br>All events JDA fires are derived from this class.
 *
 * <p>Can be used to check if an Object is a JDA event in {@link net.dv8tion.jda.core.hooks.EventListener EventListener} implementations to distinguish what event is being fired.
 * <br>Adapter implementation: {@link net.dv8tion.jda.core.hooks.ListenerAdapter ListenerAdapter}
 */
public abstract class Event
{
    protected final JDA api;
    protected final long responseNumber;

    /**
     * Creates a new Event from the given JDA instance
     *
     * @param api
     *        Current JDA instance
     * @param responseNumber
     *        The sequence number for this event
     *
     * @see   #Event(net.dv8tion.jda.core.JDA)
     */
    public Event(JDA api, long responseNumber)
    {
        this.api = api;
        this.responseNumber = responseNumber;
    }

    /**
     * Creates a new Event from the given JDA instance
     * <br>Uses the current {@link net.dv8tion.jda.core.JDA#getResponseTotal()} as sequence
     *
     * @param api
     *        Current JDA instance
     */
    public Event(JDA api)
    {
        this(api, api.getResponseTotal());
    }

    /**
     * The current JDA instance corresponding to this Event
     *
     * @return The corresponding JDA instance
     */
    public JDA getJDA()
    {
        return api;
    }

    /**
     * The current sequence for this event.
     * <br>This can be used to keep events in order when making sequencing system.
     *
     * @return The current sequence number for this event
     */
    public long getResponseNumber()
    {
        return responseNumber;
    }
}
