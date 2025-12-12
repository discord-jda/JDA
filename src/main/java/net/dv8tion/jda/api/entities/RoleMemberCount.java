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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a role ID alongside the number of members who have it.
 */
public interface RoleMemberCount {
    /**
     * The role ID as a {@code long}.
     *
     * @return The role ID
     */
    long getRoleIdLong();

    /**
     * The role ID.
     *
     * @return The role ID
     */
    @Nonnull
    default String getRoleId() {
        return Long.toUnsignedString(getRoleIdLong());
    }

    /**
     * The corresponding {@link Role} object.
     * <br>This might return {@code null} if the role has been deleted after the counts were received.
     *
     * @return The corresponding {@link Role} object or {@code null}
     */
    @Nullable
    Role getRole();

    /**
     * The number of members which have the role.
     *
     * @return The number of members which have the role
     */
    int getMemberCount();
}
