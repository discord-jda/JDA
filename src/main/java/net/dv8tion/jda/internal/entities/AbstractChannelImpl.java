/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.internal.entities;

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.ChannelManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.requests.restaction.InviteAction;
import net.dv8tion.jda.api.requests.restaction.PermissionOverrideAction;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.managers.ChannelManagerImpl;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.requests.restaction.AuditableRestActionImpl;
import net.dv8tion.jda.internal.requests.restaction.InviteActionImpl;
import net.dv8tion.jda.internal.requests.restaction.PermissionOverrideActionImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.cache.SnowflakeReference;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public abstract class AbstractChannelImpl<T extends GuildChannel, M extends AbstractChannelImpl<T, M>> implements GuildChannel
{
    protected final long id;
    protected final SnowflakeReference<Guild> guild;
    protected final JDAImpl api;

    protected final TLongObjectMap<PermissionOverride> overrides = MiscUtil.newLongMap();

    protected final ReentrantLock mngLock = new ReentrantLock();
    protected volatile ChannelManager manager;

    protected long parentId;
    protected String name;
    protected int rawPosition;

    public AbstractChannelImpl(long id, GuildImpl guild)
    {
        this.id = id;
        this.api = guild.getJDA();
        this.guild = new SnowflakeReference<>(guild, api::getGuildById);
    }

    @Override
    public int compareTo(@Nonnull GuildChannel o)
    {
        Checks.notNull(o, "Channel");
        if (getType().getSortBucket() != o.getType().getSortBucket()) // if bucket matters
            return Integer.compare(getType().getSortBucket(), o.getType().getSortBucket());
        if (getPositionRaw() != o.getPositionRaw())                   // if position matters
            return Integer.compare(getPositionRaw(), o.getPositionRaw());
        return Long.compareUnsigned(id, o.getIdLong());               // last resort by id
    }

    @Nonnull
    @Override
    public abstract ChannelAction<T> createCopy(@Nonnull Guild guild);

    @Nonnull
    @Override
    public ChannelAction<T> createCopy()
    {
        return createCopy(getGuild());
    }

    @Nonnull
    @Override
    public String getName()
    {
        return name;
    }

    @Nonnull
    @Override
    public GuildImpl getGuild()
    {
        return (GuildImpl) guild.resolve();
    }

    @Override
    public Category getParent()
    {
        return getGuild().getCategoriesView().get(parentId);
    }

    @Override
    public int getPositionRaw()
    {
        return rawPosition;
    }

    @Nonnull
    @Override
    public JDA getJDA()
    {
        return api;
    }

    @Override
    public PermissionOverride getPermissionOverride(@Nonnull IPermissionHolder permissionHolder)
    {
        Checks.notNull(permissionHolder, "Permission Holder");
        Checks.check(permissionHolder.getGuild().equals(getGuild()), "Provided permission holder is not from the same guild as this channel!");
        return overrides.get(permissionHolder.getIdLong());
    }

    @Nonnull
    @Override
    public List<PermissionOverride> getPermissionOverrides()
    {
        return Arrays.asList(overrides.values(new PermissionOverride[overrides.size()]));
    }

    @Nonnull
    @Override
    public List<PermissionOverride> getMemberPermissionOverrides()
    {
        return Collections.unmodifiableList(getPermissionOverrides().stream()
                .filter(PermissionOverride::isMemberOverride)
                .collect(Collectors.toList()));
    }

    @Nonnull
    @Override
    public List<PermissionOverride> getRolePermissionOverrides()
    {
        return Collections.unmodifiableList(getPermissionOverrides().stream()
                .filter(PermissionOverride::isRoleOverride)
                .collect(Collectors.toList()));
    }

    @Nonnull
    @Override
    public ChannelManager getManager()
    {
        ChannelManager mng = manager;
        if (mng == null)
        {
            mng = MiscUtil.locked(mngLock, () ->
            {
                if (manager == null)
                    manager = new ChannelManagerImpl(this);
                return manager;
            });
        }
        return mng;
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> delete()
    {
        checkPermission(Permission.MANAGE_CHANNEL);

        Route.CompiledRoute route = Route.Channels.DELETE_CHANNEL.compile(getId());
        return new AuditableRestActionImpl<>(getJDA(), route);
    }

    @Nonnull
    @Override
    public PermissionOverrideAction createPermissionOverride(@Nonnull IPermissionHolder permissionHolder)
    {
        Checks.notNull(permissionHolder, "PermissionHolder");
        if (getPermissionOverride(permissionHolder) != null)
            throw new IllegalStateException("Provided member already has a PermissionOverride in this channel!");

        return putPermissionOverride(permissionHolder);
    }

    @Nonnull
    @Override
    public PermissionOverrideAction putPermissionOverride(@Nonnull IPermissionHolder permissionHolder)
    {
        checkPermission(Permission.MANAGE_PERMISSIONS);
        Checks.notNull(permissionHolder, "PermissionHolder");
        Checks.check(permissionHolder.getGuild().equals(getGuild()), "Provided permission holder is not from the same guild as this channel!");
        return new PermissionOverrideActionImpl(getJDA(), this, permissionHolder);
    }

    @Nonnull
    @Override
    public InviteAction createInvite()
    {
        if (!this.getGuild().getSelfMember().hasPermission(this, Permission.CREATE_INSTANT_INVITE))
            throw new InsufficientPermissionException(this, Permission.CREATE_INSTANT_INVITE);

        return new InviteActionImpl(this.getJDA(), this.getId());
    }

    @Nonnull
    @Override
    public RestAction<List<Invite>> retrieveInvites()
    {
        if (!this.getGuild().getSelfMember().hasPermission(this, Permission.MANAGE_CHANNEL))
            throw new InsufficientPermissionException(this, Permission.MANAGE_CHANNEL);

        final Route.CompiledRoute route = Route.Invites.GET_CHANNEL_INVITES.compile(getId());

        JDAImpl jda = (JDAImpl) getJDA();
        return new RestActionImpl<>(jda, route, (response, request) ->
        {
            EntityBuilder entityBuilder = jda.getEntityBuilder();
            DataArray array = response.getArray();
            List<Invite> invites = new ArrayList<>(array.length());
            for (int i = 0; i < array.length(); i++)
                invites.add(entityBuilder.createInvite(array.getObject(i)));
            return Collections.unmodifiableList(invites);
        });
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
        if (obj == this)
            return true;
        if (!(obj instanceof GuildChannel))
            return false;
        GuildChannel channel = (GuildChannel) obj;
        return channel.getIdLong() == getIdLong();
    }

    public TLongObjectMap<PermissionOverride> getOverrideMap()
    {
        return overrides;
    }

    @SuppressWarnings("unchecked")
    public M setName(String name)
    {
        this.name = name;
        return (M) this;
    }

    @SuppressWarnings("unchecked")
    public M setParent(long parentId)
    {
        this.parentId = parentId;
        return (M) this;
    }

    @SuppressWarnings("unchecked")
    public M setPosition(int rawPosition)
    {
        this.rawPosition = rawPosition;
        return (M) this;
    }

    protected void checkPermission(Permission permission) {checkPermission(permission, null);}
    protected void checkPermission(Permission permission, String message)
    {
        if (!getGuild().getSelfMember().hasPermission(this, permission))
        {
            if (message != null)
                throw new InsufficientPermissionException(this, permission, message);
            else
                throw new InsufficientPermissionException(this, permission);
        }
    }
}
