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

package net.dv8tion.jda.api.events.guild.override;

import net.dv8tion.jda.annotations.ForRemoval;
import net.dv8tion.jda.annotations.ReplaceWith;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer;
import net.dv8tion.jda.api.utils.PermissionSet;

import java.util.EnumSet;

import javax.annotation.Nonnull;

/**
 * Indicates that a {@link PermissionOverride} in a {@link IPermissionContainer guild channel} has been updated.
 *
 * <p>Can be used to retrieve the updated override and old {@link #getOldAllow() allow} and {@link #getOldDeny() deny}.
 */
public class PermissionOverrideUpdateEvent extends GenericPermissionOverrideEvent {
    private final PermissionSet oldAllow;
    private final PermissionSet oldDeny;

    public PermissionOverrideUpdateEvent(
            @Nonnull JDA api,
            long responseNumber,
            @Nonnull IPermissionContainer channel,
            @Nonnull PermissionOverride override,
            @Nonnull PermissionSet oldAllow,
            @Nonnull PermissionSet oldDeny) {
        super(api, responseNumber, channel, override);
        this.oldAllow = oldAllow;
        this.oldDeny = oldDeny;
    }

    @Nonnull
    public PermissionSet getOldAllowedSet() {
        return oldAllow;
    }

    @Nonnull
    public PermissionSet getOldDenySet() {
        return oldDeny;
    }

    @Nonnull
    public PermissionSet getOldInheritedSet() {
        return oldAllow.or(oldDeny).not();
    }

    /**
     * The old allowed permissions as a raw bitmask.
     *
     * @return The old allowed permissions
     */
    @Deprecated
    @ForRemoval
    @ReplaceWith("getOldAllowedSet()")
    public long getOldAllowRaw() {
        return oldAllow.toBigInteger().longValue();
    }

    /**
     * The old denied permissions as a raw bitmask.
     *
     * @return The old denied permissions
     */
    @Deprecated
    @ForRemoval
    @ReplaceWith("getOldDenySet()")
    public long getOldDenyRaw() {
        return oldDeny.toBigInteger().longValue();
    }

    /**
     * The old inherited permissions as a raw bitmask.
     *
     * @return The old inherited permissions
     */
    @Deprecated
    @ForRemoval
    @ReplaceWith("getOldInheritedSet()")
    public long getOldInheritedRaw() {
        return getOldInheritedSet().toBigInteger().longValue();
    }

    /**
     * The old allowed permissions
     *
     * @return The old allowed permissions
     */
    @Nonnull
    public EnumSet<Permission> getOldAllow() {
        return oldAllow.toEnumSet();
    }

    /**
     * The old denied permissions
     *
     * @return The old denied permissions
     */
    @Nonnull
    public EnumSet<Permission> getOldDeny() {
        return oldDeny.toEnumSet();
    }

    /**
     * The old inherited permissions
     *
     * @return The old inherited permissions
     */
    @Nonnull
    public EnumSet<Permission> getOldInherited() {
        return getOldInheritedSet().toEnumSet();
    }
}
