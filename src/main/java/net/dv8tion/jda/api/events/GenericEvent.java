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

package net.dv8tion.jda.api.events;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.utils.data.DataObject;

import javax.annotation.Nonnull;

public interface GenericEvent
{
    /**
     * The current JDA instance corresponding to this Event
     *
     * @return The corresponding JDA instance
     */
    @Nonnull
    JDA getJDA();

    /**
     * The current sequence for this event.
     * <br>This can be used to keep events in order when making sequencing system.
     *
     * @return The current sequence number for this event
     */
    long getResponseNumber();

    /**
     * The passthrough data that this event was serialized from.
     * <br>This provides the full gateway message payload, including sequence and dispatch type.
     * For details, read the official <a href="https://discord.dev/topics/gateway" target="_blank">Discord Documentation</a>.
     *
     * @throws IllegalStateException 
     *         If the event has no raw data, see {@link #hasRawData()}
     *
     * @return The corresponding {@link DataObject}
     *
     * @see    #hasRawData()
     */
    @Nonnull
    DataObject getRawData();

    /**
     * Whether this event contains its raw event data
     *
     * @return True if the event has raw data
     *
     * @see #getRawData()
     */
    boolean hasRawData();
}
