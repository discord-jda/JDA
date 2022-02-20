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

package net.dv8tion.jda.api.requests.restaction.pagination;

import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.Procedure;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * {@link net.dv8tion.jda.api.requests.RestAction RestAction} specification used
 * to retrieve entities for paginated endpoints (before, after, limit).
 * <br>Note that this implementation is not considered thread-safe as modifications to the cache are not done
 * with a lock. Calling methods on this class from multiple threads is not recommended.
 *
 * <p><b>Examples</b>
 * <pre><code>
 * /**
 *   * Retrieves messages until the specified limit is reached. The messages will be limited after being filtered by the user.
 *   * If the user hasn't sent enough messages this will go through all messages so it is recommended to add an additional end condition.
 *   *&#47;
 * public static {@literal List<Message>} getMessagesByUser(MessageChannel channel, User user, int limit)
 * {
 *     <u>MessagePaginationAction</u> action = channel.<u>getIterableHistory</u>();
 *     Stream{@literal <Message>} messageStream = action.stream()
 *             .limit(limit * 2) // used to limit amount of messages to check, if user hasn't sent enough messages it would go on forever
 *             .filter( message{@literal ->} message.getAuthor().equals(user) )
 *             .limit(limit); // limit on filtered stream will be checked independently from previous limit
 *     return messageStream.collect(Collectors.toList());
 * }
 * </code></pre>
 *
 * <pre><code>
 * /**
 *  * Iterates messages in an async stream and stops once the limit has been reached.
 *  *&#47;
 * public static void onEachMessageAsync(MessageChannel channel, {@literal Consumer<Message>} consumer, int limit)
 * {
 *     if (limit{@literal <} 1)
 *         return;
 *     <u>MessagePaginationAction</u> action = channel.<u>getIterableHistory</u>();
 *     AtomicInteger counter = new AtomicInteger(limit);
 *     action.forEachAsync( (message){@literal ->}
 *     {
 *         consumer.accept(message);
 *         // if false the iteration is terminated; else it continues
 *         return counter.decrementAndGet() == 0;
 *     });
 * }
 * </code></pre>
 *
 * @param  <M>
 *         The current implementation used as chaining return value
 * @param  <T>
 *         The type of entity to paginate
 *
 * @since  3.1
 */
public interface PaginationAction<T, M extends PaginationAction<T, M>> extends RestAction<List<T>>, Iterable<T>
{
    /**
     * Skips past the specified ID for successive requests.
     * This will reset the {@link #getLast()} entity and cause a {@link NoSuchElementException} to be thrown
     * when attempting to get the last entity until a new retrieve action has been done.
     * <br>If cache is disabled this can be set to an arbitrary value irrelevant of the current last.
     * Set this to {@code 0} to start from the most recent message.
     *
     * <p>Fails if cache is enabled and the target id is newer than the current last id {@literal (id > last)}.
     *
     * <h4>Example</h4>
     * <pre>{@code
     * public MessagePaginationAction getOlderThan(MessageChannel channel, long time) {
     *     final long timestamp = TimeUtil.getDiscordTimestamp(time);
     *     final MessagePaginationAction paginator = channel.getIterableHistory();
     *     return paginator.skipTo(timestamp);
     * }
     *
     * getOlderThan(channel, System.currentTimeMillis() - TimeUnit.DAYS.toMillis(14))
     *     .forEachAsync((message) -> {
     *         boolean empty = message.getContentRaw().isEmpty();
     *         if (!empty)
     *             System.out.printf("%#s%n", message); // means: print display content
     *         return !empty; // means: continue if not empty
     *     });
     * }</pre>
     *
     * @param  id
     *         The snowflake ID to skip before, this is exclusive rather than inclusive
     *
     * @throws IllegalArgumentException
     *         If cache is enabled, and you are attempting to skip forward in time {@literal (id > last)}
     *
     * @return The current PaginationAction for chaining convenience
     *
     * @see    java.util.concurrent.TimeUnit
     * @see    net.dv8tion.jda.api.utils.TimeUtil
     */
    @Nonnull
    M skipTo(long id);

