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
package net.dv8tion.jda.api.entities.templates

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import java.awt.Color
import java.time.OffsetDateTime
import java.util.*
import javax.annotation.Nonnull

/**
 * POJO for the roles information provided by a template.
 *
 * @see TemplateGuild.getRoles
 */
class TemplateRole(
    /**
     * The ids of roles are their position as stored by Discord so this will not look like a typical snowflake.
     *
     * @return The id of the role as stored by Discord
     */
    override val idLong: Long,
    /**
     * The Name of this [Role][TemplateRole].
     *
     * @return Never-null String containing the name of this [Role][TemplateRole].
     */
    @get:Nonnull val name: String,
    /**
     * The raw color RGB value used for this role
     * <br></br>Defaults to [net.dv8tion.jda.api.entities.Role.DEFAULT_COLOR_RAW] if this role has no set color
     *
     * @return The raw RGB color value or default
     */
    val colorRaw: Int,
    /**
     * Whether this [Role][TemplateRole] is hoisted
     * <br></br>Members in a hoisted role are displayed in their own grouping on the user-list
     *
     * @return True, if this [Role][TemplateRole] is hoisted.
     */
    val isHoisted: Boolean,
    /**
     * Whether or not this Role is mentionable
     *
     * @return True, if Role is mentionable.
     */
    val isMentionable: Boolean,
    /**
     * The `long` representation of the literal permissions that this [Role][TemplateRole] has.
     *
     * @return Never-negative long containing offset permissions of this role.
     */
    val permissionsRaw: Long
) : ISnowflake {

    override val timeCreated: OffsetDateTime?
        /**
         * As the ids of roles are their position, the date of creation cannot be calculated.
         *
         * @throws java.lang.UnsupportedOperationException
         * The date of creation cannot be calculated.
         */
        get() {
            throw UnsupportedOperationException("The date of creation cannot be calculated")
        }

    /**
     * The color this [Role][TemplateRole] is displayed in.
     *
     * @return Color value of Role-color
     *
     * @see .getColorRaw
     */
    fun getColor(): Color? {
        return if (colorRaw == Role.Companion.DEFAULT_COLOR_RAW) null else Color(
            colorRaw
        )
    }

    @get:Nonnull
    val permissions: EnumSet<Permission>
        /**
         * The Guild-Wide Permissions this PermissionHolder holds.
         * <br></br><u>Changes to the returned set do not affect this entity directly.</u>
         *
         * @return An EnumSet of Permissions granted to this PermissionHolder.
         */
        get() = Permission.getPermissions(permissionsRaw)
}
