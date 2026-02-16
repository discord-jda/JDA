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

import net.dv8tion.jda.annotations.ForRemoval;
import net.dv8tion.jda.annotations.ReplaceWith;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.utils.PermissionSet;

import java.util.EnumSet;

import javax.annotation.Nonnull;

/**
 * Indicates that a {@link net.dv8tion.jda.api.entities.Role Role} updated its permissions.
 *
 * <p>Can be used to retrieve the old permissions.
 *
 * <p>Identifier: {@code permission}
 */
public class RoleUpdatePermissionsEvent extends GenericRoleUpdateEvent<EnumSet<Permission>> {
    public static final String IDENTIFIER = "permission";

    private final PermissionSet oldPermissions;
    private final PermissionSet newPermissions;

    public RoleUpdatePermissionsEvent(
            @Nonnull JDA api, long responseNumber, @Nonnull Role role, @Nonnull PermissionSet oldPermissions) {
        super(api, responseNumber, role, oldPermissions.toEnumSet(), role.getPermissions(), IDENTIFIER);
        this.oldPermissions = oldPermissions;
        this.newPermissions = role.getPermissionSet();
    }

    @Nonnull
    public PermissionSet getOldPermissionSet() {
        return oldPermissions;
    }

    /**
     * The old permissions
     *
     * @return The old permissions
     */
    @Nonnull
    public EnumSet<Permission> getOldPermissions() {
        return getOldValue();
    }

    /**
     * The old permissions
     *
     * @return The old permissions
     */
    @Deprecated
    @ForRemoval
    @ReplaceWith("getOldPermissionSet()")
    public long getOldPermissionsRaw() {
        return oldPermissions.toBigInteger().longValue();
    }

    @Nonnull
    public PermissionSet getNewPermissionSet() {
        return newPermissions;
    }

    /**
     * The new permissions
     *
     * @return The new permissions
     */
    @Nonnull
    public EnumSet<Permission> getNewPermissions() {
        return getNewValue();
    }

    /**
     * The new permissions
     *
     * @return The new permissions
     */
    @Deprecated
    @ForRemoval
    @ReplaceWith("getNewPermissionSet()")
    public long getNewPermissionsRaw() {
        return newPermissions.toBigInteger().longValue();
    }

    @Nonnull
    @Override
    public EnumSet<Permission> getOldValue() {
        return super.getOldValue();
    }

    @Nonnull
    @Override
    public EnumSet<Permission> getNewValue() {
        return super.getNewValue();
    }
}
