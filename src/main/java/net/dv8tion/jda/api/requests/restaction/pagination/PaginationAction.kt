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
package net.dv8tion.jda.api.requests.restaction.pagination

import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.utils.Procedure
import net.dv8tion.jda.internal.requests.RestActionImpl
import net.dv8tion.jda.internal.utils.Checks
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.function.BooleanSupplier
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.function.Supplier
import java.util.stream.Stream
import java.util.stream.StreamSupport
import javax.annotation.Nonnull

/**
 * [RestAction][net.dv8tion.jda.api.requests.RestAction] specification used
 * to retrieve entities for paginated endpoints (before, after, limit).
 * <br></br>Note that this implementation is not considered thread-safe as modifications to the cache are not done
 * with a lock. Calling methods on this class from multiple threads is not recommended.
 *
 *
 * **Examples**
 * <pre>`
 * / **
 * * Retrieves messages until the specified limit is reached. The messages will be limited after being filtered by the user.
 * * If the user hasn't sent enough messages this will go through all messages so it is recommended to add an additional end condition.
 * *&#47;
 * public static List<Message> getMessagesByUser(MessageChannel channel, User user, int limit)
 * {
 * <u>MessagePaginationAction</u> action = channel.<u>getIterableHistory</u>();
 * Stream<Message> messageStream = action.stream()
 * .limit(limit * 2) // used to limit amount of messages to check, if user hasn't sent enough messages it would go on forever
 * .filter( message-> message.getAuthor().equals(user) )
 * .limit(limit); // limit on filtered stream will be checked independently from previous limit
 * return messageStream.collect(Collectors.toList());
 * }
`</pre> *
 *
 * <pre>`
 * / **
 * * Iterates messages in an async stream and stops once the limit has been reached.
 * *&#47;
 * public static void onEachMessageAsync(MessageChannel channel, Consumer<Message> consumer, int limit)
 * {
 * if (limit< 1)
 * return;
 * <u>MessagePaginationAction</u> action = channel.<u>getIterableHistory</u>();
 * AtomicInteger counter = new AtomicInteger(limit);
 * action.forEachAsync( (message)->
 * {
 * consumer.accept(message);
 * // if false the iteration is terminated; else it continues
 * return counter.decrementAndGet() == 0;
 * });
 * }
`</pre> *
 *
 * @param  <M>
 * The current implementation used as chaining return value
 * @param  <T>
 * The type of entity to paginate
 *
 * @since  3.1
</T></M> */
interface PaginationAction<T, M : PaginationAction<T, M>?> : RestAction<List<T>?>, Iterable<T> {
    /**
     * Skips past the specified ID for successive requests.
     * This will reset the [.getLast] entity and cause a [NoSuchElementException] to be thrown
     * when attempting to get the last entity until a new retrieve action has been done.
     * <br></br>If cache is disabled this can be set to an arbitrary value irrelevant of the current last.
     * Set this to `0` to start from the most recent message.
     *
     *
     * Fails if cache is enabled and the target id is newer than the current last id (id &gt; last).
     *
     *
     * **Example**<br></br>
     * <pre>`public MessagePaginationAction getOlderThan(MessageChannel channel, long time) {
     * final long timestamp = TimeUtil.getDiscordTimestamp(time);
     * final MessagePaginationAction paginator = channel.getIterableHistory();
     * return paginator.skipTo(timestamp);
     * }
     *
     * getOlderThan(channel, System.currentTimeMillis() - TimeUnit.DAYS.toMillis(14))
     * .forEachAsync((message) -> {
     * boolean empty = message.getContentRaw().isEmpty();
     * if (!empty)
     * System.out.printf("%#s%n", message); // means: print display content
     * return !empty; // means: continue if not empty
     * });
    `</pre> *
     *
     * @param  id
     * The snowflake ID to skip before, this is exclusive rather than inclusive
     *
     * @throws IllegalArgumentException
     * If cache is enabled, and you are attempting to skip forward in time (id &gt; last)
     *
     * @return The current PaginationAction for chaining convenience
     *
     * @see java.util.concurrent.TimeUnit
     *
     * @see net.dv8tion.jda.api.utils.TimeUtil
     */
    @Nonnull
    fun skipTo(id: Long): M

    /**
     * The current iteration anchor used for pagination.
     * <br></br>This is updated by each retrieve action.
     *
     * @return The current iteration anchor
     *
     * @see .skipTo
     */
    val lastKey: Long
    @Nonnull
    override fun setCheck(checks: BooleanSupplier?): RestAction<List<T>?>
    @Nonnull
    override fun timeout(timeout: Long, @Nonnull unit: TimeUnit): RestAction<List<T>?>
    @Nonnull
    override fun deadline(timestamp: Long): RestAction<List<T>?>

