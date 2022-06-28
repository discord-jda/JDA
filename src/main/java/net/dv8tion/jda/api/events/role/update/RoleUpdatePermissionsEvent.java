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

package net.dv8tion.jda.api.events.role.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;

import javax.annotation.Nonnull;
import java.util.EnumSet;

/**
 * Indicates that a {@link net.dv8tion.jda.api.entities.Role Role} updated its permissions.
 *
 * <p>Can be used to retrieve the old permissions.
 *
 * <p>Identifier: {@code permission}
 */
public class RoleUpdatePermissionsEvent extends GenericRoleUpdateEvent<EnumSet<Permission>>
{
    public static final String IDENTIFIER = "permission";

    private final long oldPermissionsRaw;
    private final long newPermissionsRaw;

    public RoleUpdatePermissionsEvent(@Nonnull JDA api, long responseNumber, @Nonnull Role role, long oldPermissionsRaw)
    {
        super(api, responseNumber, role, Permission.getPermissions(oldPermissionsRaw), role.getPermissions(), IDENTIFIER);
        this.oldPermissionsRaw = oldPermissionsRaw;
        this.newPermissionsRaw = role.getPermissionsRaw();
    }

    /**
     * The old permissions
     *
     * @return The old permissions
     */
    @Nonnull
    public EnumSet<Permission> getOldPermissions()
    {
        return getOldValue();
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

    /**
     * The new permissions
     *
     * @return The new permissions
     */
    @Nonnull
    public EnumSet<Permission> getNewPermissions()
    {
        return getNewValue();
    }

    /**
     * The new permissions
     *
     * @return The new permissions
     */
    public long getNewPermissionsRaw()
    {
        return newPermissionsRaw;
    }

    @Nonnull
    @Override
    public EnumSet<Permission> getOldValue()
    {
        return super.getOldValue();
    }

    @Nonnull
    @Override
    public EnumSet<Permission> getNewValue()
    {
        return super.getNewValue();
    }
}
