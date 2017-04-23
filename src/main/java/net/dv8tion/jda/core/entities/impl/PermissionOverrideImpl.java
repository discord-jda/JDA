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

package net.dv8tion.jda.core.entities.impl;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.managers.PermOverrideManager;
import net.dv8tion.jda.core.managers.PermOverrideManagerUpdatable;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

public class PermissionOverrideImpl implements PermissionOverride
{
    private final long id;
    private final Channel channel;

    protected final Object mngLock = new Object();
    protected volatile PermOverrideManager manager;
    protected volatile PermOverrideManagerUpdatable managerUpdatable;

    private long allow;
    private long deny;

    public PermissionOverrideImpl(Channel channel, long id)
    {
        this.channel = channel;
        this.id = id;
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
    public List<Permission> getAllowed()
    {
        return Collections.unmodifiableList(Permission.getPermissions(allow));
    }

    @Override
    public List<Permission> getInherit()
    {
        return Collections.unmodifiableList(Permission.getPermissions(getInheritRaw()));
    }

    @Override
    public List<Permission> getDenied()
    {
        return Collections.unmodifiableList(Permission.getPermissions(deny));
    }

    @Override
    public JDA getJDA()
    {
        return channel.getJDA();
    }

    @Override
    public Member getMember()
    {
        return getGuild().getMemberById(id);
    }

    @Override
    public Role getRole()
    {
        return getGuild().getRoleById(id);
    }

    @Override
    public Channel getChannel()
    {
        return channel;
    }

    @Override
    public Guild getGuild()
    {
        return channel.getGuild();
    }

    @Override
    public boolean isMemberOverride()
    {
        return getMember() != null;
    }

    @Override
    public boolean isRoleOverride()
    {
        return getRole() != null;
    }

    @Override
    public PermOverrideManager getManager()
    {
        PermOverrideManager mng = manager;
        if (mng == null)
        {
            synchronized (mngLock)
            {
                mng = manager;
                if (mng == null)
                    mng = manager = new PermOverrideManager(this);
            }
        }
        return mng;
    }

    @Override
    public PermOverrideManagerUpdatable getManagerUpdatable()
    {
        PermOverrideManagerUpdatable mng = managerUpdatable;
        if (mng == null)
        {
            synchronized (mngLock)
            {
                mng = managerUpdatable;
                if (mng == null)
                    mng = managerUpdatable = new PermOverrideManagerUpdatable(this);
            }
        }
        return mng;
    }

    @Override
    public RestAction<Void> delete()
    {
        if (!channel.getGuild().getSelfMember().hasPermission(channel, Permission.MANAGE_PERMISSIONS))
            throw new PermissionException(Permission.MANAGE_PERMISSIONS);

        String targetId = isRoleOverride() ? getRole().getId() : getMember().getUser().getId();
        Route.CompiledRoute route = Route.Channels.DELETE_PERM_OVERRIDE.compile(channel.getId(), targetId);
        return new RestAction<Void>(getJDA(), route, null)
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
        if (!(o instanceof PermissionOverride))
            return false;
        PermissionOverride oPerm = (PermissionOverride) o;
        return this == oPerm || ((this.getMember() == null ? oPerm.getMember() == null : this.getMember().equals(oPerm.getMember()))
                && this.channel.equals(oPerm.getChannel()) && (this.getRole() == null ? oPerm.getRole() == null : this.getRole().equals(oPerm.getRole())));
    }

    @Override
    public int hashCode()
    {
        return (channel.getId() + getId()).hashCode();
    }

    @Override
    public String toString()
    {
        return "PermOver:(" + (isMemberOverride() ? "M" : "R") + ")(" + channel.getId() + " | " + getId() + ")";
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @Override
    public OffsetDateTime getCreationTime()
    {
        throw new UnsupportedOperationException("Unable to get creation time for a PermissionOverride!");
    }
}
