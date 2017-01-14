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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.core.requests.restaction;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.EntityBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import org.apache.http.util.Args;
import org.json.JSONObject;

import java.awt.Color;
import java.util.Collection;

/**
 * @since  3.0
 * @author Florian Spieß
 */
public class RoleAction extends RestAction<Role> //todo documentation when rolled out
{

    protected final Guild guild;
    protected long permissions = 0;
    protected String name = null;
    protected Integer color = null;
    protected Boolean hoisted = null;
    protected Boolean mentionable = null;

    public RoleAction(JDA api, Route.CompiledRoute route, Guild guild)
    {
        super(api, route, null);
        this.guild = guild;
    }

    public RoleAction setName(String name)
    {
        this.name = name;
        return this;
    }

    public RoleAction setHoisted(Boolean hoisted)
    {
        this.hoisted = hoisted;
        return this;
    }

    public RoleAction setMentionable(Boolean mentionable)
    {
        this.mentionable = mentionable;
        return this;
    }

    public RoleAction setColor(Color color)
    {
        return this.setColor(color != null ? color.getRGB() & 0xFFFFFF : null);
    }

    public RoleAction setColor(Integer rgb)
    {
        this.color = rgb;
        return this;
    }

    public RoleAction setPermissions(Permission... permissions)
    {
        this.permissions = permissions == null ? 0 : Permission.getRaw(permissions);
        return this;
    }

    public RoleAction setPermissions(Collection<Permission> permissions)
    {
        this.permissions = permissions == null ? 0 : Permission.getRaw(permissions);
        return this;
    }

    public RoleAction setPermissions(long permissions)
    {
        Args.notNegative(permissions, "Raw Permissions");
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
            object.put("color", color.intValue());
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
}