    /**
     * The current iteration anchor used for pagination.
     * <br>This is updated by each retrieve action.
     *
     * @return The current iteration anchor
     *
     * @see    #skipTo(long) Use skipTo(anchor) to change this
     */
    long getLastKey();

    @Nonnull
    @Override
    M setCheck(@Nullable BooleanSupplier checks);

    @Nonnull
    @Override
    M timeout(long timeout, @Nonnull TimeUnit unit);

    @Nonnull
    @Override
    M deadline(long timestamp);

    /**
     * The supported {@link PaginationOrder PaginationOrders} for this pagination action.
     * <br>All enum values that are not returned will cause a throw for {@link #order(PaginationOrder)}.
     *
     * <p>Most pagination endpoints only support a single order, however some endpoints such as message pagination supports both.
     *
     * @return {@link EnumSet} of {@link PaginationOrder} (Modifying this set does not affect this class)
     */
    @Nonnull
    default EnumSet<PaginationOrder> getSupportedOrders()
    {
        return EnumSet.allOf(PaginationOrder.class);
    }

    /**
     * The current iteration order.
     * <br>This defaults to {@link PaginationOrder#BACKWARD}, meaning most recent first, for most pagination endpoints.
     *
     * @return The {@link PaginationOrder}
     *
     * @see    #order(PaginationOrder)
     */
    @Nonnull
    PaginationOrder getOrder();

    /**
     * Configure the {@link PaginationOrder} of this pagination action.
     *
     * <p>You can only supply supported orders, see {@link #getSupportedOrders()}.
     *
     * @param  order
     *         The pagination order
     *
     * @throws IllegalArgumentException
     *         If the provided pagination order is null or unsupported
     * @throws IllegalStateException
     *         If this pagination action has already been used to retrieve entities
     *
     * @return The current PaginationAction implementation instance
     *
     * @see    #getSupportedOrders()
     * @see    #reverse()
     */
    @Nonnull
    M order(@Nonnull PaginationOrder order);

    /**
     * Flips the {@link #order(PaginationOrder)} of this pagination action.
     *
     * @throws IllegalArgumentException
     *         If this pagination action does not support the reversed order
     *
     * @return The current PaginationAction implementation instance
     */
    @Nonnull
    default M reverse()
    {
        if (getOrder() == PaginationOrder.BACKWARD)
            return order(PaginationOrder.FORWARD);
        return order(PaginationOrder.BACKWARD);
    }

    /**
     * The current amount of cached entities for this PaginationAction
     *
     * @return int size of currently cached entities
     */
    int cacheSize();

    /**
     * Whether the cache of this PaginationAction is empty.
     * <br>Logically equivalent to {@code cacheSize() == 0}.
     *
     * @return True, if no entities have been retrieved yet.
     */
    boolean isEmpty();

    /**
     * The currently cached entities of recent execution tasks.
     * <br>Every {@link net.dv8tion.jda.api.requests.RestAction RestAction} success
     * adds to this List. (Thread-Safe due to {@link java.util.concurrent.CopyOnWriteArrayList CopyOnWriteArrayList})
     *
     * <p><b>This <u>does not</u> contain all entities for the paginated endpoint unless the pagination has reached an end!</b>
     * <br>It only contains those entities which already have been retrieved.
     *
     * @return Immutable {@link java.util.List List} containing all currently cached entities for this PaginationAction
     */
    @Nonnull
    List<T> getCached();

    /**
     * The most recent entity retrieved by this PaginationAction instance
     *
     * @throws java.util.NoSuchElementException
     *         If no entities have been retrieved yet (see {@link #isEmpty()})
     *
     * @return The most recent cached entity
     */
    @Nonnull
    T getLast();

    /**
     * The first cached entity retrieved by this PaginationAction instance
     *
     * @throws java.util.NoSuchElementException
     *         If no entities have been retrieved yet (see {@link #isEmpty()})
     *
     * @return The very first cached entity
     */
    @Nonnull
    T getFirst();

