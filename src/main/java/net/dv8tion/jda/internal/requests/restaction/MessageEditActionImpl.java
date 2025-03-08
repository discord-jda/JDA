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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.WebhookClient;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.entities.ReceivedMessage;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.utils.message.MessageEditBuilderMixin;
import okhttp3.RequestBody;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BooleanSupplier;

public class MessageEditActionImpl extends RestActionImpl<Message> implements MessageEditAction, MessageEditBuilderMixin<MessageEditAction>
{
    private final String messageId;
    private final Guild guild;
    private final MessageChannel channel;
    private final MessageEditBuilder builder = new MessageEditBuilder();
    private WebhookClient<Message> webhook;
    private String threadId;

    public MessageEditActionImpl(@Nonnull JDA jda, @Nullable Guild guild, @Nonnull String channelId, @Nonnull String messageId)
    {
        super(jda, Route.Messages.EDIT_MESSAGE.compile(channelId, messageId));
        this.channel = null;
        this.guild = guild;
        this.messageId = messageId;
    }

    public MessageEditActionImpl(@Nonnull MessageChannel channel, @Nonnull String messageId)
    {
        super(channel.getJDA(), Route.Messages.EDIT_MESSAGE.compile(channel.getId(), messageId));
        this.channel = channel;
        this.guild = channel instanceof GuildChannel ? ((GuildChannel) channel).getGuild() : null;
        this.messageId = messageId;
    }

    public MessageEditActionImpl withHook(WebhookClient<Message> hook, ChannelType channelType, long channelId)
    {
        this.webhook = hook;
        if (!(hook instanceof InteractionHook) && channelType.isThread())
            this.threadId = Long.toUnsignedString(channelId);
        return this;
    }

    @Override
    public MessageEditBuilder getBuilder()
    {
        return builder;
    }

    @Override
    protected Route.CompiledRoute finalizeRoute()
    {
        if (webhook != null && (!(webhook instanceof InteractionHook) || !((InteractionHook) webhook).isExpired()))
        {
            Route.CompiledRoute route = Route.Webhooks.EXECUTE_WEBHOOK_EDIT.compile(webhook.getId(), webhook.getToken(), messageId);
            if (this.threadId != null)
                route = route.withQueryParams("thread_id", threadId);

            return route;
        }

        return super.finalizeRoute();
    }

    @Override
    protected RequestBody finalizeData()
    {
        try (MessageEditData data = builder.build())
        {
            return getMultipartBody(data.getFiles(), data.getAdditionalFiles(), data.toData());
        }
    }

    @Override
    protected void handleSuccess(Response response, Request<Message> request)
    {
        EntityBuilder entityBuilder = api.getEntityBuilder();
        DataObject json = response.getObject();
        ReceivedMessage message = entityBuilder.createMessageBestEffort(json, channel, guild);
        request.onSuccess(message.withHook(webhook));
    }

    @Nonnull
    @Override
    public MessageEditAction setCheck(BooleanSupplier checks)
    {
        return (MessageEditAction) super.setCheck(checks);
    }

    @Nonnull
    @Override
    public MessageEditAction deadline(long timestamp)
    {
        return (MessageEditAction) super.deadline(timestamp);
    }
}
