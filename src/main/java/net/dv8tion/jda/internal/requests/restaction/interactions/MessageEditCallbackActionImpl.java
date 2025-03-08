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

package net.dv8tion.jda.internal.requests.restaction.interactions;

import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.dv8tion.jda.internal.interactions.InteractionHookImpl;
import net.dv8tion.jda.internal.utils.message.MessageEditBuilderMixin;
import okhttp3.RequestBody;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

public class MessageEditCallbackActionImpl extends DeferrableCallbackActionImpl implements MessageEditCallbackAction, MessageEditBuilderMixin<MessageEditCallbackAction>
{
    private final MessageEditBuilder builder = new MessageEditBuilder();

    public MessageEditCallbackActionImpl(InteractionHookImpl hook)
    {
        super(hook);
    }

    @Override
    public MessageEditBuilder getBuilder()
    {
        return builder;
    }

    @Nonnull
    @Override
    public MessageEditCallbackActionImpl setCheck(BooleanSupplier checks)
    {
        return (MessageEditCallbackActionImpl) super.setCheck(checks);
    }

    @Nonnull
    @Override
    public MessageEditCallbackActionImpl timeout(long timeout, @Nonnull TimeUnit unit)
    {
        return (MessageEditCallbackActionImpl) super.timeout(timeout, unit);
    }

    @Nonnull
    @Override
    public MessageEditCallbackActionImpl deadline(long timestamp)
    {
        return (MessageEditCallbackActionImpl) super.deadline(timestamp);
    }

    @Nonnull
    @Override
    public MessageEditCallbackActionImpl closeResources()
    {
        builder.closeFiles();
        return this;
    }

    private boolean isEmpty()
    {
        return builder.isEmpty();
    }

    @Override
    protected RequestBody finalizeData()
    {
        DataObject json = DataObject.empty();
        if (isEmpty())
            return getRequestBody(json.put("type", ResponseType.DEFERRED_MESSAGE_UPDATE.getRaw()));
        json.put("type", ResponseType.MESSAGE_UPDATE.getRaw());
        try (MessageEditData data = builder.build())
        {
            json.put("data", data);
            return getMultipartBody(data.getFiles(), data.getAdditionalFiles(), json);
        }
    }
}
