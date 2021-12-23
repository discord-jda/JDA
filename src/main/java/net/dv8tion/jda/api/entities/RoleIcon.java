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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.managers.RoleManager;
import net.dv8tion.jda.api.utils.ImageProxy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * An object representing a Role's icon.
 *
 * @see Role#getIcon
 */
public class RoleIcon
{
    /** Template for {@link #getIconUrl()}. */
    public static final String ICON_URL = "https://cdn.discordapp.com/role-icons/%s/%s.png";

    private final JDA jda;
    private final String iconId;
    private final String emoji;
    private final long roleId;

    public RoleIcon(JDA jda, String iconId, String emoji, long roleId)
    {
        this.jda = jda;
        this.iconId = iconId;
        this.emoji = emoji;
        this.roleId = roleId;
    }

    /**
     * The Discord hash-id of the {@link net.dv8tion.jda.api.entities.Role Role} icon image.
     * If no icon has been set or an emoji is used in its place, this returns {@code null}.
     * <p>The Role icon can be modified using {@link RoleManager#setIcon(Icon)}.
     *
     * @return Possibly-null String containing the Role's icon hash-id.
     *
     * @since  4.3.1
     */
    @Nullable //TODO remove
    public String getIconId()
    {
        return iconId;
    }

    /**
     * The URL of the {@link net.dv8tion.jda.api.entities.Role Role} icon image.
     * If no icon has been set or an emoji is used in its place, this returns {@code null}.
     * <p>The Role icon can be modified using {@link RoleManager#setIcon(Icon)}.
     *
     * @return Possibly-null String containing the Role's icon URL.
     *
     * @since  4.3.1
     */
    @Nullable //TODO remove
    public String getIconUrl()
    {
        String iconId = getIconId();
        return iconId == null ? null : String.format(ICON_URL, roleId, iconId);
    }

    //TODO docs
    @Nullable
    public ImageProxy getIcon()
    {
        if (iconId == null) return null;

        final String iconUrl = String.format(ICON_URL, roleId, iconId);

        return new ImageProxy(getJDA(), iconUrl, iconId, "png");
    }

    /**
     * The Unicode Emoji of this {@link net.dv8tion.jda.api.entities.Role Role} that is used instead of a custom image.
     * If no emoji has been set, this returns {@code null}.
     * <p>The Role emoji can be modified using {@link RoleManager#setIcon(String)}.
     *
     * @return Possibly-null String containing the Role's Unicode Emoji.
     *
     * @since  4.3.1
     */
    @Nullable
    public String getEmoji()
    {
        return emoji;
    }

    /**
     * Whether this {@link RoleIcon} is an emoji instead of a custom image.
     *
     * @return True, if this {@link RoleIcon} is an emoji
     */
    public boolean isEmoji()
    {
        return emoji != null;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof RoleIcon))
            return false;
        RoleIcon icon = (RoleIcon) obj;
        return Objects.equals(icon.iconId, iconId)
            && Objects.equals(icon.emoji, emoji);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(iconId, emoji);
    }

    //TODO docs
    @Nonnull
    public JDA getJDA()
    {
        return jda;
    }
}
