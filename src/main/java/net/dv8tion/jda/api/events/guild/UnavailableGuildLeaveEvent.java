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

package net.dv8tion.jda.api.events.guild;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.Event;

import javax.annotation.Nonnull;

/**
 * Indicates that you left a {@link net.dv8tion.jda.api.entities.Guild Guild} that is not yet available.
 * <b>This does not extend {@link net.dv8tion.jda.api.events.guild.GenericGuildEvent GenericGuildEvent}</b>
 *
 * <p>Can be used to retrieve id of the unavailable Guild.
 */
public class UnavailableGuildLeaveEvent extends Event
{
    private final long guildId;

    public UnavailableGuildLeaveEvent(@Nonnull JDA api, long responseNumber, long guildId)
    {
        super(api, responseNumber);
        this.guildId = guildId;
    }

    /**
     * The id for the guild we left.
     *
     * @return The id for the guild
     */
    @Nonnull
    public String getGuildId()
    {
        return Long.toUnsignedString(guildId);
    }

    /**
     * The id for the guild we left.
     *
     * @return The id for the guild
     */
    public long getGuildIdLong()
    {
        return guildId;
    }
}
