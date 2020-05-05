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

package net.dv8tion.jda.api.events.guild.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that the {@link net.dv8tion.jda.api.entities.Guild#getDescription() description} of a {@link net.dv8tion.jda.api.entities.Guild Guild} changed.
 *
 * <p>Can be used to detect when the description changes and retrieve the old one
 *
 * <p>Identifier: {@code description}
 */
public class GuildUpdateDescriptionEvent extends GenericGuildUpdateEvent<String>
{
    public static final String IDENTIFIER = "description";

    public GuildUpdateDescriptionEvent(@Nonnull JDA api, long responseNumber, @Nonnull Guild guild, @Nullable String previous)
    {
        super(api, responseNumber, guild, previous, guild.getDescription(), IDENTIFIER);
    }

    /**
     * The old description for this guild
     *
     * @return The old description for this guild, or null if none was set
     */
    @Nullable
    public String getOldDescription()
    {
        return getOldValue();
    }

    /**
     * The new description for this guild
     *
     * @return The new description, or null if it was removed
     */
    @Nullable
    public String getNewDescription()
    {
        return getNewValue();
    }
}
