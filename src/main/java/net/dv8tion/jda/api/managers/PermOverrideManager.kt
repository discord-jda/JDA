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
package net.dv8tion.jda.api.managers

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.channel.unions.IPermissionContainerUnion
import net.dv8tion.jda.internal.utils.Checks
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Manager providing functionality to update one or more fields for a [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride].
 *
 *
 * **Example**
 * <pre>`manager.setDenied(Permission.MESSAGE_SEND)
 * .setAllowed(Permission.VIEW_CHANNEL)
 * .queue();
 * manager.reset(PermOverrideManager.DENIED | PermOverrideManager.ALLOWED)
 * .grant(Permission.MESSAGE_SEND)
 * .clear(Permission.MESSAGE_MANAGE)
 * .queue();
`</pre> *
 *
 * @see net.dv8tion.jda.api.entities.PermissionOverride.getManager
 */
interface PermOverrideManager : Manager<PermOverrideManager?> {
    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br></br>Example: `manager.reset(PermOverrideManager.ALLOWED | PermOverrideManager.DENIED);`
     *
     *
     * **Flag Constants:**
     *
     *  * [.DENIED]
     *  * [.ALLOWED]
     *  * [.PERMISSIONS]
     *
     *
     * @param  fields
     * Integer value containing the flags to reset.
     *
     * @return PermOverrideManager for chaining convenience
     */
    @Nonnull
    override fun reset(fields: Long): PermOverrideManager?

    /**
     * Resets the fields specified by the provided bit-flag patterns.
     * <br></br>Example: `manager.reset(PermOverrideManager.ALLOWED, PermOverrideManager.DENIED);`
     *
     *
     * **Flag Constants:**
     *
     *  * [.DENIED]
     *  * [.ALLOWED]
     *  * [.PERMISSIONS]
     *
     *
     * @param  fields
     * Integer values containing the flags to reset.
     *
     * @return PermOverrideManager for chaining convenience
     */
    @Nonnull
    override fun reset(vararg fields: Long): PermOverrideManager?

    @get:Nonnull
    val guild: Guild?
        /**
         * The [Guild][net.dv8tion.jda.api.entities.Guild] this Manager's
         * [GuildChannel] is in.
         * <br></br>This is logically the same as calling `getPermissionOverride().getGuild()`
         *
         * @return The parent [Guild][net.dv8tion.jda.api.entities.Guild]
         */
        get() = permissionOverride.guild

    @get:Nonnull
    val channel: IPermissionContainerUnion?
        /**
         * The [GuildChannel][IPermissionContainer] this Manager's
         * [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride] is in.
         * <br></br>This is logically the same as calling `getPermissionOverride().getChannel()`
         *
         * @return The parent [GuildChannel]
         */
        get() = permissionOverride.channel

    @get:Nonnull
    val permissionOverride: PermissionOverride

    /**
     * Grants the provided [Permissions][net.dv8tion.jda.api.Permission] bits
     * to the selected [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride].
     *
     * @param  permissions
     * The permissions to grant to the selected [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride]
     *
     * @return PermOverrideManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun grant(permissions: Long): PermOverrideManager?

    /**
     * Grants the provided [Permissions][net.dv8tion.jda.api.Permission]
     * to the selected [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride].
     *
     * @param  permissions
     * The permissions to grant to the selected [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride]
     *
     * @throws IllegalArgumentException
     * If any of the provided Permissions is `null`
     *
     * @return PermOverrideManager for chaining convenience
     *
     * @see net.dv8tion.jda.api.Permission.getRaw
     */
    @Nonnull
    @CheckReturnValue
    fun grant(@Nonnull vararg permissions: Permission?): PermOverrideManager? {
        Checks.notNull(permissions, "Permissions")
        return grant(Permission.getRaw(*permissions))
    }

