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

package net.dv8tion.jda.internal.requests;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.AbstractWebhookClient;
import net.dv8tion.jda.internal.entities.channel.AbstractChannelImpl;
import net.dv8tion.jda.internal.entities.channel.mixin.middleman.MessageChannelMixin;
import net.dv8tion.jda.internal.requests.restaction.WebhookMessageCreateActionImpl;
import net.dv8tion.jda.internal.requests.restaction.WebhookMessageEditActionImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class IncomingWebhookClient extends AbstractWebhookClient<Message>
{
    public IncomingWebhookClient(long webhookId, String webhookToken, JDA api)
    {
        super(webhookId, webhookToken, api);
    }

    @Override
    public WebhookMessageCreateActionImpl<Message> sendRequest()
    {
        Route.CompiledRoute route = Route.Webhooks.EXECUTE_WEBHOOK.compile(Long.toUnsignedString(id), token);
        route = route.withQueryParams("wait", "true");
        WebhookMessageCreateActionImpl<Message> action = new WebhookMessageCreateActionImpl<>(api, route, builder());
        action.run();
        return action;
    }

    @Override
    public WebhookMessageEditActionImpl<Message> editRequest(String messageId)
    {
        Route.CompiledRoute route = Route.Webhooks.EXECUTE_WEBHOOK_EDIT.compile(Long.toUnsignedString(id), token, messageId);
        route = route.withQueryParams("wait", "true");
        WebhookMessageEditActionImpl<Message> action = new WebhookMessageEditActionImpl<>(api, route, builder());
        action.run();
        return action;
    }

    @Nonnull
    @Override
    public RestAction<Message> retrieveMessageById(@Nonnull String messageId)
    {
        Checks.isSnowflake(messageId);
        Route.CompiledRoute route = Route.Interactions.GET_MESSAGE.compile(Long.toUnsignedString(id), token, messageId);
        return new RestActionImpl<>(api, route, (response, request) -> builder().apply(response.getObject()));
    }

    private Function<DataObject, Message> builder()
    {
        return (data) -> {
            JDAImpl jda = (JDAImpl) api;
            long channelId = data.getUnsignedLong("channel_id");
            MessageChannel channel = api.getChannelById(MessageChannel.class, channelId);
            if (channel == null)
                channel = new WebhookChannel(channelId);
            return jda.getEntityBuilder().createMessageWithChannel(data, channel, false);
        };
    }

    public class WebhookChannel extends AbstractChannelImpl<WebhookChannel> implements MessageChannelMixin<WebhookChannel>
    {
        WebhookChannel(long id)
        {
            super(id, IncomingWebhookClient.this.api);
            this.name = "";
        }

        @Override
        public long getIdLong()
        {
            return id;
        }

        @Nonnull
        @Override
        public ChannelType getType()
        {
            return ChannelType.WEBHOOK;
        }

        @Nonnull
        @Override
        public RestAction<Void> delete()
        {
            Route.CompiledRoute route = Route.Webhooks.DELETE_TOKEN_WEBHOOK.compile(Long.toUnsignedString(IncomingWebhookClient.this.id), token);
            return new RestActionImpl<>(api, route);
        }

        @Nonnull
        @Override
        public RestAction<Message> retrieveMessageById(@Nonnull String messageId)
        {
            return IncomingWebhookClient.this.retrieveMessageById(messageId);
        }

        @Nonnull
        @Override
        public AuditableRestAction<Void> deleteMessageById(@Nonnull String messageId)
        {
            return (AuditableRestAction<Void>) IncomingWebhookClient.this.deleteMessageById(messageId);
        }

        @Override
        public long getLatestMessageIdLong()
        {
            return 0;
        }

        @Override
        public boolean canTalk()
        {
            return true;
        }

        @Override
        public WebhookChannel setLatestMessageIdLong(long latestMessageId)
        {
            return null;
        }

        @Override
        public void checkCanAccessChannel()
        {
            throw new IllegalStateException("Cannot access webhook target channel.");
        }

        @Override
        public void checkCanViewHistory() {} // Kind of? Only specific message ids though.

        @Override
        public void checkCanSendMessage()
        {
            checkCanAccessChannel();
        }

        @Override
        public void checkCanSendMessageEmbeds()
        {
            checkCanAccessChannel();
        }

        @Override
        public void checkCanSendFiles()
        {
            checkCanAccessChannel();
        }

        @Override
        public void checkCanAddReactions()
        {
            checkCanAccessChannel();
        }

        @Override
        public void checkCanRemoveReactions()
        {
            checkCanAccessChannel();
        }

        @Override
        public void checkCanControlMessagePins()
        {
            checkCanAccessChannel();
        }

        @Override
        public boolean canDeleteOtherUsersMessages()
        {
            return false;
        }
    }
}
