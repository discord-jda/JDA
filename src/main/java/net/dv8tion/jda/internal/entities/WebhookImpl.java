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
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.attribute.IWebhookContainer;
import net.dv8tion.jda.api.entities.channel.unions.IWebhookContainerUnion;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.WebhookManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.RestConfig;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.internal.managers.WebhookManagerImpl;
import net.dv8tion.jda.internal.requests.restaction.AuditableRestActionImpl;
import net.dv8tion.jda.internal.requests.restaction.WebhookMessageCreateActionImpl;
import net.dv8tion.jda.internal.requests.restaction.WebhookMessageEditActionImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EntityString;

import javax.annotation.Nonnull;

/**
 * The implementation for {@link net.dv8tion.jda.api.entities.Webhook Webhook}
 *
 * @since  3.0
 */
public class WebhookImpl extends AbstractWebhookClient<Void> implements Webhook
{
    private final IWebhookContainer channel;
    private final WebhookType type;

    private Member owner;
    private User user, ownerUser;
    private ChannelReference sourceChannel;
    private GuildReference sourceGuild;

    public WebhookImpl(IWebhookContainer channel, long id, WebhookType type)
    {
        this(channel, channel.getJDA(), id, type);
    }

    public WebhookImpl(IWebhookContainer channel, JDA api, long id, WebhookType type)
    {
        super(id, null, api);
        this.channel = channel;
        this.type = type;
    }

    @Nonnull
    @Override
    public WebhookType getType()
    {
        return type;
    }

    @Override
    public boolean isPartial()
    {
        return channel == null;
    }

    @Nonnull
    @Override
    public JDA getJDA()
    {
        return api;
    }

    @Nonnull
    @Override
    public Guild getGuild()
    {
        if (channel == null)
            throw new IllegalStateException("Cannot provide guild for this Webhook instance because it does not belong to this shard");
        return getChannel().getGuild();
    }

    @Nonnull
    @Override
    public IWebhookContainerUnion getChannel()
    {
        if (channel == null)
            throw new IllegalStateException("Cannot provide channel for this Webhook instance because it does not belong to this shard");
        return (IWebhookContainerUnion) channel;
    }

    @Override
    public Member getOwner()
    {
        if (owner == null && channel != null && ownerUser != null)
            return getGuild().getMember(ownerUser); // maybe it exists later?
        return owner;
    }

    @Override
    public User getOwnerAsUser()
    {
        return ownerUser;
    }

    @Nonnull
    @Override
    public User getDefaultUser()
    {
        return user;
    }

    @Nonnull
    @Override
    public String getName()
    {
        return user.getName();
    }

    @Override
    public String getToken()
    {
        return token;
    }

    @Nonnull
    @Override
    public String getUrl()
    {
        return RestConfig.DEFAULT_BASE_URL + "webhooks/" + getId() + (getToken() == null ? "" : "/" + getToken());
    }

    @Override
    public ChannelReference getSourceChannel()
    {
        return sourceChannel;
    }

    @Override
    public GuildReference getSourceGuild()
    {
        return sourceGuild;
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> delete()
    {
        if (token != null)
            return delete(token);

        if (!getGuild().getSelfMember().hasPermission(getChannel(), Permission.MANAGE_WEBHOOKS))
            throw new InsufficientPermissionException(getChannel(), Permission.MANAGE_WEBHOOKS);

        Route.CompiledRoute route = Route.Webhooks.DELETE_WEBHOOK.compile(getId());
        return new AuditableRestActionImpl<>(getJDA(), route);
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> delete(@Nonnull String token)
    {
        Checks.notNull(token, "Token");
        Route.CompiledRoute route = Route.Webhooks.DELETE_TOKEN_WEBHOOK.compile(getId(), token);
        return new AuditableRestActionImpl<>(getJDA(), route);
    }

    @Nonnull
    @Override
    public WebhookManager getManager()
    {
        return new WebhookManagerImpl(this);
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    /* -- Impl Setters -- */

    public WebhookImpl setOwner(Member member, User user)
    {
        this.owner = member;
        this.ownerUser = user;
        return this;
    }

    public WebhookImpl setToken(String token)
    {
        this.token = token;
        return this;
    }

    public WebhookImpl setUser(User user)
    {
        this.user = user;
        return this;
    }

    public WebhookImpl setSourceGuild(GuildReference reference)
    {
        this.sourceGuild = reference;
        return this;
    }

    public WebhookImpl setSourceChannel(ChannelReference reference)
    {
        this.sourceChannel = reference;
        return this;
    }

    /* -- Object Overrides -- */

    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof WebhookImpl))
            return false;
        WebhookImpl impl = (WebhookImpl) obj;
        return impl.id == id;
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .setName(getName())
                .toString();
    }

    // TODO: Implement WebhookMessage

    @Override
    public WebhookMessageCreateActionImpl<Void> sendRequest()
    {
        checkToken();
        Route.CompiledRoute route = Route.Webhooks.EXECUTE_WEBHOOK.compile(getId(), token);
        WebhookMessageCreateActionImpl<Void> action = new WebhookMessageCreateActionImpl<>(api, route, (json) -> null);
        action.run();
        return action;
    }

    @Override
    public WebhookMessageEditActionImpl<Void> editRequest(String messageId)
    {
        checkToken();
        Checks.isSnowflake(messageId);
        Route.CompiledRoute route = Route.Webhooks.EXECUTE_WEBHOOK_EDIT.compile(getId(), token, messageId);
        WebhookMessageEditActionImpl<Void> action = new WebhookMessageEditActionImpl<>(api, route, (json) -> null);
        action.run();
        return action;
    }

    @Nonnull
    @Override
    public RestAction<Void> deleteMessageById(@Nonnull String messageId)
    {
        checkToken();
        return super.deleteMessageById(messageId);
    }

    private void checkToken()
    {
        if (token == null)
            throw new UnsupportedOperationException("Cannot execute webhook without a token!");
    }
}
