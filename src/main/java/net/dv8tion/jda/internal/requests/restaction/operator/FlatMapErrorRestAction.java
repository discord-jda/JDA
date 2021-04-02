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
import net.dv8tion.jda.internal.utils.Helpers;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class FlatMapErrorRestAction<T> extends RestActionOperator<T, T>
{
    private final Predicate<? super Throwable> check;
    private final Function<? super Throwable, ? extends RestAction<? extends T>> map;

    public FlatMapErrorRestAction(RestAction<T> action, Predicate<? super Throwable> check, Function<? super Throwable, ? extends RestAction<? extends T>> map)
    {
        super(action);
        this.check = check;
        this.map = map;
    }

    @Override
    public void queue(@Nullable Consumer<? super T> success, @Nullable Consumer<? super Throwable> failure)
    {
        Consumer<? super Throwable> contextFailure = contextWrap(failure);
        action.queue(success, contextWrap((error) -> {
            try
            {
                if (check.test(error))
                {
                    // If check passed we can apply the fallback function and flatten it
                    RestAction<? extends T> then = map.apply(error);
                    if (then == null)
                        doFailure(failure, new IllegalStateException("FlatMapError operand is null", error)); // No contextFailure because error already has context
                    else
                        then.queue(success, contextFailure); // Use contextFailure here to apply new context to new errors
                }
                else doFailure(failure, error); // No contextFailure because error already has context
            }
            catch (Throwable e)
            {
                doFailure(failure, Helpers.appendCause(e, error)); // No contextFailure because error already has context
            }
        }));
    }

    @Override
    public T complete(boolean shouldQueue) throws RateLimitedException
    {
        try
        {
            return action.complete(shouldQueue);
        }
        catch (Throwable error)
        {
            try
            {
                if (check.test(error))
                {
                    RestAction<? extends T> then = map.apply(error);
                    if (then == null)
                        throw new IllegalStateException("FlatMapError operand is null", error);
                    return then.complete(shouldQueue);
                }
            }
            catch (Throwable e)
            {
                if (e instanceof IllegalStateException && e.getCause() == error)
                    throw (IllegalStateException) e;
                else if (e instanceof RateLimitedException)
                    throw (RateLimitedException) Helpers.appendCause(e, error);
                else
                    fail(Helpers.appendCause(e, error));
            }
            fail(error);
        }
        throw new AssertionError("Unreachable");
    }

    @Nonnull
    @Override
    public CompletableFuture<T> submit(boolean shouldQueue)
    {
        return action.submit(shouldQueue)
                .handle((result, error) -> {
                    if (check.test(error))
                        return map.apply(error).submit(shouldQueue).thenApply(x -> (T) x);
                    else
                        return CompletableFuture.completedFuture(result);
                }).thenCompose(Function.identity());
    }

    @Contract("_ -> fail")
    private void fail(Throwable error)
    {
        if (error instanceof RuntimeException)
            throw (RuntimeException) error;
        else if (error instanceof Error)
            throw (Error) error;
        else
            throw new RuntimeException(error);
    }
}
