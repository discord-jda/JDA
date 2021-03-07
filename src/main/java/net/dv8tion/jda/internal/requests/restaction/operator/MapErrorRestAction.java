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
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class MapErrorRestAction<T> extends RestActionOperator<T, T>
{
    private final Predicate<? super Throwable> check;
    private final Function<? super Throwable, ? extends T> map;

    public MapErrorRestAction(RestAction<T> action, Predicate<? super Throwable> check, Function<? super Throwable, ? extends T> map)
    {
        super(action);
        this.check = check;
        this.map = map;
    }

    @Override
    public void queue(@Nullable Consumer<? super T> success, @Nullable Consumer<? super Throwable> failure)
    {
        action.queue(success, contextWrap((error) -> // Use contextWrap so error has a context cause
        {
            try
            {
                if (check.test(error)) // Check condition
                    doSuccess(success, map.apply(error)); // Then apply fallback function
                else // Fallback downstream
                    doFailure(failure, error); // error already has context so no contextWrap needed
            }
            catch (Throwable e)
            {
                doFailure(failure, Helpers.appendCause(e, error)); // error already has context so no contextWrap needed
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
                    return map.apply(error);
            }
            catch (Throwable e)
            {
                fail(Helpers.appendCause(e, error));
            }
            if (error instanceof RateLimitedException)
                throw (RateLimitedException) error;
            else
                fail(error);
        }
        throw new AssertionError("Unreachable");
    }

    @Nonnull
    @Override
    public CompletableFuture<T> submit(boolean shouldQueue)
    {
        return action.submit(shouldQueue).handle((value, error) -> {
            T result = value;
            if (error != null)
            {
                error = error instanceof CompletionException && error.getCause() != null ? error.getCause() : error;
                if (check.test(error))
                    result = map.apply(error);
                else
                    fail(error);
            }
            return result;
        });
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
