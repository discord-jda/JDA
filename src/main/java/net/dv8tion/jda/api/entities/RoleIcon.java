package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.managers.RoleManager;

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

    private final String iconId;
    private final String emoji;
    private final long roleId;

    public RoleIcon(String iconId, String emoji, long roleId)
    {
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
    @Nullable
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
    @Nullable
    public String getIconUrl()
    {
        String iconId = getIconId();
        return iconId == null ? null : String.format(ICON_URL, roleId, iconId);
    }

    /**
     * The Unicode Emoji of this {@link net.dv8tion.jda.api.entities.Role Role} that is used instead of a custom image.
     * If no emoji has been set, this returns {@code null}.
     * <p>The Role emoji can be modified using {@link RoleManager#setEmoji(String)}.
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
}
