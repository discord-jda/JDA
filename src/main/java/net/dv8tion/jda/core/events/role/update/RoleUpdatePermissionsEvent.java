/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.core.events.role.update;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Role;

import java.util.Collections;
import java.util.List;

/**
 * Indicates that a {@link net.dv8tion.jda.core.entities.Role Role} updated its permissions.
 *
 * <p>Can be used to retrieve the old permissions.
 *
 * <p>Identifier: {@code permission}
 */
public class RoleUpdatePermissionsEvent extends GenericRoleUpdateEvent<List<Permission>>
{
    public static final String IDENTIFIER = "permission";

    private final long oldPermissionsRaw;
    private final long newPermissionsRaw;

    public RoleUpdatePermissionsEvent(JDA api, long responseNumber, Role role, long oldPermissionsRaw)
    {
        super(api, responseNumber, role);
        this.oldPermissionsRaw = oldPermissionsRaw;
        this.newPermissionsRaw = role.getPermissionsRaw();
    }

    /**
     * The old permissions
     *
     * @return The old permissions
     */
    public List<Permission> getOldPermissions()
    {
        return Collections.unmodifiableList(
                Permission.getPermissions(oldPermissionsRaw));
    }

    /**
     * The old permissions
     *
     * @return The old permissions
     */
    public long getOldPermissionsRaw()
    {
        return oldPermissionsRaw;
    }

    public List<Permission> getNewPermissions()
    {
        return Collections.unmodifiableList(
                Permission.getPermissions(newPermissionsRaw));
    }

    public long getNewPermissionsRaw()
    {
        return newPermissionsRaw;
    }

    @Override
    public String getPropertyIdentifier()
    {
        return IDENTIFIER;
    }

    @Override
    public List<Permission> getOldValue()
    {
        return getOldPermissions();
    }

    @Override
    public List<Permission> getNewValue()
    {
        return getNewPermissions();
    }
}
