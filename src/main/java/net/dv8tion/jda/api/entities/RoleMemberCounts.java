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

package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Wrapper around a map of role IDs to the number of members with the role.
 *
 * @see Guild#retrieveRoleMemberCounts()
 */
public interface RoleMemberCounts {
    /**
     * Gets the number of members with the given role ID. Returns {@code 0} if the role is unknown.
     *
     * @param  roleId
     *         The role ID to get the member count for
     *
     * @return The number of members with the corresponding role, or {@code 0}
     */
    int get(long roleId);

    /**
     * Gets the number of members with the given role ID. Returns {@code 0} if the role is unknown.
     *
     * @param  roleId
     *         The role ID to get the member count for
     *
     * @return The number of members with the corresponding role, or {@code 0}
     */
    default int get(@Nonnull String roleId) {
        Checks.notNull(roleId, "Role ID");
        return get(Long.parseLong(roleId));
    }

    /**
     * Gets the number of members with the given {@link Role}. Returns {@code 0} if the role is unknown.
     *
     * @param  role
     *         The role to get the member count for
     *
     * @return The number of members with the corresponding role, or {@code 0}
     */
    default int get(@Nonnull Role role) {
        Checks.notNull(role, "Role");
        return get(role.getIdLong());
    }

    /**
     * Whether the given role ID has a member count.
     *
     * @param  roleId
     *         The role ID to check a member count for
     *
     * @return {@code true} if the role has a member count, {@code false} otherwise
     */
    boolean contains(long roleId);

    /**
     * Whether the given role ID has a member count.
     *
     * @param  roleId
     *         The role ID to check a member count for
     *
     * @return {@code true} if the role has a member count, {@code false} otherwise
     */
    default boolean contains(@Nonnull String roleId) {
        Checks.notNull(roleId, "Role ID");
        return contains(Long.parseLong(roleId));
    }

    /**
     * Whether the given {@link Role} has a member count.
     *
     * @param  role
     *         The role to check a member count for
     *
     * @return {@code true} if the role has a member count, {@code false} otherwise
     */
    default boolean contains(@Nonnull Role role) {
        Checks.notNull(role, "Role");
        return contains(role.getIdLong());
    }

    /**
     * Returns an unmodifiable list of {@link RoleMemberCount}.
     *
     * @return An unmodifiable list of role member counts
     */
    @Nonnull
    @Unmodifiable
    List<RoleMemberCount> asList();
}