    /**
     * Sets the limit that should be used in the next RestAction completion
     * call.
     *
     * <p>The specified limit may not be below the {@link #getMinLimit() Minimum Limit} nor above
     * the {@link #getMaxLimit() Maximum Limit}. Unless these limits are specifically omitted. (See documentation of methods)
     *
     * <p><b>This limit represents how many entities will be retrieved per request and
     * <u>NOT</u> the maximum amount of entities that should be retrieved for iteration/sequencing.</b>
     * <br>{@code action.limit(50).complete()}
     * <br>is not the same as
     * <br>{@code action.stream().limit(50).collect(collector)}
     *
     *
     * @param  limit
     *         The limit to use
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided limit is out of range
     *
     * @return The current PaginationAction implementation instance
     */
    @Nonnull
    M limit(final int limit);

    /**
     * Whether already retrieved entities should be stored
     * within the internal cache. All cached entities will be
     * available from {@link #getCached()}.
     * <b>Default: true</b>
     * <br>This being disabled allows unused entities to be removed from
     * the memory heap by the garbage collector. If this is enabled this will not
     * take place until all references to this PaginationAction have been cleared.
     *
     * @param  enableCache
     *         Whether to enable entity cache
     *
     * @return The current PaginationAction implementation instance
     */
    @Nonnull
    M cache(final boolean enableCache);

    /**
     * Whether retrieved entities are stored within an
     * internal cache. If this is {@code false} entities
     * retrieved by the iterator or a call to a {@link net.dv8tion.jda.api.requests.RestAction RestAction}
     * terminal operation will not be retrievable from {@link #getCached()}.
     * <br>This being disabled allows unused entities to be removed from
     * the memory heap by the garbage collector. If this is enabled this will not
     * take place until all references to this PaginationAction have been cleared.
     *
     * @return True, If entities will be cached.
     */
    boolean isCacheEnabled();

    /**
     * The maximum limit that can be used for this PaginationAction
     * <br>Limits provided to {@link #limit(int)} must not be greater
     * than the returned value.
     * <br>If no maximum limit is used this will return {@code 0}.
     * That means there is no upper border for limiting this PaginationAction
     *
     * @return The maximum limit
     */
    int getMaxLimit();

    /**
     * The minimum limit that can be used for this PaginationAction
     * <br>Limits provided to {@link #limit(int)} must not be less
     * than the returned value.
     * <br>If no minimum limit is used this will return {@code 0}.
     * That means there is no lower border for limiting this PaginationAction
     *
     * @return The minimum limit
     */
    int getMinLimit();

    /**
     * The currently used limit.
     * <br>If this PaginationAction does not use limitation
     * this will return {@code 0}
     *
     * @return limit
     */
    int getLimit();

    /**
     * Retrieves elements while the specified condition is met.
     *
     * @param  rule
     *         The rule which must be fulfilled for an element to be added,
     *         returns false to discard the element and finish the task
     *
     * @throws IllegalArgumentException
     *         If the provided rule is {@code null}
     *
     * @return {@link CompletableFuture} - Type: {@link List List}
     *         <br>Future representing the fetch task, the list will be sorted most recent to oldest
     *
     * @see    #takeWhileAsync(int, Predicate)
     * @see    #takeUntilAsync(Predicate)
     */
    @Nonnull
    default CompletableFuture<List<T>> takeWhileAsync(@Nonnull final Predicate<? super T> rule)
    {
        Checks.notNull(rule, "Rule");
        return takeUntilAsync(rule.negate());
    }

    /**
     * Retrieves elements while the specified condition is met.
     *
     * @param  limit
     *         The maximum amount of elements to collect or {@code 0} for no limit
     * @param  rule
     *         The rule which must be fulfilled for an element to be added,
     *         returns false to discard the element and finish the task
     *
     * @throws IllegalArgumentException
     *         If the provided rule is {@code null} or the limit is negative
     *
     * @return {@link CompletableFuture} - Type: {@link List List}
     *         <br>Future representing the fetch task, the list will be sorted most recent to oldest
     *
     * @see    #takeWhileAsync(Predicate)
     * @see    #takeUntilAsync(int, Predicate)
     */
    @Nonnull
    default CompletableFuture<List<T>> takeWhileAsync(int limit, @Nonnull final Predicate<? super T> rule)
    {
        Checks.notNull(rule, "Rule");
        return takeUntilAsync(limit, rule.negate());
    }

