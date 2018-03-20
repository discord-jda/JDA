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
package net.dv8tion.jda.core.events.guild.update;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.UpdateEvent;
import net.dv8tion.jda.core.events.guild.GenericGuildEvent;

/**
 * Indicates that a {@link net.dv8tion.jda.core.entities.Guild Guild} was updated.
 *
 * <p>Can be used to detect when a Guild is updated.
 */
public abstract class GenericGuildUpdateEvent<T> extends GenericGuildEvent implements UpdateEvent<Guild, T>
{
    protected final T previous;
    protected final T next;
    protected final String identifier;

    public GenericGuildUpdateEvent(
        JDA api, long responseNumber, Guild guild,
        T previous, T next, String identifier)
    {
        super(api, responseNumber, guild);
        this.previous = previous;
        this.next = next;
        this.identifier = identifier;
    }

    @Override
    public Guild getEntity()
    {
        return getGuild();
    }

    @Override
    public String getPropertyIdentifier()
    {
        return identifier;
    }

    @Override
    public T getOldValue()
    {
        return previous;
    }

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
