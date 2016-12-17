package net.dv8tion.jda.core.entities.impl;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.requests.*;
import net.dv8tion.jda.core.requests.Route.CompiledRoute;

import org.apache.http.util.Args;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.OffsetDateTime;

public class InviteImpl implements Invite
{
    private final JDAImpl api;
    private final String channelId;
    private final String channelName;
    private final ChannelType channelType;
    private final String code;
    private final boolean expanded;
    private final String guildIconId;
    private final String guildId;
    private final String guildName;
    private final String guildSplashId;
    private final User inviter;
    private final int maxAge;
    private final int maxUses;
    private final boolean temporary;
    private final OffsetDateTime timeCreated;
    private final int uses;

    public InviteImpl(final JDA api, final String channelId, final String channelName, final ChannelType channelType, final String code,
            final String guildIconId, final String guildId, final String guildName, final String guildSplashId, final boolean temporary, final int uses,
            final OffsetDateTime timeCreated, final int maxUses, final int maxAge, final User inviter, final boolean expanded)
    {
        this.api = (JDAImpl) api;
        this.channelId = channelId;
        this.channelName = channelName;
        this.channelType = channelType;
        this.code = code;
        this.guildIconId = guildIconId;
        this.guildId = guildId;
        this.guildName = guildName;
        this.guildSplashId = guildSplashId;
        this.temporary = temporary;
        this.uses = uses;
        this.timeCreated = timeCreated;
        this.maxUses = maxUses;
        this.maxAge = maxAge;
        this.inviter = inviter;
        this.expanded = expanded;
    }

    public static RestAction<Invite> resolve(final JDA api, final String code)
    {
        Args.notNull(code, "code");
        Args.notNull(api, "api");

        final Route.CompiledRoute route = Route.Invites.GET_INVITE.compile(code);

        return new RestAction<Invite>(api, route, null)
        {
            @Override
            protected void handleResponse(final Response response, final Request request)
            {
                if (response.isOk())
                {
                    final Invite invite = EntityBuilder.get(this.api).createInvite(response.getObject());
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
    public RestAction<Invite> expand()
    {
        final Guild guild = this.api.getGuildById(this.guildId);
        if (guild == null)
        {
            throw new UnsupportedOperationException("You're not in the guild this invite points to");
        }

        final Member member = guild.getSelfMember();

        CompiledRoute route;

        if (member.hasPermission(Permission.MANAGE_SERVER))
        {
            route = Route.Invites.GET_GUILD_INVITES.compile(guild.getId());
        }
        else
        {
            final Channel channel = this.channelType == ChannelType.TEXT ? guild.getTextChannelById(this.channelId) : guild.getVoiceChannelById(this.channelId);
            if (member.hasPermission(channel, Permission.MANAGE_CHANNEL))
            {
                route = Route.Invites.GET_CHANNEL_INVITES.compile(channel.getId());
            }
            else
            {
                throw new PermissionException("You don't have the permission to view the full invite info");
            }
        }

        return new RestAction<Invite>(this.api, route, null)
        {
            @Override
            protected void handleResponse(final Response response, final Request request)
            {
                if (response.isOk())
                {
                    final EntityBuilder entityBuilder = EntityBuilder.get(this.api);
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
                    request.onFailure(new RuntimeException("Missing the invite in the channel/guild invite list"));
                }
                else
                {
                    request.onFailure(response);
                }
            }
        };
    }

    @Override
    public String getChannelId()
    {
        return this.channelId;
    }

    @Override
    public String getChannelName()
    {
        return this.channelName;
    }

    @Override
    public ChannelType getChannelType()
    {
        return this.channelType;
    }

    @Override
    public String getCode()
    {
        return this.code;
    }

    @Override
    public String getGuildIconId()
    {
        return this.guildIconId;
    }

    @Override
    public String getGuildId()
    {
        return this.guildId;
    }

    @Override
    public String getGuildName()
    {
        return this.guildName;
    }

    @Override
    public String getGuildSplashId()
    {
        return this.guildSplashId;
    }

    @Override
    public User getInviter()
    {
        if (!this.expanded)
        {
            throw new IllegalStateException("Only valid for expanded invites");
        }
        return this.inviter;
    }

    public JDAImpl getJDA()
    {
        return this.api;
    }

    @Override
    public int getMaxAge()
    {
        if (!this.expanded)
        {
            throw new IllegalStateException("Only valid for expanded invites");
        }
        return this.maxAge;
    }

    @Override
    public int getMaxUses()
    {
        if (!this.expanded)
        {
            throw new IllegalStateException("Only valid for expanded invites");
        }
        return this.maxUses;
    }

    @Override
    public OffsetDateTime getTimeCreated()
    {
        if (!this.expanded)
        {
            throw new IllegalStateException("Only valid for expanded invites");
        }
        return this.timeCreated;
    }

    @Override
    public int getUses()
    {
        if (!this.expanded)
        {
            throw new IllegalStateException("Only valid for expanded invites");
        }
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
        {
            throw new IllegalStateException("Only valid for expanded invites");
        }
        return this.temporary;
    }

    @Override
    public String toString()
    {
        return "Invite(" + this.code + ")";
    }

}
