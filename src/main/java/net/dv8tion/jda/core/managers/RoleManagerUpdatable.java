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
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.managers.fields.PermissionField;
import net.dv8tion.jda.core.managers.fields.RoleField;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import org.apache.http.util.Args;
import org.json.JSONObject;

import java.awt.Color;

/**
 * An {@link #update() updatable} manager that allows
 * to modify role settings like the {@link #getNameField() name} or the {@link #getColorField() color}.
 *
 * <p>This manager allows to modify multiple fields at once
 * by getting the {@link net.dv8tion.jda.core.managers.fields.RoleField RoleField} for specific
 * properties and setting or resetting their values; followed by a call of {@link #update()}!
 * <br>Default values depend on the inherited properties of the <u>Public Role</u> in the parent Guild.
 *
 * <p>The {@link net.dv8tion.jda.core.managers.RoleManager RoleManager} implementation
 * simplifies this process by giving simple setters that return the {@link #update() update} {@link net.dv8tion.jda.core.requests.RestAction RestAction}
 *
 * <p><b>Note</b>: To {@link #update() update} this manager
 * the currently logged in account requires the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES MANAGE_ROLES} and
 * must be more powerful according to Discord hierarchy rules (positional strength). [ee {@link Role#canInteract(net.dv8tion.jda.core.entities.Role) Role.canInteract(Role)}]
 */
public class RoleManagerUpdatable
{
    protected final Role role;

    protected RoleField<String> name;
    protected RoleField<Color> color;
    protected RoleField<Boolean> hoisted;
    protected RoleField<Boolean> mentionable;
    protected PermissionField permissions;

    /**
     * Creates a new RoleManagerUpdatable instance
     *
     * @param role
     *        The {@link net.dv8tion.jda.core.entities.Role Role} to manage
     */
    public RoleManagerUpdatable(Role role)
    {
        this.role = role;
        setupFields();
    }

    /**
     * The {@link net.dv8tion.jda.core.JDA JDA} instance of this Manager
     *
     * @return the corresponding JDA instance
     */
    public JDA getJDA()
    {
        return role.getJDA();
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.Guild Guild} this Manager's
     * {@link net.dv8tion.jda.core.entities.Role Role} is in.
     * <br>This is logically the same as calling {@code getRole().getGuild()}
     *
     * @return The parent {@link net.dv8tion.jda.core.entities.Guild Guild}
     */
    public Guild getGuild()
    {
        return role.getGuild();
    }

    /**
     * The target {@link net.dv8tion.jda.core.entities.Role Role} for this
     * manager
     *
     * @return The target Role
     */
    public Role getRole()
    {
        return role;
    }

    /**
     * An {@link net.dv8tion.jda.core.managers.fields.RoleField RoleField}
     * for the <b><u>name</u></b> of the selected {@link net.dv8tion.jda.core.entities.Role Role}.
     *
     * <p>To set the value use {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) setValue(String)}
     * on the returned {@link net.dv8tion.jda.core.managers.fields.RoleField RoleField} instance.
     *
     * <p>A role name <b>must</b> be between 1-32 characters long!
     * <br>Otherwise {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) Field.setValue(...)} will
     * throw an {@link IllegalArgumentException IllegalArgumentException}.
     *
     * @return {@link net.dv8tion.jda.core.managers.fields.RoleField RoleField} - Type: {@code String}
     */
    public RoleField<String> getNameField()
    {
        return name;
    }

    /**
     * An {@link net.dv8tion.jda.core.managers.fields.RoleField RoleField}
     * for the <b><u>color</u></b> of the selected {@link net.dv8tion.jda.core.entities.Role Role}.
     *
     * <p>To set the value use {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) setValue(Color)}
     * on the returned {@link net.dv8tion.jda.core.managers.fields.RoleField RoleField} instance.
     * <br>Provide {@code null} or {@link Color#BLACK black} to use the default color.
     *
     * @return {@link net.dv8tion.jda.core.managers.fields.RoleField RoleField} - Type: {@link java.awt.Color Color}
     */
    public RoleField<Color> getColorField()
    {
        return color;
    }

    /**
     * An {@link net.dv8tion.jda.core.managers.fields.RoleField RoleField}
     * for the <b><u>hoist state</u></b> of the selected {@link net.dv8tion.jda.core.entities.Role Role}.
     *
     * <p>To set the value use {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) setValue(Boolean)}
     * on the returned {@link net.dv8tion.jda.core.managers.fields.RoleField RoleField} instance.
     *
     * <p>A role hoist state <b>must not</b> be {@code null}!
     * <br>Otherwise {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) Field.setValue(...)} will
     * throw an {@link IllegalArgumentException IllegalArgumentException}.
     *
     * @return {@link net.dv8tion.jda.core.managers.fields.RoleField RoleField} - Type: {@code Boolean}
     */
    public RoleField<Boolean> getHoistedField()
    {
        return hoisted;
    }

