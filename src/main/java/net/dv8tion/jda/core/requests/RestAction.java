/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.JDALogger;
import okhttp3.RequestBody;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.util.concurrent.*;
import java.util.function.BooleanSupplier;
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
 * <h2>Planning Execution</h2>
 * To <u>schedule</u> a RestAction we provide both {@link #queue()} and {@link #complete()} versions that
 * will be executed by a {@link java.util.concurrent.ScheduledExecutorService ScheduledExecutorService} after a
 * specified delay:
 * <ul>
 *     <li>{@link #queueAfter(long, TimeUnit)}
 *     <br>Schedules a call to {@link #queue()} with default callback {@link java.util.function.Consumer Consumers} to be executed after the specified {@code delay}.
 *     <br>The {@link java.util.concurrent.TimeUnit TimeUnit} is used to convert the provided long into a delay time.
 *     <br>Example: {@code queueAfter(1, TimeUnit.SECONDS);}
 *     <br>will call {@link #queue()} <b>1 second</b> later.</li>
 *
 *     <li>{@link #submitAfter(long, TimeUnit)}
 *     <br>This returns a {@link java.util.concurrent.ScheduledFuture ScheduledFuture} which
 *         can be joined into the current Thread using {@link java.util.concurrent.ScheduledFuture#get()}
 *     <br>The blocking call to {@code submitAfter(delay, unit).get()} will return
 *         the value processed by a call to {@link #complete()}</li>
 *
 *     <li>{@link #completeAfter(long, TimeUnit)}
 *     <br>This operation simply sleeps for the given delay and will call {@link #complete()}
 *         once finished sleeping.</li>
 * </ul>
 *
 * <p>All of those operations provide overloads for optional parameters such as a custom
 * {@link java.util.concurrent.ScheduledExecutorService ScheduledExecutorService} instead of using the default
 * global JDA executor. Specifically {@link #queueAfter(long, TimeUnit)} has overloads
 * to provide a success and/or failure callback due to the returned {@link java.util.concurrent.ScheduledFuture ScheduledFuture}
 * not being able to provide the response values of the {@link #queue()} callbacks.
 *
 * <h1>Using RestActions</h1>
 * The most common way to use a RestAction is not using the returned value.
 * <br>For instance sending messages usually means you will not require to view the message once
 * it was sent. Thus we can simply use the <b>asynchronous</b> {@link #queue()} operation which will
 * be executed on a rate limit worker thread in the background, without blocking your current thread:
 * <pre><code>
 *      {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel} channel = event.getChannel();
 *     {@literal RestAction<Message>} action = channel.sendMessage("Hello World");
 *      action.{@link #queue() queue()}; // Execute the rest action asynchronously
 * </code></pre>
 *
 * <p>Sometimes it is important to access the response value, possibly to modify it later.
 * <br>Now we have two options to actually access the response value, either using an asynchronous
 * callback {@link java.util.function.Consumer Consumer} or the (not recommended) {@link #complete()} which will block
 * the current thread until the response has been processed and joins with the current thread.
 *
 * <h2>Example Queue: (recommended)</h2>
 * <pre><code>
 *     {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel} channel = event.getChannel();
 *     final long time = System.currentTimeMillis();
 *    {@literal RestAction<Message>} action = channel.sendMessage("Calculating Response Time...");
 *     {@link java.util.function.Consumer Consumer}{@literal <Message>} callback = (message) {@literal ->  {
 *        Message m = message; // ^This is a lambda parameter!^
 *        m.editMessage("Response Time: " + (System.currentTimeMillis() - time) + "ms").queue();
 *        // End with queue() to not block the callback thread!
 *      }};
 *     // You can also inline this with the queue parameter: action.queue(m {@literal ->} m.editMessage(...).queue());
 *     action.{@link #queue(Consumer) queue(callback)};
 * </code></pre>
 *
 * <h2>Example Complete:</h2>
 * <pre><code>
 *     {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel} channel = event.getChannel();
 *     final long time = System.currentTimeMillis();
 *    {@literal RestAction<Message>} action = channel.sendMessage("Calculating Response Time...");
 *     Message message = action.{@link #complete() complete()};
 *     message.editMessage("Response Time: " + (System.currentTimeMillis() - time) + "ms").queue();
 *     // End with {@link #queue() queue()} to not block the callback thread!
 * </code></pre>
 *
 * <h2>Example Planning:</h2>
 * <pre><code>
 *     {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel} channel = event.getChannel();
 *    {@literal RestAction<Message>} action = channel.sendMessage("This message will destroy itself in 5 seconds!");
 *     action.queue((message) {@literal ->} message.delete().{@link #queueAfter(long, TimeUnit) queueAfter(5, TimeUnit.SECONDS)});
 * </code></pre>
 *
 * <p><b>Developer Note:</b> It is generally a good practice to use asynchronous logic because blocking threads requires resources
 * which can be avoided by using callbacks over blocking operations:
 * <br>{@link #queue(Consumer)} {@literal >} {@link #complete()}
 *
 * <p>There is a dedicated <a href="https://github.com/DV8FromTheWorld/JDA/wiki/7)-Using-RestAction" target="_blank">wiki page</a>
 * for RestActions that can be useful for learning.
 *
 * @param <T>
 *        The generic response type for this RestAction
 *
 * @since 3.0
 */
public abstract class RestAction<T>
{
    public static final Logger LOG = JDALogger.getLog(RestAction.class);

    public static Consumer DEFAULT_SUCCESS = o -> {};
    public static Consumer<Throwable> DEFAULT_FAILURE = t ->
    {
        if (LOG.isDebugEnabled())
        {
            LOG.error("RestAction queue returned failure", t);
        }
        else
        {
            LOG.error("RestAction queue returned failure: [{}] {}", t.getClass().getSimpleName(), t.getMessage());
        }
    };

    private static final Consumer FALLBACK_CONSUMER = o -> {};

    protected static boolean passContext = false;

    protected final JDAImpl api;

    private final Route.CompiledRoute route;
    private final RequestBody data;

    private Object rawData;
    private BooleanSupplier checks;

    /**
     * If enabled this will pass a {@link net.dv8tion.jda.core.exceptions.ContextException ContextException}
     * as root-cause to all failure consumers.
     * Note that the {@link #DEFAULT_FAILURE} does not print a stack-trace at all unless specified!
     * <br>This might cause performance decrease due to the creation of exceptions for <b>every</b> execution.
     *
     * <p>It is recommended to pass a context consumer as failure manually using {@code queue(success, ContextException.here(failure))}
     *
     * @param  enable
     *         True, if context should be passed to all failure consumers
     */
    public static void setPassContext(boolean enable)
    {
        passContext = enable;
    }

    /**
     * Whether RestActions will use {@link net.dv8tion.jda.core.exceptions.ContextException ContextException}
     * automatically to keep track of the caller context.
     * <br>If set to {@code true} this can cause performance drops due to the creation of stack-traces on execution.
     *
     * @return True, if RestActions will keep track of context automatically
     *
     * @see    #setPassContext(boolean)
     */
    public static boolean isPassContext()
    {
        return passContext;
    }

    /**
     * Creates a new RestAction instance
     *
     * @param  api
     *         The current JDA instance
     * @param  route
     *         The {@link net.dv8tion.jda.core.requests.Route.CompiledRoute Route.CompiledRoute}
     *         to be used for rate limit handling
     */
    public RestAction(JDA api, Route.CompiledRoute route)
    {
        this(api, route, (RequestBody) null);
    }

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
    public RestAction(JDA api, Route.CompiledRoute route, RequestBody data)
    {
        Checks.notNull(api, "api");

        this.api = (JDAImpl) api;
        this.route = route;
        this.data = data;
    }

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
    public RestAction(JDA api, Route.CompiledRoute route, JSONObject data)
    {
        this(api, route, data == null ? null : RequestBody.create(Requester.MEDIA_TYPE_JSON, data.toString()));

        this.rawData = data;
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
     * Sets the last-second checks before finally executing the http request in the queue.
     * <br>If the provided supplier evaluates to {@code false} or throws an exception this will not be finished.
     * When an exception is thrown from the supplier it will be provided to the failure callback.
     *
     * @param  checks
     *         The checks to run before executing the request, or {@code null} to run no checks
     *
     * @return The current RestAction for chaining convenience
     */
    public RestAction<T> setCheck(BooleanSupplier checks)
    {
        this.checks = checks;
        return this;
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
    public void queue(Consumer<? super T> success)
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
    @SuppressWarnings("unchecked")
    public void queue(Consumer<? super T> success, Consumer<? super Throwable> failure)
    {
        Route.CompiledRoute route = finalizeRoute();
        Checks.notNull(route, "Route");
        RequestBody data = finalizeData();
        CaseInsensitiveMap<String, String> headers = finalizeHeaders();
        BooleanSupplier finisher = getFinisher();
        if (success == null)
            success = DEFAULT_SUCCESS == null ? FALLBACK_CONSUMER : DEFAULT_SUCCESS;
        if (failure == null)
            failure = DEFAULT_FAILURE == null ? FALLBACK_CONSUMER : DEFAULT_FAILURE;
        api.getRequester().request(new Request<>(this, success, failure, finisher, true, data, rawData, route, headers));
    }

    /**
     * Submits a Request for execution and provides a {@link net.dv8tion.jda.core.requests.RequestFuture RequestFuture}
     * representing its completion task.
     * <br>Cancelling the returned Future will result in the cancellation of the Request!
     *
     * <p>Note: The usage of {@link java.util.concurrent.CompletionStage#toCompletableFuture() CompletionStage.toCompletableFuture()} is not supported.
     *
     * @return Never-null {@link net.dv8tion.jda.core.requests.RequestFuture RequestFuture} representing the completion promise
     */
    public RequestFuture<T> submit()
    {
        return submit(true);
    }

    /**
     * Submits a Request for execution and provides a {@link net.dv8tion.jda.core.requests.RequestFuture RequestFuture}
     * representing its completion task.
     * <br>Cancelling the returned Future will result in the cancellation of the Request!
     *
     * <p>Note: The usage of {@link java.util.concurrent.CompletionStage#toCompletableFuture() CompletionStage.toCompletableFuture()} is not supported.
     *
     * @param  shouldQueue
     *         Whether the Request should automatically handle rate limitations. (default true)
     *
     * @return Never-null {@link net.dv8tion.jda.core.requests.RequestFuture RequestFuture} task representing the completion promise
     */
    public RequestFuture<T> submit(boolean shouldQueue)
    {
        Route.CompiledRoute route = finalizeRoute();
        Checks.notNull(route, "Route");
        RequestBody data = finalizeData();
        CaseInsensitiveMap<String, String> headers = finalizeHeaders();
        BooleanSupplier finisher = getFinisher();
        return new RestFuture<>(this, shouldQueue, finisher, data, rawData, route, headers);
    }

    /**
     * Blocks the current Thread and awaits the completion
     * of an {@link #submit()} request.
     * <br>Used for synchronous logic.
     *
     * <p><b>This might throw {@link java.lang.RuntimeException RuntimeExceptions}</b>
     *
     * @return The response value
     */
    public T complete()
    {
        try
        {
            return complete(true);
        }
        catch (RateLimitedException e)
        {
            //This is so beyond impossible, but on the off chance that the laws of nature are rewritten
            // after the writing of this code, I'm placing this here.
            //Better safe than sorry?
            throw new AssertionError(e);
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
     *         If we were rate limited and the {@code shouldQueue} is false.
     *         Use {@link #complete()} to avoid this Exception.
     *
     * @return The response value
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

    /**
     * Schedules a call to {@link #complete()} to be executed after the specified {@code delay}.
     * <br>This is an <b>asynchronous</b> operation that will return a
     * {@link java.util.concurrent.ScheduledFuture ScheduledFuture} representing the task.
     *
     * <p>The returned Future will provide the return type of a {@link #complete()} operation when
     * received through the <b>blocking</b> call to {@link java.util.concurrent.Future#get()}!
     *
     * <p>The global JDA {@link java.util.concurrent.ScheduledExecutorService ScheduledExecutorService}
     * is used for this operation.
     * <br>You can change the core pool size for this Executor through {@link net.dv8tion.jda.core.JDABuilder#setCorePoolSize(int) JDABuilder.setCorePoolSize(int)}
     * or you can provide your own Executor using {@link #submitAfter(long, java.util.concurrent.TimeUnit, java.util.concurrent.ScheduledExecutorService)}!
     *
     * @param  delay
     *         The delay after which this computation should be executed, negative to execute immediately
     * @param  unit
     *         The {@link java.util.concurrent.TimeUnit TimeUnit} to convert the specified {@code delay}
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided TimeUnit is {@code null}
     *
     * @return {@link java.util.concurrent.ScheduledFuture ScheduledFuture} representing the
     *         delayed operation
     */
    public ScheduledFuture<T> submitAfter(long delay, TimeUnit unit)
    {
        return submitAfter(delay, unit, api.pool);
    }

    /**
     * Schedules a call to {@link #complete()} to be executed after the specified {@code delay}.
     * <br>This is an <b>asynchronous</b> operation that will return a
     * {@link java.util.concurrent.ScheduledFuture ScheduledFuture} representing the task.
     *
     * <p>The returned Future will provide the return type of a {@link #complete()} operation when
     * received through the <b>blocking</b> call to {@link java.util.concurrent.Future#get()}!
     *
     * <p>The specified {@link java.util.concurrent.ScheduledExecutorService ScheduledExecutorService} is used for this operation.
     *
     * @param  delay
     *         The delay after which this computation should be executed, negative to execute immediately
     * @param  unit
     *         The {@link java.util.concurrent.TimeUnit TimeUnit} to convert the specified {@code delay}
     * @param  executor
     *         The Non-null {@link java.util.concurrent.ScheduledExecutorService ScheduledExecutorService} that should be used
     *         to schedule this operation
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided TimeUnit or ScheduledExecutorService is {@code null}
     *
     * @return {@link java.util.concurrent.ScheduledFuture ScheduledFuture}
     *         representing the delayed operation
     */
    public ScheduledFuture<T> submitAfter(long delay, TimeUnit unit, ScheduledExecutorService executor)
    {
        Checks.notNull(executor, "Scheduler");
        Checks.notNull(unit, "TimeUnit");
        return executor.schedule((Callable<T>) this::complete, delay, unit);
    }

    /**
     * Blocks the current Thread for the specified delay and calls {@link #complete()}
     * when delay has been reached.
     * <br>If the specified delay is negative this action will execute immediately. (see: {@link TimeUnit#sleep(long)})
     *
     * @param  delay
     *         The delay after which to execute a call to {@link #complete()}
     * @param  unit
     *         The {@link java.util.concurrent.TimeUnit TimeUnit} which should be used
     *         (this will use {@link java.util.concurrent.TimeUnit#sleep(long) unit.sleep(delay)})
     *
     * @throws java.lang.IllegalArgumentException
     *         If the specified {@link java.util.concurrent.TimeUnit TimeUnit} is {@code null}
     * @throws java.lang.RuntimeException
     *         If the sleep operation is interrupted
     *
     * @return The response value
     */
    public T completeAfter(long delay, TimeUnit unit)
    {
        Checks.notNull(unit, "TimeUnit");
        try
        {
            unit.sleep(delay);
            return complete();
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Schedules a call to {@link #queue()} to be executed after the specified {@code delay}.
     * <br>This is an <b>asynchronous</b> operation that will return a
     * {@link java.util.concurrent.ScheduledFuture ScheduledFuture} representing the task.
     *
     * <p>This operation gives no access to the response value.
     * <br>Use {@link #queueAfter(long, java.util.concurrent.TimeUnit, java.util.function.Consumer)} to access
     * the success consumer for {@link #queue(java.util.function.Consumer)}!
     *
     * <p>The global JDA {@link java.util.concurrent.ScheduledExecutorService ScheduledExecutorService} is used for this operation.
     * <br>You can change the core pool size for this Executor through {@link net.dv8tion.jda.core.JDABuilder#setCorePoolSize(int) JDABuilder.setCorePoolSize(int)}
     * or provide your own Executor with {@link #queueAfter(long, java.util.concurrent.TimeUnit, java.util.concurrent.ScheduledExecutorService)}
     *
     * @param  delay
     *         The delay after which this computation should be executed, negative to execute immediately
     * @param  unit
     *         The {@link java.util.concurrent.TimeUnit TimeUnit} to convert the specified {@code delay}
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided TimeUnit is {@code null}
     *
     * @return {@link java.util.concurrent.ScheduledFuture ScheduledFuture}
     *         representing the delayed operation
     */
    public ScheduledFuture<?> queueAfter(long delay, TimeUnit unit)
    {
        return queueAfter(delay, unit, api.pool);
    }

    /**
     * Schedules a call to {@link #queue(java.util.function.Consumer)} to be executed after the specified {@code delay}.
     * <br>This is an <b>asynchronous</b> operation that will return a
     * {@link java.util.concurrent.ScheduledFuture ScheduledFuture} representing the task.
     *
     * <p>This operation gives no access to the failure callback.
     * <br>Use {@link #queueAfter(long, java.util.concurrent.TimeUnit, java.util.function.Consumer, java.util.function.Consumer)} to access
     * the failure consumer for {@link #queue(java.util.function.Consumer, java.util.function.Consumer)}!
     *
     * <p>The global JDA {@link java.util.concurrent.ScheduledExecutorService ScheduledExecutorService} is used for this operation.
     * <br>You can change the core pool size for this Executor through {@link net.dv8tion.jda.core.JDABuilder#setCorePoolSize(int) JDABuilder.setCorePoolSize(int)}
     * or provide your own Executor with {@link #queueAfter(long, java.util.concurrent.TimeUnit, java.util.function.Consumer, java.util.concurrent.ScheduledExecutorService)}
     *
     * @param  delay
     *         The delay after which this computation should be executed, negative to execute immediately
     * @param  unit
     *         The {@link java.util.concurrent.TimeUnit TimeUnit} to convert the specified {@code delay}
     * @param  success
     *         The success {@link java.util.function.Consumer Consumer} that should be called
     *         once the {@link #queue(java.util.function.Consumer)} operation completes successfully.
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided TimeUnit is {@code null}
     *
     * @return {@link java.util.concurrent.ScheduledFuture ScheduledFuture}
     *         representing the delayed operation
     */
    public ScheduledFuture<?> queueAfter(long delay, TimeUnit unit, Consumer<? super T> success)
    {
        return queueAfter(delay, unit, success, api.pool);
    }

    /**
     * Schedules a call to {@link #queue(java.util.function.Consumer, java.util.function.Consumer)}
     * to be executed after the specified {@code delay}.
     * <br>This is an <b>asynchronous</b> operation that will return a
     * {@link java.util.concurrent.ScheduledFuture ScheduledFuture} representing the task.
     *
     * <p>The global JDA {@link java.util.concurrent.ScheduledExecutorService ScheduledExecutorService} is used for this operation.
     * <br>You can change the core pool size for this Executor through {@link net.dv8tion.jda.core.JDABuilder#setCorePoolSize(int) JDABuilder.setCorePoolSize(int)}
     * or provide your own Executor with
     * {@link #queueAfter(long, java.util.concurrent.TimeUnit, java.util.function.Consumer, java.util.function.Consumer, java.util.concurrent.ScheduledExecutorService)}
     *
     * @param  delay
     *         The delay after which this computation should be executed, negative to execute immediately
     * @param  unit
     *         The {@link java.util.concurrent.TimeUnit TimeUnit} to convert the specified {@code delay}
     * @param  success
     *         The success {@link java.util.function.Consumer Consumer} that should be called
     *         once the {@link #queue(java.util.function.Consumer, java.util.function.Consumer)} operation completes successfully.
     * @param  failure
     *         The failure {@link java.util.function.Consumer Consumer} that should be called
     *         in case of an error of the {@link #queue(java.util.function.Consumer, java.util.function.Consumer)} operation.
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided TimeUnit is {@code null}
     *
     * @return {@link java.util.concurrent.ScheduledFuture ScheduledFuture}
     *         representing the delayed operation
     */
    public ScheduledFuture<?> queueAfter(long delay, TimeUnit unit, Consumer<? super T> success, Consumer<? super Throwable> failure)
    {
        return queueAfter(delay, unit, success, failure, api.pool);
    }

    /**
     * Schedules a call to {@link #queue()} to be executed after the specified {@code delay}.
     * <br>This is an <b>asynchronous</b> operation that will return a
     * {@link java.util.concurrent.ScheduledFuture ScheduledFuture} representing the task.
     *
     * <p>This operation gives no access to the response value.
     * <br>Use {@link #queueAfter(long, java.util.concurrent.TimeUnit, java.util.function.Consumer)} to access
     * the success consumer for {@link #queue(java.util.function.Consumer)}!
     *
     * <p>The specified {@link java.util.concurrent.ScheduledExecutorService ScheduledExecutorService} is used for this operation.
     *
     * @param  delay
     *         The delay after which this computation should be executed, negative to execute immediately
     * @param  unit
     *         The {@link java.util.concurrent.TimeUnit TimeUnit} to convert the specified {@code delay}
     * @param  executor
     *         The Non-null {@link java.util.concurrent.ScheduledExecutorService ScheduledExecutorService} that should be used
     *         to schedule this operation
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided TimeUnit or ScheduledExecutorService is {@code null}
     *
     * @return {@link java.util.concurrent.ScheduledFuture ScheduledFuture}
     *         representing the delayed operation
     */
    public ScheduledFuture<?> queueAfter(long delay, TimeUnit unit, ScheduledExecutorService executor)
    {
        return queueAfter(delay, unit, null, executor);
    }

    /**
     * Schedules a call to {@link #queue(java.util.function.Consumer)} to be executed after the specified {@code delay}.
     * <br>This is an <b>asynchronous</b> operation that will return a
     * {@link java.util.concurrent.ScheduledFuture ScheduledFuture} representing the task.
     *
     * <p>This operation gives no access to the failure callback.
     * <br>Use {@link #queueAfter(long, java.util.concurrent.TimeUnit, java.util.function.Consumer, java.util.function.Consumer)} to access
     * the failure consumer for {@link #queue(java.util.function.Consumer, java.util.function.Consumer)}!
     *
     * <p>The specified {@link java.util.concurrent.ScheduledExecutorService ScheduledExecutorService} is used for this operation.
     *
     * @param  delay
     *         The delay after which this computation should be executed, negative to execute immediately
     * @param  unit
     *         The {@link java.util.concurrent.TimeUnit TimeUnit} to convert the specified {@code delay}
     * @param  success
     *         The success {@link java.util.function.Consumer Consumer} that should be called
     *         once the {@link #queue(java.util.function.Consumer)} operation completes successfully.
     * @param  executor
     *         The Non-null {@link java.util.concurrent.ScheduledExecutorService ScheduledExecutorService} that should be used
     *         to schedule this operation
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided TimeUnit or ScheduledExecutorService is {@code null}
     *
     * @return {@link java.util.concurrent.ScheduledFuture ScheduledFuture}
     *         representing the delayed operation
     */
    public ScheduledFuture<?> queueAfter(long delay, TimeUnit unit, Consumer<? super T> success, ScheduledExecutorService executor)
    {
        return queueAfter(delay, unit, success, null, executor);
    }

    /**
     * Schedules a call to {@link #queue(java.util.function.Consumer, java.util.function.Consumer)}
     * to be executed after the specified {@code delay}.
     * <br>This is an <b>asynchronous</b> operation that will return a
     * {@link java.util.concurrent.ScheduledFuture ScheduledFuture} representing the task.
     *
     * <p>The specified {@link java.util.concurrent.ScheduledExecutorService ScheduledExecutorService} is used for this operation.
     *
     * @param  delay
     *         The delay after which this computation should be executed, negative to execute immediately
     * @param  unit
     *         The {@link java.util.concurrent.TimeUnit TimeUnit} to convert the specified {@code delay}
     * @param  success
     *         The success {@link java.util.function.Consumer Consumer} that should be called
     *         once the {@link #queue(java.util.function.Consumer, java.util.function.Consumer)} operation completes successfully.
     * @param  failure
     *         The failure {@link java.util.function.Consumer Consumer} that should be called
     *         in case of an error of the {@link #queue(java.util.function.Consumer, java.util.function.Consumer)} operation.
     * @param  executor
     *         The Non-null {@link java.util.concurrent.ScheduledExecutorService ScheduledExecutorService} that should be used
     *         to schedule this operation
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided TimeUnit or ScheduledExecutorService is {@code null}
     *
     * @return {@link java.util.concurrent.ScheduledFuture ScheduledFuture}
     *         representing the delayed operation
     */
    public ScheduledFuture<?> queueAfter(long delay, TimeUnit unit, Consumer<? super T> success, Consumer<? super Throwable> failure, ScheduledExecutorService executor)
    {
        Checks.notNull(executor, "Scheduler");
        Checks.notNull(unit, "TimeUnit");
        return executor.schedule(() -> queue(success, failure), delay, unit);
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

    protected abstract void handleResponse(Response response, Request<T> request);

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

        public EmptyRestAction(JDA api, T returnObj)
        {
            super(api, null);
            this.returnObj = returnObj;
        }

        @Override
        public void queue(Consumer<? super T> success, Consumer<? super Throwable> failure)
        {
            if (success != null)
                success.accept(returnObj);
        }

        @Override
        public RequestFuture<T> submit(boolean shouldQueue)
        {
            return new RestFuture<>(returnObj);
        }

        @Override
        public T complete(boolean shouldQueue)
        {
            return returnObj;
        }

        @Override
        protected void handleResponse(Response response, Request<T> request) { }
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
}
