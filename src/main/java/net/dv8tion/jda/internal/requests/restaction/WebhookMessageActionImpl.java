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
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.message.MessageCreateBuilderMixin;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

public class WebhookMessageActionImpl<T>
    extends TriggerRestAction<T>
    implements WebhookMessageAction<T>, MessageCreateBuilderMixin<WebhookMessageAction<T>>
{
    private final MessageCreateBuilder builder = new MessageCreateBuilder();
    private final Function<DataObject, T> transformer;

    private boolean ephemeral;

    public WebhookMessageActionImpl(JDA api, Route.CompiledRoute route, Function<DataObject, T> transformer)
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
    public WebhookMessageActionImpl<T> setEphemeral(boolean ephemeral)
    {
        this.ephemeral = ephemeral;
        return this;
    }

    @Override
    protected RequestBody finalizeData()
    {
        MessageCreateData data = builder.build();
        try
        {
            List<FileUpload> files = data.getFiles();
            DataObject json = data.toData();
            if (ephemeral)
                json.put("flags", json.getInt("flags", 0) | 64);
            if (files.isEmpty())
                return getRequestBody(json);

            MultipartBody.Builder body = AttachedFile.createMultipartBody(files, null);
            body.addFormDataPart("payload_json", json.toString());
            files.clear();
            return body.build();
        }
        catch (Exception e)
        {
            data.close();
            throw e;
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
    public WebhookMessageAction<T> setCheck(@Nullable BooleanSupplier checks)
    {
        return (WebhookMessageAction<T>) super.setCheck(checks);
    }

    @NotNull
    @Override
    public WebhookMessageAction<T> deadline(long timestamp)
    {
        return (WebhookMessageAction<T>) super.deadline(timestamp);
    }
}
