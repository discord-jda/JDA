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

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.exceptions.HierarchyException;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.managers.RoleManager;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.core.requests.restaction.RoleAction;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.MiscUtil;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.awt.Color;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class RoleImpl implements Role
{
    private final long id;
    private final Guild guild;

    private final ReentrantLock mngLock = new ReentrantLock();
    private volatile RoleManager manager;

    private String name;
    private boolean managed;
    private boolean hoisted;
    private boolean mentionable;
    private long rawPermissions;
    private int color;
    private int rawPosition;

    public RoleImpl(long id, Guild guild)
    {
        this.id = id;
        this.guild = guild;
    }

    @Override
    public int getPosition()
    {
        if (this == guild.getPublicRole())
            return -1;

        //Subtract 1 to get into 0-index, and 1 to disregard the everyone role.
        int i = guild.getRoles().size() - 2;
        for (Role r : guild.getRoles())
        {
            if (r == this)
                return i;
            i--;
        }
        throw new AssertionError("Somehow when determining position we never found the role in the Guild's roles? wtf?");
    }

    @Override
    public int getPositionRaw()
    {
        return rawPosition;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public boolean isManaged()
    {
        return managed;
    }

    @Override
    public boolean isHoisted()
    {
        return hoisted;
    }

    @Override
    public boolean isMentionable()
    {
        return mentionable;
    }

    @Override
    public long getPermissionsRaw()
    {
        return rawPermissions;
    }

    @Override
    public List<Permission> getPermissions()
    {
        return Collections.unmodifiableList(
                Permission.getPermissions(rawPermissions));
    }

    @Override
    public Color getColor()
    {
        return color != Role.DEFAULT_COLOR_RAW ? new Color(color) : null;
    }

    @Override
    public int getColorRaw()
    {
        return color;
    }

    @Override
    public boolean isPublicRole()
    {
        return this.equals(this.getGuild().getPublicRole());
    }

    @Override
    public boolean hasPermission(Permission... permissions)
    {
        long effectivePerms = rawPermissions | guild.getPublicRole().getPermissionsRaw();
        for (Permission perm : permissions)
        {
            final long rawValue = perm.getRawValue();
            if ((effectivePerms & rawValue) != rawValue)
                return false;
        }
        return true;
    }

    @Override
    public boolean hasPermission(Collection<Permission> permissions)
    {
        Checks.notNull(permissions, "Permission Collection");

        return hasPermission(permissions.toArray(Permission.EMPTY_PERMISSIONS));
    }

    @Override
    public boolean hasPermission(Channel channel, Permission... permissions)
    {
        long effectivePerms = PermissionUtil.getEffectivePermission(channel, this);
        for (Permission perm : permissions)
        {
            final long rawValue = perm.getRawValue();
            if ((effectivePerms & rawValue) != rawValue)
                return false;
        }
        return true;
    }

    @Override
    public boolean hasPermission(Channel channel, Collection<Permission> permissions)
    {
        Checks.notNull(permissions, "Permission Collection");

        return hasPermission(channel, permissions.toArray(Permission.EMPTY_PERMISSIONS));
    }

    @Override
    public boolean canInteract(Role role)
    {
        return PermissionUtil.canInteract(this, role);
    }

    @Override
    public Guild getGuild()
    {
        return guild;
    }

    @Override
    public RoleAction createCopy(Guild guild)
    {
        Checks.notNull(guild, "Guild");
        return guild.getController().createRole()
                    .setColor(color)
                    .setHoisted(hoisted)
                    .setMentionable(mentionable)
                    .setName(name)
                    .setPermissions(rawPermissions);
    }

    @Override
    public RoleManager getManager()
    {
        RoleManager mng = manager;
        if (mng == null)
        {
            mng = MiscUtil.locked(mngLock, () ->
            {
                if (manager == null)
                    manager = new RoleManager(this);
                return manager;
            });
        }
        return mng;
    }

    @Override
    public AuditableRestAction<Void> delete()
    {
        if (!getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES))
            throw new InsufficientPermissionException(Permission.MANAGE_ROLES);
        if(!PermissionUtil.canInteract(getGuild().getSelfMember(), this))
            throw new HierarchyException("Can't delete role >= highest self-role");
        if (managed)
            throw new UnsupportedOperationException("Cannot delete a Role that is managed. ");

        Route.CompiledRoute route = Route.Roles.DELETE_ROLE.compile(guild.getId(), getId());
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
    public JDA getJDA()
    {
        return guild.getJDA();
    }

    @Override
    public String getAsMention()
    {
        return isPublicRole() ? "@everyone" : "<@&" + getId() + '>';
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof Role))
            return false;
        Role oRole = (Role) o;
        return this == oRole || this.getIdLong() == oRole.getIdLong();
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    @Override
    public String toString()
    {
        return "R:" + getName() + '(' + id + ')';
    }

    @Override
    public int compareTo(Role r)
    {
        if (this == r)
            return 0;

        if (!this.getGuild().equals(r.getGuild()))
            throw new IllegalArgumentException("Cannot compare roles that aren't from the same guild!");

        if (this.getPositionRaw() != r.getPositionRaw())
            return this.getPositionRaw() - r.getPositionRaw();

        OffsetDateTime thisTime = this.getCreationTime();
        OffsetDateTime rTime = r.getCreationTime();

        //We compare the provided role's time to this's time instead of the reverse as one would expect due to how
        // discord deals with hierarchy. The more recent a role was created, the lower its hierarchy ranking when
        // it shares the same position as another role.
        return rTime.compareTo(thisTime);
    }

    // -- Setters --

    public RoleImpl setName(String name)
    {
        this.name = name;
        return this;
    }

    public RoleImpl setColor(int color)
    {
        this.color = color;
        return this;
    }

    public RoleImpl setManaged(boolean managed)
    {
        this.managed = managed;
        return this;
    }

    public RoleImpl setHoisted(boolean hoisted)
    {
        this.hoisted = hoisted;
        return this;
    }

    public RoleImpl setMentionable(boolean mentionable)
    {
        this.mentionable = mentionable;
        return this;
    }

    public RoleImpl setRawPermissions(long rawPermissions)
    {
        this.rawPermissions = rawPermissions;
        return this;
    }

    public RoleImpl setRawPosition(int rawPosition)
    {
        this.rawPosition = rawPosition;
        return this;
    }
}
