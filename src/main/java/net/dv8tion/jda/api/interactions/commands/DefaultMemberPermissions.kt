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
package net.dv8tion.jda.api.interactions.commands

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.internal.utils.Checks
import java.util.*
import javax.annotation.Nonnull

/**
 * Represents the default permissions for a Discord Application-Command. These permissions define the type of users that can use this command if no explicit command-specific
 * privileges are set by moderators to control who can and can't use the command within a Guild.
 *
 * For example, given a command defined with [CommandData#setDefaultPermissions][net.dv8tion.jda.api.interactions.commands.build.CommandData.setDefaultPermissions] as `command.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS))`
 * any user with the [Permission.BAN_MEMBERS] permission would be able to use the command by default.
 */
class DefaultMemberPermissions private constructor(
    /**
     * Raw permission integer representing the default permissions of a command.
     * <br></br>This returns null if it is of type [ENABLED][DefaultMemberPermissions.ENABLED]
     * <br></br>If the default member permissions are [DISABLED][DefaultMemberPermissions.DISABLED], this returns 0
     *
     * @return Raw permission integer representing the default member permissions of a command
     */
    val permissionsRaw: Long?
) {

    companion object {
        /**
         * Default permissions of a command with no restrictions applied. (Everyone can see and access this command by default)
         */
        @JvmField
        val ENABLED = DefaultMemberPermissions(null)

        /**
         * "Empty" permissions of a command.
         * <br></br>Only members with the [ADMINISTRATOR][Permission.ADMINISTRATOR] permission can see and access this command by default.
         */
        @JvmField
        val DISABLED = DefaultMemberPermissions(0L)

        /**
         * Returns a DefaultMemberPermissions instance with the predefined permissions a member must have to see and access a command.
         *
         * <br></br>**If the passed Collection is empty, this returns [ENABLED][DefaultMemberPermissions.ENABLED]**
         *
         * @param  permissions
         * Collection of [Permissions][Permission]
         *
         * @throws IllegalArgumentException
         * If any of the passed Permission is null
         *
         * @return DefaultMemberPermissions instance with the predefined permissions
         */
        @Nonnull
        fun enabledFor(@Nonnull permissions: Collection<Permission?>): DefaultMemberPermissions {
            Checks.noneNull(permissions, "Permissions")
            return if (permissions.isEmpty()) ENABLED else enabledFor(
                Permission.getRaw(permissions)
            )
        }

        /**
         * Returns a DefaultMemberPermissions instance with the predefined permissions a member must have to see and access a command.
         *
         * <br></br>**If the passed Array is empty, this returns [ENABLED][DefaultMemberPermissions.ENABLED]**
         *
         * @param  permissions
         * Vararg of [Permissions][Permission]
         *
         * @throws IllegalArgumentException
         * If any of the passed Permission is null
         *
         * @return DefaultMemberPermissions instance with the predefined permissions
         */
        @JvmStatic
        @Nonnull
        fun enabledFor(@Nonnull vararg permissions: Permission?): DefaultMemberPermissions {
            return enabledFor(Arrays.asList(*permissions))
        }

        /**
         * Returns a DefaultMemberPermissions instance with the predefined permissions a member must have to see and access a command.
         *
         * <br></br>**If the passed permission offset is 0, this returns [ENABLED][DefaultMemberPermissions.ENABLED]**
         *
         * @param  permissions
         * Raw permission bitset
         *
         * @return DefaultMemberPermissions instance with the predefined permissions
         */
        @JvmStatic
        @Nonnull
        fun enabledFor(permissions: Long): DefaultMemberPermissions {
            return DefaultMemberPermissions(permissions)
        }
    }
}
