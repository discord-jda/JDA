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

package net.dv8tion.jda.internal.requests;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.RestFuture;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.JDALogger;
import okhttp3.RequestBody;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class RestActionImpl<T> implements RestAction<T>
{
    public static final Logger LOG = JDALogger.getLog(RestAction.class);

    private static Consumer<Object> DEFAULT_SUCCESS = o -> {};
    private static Consumer<? super Throwable> DEFAULT_FAILURE = t ->
    {
        if (t instanceof CancellationException || t instanceof TimeoutException)
            LOG.debug(t.getMessage());
        else if (LOG.isDebugEnabled() || !(t instanceof ErrorResponseException))
            LOG.error("RestAction queue returned failure", t);
        else if (t.getCause() != null)
            LOG.error("RestAction queue returned failure: [{}] {}", t.getClass().getSimpleName(), t.getMessage(), t.getCause());
        else
            LOG.error("RestAction queue returned failure: [{}] {}", t.getClass().getSimpleName(), t.getMessage());
    };

    protected static boolean passContext = true;
    protected static long defaultTimeout = 0;

    protected final JDAImpl api;

    private final Route.CompiledRoute route;
    private final RequestBody data;
    private final BiFunction<Response, Request<T>, T> handler;

    private boolean priority = false;
    private long deadline = 0;
    private Object rawData;
    private BooleanSupplier checks;

    public static void setPassContext(boolean enable)
    {
        passContext = enable;
    }

    public static boolean isPassContext()
    {
        return passContext;
    }

    public static void setDefaultFailure(final Consumer<? super Throwable> callback)
    {
        DEFAULT_FAILURE = callback == null ? t -> {} : callback;
    }

    public static void setDefaultSuccess(final Consumer<Object> callback)
    {
        DEFAULT_SUCCESS = callback == null ? t -> {} : callback;
    }

    public static void setDefaultTimeout(long timeout, @Nonnull TimeUnit unit)
    {
        Checks.notNull(unit, "TimeUnit");
        defaultTimeout = unit.toMillis(timeout);
    }

    public static long getDefaultTimeout()
    {
        return defaultTimeout;
    }

    public static Consumer<? super Throwable> getDefaultFailure()
    {
        return DEFAULT_FAILURE;
    }

    public static Consumer<Object> getDefaultSuccess()
    {
        return DEFAULT_SUCCESS;
    }

    public RestActionImpl(JDA api, Route.CompiledRoute route)
    {
        this(api, route, (RequestBody) null, null);
    }

    public RestActionImpl(JDA api, Route.CompiledRoute route, DataObject data)
    {
        this(api, route, data, null);
    }

    public RestActionImpl(JDA api, Route.CompiledRoute route, RequestBody data)
    {
        this(api, route, data, null);
    }

    public RestActionImpl(JDA api, Route.CompiledRoute route, BiFunction<Response, Request<T>, T> handler)
    {
        this(api, route, (RequestBody) null, handler);
    }

    public RestActionImpl(JDA api, Route.CompiledRoute route, DataObject data, BiFunction<Response, Request<T>, T> handler)
    {
        this(api, route, data == null ? null : RequestBody.create(Requester.MEDIA_TYPE_JSON, data.toJson()), handler);
        this.rawData = data;
    }

    public RestActionImpl(JDA api, Route.CompiledRoute route, RequestBody data, BiFunction<Response, Request<T>, T> handler)
    {
        Checks.notNull(api, "api");
        this.api = (JDAImpl) api;
        this.route = route;
        this.data = data;
        this.handler = handler;
    }

    public RestActionImpl<T> priority()
    {
        priority = true;
        return this;
    }

    @Nonnull
    @Override
    public JDA getJDA()
    {
        return api;
    }

    @Nonnull
    @Override
    public RestAction<T> setCheck(BooleanSupplier checks)
    {
        this.checks = checks;
        return this;
    }

    @Nullable
    @Override
    public BooleanSupplier getCheck()
    {
        return this.checks;
    }

    @Nonnull
    @Override
    public RestAction<T> deadline(long timestamp)
    {
        this.deadline = timestamp;
        return this;
    }

    @Override
    public void queue(Consumer<? super T> success, Consumer<? super Throwable> failure)
    {
        Route.CompiledRoute route = finalizeRoute();
        Checks.notNull(route, "Route");
        RequestBody data = finalizeData();
        CaseInsensitiveMap<String, String> headers = finalizeHeaders();
        BooleanSupplier finisher = getFinisher();
        if (success == null)
            success = DEFAULT_SUCCESS;
        if (failure == null)
            failure = DEFAULT_FAILURE;
        api.getRequester().request(new Request<>(this, success, failure, finisher, true, data, rawData, getDeadline(), priority, route, headers));
    }

    @Nonnull
    @Override
    public CompletableFuture<T> submit(boolean shouldQueue)
    {
        Route.CompiledRoute route = finalizeRoute();
        Checks.notNull(route, "Route");
        RequestBody data = finalizeData();
        CaseInsensitiveMap<String, String> headers = finalizeHeaders();
        BooleanSupplier finisher = getFinisher();
        return new RestFuture<>(this, shouldQueue, finisher, data, rawData, getDeadline(), priority, route, headers);
    }

    @Override
    public T complete(boolean shouldQueue) throws RateLimitedException
    {
        if (CallbackContext.isCallbackContext())
            throw new IllegalStateException("Preventing use of complete() in callback threads! This operation can be a deadlock cause");
        try
        {
            return submit(shouldQueue).join();
        }
        catch (CompletionException e)
        {
            if (e.getCause() != null)
            {
                Throwable cause = e.getCause();
                if (cause instanceof ErrorResponseException)
                    throw (ErrorResponseException) cause.fillInStackTrace(); // this method will update the stacktrace to the current thread stack
                if (cause instanceof RateLimitedException)
                    throw (RateLimitedException) cause.fillInStackTrace();
                if (cause instanceof RuntimeException)
                    throw (RuntimeException) cause;
                if (cause instanceof Error)
                    throw (Error) cause;
            }
            throw e;
        }
    }

    protected RequestBody finalizeData() { return data; }
    protected Route.CompiledRoute finalizeRoute() { return route; }
    protected CaseInsensitiveMap<String, String> finalizeHeaders() { return null; }
    protected BooleanSupplier finalizeChecks() { return null; }

    protected RequestBody getRequestBody(DataObject object)
    {
        this.rawData = object;

        return object == null ? null : RequestBody.create(Requester.MEDIA_TYPE_JSON, object.toJson());
    }

    protected RequestBody getRequestBody(DataArray array)
    {
        this.rawData = array;

        return array == null ? null : RequestBody.create(Requester.MEDIA_TYPE_JSON, array.toJson());
    }

    private CheckWrapper getFinisher()
    {
        BooleanSupplier pre = finalizeChecks();
        BooleanSupplier wrapped = this.checks;
        return (pre != null || wrapped != null) ? new CheckWrapper(wrapped, pre) : CheckWrapper.EMPTY;
    }

    public void handleResponse(Response response, Request<T> request)
    {
        if (response.isOk())
            handleSuccess(response, request);
        else
            request.onFailure(response);
    }

    protected void handleSuccess(Response response, Request<T> request)
    {
        if (handler == null)
            request.onSuccess(null);
        else
            request.onSuccess(handler.apply(response, request));
    }

    private long getDeadline()
    {
        return deadline > 0
            ? deadline
            : defaultTimeout > 0
                ? System.currentTimeMillis() + defaultTimeout
                : 0;
    }

    /*
        useful for final permission checks:

        @Override
        protected BooleanSupplier finalizeChecks()
        {
            // throw exception, if missing perms
            return () -> hasPermission(Permission.MESSAGE_SEND);
        }
     */
    protected static class CheckWrapper implements BooleanSupplier
    {
        public static final CheckWrapper EMPTY = new CheckWrapper(null, null)
        {
            public boolean getAsBoolean() { return true; }
        };

        protected final BooleanSupplier pre;
        protected final BooleanSupplier wrapped;

        public CheckWrapper(BooleanSupplier wrapped, BooleanSupplier pre)
        {
            this.pre = pre;
            this.wrapped = wrapped;
        }

        public boolean pre()
        {
            return pre == null || pre.getAsBoolean();
        }

        public boolean test()
        {
            return wrapped == null || wrapped.getAsBoolean();
        }

        @Override
        public boolean getAsBoolean()
        {
            return pre() && test();
        }
    }
}
