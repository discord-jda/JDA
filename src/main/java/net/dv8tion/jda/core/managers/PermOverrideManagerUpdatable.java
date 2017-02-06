/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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

package net.dv8tion.jda.core.managers;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.PermissionOverride;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import org.apache.http.util.Args;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collection;

/**
 * An {@link #update() updatable} manager that allows
 * to modify {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride} settings
 * such as the granted and denied {@link net.dv8tion.jda.core.Permission Permissions}.
 *
 * <p>This manager allows to modify multiple fields at once followed by a call of {@link #update()}!
 * <br>Default is no permissions granted/denied.
 *
 * <p>The {@link net.dv8tion.jda.core.managers.PermOverrideManager PermOverrideManager} implementation
 * simplifies this process by giving simple setters that return the {@link #update() update} {@link net.dv8tion.jda.core.requests.RestAction RestAction}
 *
 * <p><b>Note</b>: To {@link #update() update} this manager
 * the currently logged in account requires the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_PERMISSIONS MANAGE_PERMISSIONS}
 * in the parent {@link net.dv8tion.jda.core.entities.Channel Channel}
 */
public class PermOverrideManagerUpdatable
{
    protected final PermissionOverride override;

    protected Long allow;
    protected Long deny;
    protected boolean set;

    /**
     * Creates a new PermOverrideManagerUpdatable instance
     *
     * @param override
     *        The {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride} to manage
     */
    public PermOverrideManagerUpdatable(PermissionOverride override)
    {
        this.override = override;
    }

    /**
     * The {@link net.dv8tion.jda.core.JDA JDA} instance of this Manager
     *
     * @return the corresponding JDA instance
     */
    public JDA getJDA()
    {
        return override.getJDA();
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.Guild Guild} this Manager's
     * {@link net.dv8tion.jda.core.entities.Channel Channel} is in.
     * <br>This is logically the same as calling {@code getPermissionOverride().getGuild()}
     *
     * @return The parent {@link net.dv8tion.jda.core.entities.Guild Guild}
     */
    public Guild getGuild()
    {
        return override.getGuild();
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.Channel Channel} this Manager's
     * {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride} is in.
     * <br>This is logically the same as calling {@code getPermissionOverride().getChannel()}
     *
     * @return The parent {@link net.dv8tion.jda.core.entities.Channel Channel}
     */
    public Channel getChannel()
    {
        return override.getChannel();
    }

    /**
     * The target {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}
     * that will be modified by this Manager
     *
     * @return The target {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}
     */
    public PermissionOverride getPermissionOverride()
    {
        return override;
    }

    /**
     * Grants the specified permission bits
     * to the target {@link net.dv8tion.jda.core.entities.PermissionOverride}
     *
     * @param  permissions
     *         Raw permission bits to grant
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If any of the provided permissions are not accessible
     *
     * @return The current Manager instance for chaining convenience
     */
    public PermOverrideManagerUpdatable grant(long permissions)
    {
        return grant(Permission.getPermissions(permissions));
    }

    /**
     * Grants the specified {@link net.dv8tion.jda.core.Permission Permissions}
     * to the target {@link net.dv8tion.jda.core.entities.PermissionOverride}
     *
     * @param  permissions
     *         Permissions to grant
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If any of the provided permissions are not accessible
     * @throws IllegalArgumentException
     *         If any of the provided permissions is {@code null}
     *
     * @return The current Manager instance for chaining convenience
     */
    public PermOverrideManagerUpdatable grant(Permission... permissions)
    {
        return grant(Arrays.asList(permissions));
    }

    /**
     * Grants the specified {@link net.dv8tion.jda.core.Permission Permissions}
     * to the target {@link net.dv8tion.jda.core.entities.PermissionOverride}
     *
     * @param  permissions
     *         Permissions to grant
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If any of the provided permissions are not accessible
     * @throws IllegalArgumentException
     *         If any of the provided permissions is {@code null}
     *
     * @return The current Manager instance for chaining convenience
     */
    public PermOverrideManagerUpdatable grant(Collection<Permission> permissions)
    {
        Args.notNull(permissions, "Permission Collection");
        permissions.forEach(perm ->
        {
            Args.notNull(perm, "Permission in Permission Collection");
            //checkPermission(perm);
        });
        setupValues();

        long allowBits = Permission.getRaw(permissions);
        allow |= allowBits;
        deny &= ~allowBits;

        return this;
    }

