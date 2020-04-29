/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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
package net.dv8tion.jda.api.events.channel.text.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.UpdateEvent;
import net.dv8tion.jda.api.events.channel.text.GenericTextChannelEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} was updated.
 * <br>Every TextChannelUpdateEvent is derived from this event and can be casted.
 *
 * <p>Can be used to detect any TextChannelUpdateEvent.
 */
public abstract class GenericTextChannelUpdateEvent<T> extends GenericTextChannelEvent implements UpdateEvent<TextChannel, T>
{
    protected final T previous;
    protected final T next;
    protected final String identifier;

    public GenericTextChannelUpdateEvent(
        @Nonnull JDA api, long responseNumber, @Nonnull TextChannel channel,
        @Nullable T previous, @Nullable T next, @Nonnull String identifier)
    {
        super(api, responseNumber, channel);
        this.previous = previous;
        this.next = next;
        this.identifier = identifier;
    }

    @Nonnull
    @Override
    public TextChannel getEntity()
    {
        return getChannel();
    }

    @Nonnull
    @Override
    public String getPropertyIdentifier()
    {
        return identifier;
    }

    @Nullable
    @Override
    public T getOldValue()
    {
        return previous;
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
        return "TextChannelUpdate[" + getPropertyIdentifier() + "](" +getOldValue() + "->" + getNewValue() + ')';
    }
}
