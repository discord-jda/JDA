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
package net.dv8tion.jda.api.events.guild.override

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.PermissionOverride
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer
import java.util.*
import javax.annotation.Nonnull

/**
 * Indicates that a [PermissionOverride] in a [guild channel][IPermissionContainer] has been updated.
 *
 *
 * Can be used to retrieve the updated override and old [allow][.getOldAllow] and [deny][.getOldDeny].
 */
class PermissionOverrideUpdateEvent(
    @Nonnull api: JDA,
    responseNumber: Long,
    @Nonnull channel: IPermissionContainer,
    @Nonnull override: PermissionOverride,
    /**
     * The old allowed permissions as a raw bitmask.
     *
     * @return The old allowed permissions
     */
    val oldAllowRaw: Long,
    /**
     * The old denied permissions as a raw bitmask.
     *
     * @return The old denied permissions
     */
    val oldDenyRaw: Long
) : GenericPermissionOverrideEvent(api, responseNumber, channel, override) {

    val oldInheritedRaw: Long
        /**
         * The old inherited permissions as a raw bitmask.
         *
         * @return The old inherited permissions
         */
        get() = (oldAllowRaw or oldDenyRaw).inv()

    /**
     * The old allowed permissions
     *
     * @return The old allowed permissions
     */
    @Nonnull
    fun getOldAllow(): EnumSet<Permission> {
        return Permission.getPermissions(oldAllowRaw)
    }

    /**
     * The old denied permissions
     *
     * @return The old denied permissions
     */
    @Nonnull
    fun getOldDeny(): EnumSet<Permission> {
        return Permission.getPermissions(oldDenyRaw)
    }

    @get:Nonnull
    val oldInherited: EnumSet<Permission>
        /**
         * The old inherited permissions
         *
         * @return The old inherited permissions
         */
        get() = Permission.getPermissions(oldInheritedRaw)
}
