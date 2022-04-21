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

package net.dv8tion.jda.internal.requests.restaction.operator;

import net.dv8tion.jda.api.exceptions.RateLimitedException;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class DelayRestAction<T> extends RestActionOperator<T, T>
{
    private final TimeUnit unit;
    private final long delay;
    private final ScheduledExecutorService scheduler;

    public DelayRestAction(RestAction<T> action, TimeUnit unit, long delay, ScheduledExecutorService scheduler)
    {
        super(action);
        this.unit = unit;
        this.delay = delay;
        this.scheduler = scheduler == null ? action.getJDA().getRateLimitPool() : scheduler;
    }

    @Override
    public void queue(@Nullable Consumer<? super T> success, @Nullable Consumer<? super Throwable> failure)
    {
        handle(action, failure, (result) ->
            scheduler.schedule(() ->
                doSuccess(success, result),
            delay, unit)
        );
    }

    @Override
    public T complete(boolean shouldQueue) throws RateLimitedException
    {
        T result = action.complete(shouldQueue);
        try
        {
            unit.sleep(delay);
            return result;
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    @Override
    public CompletableFuture<T> submit(boolean shouldQueue)
    {
        CompletableFuture<T> future = new CompletableFuture<>();
        queue(future::complete, future::completeExceptionally);
        return future;
    }
}
