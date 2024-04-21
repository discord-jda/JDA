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
package net.dv8tion.jda.api.exceptions

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.internal.utils.Checks
import javax.annotation.Nonnull

/**
 * Indicates that the currently logged in account does not meet the specified [Permission][net.dv8tion.jda.api.Permission]
 * from [.getPermission]
 */
open class PermissionException protected constructor(
    @Nonnull permission: Permission,
    reason: String? = "Cannot perform action due to a lack of Permission. Missing permission: $permission"
) : RuntimeException(reason) {
    /**
     * The [Permission][net.dv8tion.jda.api.Permission] that is required for the operation
     *
     *
     * **If this is a [HierarchyException][net.dv8tion.jda.api.exceptions.HierarchyException]
     * this will always be [Permission.UNKNOWN][net.dv8tion.jda.api.Permission.UNKNOWN]!**
     *
     * @return The required [Permission][net.dv8tion.jda.api.Permission]
     */
    @get:Nonnull
    val permission: Permission

    /**
     * Creates a new PermissionException instance
     *
     * @param reason
     * The reason for this Exception
     */
    constructor(reason: String?) : this(Permission.UNKNOWN, reason)
    /**
     * Creates a new PermissionException
     *
     * @param permission
     * The required [Permission][net.dv8tion.jda.api.Permission]
     * @param reason
     * The reason for this Exception
     */
    /**
     * Creates a new PermissionException instance
     *
     * @param permission
     * The required [Permission][net.dv8tion.jda.api.Permission]
     */
    init {
        Checks.notNull(permission, "permission")
        this.permission = permission
    }
}
