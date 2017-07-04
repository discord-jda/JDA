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
import net.dv8tion.jda.core.events.ExceptionEvent;
import net.dv8tion.jda.core.events.http.HttpRequestEvent;
import net.dv8tion.jda.core.exceptions.ErrorResponseException;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import okhttp3.RequestBody;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.util.function.Consumer;

public class Request<T>
{
    private final JDAImpl api;
    private final RestAction<T> restAction;
    private final Consumer<T> onSuccess;
    private final Consumer<Throwable> onFailure;
    private final boolean shouldQueue;
    private final Route.CompiledRoute route;
    private final RequestBody body;
    private final Object rawBody;
    private final CaseInsensitiveMap<String, String> headers;

    private boolean isCanceled = false;

    public Request(RestAction<T> restAction, Consumer<T> onSuccess, Consumer<Throwable> onFailure, boolean shouldQueue, RequestBody body, Object rawBody, Route.CompiledRoute route, CaseInsensitiveMap<String, String> headers)
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

    public void onSuccess(T successObj)
    {
        api.pool.execute(() ->
        {
            try
            {
                onSuccess.accept(successObj);
            }
            catch (Throwable t)
            {
                RestAction.LOG.fatal("Encountered error while processing success consumer");
                RestAction.LOG.log(t);
                if (t instanceof Error)
                    api.getEventManager().handle(new ExceptionEvent(api, t, true));
            }
        });
    }

    public void onFailure(Response response)
    {
        if (response.code == 429)
        {
            onFailure(new RateLimitedException(route, response.retryAfter));
        }
        else
        {
            onFailure(ErrorResponseException.create(
                    ErrorResponse.fromJSON(response.getObject()), response));
        }
    }

    public void onFailure(Throwable failException)
    {
        api.pool.execute(() ->
        {
            try
            {
                onFailure.accept(failException);
                if (failException instanceof Error)
                    api.getEventManager().handle(new ExceptionEvent(api, failException, false));
            }
            catch (Throwable t)
            {
                RestAction.LOG.fatal("Encountered error while processing failure consumer");
                RestAction.LOG.log(t);
                if (t instanceof Error)
                    api.getEventManager().handle(new ExceptionEvent(api, t, true));
            }
        });
    }

    public JDAImpl getJDA()
    {
        return api;
    }

    public RestAction<T> getRestAction()
    {
        return restAction;
    }

    public Consumer<T> getOnSuccess()
    {
        return onSuccess;
    }

    public Consumer<Throwable> getOnFailure()
    {
        return onFailure;
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
        api.getEventManager().handle(new HttpRequestEvent(this, response));
        restAction.handleResponse(response, this);
        response.close();
    }
}
