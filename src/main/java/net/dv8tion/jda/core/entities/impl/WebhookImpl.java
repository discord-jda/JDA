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
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.managers.WebhookManager;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Requester;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.core.utils.MiscUtil;
import net.dv8tion.jda.webhook.WebhookClientBuilder;

import java.util.concurrent.locks.ReentrantLock;

/**
 * The implementation for {@link net.dv8tion.jda.core.entities.Webhook Webhook}
 *
 * @since  3.0
 */
public class WebhookImpl implements Webhook
{
    protected volatile WebhookManager manager = null;

    private final ReentrantLock mngLock = new ReentrantLock();
    private final TextChannel channel;
    private final long id;

    private Member owner;
    private User user;
    private String token;

    public WebhookImpl(TextChannel channel, long id)
    {
        this.channel = channel;
        this.id = id;
    }

    @Override
    public JDA getJDA()
    {
        return channel.getJDA();
    }

    @Override
    public Guild getGuild()
    {
        return channel.getGuild();
    }

    @Override
    public TextChannel getChannel()
    {
        return channel;
    }

    @Override
    public Member getOwner()
    {
        return owner;
    }

    @Override
    public User getDefaultUser()
    {
        return user;
    }

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

    @Override
    public String getUrl()
    {
        return Requester.DISCORD_API_PREFIX + "webhooks/" + getId() + (getToken() == null ? "" : "/" + getToken());
    }

    @Override
    public AuditableRestAction<Void> delete()
    {
        if (isFake())
            throw new IllegalStateException("Fake Webhooks (such as those retrieved from Audit Logs) "
                    + "cannot be used for deletion!");
        Route.CompiledRoute route = Route.Webhooks.DELETE_TOKEN_WEBHOOK.compile(getId(), token);
        return new AuditableRestAction<Void>(getJDA(), route)
        {
            @Override
            protected void handleResponse(Response response, Request<Void> request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    @Override
    public WebhookManager getManager()
    {
        if (isFake())
            throw new IllegalStateException("Fake Webhooks (such as those retrieved from Audit Logs) "
                    + "cannot provide a WebhookManager!");
        WebhookManager mng = manager;
        if (mng == null)
        {
            mng = MiscUtil.locked(mngLock, () ->
            {
                if (manager == null)
                    manager = new WebhookManager(this);
                return manager;
            });
        }
        return mng;
    }

    @Override
    public WebhookClientBuilder newClient()
    {
        if (isFake())
            throw new IllegalStateException("Fake Webhooks (such as those retrieved from Audit Logs) "
                    + "cannot be used to create a WebhookClient!");
        return new WebhookClientBuilder(id, token);
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @Override
    public boolean isFake()
    {
        return token == null;
    }

    /* -- Impl Setters -- */

    public WebhookImpl setOwner(Member member)
    {
        this.owner = member;
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

    /* -- Object Overrides -- */

    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof WebhookImpl
                && ((WebhookImpl) obj).id == this.id;
    }

    @Override
    public String toString()
    {
        return "WH:" + getName() + "(" + id + ")";
    }
}