    /**
     * Denies the specified permission bits
     * from the target {@link net.dv8tion.jda.core.entities.PermissionOverride}
     *
     * @param  permissions
     *         Raw permission bits to deny
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If any of the provided permissions are not accessible
     *
     * @return The current Manager instance for chaining convenience
     */
    public PermOverrideManagerUpdatable deny(long permissions)
    {
        return deny(Permission.getPermissions(permissions));
    }

    /**
     * Denies the specified {@link net.dv8tion.jda.core.Permission Permissions}
     * from the target {@link net.dv8tion.jda.core.entities.PermissionOverride}
     *
     * @param  permissions
     *         Permissions to deny
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If any of the provided permissions are not accessible
     * @throws IllegalArgumentException
     *         If any of the provided permissions is {@code null}
     *
     * @return The current Manager instance for chaining convenience
     */
    public PermOverrideManagerUpdatable deny(Permission... permissions)
    {
        return deny(Arrays.asList(permissions));
    }

    /**
     * Denies the specified {@link net.dv8tion.jda.core.Permission Permissions}
     * from the target {@link net.dv8tion.jda.core.entities.PermissionOverride}
     *
     * @param  permissions
     *         Permissions to deny
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If any of the provided permissions are not accessible
     * @throws IllegalArgumentException
     *         If any of the provided permissions is {@code null}
     *
     * @return The current Manager instance for chaining convenience
     */
    public PermOverrideManagerUpdatable deny(Collection<Permission> permissions)
    {
        Args.notNull(permissions, "Permission Collection");
        permissions.forEach(perm ->
        {
            Args.notNull(perm, "Permission in Permission Collection");
            //checkPermission(perm);
        });
        setupValues();

        long denyBits = Permission.getRaw(permissions);
        allow &= ~denyBits;
        deny |= denyBits;

        return this;
    }

    /**
     * Clears the specified permission bits
     * from the target {@link net.dv8tion.jda.core.entities.PermissionOverride}
     * <br>This will make the specified Permissions be inherited
     *
     * @param  permission
     *         Raw permission bits to clear
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If any of the provided permissions are not accessible
     *
     * @return The current Manager instance for chaining convenience
     */
    public PermOverrideManagerUpdatable clear(long permission)
    {
        return clear(Permission.getPermissions(permission));
    }

    /**
     * Clears the specified {@link net.dv8tion.jda.core.Permission Permissions}
     * from the target {@link net.dv8tion.jda.core.entities.PermissionOverride}
     * <br>This will make the specified Permissions be inherited
     *
     * @param  permissions
     *         Permissions to clear
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If any of the provided permissions are not accessible
     * @throws IllegalArgumentException
     *         If any of the provided permissions is {@code null}
     *
     * @return The current Manager instance for chaining convenience
     */
    public PermOverrideManagerUpdatable clear(Permission... permissions)
    {
        return clear(Arrays.asList(permissions));
    }

    /**
     * Clears the specified {@link net.dv8tion.jda.core.Permission Permissions}
     * from the target {@link net.dv8tion.jda.core.entities.PermissionOverride}
     * <br>This will make the specified Permissions be inherited
     *
     * @param  permissions
     *         Permissions to clear
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If any of the provided permissions are not accessible
     * @throws IllegalArgumentException
     *         If any of the provided permissions is {@code null}
     *
     * @return The current Manager instance for chaining convenience
     */
    public PermOverrideManagerUpdatable clear(Collection<Permission> permissions)
    {
        Args.notNull(permissions, "Permission Collection");
        permissions.forEach(perm ->
        {
            Args.notNull(perm, "Permission in Permission Collection");
            //checkPermission(perm);
        });
        setupValues();

        long clearBits = Permission.getRaw(permissions);
        allow &= ~clearBits;
        deny &= ~clearBits;

        return this;
    }

    /**
     * The granted {@link net.dv8tion.jda.core.Permission Permissions}
     * value represented as raw long bits.
     * <br>Use {@link Permission#getPermissions(long)} to retrieve a list of {@link net.dv8tion.jda.core.Permission Permissions}
     * from the returned bits.
     *
     * <p>This value represents all permissions that should be granted by this {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}
     *
     * @return Granted {@link net.dv8tion.jda.core.Permission Permissions} value
     *         or {@code null} if {@link #isSet()} is {@code false}
     */
    public Long getAllowBits()
    {
        return allow;
    }

