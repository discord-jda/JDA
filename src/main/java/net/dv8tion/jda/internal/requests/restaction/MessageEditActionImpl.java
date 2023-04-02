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

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.utils.message.MessageEditBuilderMixin;
import okhttp3.RequestBody;

import javax.annotation.Nonnull;
import java.util.function.BooleanSupplier;

public class MessageEditActionImpl extends RestActionImpl<Message> implements MessageEditAction, MessageEditBuilderMixin<MessageEditAction>
{
    private final MessageChannel channel;
    private final MessageEditBuilder builder = new MessageEditBuilder();

    public MessageEditActionImpl(MessageChannel channel, String messageId)
    {
        super(channel.getJDA(), Route.Messages.EDIT_MESSAGE.compile(channel.getId(), messageId));
        this.channel = channel;
    }

    @Override
    public MessageEditBuilder getBuilder()
    {
        return builder;
    }

    @Override
    protected RequestBody finalizeData()
    {
        try (MessageEditData data = builder.build())
        {
            return getMultipartBody(data.getFiles(), data.toData());
        }
    }

    @Override
    protected void handleSuccess(Response response, Request<Message> request)
    {
        request.onSuccess(api.getEntityBuilder().createMessageWithChannel(response.getObject(), channel, false));
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