    /**
     * Retrieves elements until the specified condition is met.
     *
     * @param  rule
     *         The rule which must be fulfilled for an element to be discarded,
     *         returns true to discard the element and finish the task
     *
     * @throws IllegalArgumentException
     *         If the provided rule is {@code null}
     *
     * @return {@link CompletableFuture} - Type: {@link List List}
     *         <br>Future representing the fetch task, the list will be sorted most recent to oldest
     *
     * @see    #takeWhileAsync(Predicate)
     * @see    #takeUntilAsync(int, Predicate)
     */
    @Nonnull
    default CompletableFuture<List<T>> takeUntilAsync(@Nonnull final Predicate<? super T> rule)
    {
        return takeUntilAsync(0, rule);
    }

    /**
     * Retrieves elements until the specified condition is met.
     *
     * @param  limit
     *         The maximum amount of elements to collect or {@code 0} for no limit
     * @param  rule
     *         The rule which must be fulfilled for an element to be discarded,
     *         returns true to discard the element and finish the task
     *
     * @throws IllegalArgumentException
     *         If the provided rule is {@code null} or the limit is negative
     *
     * @return {@link CompletableFuture} - Type: {@link List List}
     *         <br>Future representing the fetch task, the list will be sorted most recent to oldest
     *
     * @see    #takeWhileAsync(Predicate)
     * @see    #takeUntilAsync(int, Predicate)
     */
    @Nonnull
    default CompletableFuture<List<T>> takeUntilAsync(int limit, @Nonnull final Predicate<? super T> rule)
    {
        Checks.notNull(rule, "Rule");
        Checks.notNegative(limit, "Limit");
        List<T> result = new ArrayList<>();
        CompletableFuture<List<T>> future = new CompletableFuture<>();
        CompletableFuture<?> handle = forEachAsync((element) -> {
            if (rule.test(element))
                return false;
            result.add(element);
            return limit == 0 || limit > result.size();
        });
        handle.whenComplete((r, t) -> {
           if (t != null)
               future.completeExceptionally(t);
           else
               future.complete(result);
        });
        return future;
    }

    /**
     * Convenience method to retrieve an amount of entities from this pagination action.
     * <br>This also includes already cached entities similar to {@link #forEachAsync(Procedure)}.
     *
     * @param  amount
     *         The maximum amount to retrieve
     *
     * @return {@link java.util.concurrent.CompletableFuture CompletableFuture} - Type: {@link java.util.List List}
     *
     * @see    #forEachAsync(Procedure)
     */
    @Nonnull
    CompletableFuture<List<T>> takeAsync(int amount);

    /**
     * Convenience method to retrieve an amount of entities from this pagination action.
     * <br>Unlike {@link #takeAsync(int)} this does not include already cached entities.
     *
     * @param  amount
     *         The maximum amount to retrieve
     *
     * @return {@link java.util.concurrent.CompletableFuture CompletableFuture} - Type: {@link java.util.List List}
     *
     * @see    #forEachRemainingAsync(Procedure)
     */
    @Nonnull
    CompletableFuture<List<T>> takeRemainingAsync(int amount);

    /**
     * Iterates over all entities until the provided action returns {@code false}!
     * <br>This operation is different from {@link #forEach(Consumer)} as it
     * uses successive {@link #queue()} tasks to iterate each entity in callback threads instead of
     * the calling active thread.
     * This means that this method fully works on different threads to retrieve new entities.
     * <p><b>This iteration will include already cached entities, in order to exclude cached
     * entities use {@link #forEachRemainingAsync(Procedure)}</b>
     *
     * <h4>Example</h4>
     * <pre>{@code
     * //deletes messages until it finds a user that is still in guild
     * public void cleanupMessages(MessagePaginationAction action)
     * {
     *     action.forEachAsync( (message) ->
     *     {
     *         Guild guild = message.getGuild();
     *         if (!guild.isMember(message.getAuthor()))
     *             message.delete().queue();
     *         else
     *             return false;
     *         return true;
     *     });
     * }
     * }</pre>
     *
     * @param  action
     *         {@link net.dv8tion.jda.api.utils.Procedure Procedure} returning {@code true} if iteration should continue!
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided Procedure is {@code null}
     *
     * @return {@link java.util.concurrent.Future Future} that can be cancelled to stop iteration from outside!
     */
    @Nonnull
    default CompletableFuture<?> forEachAsync(@Nonnull final Procedure<? super T> action)
    {
        return forEachAsync(action, RestActionImpl.getDefaultFailure());
    }

