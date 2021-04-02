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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class CombineRestAction<I1, I2, O> implements RestAction<O>
{
    private final RestAction<I1> action1;
    private final RestAction<I2> action2;
    private final BiFunction<? super I1, ? super I2, ? extends O> accumulator;
    private volatile boolean failed = false;

    public CombineRestAction(RestAction<I1> action1, RestAction<I2> action2, BiFunction<? super I1, ? super I2, ? extends O> accumulator)
    {
        Checks.check(action1 != action2, "Cannot combine a RestAction with itself!");
        this.action1 = action1;
        this.action2 = action2;
        this.accumulator = accumulator;
        BooleanSupplier checks = () -> !failed;
        action1.addCheck(checks);
        action2.addCheck(checks);
    }

    @Nonnull
    @Override
    public JDA getJDA()
    {
        return action1.getJDA();
    }

    @Nonnull
    @Override
    public RestAction<O> setCheck(@Nullable BooleanSupplier checks)
    {
        BooleanSupplier check = () -> !failed && (checks == null || checks.getAsBoolean());
        action1.setCheck(check);
        action2.setCheck(check);
        return this;
    }

    @Nonnull
    @Override
    public RestAction<O> addCheck(@Nonnull BooleanSupplier checks)
    {
        action1.addCheck(checks);
        action2.addCheck(checks);
        return this;
    }

    @Nullable
    @Override
    public BooleanSupplier getCheck()
    {
        BooleanSupplier check1 = action1.getCheck();
        BooleanSupplier check2 = action2.getCheck();
        return () ->
                (check1 == null || check1.getAsBoolean())
             && (check2 == null || check2.getAsBoolean())
             && !failed;
    }

    @Nonnull
    @Override
    public RestAction<O> deadline(long timestamp)
    {
        action1.deadline(timestamp);
        action2.deadline(timestamp);
        return this;
    }

    @Override
    public void queue(@Nullable Consumer<? super O> success, @Nullable Consumer<? super Throwable> failure)
    {
        ReentrantLock lock = new ReentrantLock();
        AtomicBoolean done1 = new AtomicBoolean(false);
        AtomicBoolean done2 = new AtomicBoolean(false);
        AtomicReference<I1> result1 = new AtomicReference<>();
        AtomicReference<I2> result2 = new AtomicReference<>();
        Consumer<Throwable> failureCallback = (e) ->
        {
            if (failed) return;
            failed = true;
            RestActionOperator.doFailure(failure, e);
        };
        action1.queue((s) -> MiscUtil.locked(lock, () ->
        {
            try
            {
                done1.set(true);
                result1.set(s);
                if (done2.get())
                    RestActionOperator.doSuccess(success, accumulator.apply(result1.get(), result2.get()));
            }
            catch (Exception e)
            {
                failureCallback.accept(e);
            }
        }), failureCallback);
        action2.queue((s) -> MiscUtil.locked(lock, () ->
        {
            try
            {
                done2.set(true);
                result2.set(s);
                if (done1.get())
                    RestActionOperator.doSuccess(success, accumulator.apply(result1.get(), result2.get()));
            }
            catch (Exception e)
            {
                failureCallback.accept(e);
            }
        }), failureCallback);
    }

    @Override
    public O complete(boolean shouldQueue) throws RateLimitedException
    {
        if (!shouldQueue)
            return accumulator.apply(action1.complete(false), action2.complete(false));
        try
        {
            return submit(true).join();
        }
        catch (CompletionException e)
        {
            if (e.getCause() instanceof RuntimeException)
                throw (RuntimeException) e.getCause();
            else if (e.getCause() instanceof RateLimitedException)
                throw (RateLimitedException) e.getCause();
            throw e;
        }
    }

    @Nonnull
    @Override
    public CompletableFuture<O> submit(boolean shouldQueue)
    {
        return action1.submit(shouldQueue).thenCombine(action2.submit(shouldQueue), accumulator);
    }
}
