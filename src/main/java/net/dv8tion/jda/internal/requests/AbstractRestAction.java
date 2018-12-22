/*
 * Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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
import net.dv8tion.jda.api.audit.ThreadLocalReason;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.RestFuture;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.JDALogger;
import net.dv8tion.jda.internal.utils.cache.UpstreamReference;
import okhttp3.RequestBody;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class AbstractRestAction<T> implements RestAction<T>
{
    public static final Logger LOG = JDALogger.getLog(RestAction.class);

    private static Consumer<Object> DEFAULT_SUCCESS = o -> {};
    private static Consumer<? super Throwable> DEFAULT_FAILURE = t ->
    {
        if (LOG.isDebugEnabled())
        {
            LOG.error("RestAction queue returned failure", t);
        }
        else if (t.getCause() != null)
        {
            LOG.error("RestAction queue returned failure: [{}] {}", t.getClass().getSimpleName(), t.getMessage(), t.getCause());
        }
        else
        {
            LOG.error("RestAction queue returned failure: [{}] {}", t.getClass().getSimpleName(), t.getMessage());
        }
    };

    protected static boolean passContext = true;

    protected final UpstreamReference<JDAImpl> api;

    private final Route.CompiledRoute route;
    private final RequestBody data;
    private final BiFunction<Response, Request<T>, T> handler;

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

    public static Consumer<? super Throwable> getDefaultFailure()
    {
        return DEFAULT_FAILURE;
    }

    public static Consumer<Object> getDefaultSuccess()
    {
        return DEFAULT_SUCCESS;
    }

    public AbstractRestAction(JDA api, Route.CompiledRoute route)
    {
        this(api, route, (RequestBody) null, null);
    }

    public AbstractRestAction(JDA api, Route.CompiledRoute route, JSONObject data)
    {
        this(api, route, data, null);
    }

    public AbstractRestAction(JDA api, Route.CompiledRoute route, RequestBody data)
    {
        this(api, route, data, null);
    }

    public AbstractRestAction(JDA api, Route.CompiledRoute route, BiFunction<Response, Request<T>, T> handler)
    {
        this(api, route, (RequestBody) null, handler);
    }

    public AbstractRestAction(JDA api, Route.CompiledRoute route, JSONObject data, BiFunction<Response, Request<T>, T> handler)
    {
        this(api, route, data == null ? null : RequestBody.create(Requester.MEDIA_TYPE_JSON, data.toString()), handler);
        this.rawData = data;
    }

    public AbstractRestAction(JDA api, Route.CompiledRoute route, RequestBody data, BiFunction<Response, Request<T>, T> handler)
    {
        Checks.notNull(api, "api");
        this.api = new UpstreamReference<>((JDAImpl) api);
        this.route = route;
        this.data = data;
        this.handler = handler;
    }

    @Override
    public JDA getJDA()
    {
        return api.get();
    }

    @Override
    public RestAction<T> setCheck(BooleanSupplier checks)
    {
        this.checks = checks;
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
        api.get().getRequester().request(new Request<>(this, success, failure, finisher, true, data, rawData, route, headers));
    }

    @Override
    public CompletableFuture<T> submit(boolean shouldQueue)
    {
        Route.CompiledRoute route = finalizeRoute();
        Checks.notNull(route, "Route");
        RequestBody data = finalizeData();
        CaseInsensitiveMap<String, String> headers = finalizeHeaders();
        BooleanSupplier finisher = getFinisher();
        return new RestFuture<>(this, shouldQueue, finisher, data, rawData, route, headers);
    }

    @Override
    public T complete(boolean shouldQueue) throws RateLimitedException
    {
        if (CallbackContext.isCallbackContext())
            throw new IllegalStateException("Preventing use of complete() in callback threads! This operation can be a deadlock cause");
        try
        {
            return submit(shouldQueue).get();
        }
        catch (Throwable e)
        {
            if (e instanceof ExecutionException)
            {
                Throwable t = e.getCause();
                if (t instanceof RateLimitedException)
                    throw (RateLimitedException) t;
                else if (t instanceof  PermissionException)
                    throw (PermissionException) t;
                else if (t instanceof ErrorResponseException)
                    throw (ErrorResponseException) t;
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    public ScheduledFuture<T> submitAfter(long delay, TimeUnit unit, ScheduledExecutorService executor)
    {
        Checks.notNull(unit, "TimeUnit");
        if (executor == null)
            executor = api.get().getRateLimitPool();
        return executor.schedule((Callable<T>) new ContextRunnable((Callable<T>) this::complete), delay, unit);
    }

    @Override
    public ScheduledFuture<?> queueAfter(long delay, TimeUnit unit, Consumer<? super T> success, Consumer<? super Throwable> failure, ScheduledExecutorService executor)
    {
        Checks.notNull(unit, "TimeUnit");
        if (executor == null)
            executor = api.get().getRateLimitPool();
        return executor.schedule((Runnable) new ContextRunnable(() -> queue(success, failure)), delay, unit);
    }

    protected RequestBody finalizeData() { return data; }
    protected Route.CompiledRoute finalizeRoute() { return route; }
    protected CaseInsensitiveMap<String, String> finalizeHeaders() { return null; }
    protected BooleanSupplier finalizeChecks() { return null; }

    protected RequestBody getRequestBody(JSONObject object)
    {
        this.rawData = object;

        return object == null ? null : RequestBody.create(Requester.MEDIA_TYPE_JSON, object.toString());
    }

    protected RequestBody getRequestBody(JSONArray array)
    {
        this.rawData = array;

        return array == null ? null : RequestBody.create(Requester.MEDIA_TYPE_JSON, array.toString());
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

    /*
        useful for final permission checks:

        @Override
        protected BooleanSupplier finalizeChecks()
        {
            // throw exception, if missing perms
            return () -> hasPermission(Permission.MESSAGE_WRITE);
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

    private class ContextRunnable implements Runnable, Callable<T>
    {
        private final String localReason;
        private final Runnable runnable;
        private final Callable<T> callable;

        protected ContextRunnable(Runnable runnable)
        {
            this.localReason = ThreadLocalReason.getCurrent();
            this.runnable = runnable;
            this.callable = null;
        }

        protected ContextRunnable(Callable<T> callable)
        {
            this.localReason = ThreadLocalReason.getCurrent();
            this.runnable = null;
            this.callable = callable;
        }

        @Override
        public void run()
        {
            try (ThreadLocalReason.Closable __ = ThreadLocalReason.closable(localReason))
            {
                runnable.run();
            }
        }

        @Override
        public T call() throws Exception
        {
            try (ThreadLocalReason.Closable __ = ThreadLocalReason.closable(localReason))
            {
                return callable.call();
            }
        }
    }
}
