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
import net.dv8tion.jda.api.entities.Guild.VerificationLevel;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.requests.CompletedRestAction;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.restaction.AuditableRestActionImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EntityString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

public class InviteImpl implements Invite
{
    private final JDAImpl api;
    private final Channel channel;
    private final String code;
    private final boolean expanded;
    private final Guild guild;
    private final Group group;
    private final InviteTarget target;
    private final User inviter;
    private final int maxAge;
    private final int maxUses;
    private final boolean temporary;
    private final OffsetDateTime timeCreated;
    private final int uses;
    private final Invite.InviteType type;

    public InviteImpl(final JDAImpl api, final String code, final boolean expanded, final User inviter,
            final int maxAge, final int maxUses, final boolean temporary, final OffsetDateTime timeCreated, final int uses,
            final Channel channel, final Guild guild, final Group group, final InviteTarget target, final Invite.InviteType type)
    {
        this.api = api;
        this.code = code;
        this.expanded = expanded;
        this.inviter = inviter;
        this.maxAge = maxAge;
        this.maxUses = maxUses;
        this.temporary = temporary;
        this.timeCreated = timeCreated;
        this.uses = uses;
        this.channel = channel;
        this.guild = guild;
        this.group = group;
        this.target = target;
        this.type = type;
    }

