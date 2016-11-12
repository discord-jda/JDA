/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.exceptions;

import net.dv8tion.jda.Permission;

public class PermissionException extends RuntimeException
{
    private final Permission permission;

    public PermissionException(Permission permission)
    {
        this(permission, "Cannot preform action due to a lack of Permission. Missing permission: " + permission.toString());
    }

    public PermissionException(String reason)
    {
        this(Permission.UNKNOWN, reason);
    }

    public PermissionException(Permission permission, String reason)
    {
        super(reason);
        this.permission = permission;
    }

    public Permission getPermission()
    {
        return permission;
    }
}