    @get:Nonnull
    val supportedOrders: EnumSet<PaginationOrder?>?
        /**
         * The supported [PaginationOrders][PaginationOrder] for this pagination action.
         * <br></br>All enum values that are not returned will cause a throw for [.order].
         *
         *
         * Most pagination endpoints only support a single order, however some endpoints such as message pagination supports both.
         *
         * @return [EnumSet] of [PaginationOrder] (Modifying this set does not affect this class)
         */
        get() = EnumSet.allOf(PaginationOrder::class.java)

    @get:Nonnull
    val order: PaginationOrder

    /**
     * Configure the [PaginationOrder] of this pagination action.
     *
     *
     * You can only supply supported orders, see [.getSupportedOrders].
     *
     * @param  order
     * The pagination order
     *
     * @throws IllegalArgumentException
     * If the provided pagination order is null or unsupported
     * @throws IllegalStateException
     * If this pagination action has already been used to retrieve entities
     *
     * @return The current PaginationAction implementation instance
     *
     * @see .getSupportedOrders
     * @see .reverse
     */
    @Nonnull
    fun order(@Nonnull order: PaginationOrder?): M

    /**
     * Flips the [.order] of this pagination action.
     *
     * @throws IllegalArgumentException
     * If this pagination action does not support the reversed order
     *
     * @return The current PaginationAction implementation instance
     */
    @Nonnull
    fun reverse(): M {
        return if (order == PaginationOrder.BACKWARD) order(PaginationOrder.FORWARD) else order(PaginationOrder.BACKWARD)
    }

    /**
     * The current amount of cached entities for this PaginationAction
     *
     * @return int size of currently cached entities
     */
    fun cacheSize(): Int

    /**
     * Whether the cache of this PaginationAction is empty.
     * <br></br>Logically equivalent to `cacheSize() == 0`.
     *
     * @return True, if no entities have been retrieved yet.
     */
    val isEmpty: Boolean

    @get:Nonnull
    val cached: List<T>?

    @get:Nonnull
    val last: T

    @get:Nonnull
    val first: T

    /**
     * Sets the limit that should be used in the next RestAction completion
     * call.
     *
     *
     * The specified limit may not be below the [Minimum Limit][.getMinLimit] nor above
     * the [Maximum Limit][.getMaxLimit]. Unless these limits are specifically omitted. (See documentation of methods)
     *
     *
     * **This limit represents how many entities will be retrieved per request and
     * <u>NOT</u> the maximum amount of entities that should be retrieved for iteration/sequencing.**
     * <br></br>`action.limit(50).complete()`
     * <br></br>is not the same as
     * <br></br>`action.stream().limit(50).collect(collector)`
     *
     *
     * @param  limit
     * The limit to use
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided limit is out of range
     *
     * @return The current PaginationAction implementation instance
     */
    @Nonnull
    fun limit(limit: Int): M

    /**
     * Whether already retrieved entities should be stored
     * within the internal cache. All cached entities will be
     * available from [.getCached].
     * **Default: true**
     * <br></br>This being disabled allows unused entities to be removed from
     * the memory heap by the garbage collector. If this is enabled this will not
     * take place until all references to this PaginationAction have been cleared.
     *
     * @param  enableCache
     * Whether to enable entity cache
     *
     * @return The current PaginationAction implementation instance
     */
    @Nonnull
    fun cache(enableCache: Boolean): M

    /**
     * Whether retrieved entities are stored within an
     * internal cache. If this is `false` entities
     * retrieved by the iterator or a call to a [RestAction][net.dv8tion.jda.api.requests.RestAction]
     * terminal operation will not be retrievable from [.getCached].
     * <br></br>This being disabled allows unused entities to be removed from
     * the memory heap by the garbage collector. If this is enabled this will not
     * take place until all references to this PaginationAction have been cleared.
     *
     * @return True, If entities will be cached.
     */
    val isCacheEnabled: Boolean

    /**
     * The maximum limit that can be used for this PaginationAction
     * <br></br>Limits provided to [.limit] must not be greater
     * than the returned value.
     * <br></br>If no maximum limit is used this will return `0`.
     * That means there is no upper border for limiting this PaginationAction
     *
     * @return The maximum limit
     */
    val maxLimit: Int

