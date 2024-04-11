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

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.RoleIcon;
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.RoleManager;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.RoleAction;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.channel.mixin.attribute.IPermissionContainerMixin;
import net.dv8tion.jda.internal.managers.RoleManagerImpl;
import net.dv8tion.jda.internal.requests.restaction.AuditableRestActionImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EntityString;
import net.dv8tion.jda.internal.utils.PermissionUtil;
import net.dv8tion.jda.internal.utils.cache.SortedSnowflakeCacheViewImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;

public class RoleImpl implements Role
{
    private final long id;
    private final JDAImpl api;
    private Guild guild;

    private RoleTagsImpl tags;
    private String name;
    private boolean managed;
    private boolean hoisted;
    private boolean mentionable;
    private long rawPermissions;
    private int color;
    private int rawPosition;
    private int frozenPosition = Integer.MIN_VALUE; // this is used exclusively for delete events
    private RoleIcon icon;

    public RoleImpl(long id, Guild guild)
    {
        this.id = id;
        this.api =(JDAImpl) guild.getJDA();
        this.guild = guild;
        this.tags = api.isCacheFlagSet(CacheFlag.ROLE_TAGS) ? new RoleTagsImpl() : null;
    }

    @Override
    public boolean isDetached()
    {
        return false;
    }

