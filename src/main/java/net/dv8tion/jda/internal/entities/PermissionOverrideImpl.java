/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.exceptions.MissingAccessException;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.PermissionOverrideAction;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.requests.restaction.AuditableRestActionImpl;
import net.dv8tion.jda.internal.requests.restaction.PermissionOverrideActionImpl;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.Objects;

public class PermissionOverrideImpl implements PermissionOverride
{
    private final long id;
    private final boolean isRole;
    private final JDAImpl api;
    private IPermissionContainer channel;

    protected PermissionOverrideAction manager;

    private long allow;
    private long deny;

    public PermissionOverrideImpl(IPermissionContainer channel, long id, boolean isRole)
    {
        this.isRole = isRole;
        this.api = (JDAImpl) channel.getJDA();
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

    @Nonnull
    @Override
    public EnumSet<Permission> getAllowed()
    {
        return Permission.getPermissions(allow);
    }

    @Nonnull
    @Override
    public EnumSet<Permission> getInherit()
    {
        return Permission.getPermissions(getInheritRaw());
    }

    @Nonnull
    @Override
    public EnumSet<Permission> getDenied()
    {
        return Permission.getPermissions(deny);
    }

    @Nonnull
    @Override
    public JDA getJDA()
    {
        return api;
    }

    @Override
    public IPermissionHolder getPermissionHolder()
    {
        return isRole ? getRole() : getMember();
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

    @Nonnull
    @Override
    public IPermissionContainer getChannel()
    {
        IPermissionContainer realChannel = (IPermissionContainer) api.getGuildChannelById(channel.getIdLong());
        if (realChannel != null)
            channel = realChannel;
        return channel;
    }

    @Nonnull
    @Override
    public Guild getGuild()
    {
        return getChannel().getGuild();
    }

    @Override
    public boolean isMemberOverride()
    {
        return !isRole;
    }

    @Override
    public boolean isRoleOverride()
    {
        return isRole;
    }

    @Nonnull
    @Override
    public PermissionOverrideAction getManager()
    {
        checkPermissions();

        if (manager == null)
            return manager = new PermissionOverrideActionImpl(this).setOverride(false);
        return manager;
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> delete()
    {
        checkPermissions();

        Route.CompiledRoute route = Route.Channels.DELETE_PERM_OVERRIDE.compile(this.channel.getId(), getId());
        return new AuditableRestActionImpl<>(getJDA(), route);
    }

    @Override
    public long getIdLong()
    {
        return id;
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
        if (o == this)
            return true;
        if (!(o instanceof PermissionOverrideImpl))
            return false;
        PermissionOverrideImpl oPerm = (PermissionOverrideImpl) o;
        return id == oPerm.id && this.channel.getIdLong() == oPerm.channel.getIdLong();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, channel.getIdLong());
    }

    @Override
    public String toString()
    {
        return "PermOver:(" + (isMemberOverride() ? "M" : "R") + ")(" + channel.getId() + " | " + getId() + ")";
    }

    private void checkPermissions()
    {
        Member selfMember = getGuild().getSelfMember();
        IPermissionContainer channel = getChannel();
        if (!selfMember.hasPermission(channel, Permission.VIEW_CHANNEL))
            throw new MissingAccessException(channel, Permission.VIEW_CHANNEL);
        if (!selfMember.hasAccess(channel))
            throw new MissingAccessException(channel, Permission.VOICE_CONNECT);
        if (!selfMember.hasPermission(channel, Permission.MANAGE_PERMISSIONS))
            throw new InsufficientPermissionException(channel, Permission.MANAGE_PERMISSIONS);
    }
}
