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

import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.utils.EntityString;

import java.awt.Color;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The colors of a {@link Role}.
 *
 * <p>Roles can have up to three color components, and the appearance changes depending on how many components are configured.
 *
 * <ul>
 *     <li>If a role has no configured colors, then it uses the default color of the current display theme in the client, which contrasts with the theme's background. (See {@link #isDefault()})</li>
 *     <li>If a role only has a {@linkplain #getPrimary() primary} color, it uses a solid color style. (See {@link #isSolid()})</li>
 *     <li>If a role has two configured colors with {@link #getPrimary()} and {@link #getSecondary()}, then it uses a gradient of the two colors. (See {@link #isGradient()})</li>
 *     <li>If a role has three configured colors, it uses a holographic style instead. (See {@link #isHolographic()})</li>
 * </ul>
 */
public class RoleColors implements SerializableData {
    /**
     * Sentinel values used by Discord to indicate holographic style.
     *
     * @see #isHolographic()
     */
    public static final RoleColors DEFAULT_HOLOGRAPHIC = new RoleColors(11127295, 16759788, 16761760);

    /**
     * The default colors used by a role. The display color is determined by the selected theme on each client.
     *
     * @see #isDefault()
     */
    public static final RoleColors DEFAULT =
            new RoleColors(Role.DEFAULT_COLOR_RAW, Role.DEFAULT_COLOR_RAW, Role.DEFAULT_COLOR_RAW);

    private final int primary;
    private final int secondary;
    private final int tertiary;

    public RoleColors(int primary, int secondary, int tertiary) {
        this.primary = primary;
        this.secondary = secondary;
        this.tertiary = tertiary;
    }

    /**
     * Whether this role uses the default colors, which is determined by the client theme.
     *
     * @return True, if the primary color is {@link Role#DEFAULT_COLOR_RAW}
     */
    public boolean isDefault() {
        return this.primary == Role.DEFAULT_COLOR_RAW;
    }

    /**
     * Whether this role uses solid color style.
     *
     * @return True if only a primary color is configured.
     */
    public boolean isSolid() {
        return this.primary != Role.DEFAULT_COLOR_RAW
                && this.secondary == Role.DEFAULT_COLOR_RAW
                && this.tertiary == Role.DEFAULT_COLOR_RAW;
    }

    /**
     * Whether this role uses gradient style.
     *
     * @return True if a secondary color is configured but no tertiary color.
     */
    public boolean isGradient() {
        return this.tertiary == Role.DEFAULT_COLOR_RAW
                && this.secondary != Role.DEFAULT_COLOR_RAW
                && this.primary != Role.DEFAULT_COLOR_RAW;
    }

    /**
     * Whether this role uses holographic style.
     *
     * @return True, if the colors match {@link #DEFAULT_HOLOGRAPHIC}
     */
    public boolean isHolographic() {
        return this.equals(DEFAULT_HOLOGRAPHIC);
    }

    /**
     * The primary color component of the role.
     *
     * @return Primary Color or {@code null} if the default color is used
     *
     * @see    #getPrimaryRaw()
     */
    @Nullable
    public Color getPrimary() {
        return primary == Role.DEFAULT_COLOR_RAW ? null : new Color(primary);
    }

    /**
     * The primary color component of the role.
     *
     * @return Primary Color as a raw RGB value or {@link Role#DEFAULT_COLOR_RAW} if the default color is used
     *
     * @see    #getPrimary()
     */
    public int getPrimaryRaw() {
        return primary;
    }

    /**
     * The secondary color component of the role.
     *
     * <p>This is only set for roles with gradient colors
     * and only applied if the {@link Guild} has the {@code ENHANCED_ROLE_COLORS} feature enabled.
     *
     * @return Secondary Color or {@code null} if this role has no gradient color
     *
     * @see    #getSecondaryRaw()
     */
    @Nullable
    public Color getSecondary() {
        return secondary == Role.DEFAULT_COLOR_RAW ? null : new Color(secondary);
    }

    /**
     * The secondary color component of the role.
     *
     * <p>This is only set for roles with gradient colors
     * and only applied if the {@link Guild} has the {@code ENHANCED_ROLE_COLORS} feature enabled.
     *
     * @return Secondary Color as a raw RGB value or {@link Role#DEFAULT_COLOR_RAW} if this role has no gradient color
     *
     * @see    #getSecondary()
     */
    public int getSecondaryRaw() {
        return secondary;
    }

    /**
     * The tertiary color component of the role.
     *
     * <p>This is only set for roles with holographic style
     * and only applied if the {@link Guild} has the {@code ENHANCED_ROLE_COLORS} feature enabled.
     *
     * @return Tertiary Color or {@code null} if this role has no holographic style
     *
     * @see    #getTertiaryRaw()
     * @see    #isHolographic()
     * @see    #DEFAULT_HOLOGRAPHIC
     */
    @Nullable
    public Color getTertiary() {
        return tertiary == Role.DEFAULT_COLOR_RAW ? null : new Color(tertiary);
    }

    /**
     * The tertiary color component of the role.
     *
     * <p>This is only set for roles with holographic style
     * and only applied if the {@link Guild} has the {@code ENHANCED_ROLE_COLORS} feature enabled.
     *
     * @return Tertiary Color as a raw RGB value or {@link Role#DEFAULT_COLOR_RAW} if this role has no holographic style
     *
     * @see    #getTertiary()
     * @see    #isHolographic()
     * @see    #DEFAULT_HOLOGRAPHIC
     */
    public int getTertiaryRaw() {
        return tertiary;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof RoleColors)) {
            return false;
        }
        RoleColors other = (RoleColors) obj;
        return this.primary == other.primary && this.secondary == other.secondary && this.tertiary == other.tertiary;
    }

    @Override
    public int hashCode() {
        return Objects.hash(primary, secondary, tertiary);
    }

    @Override
    public String toString() {
        return new EntityString(this)
                .addMetadata("primary", colorToHex(primary))
                .addMetadata("secondary", colorToHex(secondary))
                .addMetadata("tertiary", colorToHex(tertiary))
                .toString();
    }

    private static String colorToHex(int color) {
        return color == Role.DEFAULT_COLOR_RAW ? null : Integer.toHexString(color);
    }

    @Nonnull
    @Override
    public DataObject toData() {
        return DataObject.empty()
                .put("primary_color", primary != Role.DEFAULT_COLOR_RAW ? primary & 0xFFFFFF : 0)
                .put("secondary_color", secondary != Role.DEFAULT_COLOR_RAW ? secondary & 0xFFFFFF : null)
                .put("tertiary_color", tertiary != Role.DEFAULT_COLOR_RAW ? tertiary & 0xFFFFFF : null);
    }
}
