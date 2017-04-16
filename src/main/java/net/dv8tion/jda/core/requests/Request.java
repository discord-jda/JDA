/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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
import net.dv8tion.jda.core.exceptions.FailureResponseException;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

import java.util.function.Consumer;

public class Request<T>
{
    private final JDAImpl api;
    private final RestAction<T> restAction;
    private final Object data;
    private final Consumer<T> onSuccess;
    private final Consumer<Throwable> onFailure;
    private final boolean shouldQueue;

    private boolean isCanceled = false;

    public Request(RestAction<T> restAction, Consumer<T> onSuccess, Consumer<Throwable> onFailure, boolean shouldQueue)
    {
        this.restAction = restAction;
        this.data = restAction.data;
        this.onSuccess = onSuccess;
        this.onFailure = onFailure;
        this.shouldQueue = shouldQueue;
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
            onFailure(new RateLimitedException(getRoute(), response.retryAfter));
        }
        else
        {
            onFailure(new FailureResponseException(
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

    public Route.CompiledRoute getRoute()
    {
        return restAction.route;
    }

    public Object getData()
    {
        return data;
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
}
