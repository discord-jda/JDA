/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.core.entities;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.managers.PermOverrideManager;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;

import javax.annotation.CheckReturnValue;
import java.util.List;

/**
 * Represents the specific {@link net.dv8tion.jda.core.entities.Member Member} or {@link net.dv8tion.jda.core.entities.Role Role}
 * permission overrides that can be set for channels.
 */
public interface PermissionOverride
{
    /**
     * This is the raw binary representation (as a base 10 long) of the permissions <b>allowed</b> by this override.
     * <br>The long relates to the offsets used by each {@link net.dv8tion.jda.core.Permission Permission}.
     *
     * @return Never-negative long containing the binary representation of the allowed permissions of this override.
     */
    long getAllowedRaw();

    /**
     * This is the raw binary representation (as a base 10 long) of the permissions <b>not affected</b> by this override.
     * <br>The long relates to the offsets used by each {@link net.dv8tion.jda.core.Permission Permission}.
     *
     * @return Never-negative long containing the binary representation of the unaffected permissions of this override.
     */
    long getInheritRaw();

    /**
     * This is the raw binary representation (as a base 10 long) of the permissions <b>denied</b> by this override.
     * <br>The long relates to the offsets used by each {@link net.dv8tion.jda.core.Permission Permission}.
     *
     * @return Never-negative long containing the binary representation of the denied permissions of this override.
     */
    long getDeniedRaw();

    /**
     * An unmodifiable list of all {@link net.dv8tion.jda.core.Permission Permissions} that are specifically allowed by this override.
     *
     * @return Possibly-empty unmodifiable list of allowed {@link net.dv8tion.jda.core.Permission Permissions}.
     */
    List<Permission> getAllowed();

    /**
     * An unmodifiable list of all {@link net.dv8tion.jda.core.Permission Permission} that are unaffected by this override.
     *
     * @return Possibly-empty unmodifiable list of unaffected {@link net.dv8tion.jda.core.Permission Permissions}.
     */
    List<Permission> getInherit();

    /**
     * An unmodifiable list of all {@link net.dv8tion.jda.core.Permission Permissions} that are denied by this override.
     *
     * @return Possibly-empty unmodifiable list of denied {@link net.dv8tion.jda.core.Permission Permissions}.
     */
    List<Permission> getDenied();

    /**
     * The {@link net.dv8tion.jda.core.JDA JDA} instance that this {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}
     * is related to.
     *
     * @return Never-null {@link net.dv8tion.jda.core.JDA JDA} instance.
     */
    JDA getJDA();

    /**
     * If this {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride} is an override dealing with
     * a {@link net.dv8tion.jda.core.entities.Member Member}, then this method will return the related {@link net.dv8tion.jda.core.entities.Member Member}.
     * <br>Otherwise, this method returns {@code null}.
     * <br>Basically: if {@link net.dv8tion.jda.core.entities.PermissionOverride#isMemberOverride()}
     * returns {@code false}, this returns {@code null}.
     *
     * @return Possibly-null related {@link net.dv8tion.jda.core.entities.Member Member}.
     */
    Member getMember();

    /**
     * If this {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride} is an override dealing with
     * a {@link net.dv8tion.jda.core.entities.Role Role}, then this method will return the related {@link net.dv8tion.jda.core.entities.Role Role}.
     * <br>Otherwise, this method returns {@code null}.
     * Basically: if {@link net.dv8tion.jda.core.entities.PermissionOverride#isRoleOverride()}
     * returns {@code false}, this returns {@code null}.
     *
     * @return Possibly-null related {@link net.dv8tion.jda.core.entities.Role}.
     */
    Role getRole();

    /**
     * The {@link net.dv8tion.jda.core.entities.Channel Channel} that this {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride} affects.
     *
     * @return Never-null related {@link net.dv8tion.jda.core.entities.Channel Channel} that this override is part of.
     */
    Channel getChannel();

    /**
     * The {@link net.dv8tion.jda.core.entities.Guild Guild} that the {@link net.dv8tion.jda.core.entities.Channel Channel}
     * returned from {@link net.dv8tion.jda.core.entities.PermissionOverride#getChannel()} is a part of.
     * By inference, this is the {@link net.dv8tion.jda.core.entities.Guild Guild} that this
     * {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride} is part of.
     *
     * @return Never-null related {@link net.dv8tion.jda.core.entities.Guild Guild}.
     */
    Guild getGuild();

    /**
     * Used to determine if this {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride} relates to
     * a specific {@link net.dv8tion.jda.core.entities.Member Member}.
     *
     * @return True if this override is a user override.
     */
    boolean isMemberOverride();

    /**
     * Used to determine if this {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride} relates to
     * a specific {@link net.dv8tion.jda.core.entities.Role Role}.
     *
     * @return True if this override is a role override.
     */
    boolean isRoleOverride();

    /**
     * Returns the {@link net.dv8tion.jda.core.managers.PermOverrideManager PermOverrideManager} for this PermissionOverride.
     * <br>In the PermOverrideManager you can modify the permissions of the override.
     * You modify multiple fields in one request by chaining setters before calling {@link net.dv8tion.jda.core.requests.RestAction#queue() RestAction.queue()}.
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.core.Permission#MANAGE_CHANNEL Permission.MANAGE_CHANNEL}
     *         or {@link net.dv8tion.jda.core.Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *
     * @return The PermOverrideManager of this override.
     */
    PermOverrideManager getManager();

    /**
     * Deletes this PermissionOverride.
     *
     * <p>Possible ErrorResponses include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_OVERRIDE}
     *     <br>If the the override was already deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>If the channel this override was a part of was already deleted</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>If we were removed from the Guild</li>
     * </ul>
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         if we don't have the permission to {@link net.dv8tion.jda.core.Permission#MANAGE_CHANNEL MANAGE_CHANNEL}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @CheckReturnValue
    AuditableRestAction<Void> delete();
}
