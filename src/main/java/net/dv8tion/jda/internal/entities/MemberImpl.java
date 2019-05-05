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

package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.PermissionUtil;
import net.dv8tion.jda.internal.utils.cache.UpstreamReference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MemberImpl implements Member
{
    private static final ZoneOffset OFFSET = ZoneOffset.of("+00:00");
    private final long guildId, userId;
    private final UpstreamReference<JDAImpl> api;
    private final Set<Role> roles = ConcurrentHashMap.newKeySet();
    private final GuildVoiceState voiceState;
    private final Map<ClientType, OnlineStatus> clientStatus;

    private String nickname;
    private long joinDate;
    private List<Activity> activities = null;
    private OnlineStatus onlineStatus = OnlineStatus.OFFLINE;

    public MemberImpl(GuildImpl guild, User user)
    {
        this.guildId = guild.getIdLong();
        this.userId = user.getIdLong();
        JDAImpl jda;
        this.api = new UpstreamReference<>(jda = (JDAImpl) user.getJDA());
        boolean cacheState = jda.isCacheFlagSet(CacheFlag.VOICE_STATE) || user.equals(jda.getSelfUser());
        boolean cacheOnline = jda.isCacheFlagSet(CacheFlag.CLIENT_STATUS);
        this.voiceState = cacheState ? new GuildVoiceStateImpl(this) : null;
        this.clientStatus = cacheOnline ? new ConcurrentHashMap<>(5) : null;
    }

    @Nonnull
    @Override
    public User getUser()
    {
        User user = getJDA().getUserById(userId);
        if (user == null)
            throw new IllegalStateException("Cannot get reference to upstream User with id: " + Long.toUnsignedString(userId));
        return user;
    }

    @Nonnull
    @Override
    public GuildImpl getGuild()
    {
        GuildImpl guild = (GuildImpl) getJDA().getGuildById(guildId);
        if (guild == null)
            throw new IllegalStateException("Cannot get reference to upstream Guild with id: " + Long.toUnsignedString(guildId));
        return guild;
    }

    @Nonnull
    @Override
    public JDA getJDA()
    {
        return api.get();
    }

    @Nonnull
    @Override
    public OffsetDateTime getTimeJoined()
    {
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(joinDate), OFFSET);
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
        return activities == null || activities.isEmpty() ? Collections.emptyList() : activities;
    }

    @Nonnull
    @Override
    public OnlineStatus getOnlineStatus()
    {
        return onlineStatus;
    }

    @Nonnull
    @Override
    public OnlineStatus getOnlineStatus(@Nonnull ClientType type)
    {
        Checks.notNull(type, "Type");
        if (this.clientStatus == null || this.clientStatus.isEmpty())
            return OnlineStatus.OFFLINE;
        OnlineStatus status = this.clientStatus.get(type);
        return status == null ? OnlineStatus.OFFLINE : status;
    }

    @Override
    public String getNickname()
    {
        return nickname;
    }

    @Nonnull
    @Override
    public String getEffectiveName()
    {
        return nickname != null ? nickname : getUser().getName();
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

        return Permission.getPermissions(PermissionUtil.getEffectivePermission(channel, this));
    }

    @Override
    public boolean hasPermission(@Nonnull Permission... permissions)
    {
        return PermissionUtil.checkPermission(this, permissions);
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
        return PermissionUtil.checkPermission(channel, this, permissions);
    }

    @Override
    public boolean hasPermission(@Nonnull GuildChannel channel, @Nonnull Collection<Permission> permissions)
    {
        Checks.notNull(permissions, "Permission Collection");

        return hasPermission(channel, permissions.toArray(Permission.EMPTY_PERMISSIONS));
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
    public boolean canInteract(@Nonnull Emote emote)
    {
        return PermissionUtil.canInteract(this, emote);
    }

    @Override
    public boolean isOwner() {
        return this.equals(getGuild().getOwner());
    }

    @Override
    public long getIdLong()
    {
        return userId;
    }

    public MemberImpl setNickname(String nickname)
    {
        this.nickname = nickname;
        return this;
    }

    public MemberImpl setJoinDate(long joinDate)
    {
        this.joinDate = joinDate;
        return this;
    }

    public MemberImpl setActivities(List<Activity> activities)
    {
        this.activities = Collections.unmodifiableList(activities);
        return this;
    }

    public MemberImpl setOnlineStatus(ClientType type, OnlineStatus status)
    {
        if (this.clientStatus == null || type == ClientType.UNKNOWN || type == null)
            return this;
        if (status == null || status == OnlineStatus.UNKNOWN || status == OnlineStatus.OFFLINE)
            this.clientStatus.remove(type);
        else
            this.clientStatus.put(type, status);
        return this;
    }

    public MemberImpl setOnlineStatus(OnlineStatus onlineStatus)
    {
        this.onlineStatus = onlineStatus;
        return this;
    }

    public Set<Role> getRoleSet()
    {
        return roles;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this)
            return true;
        if (!(o instanceof MemberImpl))
            return false;

        MemberImpl oMember = (MemberImpl) o;
        return oMember.userId == userId && oMember.guildId == guildId;
    }

    @Override
    public int hashCode()
    {
        return (Long.toUnsignedString(guildId) + Long.toUnsignedString(userId)).hashCode();
    }

    @Override
    public String toString()
    {
        return "MB:" + getEffectiveName() + '(' + getUser().toString() + " / " + getGuild().toString() +')';
    }

    @Nonnull
    @Override
    public String getAsMention()
    {
        return (nickname == null ? "<@" : "<@!") + Long.toUnsignedString(userId) + '>';
    }

    @Nullable
    @Override
    public TextChannel getDefaultChannel()
    {
        return getGuild().getTextChannelsView().stream()
                 .sorted(Comparator.reverseOrder())
                 .filter(c -> hasPermission(c, Permission.MESSAGE_READ))
                 .findFirst().orElse(null);
    }
}
