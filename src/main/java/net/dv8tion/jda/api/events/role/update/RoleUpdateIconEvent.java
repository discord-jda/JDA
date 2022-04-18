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
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.RoleIcon;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that the Icon of a {@link net.dv8tion.jda.api.entities.Role Role} changed.
 *
 * <p>Can be used to detect when a role's icon or emoji changes and retrieve the old one
 *
 * <p>Identifier: {@code icon}
 */
public class RoleUpdateIconEvent extends GenericRoleUpdateEvent<RoleIcon>
{
    public static final String IDENTIFIER = "icon";

    public RoleUpdateIconEvent(@Nonnull JDA api, long responseNumber, @Nonnull Role role, @Nullable RoleIcon oldIcon)
    {
        super(api, responseNumber, role, oldIcon, role.getIcon(), IDENTIFIER);
    }

    /**
     * The old icon
     *
     * @return The old icon
     */
    @Nullable
    public RoleIcon getOldIcon()
    {
        return getOldValue();
    }

    /**
     * The new icon
     *
     * @return The new icon
     */
    @Nullable
    public RoleIcon getNewIcon()
    {
        return getNewValue();
    }
}
