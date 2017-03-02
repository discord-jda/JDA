/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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
import net.dv8tion.jda.core.managers.WebhookManagerUpdatable;
import net.dv8tion.jda.core.requests.*;

/**
 * The implementation for {@link net.dv8tion.jda.core.entities.Webhook Webhook}
 *
 * @since  3.0
 * @author Florian Spieß
 */
public class WebhookImpl implements Webhook
{

    protected volatile WebhookManagerUpdatable managerUpdatable = null;
    protected volatile WebhookManager manager = null;

    private final Object mngLock = new Object();
    private final TextChannel channel;
    private final String id;

    private Member owner;
    private User user;
    private String token;


    public WebhookImpl(TextChannel channel, String id)
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
        return Requester.DISCORD_API_PREFIX + "webhooks/" + getId() + "/" + getToken();
    }

    @Override
    public RestAction<Void> delete()
    {
        Route.CompiledRoute route = Route.Webhooks.DELETE_TOKEN_WEBHOOK.compile(id, token);
        return new RestAction<Void>(getJDA(), route, null)
        {
            @Override
            protected void handleResponse(Response response, Request request)
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

    @Override
    public String getId()
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
        return getId().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof Webhook
                && ((Webhook) obj).getId().equals(id);
    }

    @Override
    public String toString()
    {
        return "WH:" + getName() + "(" + id + ")";
    }
}
