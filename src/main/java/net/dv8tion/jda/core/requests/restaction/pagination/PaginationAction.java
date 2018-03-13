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

package net.dv8tion.jda.core.requests.restaction.pagination;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.requests.*;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.Procedure;
import net.dv8tion.jda.core.utils.Promise;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * {@link net.dv8tion.jda.core.requests.RestAction RestAction} specification used
 * to retrieve entities for paginated endpoints (before, after, limit).
 *
 * <p><b>Examples</b>
 * <pre><code>
 * /**
 *   * Retrieves messages until the specified limit is reached. The messages will be limited after being filtered by the user.
 *   * If the user hasn't sent enough messages this will go through all messages so it is recommended to add an additional end condition.
 *   *&#47;
 * public static{@literal List<Message>} getMessagesByUser(MessageChannel channel, User user, int limit)
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
public abstract class PaginationAction<T, M extends PaginationAction<T, M>>
    extends RestAction<List<T>> implements Iterable<T>
{
    protected final List<T> cached = new CopyOnWriteArrayList<>();
    protected final int maxLimit;
    protected final int minLimit;
    protected final AtomicInteger limit;

    protected volatile T last = null;
    protected volatile boolean useCache = true;

    /**
     * Creates a new PaginationAction instance
     *
     * @param api
     *        The current JDA instance
     * @param route
     *        The base route
     * @param maxLimit
     *        The inclusive maximum limit that can be used in {@link #limit(int)}
     * @param minLimit
     *        The inclusive minimum limit that can be used in {@link #limit(int)}
     * @param initialLimit
     *        The initial limit to use on the pagination endpoint
     */
    public PaginationAction(JDA api, Route.CompiledRoute route, int minLimit, int maxLimit, int initialLimit)
    {
        super(api, route);
        this.maxLimit = maxLimit;
        this.minLimit = minLimit;
        this.limit = new AtomicInteger(initialLimit);
    }

    /**
     * Creates a new PaginationAction instance
     * <br>This is used for PaginationActions that should not deal with
     * {@link #limit(int)}
     *
     * @param api
     *        The current JDA instance
     */
    public PaginationAction(JDA api)
    {
        super(api, null);
        this.maxLimit = 0;
        this.minLimit = 0;
        this.limit = new AtomicInteger(0);
    }

    @Override
    @SuppressWarnings("unchecked")
    public M setCheck(BooleanSupplier checks)
    {
        return (M) super.setCheck(checks);
    }

    /**
     * The current amount of cached entities for this PaginationAction
     *
     * @return int size of currently cached entities
     */
    public int cacheSize()
    {
        return cached.size();
    }

    /**
     * Whether the cache of this PaginationAction is empty.
     * <br>Logically equivalent to {@code cacheSize() == 0}.
     *
     * @return True, if no entities have been retrieved yet.
     */
    public boolean isEmpty()
    {
        return cached.isEmpty();
    }

    /**
     * The currently cached entities of recent execution tasks.
     * <br>Every {@link net.dv8tion.jda.core.requests.RestAction RestAction} success
     * adds to this List. (Thread-Safe due to {@link java.util.concurrent.CopyOnWriteArrayList CopyOnWriteArrayList})
     *
     * <p><b>This <u>does not</u> contain all entities for the paginated endpoint unless the pagination has reached an end!</b>
     * <br>It only contains those entities which already have been retrieved.
     *
     * @return Immutable {@link java.util.List List} containing all currently cached entities for this PaginationAction
     */
    public List<T> getCached()
    {
        return Collections.unmodifiableList(cached);
    }

    /**
     * The most recent entity retrieved by this PaginationAction instance
     *
     * @throws java.util.NoSuchElementException
     *         If no entities have been retrieved yet (see {@link #isEmpty()})
     *
     * @return The most recent cached entity
     */
    public T getLast()
    {
        final T last = this.last;
        if (last == null)
            throw new NoSuchElementException("No entities have been retrieved yet.");
        return last;
    }

    /**
     * The first cached entity retrieved by this PaginationAction instance
     *
     * @throws java.util.NoSuchElementException
     *         If no entities have been retrieved yet (see {@link #isEmpty()})
     *
     * @return The very first cached entity
     */
    public T getFirst()
    {
        if (cached.isEmpty())
            throw new NoSuchElementException("No entities have been retrieved yet.");
        return cached.get(0);
    }

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
    @SuppressWarnings("unchecked")
    public M limit(final int limit)
    {
        Checks.check(maxLimit == 0 || limit <= maxLimit, "Limit must not exceed %d!", maxLimit);
        Checks.check(minLimit == 0 || limit >= minLimit, "Limit must be greater or equal to %d", minLimit);

        synchronized (this.limit)
        {
            this.limit.set(limit);
        }
        return (M) this;
    }

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
    @SuppressWarnings("unchecked")
    public M cache(final boolean enableCache)
    {
        this.useCache = enableCache;
        return (M) this;
    }

    /**
     * Whether retrieved entities are stored within an
     * internal cache. If this is {@code false} entities
     * retrieved by the iterator or a call to a {@link net.dv8tion.jda.core.requests.RestAction RestAction}
     * terminal operation will not be retrievable from {@link #getCached()}.
     * <br>This being disabled allows unused entities to be removed from
     * the memory heap by the garbage collector. If this is enabled this will not
     * take place until all references to this PaginationAction have been cleared.
     *
     * @return True, If entities will be cached.
     */
    public boolean isCacheEnabled()
    {
        return useCache;
    }

    /**
     * The maximum limit that can be used for this PaginationAction
     * <br>Limits provided to {@link #limit(int)} must not be greater
     * than the returned value.
     * <br>If no maximum limit is used this will return {@code 0}.
     * That means there is no upper border for limiting this PaginationAction
     *
     * @return The maximum limit
     */
    public final int getMaxLimit()
    {
        return maxLimit;
    }

    /**
     * The minimum limit that can be used for this PaginationAction
     * <br>Limits provided to {@link #limit(int)} must not be less
     * than the returned value.
     * <br>If no minimum limit is used this will return {@code 0}.
     * That means there is no lower border for limiting this PaginationAction
     *
     * @return The minimum limit
     */
    public final int getMinLimit()
    {
        return minLimit;
    }

    /**
     * The currently used limit.
     * <br>If this PaginationAction does not use limitation
     * this will return {@code 0}
     *
     * @return limit
     */
    public final int getLimit()
    {
        return limit.get();
    }

    /**
     * {@link net.dv8tion.jda.core.requests.restaction.pagination.PaginationAction.PaginationIterator PaginationIterator}
     * that will iterate over all entities for this PaginationAction.
     *
     * @return new PaginationIterator
     */
    @Nonnull
    @Override
    public PaginationIterator iterator()
    {
        return new PaginationIterator();
    }

    /**
     * Iterates over all entities until the provided action returns {@code false}!
     * <br>This operation is different from {@link #forEach(Consumer)} as it
     * uses successive {@link #queue()} tasks to iterate each entity in callback threads instead of
     * the calling active thread.
     * This means that this method fully works on different threads to retrieve new entities.
     * <p><b>This iteration will include already cached entities, in order to exclude cached
     * entities use {@link #forEachRemainingAsync(Procedure)}</b>
     *
     * <h1>Example</h1>
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
     *         {@link net.dv8tion.jda.core.utils.Procedure Procedure} returning {@code true} if iteration should continue!
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided Procedure is {@code null}
     *
     * @return {@link java.util.concurrent.Future Future} that can be cancelled to stop iteration from outside!
     */
    public RequestFuture<?> forEachAsync(final Procedure<T> action)
    {
        return forEachAsync(action, (throwable) ->
        {
            if (RestAction.DEFAULT_FAILURE != null)
                RestAction.DEFAULT_FAILURE.accept(throwable);
        });
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
     * <h1>Example</h1>
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
     *         {@link net.dv8tion.jda.core.utils.Procedure Procedure} returning {@code true} if iteration should continue!
     * @param  failure
     *         {@link java.util.function.Consumer Consumer} that should handle any throwables from the action
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided Procedure or the failure Consumer is {@code null}
     *
     * @return {@link java.util.concurrent.Future Future} that can be cancelled to stop iteration from outside!
     */
    public RequestFuture<?> forEachAsync(final Procedure<T> action, final Consumer<Throwable> failure)
    {
        Checks.notNull(action, "Procedure");
        Checks.notNull(failure, "Failure Consumer");

        final Promise<?> task = new Promise<>();
        final Consumer<List<T>> acceptor = new ChainedConsumer(task, action, (throwable) ->
        {
            task.completeExceptionally(throwable);
            failure.accept(throwable);
        });
        try
        {
            acceptor.accept(cached);
        }
        catch (Exception ex)
        {
            failure.accept(ex);
            task.completeExceptionally(ex);
        }
        return task;
    }

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
     * <h1>Example</h1>
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
     *         {@link net.dv8tion.jda.core.utils.Procedure Procedure} returning {@code true} if iteration should continue!
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided Procedure is {@code null}
     *
     * @return {@link java.util.concurrent.Future Future} that can be cancelled to stop iteration from outside!
     */
    public RequestFuture<?> forEachRemainingAsync(final Procedure<T> action)
    {
        return forEachRemainingAsync(action, (throwable) ->
        {
            if (RestAction.DEFAULT_FAILURE != null)
                RestAction.DEFAULT_FAILURE.accept(throwable);
        });
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
     * <h1>Example</h1>
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
     *         {@link net.dv8tion.jda.core.utils.Procedure Procedure} returning {@code true} if iteration should continue!
     * @param  failure
     *         {@link java.util.function.Consumer Consumer} that should handle any throwables from the action
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided Procedure or the failure Consumer is {@code null}
     *
     * @return {@link java.util.concurrent.Future Future} that can be cancelled to stop iteration from outside!
     */
    public RequestFuture<?> forEachRemainingAsync(final Procedure<T> action, final Consumer<Throwable> failure)
    {
        Checks.notNull(action, "Procedure");
        Checks.notNull(failure, "Failure Consumer");

        final Promise<?> task = new Promise<>();
        final Consumer<List<T>> acceptor = new ChainedConsumer(task, action, (throwable) ->
        {
            task.completeExceptionally(throwable);
            failure.accept(throwable);
        });
        try
        {
            //not starting with cache here unlike forEachAsync
            acceptor.accept(Collections.emptyList());
        }
        catch (Exception ex)
        {
            failure.accept(ex);
            task.completeExceptionally(ex);
        }
        return task;
    }

    /**
     * Iterates over all remaining entities until the provided action returns {@code false}!
     * <br>Skipping past already cached entities to iterate all remaining entities of this PaginationAction.
     *
     * <p><b>This is a blocking operation that might take a while to complete</b>
     *
     * @param  action
     *         The {@link net.dv8tion.jda.core.utils.Procedure Procedure}
     *         which should return {@code true} to continue iterating
     */
    public void forEachRemaining(final Procedure<T> action)
    {
        Checks.notNull(action, "Procedure");
        Queue<T> queue = new LinkedList<>();
        while (queue.addAll(getNextChunk()))
        {
            while (!queue.isEmpty())
            {
                if (!action.execute(queue.poll()))
                    return;
            }
        }
    }

    @Override
    public Spliterator<T> spliterator()
    {
        return Spliterators.spliteratorUnknownSize(iterator(), Spliterator.IMMUTABLE);
    }

    /**
     * A sequential {@link java.util.stream.Stream Stream} with this PaginationAction as its source.
     *
     * @return a sequential {@code Stream} over the elements in this PaginationAction
     */
    public Stream<T> stream()
    {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * Returns a possibly parallel {@link java.util.stream.Stream Stream} with this PaginationAction as its
     * source. It is allowable for this method to return a sequential stream.
     *
     * @return a sequential {@code Stream} over the elements in this PaginationAction
     */
    public Stream<T> parallelStream()
    {
        return StreamSupport.stream(spliterator(), true);
    }

    protected abstract void handleResponse(Response response, Request<List<T>> request);

    private List<T> getNextChunk()
    {
        List<T> items;
        synchronized (limit)
        {
            final int current = limit.getAndSet(getMaxLimit());
            items = complete();
            limit.set(current);
        }
        return items;
    }

    /**
     * Iterator implementation for a {@link net.dv8tion.jda.core.requests.restaction.pagination.PaginationAction PaginationAction}.
     * <br>This iterator will first iterate over all currently cached entities and continue to retrieve new entities
     * as needed.
     *
     * <p>To retrieve new entities after reaching the end of the current cache, this iterator will
     * request a List of new entities through a call of {@link net.dv8tion.jda.core.requests.RestAction#complete() RestAction.complete()}.
     * <br><b>It is recommended to use the highest possible limit for this task. (see {@link #limit(int)})</b>
     */
    public class PaginationIterator implements Iterator<T>
    {
        protected Queue<T> items = new LinkedList<>(cached);

        @Override
        public boolean hasNext()
        {
            if (items == null)
                return false;
            if (!hitEnd())
                return true;

            if (items.addAll(getNextChunk()))
                return true;

            // null indicates that the real end has been reached
            items = null;
            return false;
        }

        @Override
        public T next()
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

    protected class ChainedConsumer implements Consumer<List<T>>
    {
        protected final CompletableFuture<?> task;
        protected final Procedure<T> action;
        protected final Consumer<Throwable> throwableConsumer;
        protected boolean initial = true;

        protected ChainedConsumer(final CompletableFuture<?> task, final Procedure<T> action,
                                  final Consumer<Throwable> throwableConsumer)
        {
            this.task = task;
            this.action = action;
            this.throwableConsumer = throwableConsumer;
        }

        @Override
        public void accept(final List<T> list)
        {
            if (list.isEmpty() && !initial)
            {
                task.complete(null);
                return;
            }
            initial = false;

            for (T it : list)
            {
                if (task.isCancelled())
                    return;
                if (action.execute(it))
                    continue;
                task.complete(null);
                return;
            }
            synchronized (limit)
            {
                final int currentLimit = limit.getAndSet(maxLimit);
                queue(this, throwableConsumer);
                limit.set(currentLimit);
            }
        }
    }
}
