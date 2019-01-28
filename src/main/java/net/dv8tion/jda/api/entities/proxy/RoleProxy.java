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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.exceptions.ProxyResolutionException;
import net.dv8tion.jda.api.managers.RoleManager;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.RoleAction;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.Collection;
import java.util.EnumSet;

public class RoleProxy implements Role, ProxyEntity
{
    private final GuildProxy guild;
    private final long id;

    public RoleProxy(Role role)
    {
        this.guild = role.getGuild().getProxy();
        this.id = role.getIdLong();
    }

    @Override
    public Role getSubject()
    {
        Role role = getGuild().getRoleById(id);
        if (role == null)
            throw new ProxyResolutionException("Role(" + getId() + ")");
        return role;
    }

    @Override
    public RoleProxy getProxy()
    {
        return this;
    }

    @Override
    public int getPosition()
    {
        return getSubject().getPosition();
    }

    @Override
    public int getPositionRaw()
    {
        return getSubject().getPositionRaw();
    }

    @Override
    public String getName()
    {
        return getSubject().getName();
    }

    @Override
    public boolean isManaged()
    {
        return getSubject().isManaged();
    }

    @Override
    public boolean isHoisted()
    {
        return getSubject().isHoisted();
    }

    @Override
    public boolean isMentionable()
    {
        return getSubject().isMentionable();
    }

    @Override
    public long getPermissionsRaw()
    {
        return getSubject().getPermissionsRaw();
    }

    @Override
    public Color getColor()
    {
        return getSubject().getColor();
    }

    @Override
    public int getColorRaw()
    {
        return getSubject().getColorRaw();
    }

    @Override
    public boolean isPublicRole()
    {
        return getSubject().isPublicRole();
    }

    @Override
    public boolean canInteract(Role role)
    {
        return getSubject().canInteract(role);
    }

    @Override
    public Guild getGuild()
    {
        return guild.getSubject();
    }

    @Override
    public EnumSet<Permission> getPermissions()
    {
        return getSubject().getPermissions();
    }

    @Override
    public boolean hasPermission(Permission... permissions)
    {
        return getSubject().hasPermission(permissions);
    }

    @Override
    public boolean hasPermission(Collection<Permission> permissions)
    {
        return getSubject().hasPermission(permissions);
    }

    @Override
    public boolean hasPermission(GuildChannel channel, Permission... permissions)
    {
        return getSubject().hasPermission(channel, permissions);
    }

    @Override
    public boolean hasPermission(GuildChannel channel, Collection<Permission> permissions)
    {
        return getSubject().hasPermission(channel, permissions);
    }

    @Override
    public RoleAction createCopy(Guild guild)
    {
        return getSubject().createCopy(guild);
    }

    @Override
    public RoleManager getManager()
    {
        return getSubject().getManager();
    }

    @Override
    public AuditableRestAction<Void> delete()
    {
        return getSubject().delete();
    }

    @Override
    public JDA getJDA()
    {
        return getGuild().getJDA();
    }

    @Override
    public int compareTo(@NotNull Role o)
    {
        return getSubject().compareTo(o);
    }

    @Override
    public String getAsMention()
    {
        return getSubject().getAsMention();
    }

    @Override
    public long getIdLong()
    {
        return id;
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
