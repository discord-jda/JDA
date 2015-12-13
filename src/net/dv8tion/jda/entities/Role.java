package net.dv8tion.jda.entities;

import net.dv8tion.jda.Permission;

/**
 * Created by Michael Ritter on 13.12.2015.
 */
public interface Role
{
    /**
     * The Position of this Role on the server (higher value -> higher role)
     * The @everyone Role always return -1
     *
     * @return The position of this Role as integer
     */
    int getPosition();

    /**
     * The Name of the Role
     *
     * @return The name of the Role as String
     */
    String getName();

    /**
     * Is this role managed? (Via plugins like Twitch)
     *
     * @return If this Role is managed
     */
    boolean isManaged();

    /**
     * Is this Role hoist? (Users with this Role are grouped in the online-list)
     *
     * @return If this Role is hoist
     */
    boolean isHoist();

    /**
     * The ID of this Role
     *
     * @return The Id of this Role
     */
    String getId();

    /**
     * The color this Role is displayed in
     *
     * @return integer value of Role-color
     */
    int getColor();

    /**
     * Checks if this Role has a given Permission
     * This does not check the Channel-specific Permissions
     *
     * @param perm the Permission to check for
     * @return If the given Permission is available to this Role
     */
    boolean hasPermission(Permission perm);
}