    /**
     * The minimum limit that can be used for this PaginationAction
     * <br></br>Limits provided to [.limit] must not be less
     * than the returned value.
     * <br></br>If no minimum limit is used this will return `0`.
     * That means there is no lower border for limiting this PaginationAction
     *
     * @return The minimum limit
     */
    val minLimit: Int

    /**
     * The currently used limit.
     * <br></br>If this PaginationAction does not use limitation
     * this will return `0`
     *
     * @return limit
     */
    val limit: Int

    /**
     * Retrieves elements while the specified condition is met.
     *
     * @param  rule
     * The rule which must be fulfilled for an element to be added,
     * returns false to discard the element and finish the task
     *
     * @throws IllegalArgumentException
     * If the provided rule is `null`
     *
     * @return [CompletableFuture] - Type: [List]
     * <br></br>Future representing the fetch task, the list will be sorted most recent to oldest
     *
     * @see .takeWhileAsync
     * @see .takeUntilAsync
     */
    @Nonnull
    fun takeWhileAsync(@Nonnull rule: Predicate<in T>): CompletableFuture<List<T>?>? {
        Checks.notNull(rule, "Rule")
        return takeUntilAsync(rule.negate())
    }

    /**
     * Retrieves elements while the specified condition is met.
     *
     * @param  limit
     * The maximum amount of elements to collect or `0` for no limit
     * @param  rule
     * The rule which must be fulfilled for an element to be added,
     * returns false to discard the element and finish the task
     *
     * @throws IllegalArgumentException
     * If the provided rule is `null` or the limit is negative
     *
     * @return [CompletableFuture] - Type: [List]
     * <br></br>Future representing the fetch task, the list will be sorted most recent to oldest
     *
     * @see .takeWhileAsync
     * @see .takeUntilAsync
     */
    @Nonnull
    fun takeWhileAsync(limit: Int, @Nonnull rule: Predicate<in T>): CompletableFuture<List<T>?>? {
        Checks.notNull(rule, "Rule")
        return takeUntilAsync(limit, rule.negate())
    }

    /**
     * Retrieves elements until the specified condition is met.
     *
     * @param  rule
     * The rule which must be fulfilled for an element to be discarded,
     * returns true to discard the element and finish the task
     *
     * @throws IllegalArgumentException
     * If the provided rule is `null`
     *
     * @return [CompletableFuture] - Type: [List]
     * <br></br>Future representing the fetch task, the list will be sorted most recent to oldest
     *
     * @see .takeWhileAsync
     * @see .takeUntilAsync
     */
    @Nonnull
    fun takeUntilAsync(@Nonnull rule: Predicate<in T>): CompletableFuture<List<T>?>? {
        return takeUntilAsync(0, rule)
    }

    /**
     * Retrieves elements until the specified condition is met.
     *
     * @param  limit
     * The maximum amount of elements to collect or `0` for no limit
     * @param  rule
     * The rule which must be fulfilled for an element to be discarded,
     * returns true to discard the element and finish the task
     *
     * @throws IllegalArgumentException
     * If the provided rule is `null` or the limit is negative
     *
     * @return [CompletableFuture] - Type: [List]
     * <br></br>Future representing the fetch task, the list will be sorted most recent to oldest
     *
     * @see .takeWhileAsync
     * @see .takeUntilAsync
     */
    @Nonnull
    fun takeUntilAsync(limit: Int, @Nonnull rule: Predicate<in T>): CompletableFuture<List<T>?>? {
        Checks.notNull(rule, "Rule")
        Checks.notNegative(limit, "Limit")
        val result: MutableList<T> = ArrayList()
        val future = CompletableFuture<List<T>?>()
        val handle = forEachAsync { element: T ->
            if (rule.test(element)) return@forEachAsync false
            result.add(element)
            limit == 0 || limit > result.size
        }
        handle.whenComplete { r: Any?, t: Throwable? ->
            if (t != null) future.completeExceptionally(t) else future.complete(
                result
            )
        }
        return future
    }

    /**
     * Convenience method to retrieve an amount of entities from this pagination action.
     * <br></br>This also includes already cached entities similar to [.forEachAsync].
     *
     * @param  amount
     * The maximum amount to retrieve
     *
     * @return [CompletableFuture][java.util.concurrent.CompletableFuture] - Type: [List][java.util.List]
     *
     * @see .forEachAsync
     */
    @Nonnull
    fun takeAsync(amount: Int): CompletableFuture<List<T>?>?

