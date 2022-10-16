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

package net.dv8tion.jda.api.interactions.commands;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Represents a slash command mention, such as {@code </ban soft:1021082477038678126>}
 */
public class SlashCommandReference implements ICommandReference
{
    private final long id;
    private final String name;
    private final String subcommand;
    private final String subcommandGroup;

    public SlashCommandReference(String name, String subcommandGroup, String subcommand, long id)
    {
        this.name = name;
        this.subcommandGroup = subcommandGroup;
        this.subcommand = subcommand;
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

    /**
     * Returns the subcommand of the slash command
     *
     * @return the subcommand of the slash command
     */
    @Nullable
    public String getSubcommand()
    {
        return subcommand;
    }

    /**
     * Returns the subcommand group of the slash command
     *
     * @return the subcommand group of the slash command
     */
    @Nullable
    public String getSubcommandGroup()
    {
        return subcommandGroup;
    }

    @Nonnull
    @Override
    public String getCommandPath()
    {
        final StringJoiner joiner = new StringJoiner(" ");
        joiner.add(name);
        if (subcommandGroup != null)
            joiner.add(subcommandGroup);
        if (subcommand != null)
            joiner.add(subcommand);

        return joiner.toString();
    }

    @Override
    public String toString()
    {
        return "SlashCommandReference: " + getCommandPath() + " (" + getId() + ")";
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof SlashCommandReference)) return false;

        SlashCommandReference that = (SlashCommandReference) o;

        if (id != that.id) return false;
        if (!name.equals(that.name)) return false;
        if (!Objects.equals(subcommand, that.subcommand)) return false;
        return Objects.equals(subcommandGroup, that.subcommandGroup);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, name, subcommand, subcommandGroup);
    }
}
