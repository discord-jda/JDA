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
import net.dv8tion.jda.api.entities.guild.SecurityIncidentDetections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that the {@link SecurityIncidentDetections} of a {@link Guild Guild} changed.
 *
 * <p>Can be used to detect when Discord detects a raid or spammers in direct messages.
 *
 * <p>Identifier: {@code security_incident_detections}
 */
public class GuildUpdateSecurityIncidentDetectionsEvent extends GenericGuildUpdateEvent<SecurityIncidentDetections>
{
    public static final String IDENTIFIER = "security_incident_detections";

    public GuildUpdateSecurityIncidentDetectionsEvent(@Nonnull JDA api, long responseNumber, @Nonnull Guild guild, @Nullable SecurityIncidentDetections previous)
    {
        super(api, responseNumber, guild, previous, guild.getSecurityIncidentDetections(), IDENTIFIER);
    }

    /**
     * The old security incident actions, or null if disabled.
     *
     * @return The old incident actions
     */
    @Nullable
    public SecurityIncidentDetections getOldSecurityIncidentDetections()
    {
        return getOldValue();
    }

    /**
     * The new security incident actions, or null if disabled.
     *
     * @return The new incident actions
     */
    @Nullable
    public SecurityIncidentDetections getNewSecurityIncidentDetections()
    {
        return getNewValue();
    }
}