    /**
     * Convenience method to retrieve an amount of entities from this pagination action.
     * <br></br>Unlike [.takeAsync] this does not include already cached entities.
     *
     * @param  amount
     * The maximum amount to retrieve
     *
     * @return [CompletableFuture][java.util.concurrent.CompletableFuture] - Type: [List][java.util.List]
     *
     * @see .forEachRemainingAsync
     */
    @Nonnull
    fun takeRemainingAsync(amount: Int): CompletableFuture<List<T>?>?

    /**
     * Iterates over all entities until the provided action returns `false`!
     * <br></br>This operation is different from [.forEach] as it
     * uses successive [.queue] tasks to iterate each entity in callback threads instead of
     * the calling active thread.
     * This means that this method fully works on different threads to retrieve new entities.
     *
     * **This iteration will include already cached entities, in order to exclude cached
     * entities use [.forEachRemainingAsync]**
     *
     *
     * **Example**<br></br>
     * <pre>`//deletes messages until it finds a user that is still in guild
     * public void cleanupMessages(MessagePaginationAction action)
     * {
     * action.forEachAsync( (message) ->
     * {
     * Guild guild = message.getGuild();
     * if (!guild.isMember(message.getAuthor()))
     * message.delete().queue();
     * else
     * return false;
     * return true;
     * });
     * }
    `</pre> *
     *
     * @param  action
     * [Procedure][net.dv8tion.jda.api.utils.Procedure] returning `true` if iteration should continue!
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided Procedure is `null`
     *
     * @return [Future][java.util.concurrent.Future] that can be cancelled to stop iteration from outside!
     */
    @Nonnull
    fun forEachAsync(@Nonnull action: Procedure<in T>?): CompletableFuture<*> {
        return forEachAsync(action, RestActionImpl.getDefaultFailure())
    }

    /**
     * Iterates over all entities until the provided action returns `false`!
     * <br></br>This operation is different from [.forEach] as it
     * uses successive [.queue] tasks to iterate each entity in callback threads instead of
     * the calling active thread.
     * This means that this method fully works on different threads to retrieve new entities.
     *
     *
     * **This iteration will include already cached entities, in order to exclude cached
     * entities use [.forEachRemainingAsync]**
     *
     *
     * **Example**<br></br>
     * <pre>`//deletes messages until it finds a user that is still in guild
     * public void cleanupMessages(MessagePaginationAction action)
     * {
     * action.forEachAsync( (message) ->
     * {
     * Guild guild = message.getGuild();
     * if (!guild.isMember(message.getAuthor()))
     * message.delete().queue();
     * else
     * return false;
     * return true;
     * }, Throwable::printStackTrace);
     * }
    `</pre> *
     *
     * @param  action
     * [Procedure][net.dv8tion.jda.api.utils.Procedure] returning `true` if iteration should continue!
     * @param  failure
     * [Consumer][java.util.function.Consumer] that should handle any throwables from the action
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided Procedure or the failure Consumer is `null`
     *
     * @return [Future][java.util.concurrent.Future] that can be cancelled to stop iteration from outside!
     */
    @Nonnull
    fun forEachAsync(
        @Nonnull action: Procedure<in T>?,
        @Nonnull failure: Consumer<in Throwable?>?
    ): CompletableFuture<*>

    /**
     * Iterates over all remaining entities until the provided action returns `false`!
     * <br></br>This operation is different from [.forEachRemaining] as it
     * uses successive [.queue] tasks to iterate each entity in callback threads instead of
     * the calling active thread.
     * This means that this method fully works on different threads to retrieve new entities.
     *
     *
     * **This iteration will exclude already cached entities, in order to include cached
     * entities use [.forEachAsync]**
     *
     *
     * **Example**<br></br>
     * <pre>`//deletes messages until it finds a user that is still in guild
     * public void cleanupMessages(MessagePaginationAction action)
     * {
     * action.forEachRemainingAsync( (message) ->
     * {
     * Guild guild = message.getGuild();
     * if (!guild.isMember(message.getAuthor()))
     * message.delete().queue();
     * else
     * return false;
     * return true;
     * });
     * }
    `</pre> *
     *
     * @param  action
     * [Procedure][net.dv8tion.jda.api.utils.Procedure] returning `true` if iteration should continue!
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided Procedure is `null`
     *
     * @return [Future][java.util.concurrent.Future] that can be cancelled to stop iteration from outside!
     */
    @Nonnull
    fun forEachRemainingAsync(@Nonnull action: Procedure<in T>?): CompletableFuture<*>? {
        return forEachRemainingAsync(action, RestActionImpl.getDefaultFailure())
    }

