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

package net.dv8tion.jda.internal.interactions.command;

import net.dv8tion.jda.api.interactions.commands.ICommandReference;

import javax.annotation.Nonnull;

public class SlashCommandReference implements ICommandReference
{
    private final long id;
    private final String name;

    public SlashCommandReference(String name, long id)
    {
        this.name = name;
        this.id = id;
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @Nonnull
    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return "SlashCommandReference: " + getName() + " (" + getId() + ")";
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SlashCommandReference that = (SlashCommandReference) o;

        if (id != that.id) return false;
        return name.equals(that.name);
    }

    @Override
    public int hashCode()
    {
        int result = Long.hashCode(id);
        result = 31 * result + name.hashCode();
        return result;
    }
}