    /**
     * The denied {@link net.dv8tion.jda.core.Permission Permissions}
     * value represented as raw long bits.
     * <br>Use {@link Permission#getPermissions(long)} to retrieve a list of {@link net.dv8tion.jda.core.Permission Permissions}
     * from the returned bits.
     *
     * <p>This value represents all permissions that should be denied by this {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}
     *
     * @return Denied {@link net.dv8tion.jda.core.Permission Permissions} value
     *         or {@code null} if {@link #isSet()} is {@code false}
     */
    public Long getDenyBits()
    {
        return deny;
    }

    /**
     * The inherited {@link net.dv8tion.jda.core.Permission Permissions}
     * value represented as raw long bits.
     * <br>Use {@link Permission#getPermissions(long)} to retrieve a list of {@link net.dv8tion.jda.core.Permission Permissions}
     * from the returned bits.
     *
     * <p>This value represents all permissions that are not granted or denied by the settings of this Manager instance
     * - thus they represent all permissions that are set to inherit from other overrides.
     *
     * @return Inherited {@link net.dv8tion.jda.core.Permission Permissions} value
     *         or {@code null} if {@link #isSet()} is {@code false}
     */
    public Long getInheritBits()
    {
        if (!set)
            return null;

        long maxPerms = 0;
        for (Permission perm : Permission.values())
        {
            if (perm.getOffset() > maxPerms)
                maxPerms = perm.getOffset();
        }
        maxPerms = ~(1 << (maxPerms + 1)); //push 1 to max offset + 1, then flip to get a full-permission bit mask.

        return (~allow | ~deny) & maxPerms;
    }

    /**
     * Whether anything has been modified yet
     *
     * @return Whether anything has been modified
     */
    public boolean isSet()
    {
        return set;
    }

    /**
     * Resets all {@link net.dv8tion.jda.core.Permission Permission} values
     * <br>This is automatically called by {@link #update()}
     */
    public void reset()
    {
        set = false;
        allow = null;
        deny = null;
    }

    /**
     * Creates a new {@link net.dv8tion.jda.core.requests.RestAction RestAction} instance
     * that will apply <b>all</b> changes that have been made to this manager instance.
     * <br>If no changes have been made this will simply return {@link net.dv8tion.jda.core.requests.RestAction.EmptyRestAction EmptyRestAction}.
     *
     * <p>Before applying new changes it is recommended to call {@link #reset()} to reset previous changes.
     * <br>This is automatically called if this method returns successfully.
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} for this
     * update include the following:
     * <ul>
     *      <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_OVERRIDE UNKNOWN_OVERRIDE}
     *      <br>If the PermissionOverride was deleted before finishing the task</li>
     *
     *      <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *      <br>If the currently logged in account was removed from the Guild before finishing the task</li>
     *
     *      <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *      <br>If the currently logged in account loses the {@link net.dv8tion.jda.core.Permission#MANAGE_PERMISSIONS MANAGE_PERMISSIONS Permission}</li>
     * </ul>
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_PERMISSIONS MANAGE_PERMISSIONS}
     *         in the {@link #getChannel() Channel}
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction}
     *         <br>Applies all changes that have been made in a single api-call.
     */
    public RestAction<Void> update()
    {
        checkPermission(Permission.MANAGE_PERMISSIONS);

        if (!shouldUpdate())
            return new RestAction.EmptyRestAction<>(null);

        String targetId = override.isRoleOverride() ? override.getRole().getId() : override.getMember().getUser().getId();
        JSONObject body = new JSONObject()
                .put("id", targetId)
                .put("type", override.isRoleOverride() ? "role" : "member")
                .put("allow", getAllowBits())
                .put("deny", getDenyBits());

        reset();
        Route.CompiledRoute route = Route.Channels.MODIFY_PERM_OVERRIDE.compile(override.getChannel().getId(), targetId);
        return new RestAction<Void>(getJDA(), route, body)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    protected boolean shouldUpdate()
    {
        return set && (allow != override.getAllowedRaw() || deny != override.getDeniedRaw());
    }

    protected void setupValues()
    {
        if (!set)
        {
            set = true;
            allow = override.getAllowedRaw();
            deny = override.getDeniedRaw();
        }
    }

    protected void checkPermission(Permission perm)
    {
        if (!getGuild().getSelfMember().hasPermission(getChannel(), perm))
            throw new PermissionException(perm);
    }
}
