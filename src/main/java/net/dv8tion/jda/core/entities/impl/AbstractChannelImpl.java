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

package net.dv8tion.jda.core.entities.impl;

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.managers.ChannelManager;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.core.requests.restaction.InviteAction;
import net.dv8tion.jda.core.requests.restaction.PermissionOverrideAction;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.MiscUtil;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public abstract class AbstractChannelImpl<T extends AbstractChannelImpl<T>> implements Channel
{
    protected final long id;
    protected final GuildImpl guild;

    protected final TLongObjectMap<PermissionOverride> overrides = MiscUtil.newLongMap();

    protected final ReentrantLock mngLock = new ReentrantLock();
    protected volatile ChannelManager manager;

    protected long parentId;
    protected String name;
    protected int rawPosition;

    public AbstractChannelImpl(long id, GuildImpl guild)
    {
        this.id = id;
        this.guild = guild;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public Guild getGuild()
    {
        return guild;
    }

    @Override
    public Category getParent()
    {
        return guild.getCategoriesMap().get(parentId);
    }

    @Override
    public int getPositionRaw()
    {
        return rawPosition;
    }

    @Override
    public JDA getJDA()
    {
        return getGuild().getJDA();
    }

    @Override
    public PermissionOverride getPermissionOverride(Member member)
    {
        return member != null ? overrides.get(member.getUser().getIdLong()) : null;
    }

    @Override
    public PermissionOverride getPermissionOverride(Role role)
    {
        return role != null ? overrides.get(role.getIdLong()) : null;
    }

    @Override
    public List<PermissionOverride> getPermissionOverrides()
    {
        // already unmodifiable!
        return Arrays.asList(overrides.values(new PermissionOverride[overrides.size()]));
    }

    @Override
    public List<PermissionOverride> getMemberPermissionOverrides()
    {
        return Collections.unmodifiableList(getPermissionOverrides().stream()
                .filter(PermissionOverride::isMemberOverride)
                .collect(Collectors.toList()));
    }

    @Override
    public List<PermissionOverride> getRolePermissionOverrides()
    {
        return Collections.unmodifiableList(getPermissionOverrides().stream()
                .filter(PermissionOverride::isRoleOverride)
                .collect(Collectors.toList()));
    }

    @Override
    public ChannelManager getManager()
    {
        ChannelManager mng = manager;
        if (mng == null)
        {
            mng = MiscUtil.locked(mngLock, () ->
            {
                if (manager == null)
                    manager = new ChannelManager(this);
                return manager;
            });
        }
        return mng;
    }

    @Override
    public AuditableRestAction<Void> delete()
    {
        checkPermission(Permission.MANAGE_CHANNEL);

        Route.CompiledRoute route = Route.Channels.DELETE_CHANNEL.compile(getId());
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

    @Override
    public PermissionOverrideAction createPermissionOverride(Member member)
    {
        Checks.notNull(member, "member");
        if (overrides.containsKey(member.getUser().getIdLong()))
            throw new IllegalStateException("Provided member already has a PermissionOverride in this channel!");

        return putPermissionOverride(member);
    }

    @Override
    public PermissionOverrideAction createPermissionOverride(Role role)
    {
        Checks.notNull(role, "role");
        if (overrides.containsKey(role.getIdLong()))
            throw new IllegalStateException("Provided role already has a PermissionOverride in this channel!");

        return putPermissionOverride(role);
    }

    @Override
    public PermissionOverrideAction putPermissionOverride(Member member)
    {
        checkPermission(Permission.MANAGE_PERMISSIONS);
        Checks.notNull(member, "member");

        if (!guild.equals(member.getGuild()))
            throw new IllegalArgumentException("Provided member is not from the same guild as this channel!");
        Route.CompiledRoute route = Route.Channels.CREATE_PERM_OVERRIDE.compile(getId(), member.getUser().getId());
        return new PermissionOverrideAction(getJDA(), route, this, member);
    }

    @Override
    public PermissionOverrideAction putPermissionOverride(Role role)
    {
        checkPermission(Permission.MANAGE_PERMISSIONS);
        Checks.notNull(role, "role");

        if (!guild.equals(role.getGuild()))
            throw new IllegalArgumentException("Provided role is not from the same guild as this channel!");
        Route.CompiledRoute route = Route.Channels.CREATE_PERM_OVERRIDE.compile(getId(), role.getId());
        return new PermissionOverrideAction(getJDA(), route, this, role);
    }

    @Override
    public InviteAction createInvite()
    {
        if (!this.guild.getSelfMember().hasPermission(this, Permission.CREATE_INSTANT_INVITE))
            throw new InsufficientPermissionException(Permission.CREATE_INSTANT_INVITE);

        return new InviteAction(this.getJDA(), this.getId());
    }

    @Override
    public RestAction<List<Invite>> getInvites()
    {
        if (!this.guild.getSelfMember().hasPermission(this, Permission.MANAGE_CHANNEL))
            throw new InsufficientPermissionException(Permission.MANAGE_CHANNEL);

        final Route.CompiledRoute route = Route.Invites.GET_CHANNEL_INVITES.compile(getId());

        return new RestAction<List<Invite>>(getJDA(), route)
        {
            @Override
            protected void handleResponse(final Response response, final Request<List<Invite>> request)
            {
                if (response.isOk())
                {
                    EntityBuilder entityBuilder = this.api.getEntityBuilder();
                    JSONArray array = response.getArray();
                    List<Invite> invites = new ArrayList<>(array.length());
                    for (int i = 0; i < array.length(); i++)
                        invites.add(entityBuilder.createInvite(array.getJSONObject(i)));
                    request.onSuccess(Collections.unmodifiableList(invites));
                }
                else
                {
                    request.onFailure(response);
                }
            }
        };
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof Channel))
            return false;
        if (obj == this)
            return true;
        Channel channel = (Channel) obj;
        return channel.getIdLong() == getIdLong();
    }

    public TLongObjectMap<PermissionOverride> getOverrideMap()
    {
        return overrides;
    }

    @SuppressWarnings("unchecked")
    public T setName(String name)
    {
        this.name = name;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setParent(long parentId)
    {
        this.parentId = parentId;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setPosition(int rawPosition)
    {
        this.rawPosition = rawPosition;
        return (T) this;
    }

    protected void checkPermission(Permission permission) {checkPermission(permission, null);}
    protected void checkPermission(Permission permission, String message)
    {
        if (!guild.getSelfMember().hasPermission(this, permission))
        {
            if (message != null)
                throw new InsufficientPermissionException(permission, message);
            else
                throw new InsufficientPermissionException(permission);
        }
    }
}
