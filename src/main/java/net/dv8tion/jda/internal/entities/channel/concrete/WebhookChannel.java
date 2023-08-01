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

package net.dv8tion.jda.internal.entities.channel.concrete;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.WebhookClient;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.internal.entities.channel.AbstractChannelImpl;
import net.dv8tion.jda.internal.entities.channel.mixin.middleman.MessageChannelMixin;
import net.dv8tion.jda.internal.requests.RestActionImpl;

import javax.annotation.Nonnull;

public class WebhookChannel extends AbstractChannelImpl<WebhookChannel> implements MessageChannelMixin<WebhookChannel>
{
    private final WebhookClient<?> client;

    public WebhookChannel(WebhookClient<?> client, long id)
    {
        super(id, client.getJDA());
        this.client = client;
        this.name = "";
    }

    @Nonnull
    @Override
    public ChannelType getType()
    {
        return ChannelType.UNKNOWN_WEBHOOK_TARGET;
    }

    @Nonnull
    @Override
    public RestAction<Void> delete()
    {
        Route.CompiledRoute route = Route.Webhooks.DELETE_TOKEN_WEBHOOK.compile(client.getId(), client.getToken());
        return new RestActionImpl<>(api, route);
    }

    @Nonnull
    @Override
    public RestAction<Message> retrieveMessageById(@Nonnull String messageId)
    {
        return client.retrieveMessageById(messageId);
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> deleteMessageById(@Nonnull String messageId)
    {
        return (AuditableRestAction<Void>) client.deleteMessageById(messageId);
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
        return this;
    }

    @Override
    public void checkCanAccessChannel()
    {
        throw new IllegalStateException("Cannot access webhook target channel.");
    }

    @Override
    public void checkCanViewHistory() { } // Kind of? Only specific message ids though.

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
