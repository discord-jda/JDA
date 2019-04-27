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
package net.dv8tion.jda.api.events.guild.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.UpdateEvent;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that a {@link net.dv8tion.jda.api.entities.Guild Guild} was updated.
 *
 * <p>Can be used to detect when a Guild is updated.
 */
public abstract class GenericGuildUpdateEvent<T> extends GenericGuildEvent implements UpdateEvent<Guild, T>
{
    protected final T previous;
    protected final T next;
    protected final String identifier;

    public GenericGuildUpdateEvent(
        @Nonnull JDA api, long responseNumber, @Nonnull Guild guild,
        @Nullable T previous, @Nullable T next, @Nonnull String identifier)
    {
        super(api, responseNumber, guild);
        this.previous = previous;
        this.next = next;
        this.identifier = identifier;
    }

    @Nonnull
    @Override
    public Guild getEntity()
    {
        return getGuild();
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
        return "GuildUpdate[" + getPropertyIdentifier() + "](" + getOldValue() + "->" + getNewValue() + ')';
    }
}
