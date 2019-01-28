/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.api.entities.proxy;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.ProxyResolutionException;
import net.dv8tion.jda.api.managers.PermOverrideManager;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;

import java.util.EnumSet;

public class PermissionOverrideProxy implements PermissionOverride, ProxyEntity
{
    private final GuildChannelProxy channel;
    private final long id;

    public PermissionOverrideProxy(PermissionOverride override)
    {
        this.channel = override.getChannel().getProxy();
        this.id = override.isMemberOverride() ? override.getMember().getUser().getIdLong() : override.getRole().getIdLong();
    }

    @Override
    public PermissionOverride getSubject()
    {
        GuildChannel channel = getChannel();
        PermissionOverride override = channel.getPermissionOverrideById(id);
        if (override == null)
            throw new ProxyResolutionException("PermissionOverride(" + Long.toUnsignedString(id) + ")");
        return override;
    }

    @Override
    public PermissionOverrideProxy getProxy()
    {
        return this;
    }

    @Override
    public long getAllowedRaw()
    {
        return getSubject().getAllowedRaw();
    }

    @Override
    public long getInheritRaw()
    {
        return getSubject().getInheritRaw();
    }

    @Override
    public long getDeniedRaw()
    {
        return getSubject().getDeniedRaw();
    }

    @Override
    public EnumSet<Permission> getAllowed()
    {
        return getSubject().getAllowed();
    }

    @Override
    public EnumSet<Permission> getInherit()
    {
        return getSubject().getInherit();
    }

    @Override
    public EnumSet<Permission> getDenied()
    {
        return getSubject().getDenied();
    }

    @Override
    public JDA getJDA()
    {
        return getChannel().getJDA();
    }

    @Override
    public Member getMember()
    {
        return getSubject().getMember();
    }

    @Override
    public Role getRole()
    {
        return getSubject().getRole();
    }

    @Override
    public GuildChannel getChannel()
    {
        return channel.getSubject();
    }

    @Override
    public Guild getGuild()
    {
        return getChannel().getGuild();
    }

    @Override
    public boolean isMemberOverride()
    {
        return getSubject().isMemberOverride();
    }

    @Override
    public boolean isRoleOverride()
    {
        return getSubject().isRoleOverride();
    }

    @Override
    public PermOverrideManager getManager()
    {
        return getSubject().getManager();
    }

    @Override
    public AuditableRestAction<Void> delete()
    {
        return getSubject().delete();
    }

    @Override
    public int hashCode()
    {
        return getSubject().hashCode();
    }

    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object obj)
    {
        return obj == this || getSubject().equals(obj);
    }

    @Override
    public String toString()
    {
        return getSubject().toString();
    }
}
