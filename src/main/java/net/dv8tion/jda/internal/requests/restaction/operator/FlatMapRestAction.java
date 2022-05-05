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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class FlatMapRestAction<I, O> extends RestActionOperator<I, O>
{
    private final Function<? super I, ? extends RestAction<O>> function;
    private final Predicate<? super I> condition;

    public FlatMapRestAction(RestAction<I> action, Predicate<? super I> condition,
                             Function<? super I, ? extends RestAction<O>> function)
    {
        super(action);
        this.function = function;
        this.condition = condition;
    }

    private RestAction<O> supply(I input)
    {
        return applyContext(function.apply(input));
    }

    @Override
    public void queue(@Nullable Consumer<? super O> success, @Nullable Consumer<? super Throwable> failure)
    {
        Consumer<? super Throwable> catcher = contextWrap(failure);
        handle(action, catcher, (result) -> {
            if (condition != null && !condition.test(result))
                return;
            RestAction<O> then = supply(result);
            if (then == null) // caught by handle try/catch abstraction
                throw new IllegalStateException("FlatMap operand is null");
            then.queue(success, catcher);
        });
    }

    @Override
    public O complete(boolean shouldQueue) throws RateLimitedException
    {
        return supply(action.complete(shouldQueue)).complete(shouldQueue);
    }

    @Nonnull
    @Override
    public CompletableFuture<O> submit(boolean shouldQueue)
    {
        return action.submit(shouldQueue)
                .thenCompose((result) -> supply(result).submit(shouldQueue));
    }
}
