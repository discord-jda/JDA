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
import net.dv8tion.jda.api.entities.Message.MessageFlag;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.internal.utils.message.MessageCreateBuilderMixin;
import okhttp3.RequestBody;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

public class WebhookMessageCreateActionImpl<T>
    extends TriggerRestAction<T>
    implements WebhookMessageCreateAction<T>, MessageCreateBuilderMixin<WebhookMessageCreateAction<T>>
{
    private final MessageCreateBuilder builder = new MessageCreateBuilder();
    private final Function<DataObject, T> transformer;

    private boolean ephemeral;

    public WebhookMessageCreateActionImpl(JDA api, Route.CompiledRoute route, Function<DataObject, T> transformer)
    {
        super(api, route);
        this.transformer = transformer;
    }

    @Override
    public MessageCreateBuilder getBuilder()
    {
        return builder;
    }

    @Nonnull
    @Override
    public WebhookMessageCreateActionImpl<T> setEphemeral(boolean ephemeral)
    {
        this.ephemeral = ephemeral;
        return this;
    }

    @Override
    protected RequestBody finalizeData()
    {
        try (MessageCreateData data = builder.build())
        {
            List<FileUpload> files = data.getFiles();
            DataObject json = data.toData();
            if (ephemeral)
                json.put("flags", json.getInt("flags", 0) | MessageFlag.EPHEMERAL.getValue());

            return getMultipartBody(files, json);
        }
    }

    @Override
    protected void handleSuccess(Response response, Request<T> request)
    {
        T message = transformer.apply(response.getObject());
        request.onSuccess(message);
    }

    @Nonnull
    @Override
    public WebhookMessageCreateAction<T> setCheck(@Nullable BooleanSupplier checks)
    {
        return (WebhookMessageCreateAction<T>) super.setCheck(checks);
    }

    @Nonnull
    @Override
    public WebhookMessageCreateAction<T> deadline(long timestamp)
    {
        return (WebhookMessageCreateAction<T>) super.deadline(timestamp);
    }
}
