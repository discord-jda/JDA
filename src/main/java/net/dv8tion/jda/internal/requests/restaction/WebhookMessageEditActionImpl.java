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
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.dv8tion.jda.internal.utils.message.MessageEditBuilderMixin;
import okhttp3.RequestBody;

import java.util.function.Function;

public class WebhookMessageEditActionImpl<T>
    extends AbstractWebhookMessageActionImpl<T, WebhookMessageEditActionImpl<T>>
    implements WebhookMessageEditAction<T>, MessageEditBuilderMixin<WebhookMessageEditAction<T>>
{
    private final Function<DataObject, T> transformer;
    private final MessageEditBuilder builder = new MessageEditBuilder();

    public WebhookMessageEditActionImpl(JDA api, Route.CompiledRoute route, Function<DataObject, T> transformer)
    {
        super(api, route);
        this.transformer = transformer;
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
            DataObject payload = data.toData();
            return getMultipartBody(data.getFiles(), data.getAdditionalFiles(), payload);
        }
    }

    @Override
    protected Route.CompiledRoute finalizeRoute()
    {
        Route.CompiledRoute route = super.finalizeRoute();
        if (threadId != null)
            route = route.withQueryParams("thread_id", threadId);
        return route;
    }

    @Override
    protected void handleSuccess(Response response, Request<T> request)
    {
        T message = transformer.apply(response.getObject());
        request.onSuccess(message);
    }
}
