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
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.handle.SocketHandler;
import net.dv8tion.jda.internal.utils.EntityString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Top-level event type
 * <br>All events JDA fires are derived from this class.
 *
 * <p>Can be used to check if an Object is a JDA event in {@link net.dv8tion.jda.api.hooks.EventListener EventListener} implementations to distinguish what event is being fired.
 * <br>Adapter implementation: {@link net.dv8tion.jda.api.hooks.ListenerAdapter ListenerAdapter}
 */
public abstract class Event implements GenericEvent
{
    protected final JDA api;
    protected final long responseNumber;
    protected final DataObject rawData;

    /**
     * Creates a new Event from the given JDA instance
     *
     * @param api
     *        Current JDA instance
     * @param responseNumber
     *        The sequence number for this event
     *
     * @see   #Event(net.dv8tion.jda.api.JDA)
     */
    public Event(@Nonnull JDA api, long responseNumber)
    {
        this.api = api;
        this.responseNumber = responseNumber;
        this.rawData = api instanceof JDAImpl && ((JDAImpl) api).isEventPassthrough() ? SocketHandler.CURRENT_EVENT.get() : null;
    }

    /**
     * Creates a new Event from the given JDA instance
     * <br>Uses the current {@link net.dv8tion.jda.api.JDA#getResponseTotal()} as sequence
     *
     * @param api
     *        Current JDA instance
     */
    public Event(@Nonnull JDA api)
    {
        this(api, api.getResponseTotal());
    }

    @Nonnull
    @Override
    public JDA getJDA()
    {
        return api;
    }

    @Override
    public long getResponseNumber()
    {
        return responseNumber;
    }

    @Nullable
    @Override
    public DataObject getRawData()
    {
        if (api instanceof JDAImpl) {
            if (!((JDAImpl) api).isEventPassthrough())
                throw new IllegalStateException("Event passthrough is not enabled, see JDABuilder#setEventPassthrough(boolean)");
        }

        return rawData;
    }

    @Override
    public String toString()
    {
        if (this instanceof UpdateEvent<?, ?>)
        {
            final UpdateEvent<?, ?> event = (UpdateEvent<?, ?>) this;
            return new EntityString(this)
                    .setType(event.getPropertyIdentifier())
                    .addMetadata(null, event.getOldValue() + " -> " + event.getNewValue())
                    .toString();
        }
        else
        {
            return new EntityString(this).toString();
        }
    }
}
