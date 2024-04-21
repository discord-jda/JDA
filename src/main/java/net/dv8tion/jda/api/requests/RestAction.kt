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
package net.dv8tion.jda.api.requests

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.exceptions.ContextException.here
import net.dv8tion.jda.api.exceptions.RateLimitedException
import net.dv8tion.jda.api.utils.Result
import net.dv8tion.jda.api.utils.concurrent.DelayedCompletableFuture
import net.dv8tion.jda.internal.requests.RestActionImpl
import net.dv8tion.jda.internal.requests.restaction.operator.*
import net.dv8tion.jda.internal.utils.*
import java.time.Duration
import java.util.*
import java.util.concurrent.*
import java.util.function.*
import java.util.function.Function
import java.util.stream.Collector
import java.util.stream.Collectors
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * A class representing a terminal between the user and the discord API.
 * <br></br>This is used to offer users the ability to decide how JDA should limit a Request.
 *
 *
 * Methods that return an instance of RestAction require an additional step
 * to complete the execution. Thus the user needs to append a follow-up method.
 *
 *
 * A default RestAction is issued with the following operations:
 *
 *  * [.queue], [.queue], [.queue]
 * <br></br>The fastest and most simplistic way to execute a RestAction is to queue it.
 * <br></br>This method has two optional callback functions, one with the generic type and another with a failure exception.
 *
 *  * [.submit], [.submit]
 * <br></br>Provides a Future representing the pending request.
 * <br></br>An optional parameter of type boolean can be passed to disable automated rate limit handling. (not recommended)
 *
 *  * [.complete], [.complete]
 * <br></br>Blocking execution building up on [.submit].
 * <br></br>This will simply block the thread and return the Request result, or throw an exception.
 * <br></br>An optional parameter of type boolean can be passed to disable automated rate limit handling. (not recommended)
 *
 *
 * The most efficient way to use a RestAction is by using the asynchronous [.queue] operations.
 * <br></br>These allow users to provide success and failure callbacks which will be called at a convenient time.
 *
 *
 * **Planning Execution**<br></br>
 * To <u>schedule</u> a RestAction we provide both [.queue] and [.complete] versions that
 * will be executed by a [ScheduledExecutorService][java.util.concurrent.ScheduledExecutorService] after a
 * specified delay:
 *
 *  * [.queueAfter]
 * <br></br>Schedules a call to [.queue] with default callback [Consumers][java.util.function.Consumer] to be executed after the specified `delay`.
 * <br></br>The [TimeUnit][java.util.concurrent.TimeUnit] is used to convert the provided long into a delay time.
 * <br></br>Example: `queueAfter(1, TimeUnit.SECONDS);`
 * <br></br>will call [.queue] **1 second** later.
 *
 *  * [.submitAfter]
 * <br></br>This returns a [ScheduledFuture][java.util.concurrent.ScheduledFuture] which
 * can be joined into the current Thread using [java.util.concurrent.ScheduledFuture.get]
 * <br></br>The blocking call to `submitAfter(delay, unit).get()` will return
 * the value processed by a call to [.complete]
 *
 *  * [.completeAfter]
 * <br></br>This operation simply sleeps for the given delay and will call [.complete]
 * once finished sleeping.
 *
 *
 *
 * All of those operations provide overloads for optional parameters such as a custom
 * [ScheduledExecutorService][java.util.concurrent.ScheduledExecutorService] instead of using the default
 * global JDA executor. Specifically [.queueAfter] has overloads
 * to provide a success and/or failure callback due to the returned [ScheduledFuture][java.util.concurrent.ScheduledFuture]
 * not being able to provide the response values of the [.queue] callbacks.
 *
 *
 * **Using RestActions**<br></br>
 * The most common way to use a RestAction is not using the returned value.
 * <br></br>For instance sending messages usually means you will not require to view the message once
 * it was sent. Thus we can simply use the **asynchronous** [.queue] operation which will
 * be executed on a rate limit worker thread in the background, without blocking your current thread:
 * <pre>`
 * [MessageChannel][net.dv8tion.jda.api.entities.channel.middleman.MessageChannel] channel = event.getChannel();
 * RestAction<Message> action = channel.sendMessage("Hello World");
 * action.[queue()][.queue]; // Execute the rest action asynchronously
`</pre> *
 *
 *
 * Sometimes it is important to access the response value, possibly to modify it later.
 * <br></br>Now we have two options to actually access the response value, either using an asynchronous
 * callback [Consumer][java.util.function.Consumer] or the (not recommended) [.complete] which will block
 * the current thread until the response has been processed and joins with the current thread.
 *
 *
 * **Example Queue: (recommended)**<br></br>
 * <pre>`
 * [MessageChannel][net.dv8tion.jda.api.entities.channel.middleman.MessageChannel] channel = event.getChannel();
 * final long time = System.currentTimeMillis();
 * RestAction<Message> action = channel.sendMessage("Calculating Response Time...");
 * [Consumer][java.util.function.Consumer]<Message> callback = (message) ->  {
 * Message m = message; // ^This is a lambda parameter!^
 * m.editMessage("Response Time: " + (System.currentTimeMillis() - time) + "ms").queue();
 * // End with queue() to not block the callback thread!
 * };
 * // You can also inline this with the queue parameter: action.queue(m -> m.editMessage(...).queue());
 * action.[queue(callback)][.queue];
`</pre> *
 *
 *
 * **Example Complete:**<br></br>
 * <pre>`
 * [MessageChannel][net.dv8tion.jda.api.entities.channel.middleman.MessageChannel] channel = event.getChannel();
 * final long time = System.currentTimeMillis();
 * RestAction<Message> action = channel.sendMessage("Calculating Response Time...");
 * Message message = action.[complete()][.complete];
 * message.editMessage("Response Time: " + (System.currentTimeMillis() - time) + "ms").queue();
 * // End with [queue()][.queue] to not block the callback thread!
`</pre> *
 *
 *
 * **Example Planning:**<br></br>
 * <pre>`
 * [MessageChannel][net.dv8tion.jda.api.entities.channel.middleman.MessageChannel] channel = event.getChannel();
 * RestAction<Message> action = channel.sendMessage("This message will destroy itself in 5 seconds!");
 * action.queue((message) -> message.delete().[queueAfter(5, TimeUnit.SECONDS)][.queueAfter]);
`</pre> *
 *
 *
 * **Developer Note:** It is generally a good practice to use asynchronous logic because blocking threads requires resources
 * which can be avoided by using callbacks over blocking operations:
 * <br></br>[.queue] &gt; [.complete]
 *
 *
 * There is a dedicated [wiki page](https://github.com/discord-jda/JDA/wiki/7)-Using-RestAction)
 * for RestActions that can be useful for learning.
 *
 * @param <T>
 * The generic response type for this RestAction
 *
 * @since 3.0
 *
 * @see net.dv8tion.jda.api.exceptions.ErrorHandler
 *
 * @see net.dv8tion.jda.api.exceptions.ErrorResponseException
</T> */
interface RestAction<T> {
    @JvmField
    @get:Nonnull
    val jDA: JDA

    /**
     * Sets the last-second checks before finally executing the http request in the queue.
     * <br></br>If the provided supplier evaluates to `false` or throws an exception this will not be finished.
     * When an exception is thrown from the supplier it will be provided to the failure callback.
     *
     * @param  checks
     * The checks to run before executing the request, or `null` to run no checks
     *
     * @return The current RestAction for chaining convenience
     *
     * @see .getCheck
     * @see .addCheck
     */
    @Nonnull
    fun setCheck(checks: BooleanSupplier?): RestAction<T>?
    val check: BooleanSupplier?
        /**
         * The current checks for this RestAction.
         *
         * @return The current checks, or null if none were set
         *
         * @see .setCheck
         * @since  4.2.1
         */
        get() = null

    /**
     * Shortcut for `setCheck(() -> getCheck().getAsBoolean() && checks.getAsBoolean())`.
     *
     * @param  checks
     * Other checks to run
     *
     * @throws IllegalArgumentException
     * If the provided checks are null
     *
     * @return The current RestAction for chaining convenience
     *
     * @see .setCheck
     * @since  4.2.1
     */
    @Nonnull
    @CheckReturnValue
    fun addCheck(@Nonnull checks: BooleanSupplier): RestAction<T>? {
        Checks.notNull(checks, "Checks")
        val check = check
        return setCheck { (check == null || check.asBoolean) && checks.asBoolean }
    }

    /**
     * Timeout for this RestAction instance.
     * <br></br>If the request doesn't get executed within the timeout it will fail.
     *
     *
     * When a RestAction times out, it will fail with a [TimeoutException][java.util.concurrent.TimeoutException].
     * This is the same as `deadline(System.currentTimeMillis() + unit.toMillis(timeout))`.
     *
     *
     * **Example**<br></br>
     * <pre>`action.timeout(10, TimeUnit.SECONDS) // 10 seconds from now
     * .queueAfter(20, SECONDS); // request will not be executed within deadline and timeout immediately after 20 seconds
    `</pre> *
     *
     * @param  timeout
     * The timeout to use
     * @param  unit
     * [Unit][TimeUnit] for the timeout value
     *
     * @throws IllegalArgumentException
     * If the provided unit is null
     *
     * @return The same RestAction instance with the applied timeout
     *
     * @see .setDefaultTimeout
     */
    @Nonnull
    fun timeout(timeout: Long, @Nonnull unit: TimeUnit): RestAction<T>? {
        Checks.notNull(unit, "TimeUnit")
        return deadline(if (timeout <= 0) 0 else System.currentTimeMillis() + unit.toMillis(timeout))
    }

    /**
     * Similar to [.timeout] but schedules a deadline at which the request has to be completed.
     * <br></br>If the deadline is reached, the request will fail with a [TimeoutException][java.util.concurrent.TimeoutException].
     *
     *
     * This does not mean that the request will immediately timeout when the deadline is reached. JDA will check the deadline
     * right before executing the request or within intervals in a worker thread. This only means the request will timeout
     * if the deadline has passed.
     *
     *
     * **Example**<br></br>
     * <pre>`action.deadline(System.currentTimeMillis() + 10000) // 10 seconds from now
     * .queueAfter(20, SECONDS); // request will not be executed within deadline and timeout immediately after 20 seconds
    `</pre> *
     *
     * @param  timestamp
     * Millisecond timestamp at which the request will timeout
     *
     * @return The same RestAction with the applied deadline
     *
     * @see .timeout
     * @see .setDefaultTimeout
     */
    @Nonnull
    fun deadline(timestamp: Long): RestAction<T>? {
        throw UnsupportedOperationException()
    }
    /**
     * Submits a Request for execution.
     * <br></br>Using the default failure callback function.
     *
     *
     * To handle failures use [.queue].
     *
     *
     * **This method is asynchronous**
     *
     *
     * **Example**<br></br>
     * <pre>`public static void sendPrivateMessage(User user, String content)
     * {
     * // The "<PrivateChannel>" is the response type for the parameter in the success callback
     * RestAction<PrivateChannel> action = user.openPrivateChannel();
     * // "channel" is the identifier we use to access the channel of the response
     * // this is like the "user" we declared above, just a name for the function parameter
     * action.queue((channel) -> channel.sendMessage(content).queue());
     * }
    `</pre> *
     *
     * @throws java.util.concurrent.RejectedExecutionException
     * If the requester has been shutdown by [JDA.shutdown] or [JDA.shutdownNow]
     *
     * @param  success
     * The success callback that will be called at a convenient time
     * for the API. (can be null)
     *
     * @see .queue
     */
    /**
     * Submits a Request for execution.
     * <br></br>Using the default callback functions:
     * [.setDefaultSuccess] and [.setDefaultFailure]
     *
     *
     * To access the response you can use [.queue]
     * and to handle failures use [.queue].
     *
     *
     * **This method is asynchronous**
     *
     *
     * **Example**<br></br>
     * <pre>`public static void sendMessage(MessageChannel channel, String content)
     * {
     * // sendMessage returns "MessageAction" which is a specialization for "RestAction<Message>"
     * RestAction<Message> action = channel.sendMessage(content);
     * // call queue() to send the message off to discord.
     * action.queue();
     * }
    `</pre> *
     *
     * @throws java.util.concurrent.RejectedExecutionException
     * If the requester has been shutdown by [JDA.shutdown] or [JDA.shutdownNow]
     *
     * @see net.dv8tion.jda.api.entities.channel.middleman.MessageChannel.sendMessage
     * @see .queue
     * @see .queue
     */
    @JvmOverloads
    fun queue(success: Consumer<in T>? = null) {
        queue(success, null)
    }

    /**
     * Submits a Request for execution.
     *
     *
     * **This method is asynchronous**
     *
     *
     * **Example**<br></br>
     * <pre>`public static void sendPrivateMessage(JDA jda, String userId, String content)
     * {
     * // Retrieve the user by their id
     * RestAction<User> action = jda.retrieveUserById(userId);
     * action.queue(
     * // Handle success if the user exists
     * (user) -> user.openPrivateChannel().queue(
     * (channel) -> channel.sendMessage(content).queue()),
     *
     * // Handle failure if the user does not exist (or another issue appeared)
     * (error) -> error.printStackTrace()
     * );
     *
     * // Alternatively use submit() to remove nested callbacks
     * }
    `</pre> *
     *
     * @throws java.util.concurrent.RejectedExecutionException
     * If the requester has been shutdown by [JDA.shutdown] or [JDA.shutdownNow]
     *
     * @param  success
     * The success callback that will be called at a convenient time
     * for the API. (can be null to use default)
     * @param  failure
     * The failure callback that will be called if the Request
     * encounters an exception at its execution point. (can be null to use default)
     *
     * @see .submit
     * @see net.dv8tion.jda.api.exceptions.ErrorHandler
     */
    fun queue(success: Consumer<in T>?, failure: Consumer<in Throwable>?)

    /**
     * Blocks the current Thread and awaits the completion
     * of an [.submit] request.
     * <br></br>Used for synchronous logic.
     *
     *
     * **This might throw [RuntimeExceptions][java.lang.RuntimeException]**
     *
     * @throws java.util.concurrent.RejectedExecutionException
     * If the requester has been shutdown by [JDA.shutdown] or [JDA.shutdownNow]
     * @throws IllegalStateException
     * If used within a [queue(...)][.queue] callback
     *
     * @return The response value
     */
    fun complete(): T {
        return try {
            complete(true)
        } catch (e: RateLimitedException) {
            //This is so beyond impossible, but on the off chance that the laws of nature are rewritten
            // after the writing of this code, I'm placing this here.
            //Better safe than sorry?
            throw AssertionError(e)
        }
    }

    /**
     * Blocks the current Thread and awaits the completion
     * of an [.submit] request.
     * <br></br>Used for synchronous logic.
     *
     * @param  shouldQueue
     * Whether this should automatically handle rate limitations (default true)
     *
     * @throws java.util.concurrent.RejectedExecutionException
     * If the requester has been shutdown by [JDA.shutdown] or [JDA.shutdownNow]
     * @throws IllegalStateException
     * If used within a [queue(...)][.queue] callback
     * @throws RateLimitedException
     * If we were rate limited and the `shouldQueue` is false.
     * Use [.complete] to avoid this Exception.
     *
     * @return The response value
     */
    @Throws(RateLimitedException::class)
    fun complete(shouldQueue: Boolean): T

    /**
     * Submits a Request for execution and provides a [CompletableFuture][java.util.concurrent.CompletableFuture]
     * representing its completion task.
     * <br></br>Cancelling the returned Future will result in the cancellation of the Request!
     *
     *
     * **Example**<br></br>
     * <pre>`public static void sendPrivateMessage(JDA jda, String userId, String content)
     * {
     * // Retrieve the user by their id
     * RestAction<User> action = jda.retrieveUserById(userId);
     * action.submit() // CompletableFuture<User>
     * // Handle success if the user exists
     * .thenCompose((user) -> user.openPrivateChannel().submit()) // CompletableFuture<PrivateChannel>
     * .thenCompose((channel) -> channel.sendMessage(content).submit()) // CompletableFuture<Void>
     * .whenComplete((v, error) -> {
     * // Handle failure if the user does not exist (or another issue appeared)
     * if (error != null) error.printStackTrace();
     * });
     * }
    `</pre> *
     *
     * @throws java.util.concurrent.RejectedExecutionException
     * If the requester has been shutdown by [JDA.shutdown] or [JDA.shutdownNow]
     *
     * @return Never-null [CompletableFuture][java.util.concurrent.CompletableFuture] representing the completion promise
     */
    @Nonnull
    fun submit(): CompletableFuture<T>? {
        return submit(true)
    }

    /**
     * Submits a Request for execution and provides a [CompletableFuture][java.util.concurrent.CompletableFuture]
     * representing its completion task.
     * <br></br>Cancelling the returned Future will result in the cancellation of the Request!
     *
     * @throws java.util.concurrent.RejectedExecutionException
     * If the requester has been shutdown by [JDA.shutdown] or [JDA.shutdownNow]
     *
     * @param  shouldQueue
     * Whether the Request should automatically handle rate limitations. (default true)
     *
     * @return Never-null [CompletableFuture][java.util.concurrent.CompletableFuture] task representing the completion promise
     */
    @Nonnull
    fun submit(shouldQueue: Boolean): CompletableFuture<T>?

    /**
     * Converts the success and failure callbacks into a [Result].
     * <br></br>This means the [.queue] failure consumer will never be used.
     * Instead, all results will be evaluated into a success consumer which provides an instance of [Result].
     *
     *
     * [Result] will either be [successful][Result.isSuccess] or [failed][Result.isFailure].
     * This can be useful in combination with [.allOf] to handle failed requests individually for each
     * action.
     *
     *
     * **Note: You have to handle failures explicitly with this.**
     * You should use [Result.onFailure], [Result.getFailure], or [Result.expect]!
     *
     * @return RestAction - Type: [Result]
     *
     * @since  4.2.1
     */
    @Nonnull
    @CheckReturnValue
    fun mapToResult(): RestAction<Result<T>>? {
        return map { value: T -> Result.success(value) }.onErrorMap(
            Function { error: Throwable? ->
                Result.failure(
                    error!!
                )
            })
    }

    /**
     * Intermediate operator that returns a modified RestAction.
     *
     *
     * This does not modify this instance but returns a new RestAction which will apply
     * the map function on successful execution.
     *
     *
     * **Example**<br></br>
     * <pre>`public RestAction<String> retrieveMemberNickname(Guild guild, String userId) {
     * return guild.retrieveMemberById(userId)
     * .map(Member::getNickname);
     * }
    `</pre> *
     *
     * @param  map
     * The mapping function to apply to the action result
     *
     * @param  <O>
     * The target output type
     *
     * @return RestAction for the mapped type
     *
     * @since  4.1.1
    </O> */
    @Nonnull
    @CheckReturnValue
    fun <O> map(@Nonnull map: Function<in T, out O>?): RestAction<O> {
        Checks.notNull(map, "Function")
        return MapRestAction(this, map)
    }

    /**
     * An intermediate operator that returns a modified RestAction.
     *
     *
     * This does not modify this instance but returns a new RestAction, which will consume
     * the actions result using the given consumer on successful execution.
     * The resulting action continues with the previous result.
     *
     *
     * **Example**<br></br>
     * <pre>`public RestAction<String> retrieveMemberNickname(Guild guild, String userId) {
     * return guild.retrieveMemberById(userId)
     * .map(Member::getNickname)
     * .onSuccess(System.out::println);
     * }
    `</pre> *
     *
     * Prefer using [.queue] instead, if continuation of the action
     * chain is not desired.
     *
     * @param  consumer
     * The consuming function to apply to the action result, failures are propagated
     * into the resulting action
     *
     * @throws IllegalArgumentException
     * If the consumer is null
     *
     * @return RestAction that consumes the action result
     */
    @Nonnull
    @CheckReturnValue
    fun onSuccess(@Nonnull consumer: Consumer<in T>): RestAction<T>? {
        Checks.notNull(consumer, "Consumer")
        return map { result: T ->
            consumer.accept(result)
            result
        }
    }

    /**
     * Supply a fallback value when the RestAction fails for any reason.
     *
     *
     * This does not modify this instance but returns a new RestAction which will apply
     * the map function on failed execution.
     *
     *
     * **Example**<br></br>
     * <pre>`public RestAction<String> sendMessage(User user, String content) {
     * return user.openPrivateChannel() // RestAction<PrivateChannel>
     * .flatMap((channel) -> channel.sendMessage(content)) // RestAction<Message>
     * .map(Message::getContentRaw) // RestAction<String>
     * .onErrorMap(Throwable::getMessage); // RestAction<String> (must be the same as above)
     * }
    `</pre> *
     *
     * @param  map
     * The mapping function which provides the fallback value to use
     *
     * @throws IllegalArgumentException
     * If the mapping function is null
     *
     * @return RestAction with fallback handling
     *
     * @since  4.2.0
     */
    @Nonnull
    @CheckReturnValue
    fun onErrorMap(@Nonnull map: Function<in Throwable?, out T>?): RestAction<T>? {
        return onErrorMap(null, map)
    }

    /**
     * Supply a fallback value when the RestAction fails for a specific reason.
     *
     *
     * This does not modify this instance but returns a new RestAction which will apply
     * the map function on failed execution.
     *
     *
     * **Example**<br></br>
     * <pre>`public RestAction<String> sendMessage(User user, String content) {
     * return user.openPrivateChannel() // RestAction<PrivateChannel>
     * .flatMap((channel) -> channel.sendMessage(content)) // RestAction<Message>
     * .map(Message::getContentRaw) // RestAction<String>
     * .onErrorMap(CANNOT_SEND_TO_USER::test, Throwable::getMessage); // RestAction<String> (must be the same as above)
     * }
    `</pre> *
     *
     * @param  condition
     * A condition that must return true to apply this fallback
     * @param  map
     * The mapping function which provides the fallback value to use
     *
     * @throws IllegalArgumentException
     * If the mapping function is null
     *
     * @return RestAction with fallback handling
     *
     * @see ErrorResponse.test
     * @see ErrorResponse.test
     * @since  4.2.0
     */
    @Nonnull
    @CheckReturnValue
    fun onErrorMap(
        condition: Predicate<in Throwable?>?,
        @Nonnull map: Function<in Throwable?, out T>?
    ): RestAction<T>? {
        Checks.notNull(map, "Function")
        return MapErrorRestAction(this, condition ?: Predicate { x: Throwable? -> true }, map)
    }

    /**
     * Supply a fallback value when the RestAction fails for a any reason.
     *
     *
     * This does not modify this instance but returns a new RestAction which will apply
     * the map function on failed execution.
     *
     *
     * **Example**<br></br>
     * <pre>`public RestAction<Message> sendMessage(User user, TextChannel context, String content) {
     * return user.openPrivateChannel() // RestAction<PrivateChannel>
     * .flatMap((channel) -> channel.sendMessage(content)) // RestAction<Message>
     * .onErrorFlatMap(
     * (error) -> context.sendMessage("Failed to send direct message to " + user.getAsMention() + " Reason: " + error)
     * ); // RestAction<Message> (must be the same as above)
     * }
    `</pre> *
     *
     * @param  map
     * The mapping function which provides the fallback action to use
     *
     * @throws IllegalArgumentException
     * If the mapping function is null
     *
     * @return RestAction with fallback handling
     *
     * @since  4.2.0
     */
    @Nonnull
    @CheckReturnValue
    fun onErrorFlatMap(@Nonnull map: Function<in Throwable?, out RestAction<out T>?>?): RestAction<T>? {
        return onErrorFlatMap(null, map)
    }

    /**
     * Supply a fallback value when the RestAction fails for a specific reason.
     *
     *
     * This does not modify this instance but returns a new RestAction which will apply
     * the map function on failed execution.
     *
     *
     * **Example**<br></br>
     * <pre>`public RestAction<Message> sendMessage(User user, TextChannel context, String content) {
     * return user.openPrivateChannel() // RestAction<PrivateChannel>
     * .flatMap((channel) -> channel.sendMessage(content)) // RestAction<Message>
     * .onErrorFlatMap(CANNOT_SEND_TO_USER::test,
     * (error) -> context.sendMessage("Cannot send direct message to " + user.getAsMention())
     * ); // RestAction<Message> (must be the same as above)
     * }
    `</pre> *
     *
     * @param  condition
     * A condition that must return true to apply this fallback
     * @param  map
     * The mapping function which provides the fallback action to use
     *
     * @throws IllegalArgumentException
     * If the mapping function is null
     *
     * @return RestAction with fallback handling
     *
     * @see ErrorResponse.test
     * @see ErrorResponse.test
     * @since  4.2.0
     */
    @Nonnull
    @CheckReturnValue
    fun onErrorFlatMap(
        condition: Predicate<in Throwable?>?,
        @Nonnull map: Function<in Throwable?, out RestAction<out T>?>?
    ): RestAction<T>? {
        Checks.notNull(map, "Function")
        return FlatMapErrorRestAction(this, condition ?: Predicate { x: Throwable? -> true }, map)
    }

    /**
     * Intermediate operator that returns a modified RestAction.
     *
     *
     * This does not modify this instance but returns a new RestAction which will apply
     * the map function on successful execution. This will compute the result of both RestActions.
     * <br></br>The returned RestAction must not be null!
     * To terminate the execution chain on a specific condition you can use [.flatMap].
     *
     *
     * **Example**<br></br>
     * <pre>`public RestAction<Void> initializeGiveaway(Guild guild, String channelName) {
     * return guild.createTextChannel(channelName)
     * .addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.MESSAGE_SEND)) // deny write for everyone
     * .addPermissionOverride(guild.getSelfMember(), EnumSet.of(Permission.MESSAGE_SEND), null) // allow for self user
     * .flatMap((channel) -> channel.sendMessage("React to enter giveaway!")) // send message
     * .flatMap((message) -> message.addReaction(REACTION)); // add reaction
     * }
    `</pre> *
     *
     * @param  flatMap
     * The mapping function to apply to the action result, must return a RestAction
     *
     * @param  <O>
     * The target output type
     *
     * @return RestAction for the mapped type
     *
     * @since  4.1.1
    </O> */
    @Nonnull
    @CheckReturnValue
    fun <O> flatMap(@Nonnull flatMap: Function<in T, out RestAction<O>?>?): RestAction<O>? {
        return flatMap(null, flatMap)
    }

    /**
     * Intermediate operator that returns a modified RestAction.
     *
     *
     * This does not modify this instance but returns a new RestAction which will apply
     * the map function on successful execution. This will compute the result of both RestActions.
     * <br></br>The provided RestAction must not be null!
     *
     *
     * **Example**<br></br>
     * <pre>`private static final int MAX_COUNT = 1000;
     * public void updateCount(MessageChannel channel, String messageId, int count) {
     * channel.retrieveMessageById(messageId) // retrieve message for check
     * .map(Message::getContentRaw) // get content of the message
     * .map(Integer::parseInt) // convert it to an int
     * .flatMap(
     * (currentCount) -> currentCount + count <= MAX_COUNT, // Only edit if new count does not exceed maximum
     * (currentCount) -> channel.editMessageById(messageId, String.valueOf(currentCount + count)) // edit message
     * )
     * .map(Message::getContentRaw) // get content of the message
     * .map(Integer::parseInt) // convert it to an int
     * .queue((newCount) -> System.out.println("Updated count to " + newCount));
     * }
    `</pre> *
     *
     * @param  condition
     * A condition predicate that decides whether to apply the flat map operator or not
     * @param  flatMap
     * The mapping function to apply to the action result, must return a RestAction
     *
     * @param  <O>
     * The target output type
     *
     * @return RestAction for the mapped type
     *
     * @see .flatMap
     * @see .map
     * @since  4.1.1
    </O> */
    @Nonnull
    @CheckReturnValue
    fun <O> flatMap(
        condition: Predicate<in T>?,
        @Nonnull flatMap: Function<in T, out RestAction<O>?>?
    ): RestAction<O>? {
        Checks.notNull(flatMap, "Function")
        return FlatMapRestAction(this, condition, flatMap)
    }

    /**
     * Combines this RestAction with the provided action.
     * <br></br>The result is computed by the provided [BiFunction].
     *
     *
     * If one of the actions fails, the other will be cancelled.
     * To handle failures individually instead of cancelling you can use [.mapToResult].
     *
     * @param  other
     * The action to combine
     * @param  accumulator
     * BiFunction to compute the result
     * @param  <U>
     * The type of the other action
     * @param  <O>
     * The result type after applying the accumulator function
     *
     * @throws IllegalArgumentException
     * If null is provided or you tried to combine an action with itself
     *
     * @return Combined RestAction
     *
     * @since  4.2.1
    </O></U> */
    @Nonnull
    @CheckReturnValue
    fun <U, O> and(
        @Nonnull other: RestAction<U>?,
        @Nonnull accumulator: BiFunction<in T, in U, out O>?
    ): RestAction<O> {
        Checks.notNull(other, "RestAction")
        Checks.notNull(accumulator, "Accumulator")
        return CombineRestAction(this, other, accumulator)
    }

    /**
     * Combines this RestAction with the provided action.
     *
     *
     * If one of the actions fails, the other will be cancelled.
     * To handle failures individually instead of cancelling you can use [.mapToResult].
     *
     * @param  other
     * The action to combine
     * @param  <U>
     * The type of the other action
     *
     * @throws IllegalArgumentException
     * If null is provided or you tried to combine an action with itself
     *
     * @return Combined RestAction with empty result
     *
     * @since  4.2.1
    </U> */
    @Nonnull
    @CheckReturnValue
    fun <U> and(@Nonnull other: RestAction<U>?): RestAction<Void?>? {
        return and(other) { a: T, b: U -> null }
    }

    /**
     * Accumulates this RestAction with the provided actions into a [List].
     *
     *
     * If one of the actions fails, the others will be cancelled.
     * To handle failures individually instead of cancelling you can use [.mapToResult].
     *
     * @param  first
     * The first other action to accumulate into the list
     * @param  other
     * The other actions to accumulate into the list
     *
     * @throws IllegalArgumentException
     * If null is provided or you tried to combine an action with itself
     *
     * @return Combined RestAction with empty result
     *
     * @see .allOf
     * @see .and
     * @since  4.2.1
     */
    @Nonnull
    @CheckReturnValue
    fun zip(@Nonnull first: RestAction<out T>?, @Nonnull vararg other: RestAction<out T>): RestAction<List<T>?>? {
        Checks.notNull(first, "RestAction")
        Checks.noneNull(other, "RestAction")
        val list: MutableList<RestAction<out T>?> = ArrayList()
        list.add(this)
        list.add(first)
        Collections.addAll(list, *other)
        return allOf(list)
    }

    /**
     * Intermediate operator that returns a modified RestAction.
     *
     *
     * This does not modify this instance but returns a new RestAction which will delay its result by the provided delay.
     *
     *
     * **Example**<br></br>
     * <pre>`public RestAction<Void> selfDestruct(MessageChannel channel, String content) {
     * return channel.sendMessage("The following message will destroy itself in 1 minute!")
     * .delay(Duration.ofSeconds(10)) // edit 10 seconds later
     * .flatMap((it) -> it.editMessage(content))
     * .delay(Duration.ofMinutes(1)) // delete 1 minute later
     * .flatMap(Message::delete);
     * }
    `</pre> *
     *
     * @param  duration
     * The delay
     *
     * @return RestAction with delay
     *
     * @see .queueAfter
     * @since  4.1.1
     */
    @Nonnull
    @CheckReturnValue
    fun delay(@Nonnull duration: Duration): RestAction<T>? {
        return delay(duration, null)
    }

    /**
     * Intermediate operator that returns a modified RestAction.
     *
     *
     * This does not modify this instance but returns a new RestAction which will delay its result by the provided delay.
     *
     *
     * **Example**<br></br>
     * <pre>`public RestAction<Void> selfDestruct(MessageChannel channel, String content) {
     * return channel.sendMessage("The following message will destroy itself in 1 minute!")
     * .delay(Duration.ofSeconds(10), scheduler) // edit 10 seconds later
     * .flatMap((it) -> it.editMessage(content))
     * .delay(Duration.ofMinutes(1), scheduler) // delete 1 minute later
     * .flatMap(Message::delete);
     * }
    `</pre> *
     *
     * @param  duration
     * The delay
     * @param  scheduler
     * The scheduler to use, null to use [JDA.getRateLimitPool]
     *
     * @return RestAction with delay
     *
     * @see .queueAfter
     * @since  4.1.1
     */
    @Nonnull
    @CheckReturnValue
    fun delay(@Nonnull duration: Duration, scheduler: ScheduledExecutorService?): RestAction<T>? {
        Checks.notNull(duration, "Duration")
        return DelayRestAction(this, TimeUnit.MILLISECONDS, duration.toMillis(), scheduler)
    }

    /**
     * Intermediate operator that returns a modified RestAction.
     *
     *
     * This does not modify this instance but returns a new RestAction which will delay its result by the provided delay.
     *
     *
     * **Example**<br></br>
     * <pre>`public RestAction<Void> selfDestruct(MessageChannel channel, String content) {
     * return channel.sendMessage("The following message will destroy itself in 1 minute!")
     * .delay(10, SECONDS) // edit 10 seconds later
     * .flatMap((it) -> it.editMessage(content))
     * .delay(1, MINUTES) // delete 1 minute later
     * .flatMap(Message::delete);
     * }
    `</pre> *
     *
     * @param  delay
     * The delay value
     * @param  unit
     * The time unit for the delay value
     *
     * @return RestAction with delay
     *
     * @see .queueAfter
     * @since  4.1.1
     */
    @Nonnull
    @CheckReturnValue
    fun delay(delay: Long, @Nonnull unit: TimeUnit?): RestAction<T>? {
        return delay(delay, unit, null)
    }

    /**
     * Intermediate operator that returns a modified RestAction.
     *
     *
     * This does not modify this instance but returns a new RestAction which will delay its result by the provided delay.
     *
     *
     * **Example**<br></br>
     * <pre>`public RestAction<Void> selfDestruct(MessageChannel channel, String content) {
     * return channel.sendMessage("The following message will destroy itself in 1 minute!")
     * .delay(10, SECONDS, scheduler) // edit 10 seconds later
     * .flatMap((it) -> it.editMessage(content))
     * .delay(1, MINUTES, scheduler) // delete 1 minute later
     * .flatMap(Message::delete);
     * }
    `</pre> *
     *
     * @param  delay
     * The delay value
     * @param  unit
     * The time unit for the delay value
     * @param  scheduler
     * The scheduler to use, null to use [JDA.getRateLimitPool]
     *
     * @return RestAction with delay
     *
     * @see .queueAfter
     * @since  4.1.1
     */
    @Nonnull
    @CheckReturnValue
    fun delay(delay: Long, @Nonnull unit: TimeUnit?, scheduler: ScheduledExecutorService?): RestAction<T>? {
        Checks.notNull(unit, "TimeUnit")
        return DelayRestAction(this, unit, delay, scheduler)
    }

    /**
     * Schedules a call to [.queue] to be executed after the specified `delay`.
     * <br></br>This is an **asynchronous** operation that will return a
     * [CompletableFuture] representing the task.
     *
     *
     * Similar to [.queueAfter] but does not require callbacks to be passed.
     * Continuations of [CompletableFuture] can be used instead.
     *
     *
     * The global JDA RateLimit [ScheduledExecutorService][java.util.concurrent.ScheduledExecutorService]
     * is used for this operation.
     * <br></br>You can provide your own Executor using [.submitAfter]!
     *
     * @param  delay
     * The delay after which this computation should be executed, negative to execute immediately
     * @param  unit
     * The [TimeUnit][java.util.concurrent.TimeUnit] to convert the specified `delay`
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided TimeUnit is `null`
     *
     * @return [DelayedCompletableFuture]
     * representing the delayed operation
     */
    @Nonnull
    fun submitAfter(delay: Long, @Nonnull unit: TimeUnit?): DelayedCompletableFuture<T>? {
        return submitAfter(delay, unit, null)
    }

    /**
     * Schedules a call to [.queue] to be executed after the specified `delay`.
     * <br></br>This is an **asynchronous** operation that will return a
     * [CompletableFuture] representing the task.
     *
     *
     * Similar to [.queueAfter] but does not require callbacks to be passed.
     * Continuations of [CompletableFuture] can be used instead.
     *
     *
     * The specified [ScheduledExecutorService][java.util.concurrent.ScheduledExecutorService] is used for this operation.
     *
     * @param  delay
     * The delay after which this computation should be executed, negative to execute immediately
     * @param  unit
     * The [TimeUnit][java.util.concurrent.TimeUnit] to convert the specified `delay`
     * @param  executor
     * The [ScheduledExecutorService][java.util.concurrent.ScheduledExecutorService] that should be used
     * to schedule this operation, or null to use the default
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided TimeUnit is `null`
     *
     * @return [DelayedCompletableFuture]
     * representing the delayed operation
     */
    @Nonnull
    fun submitAfter(
        delay: Long,
        @Nonnull unit: TimeUnit?,
        executor: ScheduledExecutorService?
    ): DelayedCompletableFuture<T>? {
        var executor = executor
        Checks.notNull(unit, "TimeUnit")
        if (executor == null) executor = jDA.rateLimitPool
        return DelayedCompletableFuture.make(executor, delay, unit!!,
            Function<DelayedCompletableFuture<T>, Runnable> { task: DelayedCompletableFuture<T> ->
                val onFailure: Consumer<in Throwable>
                onFailure =
                    if (isPassContext) here { ex: Throwable? -> task.completeExceptionally(ex) } else Consumer { ex: Throwable? ->
                        task.completeExceptionally(ex)
                    }
                ContextRunnable<T> { queue({ value: T -> task.complete(value) }, onFailure) }
            })
    }

    /**
     * Blocks the current Thread for the specified delay and calls [.complete]
     * when delay has been reached.
     * <br></br>If the specified delay is negative this action will execute immediately. (see: [TimeUnit.sleep])
     *
     * @param  delay
     * The delay after which to execute a call to [.complete]
     * @param  unit
     * The [TimeUnit][java.util.concurrent.TimeUnit] which should be used
     * (this will use [unit.sleep(delay)][java.util.concurrent.TimeUnit.sleep])
     *
     * @throws java.lang.IllegalArgumentException
     * If the specified [TimeUnit][java.util.concurrent.TimeUnit] is `null`
     * @throws java.lang.RuntimeException
     * If the sleep operation is interrupted
     *
     * @return The response value
     */
    fun completeAfter(delay: Long, @Nonnull unit: TimeUnit): T {
        Checks.notNull(unit, "TimeUnit")
        return try {
            unit.sleep(delay)
            complete()
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
    }

    /**
     * Schedules a call to [.queue] to be executed after the specified `delay`.
     * <br></br>This is an **asynchronous** operation that will return a
     * [ScheduledFuture][java.util.concurrent.ScheduledFuture] representing the task.
     *
     *
     * This operation gives no access to the response value.
     * <br></br>Use [.queueAfter] to access
     * the success consumer for [.queue]!
     *
     *
     * The global JDA [ScheduledExecutorService][java.util.concurrent.ScheduledExecutorService] is used for this operation.
     * <br></br>You can provide your own Executor with [.queueAfter]
     *
     * @param  delay
     * The delay after which this computation should be executed, negative to execute immediately
     * @param  unit
     * The [TimeUnit][java.util.concurrent.TimeUnit] to convert the specified `delay`
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided TimeUnit is `null`
     *
     * @return [ScheduledFuture][java.util.concurrent.ScheduledFuture]
     * representing the delayed operation
     */
    @Nonnull
    fun queueAfter(delay: Long, @Nonnull unit: TimeUnit?): ScheduledFuture<*>? {
        return queueAfter(delay, unit, null, null, null)
    }

    /**
     * Schedules a call to [.queue] to be executed after the specified `delay`.
     * <br></br>This is an **asynchronous** operation that will return a
     * [ScheduledFuture][java.util.concurrent.ScheduledFuture] representing the task.
     *
     *
     * This operation gives no access to the failure callback.
     * <br></br>Use [.queueAfter] to access
     * the failure consumer for [.queue]!
     *
     *
     * The global JDA [ScheduledExecutorService][java.util.concurrent.ScheduledExecutorService] is used for this operation.
     * <br></br>You can provide your own Executor with [.queueAfter]
     *
     * @param  delay
     * The delay after which this computation should be executed, negative to execute immediately
     * @param  unit
     * The [TimeUnit][java.util.concurrent.TimeUnit] to convert the specified `delay`
     * @param  success
     * The success [Consumer][java.util.function.Consumer] that should be called
     * once the [.queue] operation completes successfully.
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided TimeUnit is `null`
     *
     * @return [ScheduledFuture][java.util.concurrent.ScheduledFuture]
     * representing the delayed operation
     */
    @Nonnull
    fun queueAfter(delay: Long, @Nonnull unit: TimeUnit?, success: Consumer<in T>?): ScheduledFuture<*>? {
        return queueAfter(delay, unit, success, null, null)
    }

    /**
     * Schedules a call to [.queue]
     * to be executed after the specified `delay`.
     * <br></br>This is an **asynchronous** operation that will return a
     * [ScheduledFuture][java.util.concurrent.ScheduledFuture] representing the task.
     *
     *
     * The global JDA [ScheduledExecutorService][java.util.concurrent.ScheduledExecutorService] is used for this operation.
     * <br></br>You provide your own Executor with [.queueAfter]
     *
     * @param  delay
     * The delay after which this computation should be executed, negative to execute immediately
     * @param  unit
     * The [TimeUnit][java.util.concurrent.TimeUnit] to convert the specified `delay`
     * @param  success
     * The success [Consumer][java.util.function.Consumer] that should be called
     * once the [.queue] operation completes successfully.
     * @param  failure
     * The failure [Consumer][java.util.function.Consumer] that should be called
     * in case of an error of the [.queue] operation.
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided TimeUnit is `null`
     *
     * @return [ScheduledFuture][java.util.concurrent.ScheduledFuture]
     * representing the delayed operation
     *
     * @see net.dv8tion.jda.api.exceptions.ErrorHandler
     */
    @Nonnull
    fun queueAfter(
        delay: Long,
        @Nonnull unit: TimeUnit?,
        success: Consumer<in T>?,
        failure: Consumer<in Throwable>?
    ): ScheduledFuture<*>? {
        return queueAfter(delay, unit, success, failure, null)
    }

    /**
     * Schedules a call to [.queue] to be executed after the specified `delay`.
     * <br></br>This is an **asynchronous** operation that will return a
     * [ScheduledFuture][java.util.concurrent.ScheduledFuture] representing the task.
     *
     *
     * This operation gives no access to the response value.
     * <br></br>Use [.queueAfter] to access
     * the success consumer for [.queue]!
     *
     *
     * The specified [ScheduledExecutorService][java.util.concurrent.ScheduledExecutorService] is used for this operation.
     *
     * @param  delay
     * The delay after which this computation should be executed, negative to execute immediately
     * @param  unit
     * The [TimeUnit][java.util.concurrent.TimeUnit] to convert the specified `delay`
     * @param  executor
     * The Non-null [ScheduledExecutorService][java.util.concurrent.ScheduledExecutorService] that should be used
     * to schedule this operation
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided TimeUnit or ScheduledExecutorService is `null`
     *
     * @return [ScheduledFuture][java.util.concurrent.ScheduledFuture]
     * representing the delayed operation
     */
    @Nonnull
    fun queueAfter(delay: Long, @Nonnull unit: TimeUnit?, executor: ScheduledExecutorService?): ScheduledFuture<*>? {
        return queueAfter(delay, unit, null, null, executor)
    }

    /**
     * Schedules a call to [.queue] to be executed after the specified `delay`.
     * <br></br>This is an **asynchronous** operation that will return a
     * [ScheduledFuture][java.util.concurrent.ScheduledFuture] representing the task.
     *
     *
     * This operation gives no access to the failure callback.
     * <br></br>Use [.queueAfter] to access
     * the failure consumer for [.queue]!
     *
     *
     * The specified [ScheduledExecutorService][java.util.concurrent.ScheduledExecutorService] is used for this operation.
     *
     * @param  delay
     * The delay after which this computation should be executed, negative to execute immediately
     * @param  unit
     * The [TimeUnit][java.util.concurrent.TimeUnit] to convert the specified `delay`
     * @param  success
     * The success [Consumer][java.util.function.Consumer] that should be called
     * once the [.queue] operation completes successfully.
     * @param  executor
     * The Non-null [ScheduledExecutorService][java.util.concurrent.ScheduledExecutorService] that should be used
     * to schedule this operation
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided TimeUnit or ScheduledExecutorService is `null`
     *
     * @return [ScheduledFuture][java.util.concurrent.ScheduledFuture]
     * representing the delayed operation
     */
    @Nonnull
    fun queueAfter(
        delay: Long,
        @Nonnull unit: TimeUnit?,
        success: Consumer<in T>?,
        executor: ScheduledExecutorService?
    ): ScheduledFuture<*>? {
        return queueAfter(delay, unit, success, null, executor)
    }

    /**
     * Schedules a call to [.queue]
     * to be executed after the specified `delay`.
     * <br></br>This is an **asynchronous** operation that will return a
     * [ScheduledFuture][java.util.concurrent.ScheduledFuture] representing the task.
     *
     *
     * The specified [ScheduledExecutorService][java.util.concurrent.ScheduledExecutorService] is used for this operation.
     *
     * @param  delay
     * The delay after which this computation should be executed, negative to execute immediately
     * @param  unit
     * The [TimeUnit][java.util.concurrent.TimeUnit] to convert the specified `delay`
     * @param  success
     * The success [Consumer][java.util.function.Consumer] that should be called
     * once the [.queue] operation completes successfully.
     * @param  failure
     * The failure [Consumer][java.util.function.Consumer] that should be called
     * in case of an error of the [.queue] operation.
     * @param  executor
     * The Non-null [ScheduledExecutorService][java.util.concurrent.ScheduledExecutorService] that should be used
     * to schedule this operation
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided TimeUnit or ScheduledExecutorService is `null`
     *
     * @return [ScheduledFuture][java.util.concurrent.ScheduledFuture]
     * representing the delayed operation
     *
     * @see net.dv8tion.jda.api.exceptions.ErrorHandler
     */
    @Nonnull
    fun queueAfter(
        delay: Long,
        @Nonnull unit: TimeUnit?,
        success: Consumer<in T>?,
        failure: Consumer<in Throwable>?,
        executor: ScheduledExecutorService?
    ): ScheduledFuture<*>? {
        var executor = executor
        Checks.notNull(unit, "TimeUnit")
        if (executor == null) executor = jDA.rateLimitPool
        val onFailure: Consumer<in Throwable>?
        onFailure = if (isPassContext) here(failure ?: defaultFailure) else failure
        val task: Runnable = ContextRunnable<Void> { queue(success, onFailure) }
        return executor.schedule(task, delay, unit)
    }

    companion object {
        @JvmStatic
        var isPassContext: Boolean
            /**
             * Whether RestActions will use [ContextException][net.dv8tion.jda.api.exceptions.ContextException]
             * automatically to keep track of the caller context.
             * <br></br>If set to `true` this can cause performance drops due to the creation of stack-traces on execution.
             *
             * @return True, if RestActions will keep track of context automatically
             *
             * @see .setPassContext
             */
            get() = RestActionImpl.isPassContext()
            /**
             * If enabled this will pass a [ContextException][net.dv8tion.jda.api.exceptions.ContextException]
             * as root-cause to all failure consumers.
             * <br></br>This might cause performance decrease due to the creation of exceptions for **every** execution.
             *
             *
             * It is recommended to pass a context consumer as failure manually using `queue(success, ContextException.here(failure))`
             *
             * @param  enable
             * True, if context should be passed to all failure consumers
             */
            set(enable) {
                RestActionImpl.setPassContext(enable)
            }

        /**
         * Default timeout to apply to every RestAction.
         * <br></br>This will use no timeout unless specified otherwise.
         * <br></br>If the request doesn't get executed within the specified timeout it will fail.
         *
         *
         * When a RestAction times out, it will fail with a [TimeoutException][java.util.concurrent.TimeoutException].
         *
         * @param  timeout
         * The default timeout to use
         * @param  unit
         * [Unit][TimeUnit] for the timeout value
         *
         * @throws IllegalArgumentException
         * If the provided unit is null
         */
        fun setDefaultTimeout(timeout: Long, @Nonnull unit: TimeUnit?) {
            RestActionImpl.setDefaultTimeout(timeout, unit!!)
        }

        val defaultTimeout: Long
            /**
             * The default timeout to apply to every RestAction in milliseconds.
             * <br></br>If no timeout has been configured, this will return 0.
             *
             *
             * When a RestAction times out, it will fail with a [TimeoutException][java.util.concurrent.TimeoutException].
             *
             * @return The default timeout in milliseconds, or 0
             */
            get() = RestActionImpl.getDefaultTimeout()

        @JvmStatic
        @get:Nonnull
        var defaultFailure: Consumer<in Throwable?>?
            /**
             * The default failure callback used when none is provided in [.queue].
             *
             * @return The fallback consumer
             */
            get() = RestActionImpl.getDefaultFailure()
            /**
             * The default failure callback used when none is provided in [.queue].
             *
             * @param callback
             * The fallback to use, or null to ignore failures (not recommended)
             */
            set(callback) {
                RestActionImpl.setDefaultFailure(callback)
            }

        @JvmStatic
        @get:Nonnull
        var defaultSuccess: Consumer<Any?>?
            /**
             * The default success callback used when none is provided in [.queue] or [.queue].
             *
             * @return The fallback consumer
             */
            get() = RestActionImpl.getDefaultSuccess()
            /**
             * The default success callback used when none is provided in [.queue] or [.queue].
             *
             * @param callback
             * The fallback to use, or null to ignore success
             */
            set(callback) {
                RestActionImpl.setDefaultSuccess(callback)
            }

        /**
         * Creates a RestAction instance which accumulates all results of the provided actions.
         * <br></br>If one action fails, all others will be cancelled.
         * To handle failures individually instead of cancelling you can use [.mapToResult].
         *
         * @param  first
         * The initial RestAction starting point
         * @param  others
         * The remaining actions to accumulate
         * @param  <E>
         * The result type
         *
         * @throws IllegalArgumentException
         * If null is provided
         *
         * @return RestAction - Type: [List] of the results
         *
         * @see .and
         * @see .zip
         * @since  4.2.1
        </E> */
        @Nonnull
        @SafeVarargs
        @CheckReturnValue
        fun <E> allOf(
            @Nonnull first: RestAction<out E>?,
            @Nonnull vararg others: RestAction<out E>
        ): RestAction<List<E>?>? {
            Checks.notNull(first, "RestAction")
            Checks.noneNull(others, "RestAction")
            val list: MutableList<RestAction<out E>?> = ArrayList(others.size + 1)
            list.add(first)
            Collections.addAll(list, *others)
            return allOf(list)
        }

        /**
         * Creates a RestAction instance which accumulates all results of the provided actions.
         * <br></br>If one action fails, all others will be cancelled.
         * To handle failures individually instead of cancelling you can use [.mapToResult].
         *
         * @param  actions
         * Non-empty collection of RestActions to accumulate
         * @param  <E>
         * The result type
         *
         * @throws IllegalArgumentException
         * If null is provided or the collection is empty
         *
         * @return RestAction - Type: [List] of the results
         *
         * @see .and
         * @see .zip
         * @since  4.2.1
        </E> */
        @Nonnull
        @CheckReturnValue
        fun <E> allOf(@Nonnull actions: Collection<RestAction<out E>?>): RestAction<List<E>?>? {
            return accumulate<E, Any, List<E>?>(actions, Collectors.toList())
        }

        /**
         * Creates a RestAction instance which accumulates all results of the provided actions.
         * <br></br>If one action fails, all others will be cancelled.
         * To handle failures individually instead of cancelling you can use [.mapToResult].
         *
         * @param  actions
         * Non-empty collection of RestActions to accumulate
         * @param  collector
         * The [Collector] to use
         * @param  <E>
         * The input type
         * @param  <A>
         * The accumulator type
         * @param  <O>
         * The output type
         *
         * @throws IllegalArgumentException
         * If null is provided or the collection is empty
         *
         * @return RestAction - Type: [List] of the results
         *
         * @see .and
         * @see .zip
         * @since  4.2.1
        </O></A></E> */
        @Nonnull
        @CheckReturnValue
        fun <E, A, O> accumulate(
            @Nonnull actions: Collection<RestAction<out E?>?>,
            @Nonnull collector: Collector<in E?, A, out O>
        ): RestAction<O>? {
            var actions = actions
            Checks.noneNull(actions, "RestAction")
            Checks.notEmpty(actions, "RestActions")
            Checks.notNull(collector, "Collector")
            val accumulator = collector.supplier()
            val add = collector.accumulator()
            val output = collector.finisher()
            actions = LinkedHashSet(actions)
            val iterator = actions.iterator()
            var result = iterator.next()!!.map<A> { it: E? ->
                val list = accumulator.get()
                add.accept(list, it)
                list
            }
            while (iterator.hasNext()) {
                val next = iterator.next()
                result = result.and(next) { list: A, b: E? ->
                    add.accept(list, b)
                    list
                }
            }
            return result.map(output)
        }
    }
}
