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

import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.internal.interactions.InteractionHookImpl;
import net.dv8tion.jda.internal.utils.message.MessageCreateBuilderMixin;
import okhttp3.RequestBody;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import static net.dv8tion.jda.api.entities.Message.MessageFlag.EPHEMERAL;

public class ReplyCallbackActionImpl extends DeferrableCallbackActionImpl implements ReplyCallbackAction, MessageCreateBuilderMixin<ReplyCallbackAction>
{
    private final MessageCreateBuilder builder = new MessageCreateBuilder();
    private int flags;

    public ReplyCallbackActionImpl(InteractionHookImpl hook)
    {
        super(hook);
    }

    @Override
    public MessageCreateBuilder getBuilder()
    {
        return builder;
    }

    @Nonnull
    @Override
    public ReplyCallbackActionImpl closeResources()
    {
        builder.closeFiles();
        return this;
    }

    @Nonnull
    protected RequestBody finalizeData()
    {
        DataObject json = DataObject.empty();
        if (builder.isEmpty())
        {
            json.put("type", ResponseType.DEFERRED_CHANNEL_MESSAGE_WITH_SOURCE.getRaw());
            if (flags != 0)
                json.put("data", DataObject.empty().put("flags", flags));
            return getRequestBody(json);
        }

        json.put("type", ResponseType.CHANNEL_MESSAGE_WITH_SOURCE.getRaw());
        try (MessageCreateData data = builder.build())
        {
            DataObject msg = data.toData();
            msg.put("flags", msg.getInt("flags", 0) | flags);
            json.put("data",msg);
            return getMultipartBody(data.getFiles(), data.getAdditionalFiles(), json);
        }
    }

    @Nonnull
    @Override
    public ReplyCallbackActionImpl setEphemeral(boolean ephemeral)
    {
        int flag = EPHEMERAL.getValue();
        if (ephemeral)
            this.flags |= flag;
        else
            this.flags &= ~flag;
        return this;
    }

    @Nonnull
    @Override
    public ReplyCallbackAction setCheck(BooleanSupplier checks)
    {
        return (ReplyCallbackAction) super.setCheck(checks);
    }

    @Nonnull
    @Override
    public ReplyCallbackAction timeout(long timeout, @Nonnull TimeUnit unit)
    {
        return (ReplyCallbackAction) super.timeout(timeout, unit);
    }

    @Nonnull
    @Override
    public ReplyCallbackAction deadline(long timestamp)
    {
        return (ReplyCallbackAction) super.deadline(timestamp);
    }
}
