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

package net.dv8tion.jda.api.events.channel.store.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.StoreChannel;
import net.dv8tion.jda.api.events.UpdateEvent;
import net.dv8tion.jda.api.events.channel.store.GenericStoreChannelEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that a {@link net.dv8tion.jda.api.entities.StoreChannel StoreChannel} was updated.
 * <br>Every StoreChannelUpdateEvent is derived from this event and can be casted.
 *
 * <p>Can be used to detect any StoreChannelUpdateEvent.
 */
public abstract class GenericStoreChannelUpdateEvent<T> extends GenericStoreChannelEvent implements UpdateEvent<StoreChannel, T>
{
    protected final T prev;
    protected final T next;
    protected final String identifier;

    public GenericStoreChannelUpdateEvent(@Nonnull JDA api, long responseNumber, @Nonnull StoreChannel channel,
                                          @Nullable T prev, @Nullable T next, @Nonnull String identifier)
    {
        super(api, responseNumber, channel);
        this.prev = prev;
        this.next = next;
        this.identifier = identifier;
    }

    @Nonnull
    @Override
    public String getPropertyIdentifier()
    {
        return identifier;
    }

    @Nonnull
    @Override
    public StoreChannel getEntity()
    {
        return channel;
    }

    @Nullable
    @Override
    public T getOldValue()
    {
        return prev;
    }

    @Nullable
    @Override
    public T getNewValue()
    {
        return next;
    }

    @Override
    public String toString()
    {
        return "StoreChannelUpdate[" + getPropertyIdentifier() + "](" +getOldValue() + "->" + getNewValue() + ')';
    }
}
