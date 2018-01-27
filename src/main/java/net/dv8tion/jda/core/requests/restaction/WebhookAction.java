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

package net.dv8tion.jda.core.requests.restaction;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.Webhook;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.utils.Checks;
import okhttp3.RequestBody;
import org.json.JSONObject;

import javax.annotation.CheckReturnValue;
import java.util.function.BooleanSupplier;

/**
 * {@link net.dv8tion.jda.core.entities.Webhook Webhook} Builder system created as an extension of {@link net.dv8tion.jda.core.requests.RestAction}
 * <br>Provides an easy way to gather and deliver information to Discord to create {@link net.dv8tion.jda.core.entities.Webhook Webhooks}.
 */
public class WebhookAction extends AuditableRestAction<Webhook>
{
    protected String name;
    protected Icon avatar = null;

    public WebhookAction(JDA api, Route.CompiledRoute route, String name)
    {
        super(api, route);
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
    @CheckReturnValue
    public WebhookAction setName(String name)
    {
        Checks.notNull(name, "Webhook name");
        Checks.check(name.length() >= 2 && name.length() <= 100, "The webhook name must be in the range of 2-100!");

        this.name = name;
        return this;
    }

    @Override
    public WebhookAction setCheck(BooleanSupplier checks)
    {
        return (WebhookAction) super.setCheck(checks);
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
    @CheckReturnValue
    public WebhookAction setAvatar(Icon icon)
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
    protected void handleResponse(Response response, Request<Webhook> request)
    {
        if (!response.isOk())
        {
            request.onFailure(response);
            return;
        }
        JSONObject json = response.getObject();
        Webhook webhook = api.getEntityBuilder().createWebhook(json);

        request.onSuccess(webhook);
    }
}