    /**
     * Iterates over all entities until the provided action returns {@code false}!
     * <br>This operation is different from {@link #forEach(Consumer)} as it
     * uses successive {@link #queue()} tasks to iterate each entity in callback threads instead of
     * the calling active thread.
     * This means that this method fully works on different threads to retrieve new entities.
     *
     * <p><b>This iteration will include already cached entities, in order to exclude cached
     * entities use {@link #forEachRemainingAsync(Procedure, Consumer)}</b>
     *
     * <h4>Example</h4>
     * <pre>{@code
     * //deletes messages until it finds a user that is still in guild
     * public void cleanupMessages(MessagePaginationAction action)
     * {
     *     action.forEachAsync( (message) ->
     *     {
     *         Guild guild = message.getGuild();
     *         if (!guild.isMember(message.getAuthor()))
     *             message.delete().queue();
     *         else
     *             return false;
     *         return true;
     *     }, Throwable::printStackTrace);
     * }
     * }</pre>
     *
     * @param  action
     *         {@link net.dv8tion.jda.api.utils.Procedure Procedure} returning {@code true} if iteration should continue!
     * @param  failure
     *         {@link java.util.function.Consumer Consumer} that should handle any throwables from the action
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided Procedure or the failure Consumer is {@code null}
     *
     * @return {@link java.util.concurrent.Future Future} that can be cancelled to stop iteration from outside!
     */
    @Nonnull
    CompletableFuture<?> forEachAsync(@Nonnull final Procedure<? super T> action, @Nonnull final Consumer<? super Throwable> failure);

    /**
     * Iterates over all remaining entities until the provided action returns {@code false}!
     * <br>This operation is different from {@link #forEachRemaining(Procedure)} as it
     * uses successive {@link #queue()} tasks to iterate each entity in callback threads instead of
     * the calling active thread.
     * This means that this method fully works on different threads to retrieve new entities.
     *
     * <p><b>This iteration will exclude already cached entities, in order to include cached
     * entities use {@link #forEachAsync(Procedure)}</b>
     *
     * <h4>Example</h4>
     * <pre>{@code
     * //deletes messages until it finds a user that is still in guild
     * public void cleanupMessages(MessagePaginationAction action)
     * {
     *     action.forEachRemainingAsync( (message) ->
     *     {
     *         Guild guild = message.getGuild();
     *         if (!guild.isMember(message.getAuthor()))
     *             message.delete().queue();
     *         else
     *             return false;
     *         return true;
     *     });
     * }
     * }</pre>
     *
     * @param  action
     *         {@link net.dv8tion.jda.api.utils.Procedure Procedure} returning {@code true} if iteration should continue!
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided Procedure is {@code null}
     *
     * @return {@link java.util.concurrent.Future Future} that can be cancelled to stop iteration from outside!
     */
    @Nonnull
    default CompletableFuture<?> forEachRemainingAsync(@Nonnull final Procedure<? super T> action)
    {
        return forEachRemainingAsync(action, RestActionImpl.getDefaultFailure());
    }

