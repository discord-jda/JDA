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
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.unions.DefaultGuildChannelUnion;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.cache.CacheView;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.channel.mixin.attribute.IPermissionContainerMixin;
import net.dv8tion.jda.internal.entities.mixin.MemberMixin;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EntityString;
import net.dv8tion.jda.internal.utils.Helpers;
import net.dv8tion.jda.internal.utils.PermissionUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class MemberImpl implements Member, MemberMixin<MemberImpl>
{
    private final JDAImpl api;
    private final Set<Role> roles = ConcurrentHashMap.newKeySet();
    private final GuildVoiceState voiceState;

    private GuildImpl guild;
    private User user;
    private String nickname;
    private String avatarId;
    private long joinDate, boostDate, timeOutEnd;
    private boolean pending = false;
    private int flags;

    public MemberImpl(GuildImpl guild, User user)
    {
        this.api = (JDAImpl) user.getJDA();
        this.guild = guild;
        this.user = user;
        this.joinDate = 0;
        boolean cacheState = api.isCacheFlagSet(CacheFlag.VOICE_STATE) || user.equals(api.getSelfUser());
        this.voiceState = cacheState ? new GuildVoiceStateImpl(this) : null;
    }

    @Override
    public boolean isDetached()
    {
        return false;
    }

    public MemberPresenceImpl getPresence()
    {
        CacheView.SimpleCacheView<MemberPresenceImpl> presences = guild.getPresenceView();
        return presences == null ? null : presences.get(getIdLong());
    }

    @Nonnull
    @Override
    public User getUser()
    {
        // Load user from cache if one exists, ideally two members with the same id should wrap the same user object
        User realUser = getJDA().getUserById(user.getIdLong());
        if (realUser != null)
            this.user = realUser;
        return user;
    }

    @Nonnull
    @Override
    public GuildImpl getGuild()
    {
        GuildImpl realGuild = (GuildImpl) api.getGuildById(guild.getIdLong());
        if (realGuild != null)
            guild = realGuild;
        return guild;
    }

    @Nonnull
    @Override
    public JDA getJDA()
    {
        return api;
    }

    @Nonnull
    @Override
    public OffsetDateTime getTimeJoined()
    {
        if (hasTimeJoined())
            return Helpers.toOffset(joinDate);
        return getGuild().getTimeCreated();
    }

    @Override
    public boolean hasTimeJoined()
    {
        return joinDate != 0;
    }

    @Nullable
    @Override
    public OffsetDateTime getTimeBoosted()
    {
        return isBoosting() ? Helpers.toOffset(boostDate) : null;
    }

    @Override
    public boolean isBoosting()
    {
        return boostDate != 0;
    }

    @Nullable
    @Override
    public OffsetDateTime getTimeOutEnd()
    {
        return timeOutEnd != 0 ? Helpers.toOffset(timeOutEnd) : null;
    }

    @Override
    public GuildVoiceState getVoiceState()
    {
        return voiceState;
    }

    @Nonnull
    @Override
    public List<Activity> getActivities()
    {
        MemberPresenceImpl presence = getPresence();
        return presence == null ? Collections.emptyList() : presence.getActivities();
    }

    @Nonnull
    @Override
    public OnlineStatus getOnlineStatus()
    {
        MemberPresenceImpl presence = getPresence();
        return presence == null ? OnlineStatus.OFFLINE : presence.getOnlineStatus();
    }

    @Nonnull
    @Override
    public OnlineStatus getOnlineStatus(@Nonnull ClientType type)
    {
        Checks.notNull(type, "Type");
        MemberPresenceImpl presence = getPresence();
        if (presence == null)
            return OnlineStatus.OFFLINE;
        OnlineStatus status = presence.getClientStatus().get(type);
        return status == null ? OnlineStatus.OFFLINE : status;
    }

    @Nonnull
    @Override
    public EnumSet<ClientType> getActiveClients()
    {
        MemberPresenceImpl presence = getPresence();
        return presence == null ? EnumSet.noneOf(ClientType.class) : Helpers.copyEnumSet(ClientType.class, presence.getClientStatus().keySet());
    }

    @Override
    public String getNickname()
    {
        return nickname;
    }

    @Override
    public String getAvatarId()
    {
        return avatarId;
    }

    @Nonnull
    @Override
    public String getEffectiveName()
    {
        return nickname != null ? nickname : getUser().getEffectiveName();
    }

    @Nonnull
    @Override
    public List<Role> getRoles()
    {
        List<Role> roleList = new ArrayList<>(roles);
        roleList.sort(Comparator.reverseOrder());

        return Collections.unmodifiableList(roleList);
    }

    @Override
    public Color getColor()
    {
        final int raw = getColorRaw();
        return raw != Role.DEFAULT_COLOR_RAW ? new Color(raw) : null;
    }

    @Override
    public int getColorRaw()
    {
        for (Role r : getRoles())
        {
            final int colorRaw = r.getColorRaw();
            if (colorRaw != Role.DEFAULT_COLOR_RAW)
                return colorRaw;
        }
        return Role.DEFAULT_COLOR_RAW;
    }

    @Override
    public int getFlagsRaw()
    {
        return flags;
    }

    @Nonnull
    @Override
    public EnumSet<Permission> getPermissions()
    {
        return Permission.getPermissions(PermissionUtil.getEffectivePermission(this));
    }

    @Nonnull
    @Override
    public EnumSet<Permission> getPermissions(@Nonnull GuildChannel channel)
    {
        Checks.notNull(channel, "Channel");
        if (!getGuild().equals(channel.getGuild()))
            throw new IllegalArgumentException("Provided channel is not in the same guild as this member!");

        return Permission.getPermissions(PermissionUtil.getEffectivePermission(channel.getPermissionContainer(), this));
    }

    @Nonnull
    @Override
    public EnumSet<Permission> getPermissionsExplicit()
    {
        return Permission.getPermissions(PermissionUtil.getExplicitPermission(this));
    }

    @Nonnull
    @Override
    public EnumSet<Permission> getPermissionsExplicit(@Nonnull GuildChannel channel)
    {
        return Permission.getPermissions(PermissionUtil.getExplicitPermission(channel.getPermissionContainer(), this));
    }

    @Override
    public boolean hasPermission(@Nonnull Permission... permissions)
    {
        return PermissionUtil.checkPermission(this, permissions);
    }

    @Override
    public boolean hasPermission(@Nonnull GuildChannel channel, @Nonnull Permission... permissions)
    {
        return PermissionUtil.checkPermission(channel.getPermissionContainer(), this, permissions);
    }

    @Override
    public boolean canSync(@Nonnull IPermissionContainer targetChannel, @Nonnull IPermissionContainer syncSource)
    {
        Checks.notNull(targetChannel, "Channel");
        Checks.notNull(syncSource, "Channel");
        Checks.check(targetChannel.getGuild().equals(getGuild()), "Channels must be from the same guild!");
        Checks.check(syncSource.getGuild().equals(getGuild()), "Channels must be from the same guild!");
        long userPerms = PermissionUtil.getEffectivePermission(targetChannel, this);
        if ((userPerms & Permission.MANAGE_PERMISSIONS.getRawValue()) == 0)
            return false; // We can't manage permissions at all!

        long channelPermissions = PermissionUtil.getExplicitPermission(targetChannel, this, false);
        // If the user has ADMINISTRATOR or MANAGE_PERMISSIONS then it can also set any other permission on the channel
        boolean hasLocalAdmin = ((userPerms & Permission.ADMINISTRATOR.getRawValue()) | (channelPermissions & Permission.MANAGE_PERMISSIONS.getRawValue())) != 0;
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
            // If any permissions changed that the user doesn't have in the channel, they can't sync it :(
            if (((allow | deny) & ~userPerms) != 0)
                return false;
        }
        return true;
    }

    @Override
    public boolean canSync(@Nonnull IPermissionContainer channel)
    {
        Checks.notNull(channel, "Channel");
        Checks.check(channel.getGuild().equals(getGuild()), "Channels must be from the same guild!");
        long userPerms = PermissionUtil.getEffectivePermission(channel, this);
        if ((userPerms & Permission.MANAGE_PERMISSIONS.getRawValue()) == 0)
            return false; // We can't manage permissions at all!

        long channelPermissions = PermissionUtil.getExplicitPermission(channel, this, false);
        // If the user has ADMINISTRATOR or MANAGE_PERMISSIONS then it can also set any other permission on the channel
        return ((userPerms & Permission.ADMINISTRATOR.getRawValue()) | (channelPermissions & Permission.MANAGE_PERMISSIONS.getRawValue())) != 0;
    }

    @Override
    public boolean canInteract(@Nonnull Member member)
    {
        return PermissionUtil.canInteract(this, member);
    }

    @Override
    public boolean canInteract(@Nonnull Role role)
    {
        return PermissionUtil.canInteract(this, role);
    }

    @Override
    public boolean canInteract(@Nonnull RichCustomEmoji emoji)
    {
        return PermissionUtil.canInteract(this, emoji);
    }

    @Override
    public boolean isOwner()
    {
        return this.user.getIdLong() == getGuild().getOwnerIdLong();
    }

    @Override
    public boolean isPending()
    {
        return this.pending;
    }

    @Override
    public long getIdLong()
    {
        return user.getIdLong();
    }

    @Nonnull
    @Override
    public String getAsMention()
    {
        return "<@" + user.getId() + '>';
    }

    @Nullable
    @Override
    public DefaultGuildChannelUnion getDefaultChannel()
    {
        return (DefaultGuildChannelUnion) Stream.concat(getGuild().getTextChannelCache().stream(), getGuild().getNewsChannelCache().stream())
                .filter(c -> hasPermission(c, Permission.VIEW_CHANNEL))
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    @Nonnull
    @Override
    public String getDefaultAvatarId()
    {
        return user.getDefaultAvatarId();
    }

    @Override
    public MemberImpl setNickname(String nickname)
    {
        this.nickname = nickname;
        return this;
    }

    @Override
    public MemberImpl setAvatarId(String avatarId)
    {
        this.avatarId = avatarId;
        return this;
    }

    @Override
    public MemberImpl setJoinDate(long joinDate)
    {
        this.joinDate = joinDate;
        return this;
    }

    @Override
    public MemberImpl setBoostDate(long boostDate)
    {
        this.boostDate = boostDate;
        return this;
    }

    @Override
    public MemberImpl setTimeOutEnd(long time)
    {
        this.timeOutEnd = time;
        return this;
    }

    @Override
    public MemberImpl setPending(boolean pending)
    {
        this.pending = pending;
        return this;
    }

    @Override
    public MemberImpl setFlags(int flags)
    {
        this.flags = flags;
        return this;
    }

    public Set<Role> getRoleSet()
    {
        return roles;
    }

    public long getBoostDateRaw()
    {
        return boostDate;
    }

    public long getTimeOutEndRaw()
    {
        return timeOutEnd;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this)
            return true;
        if (!(o instanceof MemberImpl))
            return false;

        MemberImpl oMember = (MemberImpl) o;
        return oMember.user.getIdLong() == user.getIdLong()
            && oMember.guild.getIdLong() == guild.getIdLong();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(guild.getIdLong(), user.getIdLong());
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .setName(getEffectiveName())
                .addMetadata("user", getUser())
                .addMetadata("guild", getGuild())
                .toString();
    }
}
