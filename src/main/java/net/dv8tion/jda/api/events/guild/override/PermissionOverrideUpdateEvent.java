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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer;
import net.dv8tion.jda.api.events.annotations.RequiredCacheFlags;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.annotation.Nonnull;
import java.util.EnumSet;

/**
 * Indicates that a {@link PermissionOverride} in a {@link IPermissionContainer guild channel} has been updated.
 *
 * <p>Can be used to retrieve the updated override and old {@link #getOldAllow() allow} and {@link #getOldDeny() deny}.
 *
 * <p><b>Note:</b> This event will <b>not</b> be fired when the {@link net.dv8tion.jda.api.entities.Guild#getPublicRole() @everyone} override:
 * <ul>
 *     <li>Is modified, but previously had no allowed/denied permissions, in which case {@link PermissionOverrideCreateEvent} is fired</li>
 *     <li>Had allowed/denied permissions, but has been cleared, in which case {@link PermissionOverrideDeleteEvent} is fired</li>
 * </ul>
 *
 * <p><b>Requirements</b><br>
 *
 * <p>These events require {@link CacheFlag#MEMBER_OVERRIDES} to be enabled for member overrides,
 * unless the member is the {@link net.dv8tion.jda.api.entities.Guild#getSelfMember() self member}.
 * <br>{@link net.dv8tion.jda.api.JDABuilder#createLight(String) createLight(String)} disables this by default!
 */
@RequiredCacheFlags(sometimes = CacheFlag.MEMBER_OVERRIDES)
public class PermissionOverrideUpdateEvent extends GenericPermissionOverrideEvent
{
    private final long oldAllow, oldDeny;

    public PermissionOverrideUpdateEvent(@Nonnull JDA api, long responseNumber, @Nonnull IPermissionContainer channel, @Nonnull PermissionOverride override, long oldAllow, long oldDeny)
    {
        super(api, responseNumber, channel, override);
        this.oldAllow = oldAllow;
        this.oldDeny = oldDeny;
    }

    /**
     * The old allowed permissions as a raw bitmask.
     *
     * @return The old allowed permissions
     */
    public long getOldAllowRaw()
    {
        return oldAllow;
    }

    /**
     * The old denied permissions as a raw bitmask.
     *
     * @return The old denied permissions
     */
    public long getOldDenyRaw()
    {
        return oldDeny;
    }

    /**
     * The old inherited permissions as a raw bitmask.
     *
     * @return The old inherited permissions
     */
    public long getOldInheritedRaw()
    {
        return ~(oldAllow | oldDeny);
    }

    /**
     * The old allowed permissions
     *
     * @return The old allowed permissions
     */
    @Nonnull
    public EnumSet<Permission> getOldAllow()
    {
        return Permission.getPermissions(oldAllow);
    }

    /**
     * The old denied permissions
     *
     * @return The old denied permissions
     */
    @Nonnull
    public EnumSet<Permission> getOldDeny()
    {
        return Permission.getPermissions(oldDeny);
    }

    /**
     * The old inherited permissions
     *
     * @return The old inherited permissions
     */
    @Nonnull
    public EnumSet<Permission> getOldInherited()
    {
        return Permission.getPermissions(getOldInheritedRaw());
    }
}