    /**
     * Iterates over all remaining entities until the provided action returns {@code false}!
     * <br>This operation is different from {@link #forEachRemaining(Procedure)} as it
     * uses successive {@link #queue()} tasks to iterate each entity in callback threads instead of
     * the calling active thread.
     * This means that this method fully works on different threads to retrieve new entities.
     *
     * <p><b>This iteration will exclude already cached entities, in order to include cached
     * entities use {@link #forEachAsync(Procedure, Consumer)}</b>
     *
     * <h4>Example</h4>
     * <pre>{@code
     * //deletes messages until it finds a user that is still in guild
     * public void cleanupMessages(MessagePaginationAction action)
     * {
     *     action.forEachRemainingAsync( (message) ->
     *     {
     *         Guild guild = message.getGuild();
     *         if (!guild.isMember(message.getAuthor()))
     *             message.delete().queue();
     *         else
     *             return false;
     *         return true;
     *     }, Throwable::printStackTrace);
     * }
     * }</pre>
     *
     * @param  action
     *         {@link net.dv8tion.jda.api.utils.Procedure Procedure} returning {@code true} if iteration should continue!
     * @param  failure
     *         {@link java.util.function.Consumer Consumer} that should handle any throwables from the action
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided Procedure or the failure Consumer is {@code null}
     *
     * @return {@link java.util.concurrent.Future Future} that can be cancelled to stop iteration from outside!
     */
    @Nonnull
    CompletableFuture<?> forEachRemainingAsync(@Nonnull final Procedure<? super T> action, @Nonnull final Consumer<? super Throwable> failure);

    /**
     * Iterates over all remaining entities until the provided action returns {@code false}!
     * <br>Skipping past already cached entities to iterate all remaining entities of this PaginationAction.
     *
     * <p><b>This is a blocking operation that might take a while to complete</b>
     *
     * @param  action
     *         The {@link net.dv8tion.jda.api.utils.Procedure Procedure}
     *         which should return {@code true} to continue iterating
     */
    void forEachRemaining(@Nonnull final Procedure<? super T> action);

    @Override
    default Spliterator<T> spliterator()
    {
        return Spliterators.spliteratorUnknownSize(iterator(), Spliterator.IMMUTABLE);
    }

    /**
     * A sequential {@link java.util.stream.Stream Stream} with this PaginationAction as its source.
     *
     * @return a sequential {@code Stream} over the elements in this PaginationAction
     */
    @Nonnull
    default Stream<T> stream()
    {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * Returns a possibly parallel {@link java.util.stream.Stream Stream} with this PaginationAction as its
     * source. It is allowable for this method to return a sequential stream.
     *
     * @return a sequential {@code Stream} over the elements in this PaginationAction
     */
    @Nonnull
    default Stream<T> parallelStream()
    {
        return StreamSupport.stream(spliterator(), true);
    }

    /**
     * {@link PaginationIterator PaginationIterator}
     * that will iterate over all entities for this PaginationAction.
     *
     * @return new PaginationIterator
     */
    @Nonnull
    @Override
    PaginationIterator<T> iterator();

    /**
     * Defines the pagination order for a pagination endpoint.
     */
    enum PaginationOrder
    {
        /**
         * Iterates backwards in time, listing the most recent entities first.
         */
        BACKWARD("before"),
        /**
         * Iterates forward in time, listing the oldest entities first.
         */
        FORWARD("after");

        private final String key;

        PaginationOrder(String key)
        {
            this.key = key;
        }

        /**
         * The API query parameter key
         *
         * @return The query key
         */
        @Nonnull
        public String getKey()
        {
            return key;
        }
    }

    /**
     * Iterator implementation for a {@link PaginationAction PaginationAction}.
     * <br>This iterator will first iterate over all currently cached entities and continue to retrieve new entities
     * as needed.
     *
     * <p>To retrieve new entities after reaching the end of the current cache, this iterator will
     * request a List of new entities through a call of {@link net.dv8tion.jda.api.requests.RestAction#complete() RestAction.complete()}.
     * <br><b>It is recommended to use the highest possible limit for this task. (see {@link #limit(int)})</b>
     */
    class PaginationIterator<E> implements Iterator<E>
    {
        protected Queue<E> items;
        protected final Supplier<List<E>> supply;

        public PaginationIterator(Collection<E> queue, Supplier<List<E>> supply)
        {
            this.items = new LinkedList<>(queue);
            this.supply = supply;
        }

        @Override
        public boolean hasNext()
        {
            if (items == null)
                return false;
            if (!hitEnd())
                return true;

            if (items.addAll(supply.get()))
                return true;

            // null indicates that the real end has been reached
            items = null;
            return false;
        }

        @Override
        public E next()
        {
            if (!hasNext())
                throw new NoSuchElementException("Reached End of pagination task!");
            return items.poll();
        }

        protected boolean hitEnd()
        {
            return items.isEmpty();
        }
    }
}
