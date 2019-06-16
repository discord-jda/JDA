/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.api.utils.concurrent;

import javax.annotation.Nonnull;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * Specialized {@link CompletableFuture} used in combination with a scheduler.
 *
 * @param <T>
 *        The result type
 *
 * @since  4.0.0
 *
 * @see    CompletableFuture
 * @see    Delayed
 */
public class DelayedCompletableFuture<T> extends CompletableFuture<T> implements ScheduledFuture<T>
{
    private ScheduledFuture<?> future;

    private DelayedCompletableFuture() {}

    /**
     * Creates a new DelayedCompletableFuture scheduled on the supplied executor.
     *
     * @param  executor
     *         The {@link ScheduledExecutorService} to use for scheduling
     * @param  delay
     *         The delay of the task
     * @param  unit
     *         Conversion {@link TimeUnit} for the delay
     * @param  mapping
     *         Conversion function which calls {@link #complete(Object)} of the future it receives
     * @param  <E>
     *         The result type of the scheduled task
     *
     * @return DelayedCompletableFuture for the specified runnable
     */
    @Nonnull
    public static <E> DelayedCompletableFuture<E> make(@Nonnull ScheduledExecutorService executor, long delay, @Nonnull TimeUnit unit, @Nonnull Function<? super DelayedCompletableFuture<E>, ? extends Runnable> mapping)
    {
        DelayedCompletableFuture<E> handle = new DelayedCompletableFuture<>();
        ScheduledFuture<?> future = executor.schedule(mapping.apply(handle), delay, unit);
        handle.initProxy(future);
        return handle;
    }

    /**
     * Initializes the backing scheduled task for this promise.
     *
     * <p>The provided future will be cancelled when {@link #cancel(boolean)} is invoked
     * and is used as provider for {@link #getDelay(TimeUnit)}.
     *
     * @param  future
     *         The future that should be cancelled when this task is cancelled
     *
     * @throws IllegalStateException
     *         If this was already initialized
     */
    private void initProxy(ScheduledFuture<?> future)
    {
        if (this.future == null)
            this.future = future;
        else
            throw new IllegalStateException("Cannot initialize twice");
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning)
    {
        if (future != null && !future.isDone())
            future.cancel(mayInterruptIfRunning);
        return super.cancel(mayInterruptIfRunning);
    }

    @Override
    public long getDelay(@Nonnull TimeUnit unit)
    {
        return future.getDelay(unit);
    }

    @Override
    public int compareTo(@Nonnull Delayed o)
    {
        return future.compareTo(o);
    }
}
