/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.core.requests;

import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.http.HttpRequestEvent;
import okhttp3.RequestBody;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.util.function.Consumer;

public class Request<T>
{
    private final JDAImpl api;
    private final RestAction<T> restAction;
    private final Consumer<Response> onSuccess;
    private final Consumer<Response> onFailure;
    private final boolean shouldQueue;
    private final Route.CompiledRoute route;
    private final RequestBody body;
    private final Object rawBody;
    private final CaseInsensitiveMap<String, String> headers;

    private boolean isCanceled = false;

    public Request(RestAction<T> restAction, Consumer<Response> onSuccess, Consumer<Response> onFailure, boolean shouldQueue, RequestBody body, Object rawBody, Route.CompiledRoute route, CaseInsensitiveMap<String, String> headers)
    {
        this.restAction = restAction;
        this.onSuccess = onSuccess;
        this.onFailure = onFailure;
        this.shouldQueue = shouldQueue;
        this.body = body;
        this.rawBody = rawBody;
        this.route = route;
        this.headers = headers;

        this.api = (JDAImpl) restAction.getJDA();
    }

    public JDAImpl getJDA()
    {
        return api;
    }

    public RestAction<T> getRestAction()
    {
        return restAction;
    }

    public CaseInsensitiveMap<String, String> getHeaders()
    {
        return headers;
    }

    public Route.CompiledRoute getRoute()
    {
        return route;
    }

    public RequestBody getBody()
    {
        return body;
    }

    public Object getRawBody()
    {
        return rawBody;
    }

    public boolean shouldQueue()
    {
        return shouldQueue;
    }

    public void cancel()
    {
        this.isCanceled = true;
    }

    public boolean isCanceled()
    {
        return isCanceled;
    }

    public void handleResponse(Response response)
    {
        api.pool.execute(() ->
        {
            api.getEventManager().handle(new HttpRequestEvent(this, response));

            if (response.isOk())
            {
                onSuccess.accept(response);
            }
            else
            {
                onFailure.accept(response);
            }
        });
    }
}
