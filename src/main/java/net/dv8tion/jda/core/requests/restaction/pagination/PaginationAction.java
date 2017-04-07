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

package net.dv8tion.jda.core.requests.restaction.pagination;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import org.apache.http.util.Args;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * {@link net.dv8tion.jda.core.requests.RestAction RestAction} specification used
 * to retrieve entities for paginated endpoints (before, after, limit).
 *
 * @param  <M>
 *         The current implementation used as chaining return value
 * @param  <T>
 *         The type of entity to paginate
 *
 * @since  3.1
 * @author Florian Spieß
 */
public abstract class PaginationAction<T, M extends PaginationAction<T, M>> extends RestAction<List<T>> implements Iterable<T>
{

    protected final List<T> cached = new CopyOnWriteArrayList<>();
    protected final int maxLimit;
    protected final int minLimit;
    protected final AtomicInteger limit;

    /**
     * Creates a new PaginationAction instance
     *
     * @param api
     *        The current JDA instance
     * @param maxLimit
     *        The inclusive maximum limit that can be used in {@link #limit(int)}
     * @param minLimit
     *        The inclusive minimum limit that can be used in {@link #limit(int)}
     * @param initialLimit
     *        The initial limit to use on the pagination endpoint
     */
    public PaginationAction(JDA api, int minLimit, int maxLimit, int initialLimit)
    {
        super(api, null, null);
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
        super(api, null, null);
        this.maxLimit = 0;
        this.minLimit = 0;
        this.limit = new AtomicInteger(0);
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
        if (cached.isEmpty())
            throw new NoSuchElementException("No entities have been retrieved yet.");
        return cached.get(cached.size() - 1);
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
     * call or by the next iterator retrieve call.
     *
     * @param  limit
     *         The limit to use
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided limit is out of range
     *
     * @return The current PaginationAction implementation instance
     */
    public M limit(int limit)
    {
        Args.check(maxLimit == 0 || limit <= maxLimit, "Limit must not exceed %d!", maxLimit);
        Args.check(minLimit == 0 || limit >= minLimit, "Limit must be greater or equal to %d", minLimit);

        this.limit.set(limit);
        return (M) this;
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
    @Override
    public PaginationIterator iterator()
    {
        return new PaginationIterator();
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

    protected abstract void finalizeRoute();
    protected abstract void handleResponse(Response response, Request request);

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
        protected int current = 0;

        @Override
        public boolean hasNext()
        {
            if (current < 0)
                return false;

            if (hitEnd())
            {
                synchronized (limit)
                {
                    final int tmp = limit.getAndSet(maxLimit);
                    complete();
                    limit.set(tmp);
                }

                if (!hitEnd())
                    return true;

                // -1 indicates that the real end has been reached
                current = -1;
                return false;
            }

            return true;
        }

        @Override
        public T next()
        {
            if (!hasNext())
                throw new NoSuchElementException("Reached End of pagination task!");
            return cached.get(current++);
        }

        protected boolean hitEnd()
        {
            return current < 0 || current >= cached.size();
        }

    }

}
