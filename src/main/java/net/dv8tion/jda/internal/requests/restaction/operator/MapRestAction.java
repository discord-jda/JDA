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

package net.dv8tion.jda.internal.requests.restaction.operator;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public class MapRestAction<I, O> extends RestActionOperator<O>
{
    private final RestAction<I> input;
    private final Function<? super I, ? extends O> function;

    public MapRestAction(RestAction<I> input, Function<? super I, ? extends O> function)
    {
        this.input = input;
        this.function = function;
    }

    @Nonnull
    @Override
    public JDA getJDA()
    {
        return input.getJDA();
    }

    @Nonnull
    @Override
    public RestAction<O> setCheck(@Nullable BooleanSupplier checks)
    {
        input.setCheck(checks);
        return this;
    }

    @Override
    public void queue(@Nullable Consumer<? super O> success, @Nullable Consumer<? super Throwable> failure)
    {
        input.queue((result) ->
        {
            O mapped = function.apply(result);
            if (success == null)
                RestAction.getDefaultSuccess().accept(mapped);
            else
                success.accept(mapped);
        }, contextWrap(failure));
    }

    @Override
    public O complete(boolean shouldQueue) throws RateLimitedException
    {
        return function.apply(input.complete(shouldQueue));
    }

    @Nonnull
    @Override
    public CompletableFuture<O> submit(boolean shouldQueue)
    {
        return input.submit(shouldQueue).thenApply(function);
    }
}
