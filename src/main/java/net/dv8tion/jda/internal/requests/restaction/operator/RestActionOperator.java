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
import net.dv8tion.jda.api.exceptions.ContextException;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public abstract class RestActionOperator<I, O> implements RestAction<O>
{
    protected BooleanSupplier check;
    protected long deadline = -1;
    protected final RestAction<I> action;

    public RestActionOperator(RestAction<I> action)
    {
        this.action = action;
    }

    protected static <E> void doSuccess(Consumer<? super E> callback, E value)
    {
        if (callback == null)
            RestAction.getDefaultSuccess().accept(value);
        else
            callback.accept(value);
    }

    protected static void doFailure(Consumer<? super Throwable> callback, Throwable throwable)
    {
        if (callback == null)
            RestAction.getDefaultFailure().accept(throwable);
        else
            callback.accept(throwable);
        if (throwable instanceof Error)
            throw (Error) throwable;
    }

    protected void handle(RestAction<I> action, Consumer<? super Throwable> failure, Consumer<? super I> success)
    {
        Consumer<? super Throwable> catcher = contextWrap(failure);
        action.queue((result) -> {
            try
            {
                if (success != null)
                    success.accept(result);
            }
            catch (Throwable ex)
            {
                doFailure(catcher, ex);
            }
        }, catcher);
    }

    @Nonnull
    @Override
    public JDA getJDA()
    {
        return action.getJDA();
    }

    @Nonnull
    @Override
    public RestAction<O> setCheck(@Nullable BooleanSupplier checks)
    {
        this.check = checks;
        action.setCheck(checks);
        return this;
    }

    @Nullable
    @Override
    public BooleanSupplier getCheck()
    {
        return action.getCheck();
    }

    @Nonnull
    @Override
    public RestAction<O> deadline(long timestamp)
    {
        this.deadline = timestamp;
        action.deadline(timestamp);
        return this;
    }

    @Nullable
    protected <T> RestAction<T> applyContext(RestAction<T> action)
    {
        if (action == null)
            return null;
        if (check != null)
            action.setCheck(check);
        if (deadline >= 0)
            action.deadline(deadline);
        return action;
    }

    @Nullable
    protected Consumer<? super Throwable> contextWrap(@Nullable Consumer<? super Throwable> callback)
    {
        if (callback instanceof ContextException.ContextConsumer)
            return callback;
        else if (RestAction.isPassContext())
            return ContextException.here(callback == null ? RestAction.getDefaultFailure() : callback);
        return callback;
    }
}
