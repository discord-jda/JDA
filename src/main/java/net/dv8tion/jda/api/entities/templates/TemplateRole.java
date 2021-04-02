package net.dv8tion.jda.api.entities.templates;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ISnowflake;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.time.OffsetDateTime;
import java.util.EnumSet;

/**
 * POJO for the roles information provided by a template.
 *
 * @see TemplateGuild#getRoles()
 */
public class TemplateRole implements ISnowflake
{
    private final long id;
    private final String name;
    private final int color;
    private final boolean hoisted;
    private final boolean mentionable;
    private final long rawPermissions;

    public TemplateRole(final long id, final String name, final int color, final boolean hoisted, final boolean mentionable, final long rawPermissions)
    {
        this.id = id;
        this.name = name;
        this.color = color;
        this.hoisted = hoisted;
        this.mentionable = mentionable;
        this.rawPermissions = rawPermissions;
    }

    /**
     * The ids of roles are their position as stored by Discord so this will not look like a typical snowflake.
     *
     * @return The id of the role as stored by Discord
     */
    @Override
    public long getIdLong()
    {
        return this.id;
    }

    /**
     * As the ids of roles are their position, the date of creation cannot be calculated.
     *
     * @return {@code null}
     */
    @Override
    public OffsetDateTime getTimeCreated()
    {
        return null;
    }

    /**
     * The Name of this {@link TemplateRole Role}.
     *
     * @return Never-null String containing the name of this {@link TemplateRole Role}.
     */
    @Nonnull
    public String getName()
    {
        return this.name;
    }

    /**
     * The color this {@link TemplateRole Role} is displayed in.
     *
     * @return Color value of Role-color
     *
     * @see    #getColorRaw()
     */
    @Nullable
    public Color getColor()
    {
        return this.color == net.dv8tion.jda.api.entities.Role.DEFAULT_COLOR_RAW ? null : new Color(this.color);
    }

    /**
     * The raw color RGB value used for this role
     * <br>Defaults to {@link net.dv8tion.jda.api.entities.Role#DEFAULT_COLOR_RAW} if this role has no set color
     *
     * @return The raw RGB color value or default
     */
    public int getColorRaw()
    {
        return this.color;
    }

    /**
     * Whether this {@link TemplateRole Role} is hoisted
     * <br>Members in a hoisted role are displayed in their own grouping on the user-list
     *
     * @return True, if this {@link TemplateRole Role} is hoisted.
     */
    public boolean isHoisted()
    {
        return this.hoisted;
    }

    /**
     * Whether or not this Role is mentionable
     *
     * @return True, if Role is mentionable.
     */
    public boolean isMentionable()
    {
        return this.mentionable;
    }

    /**
     * The Guild-Wide Permissions this PermissionHolder holds.
     * <br><u>Changes to the returned set do not affect this entity directly.</u>
     *
     * @return An EnumSet of Permissions granted to this PermissionHolder.
     */
    @Nonnull
    public EnumSet<Permission> getPermissions()
    {
        return Permission.getPermissions(rawPermissions);
    }

    /**
     * The {@code long} representation of the literal permissions that this {@link TemplateRole Role} has.
     *
     * @return Never-negative long containing offset permissions of this role.
     */
    public long getPermissionsRaw()
    {
        return rawPermissions;
    }
}
