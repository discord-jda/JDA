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

package net.dv8tion.jda.api.events.guild.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.SecurityIncidents;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that the {@link SecurityIncidents} of a {@link Guild Guild} changed.
 *
 * <p>Can be used to detect when a guild pauses or unpauses invites.
 *
 * <p>Identifier: {@code security_incidents}
 */
public class GuildUpdateSecurityIncidentsEvent extends GenericGuildUpdateEvent<SecurityIncidents>
{
    public static final String IDENTIFIER = "security_incidents";

    public GuildUpdateSecurityIncidentsEvent(@Nonnull JDA api, long responseNumber, @Nonnull Guild guild, @Nullable SecurityIncidents previous)
    {
        super(api, responseNumber, guild, previous, guild.getSecurityIncidents(), IDENTIFIER);
    }

    /**
     * The old security incidents, or null if disabled.
     *
     * @return The old incidents
     */
    @Nullable
    public SecurityIncidents getOldSecurityIncidents()
    {
        return getOldValue();
    }

    /**
     * The new security incidents, or null if disabled.
     *
     * @return The new incidents
     */
    @Nullable
    public SecurityIncidents getNewSecurityIncidents()
    {
        return getNewValue();
    }
}
