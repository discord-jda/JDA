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

package net.dv8tion.jda.internal.entities.detached;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.RoleIcon;
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.exceptions.DetachedEntityException;
import net.dv8tion.jda.api.managers.RoleManager;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.RoleImpl.RoleTagsImpl;
import net.dv8tion.jda.internal.entities.mixin.RoleMixin;
import net.dv8tion.jda.internal.utils.EntityString;
import net.dv8tion.jda.internal.utils.PermissionUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.EnumSet;

public class DetachedRoleImpl implements Role, RoleMixin<DetachedRoleImpl>
{
    private final long id;
    private final JDAImpl api;
    private final DetachedGuildImpl guild;

    private RoleTagsImpl tags;
    private String name;
    private boolean managed;
    private boolean hoisted;
    private boolean mentionable;
    private long rawPermissions;
    private int color;
    private int rawPosition;
    private RoleIcon icon;

    public DetachedRoleImpl(long id, DetachedGuildImpl guild)
    {
        this.id = id;
        this.api = guild.getJDA();
        this.guild = guild;
        this.tags = api.isCacheFlagSet(CacheFlag.ROLE_TAGS) ? new RoleTagsImpl() : null;
    }

    @Override
    public boolean isDetached()
    {
        return true;
    }

    @Override
    public int getPosition()
    {
        throw new DetachedEntityException("Cannot get the position of a detached role, only the raw position is available");
    }

    @Override
    public int getPositionRaw()
    {
        return rawPosition;
    }

    @Nonnull
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

    @Nonnull
    @Override
    public EnumSet<Permission> getPermissions()
    {
        return Permission.getPermissions(rawPermissions);
    }

    @Nonnull
    @Override
    public EnumSet<Permission> getPermissions(@Nonnull GuildChannel channel)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public EnumSet<Permission> getPermissionsExplicit()
    {
        return getPermissions();
    }

    @Nonnull
    @Override
    public EnumSet<Permission> getPermissionsExplicit(@Nonnull GuildChannel channel)
    {
        throw detachedException();
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
        return getIdLong() == this.getGuild().getIdLong();
    }

    @Override
    public boolean hasPermission(@Nonnull Permission... permissions)
    {
        long effectivePerms = rawPermissions;
        for (Permission perm : permissions)
        {
            final long rawValue = perm.getRawValue();
            if ((effectivePerms & rawValue) != rawValue)
                return false;
        }
        return true;
    }

    @Override
    public boolean hasPermission(@Nonnull GuildChannel channel, @Nonnull Permission... permissions)
    {
        throw detachedException();
    }

    @Override
    public boolean canSync(@Nonnull IPermissionContainer targetChannel, @Nonnull IPermissionContainer syncSource)
    {
        throw detachedException();
    }

    @Override
    public boolean canSync(@Nonnull IPermissionContainer channel)
    {
        throw detachedException();
    }

    @Override
    public boolean canInteract(@Nonnull Role role)
    {
        // Works, depends on compareTo
        return PermissionUtil.canInteract(this, role);
    }

    @Nonnull
    @Override
    public Guild getGuild()
    {
        return guild;
    }

    @Nonnull
    @Override
    public RoleManager getManager()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> delete()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public JDA getJDA()
    {
        return api;
    }

    @Nonnull
    @Override
    public RoleTags getTags()
    {
        return tags == null ? RoleTagsImpl.EMPTY : tags;
    }

    @Nullable
    @Override
    public RoleIcon getIcon()
    {
        return icon;
    }

    @Nonnull
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
        if (o == this)
            return true;
        if (!(o instanceof Role))
            return false;
        Role oRole = (Role) o;
        return this.getIdLong() == oRole.getIdLong();
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .setName(getName())
                .toString();
    }

    // -- Setters --

    @Override
    public DetachedRoleImpl setName(String name)
    {
        this.name = name;
        return this;
    }

    @Override
    public DetachedRoleImpl setColor(int color)
    {
        this.color = color;
        return this;
    }

    @Override
    public DetachedRoleImpl setManaged(boolean managed)
    {
        this.managed = managed;
        return this;
    }

    @Override
    public DetachedRoleImpl setHoisted(boolean hoisted)
    {
        this.hoisted = hoisted;
        return this;
    }

    @Override
    public DetachedRoleImpl setMentionable(boolean mentionable)
    {
        this.mentionable = mentionable;
        return this;
    }

    @Override
    public DetachedRoleImpl setRawPermissions(long rawPermissions)
    {
        this.rawPermissions = rawPermissions;
        return this;
    }

    @Override
    public DetachedRoleImpl setRawPosition(int rawPosition)
    {
        this.rawPosition = rawPosition;
        return this;
    }

    @Override
    public DetachedRoleImpl setTags(DataObject tags)
    {
        if (this.tags == null)
            return this;
        this.tags = new RoleTagsImpl(tags);
        return this;
    }

    @Override
    public DetachedRoleImpl setIcon(RoleIcon icon)
    {
        this.icon = icon;
        return this;
    }

}