    @Override
    public int getPosition()
    {
        if (frozenPosition > Integer.MIN_VALUE)
            return frozenPosition;
        Guild guild = getGuild();
        if (equals(guild.getPublicRole()))
            return -1;

        //Subtract 1 to get into 0-index, and 1 to disregard the everyone role.
        int i = guild.getRoles().size() - 2;
        for (Role r : guild.getRoles())
        {
            if (equals(r))
                return i;
            i--;
        }
        throw new IllegalStateException("Somehow when determining position we never found the role in the Guild's roles? wtf?");
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
        return Permission.getPermissions(PermissionUtil.getEffectivePermission(channel.getPermissionContainer(), this));
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
        return Permission.getPermissions(PermissionUtil.getExplicitPermission(channel.getPermissionContainer(), this));
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
    public boolean hasPermission(@Nonnull Permission... permissions)
    {
        long effectivePerms = rawPermissions | getGuild().getPublicRole().getPermissionsRaw();
        for (Permission perm : permissions)
        {
            final long rawValue = perm.getRawValue();
            if ((effectivePerms & rawValue) != rawValue)
                return false;
        }
        return true;
    }

    @Override
    public boolean hasPermission(@Nonnull Collection<Permission> permissions)
    {
        Checks.notNull(permissions, "Permission Collection");

        return hasPermission(permissions.toArray(Permission.EMPTY_PERMISSIONS));
    }

    @Override
    public boolean hasPermission(@Nonnull GuildChannel channel, @Nonnull Permission... permissions)
    {
        long effectivePerms = PermissionUtil.getEffectivePermission(channel.getPermissionContainer(), this);
        for (Permission perm : permissions)
        {
            final long rawValue = perm.getRawValue();
            if ((effectivePerms & rawValue) != rawValue)
                return false;
        }
        return true;
    }

    @Override
    public boolean hasPermission(@Nonnull GuildChannel channel, @Nonnull Collection<Permission> permissions)
    {
        Checks.notNull(permissions, "Permission Collection");

        return hasPermission(channel, permissions.toArray(Permission.EMPTY_PERMISSIONS));
    }

    @Override
    public boolean canSync(@Nonnull IPermissionContainer targetChannel, @Nonnull IPermissionContainer syncSource)
    {
        Checks.notNull(targetChannel, "Channel");
        Checks.notNull(syncSource, "Channel");
        Checks.check(targetChannel.getGuild().equals(getGuild()), "Channels must be from the same guild!");
        Checks.check(syncSource.getGuild().equals(getGuild()), "Channels must be from the same guild!");
        long rolePerms = PermissionUtil.getEffectivePermission(targetChannel, this);
        if ((rolePerms & Permission.MANAGE_PERMISSIONS.getRawValue()) == 0)
            return false; // Role can't manage permissions at all!

        long channelPermissions = PermissionUtil.getExplicitPermission(targetChannel, this, false);
        // If the role has ADMINISTRATOR or MANAGE_PERMISSIONS then it can also set any other permission on the channel
        boolean hasLocalAdmin = ((rolePerms & Permission.ADMINISTRATOR.getRawValue()) | (channelPermissions & Permission.MANAGE_PERMISSIONS.getRawValue())) != 0;
        if (hasLocalAdmin)
            return true;

        TLongObjectMap<PermissionOverride> existingOverrides = ((IPermissionContainerMixin<?>) targetChannel).getPermissionOverrideMap();
        for (PermissionOverride override : syncSource.getPermissionOverrides())
        {
            PermissionOverride existing = existingOverrides.get(override.getIdLong());
            long allow = override.getAllowedRaw();
            long deny = override.getDeniedRaw();
            if (existing != null)
            {
                allow ^= existing.getAllowedRaw();
                deny ^= existing.getDeniedRaw();
            }
            // If any permissions changed that the role doesn't have in the channel, the role can't sync it :(
            if (((allow | deny) & ~rolePerms) != 0)
                return false;
        }
        return true;
    }

    @Override
    public boolean canSync(@Nonnull IPermissionContainer channel)
    {
        Checks.notNull(channel, "Channel");
        Checks.check(channel.getGuild().equals(getGuild()), "Channels must be from the same guild!");
        long rolePerms = PermissionUtil.getEffectivePermission(channel, this);
        if ((rolePerms & Permission.MANAGE_PERMISSIONS.getRawValue()) == 0)
            return false; // Role can't manage permissions at all!

        long channelPermissions = PermissionUtil.getExplicitPermission(channel, this, false);
        // If the role has ADMINISTRATOR or MANAGE_PERMISSIONS then it can also set any other permission on the channel
        return ((rolePerms & Permission.ADMINISTRATOR.getRawValue()) | (channelPermissions & Permission.MANAGE_PERMISSIONS.getRawValue())) != 0;
    }

    @Override
    public boolean canInteract(@Nonnull Role role)
    {
        return PermissionUtil.canInteract(this, role);
    }

    @Nonnull
    @Override
    public Guild getGuild()
    {
        Guild realGuild = api.getGuildById(guild.getIdLong());
        if (realGuild != null)
            guild = realGuild;
        return guild;
    }

    @Nonnull
    @Override
    public RoleAction createCopy(@Nonnull Guild guild)
    {
        Checks.notNull(guild, "Guild");
        return guild.createRole()
                    .setColor(color)
                    .setHoisted(hoisted)
                    .setMentionable(mentionable)
                    .setName(name)
                    .setPermissions(rawPermissions)
                    .setIcon(icon == null ? null : icon.getEmoji()); // we can only copy the emoji as we don't have access to the Icon instance
    }

    @Nonnull
    @Override
    public RoleManager getManager()
    {
        return new RoleManagerImpl(this);
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> delete()
    {
        Guild guild = getGuild();
        if (!guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES))
            throw new InsufficientPermissionException(guild, Permission.MANAGE_ROLES);
        if(!PermissionUtil.canInteract(guild.getSelfMember(), this))
            throw new HierarchyException("Can't delete role >= highest self-role");
        if (managed)
            throw new UnsupportedOperationException("Cannot delete a Role that is managed. ");

        Route.CompiledRoute route = Route.Roles.DELETE_ROLE.compile(guild.getId(), getId());
        return new AuditableRestActionImpl<>(getJDA(), route);
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

    @Override
    public int compareTo(@Nonnull Role r)
    {
        if (this == r)
            return 0;
        if (!(r instanceof RoleImpl))
            throw new IllegalArgumentException("Cannot compare different role implementations");
        RoleImpl impl = (RoleImpl) r;

        if (this.guild.getIdLong() != impl.guild.getIdLong())
            throw new IllegalArgumentException("Cannot compare roles that aren't from the same guild!");

        if (this.getPositionRaw() != r.getPositionRaw())
            return this.getPositionRaw() - r.getPositionRaw();

        OffsetDateTime thisTime = this.getTimeCreated();
        OffsetDateTime rTime = r.getTimeCreated();

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
        SortedSnowflakeCacheViewImpl<Role> roleCache = (SortedSnowflakeCacheViewImpl<Role>) getGuild().getRoleCache();
        roleCache.clearCachedLists();
        this.rawPosition = rawPosition;
        return this;
    }

    public RoleImpl setTags(DataObject tags)
    {
        if (this.tags == null)
            return this;
        this.tags = new RoleTagsImpl(tags);
        return this;
    }

    public RoleImpl setIcon(RoleIcon icon)
    {
        this.icon = icon;
        return this;
    }

    public void freezePosition()
    {
        this.frozenPosition = getPosition();
    }

    public static class RoleTagsImpl implements RoleTags
    {
        public static final RoleTags EMPTY = new RoleTagsImpl();
        private final long botId;
        private final long integrationId;
        private final long subscriptionListingId;
        private final boolean premiumSubscriber;
        private final boolean availableForPurchase;
        private final boolean isGuildConnections;

        public RoleTagsImpl()
        {
            this.botId = 0L;
            this.integrationId = 0L;
            this.subscriptionListingId = 0L;
            this.premiumSubscriber = false;
            this.availableForPurchase = false;
            this.isGuildConnections = false;
        }

        public RoleTagsImpl(DataObject tags)
        {
            this.botId = tags.getUnsignedLong("bot_id", 0L);
            this.integrationId = tags.getUnsignedLong("integration_id", 0L);
            this.subscriptionListingId = tags.getUnsignedLong("subscription_listing_id", 0L);
            this.premiumSubscriber = tags.hasKey("premium_subscriber");
            this.availableForPurchase = tags.hasKey("available_for_purchase");
            this.isGuildConnections = tags.hasKey("guild_connections");
        }

        @Override
        public boolean isBot()
        {
            return botId != 0;
        }

        @Override
        public long getBotIdLong()
        {
            return botId;
        }

        @Override
        public boolean isBoost()
        {
            return premiumSubscriber;
        }

        @Override
        public boolean isIntegration()
        {
            return integrationId != 0;
        }

        @Override
        public long getIntegrationIdLong()
        {
            return integrationId;
        }

        @Override
        public long getSubscriptionIdLong()
        {
            return subscriptionListingId;
        }

        @Override
        public boolean isAvailableForPurchase()
        {
            return availableForPurchase;
        }

        @Override
        public boolean isLinkedRole()
        {
            return isGuildConnections;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(botId, integrationId, premiumSubscriber, availableForPurchase, subscriptionListingId, isGuildConnections);
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == this)
                return true;
            if (!(obj instanceof RoleTagsImpl))
                return false;
            RoleTagsImpl other = (RoleTagsImpl) obj;
            return botId == other.botId
                && integrationId == other.integrationId
                && premiumSubscriber == other.premiumSubscriber
                && availableForPurchase == other.availableForPurchase
                && subscriptionListingId == other.subscriptionListingId
                && isGuildConnections == other.isGuildConnections;
        }

        @Override
        public String toString()
        {
            return new EntityString(this)
                    .addMetadata("bot", getBotId())
                    .addMetadata("integration", getIntegrationId())
                    .addMetadata("subscriptionListing", getSubscriptionId())
                    .addMetadata("isBoost", isBoost())
                    .addMetadata("isAvailableForPurchase", isAvailableForPurchase())
                    .addMetadata("isGuildConnections", isLinkedRole())
                    .toString();
        }
    }
}
