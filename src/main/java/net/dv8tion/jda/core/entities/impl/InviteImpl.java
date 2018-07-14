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
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.Guild.VerificationLevel;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.core.utils.Checks;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Set;

public class InviteImpl implements Invite
{
    private final JDAImpl api;
    private final Channel channel;
    private final String code;
    private final boolean expanded;
    private final Guild guild;
    private final User inviter;
    private final int maxAge;
    private final int maxUses;
    private final boolean temporary;
    private final OffsetDateTime timeCreated;
    private final int uses;

    public InviteImpl(final JDAImpl api, final String code, final boolean expanded, final User inviter,
            final int maxAge, final int maxUses, final boolean temporary, final OffsetDateTime timeCreated,
            final int uses, final Channel channel, final Guild guild)
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
    }

    public static RestAction<Invite> resolve(final JDA api, final String code, final boolean withCounts)
    {
        Checks.notNull(code, "code");
        Checks.notNull(api, "api");

        Route.CompiledRoute route = Route.Invites.GET_INVITE.compile(code);
        
        if (withCounts)
            route = route.withQueryParams("with_counts", "true");

        return new RestAction<Invite>(api, route)
        {
            @Override
            protected void handleResponse(final Response response, final Request<Invite> request)
            {
                if (response.isOk())
                {
                    final Invite invite = this.api.getEntityBuilder().createInvite(response.getObject());
                    request.onSuccess(invite);
                }
                else
                {
                    request.onFailure(response);
                }
            }
        };
    }

    @Override
    public AuditableRestAction<Void> delete()
    {
        final Route.CompiledRoute route = Route.Invites.DELETE_INVITE.compile(this.code);

        return new AuditableRestAction<Void>(this.api, route)
        {
            @Override
            protected void handleResponse(final Response response, final Request<Void> request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    @Override
    public RestAction<Invite> expand()
    {
        if (this.expanded)
            return new RestAction.EmptyRestAction<>(getJDA(), this);

        final net.dv8tion.jda.core.entities.Guild guild = this.api.getGuildById(this.guild.getIdLong());

        if (guild == null)
            throw new UnsupportedOperationException("You're not in the guild this invite points to");

        final Member member = guild.getSelfMember();

        Route.CompiledRoute route;

        final net.dv8tion.jda.core.entities.Channel channel = this.channel.getType() == ChannelType.TEXT
                ? guild.getTextChannelById(this.channel.getIdLong())
                : guild.getVoiceChannelById(this.channel.getIdLong());

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
            throw new InsufficientPermissionException(Permission.MANAGE_CHANNEL, "You don't have the permission to view the full invite info");
        }

        return new RestAction<Invite>(this.api, route)
        {
            @Override
            protected void handleResponse(final Response response, final Request<Invite> request)
            {
                if (response.isOk())
                {
                    final EntityBuilder entityBuilder = this.api.getEntityBuilder();
                    final JSONArray array = response.getArray();
                    for (int i = 0; i < array.length(); i++)
                    {
                        final JSONObject object = array.getJSONObject(i);
                        if (InviteImpl.this.code.equals(object.getString("code")))
                        {
                            request.onSuccess(entityBuilder.createInvite(object));
                            return;
                        }
                    }
                    request.onFailure(new IllegalStateException("Missing the invite in the channel/guild invite list"));
                }
                else
                {
                    request.onFailure(response);
                }
            }
        };
    }

    @Override
    public Channel getChannel()
    {
        return this.channel;
    }

    @Override
    public String getCode()
    {
        return this.code;
    }

    @Override
    public OffsetDateTime getCreationTime()
    {
        if (!this.expanded)
            throw new IllegalStateException("Only valid for expanded invites");
        return this.timeCreated;
    }

    @Override
    public Guild getGuild()
    {
        return this.guild;
    }

    @Override
    public User getInviter()
    {
        return this.inviter;
    }

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
    public String toString()
    {
        return "Invite(" + this.code + ")";
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

        @Override
        public long getIdLong()
        {
            return id;
        }

        @Override
        public String getName()
        {
            return this.name;
        }

        @Override
        public ChannelType getType()
        {
            return this.type;
        }

    }

    public static class GuildImpl implements Guild
    {
        private final String iconId, name, splashId;
        private final int presenceCount, memberCount;
        private final long id;
        private final VerificationLevel verificationLevel;
        private final Set<String> features;

        public GuildImpl(final long id, final String iconId, final String name, final String splashId, 
                         final VerificationLevel verificationLevel, final int presenceCount, final int memberCount, final Set<String> features)
        {
            this.id = id;
            this.iconId = iconId;
            this.name = name;
            this.splashId = splashId;
            this.verificationLevel = verificationLevel;
            this.presenceCount = presenceCount;
            this.memberCount = memberCount;
            this.features = features;
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
                    : "https://cdn.discordapp.com/icons/" + this.id + "/" + this.iconId + ".jpg";
        }

        @Override
        public long getIdLong()
        {
            return id;
        }

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
                    : "https://cdn.discordapp.com/splashes/" + this.id + "/" + this.splashId + ".jpg";
        }

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

        @Override
        public Set<String> getFeatures()
        {
            return features;
        }
    }

}
