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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that the Icon of a {@link net.dv8tion.jda.api.entities.Role Role} changed.
 *
 * <p>Can be used to detect when a role icon changes and retrieve the old one
 *
 * <p>Identifier: {@code icon}
 */
public class RoleUpdateIconEvent extends GenericRoleUpdateEvent<String>
{
    public static final String IDENTIFIER = "icon";

    public RoleUpdateIconEvent(@Nonnull JDA api, long responseNumber, @Nonnull Role role, @Nullable String oldIconId)
    {
        super(api, responseNumber, role, oldIconId, role.getIconId(), IDENTIFIER);
    }

    /**
     * The old icon id
     *
     * @return The old icon id, or null
     */
    @Nullable
    public String getOldIconId()
    {
        return getOldValue();
    }

    /**
     * The url of the old icon
     *
     * @return The url of the old icon, or null
     */
    @Nullable
    public String getOldIconUrl()
    {
        return previous == null ? null : String.format(Role.ICON_URL, role.getId(), previous);
    }

    /**
     * The old icon id
     *
     * @return The old icon id, or null
     */
    @Nullable
    public String getNewIconId()
    {
        return getNewValue();
    }

    /**
     * The url of the new icon
     *
     * @return The url of the new icon, or null
     */
    @Nullable
    public String getNewIconUrl()
    {
        return next == null ? null : String.format(Role.ICON_URL, role.getId(), next);
    }
}
