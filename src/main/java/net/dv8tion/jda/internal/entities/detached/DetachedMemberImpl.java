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
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.unions.DefaultGuildChannelUnion;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.exceptions.MissingEntityInteractionPermissionsException;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.channel.mixin.attribute.IInteractionPermissionMixin;
import net.dv8tion.jda.internal.entities.mixin.MemberMixin;
import net.dv8tion.jda.internal.interactions.ChannelInteractionPermissions;
import net.dv8tion.jda.internal.interactions.MemberInteractionPermissions;
import net.dv8tion.jda.internal.utils.EntityString;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

public class DetachedMemberImpl implements Member, MemberMixin<DetachedMemberImpl>
{
    private final JDAImpl api;

    private final DetachedGuildImpl guild;
    private User user;
    private String nickname;
    private String avatarId;
    private long joinDate, boostDate, timeOutEnd;
    private boolean pending = false;
    private int flags;

    // Permissions calculated by Discord
    private MemberInteractionPermissions interactionPermissions;

    public DetachedMemberImpl(DetachedGuildImpl guild, User user)
    {
        this.api = (JDAImpl) user.getJDA();
        this.guild = guild;
        this.user = user;
        this.joinDate = 0;
    }

    @Override
    public boolean isDetached()
    {
        return true;
    }

    @Nonnull
    @Override
    public User getUser()
    {
        // The user could come from another guild
        // Load user from cache if one exists, ideally two members with the same id should wrap the same user object
        User realUser = getJDA().getUserById(user.getIdLong());
        if (realUser != null)
            this.user = realUser;
        return user;
    }

    @Nonnull
    @Override
    public Guild getGuild()
    {
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
        throw detachedException();
    }

    @Nonnull
    @Override
    public List<Activity> getActivities()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public OnlineStatus getOnlineStatus()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public OnlineStatus getOnlineStatus(@Nonnull ClientType type)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public EnumSet<ClientType> getActiveClients()
    {
        throw detachedException();
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
        throw detachedException();
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
        throw detachedRequiresChannelException();
    }

    @Nonnull
    @Override
    public EnumSet<Permission> getPermissions(@Nonnull GuildChannel channel)
    {
        return Permission.getPermissions(getRawInteractionPermissions(channel));
    }

    @Nonnull
    @Override
    public EnumSet<Permission> getPermissionsExplicit()
    {
        throw detachedRequiresChannelException();
    }

    @Nonnull
    @Override
    public EnumSet<Permission> getPermissionsExplicit(@Nonnull GuildChannel channel)
    {
        return Permission.getPermissions(getRawInteractionPermissions(channel));
    }

    @Override
    public boolean hasPermission(@Nonnull Permission... permissions)
    {
        throw detachedRequiresChannelException();
    }

    @Override
    public boolean hasPermission(@Nonnull GuildChannel channel, @Nonnull Permission... permissions)
    {
        final long rawPermissions = Permission.getRaw(permissions);
        return (getRawInteractionPermissions(channel) & rawPermissions) == rawPermissions;
    }

    private long getRawInteractionPermissions(@Nonnull GuildChannel channel)
    {
        if (interactionPermissions.getChannelId() == channel.getIdLong())
            return interactionPermissions.getPermissions();

        if (channel instanceof IInteractionPermissionMixin<?>)
        {
            final ChannelInteractionPermissions channelInteractionPermissions = ((IInteractionPermissionMixin<?>) channel).getInteractionPermissions();
            if (channelInteractionPermissions.getMemberId() == this.getIdLong())
                return channelInteractionPermissions.getPermissions();
        }

        throw new MissingEntityInteractionPermissionsException(
                "Detached member permissions can only be retrieved in the interaction channel, " +
                        "and channels only contain the permissions of the interaction caller"
        );
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
    public boolean canInteract(@Nonnull Member member)
    {
        throw detachedException();
    }

    @Override
    public boolean canInteract(@Nonnull Role role)
    {
        throw detachedException();
    }

    @Override
    public boolean canInteract(@Nonnull RichCustomEmoji emoji)
    {
        throw detachedException();
    }

    @Override
    public boolean isOwner()
    {
        throw detachedException();
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
        return user.getAsMention();
    }

    @Nullable
    @Override
    public DefaultGuildChannelUnion getDefaultChannel()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public String getDefaultAvatarId()
    {
        return user.getDefaultAvatarId();
    }

    @Nonnull
    public MemberInteractionPermissions getInteractionPermissions()
    {
        return interactionPermissions;
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> modifyFlags(@Nonnull Collection<MemberFlag> newFlags)
    {
        throw detachedException();
    }

    @Override
    public DetachedMemberImpl setNickname(String nickname)
    {
        this.nickname = nickname;
        return this;
    }

    @Override
    public DetachedMemberImpl setAvatarId(String avatarId)
    {
        this.avatarId = avatarId;
        return this;
    }

    @Override
    public DetachedMemberImpl setJoinDate(long joinDate)
    {
        this.joinDate = joinDate;
        return this;
    }

    @Override
    public DetachedMemberImpl setBoostDate(long boostDate)
    {
        this.boostDate = boostDate;
        return this;
    }

    @Override
    public DetachedMemberImpl setTimeOutEnd(long time)
    {
        this.timeOutEnd = time;
        return this;
    }

    @Override
    public DetachedMemberImpl setPending(boolean pending)
    {
        this.pending = pending;
        return this;
    }

    @Override
    public DetachedMemberImpl setFlags(int flags)
    {
        this.flags = flags;
        return this;
    }

    public DetachedMemberImpl setInteractionPermissions(@Nonnull MemberInteractionPermissions interactionPermissions)
    {
        this.interactionPermissions = interactionPermissions;
        return this;
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
        if (!(o instanceof DetachedMemberImpl))
            return false;

        DetachedMemberImpl oMember = (DetachedMemberImpl) o;
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
