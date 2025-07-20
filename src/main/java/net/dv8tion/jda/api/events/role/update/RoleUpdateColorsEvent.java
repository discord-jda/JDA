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
import net.dv8tion.jda.internal.entities.RoleImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that a {@link net.dv8tion.jda.api.entities.Role Role} updated its colors.
 *
 * <p>Can be used to retrieve the old colors.
 *
 * <p>Identifier: {@code colors}
 *
 * @see net.dv8tion.jda.api.entities.Role.RoleColors
 * @see net.dv8tion.jda.internal.entities.RoleImpl.RoleColorsImpl
 */
public class RoleUpdateColorsEvent extends GenericRoleUpdateEvent<Role.RoleColors>
{
    public static final String IDENTIFIER = "colors";

    public RoleUpdateColorsEvent(@Nonnull JDA api, long responseNumber, @Nonnull Role role, @Nonnull Role.RoleColors oldColors)
    {
        super(api, responseNumber, role, oldColors, role.getColors(), IDENTIFIER);
    }

    /**
     * The old colors object.
     *
     * @return The old colors, or null
     */
    @Nullable
    public Role.RoleColors getOldColors()
    {
        return previous != RoleImpl.RoleColorsImpl.EMPTY ? previous : null;
    }

    /**
     * The raw rgb value of the old primary color.
     *
     * @return The raw rgb value of the old primary color
     */
    public int getOldColorRaw()
    {
        return getOldValue() == null ? Role.DEFAULT_COLOR_RAW : getOldValue().getPrimaryColorRaw();
    }

    /**
     * The new colors.
     *
     * @return The new colors, or null
     */
    public Role.RoleColors getNewColors()
    {
        return next != RoleImpl.RoleColorsImpl.EMPTY ? next : null;
    }

    /**
     * The raw rgb value of the new primary color.
     *
     * @return The raw rgb value of the new primary color.
     */
    public int getNewColorRaw()
    {
        return getNewValue() == null ? Role.DEFAULT_COLOR_RAW : getNewValue().getPrimaryColorRaw();
    }

    @Nullable
    @Override
    public Role.RoleColors getOldValue()
    {
        return super.getOldValue();
    }

    @Nullable
    @Override
    public Role.RoleColors getNewValue()
    {
        return super.getNewValue();
    }
}

