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

package net.dv8tion.jda.core.requests.restaction;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.EntityBuilder;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.Webhook;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import org.apache.http.util.Args;
import org.json.JSONObject;

/**
 * {@link net.dv8tion.jda.core.entities.Webhook Webhook} Builder system created as an extension of {@link net.dv8tion.jda.core.requests.RestAction}
 * <br>Provides an easy way to gather and deliver information to Discord to create {@link net.dv8tion.jda.core.entities.Webhook Webhooks}.
 */
public class WebhookAction extends RestAction<Webhook>
{

    protected String name;
    protected Icon avatar = null;

    public WebhookAction(JDA api, Route.CompiledRoute route, String name)
    {
        super(api, route, null);
        this.name = name;
    }

    /**
     * Sets the <b>Name</b> for the custom Webhook User
     *
     * @param  name
     *         A not-null String name for the new Webhook user.
     *
     * @throws IllegalArgumentException
     *         If the specified name is not in the range of 2-100.
     *
     * @return The current WebhookAction for chaining convenience.
     */
    public WebhookAction setName(String name)
    {
        Args.notNull(name, "Webhook name");
        if (name.length() < 2 || name.length() > 100)
            throw new IllegalArgumentException("The webhook name must be in the range of 2-100!");

        this.name = name;
        return this;
    }

    /**
     * Sets the <b>Avatar</b> for the custom Webhook User
     *
     * @param  icon
     *         An {@link net.dv8tion.jda.core.entities.Icon Icon} for the new avatar.
     *         Or null to use default avatar.
     *
     * @return The current WebhookAction for chaining convenience.
     */
    public WebhookAction setAvatar(Icon icon)
    {
        this.avatar = icon;
        return this;
    }

    @Override
    public void finalizeData()
    {
        JSONObject data = new JSONObject();
        data.put("name",   name);
        data.put("avatar", avatar != null ? avatar.getEncoding() : JSONObject.NULL);

        super.data = data;
    }

    @Override
    protected void handleResponse(Response response, Request request)
    {
        if (!response.isOk())
        {
            request.onFailure(response);
            return;
        }
        JSONObject json = response.getObject();
        Webhook webhook = EntityBuilder.get(api).createWebhook(json);

        request.onSuccess(webhook);
    }
}
