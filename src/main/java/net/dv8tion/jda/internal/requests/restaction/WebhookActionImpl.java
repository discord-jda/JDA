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

package net.dv8tion.jda.internal.requests.restaction;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.BaseGuildMessageChannel;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.restaction.WebhookAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;
import okhttp3.RequestBody;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 * {@link net.dv8tion.jda.api.entities.Webhook Webhook} Builder system created as an extension of {@link net.dv8tion.jda.api.requests.RestAction}
 * <br>Provides an easy way to gather and deliver information to Discord to create {@link net.dv8tion.jda.api.entities.Webhook Webhooks}.
 */
public class WebhookActionImpl extends AuditableRestActionImpl<Webhook> implements WebhookAction
{
    protected final BaseGuildMessageChannel channel;
    protected String name;
    protected Icon avatar = null;

    public WebhookActionImpl(JDA api, BaseGuildMessageChannel channel, String name)
    {
        super(api, Route.Channels.CREATE_WEBHOOK.compile(channel.getId()));
        this.channel = channel;
        this.name = name;
    }

    @Nonnull
    @Override
    public WebhookActionImpl setCheck(BooleanSupplier checks)
    {
        return (WebhookActionImpl) super.setCheck(checks);
    }

    @Nonnull
    @Override
    public WebhookActionImpl timeout(long timeout, @Nonnull TimeUnit unit)
    {
        return (WebhookActionImpl) super.timeout(timeout, unit);
    }

    @Nonnull
    @Override
    public WebhookActionImpl deadline(long timestamp)
    {
        return (WebhookActionImpl) super.deadline(timestamp);
    }

    @Nonnull
    @Override
    public BaseGuildMessageChannel getChannel()
    {
        return channel;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public WebhookActionImpl setName(@Nonnull String name)
    {
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, 100, "Name");

        this.name = name;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public WebhookActionImpl setAvatar(Icon icon)
    {
        this.avatar = icon;
        return this;
    }

    @Override
    public RequestBody finalizeData()
    {
        DataObject object = DataObject.empty();
        object.put("name",   name);
        object.put("avatar", avatar != null ? avatar.getEncoding() : null);

        return getRequestBody(object);
    }

    @Override
    protected void handleSuccess(Response response, Request<Webhook> request)
    {
        DataObject json = response.getObject();
        Webhook webhook = api.getEntityBuilder().createWebhook(json);

        request.onSuccess(webhook);
    }
}
