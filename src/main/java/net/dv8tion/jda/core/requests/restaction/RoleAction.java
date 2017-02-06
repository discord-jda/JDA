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

package net.dv8tion.jda.core.requests.restaction;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.EntityBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import org.apache.http.util.Args;
import org.json.JSONObject;

import java.awt.Color;
import java.util.Collection;

/**
 * Extension of {@link net.dv8tion.jda.core.requests.RestAction RestAction} specifically
 * designed to create a {@link net.dv8tion.jda.core.entities.Role Role}.
 * This extension allows setting properties before executing the action.
 *
 * @since  3.0
 * @author Florian Spieß
 */
public class RoleAction extends RestAction<Role>
{

    protected final Guild guild;
    protected long permissions = 0;
    protected String name = null;
    protected Integer color = null;
    protected Boolean hoisted = null;
    protected Boolean mentionable = null;

    /**
     * Creates a new RoleAction instance
     *
     * @param  route
     *         A {@link net.dv8tion.jda.core.requests.Route.CompiledRoute CompiledRoute}
     *         Which will be used for the Role creation.
     * @param  guild
     *         The {@link net.dv8tion.jda.core.entities.Guild Guild} for which the Role should be created.
     */
    public RoleAction(Route.CompiledRoute route, Guild guild)
    {
        super(guild.getJDA(), route, null);
        this.guild = guild;
    }

    /**
     * Sets the name for new role (optional)
     *
     * @param  name
     *         The name for the new role, null to use default name
     *
     * @return The current RoleAction, for chaining convenience
     */
    public RoleAction setName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Sets whether or not the new role should be hoisted
     *
     * @param  hoisted
     *         Whether the new role should be hoisted (grouped). Default is {@code false}
     *
     * @return The current RoleAction, for chaining convenience
     */
    public RoleAction setHoisted(Boolean hoisted)
    {
        this.hoisted = hoisted;
        return this;
    }

    /**
     * Sets whether the new role should be mentionable by members of
     * the parent {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *
     * @param  mentionable
     *         Whether the new role should be mentionable. Default is {@code false}
     *
     * @return The current RoleAction, for chaining convenience
     */
    public RoleAction setMentionable(Boolean mentionable)
    {
        this.mentionable = mentionable;
        return this;
    }

    /**
     * Sets the color which the new role should be displayed with.
     *
     * @param  color
     *         An {@link java.awt.Color Color} for the new role, null to use default white/black
     *
     * @return The current RoleAction, for chaining convenience
     */
    public RoleAction setColor(Color color)
    {
        return this.setColor(color != null ? color.getRGB() : null);
    }

    /**
     * Sets the Color for the new role.
     * This accepts colors from the range {@code 0x000} to {@code 0xFFFFFF}.
     * The provided value will be ranged using {@code rbg & 0xFFFFFF}
     *
     * @param  rgb
     *         The color for the new role in integer form, {@code null} to use default white/black
     *
     * @return The current RoleAction, for chaining convenience
     */
    public RoleAction setColor(Integer rgb)
    {
        this.color = rgb;
        return this;
    }

    /**
     * Sets the Permissions the new Role should have.
     * This will only allow permissions that the current account already holds unless
     * the account is owner or {@link net.dv8tion.jda.core.Permission#ADMINISTRATOR admin} of the parent {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *
     * @param  permissions
     *         The varargs {@link net.dv8tion.jda.core.Permission Permissions} for the new role
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If the currently logged in account does not hold one of the specified permissions
     * @throws IllegalArgumentException
     *         If any of the provided permissions is {@code null}
     *
     * @return The current RoleAction, for chaining convenience
     */
    public RoleAction setPermissions(Permission... permissions)
    {
        if (permissions != null)
        {
            for (Permission p : permissions)
            {
                Args.notNull(p, "Permissions");
                checkPermission(p);
            }
        }

        this.permissions = permissions == null ? 0 : Permission.getRaw(permissions);
        return this;
    }

    /**
     * Sets the Permissions the new Role should have.
     * This will only allow permissions that the current account already holds unless
     * the account is owner or {@link net.dv8tion.jda.core.Permission#ADMINISTRATOR admin} of the parent {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *
     * @param  permissions
     *         A {@link java.util.Collection Collection} of {@link net.dv8tion.jda.core.Permission Permissions} for the new role
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If the currently logged in account does not hold one of the specified permissions
     * @throws IllegalArgumentException
     *         If any of the provided permissions is {@code null}
     *
     * @return The current RoleAction, for chaining convenience
     */
    public RoleAction setPermissions(Collection<Permission> permissions)
    {
        if (permissions != null)
        {
            for (Permission p : permissions)
            {
                Args.notNull(p, "Permissions");
                checkPermission(p);
            }
        }

        this.permissions = permissions == null ? 0 : Permission.getRaw(permissions);
        return this;
    }

    /**
     * Sets the Permissions the new Role should have.
     * This will only allow permissions that the current account already holds unless
     * the account is owner or {@link net.dv8tion.jda.core.Permission#ADMINISTRATOR admin} of the parent {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *
     * @param  permissions
     *         The raw {@link net.dv8tion.jda.core.Permission Permissions} value for the new role.
     *         To retrieve this use {@link net.dv8tion.jda.core.Permission#getRawValue()}
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided permission value is invalid
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If the currently logged in account does not hold one of the specified permissions
     *
     * @return The current RoleAction, for chaining convenience
     *
     * @see    net.dv8tion.jda.core.Permission#getRawValue()
     * @see    net.dv8tion.jda.core.Permission#getRaw(java.util.Collection)
     * @see    net.dv8tion.jda.core.Permission#getRaw(net.dv8tion.jda.core.Permission...)
     */
    public RoleAction setPermissions(long permissions)
    {
        Args.notNegative(permissions, "Raw Permissions");
        Args.check(permissions <= Permission.ALL_PERMISSIONS, "Provided permissions may not be greater than a full permission set!");
        for (Permission p : Permission.getPermissions(permissions))
            checkPermission(p);
        this.permissions = permissions;
        return this;
    }

    @Override
    protected void finalizeData()
    {
        JSONObject object = new JSONObject();
        if (name != null)
            object.put("name", name);
        if (color != null)
            object.put("color", color & 0xFFFFFF);
        if (permissions > 0)
            object.put("permissions", permissions);
        if (hoisted != null)
            object.put("hoist", hoisted.booleanValue());
        if (mentionable != null)
            object.put("mentionable", mentionable.booleanValue());

        super.data = object;
        super.finalizeData();
    }

    @Override
    protected void handleResponse(Response response, Request request)
    {
        if (response.isOk())
            request.onSuccess(EntityBuilder.get(api).createRole(response.getObject(), guild.getId()));
        else
            request.onFailure(response);
    }

    private void checkPermission(Permission permission)
    {
        if (!guild.getSelfMember().hasPermission(permission))
            throw new PermissionException(permission);
    }
}
