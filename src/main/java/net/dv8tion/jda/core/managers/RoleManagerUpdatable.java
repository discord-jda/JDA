/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
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
import net.dv8tion.jda.core.utils.PermissionUtil;
import org.json.JSONObject;

import java.awt.*;

public class RoleManagerUpdatable
{
    protected final Role role;

    protected RoleField<String> name;
    protected RoleField<Color> color;
    protected RoleField<Boolean> hoisted;
    protected RoleField<Boolean> mentionable;
    protected PermissionField permissions;

    public RoleManagerUpdatable(Role role)
    {
        this.role = role;
        setupFields();
    }

    public JDA getJDA()
    {
        return role.getJDA();
    }

    public Guild getGuild()
    {
        return role.getGuild();
    }

    public Role getRole()
    {
        return role;
    }

    public RoleField<String> getNameField()
    {
        checkPermission(Permission.MANAGE_ROLES);
        checkPosition();

        return name;
    }


    public RoleField<Color> getColorField()
    {
        checkPermission(Permission.MANAGE_ROLES);
        checkPosition();

        return color;
    }

    public RoleField<Boolean> getHoistedField()
    {
        checkPermission(Permission.MANAGE_ROLES);
        checkPosition();

        return hoisted;
    }

    public RoleField<Boolean> getMentionableField()
    {
        checkPermission(Permission.MANAGE_ROLES);
        checkPosition();

        return mentionable;
    }


    public PermissionField getPermissionField()
    {
        checkPermission(Permission.MANAGE_ROLES);
        checkPosition();

        return permissions;
    }


    /**
     * Resets all queued updates. So the next call to {@link #update()} will change nothing.
     */
    public void reset() {
        name.reset();
        color.reset();
        hoisted.reset();
        mentionable.reset();
        permissions.reset();
    }

    /**
     * This method will apply all accumulated changes received by setters
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
            body.put("color", color.getValue() == null ? 0 : color.getValue().getRGB());
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

    protected void checkNull(Object obj, String name)
    {
        if (obj == null)
            throw new NullPointerException("Provided " + name + " was null!");
    }

    protected void checkPermission(Permission perm)
    {
        if (!PermissionUtil.checkPermission(getGuild(), getGuild().getSelfMember(), perm))
            throw new PermissionException(perm);
    }

    protected void checkPosition()
    {
        if(!PermissionUtil.canInteract(getGuild().getSelfMember(), role))
            throw new PermissionException("Can't modify role >= highest self-role");
    }

    protected void setupFields()
    {
        this.name = new RoleField<String>(this, role::getName)
        {
            @Override
            public void checkValue(String value)
            {
                checkNull(value, "name");
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
                checkNull(value, "hoisted Boolean");
            }
        };

        this.mentionable = new RoleField<Boolean>(this, role::isMentionable)
        {
            @Override
            public void checkValue(Boolean value)
            {
                checkNull(value, "mentionable Boolean");
            }
        };

        this.permissions = new PermissionField(this, role::getPermissionsRaw);
    }
}