    /**
     * Iterates over all remaining entities until the provided action returns `false`!
     * <br></br>This operation is different from [.forEachRemaining] as it
     * uses successive [.queue] tasks to iterate each entity in callback threads instead of
     * the calling active thread.
     * This means that this method fully works on different threads to retrieve new entities.
     *
     *
     * **This iteration will exclude already cached entities, in order to include cached
     * entities use [.forEachAsync]**
     *
     *
     * **Example**<br></br>
     * <pre>`//deletes messages until it finds a user that is still in guild
     * public void cleanupMessages(MessagePaginationAction action)
     * {
     * action.forEachRemainingAsync( (message) ->
     * {
     * Guild guild = message.getGuild();
     * if (!guild.isMember(message.getAuthor()))
     * message.delete().queue();
     * else
     * return false;
     * return true;
     * }, Throwable::printStackTrace);
     * }
    `</pre> *
     *
     * @param  action
     * [Procedure][net.dv8tion.jda.api.utils.Procedure] returning `true` if iteration should continue!
     * @param  failure
     * [Consumer][java.util.function.Consumer] that should handle any throwables from the action
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided Procedure or the failure Consumer is `null`
     *
     * @return [Future][java.util.concurrent.Future] that can be cancelled to stop iteration from outside!
     */
    @Nonnull
    fun forEachRemainingAsync(
        @Nonnull action: Procedure<in T>?,
        @Nonnull failure: Consumer<in Throwable?>?
    ): CompletableFuture<*>?

    /**
     * Iterates over all remaining entities until the provided action returns `false`!
     * <br></br>Skipping past already cached entities to iterate all remaining entities of this PaginationAction.
     *
     *
     * **This is a blocking operation that might take a while to complete**
     *
     * @param  action
     * The [Procedure][net.dv8tion.jda.api.utils.Procedure]
     * which should return `true` to continue iterating
     */
    fun forEachRemaining(@Nonnull action: Procedure<in T>?)
    override fun spliterator(): Spliterator<T> {
        return Spliterators.spliteratorUnknownSize(iterator(), Spliterator.IMMUTABLE)
    }

    /**
     * A sequential [Stream][java.util.stream.Stream] with this PaginationAction as its source.
     *
     * @return a sequential `Stream` over the elements in this PaginationAction
     */
    @Nonnull
    fun stream(): Stream<T>? {
        return StreamSupport.stream(spliterator(), false)
    }

    /**
     * Returns a possibly parallel [Stream][java.util.stream.Stream] with this PaginationAction as its
     * source. It is allowable for this method to return a sequential stream.
     *
     * @return a sequential `Stream` over the elements in this PaginationAction
     */
    @Nonnull
    fun parallelStream(): Stream<T>? {
        return StreamSupport.stream(spliterator(), true)
    }

    /**
     * [PaginationIterator]
     * that will iterate over all entities for this PaginationAction.
     *
     * @return new PaginationIterator
     */
    @Nonnull
    override fun iterator(): PaginationIterator<T>

    /**
     * Defines the pagination order for a pagination endpoint.
     */
    enum class PaginationOrder(
        /**
         * The API query parameter key
         *
         * @return The query key
         */
        @JvmField @get:Nonnull val key: String
    ) {
        /**
         * Iterates backwards in time, listing the most recent entities first.
         */
        BACKWARD("before"),

        /**
         * Iterates forward in time, listing the oldest entities first.
         */
        FORWARD("after")

    }

    /**
     * Iterator implementation for a [PaginationAction].
     * <br></br>This iterator will first iterate over all currently cached entities and continue to retrieve new entities
     * as needed.
     *
     *
     * To retrieve new entities after reaching the end of the current cache, this iterator will
     * request a List of new entities through a call of [RestAction.complete()][net.dv8tion.jda.api.requests.RestAction.complete].
     * <br></br>**It is recommended to use the highest possible limit for this task. (see [.limit])**
     */
    class PaginationIterator<E>(queue: Collection<E>?, supply: Supplier<List<E>>) : MutableIterator<E> {
        protected var items: Queue<E>?
        protected val supply: Supplier<List<E>>

        init {
            items = LinkedList(queue)
            this.supply = supply
        }

        override fun hasNext(): Boolean {
            if (items == null) return false
            if (!hitEnd()) return true
            if (items!!.addAll(supply.get())) return true

            // null indicates that the real end has been reached
            items = null
            return false
        }

        override fun next(): E {
            if (!hasNext()) throw NoSuchElementException("Reached End of pagination task!")
            return items!!.poll()
        }

        protected fun hitEnd(): Boolean {
            return items!!.isEmpty()
        }
    }
}
