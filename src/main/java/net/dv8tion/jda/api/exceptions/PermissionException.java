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
package net.dv8tion.jda.api.exceptions;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;

/**
 * Indicates that the currently logged in account does not meet the specified {@link net.dv8tion.jda.api.Permission Permission}
 * from {@link #getPermission()}
 */
public class PermissionException extends RuntimeException
{
    private final Permission permission;

    /**
     * Creates a new PermissionException instance
     *
     * @param reason
     *        The reason for this Exception
     */
    public PermissionException(String reason)
    {
        this(Permission.UNKNOWN, reason);
    }

    /**
     * Creates a new PermissionException instance
     *
     * @param permission
     *        The required {@link net.dv8tion.jda.api.Permission Permission}
     */
    protected PermissionException(@Nonnull Permission permission)
    {
        this(permission, "Cannot perform action due to a lack of Permission. Missing permission: " + permission.toString());
    }

    /**
     * Creates a new PermissionException
     *
     * @param permission
     *        The required {@link net.dv8tion.jda.api.Permission Permission}
     * @param reason
     *        The reason for this Exception
     */
    protected PermissionException(@Nonnull Permission permission, String reason)
    {
        super(reason);
        Checks.notNull(permission, "permission");
        this.permission = permission;
    }

    /**
     * The {@link net.dv8tion.jda.api.Permission Permission} that is required for the operation
     *
     * <p><b>If this is a {@link net.dv8tion.jda.api.exceptions.HierarchyException HierarchyException}
     * this will always be {@link net.dv8tion.jda.api.Permission#UNKNOWN Permission.UNKNOWN}!</b>
     *
     * @return The required {@link net.dv8tion.jda.api.Permission Permission}
     */
    @Nonnull
    public Permission getPermission()
    {
        return permission;
    }
}
