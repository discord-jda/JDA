/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
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

package net.dv8tion.jda.core.managers;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.Webhook;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.managers.fields.WebhookField;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import org.json.JSONObject;

public class WebhookManagerUpdatable
{

    protected final Webhook webhook;

    protected WebhookField<String> name;
    protected WebhookField<Icon> avatar;
    protected WebhookField<TextChannel> channel;

    public WebhookManagerUpdatable(Webhook webhook)
    {
        this.webhook = webhook;
        setupFields();
    }

    public JDA getJDA()
    {
        return webhook.getJDA();
    }

    public Webhook getWebhook()
    {
        return webhook;
    }

    public WebhookField<String> getNameField()
    {
        return name;
    }

    public WebhookField<Icon> getAvatarField()
    {
        return avatar;
    }

    public WebhookField<TextChannel> getChannelField()
    {
        return channel;
    }

    public void reset()
    {
        name.reset();
        avatar.reset();
        channel.reset();
    }

    public RestAction<Void> update()
    {
        if (!webhook.getGuild().getSelfMember().hasPermission(webhook.getChannel(), Permission.MANAGE_WEBHOOKS))
            throw new PermissionException(Permission.MANAGE_WEBHOOKS);
        if (!shouldUpdate())
            return new RestAction.EmptyRestAction<>(null);

        JSONObject data = new JSONObject();
        data.put("name", name.getOriginalValue());

        if (channel.shouldUpdate())
            data.put("channel_id", channel.getValue().getId());
        if (name.shouldUpdate())
            data.put("name", name.getValue());
        if (avatar.shouldUpdate())
        {
            Icon value = avatar.getValue();
            data.put("avatar", value != null ? value.getEncoding() : JSONObject.NULL);
        }

        Route.CompiledRoute route = Route.Webhooks.MODIFY_WEBHOOK.compile(webhook.getId());
        return new RestAction<Void>(getJDA(), route, data)
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

    protected boolean shouldUpdate()
    {
        return name.shouldUpdate()
                || avatar.shouldUpdate()
                || channel.shouldUpdate();
    }

    protected void setupFields()
    {
        name = new WebhookField<String>(this, webhook::getName)
        {
            @Override
            public void checkValue(String value)
            {
                checkNull(value, "default name");
            }
        };

        avatar = new WebhookField<Icon>(this, null)
        {
            @Override
            public void checkValue(Icon value)
            {
                checkNull(value, "default avatar icon");
            }

            @Override
            public Icon getOriginalValue()
            {
                throw new UnsupportedOperationException("Cannot easily provide the original Avatar. Use User#getIconUrl() and download it yourself.");
            }

            @Override
            public boolean shouldUpdate()
            {
                return isSet();
            }
        };

        channel = new WebhookField<TextChannel>(this, webhook::getChannel)
        {
            @Override
            public void checkValue(TextChannel value)
            {
                checkNull(value, "channel");
            }
        };
    }
}