    /**
     * Grants the provided [Permissions][net.dv8tion.jda.api.Permission]
     * to the selected [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride].
     *
     * @param  permissions
     * The permissions to grant to the selected [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride]
     *
     * @throws IllegalArgumentException
     * If any of the provided Permissions is `null`
     *
     * @return PermOverrideManager for chaining convenience
     *
     * @see java.util.EnumSet EnumSet
     *
     * @see net.dv8tion.jda.api.Permission.getRaw
     */
    @Nonnull
    @CheckReturnValue
    fun grant(@Nonnull permissions: Collection<Permission?>?): PermOverrideManager? {
        return grant(Permission.getRaw(permissions!!))
    }

    /**
     * Denies the provided [Permissions][net.dv8tion.jda.api.Permission] bits
     * from the selected [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride].
     *
     * @param  permissions
     * The permissions to deny from the selected [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride]
     *
     * @return PermOverrideManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun deny(permissions: Long): PermOverrideManager?

    /**
     * Denies the provided [Permissions][net.dv8tion.jda.api.Permission]
     * from the selected [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride].
     *
     * @param  permissions
     * The permissions to deny from the selected [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride]
     *
     * @throws IllegalArgumentException
     * If any of the provided Permissions is `null`
     *
     * @return PermOverrideManager for chaining convenience
     *
     * @see net.dv8tion.jda.api.Permission.getRaw
     */
    @Nonnull
    @CheckReturnValue
    fun deny(@Nonnull vararg permissions: Permission?): PermOverrideManager? {
        Checks.notNull(permissions, "Permissions")
        return deny(Permission.getRaw(*permissions))
    }

    /**
     * Denies the provided [Permissions][net.dv8tion.jda.api.Permission]
     * from the selected [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride].
     *
     * @param  permissions
     * The permissions to deny from the selected [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride]
     *
     * @throws IllegalArgumentException
     * If any of the provided Permissions is `null`
     *
     * @return PermOverrideManager for chaining convenience
     *
     * @see java.util.EnumSet EnumSet
     *
     * @see net.dv8tion.jda.api.Permission.getRaw
     */
    @Nonnull
    @CheckReturnValue
    fun deny(@Nonnull permissions: Collection<Permission?>?): PermOverrideManager? {
        return deny(Permission.getRaw(permissions!!))
    }

    /**
     * Clears the provided [Permissions][net.dv8tion.jda.api.Permission] bits
     * from the selected [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride].
     * <br></br>This will cause the provided Permissions to be inherited
     *
     * @param  permissions
     * The permissions to clear from the selected [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride]
     *
     * @return PermOverrideManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun clear(permissions: Long): PermOverrideManager?

    /**
     * Clears the provided [Permissions][net.dv8tion.jda.api.Permission] bits
     * from the selected [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride].
     * <br></br>This will cause the provided Permissions to be inherited
     *
     * @param  permissions
     * The permissions to clear from the selected [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride]
     *
     * @throws IllegalArgumentException
     * If any of the provided Permissions is `null`
     *
     * @return PermOverrideManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun clear(@Nonnull vararg permissions: Permission?): PermOverrideManager? {
        Checks.notNull(permissions, "Permissions")
        return clear(Permission.getRaw(*permissions))
    }

    /**
     * Clears the provided [Permissions][net.dv8tion.jda.api.Permission] bits
     * from the selected [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride].
     * <br></br>This will cause the provided Permissions to be inherited
     *
     * @param  permissions
     * The permissions to clear from the selected [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride]
     *
     * @throws IllegalArgumentException
     * If any of the provided Permissions is `null`
     *
     * @return PermOverrideManager for chaining convenience
     *
     * @see java.util.EnumSet EnumSet
     *
     * @see net.dv8tion.jda.api.Permission.getRaw
     */
    @Nonnull
    @CheckReturnValue
    fun clear(@Nonnull permissions: Collection<Permission?>?): PermOverrideManager? {
        return clear(Permission.getRaw(permissions!!))
    }

    companion object {
        /** Used to reset the denied field  */
        const val DENIED: Long = 1

        /** Used to reset the granted field  */
        const val ALLOWED = (1 shl 1).toLong()

        /** Used to reset **all** permissions to their original value  */
        const val PERMISSIONS = ALLOWED or DENIED
    }
}
