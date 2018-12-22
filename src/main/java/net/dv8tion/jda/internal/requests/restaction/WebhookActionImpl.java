/*
 * Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.restaction.WebhookAction;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;
import okhttp3.RequestBody;
import org.json.JSONObject;

import javax.annotation.CheckReturnValue;
import java.util.function.BooleanSupplier;

/**
 * {@link net.dv8tion.jda.api.entities.Webhook Webhook} Builder system created as an extension of {@link net.dv8tion.jda.api.requests.RestAction}
 * <br>Provides an easy way to gather and deliver information to Discord to create {@link net.dv8tion.jda.api.entities.Webhook Webhooks}.
 */
public class WebhookActionImpl extends AuditableRestActionImpl<Webhook> implements WebhookAction
{
    protected String name;
    protected Icon avatar = null;

    public WebhookActionImpl(JDA api, Route.CompiledRoute route, String name)
    {
        super(api, route);
        this.name = name;
    }

    @Override
    public WebhookActionImpl setCheck(BooleanSupplier checks)
    {
        return (WebhookActionImpl) super.setCheck(checks);
    }

    @Override
    @CheckReturnValue
    public WebhookActionImpl setName(String name)
    {
        Checks.notNull(name, "Webhook name");
        Checks.check(name.length() >= 2 && name.length() <= 100, "The webhook name must be in the range of 2-100!");

        this.name = name;
        return this;
    }

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
        JSONObject object = new JSONObject();
        object.put("name",   name);
        object.put("avatar", avatar != null ? avatar.getEncoding() : JSONObject.NULL);

        return getRequestBody(object);
    }

    @Override
    protected void handleSuccess(Response response, Request<Webhook> request)
    {
        JSONObject json = response.getObject();
        Webhook webhook = api.get().getEntityBuilder().createWebhook(json);

        request.onSuccess(webhook);
    }
}
