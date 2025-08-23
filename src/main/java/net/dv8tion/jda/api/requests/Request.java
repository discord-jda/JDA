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

package net.dv8tion.jda.api.requests;

import net.dv8tion.jda.api.audit.ThreadLocalReason;
import net.dv8tion.jda.api.events.ExceptionEvent;
import net.dv8tion.jda.api.events.http.HttpRequestEvent;
import net.dv8tion.jda.api.exceptions.ContextException;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.requests.CallbackContext;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.utils.IOUtil;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Internal class used for representing HTTP requests.
 *
 * @param <T> The expected type of the response
 */
public class Request<T>
{
    private final JDAImpl api;
    private final RestActionImpl<T> restAction;
    private final Consumer<? super T> onSuccess;
    private final Consumer<? super Throwable> onFailure;
    private final BooleanSupplier checks;
    private final boolean shouldQueue;
    private final Route.CompiledRoute route;
    private final RequestBody body;
    private final Object rawBody;
    private final CaseInsensitiveMap<String, String> headers;
    private final long deadline;
    private final boolean priority;

    private final String localReason;

    private boolean done = false;
    private boolean isCancelled = false;

    public Request(
            RestActionImpl<T> restAction, Consumer<? super T> onSuccess, Consumer<? super Throwable> onFailure,
            BooleanSupplier checks, boolean shouldQueue, RequestBody body, Object rawBody, long deadline, boolean priority,
            Route.CompiledRoute route, CaseInsensitiveMap<String, String> headers)
    {
        this.deadline = deadline;
        this.priority = priority;
        this.restAction = restAction;
        this.onSuccess = onSuccess;
        if (onFailure instanceof ContextException.ContextConsumer)
            this.onFailure = onFailure;
        else if (RestActionImpl.isPassContext())
            this.onFailure = ContextException.here(onFailure);
        else
            this.onFailure = onFailure;
        this.checks = checks;
        this.shouldQueue = shouldQueue;
        this.body = body;
        this.rawBody = rawBody;
        this.route = route;
        this.headers = headers;

        this.api = (JDAImpl) restAction.getJDA();
        this.localReason = ThreadLocalReason.getCurrent();
    }

    private void cleanup()
    {
        // Try closing any open request bodies that were never read from
        if (body instanceof MultipartBody)
        {
            MultipartBody multi = (MultipartBody) body;
            multi.parts()
                 .stream()
                 .map(MultipartBody.Part::body)
                 .filter(AutoCloseable.class::isInstance)
                 .map(AutoCloseable.class::cast)
                 .forEach(IOUtil::silentClose);
        }
        else if (body instanceof AutoCloseable)
        {
            IOUtil.silentClose((AutoCloseable) body);
        }
    }

    public void onSuccess(@Nullable T successObj)
    {
        if (done)
            return;
        done = true;
        cleanup();
        RestActionImpl.LOG.trace("Scheduling success callback for request with route {}/{}", route.getMethod(), route.getCompiledRoute());
        api.getCallbackPool().execute(() ->
        {
            try (ThreadLocalReason.Closable __ = ThreadLocalReason.closable(localReason);
                 CallbackContext ___ = CallbackContext.getInstance())
            {
                RestActionImpl.LOG.trace("Running success callback for request with route {}/{}", route.getMethod(), route.getCompiledRoute());
                onSuccess.accept(successObj);
            }
            catch (Throwable t)
            {
                RestActionImpl.LOG.error("Encountered error while processing success consumer", t);
                if (t instanceof Error)
                {
                    api.handleEvent(new ExceptionEvent(api, t, true));
                    throw (Error) t;
                }
            }
        });
    }

    public void onFailure(@Nonnull Response response)
    {
        if (response.code == 429)
        {
            onRateLimited(response);
        }
        else
        {
            onFailure(createErrorResponseException(response));
        }
    }

    public void onRateLimited(@Nonnull Response response)
    {
        onFailure(new RateLimitedException(route, response.retryAfter));
    }

    @Nonnull
    public ErrorResponseException createErrorResponseException(@Nonnull Response response)
    {
        return ErrorResponseException.create(
                ErrorResponse.fromJSON(response.optObject().orElse(null)), response);
    }

    public void onFailure(@Nonnull Throwable failException)
    {
        if (done)
            return;
        done = true;
        cleanup();
        RestActionImpl.LOG.trace("Scheduling failure callback for request with route {}/{}", route.getMethod(), route.getCompiledRoute());
        api.getCallbackPool().execute(() ->
        {
            try (ThreadLocalReason.Closable __ = ThreadLocalReason.closable(localReason);
                 CallbackContext ___ = CallbackContext.getInstance())
            {
                RestActionImpl.LOG.trace("Running failure callback for request with route {}/{}", route.getMethod(), route.getCompiledRoute());
                onFailure.accept(failException);
                if (failException instanceof Error)
                    api.handleEvent(new ExceptionEvent(api, failException, false));
            }
            catch (Throwable t)
            {
                RestActionImpl.LOG.error("Encountered error while processing failure consumer", t);
                if (t instanceof Error)
                {
                    api.handleEvent(new ExceptionEvent(api, t, true));
                    throw (Error) t;
                }
            }
        });
    }

    public void onCancelled()
    {
        onFailure(new CancellationException("RestAction has been cancelled"));
    }

    public void onTimeout()
    {
        onFailure(new TimeoutException("RestAction has timed out"));
    }

    @Nonnull
    public JDAImpl getJDA()
    {
        return api;
    }

    @Nonnull
    @CheckReturnValue
    public RestAction<T> getRestAction()
    {
        return restAction;
    }

    @Nonnull
    public Consumer<? super T> getOnSuccess()
    {
        return onSuccess;
    }

    @Nonnull
    public Consumer<? super Throwable> getOnFailure()
    {
        return onFailure;
    }

    public boolean isPriority()
    {
        return priority;
    }

    public boolean isSkipped()
    {
        if (isTimeout())
        {
            onTimeout();
            return true;
        }
        boolean skip = runChecks();
        if (skip)
            onCancelled();
        return skip;
    }

    private boolean isTimeout()
    {
        return deadline > 0 && deadline < System.currentTimeMillis();
    }

    private boolean runChecks()
    {
        try
        {
            return isCancelled() || (checks != null && !checks.getAsBoolean());
        }
        catch (Exception e)
        {
            onFailure(e);
            return true;
        }
    }

    @Nullable
    public CaseInsensitiveMap<String, String> getHeaders()
    {
        return headers;
    }

    @Nonnull
    public Route.CompiledRoute getRoute()
    {
        return route;
    }

    @Nullable
    public RequestBody getBody()
    {
        return body;
    }

    @Nullable
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
        if (!this.isCancelled)
            onCancelled();
        this.isCancelled = true;
    }

    public boolean isCancelled()
    {
        return isCancelled;
    }

    public void handleResponse(@Nonnull Response response)
    {
        RestActionImpl.LOG.trace("Handling response for request with route {}/{} and code {}", route.getMethod(), route.getCompiledRoute(), response.code);
        restAction.handleResponse(response, this);
        api.handleEvent(new HttpRequestEvent(this, response));
    }
}