    /**
     * An {@link net.dv8tion.jda.core.managers.fields.RoleField RoleField}
     * for the <b><u>mentionable state</u></b> of the selected {@link net.dv8tion.jda.core.entities.Role Role}.
     *
     * <p>To set the value use {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) setValue(Boolean)}
     * on the returned {@link net.dv8tion.jda.core.managers.fields.RoleField RoleField} instance.
     *
     * <p>A role mentionable state <b>must not</b> be {@code null}!
     * <br>Otherwise {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) Field.setValue(...)} will
     * throw an {@link IllegalArgumentException IllegalArgumentException}.
     *
     * @return {@link net.dv8tion.jda.core.managers.fields.RoleField RoleField} - Type: {@code Boolean}
     */
    public RoleField<Boolean> getMentionableField()
    {
        return mentionable;
    }

    /**
     * An {@link net.dv8tion.jda.core.managers.fields.PermissionField PermissionField}
     * for the {@link net.dv8tion.jda.core.Permission Permissions} of the selected {@link net.dv8tion.jda.core.entities.Role Role}.
     *
     * <p>To set the value use {@link net.dv8tion.jda.core.managers.fields.PermissionField#setPermissions(Permission...) setPermissions(Permission...)}
     * on the returned {@link net.dv8tion.jda.core.managers.fields.PermissionField PermissionField} instance.
     *
     * <p>A role permissions <b>must not</b> be {@code null}!
     * <br>Otherwise the {@link net.dv8tion.jda.core.managers.fields.PermissionField PermissionField} will
     * throw an {@link IllegalArgumentException IllegalArgumentException}.
     *
     * @return {@link net.dv8tion.jda.core.managers.fields.PermissionField PermissionField}
     */
    public PermissionField getPermissionField()
    {
        return permissions;
    }

    /**
     * Resets all {@link net.dv8tion.jda.core.managers.fields.Field Fields}
     * for this manager instance by calling {@link net.dv8tion.jda.core.managers.fields.Field#reset() Field.reset()} sequentially
     * <br>This is automatically called by {@link #update()}
     */
    public void reset() {
        name.reset();
        color.reset();
        hoisted.reset();
        mentionable.reset();
        permissions.reset();
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
     *      <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_ROLE UNKNOWN_ROLE}
     *      <br>If the Role was deleted before finishing the task</li>
     *
     *      <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *      <br>If the currently logged in account was removed from the Guild before finishing the task</li>
     *
     *      <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *      <br>If the currently logged in account loses the {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES MANAGE_ROLES Permission} or lost
     *          positional power before finishing the task</li>
     * </ul>
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES MANAGE_ROLES}
     *         or does not have the power to {@link Role#canInteract(net.dv8tion.jda.core.entities.Role) interact} with this Role
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction}
     *         <br>Applies all changes that have been made in a single api-call.
     */
    public RestAction<Void> update()
    {
        checkPermission(Permission.MANAGE_ROLES);
        checkPosition();

        if (!needsUpdate())
            return new RestAction.EmptyRestAction<>(null);

        //TODO: check if all of this is *actually* needed.
        JSONObject body = new JSONObject().put("name", role.getName());

        if(name.shouldUpdate())
            body.put("name", name.getValue());
        if(color.shouldUpdate())
            body.put("color", color.getValue() == null ? 0 : color.getValue().getRGB() & 0xFFFFFF);
        if(hoisted.shouldUpdate())
            body.put("hoist", hoisted.getValue().booleanValue());
        if(mentionable.shouldUpdate())
            body.put("mentionable", mentionable.getValue().booleanValue());
        if (permissions.shouldUpdate())
            body.put("permissions", permissions.getValue());

        reset();
        Route.CompiledRoute route = Route.Roles.MODIFY_ROLE.compile(getGuild().getId(), role.getId());
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

    protected boolean needsUpdate()
    {
        return name.shouldUpdate()
                || color.shouldUpdate()
                || hoisted.shouldUpdate()
                || mentionable.shouldUpdate()
                || permissions.shouldUpdate();
    }

    protected void checkPermission(Permission perm)
    {
        if (!getGuild().getSelfMember().hasPermission(perm))
            throw new PermissionException(perm);
    }

    protected void checkPosition()
    {
        if(!getGuild().getSelfMember().canInteract(role))
            throw new PermissionException("Can't modify role >= highest self-role");
    }

    protected void setupFields()
    {
        this.name = new RoleField<String>(this, role::getName)
        {
            @Override
            public void checkValue(String value)
            {
                Args.notNull(value, "name");
                if (value.isEmpty() || value.length() > 32)
                    throw new IllegalArgumentException("Provided role name must be 1 to 32 characters in length");
            }
        };

        this.color = new RoleField<Color>(this, role::getColor)
        {
            @Override
            public RoleManagerUpdatable setValue(Color color)
            {
                if (color != null && color.getRGB() == 0)
                    color = null;

                super.setValue(color);
                return manager;
            }

            @Override
            public void checkValue(Color value) {}
        };

        this.hoisted = new RoleField<Boolean>(this, role::isHoisted)
        {
            @Override
            public void checkValue(Boolean value)
            {
                Args.notNull(value, "hoisted Boolean");
            }
        };

        this.mentionable = new RoleField<Boolean>(this, role::isMentionable)
        {
            @Override
            public void checkValue(Boolean value)
            {
                Args.notNull(value, "mentionable Boolean");
            }
        };

        this.permissions = new PermissionField(this, role::getPermissionsRaw);
    }
}
