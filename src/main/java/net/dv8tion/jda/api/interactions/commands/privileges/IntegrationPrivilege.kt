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
package net.dv8tion.jda.api.interactions.commands.privileges

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.ISnowflake
import net.dv8tion.jda.api.interactions.commands.privileges.IntegrationPrivilege.Type
import net.dv8tion.jda.internal.utils.EntityString
import java.util.*
import javax.annotation.Nonnull

/**
 * Privilege used to restrict access to a command within a [Guild][net.dv8tion.jda.api.entities.Guild].
 *
 *
 * Moderators of a Guild can create these privileges inside the Integrations Menu
 *
 * @see Guild.retrieveCommandPrivileges
 * @see net.dv8tion.jda.api.events.interaction.command.ApplicationCommandUpdatePrivilegesEvent
 *
 * @see net.dv8tion.jda.api.events.interaction.command.ApplicationUpdatePrivilegesEvent
 */
class IntegrationPrivilege(
    /**
     * The [Guild] this IntegrationPrivilege was created in.
     *
     * @return the guild in which this IntegrationPrivilege was created in.
     */
    @get:Nonnull
    @param:Nonnull val guild: Guild,
    /**
     * The [Type] of entity this privilege is applied to.
     *
     * @return The target [Type]
     */
    @get:Nonnull
    @param:Nonnull val type: Type,
    /**
     * True if this privilege is granting access to the command
     *
     * @return Whether this privilege grants access
     */
    val isEnabled: Boolean, override val idLong: Long
) : ISnowflake {

    /**
     * Whether this IntegrationPrivilege targets the @everyone Role
     *
     * @return True, if this IntegrationPrivilege targets the @everyone Role
     */
    fun targetsEveryone(): Boolean {
        return type == Type.ROLE && idLong == guild.idLong
    }

    /**
     * Whether this IntegrationPrivilege targets "All channels"
     *
     * @return True, if this IntegrationPrivilege targets all channels
     */
    fun targetsAllChannels(): Boolean {
        return type == Type.CHANNEL && idLong == guild.idLong - 1
    }

    val isDisabled: Boolean
        /**
         * True if this privilege is denying access to the command
         *
         * @return Whether this privilege denies access
         */
        get() = !isEnabled

    override fun hashCode(): Int {
        return Objects.hash(idLong, isEnabled)
    }

    override fun equals(obj: Any?): Boolean {
        if (obj === this) return true
        if (obj !is IntegrationPrivilege) return false
        val other = obj
        return other.idLong == idLong && other.isEnabled == isEnabled
    }

    override fun toString(): String {
        return EntityString(this)
            .setType(type)
            .addMetadata("enabled", isEnabled)
            .toString()
    }

    /**
     * The target type this privilege applies to.
     */
    enum class Type(private val key: Int) {
        UNKNOWN(-1),
        ROLE(1),
        USER(2),
        CHANNEL(3);

        companion object {
            /**
             * Returns the appropriate enum constant for the given key.
             *
             * @param  key
             * The API key for the type
             *
             * @return The Type constant, or [.UNKNOWN] if there is no known representation
             */
            @JvmStatic
            @Nonnull
            fun fromKey(key: Int): Type {
                for (type in entries) {
                    if (type.key == key) return type
                }
                return UNKNOWN
            }
        }
    }
}
