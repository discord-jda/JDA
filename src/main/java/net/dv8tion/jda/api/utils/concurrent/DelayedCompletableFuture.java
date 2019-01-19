/*
 * Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Delayed;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Specialized {@link CompletableFuture} used in combination with a scheduler.
 *
 * @param <T>
 *        The result type
 *
 * @see    CompletableFuture
 * @see    Delayed
 */
public class DelayedCompletableFuture<T> extends CompletableFuture<T> implements ScheduledFuture<T>
{
    private ScheduledFuture<?> future;

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
    public void initProxy(ScheduledFuture<?> future)
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
