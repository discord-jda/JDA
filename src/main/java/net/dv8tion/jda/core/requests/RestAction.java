/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.dv8tion.jda.core.requests;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.exceptions.ErrorResponseException;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.utils.SimpleLog;

import java.util.concurrent.*;
import java.util.function.Consumer;

public abstract class RestAction<T>
{
    public static final SimpleLog LOG = SimpleLog.getLog("RestAction");

    public static final Consumer DEFAULT_SUCCESS = o -> {};
    public static final Consumer<Throwable> DEFAULT_FAILURE = t ->
    {
        LOG.fatal("RestAction queue returned failure: [" + t.getClass().getSimpleName() + "] " + t.getMessage());
        if (LOG.getEffectiveLevel().getPriority() <= SimpleLog.Level.DEBUG.getPriority())
            LOG.log(t);
        if (t instanceof ErrorResponseException)
        {
            ErrorResponseException ex = (ErrorResponseException) t;
            LOG.fatal(ex.getResponse().getString());
        }
    };

    protected final JDAImpl api;
    protected final Route.CompiledRoute route;
    protected final Object data;

    public RestAction(JDA api, Route.CompiledRoute route, Object data)
    {
        this.api = (JDAImpl) api;
        this.route = route;
        this.data = data != null ? data : "";
    }

    public void queue()
    {
        queue(null, null);
    }

    public void queue(Consumer<T> success)
    {
        queue(success, null);
    }

    public void queue(Consumer<T> success, Consumer<Throwable> failure)
    {
        if (success == null)
            success = DEFAULT_SUCCESS;
        if (failure == null)
            failure = DEFAULT_FAILURE;
        api.getRequester().request(new Request<T>(this, success, failure, true));
    }


    public T block() throws RateLimitedException
    {
        CompletableFuture<T> future = new CompletableFuture<T>();
        api.getRequester().request(new Request<T>(this,
                successReturn -> future.complete(successReturn),
                failThrowable -> future.completeExceptionally(failThrowable),
                false));
        try
        {
            return future.get();
        }
        catch (Exception e)
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

    public T block(long timeout, TimeUnit timeUnit) throws RateLimitedException, TimeoutException
    {
        CompletableFuture<T> future = new CompletableFuture<T>();
        api.getRequester().request(new Request<T>(this,
                successReturn -> future.complete(successReturn),
                failThrowable -> future.completeExceptionally(failThrowable),
                false));

        try
        {
            return future.get(timeout, timeUnit);
        }
        catch (Exception e)
        {
            if (e instanceof ExecutionException)
            {
                Throwable t = e.getCause();
                if (t instanceof RateLimitedException)
                    throw (RateLimitedException) t;
                else if (t instanceof PermissionException)
                    throw (PermissionException) t;
                else if (t instanceof ErrorResponseException)
                    throw (ErrorResponseException) t;
            }
            else if (e instanceof TimeoutException)
            {
                throw (TimeoutException) e;
            }
            throw new RuntimeException(e);
        }
    }

    protected abstract void handleResponse(Response response, Request request);
}
