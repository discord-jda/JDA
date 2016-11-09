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
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.PermissionOverride;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.utils.PermissionUtil;
import org.apache.http.util.Args;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collection;

public class PermOverrideManagerUpdatable
{
    protected final PermissionOverride override;

    protected Long allow;
    protected Long deny;
    protected boolean set;

    public PermOverrideManagerUpdatable(PermissionOverride override)
    {
        this.override = override;
    }

    public JDA getJDA()
    {
        return override.getJDA();
    }

    public Guild getGuild()
    {
        return override.getGuild();
    }

    public Channel getChannel()
    {
        return override.getChannel();
    }

    public PermissionOverride getPermissionOverride()
    {
        return override;
    }

    public PermOverrideManagerUpdatable grant(long permissions)
    {
        return grant(Permission.getPermissions(permissions));
    }

    public PermOverrideManagerUpdatable grant(Permission... permissions)
    {
        return grant(Arrays.asList(permissions));
    }

    public PermOverrideManagerUpdatable grant(Collection<Permission> permissions)
    {
        Args.notNull(permissions, "Permission Collection");
        permissions.forEach(perm ->
        {
            Args.notNull(perm, "Permission in Permission Collection");
            checkPermission(perm);
        });
        setupValues();

        long allowBits = Permission.getRaw(permissions);
        allow |= allowBits;
        deny &= ~allowBits;

        return this;
    }

    public PermOverrideManagerUpdatable deny(long permissions)
    {
        return deny(Permission.getPermissions(permissions));
    }

    public PermOverrideManagerUpdatable deny(Permission... permissions)
    {
        return deny(Arrays.asList(permissions));
    }

    public PermOverrideManagerUpdatable deny(Collection<Permission> permissions)
    {
        Args.notNull(permissions, "Permission Collection");
        permissions.forEach(perm ->
        {
            Args.notNull(perm, "Permission in Permission Collection");
            checkPermission(perm);
        });
        setupValues();

        System.out.println("perms: " + permissions);
        long denyBits = Permission.getRaw(permissions);
        System.out.println("deny bits: " + denyBits);
        allow &= ~denyBits;
        deny |= denyBits;

        return this;
    }

    public PermOverrideManagerUpdatable clear(long permission)
    {
        return clear(Permission.getPermissions(permission));
    }

    public PermOverrideManagerUpdatable clear(Permission... permissions)
    {
        return clear(Arrays.asList(permissions));
    }

    public PermOverrideManagerUpdatable clear(Collection<Permission> permissions)
    {
        Args.notNull(permissions, "Permission Collection");
        permissions.forEach(perm ->
        {
            Args.notNull(perm, "Permission in Permission Collection");
            checkPermission(perm);
        });
        setupValues();

        long clearBits = Permission.getRaw(permissions);
        allow &= ~clearBits;
        deny &= ~clearBits;

        return this;
    }

    public Long getAllowBits()
    {
        return allow;
    }

    public Long getDenyBits()
    {
        return deny;
    }

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

    public boolean isSet()
    {
        return set;
    }

    public boolean shouldUpdate()
    {
        System.out.println("set: " + set);
        System.out.println("allow: " + allow + "   : raw: " + override.getAllowedRaw());
        System.out.println("deny: " + deny + "    : raw: " + override.getDeniedRaw());
        return set && (allow != override.getAllowedRaw() || deny != override.getDeniedRaw());
    }

    public void reset()
    {
        set = false;
        allow = null;
        deny = null;
    }

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
                System.out.println(response.getString());
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
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
