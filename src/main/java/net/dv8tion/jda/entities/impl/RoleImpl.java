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
package net.dv8tion.jda.entities.impl;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.managers.RoleManager;
import net.dv8tion.jda.utils.MiscUtil;

import java.time.OffsetDateTime;
import java.util.List;

public class RoleImpl implements net.dv8tion.jda.entities.Role
{
    private final String id;
    private final Guild guild;
    private String name;
    private int color;
    private int position;
    private int permissions;
    private boolean managed, grouped, mentionable;
    private RoleManager manager = null;

    public RoleImpl(String id, Guild guild)
    {
        this.id = id;
        this.guild = guild;
    }

    @Override
    public Guild getGuild()
    {
        return guild;
    }

    @Override
    public JDA getJDA()
    {
        return guild.getJDA();
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public int getPermissionsRaw()
    {
        return permissions;
    }

    @Override
    public List<Permission> getPermissions()
    {
        return Permission.getPermissions(permissions);
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public int getColor()
    {
        return color;
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
        throw new RuntimeException("Somehow when determining position we never found the role in the Guild's roles? wtf?");
    }

    @Override
    public int getPositionRaw()
    {
        return position;
    }

    @Override
    public boolean hasPermission(Permission perm)
    {
        return ((1 << perm.getOffset()) & permissions) > 0 || ((1 << Permission.ADMINISTRATOR.getOffset()) & permissions) > 0;
    }

    @Override
    public boolean isManaged()
    {
        return managed;
    }

    @Override
    public boolean isGrouped()
    {
        return grouped;
    }

    @Override
    public boolean isMentionable()
    {
        return mentionable;
    }

    @Override
    public String getAsMention()
    {
        return "<@&" + getId() + '>';
    }

    @Override
    public synchronized RoleManager getManager()
    {
        if (manager == null)
            manager = new RoleManager(this);
        return manager;
    }

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

    public RoleImpl setPosition(int position)
    {
        this.position = position;
        return this;
    }

    public RoleImpl setPermissions(int permissions)
    {
        this.permissions = permissions;
        return this;
    }

    public RoleImpl setManaged(boolean managed)
    {
        this.managed = managed;
        return this;
    }

    public RoleImpl setGrouped(boolean grouped)
    {
        this.grouped = grouped;
        return this;
    }

    public RoleImpl setMentionable(boolean mentionable)
    {
        this.mentionable = mentionable;
        return this;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof Role))
            return false;
        Role oRole = (Role) o;
        return this == oRole || this.getId().equals(oRole.getId());
    }

    @Override
    public int hashCode()
    {
        return getId().hashCode();
    }

    @Override
    public String toString()
    {
        return "R:" + getName() + '(' + getId() + ')';
    }

    @Override
    public int compareTo(Role r)
    {
        if (this == r)
            return 0;

        if (this.getGuild() != r.getGuild())
            throw new IllegalArgumentException("Cannot compare roles that aren't from the same guild!");

        if (this.getPositionRaw() != r.getPositionRaw())
            return this.getPositionRaw() - r.getPositionRaw();

        OffsetDateTime thisTime = MiscUtil.getCreationTime(this);
        OffsetDateTime rTime = MiscUtil.getCreationTime(r);

        //We compare the provided role's time to this's time instead of the reverse as one would expect due to how
        // discord deals with hierarchy. The more recent a role was created, the lower its hierarchy ranking when
        // it shares the same position as another role.
        return rTime.compareTo(thisTime);
    }
}
