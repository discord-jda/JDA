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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The active security incident actions in a {@link Guild}.
 *
 * <p>Security incidents are used to temporarily disable features for the purpose of moderation.
 *
 * @see #enabled(OffsetDateTime, OffsetDateTime)
 * @see #disabled()
 */
public class SecurityIncidentActions {
    private static final SecurityIncidentActions disabled = new SecurityIncidentActions(0, 0);

    private final long invitesDisabledUntil;
    private final long directMessagesDisabledUntil;

    private SecurityIncidentActions(long invitesDisabledUntil, long directMessagesDisabledUntil) {
        this.invitesDisabledUntil = invitesDisabledUntil;
        this.directMessagesDisabledUntil = directMessagesDisabledUntil;
    }

    /**
     * The time until when invites are paused.
     *
     * @return The time until invites are paused, or null if unpaused
     */
    @Nullable
    public OffsetDateTime getInvitesDisabledUntil() {
        return invitesDisabledUntil == 0 ? null : Helpers.toOffset(invitesDisabledUntil);
    }

    /**
     * The time until when direct messages are paused.
     *
     * @return The time until direct messages are paused, or null if unpaused
     */
    @Nullable
    public OffsetDateTime getDirectMessagesDisabledUntil() {
        return directMessagesDisabledUntil == 0
                ? null
                : Helpers.toOffset(directMessagesDisabledUntil);
    }

    /**
     * Incidents state, which disables all active security incidents.
     * <br>The resulting object is used with {@link Guild#modifySecurityIncidents(SecurityIncidentActions)} to update the active incidents of the guild.
     *
     * @return The new security incidents
     */
    @Nonnull
    public static SecurityIncidentActions disabled() {
        return disabled;
    }

    /**
     * Incidents state, which enables security incidents based on the provided deadlines.
     * <br>The resulting object is used with {@link Guild#modifySecurityIncidents(SecurityIncidentActions)} to update the active incidents of the guild.
     *
     * @param  invitesDisabledUntil
     *         The time until invites are paused
     * @param  directMessagesDisabledUntil
     *         The time until direct messages are paused
     *
     * @return The new security incidents
     */
    @Nonnull
    public static SecurityIncidentActions enabled(
            @Nullable OffsetDateTime invitesDisabledUntil,
            @Nullable OffsetDateTime directMessagesDisabledUntil) {
        return new SecurityIncidentActions(
                invitesDisabledUntil == null
                        ? 0
                        : invitesDisabledUntil.toInstant().toEpochMilli(),
                directMessagesDisabledUntil == null
                        ? 0
                        : directMessagesDisabledUntil.toInstant().toEpochMilli());
    }

    @Override
    public int hashCode() {
        return Objects.hash(invitesDisabledUntil, directMessagesDisabledUntil);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SecurityIncidentActions)) {
            return false;
        }
        SecurityIncidentActions other = (SecurityIncidentActions) obj;
        return this.invitesDisabledUntil == other.invitesDisabledUntil
                && this.directMessagesDisabledUntil == other.directMessagesDisabledUntil;
    }

    @Override
    public String toString() {
        return new EntityString(this)
                .addMetadata("invitesDisabledUntil", getInvitesDisabledUntil())
                .addMetadata("directMessagesDisabledUntil", getDirectMessagesDisabledUntil())
                .toString();
    }
}
