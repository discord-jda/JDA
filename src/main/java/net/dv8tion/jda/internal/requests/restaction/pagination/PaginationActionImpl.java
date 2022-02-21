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

package net.dv8tion.jda.internal.requests.restaction.pagination;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.requests.restaction.pagination.PaginationAction;
import net.dv8tion.jda.api.utils.Procedure;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public abstract class PaginationActionImpl<T, M extends PaginationAction<T, M>>
    extends RestActionImpl<List<T>>
    implements PaginationAction<T, M>
{
    protected final List<T> cached = new CopyOnWriteArrayList<>();
    protected final int maxLimit;
    protected final int minLimit;
    protected final AtomicInteger limit;

    protected PaginationOrder order = PaginationOrder.BACKWARD;

    protected volatile long iteratorIndex = 0;
    protected volatile long lastKey = 0;
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
    public PaginationActionImpl(JDA api, Route.CompiledRoute route, int minLimit, int maxLimit, int initialLimit)
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
    public PaginationActionImpl(JDA api)
    {
        super(api, null);
        this.maxLimit = 0;
        this.minLimit = 0;
        this.limit = new AtomicInteger(0);
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public M skipTo(long id)
    {
        if (!cached.isEmpty())
        {
            int cmp = Long.compareUnsigned(this.lastKey, id);
            if (cmp < 0) // old - new < 0 => old < new
                throw new IllegalArgumentException("Cannot jump to that id, it is newer than the current oldest element.");
        }
        if (this.lastKey != id)
            this.last = null;
        this.iteratorIndex = id;
        this.lastKey = id;
        return (M) this;
    }

    @Override
    public long getLastKey()
    {
        return lastKey;
    }

    @Nonnull
    @Override
    public PaginationOrder getOrder()
    {
        return order;
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public M order(@Nonnull PaginationAction.PaginationOrder order)
    {
        Checks.notNull(order, "PaginationOrder");
        if (order != this.order)
        {
            if (!isEmpty())
                throw new IllegalStateException("Cannot change pagination order after retrieving.");
            if (!getSupportedOrders().contains(order))
                throw new IllegalArgumentException("Cannot use PaginationOrder." + order + " for this pagination endpoint.");
        }
        this.order = order;
        return (M) this;
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public M setCheck(BooleanSupplier checks)
    {
        return (M) super.setCheck(checks);
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public M timeout(long timeout, @Nonnull TimeUnit unit)
    {
        return (M) super.timeout(timeout, unit);
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public M deadline(long timestamp)
    {
        return (M) super.deadline(timestamp);
    }

    @Override
    public int cacheSize()
    {
        return cached.size();
    }

    @Override
    public boolean isEmpty()
    {
        return cached.isEmpty();
    }

    @Nonnull
    @Override
    public List<T> getCached()
    {
        return Collections.unmodifiableList(cached);
    }

    @Nonnull
    @Override
    public T getLast()
    {
        final T last = this.last;
        if (last == null)
            throw new NoSuchElementException("No entities have been retrieved yet.");
        return last;
    }

    @Nonnull
    @Override
    public T getFirst()
    {
        if (cached.isEmpty())
            throw new NoSuchElementException("No entities have been retrieved yet.");
        return cached.get(0);
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public M limit(final int limit)
    {
        Checks.check(maxLimit == 0 || limit <= maxLimit, "Limit must not exceed %d!", maxLimit);
        Checks.check(minLimit == 0 || limit >= minLimit, "Limit must be greater or equal to %d", minLimit);
        this.limit.set(limit);
        return (M) this;
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public M cache(final boolean enableCache)
    {
        this.useCache = enableCache;
        return (M) this;
    }

    @Override
    public boolean isCacheEnabled()
    {
        return useCache;
    }

    @Override
    public final int getMaxLimit()
    {
        return maxLimit;
    }

    @Override
    public final int getMinLimit()
    {
        return minLimit;
    }

    @Override
    public final int getLimit()
    {
        return limit.get();
    }

    @Nonnull
    @Override
    public CompletableFuture<List<T>> takeAsync(int amount)
    {
        return takeAsync0(amount, (task, list) -> forEachAsync(val -> {
            list.add(val);
            return list.size() < amount;
        }, task::completeExceptionally));
    }

    @Nonnull
    @Override
    public CompletableFuture<List<T>> takeRemainingAsync(int amount)
    {
        return takeAsync0(amount, (task, list) -> forEachRemainingAsync(val -> {
            list.add(val);
            return list.size() < amount;
        }, task::completeExceptionally));
    }

    private CompletableFuture<List<T>> takeAsync0(int amount, BiFunction<CompletableFuture<?>, List<T>, CompletableFuture<?>> converter)
    {
        CompletableFuture<List<T>> task = new CompletableFuture<>();
        List<T> list = new ArrayList<>(amount);
        CompletableFuture<?> promise = converter.apply(task, list);
        promise.thenRun(() -> task.complete(list));
        return task;
    }

    @Nonnull
    @Override
    public PaginationIterator<T> iterator()
    {
        return new PaginationIterator<>(cached, this::getNextChunk);
    }

    @Nonnull
    @Override
    public CompletableFuture<?> forEachAsync(@Nonnull final Procedure<? super T> action, @Nonnull final Consumer<? super Throwable> failure)
    {
        Checks.notNull(action, "Procedure");
        Checks.notNull(failure, "Failure Consumer");

        final CompletableFuture<?> task = new CompletableFuture<>();
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

    @Nonnull
    @Override
    public CompletableFuture<?> forEachRemainingAsync(@Nonnull final Procedure<? super T> action, @Nonnull final Consumer<? super Throwable> failure)
    {
        Checks.notNull(action, "Procedure");
        Checks.notNull(failure, "Failure Consumer");

        final CompletableFuture<?> task = new CompletableFuture<>();
        final Consumer<List<T>> acceptor = new ChainedConsumer(task, action, (throwable) ->
        {
            task.completeExceptionally(throwable);
            failure.accept(throwable);
        });
        try
        {
            acceptor.accept(getRemainingCache());
        }
        catch (Exception ex)
        {
            failure.accept(ex);
            task.completeExceptionally(ex);
        }
        return task;
    }

    @Override
    public void forEachRemaining(@Nonnull final Procedure<? super T> action)
    {
        Checks.notNull(action, "Procedure");
        Queue<T> queue = new LinkedList<>();
        while (queue.addAll(getNextChunk()))
        {
            while (!queue.isEmpty())
            {
                T it = queue.poll();
                if (!action.execute(it))
                {
                    // set the iterator index for next call of remaining
                    updateIndex(it);
                    return;
                }
            }
        }
    }

    @Override
    protected Route.CompiledRoute finalizeRoute()
    {
        Route.CompiledRoute route = super.finalizeRoute();

        final String limit = String.valueOf(this.getLimit());
        final long last = this.lastKey;

        route = route.withQueryParams("limit", limit);

        if (last != 0)
            route = route.withQueryParams(order.getKey(), Long.toUnsignedString(last));
        else if (order == PaginationOrder.FORWARD)
            route = route.withQueryParams("after", "0");

        return route;
    }

    protected List<T> getRemainingCache()
    {
        int index = getIteratorIndex();
        if (useCache && index > -1 && index < cached.size())
            return cached.subList(index, cached.size());
        return Collections.emptyList();
    }

    public List<T> getNextChunk()
    {
        List<T> list = getRemainingCache();
        if (!list.isEmpty())
            return list;

        final int current = limit.getAndSet(getMaxLimit());
        list = complete();
        limit.set(current);
        return list;
    }

    protected abstract long getKey(T it);

    protected int getIteratorIndex()
    {
        for (int i = 0; i < cached.size(); i++)
        {
            if (getKey(cached.get(i)) == iteratorIndex)
                return i + 1;
        }
        return -1;
    }

    protected void updateIndex(T it)
    {
        long key = getKey(it);
        iteratorIndex = key;
        if (!useCache)
        {
            lastKey = key;
            last = it;
        }
    }

    protected class ChainedConsumer implements Consumer<List<T>>
    {
        protected final CompletableFuture<?> task;
        protected final Procedure<? super T> action;
        protected final Consumer<Throwable> throwableConsumer;
        protected boolean initial = true;

        protected ChainedConsumer(final CompletableFuture<?> task, final Procedure<? super T> action,
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

            T previous = null;
            for (T it : list)
            {
                if (task.isCancelled())
                {
                    if (previous != null)
                        updateIndex(previous);
                    return;
                }
                if (action.execute(it))
                {
                    previous = it;
                    continue;
                }
                // set the iterator index for next call of remaining
                updateIndex(it);
                task.complete(null);
                return;
            }

            final int currentLimit = limit.getAndSet(maxLimit);
            queue(this, throwableConsumer);
            limit.set(currentLimit);
        }
    }
}
