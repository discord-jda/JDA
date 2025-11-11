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

package net.dv8tion.jda.api.entities.guild;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.internal.utils.EntityString;
import net.dv8tion.jda.internal.utils.Helpers;

import java.time.OffsetDateTime;
import java.util.Objects;

import javax.annotation.Nullable;

/**
 * Detected security incidents of a {@link Guild}.
 */
public class SecurityIncidentDetections {
    public static final SecurityIncidentDetections EMPTY = new SecurityIncidentDetections(0, 0);

    private final long dmSpamDetectedAt;
    private final long raidDetectedAt;

    public SecurityIncidentDetections(long dmSpamDetectedAt, long raidDetectedAt) {
        this.dmSpamDetectedAt = dmSpamDetectedAt;
        this.raidDetectedAt = raidDetectedAt;
    }

    /**
     * Timestamp when Discord detected spam in direct messages.
     *
     * @return {@link OffsetDateTime} of the detection, or null when there is no current detection
     */
    @Nullable
    public OffsetDateTime getTimeDetectedDmSpam() {
        return this.dmSpamDetectedAt == 0 ? null : Helpers.toOffset(dmSpamDetectedAt);
    }

    /**
     * Timestamp when Discord detected an ongoing raid.
     *
     * @return {@link OffsetDateTime} of the detection, or null when there is no current detection
     */
    @Nullable
    public OffsetDateTime getTimeDetectedRaid() {
        return this.raidDetectedAt == 0 ? null : Helpers.toOffset(raidDetectedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dmSpamDetectedAt, raidDetectedAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SecurityIncidentDetections)) {
            return false;
        }
        SecurityIncidentDetections that = (SecurityIncidentDetections) o;
        return this.dmSpamDetectedAt == that.dmSpamDetectedAt
                && this.raidDetectedAt == that.raidDetectedAt;
    }

    @Override
    public String toString() {
        return new EntityString(this)
                .addMetadata("dmSpamDetectedAt", getTimeDetectedDmSpam())
                .addMetadata("raidDetectedAt", getTimeDetectedRaid())
                .toString();
    }
}
