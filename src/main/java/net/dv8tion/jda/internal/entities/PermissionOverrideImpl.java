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

package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.PermOverrideManager;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.cache.UpstreamReference;

import java.util.EnumSet;
import java.util.concurrent.locks.ReentrantLock;

public class PermissionOverrideImpl implements PermissionOverride
{
    private final long id;
    private final UpstreamReference<GuildChannel> channel;
    private final IPermissionHolder permissionHolder;

    protected final ReentrantLock mngLock = new ReentrantLock();
    protected volatile PermOverrideManager manager;

    private long allow;
    private long deny;

    public PermissionOverrideImpl(GuildChannel channel, long id, IPermissionHolder permissionHolder)
    {
        this.channel = new UpstreamReference<>(channel);
        this.id = id;
        this.permissionHolder = permissionHolder;
    }

    @Override
    public long getAllowedRaw()
    {
        return allow;
    }

    @Override
    public long getInheritRaw()
    {
        return ~(allow | deny);
    }

    @Override
    public long getDeniedRaw()
    {
        return deny;
    }

    @Override
    public EnumSet<Permission> getAllowed()
    {
        return Permission.getPermissions(allow);
    }

    @Override
    public EnumSet<Permission> getInherit()
    {
        return Permission.getPermissions(getInheritRaw());
    }

    @Override
    public EnumSet<Permission> getDenied()
    {
        return Permission.getPermissions(deny);
    }

    @Override
    public JDA getJDA()
    {
        return getChannel().getJDA();
    }

    @Override
    public Member getMember()
    {
        return isMemberOverride() ? (Member) permissionHolder : null;
    }

    @Override
    public Role getRole()
    {
        return isRoleOverride() ? (Role) permissionHolder : null;
    }

    @Override
    public GuildChannel getChannel()
    {
        return channel.get();
    }

    @Override
    public Guild getGuild()
    {
        return getChannel().getGuild();
    }

    @Override
    public boolean isMemberOverride()
    {
        return permissionHolder instanceof Member;
    }

    @Override
    public boolean isRoleOverride()
    {
        return permissionHolder instanceof Role;
    }

    @Override
    public PermOverrideManager getManager()
    {
        PermOverrideManager mng = manager;
        if (mng == null)
        {
            mng = MiscUtil.locked(mngLock, () ->
            {
                if (manager == null)
                    manager = new PermOverrideManager(this);
                return manager;
            });
        }
        return mng;
    }

    @Override
    public AuditableRestAction<Void> delete()
    {
        if (!getGuild().getSelfMember().hasPermission(getChannel(), Permission.MANAGE_PERMISSIONS))
            throw new InsufficientPermissionException(Permission.MANAGE_PERMISSIONS);

        String targetId = isRoleOverride() ? getRole().getId() : getMember().getUser().getId();
        Route.CompiledRoute route = Route.Channels.DELETE_PERM_OVERRIDE.compile(getChannel().getId(), targetId);
        return new AuditableRestAction<Void>(getJDA(), route)
        {
            @Override
            protected void handleResponse(Response response, Request<Void> request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    public PermissionOverrideImpl setAllow(long allow)
    {
        this.allow = allow;
        return this;
    }

    public PermissionOverrideImpl setDeny(long deny)
    {
        this.deny = deny;
        return this;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof PermissionOverrideImpl))
            return false;
        PermissionOverrideImpl oPerm = (PermissionOverrideImpl) o;
        return this == oPerm
                || ((this.permissionHolder.equals(oPerm.permissionHolder)) && this.getChannel().equals(oPerm.getChannel()));
    }

    @Override
    public int hashCode()
    {
        return toString().hashCode();
    }

    @Override
    public String toString()
    {
        return "PermOver:(" + (isMemberOverride() ? "M" : "R") + ")(" + getChannel().getId() + " | " + id + ")";
    }

}