    public static RestAction<Invite> resolve(final JDA api, final String code, final boolean withCounts)
    {
        Checks.notNull(code, "code");
        Checks.notNull(api, "api");

        Route.CompiledRoute route = Route.Invites.GET_INVITE.compile(code);
        
        if (withCounts)
            route = route.withQueryParams("with_counts", "true");

        JDAImpl jda = (JDAImpl) api;
        return new RestActionImpl<>(api, route, (response, request) ->
                jda.getEntityBuilder().createInvite(response.getObject()));
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> delete()
    {
        final Route.CompiledRoute route = Route.Invites.DELETE_INVITE.compile(this.code);

        return new AuditableRestActionImpl<>(this.api, route);
    }

    @Nonnull
    @Override
    public RestAction<Invite> expand()
    {
        if (this.expanded)
            return new CompletedRestAction<>(getJDA(), this);

        if (this.type != Invite.InviteType.GUILD)
            throw new IllegalStateException("Only guild invites can be expanded");

        final net.dv8tion.jda.api.entities.Guild guild = this.api.getGuildById(this.guild.getIdLong());

        if (guild == null)
            throw new UnsupportedOperationException("You're not in the guild this invite points to");

        final Member member = guild.getSelfMember();

        Route.CompiledRoute route;

        final GuildChannel channel = guild.getChannelById(GuildChannel.class, this.channel.getIdLong());
        if (channel == null)
            throw new UnsupportedOperationException("Cannot expand invite without known channel. Channel ID: " + this.channel.getId());

        if (member.hasPermission(channel, Permission.MANAGE_CHANNEL))
        {
            route = Route.Invites.GET_CHANNEL_INVITES.compile(channel.getId());
        }
        else if (member.hasPermission(Permission.MANAGE_SERVER))
        {
            route = Route.Invites.GET_GUILD_INVITES.compile(guild.getId());
        }
        else
        {
            throw new InsufficientPermissionException(channel, Permission.MANAGE_CHANNEL, "You don't have the permission to view the full invite info");
        }

        return new RestActionImpl<>(this.api, route, (response, request) ->
        {
            final EntityBuilder entityBuilder = this.api.getEntityBuilder();
            final DataArray array = response.getArray();
            for (int i = 0; i < array.length(); i++)
            {
                final DataObject object = array.getObject(i);
                if (InviteImpl.this.code.equals(object.getString("code")))
                {
                    return entityBuilder.createInvite(object);
                }
            }
            throw new IllegalStateException("Missing the invite in the channel/guild invite list");
        });
    }

    @Nonnull
    @Override
    public Invite.InviteType getType()
    {
        return this.type;
    }

    @Nonnull
    @Override
    public TargetType getTargetType()
    {
        return target == null ? TargetType.NONE : target.getType();
    }

    @Override
    public Channel getChannel()
    {
        return this.channel;
    }

    @Nonnull
    @Override
    public String getCode()
    {
        return this.code;
    }

    @Override
    public Guild getGuild()
    {
        return this.guild;
    }

    @Override
    public Group getGroup()
    {
        return this.group;
    }

    @Nullable
    @Override
    public InviteTarget getTarget()
    {
        return target;
    }

    @Override
    public User getInviter()
    {
        return this.inviter;
    }

    @Nonnull
    @Override
    public JDAImpl getJDA()
    {
        return this.api;
    }

    @Override
    public int getMaxAge()
    {
        if (!this.expanded)
            throw new IllegalStateException("Only valid for expanded invites");
        return this.maxAge;
    }

    @Override
    public int getMaxUses()
    {
        if (!this.expanded)
            throw new IllegalStateException("Only valid for expanded invites");
        return this.maxUses;
    }

    @Nonnull
    @Override
    public OffsetDateTime getTimeCreated()
    {
        if (!this.expanded)
            throw new IllegalStateException("Only valid for expanded invites");
        return this.timeCreated;
    }

    @Override
    public int getUses()
    {
        if (!this.expanded)
            throw new IllegalStateException("Only valid for expanded invites");
        return this.uses;
    }

    @Override
    public boolean isExpanded()
    {
        return this.expanded;
    }

    @Override
    public boolean isTemporary()
    {
        if (!this.expanded)
            throw new IllegalStateException("Only valid for expanded invites");
        return this.temporary;
    }

    @Override
    public int hashCode()
    {
        return code.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof InviteImpl))
            return false;
        InviteImpl impl = (InviteImpl) obj;
        return impl.code.equals(this.code);
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .addMetadata("code", code)
                .toString();
    }

    public static class ChannelImpl implements Channel
    {
        private final long id;
        private final String name;
        private final ChannelType type;

        public ChannelImpl(final long id, final String name, final ChannelType type)
        {
            this.id = id;
            this.name = name;
            this.type = type;
        }

        public ChannelImpl(final GuildChannel channel)
        {
            this(channel.getIdLong(), channel.getName(), channel.getType());
        }

        @Override
        public long getIdLong()
        {
            return id;
        }

        @Nonnull
        @Override
        public String getName()
        {
            return this.name;
        }

        @Nonnull
        @Override
        public ChannelType getType()
        {
            return this.type;
        }

        @Override
        public String toString()
        {
            return new EntityString(this)
                    .setType(getType())
                    .setName(name)
                    .toString();
        }
    }

    public static class GuildImpl implements Guild
    {
        private final String iconId, name, splashId;
        private final int presenceCount, memberCount;
        private final long id;
        private final VerificationLevel verificationLevel;
        private final Set<String> features;
        private final GuildWelcomeScreen welcomeScreen;

        public GuildImpl(final long id, final String iconId, final String name, final String splashId,
                         final VerificationLevel verificationLevel, final int presenceCount, final int memberCount, final Set<String> features,
                         final GuildWelcomeScreen welcomeScreen)
        {
            this.id = id;
            this.iconId = iconId;
            this.name = name;
            this.splashId = splashId;
            this.verificationLevel = verificationLevel;
            this.presenceCount = presenceCount;
            this.memberCount = memberCount;
            this.features = features;
            this.welcomeScreen = welcomeScreen;
        }

        public GuildImpl(final net.dv8tion.jda.api.entities.Guild guild)
        {
            this(guild.getIdLong(), guild.getIconId(), guild.getName(), guild.getSplashId(),
                 guild.getVerificationLevel(), -1, -1, guild.getFeatures(), null);
        }

        @Override
        public String getIconId()
        {
            return this.iconId;
        }

        @Override
        public String getIconUrl()
        {
            return this.iconId == null ? null
                    : "https://cdn.discordapp.com/icons/" + this.id + "/" + this.iconId + ".png";
        }

        @Override
        public long getIdLong()
        {
            return id;
        }

        @Nonnull
        @Override
        public String getName()
        {
            return this.name;
        }

        @Override
        public String getSplashId()
        {
            return this.splashId;
        }

        @Override
        public String getSplashUrl()
        {
            return this.splashId == null ? null
                    : "https://cdn.discordapp.com/splashes/" + this.id + "/" + this.splashId + ".png";
        }

        @Nonnull
        @Override
        public VerificationLevel getVerificationLevel()
        {
            return verificationLevel;
        }
        
        @Override
        public int getOnlineCount()
        {
            return presenceCount;
        }
        
        @Override
        public int getMemberCount()
        {
            return memberCount;
        }

        @Nonnull
        @Override
        public Set<String> getFeatures()
        {
            return features;
        }

        @Nullable
        @Override
        public GuildWelcomeScreen getWelcomeScreen()
        {
            return welcomeScreen;
        }

        @Override
        public String toString()
        {
            return new EntityString(this)
                    .setName(name)
                    .toString();
        }
    }

    public static class GroupImpl implements Group
    {
        private final String iconId, name;
        private final long id;
        private final List<String> users;

        public GroupImpl(final String iconId, final String name, final long id, final List<String> users)
        {
            this.iconId = iconId;
            this.name = name;
            this.id = id;
            this.users = users;
        }

        @Override
        public String getIconId()
        {
            return iconId;
        }

        @Override
        public String getIconUrl()
        {
            return this.iconId == null ? null
                : "https://cdn.discordapp.com/channel-icons/" + this.id + "/" + this.iconId + ".png";
        }

        @Override
        public String getName()
        {
            return name;
        }

        @Override
        public long getIdLong()
        {
            return id;
        }

        @Override
        public List<String> getUsers()
        {
            return users;
        }

        @Override
        public String toString()
        {
            return new EntityString(this)
                    .setName(name)
                    .toString();
        }
    }

    public static class InviteTargetImpl implements InviteTarget
    {
        private final TargetType type;
        private final EmbeddedApplication targetApplication;
        private final User targetUser;

        public InviteTargetImpl(TargetType type, EmbeddedApplication targetApplication, User targetUser)
        {
            this.type = type;
            this.targetApplication = targetApplication;
            this.targetUser = targetUser;
        }

        @Nonnull
        @Override
        public TargetType getType()
        {
            return type;
        }

        @Nonnull
        @Override
        public String getId()
        {
            return getTargetEntity().getId();
        }

        @Override
        public long getIdLong()
        {
            return getTargetEntity().getIdLong();
        }

        @Nullable
        @Override
        public User getUser()
        {
            return targetUser;
        }

        @Nullable
        @Override
        public EmbeddedApplication getApplication()
        {
            return targetApplication;
        }

        @Override
        public String toString()
        {
            return new EntityString(this)
                    .setType(getType())
                    .addMetadata("target", getTargetEntity())
                    .toString();
        }

        @Nonnull
        private ISnowflake getTargetEntity()
        {
            if (targetUser != null) return targetUser;
            if (targetApplication != null) return targetApplication;
            throw new IllegalStateException("No target entity");
        }

    }

    public static class EmbeddedApplicationImpl implements EmbeddedApplication
    {
        private final String iconId, name, description, summary;
        private final long id;
        private final int maxParticipants;

        public EmbeddedApplicationImpl(final String iconId, final String name, final String description, final String summary, final long id, final int maxParticipants)
        {
            this.iconId = iconId;
            this.name = name;
            this.description = description;
            this.summary = summary;
            this.id = id;
            this.maxParticipants = maxParticipants;
        }

        @Override
        public long getIdLong()
        {
            return this.id;
        }

        @Nonnull
        @Override
        public String getName()
        {
            return this.name;
        }

        @Nonnull
        @Override
        public String getDescription()
        {
            return this.description;
        }

        @Nullable
        @Override
        public String getSummary()
        {
            return this.summary;
        }

        @Nullable
        @Override
        public String getIconId()
        {
            return this.iconId;
        }

        @Nullable
        @Override
        public String getIconUrl()
        {
            return this.iconId == null ? null
                    : "https://cdn.discordapp.com/app-icons/" + this.id + '/' + this.iconId + ".png";
        }

        @Override
        public int getMaxParticipants()
        {
            return maxParticipants;
        }

        @Override
        public String toString()
        {
            return new EntityString(this)
                    .setName(name)
                    .toString();
        }
    }
}
