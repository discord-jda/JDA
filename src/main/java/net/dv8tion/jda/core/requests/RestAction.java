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

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.exceptions.ErrorResponseException;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.requests.restaction.CompletedFuture;
import net.dv8tion.jda.core.requests.restaction.RequestFuture;
import net.dv8tion.jda.core.utils.SimpleLog;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * A class representing a terminal between the user and the discord API.
 * <br>This is used to offer users the ability to decide how JDA should limit a Request.
 *
 * <p>Methods that return an instance of RestAction require an additional step
 * to complete the execution. Thus the user needs to append a follow-up method.
 *
 * <p>A default RestAction is issued with the following operations:
 * <ul>
 *     <li>{@link #queue()}, {@link #queue(Consumer)}, {@link #queue(Consumer, Consumer)}
 *     <br>The fastest and most simplistic way to execute a RestAction is to queue it.
 *     <br>This method has two optional callback functions, one with the generic type and another with a failure exception.</li>
 *
 *     <li>{@link #submit()}, {@link #submit(boolean)}
 *     <br>Provides a Future representing the pending request.
 *     <br>An optional parameter of type boolean can be passed to disable automated rate limit handling. (not recommended)</li>
 *
 *     <li>{@link #complete()}, {@link #complete(boolean)}
 *     <br>Blocking execution building up on {@link #submit()}.
 *     <br>This will simply block the thread and return the Request result, or throw an exception.
 *     <br>An optional parameter of type boolean can be passed to disable automated rate limit handling. (not recommended)</li>
 * </ul>
 *
 * The most efficient way to use a RestAction is by using the asynchronous {@link #queue()} operations.
 * <br>These allow users to provide success and failure callbacks which will be called at a convenient time.
 *
 * @param <T>
 *        The generic response type for this RestAction
 *
 * @since 3.0
 */
public abstract class RestAction<T>
{
    public static final SimpleLog LOG = SimpleLog.getLog("RestAction");

    public static Consumer DEFAULT_SUCCESS = o -> {};
    public static Consumer<Throwable> DEFAULT_FAILURE = t ->
    {
        if (LOG.getEffectiveLevel().getPriority() <= SimpleLog.Level.DEBUG.getPriority())
        {
            LOG.log(t);
        }
        else
        {
            LOG.fatal("RestAction queue returned failure: [" + t.getClass().getSimpleName() + "] " + t.getMessage());
        }
    };

    protected final JDAImpl api;
    protected Route.CompiledRoute route;
    protected Object data;

    /**
     * Creates a new RestAction instance
     *
     * @param  api
     *         The current JDA instance
     * @param  route
     *         The {@link net.dv8tion.jda.core.requests.Route.CompiledRoute Route.CompiledRoute}
     *         to be used for rate limit handling
     * @param  data
     *         The data that should be sent to the specified route. (can be null)
     */
    public RestAction(JDA api, Route.CompiledRoute route, Object data)
    {
        this.api = (JDAImpl) api;
        this.route = route;
        this.data = data != null ? data : "";
    }

    /**
     * The current JDA instance
     *
     * @return The corresponding JDA instance
     */
    public JDA getJDA()
    {
        return api;
    }

    /**
     * Submits a Request for execution.
     * <br>Using the default callback functions:
     * {@link #DEFAULT_SUCCESS DEFAULT_SUCCESS} and
     * {@link #DEFAULT_FAILURE DEFAULT_FAILURE}
     *
     * <p><b>This method is asynchronous</b>
     */
    public void queue()
    {
        queue(null, null);
    }

    /**
     * Submits a Request for execution.
     * <br>Using the default failure callback function.
     *
     * <p><b>This method is asynchronous</b>
     *
     * @param  success
     *         The success callback that will be called at a convenient time
     *         for the API. (can be null)
     */
    public void queue(Consumer<T> success)
    {
        queue(success, null);
    }

    /**
     * Submits a Request for execution.
     *
     * <p><b>This method is asynchronous</b>
     *
     * @param  success
     *         The success callback that will be called at a convenient time
     *         for the API. (can be null)
     * @param  failure
     *         The failure callback that will be called if the Request
     *         encounters an exception at its execution point.
     */
    public void queue(Consumer<T> success, Consumer<Throwable> failure)
    {
        finalizeData();
        finalizeRoute();
        if (success == null)
            success = DEFAULT_SUCCESS;
        if (failure == null)
            failure = DEFAULT_FAILURE;
        api.getRequester().request(new Request<T>(this, success, failure, true));
    }

    /**
     * Submits a Request for execution and provides
     * an {@link java.util.concurrent.Future Future} representing
     * its completion task.
     * <br>Cancelling the returned Future will result in the cancellation
     * of the Request!
     *
     * @return Never-null {@link java.util.concurrent.Future Future} task representing the completion promise
     */
    public Future<T> submit()
    {
        return submit(true);
    }

    /**
     * Submits a Request for execution and provides
     * an {@link java.util.concurrent.Future Future} representing
     * its completion task.
     * <br>Cancelling the returned Future will result in the cancellation
     * of the Request!
     *
     * @param  shouldQueue
     *         Whether the Request should automatically handle rate limitations. (default true)
     *
     * @return Never-null {@link java.util.concurrent.Future Future} task representing the completion promise
     */
    public Future<T> submit(boolean shouldQueue)
    {
        finalizeData();
        finalizeRoute();
        return new RequestFuture<T>(this, shouldQueue);
    }

    /**
     * Blocks the current Thread and awaits the completion
     * of an {@link #submit()} request.
     * <br>Used for synchronous logic.
     *
     * <p><b>This might throw {@link java.lang.RuntimeException RuntimeExceptions}</b>
     *
     * @return The never-null response value
     */
    public T complete()
    {
        try
        {
            return complete(true);
        }
        catch (RateLimitedException ignored)
        {
            //This is so beyond impossible, but on the off chance that the laws of nature are rewritten
            // after the writing of this code, I'm placing this here.
            //Better safe than sorry?
            throw new RuntimeException(ignored);
        }
    }

    /**
     * Blocks the current Thread and awaits the completion
     * of an {@link #submit()} request.
     * <br>Used for synchronous logic.
     *
     * @param  shouldQueue
     *         Whether this should automatically handle rate limitations (default true)
     *
     * @throws RateLimitedException
     *         If we were rate limited and the {@code shouldQueue} is false
     *         <br>Use {@link #complete()} to avoid this Exception.
     *
     * @return The never-null response value
     */
    public T complete(boolean shouldQueue) throws RateLimitedException
    {
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

    protected void finalizeData() { }

    protected void finalizeRoute() { }

    protected abstract void handleResponse(Response response, Request request);

    /**
     * Specialized form of {@link net.dv8tion.jda.core.requests.RestAction} that is used to provide information that
     * has already been retrieved or generated so that another request does not need to be made to Discord.
     * <br>Basically: Allows you to provide a value directly to the success returns.
     *
     * @param <T>
     *        The generic response type for this RestAction
     */
    public static class EmptyRestAction<T> extends RestAction<T>
    {
        private final T returnObj;

        public EmptyRestAction(T returnObj)
        {
            super(null, null, null);
            this.returnObj = returnObj;
        }

        @Override
        public void queue(Consumer<T> success, Consumer<Throwable> failure)
        {
            if (success != null)
                success.accept(returnObj);
        }

        @Override
        public Future<T> submit(boolean shouldQueue)
        {
            return new CompletedFuture<>(returnObj);
        }

        @Override
        public T complete(boolean shouldQueue)
        {
            return returnObj;
        }

        @Override
        protected void handleResponse(Response response, Request request) { }
    }
}
