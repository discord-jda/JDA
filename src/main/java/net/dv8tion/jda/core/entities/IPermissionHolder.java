package net.dv8tion.jda.core.entities;

import net.dv8tion.jda.core.Permission;

import java.util.Collection;
import java.util.List;

public interface IPermissionHolder {

    /**
     * The Guild to which this PermissionHolder is related
     * 
     * @return 
     *      A never-null Guild to which this PermissionHolder is linked
     */
    Guild getGuild();

    /**
     * The Guild-Wide Permissions this PermissionHolder holds.
     *
     * @return
     *      An immutable List of Permissions granted to this PermissionHolder.
     */
    List<Permission> getPermissions();

    /**
     * Checks whether or not this PermissionHolder has the given {@link net.dv8tion.jda.core.Permission Permissions} in the Guild.
     *
     * @param permissions
     *          Permissions to check for.
     * @return
     *      True - if all of the specified Permissions are granted to this PermissionHolder.
     */
    boolean hasPermission(Permission... permissions);

    /**
     * Checks whether or not this PermissionHolder has the {@link net.dv8tion.jda.core.Permission Permissions} in the provided
     * Collection&lt;Permission&gt; in the Guild.
     *
     * @param permissions
     *          Permissions to check for.
     * @return
     *      True - if all of the specified Permissions are granted to this PermissionHolder.
     */
    boolean hasPermission(Collection<Permission> permissions);

    /**
     * Checks whether or not this PermissionHolder has the given {@link net.dv8tion.jda.core.Permission Permissions} in the specified Channel.
     *
     * @param channel
     *          The {@link net.dv8tion.jda.core.entities.Channel Channel} in which to check.
     * @param permissions
     *          Permissions to check for.
     * @return
     *      True - if all of the specified Permissions are granted to this PermissionHolder in the provided Channel.
     */
    boolean hasPermission(Channel channel, Permission... permissions);

    /**
     * Checks whether or not this PermissionHolder has the {@link net.dv8tion.jda.core.Permission Permissions} in the provided
     * Collection&lt;Permission&gt; in the specified Channel.
     *
     * @param channel
     *          The {@link net.dv8tion.jda.core.entities.Channel Channel} in which to check.
     * @param permissions
     *          Permissions to check for.
     * @return
     *      True - if all of the specified Permissions are granted to this PermissionHolder in the provided Channel.
     */
    boolean hasPermission(Channel channel, Collection<Permission> permissions);
}
