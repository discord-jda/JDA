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

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.managers.WebhookManager;
import net.dv8tion.jda.core.managers.WebhookManagerUpdatable;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Requester;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.webhook.WebhookClientBuilder;

/**
 * The implementation for {@link net.dv8tion.jda.core.entities.Webhook Webhook}
 *
 * @since  3.0
 */
public class WebhookImpl implements Webhook
{
    protected volatile WebhookManagerUpdatable managerUpdatable = null;
    protected volatile WebhookManager manager = null;

    private final Object mngLock = new Object();
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

    @NonNull
    @Override
    public JDA getJDA()
    {
        return channel.getJDA();
    }

    @NonNull
    @Override
    public Guild getGuild()
    {
        return channel.getGuild();
    }

    @NonNull
    @Override
    public TextChannel getChannel()
    {
        return channel;
    }

    @NonNull
    @Override
    public Member getOwner()
    {
        return owner;
    }

    @NonNull
    @Override
    public User getDefaultUser()
    {
        return user;
    }

    @NonNull
    @Override
    public String getName()
    {
        return user.getName();
    }

    @NonNull
    @Override
    public String getToken()
    {
        return token;
    }

    @NonNull
    @Override
    public String getUrl()
    {
        return Requester.DISCORD_API_PREFIX + "webhooks/" + getId() + "/" + getToken();
    }

    @NonNull
    @Override
    public AuditableRestAction<Void> delete()
    {
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

    @NonNull
    @Override
    public WebhookManager getManager()
    {
        WebhookManager mng = manager;
        if (mng == null)
        {
            synchronized (mngLock)
            {
                mng = manager;
                if (mng == null)
                    mng = manager = new WebhookManager(this);
            }
        }
        return mng;
    }

    @NonNull
    @Override
    public WebhookManagerUpdatable getManagerUpdatable()
    {
        WebhookManagerUpdatable mng = managerUpdatable;
        if (mng == null)
        {
            synchronized (mngLock)
            {
                mng = managerUpdatable;
                if (mng == null)
                    mng = managerUpdatable = new WebhookManagerUpdatable(this);
            }
        }
        return mng;
    }

    @NonNull
    @Override
    public WebhookClientBuilder newClient()
    {
        return new WebhookClientBuilder(id, token);
    }

    @Override
    public long getIdLong()
    {
        return id;
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
    public boolean equals(@Nullable Object obj)
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
