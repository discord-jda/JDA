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

package net.dv8tion.jda.internal.requests;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class CompletedRestAction<T> implements AuditableRestAction<T>
{
    private final JDA api;
    private final T value;
    private final Throwable error;

    public CompletedRestAction(JDA api, T value, Throwable error)
    {
        this.api = api;
        this.value = value;
        this.error = error;
    }

    public CompletedRestAction(JDA api, T value)
    {
        this(api, value, null);
    }

    public CompletedRestAction(JDA api, Throwable error)
    {
        this(api, null, error);
    }


    @Nonnull
    @Override
    public AuditableRestAction<T> reason(@Nullable String reason)
    {
        return this;
    }

    @Nonnull
    @Override
    public JDA getJDA()
    {
        return api;
    }

    @Nonnull
    @Override
    public AuditableRestAction<T> setCheck(@Nullable BooleanSupplier checks)
    {
        return this;
    }

    @Override
    public void queue(@Nullable Consumer<? super T> success, @Nullable Consumer<? super Throwable> failure)
    {
        if (error == null)
        {
            if (success == null)
                RestAction.getDefaultSuccess().accept(value);
            else
                success.accept(value);
        }
        else
        {
            if (failure == null)
                RestAction.getDefaultFailure().accept(error);
            else
                failure.accept(error);
        }
    }

    @Override
    public T complete(boolean shouldQueue) throws RateLimitedException
    {
        if (error != null)
        {
            if (error instanceof RateLimitedException)
                throw (RateLimitedException) error;
            if (error instanceof RuntimeException)
                throw (RuntimeException) error;
            if (error instanceof Error)
                throw (Error) error;
            throw new IllegalStateException(error);
        }
        return value;
    }

    @Nonnull
    @Override
    public CompletableFuture<T> submit(boolean shouldQueue)
    {
        CompletableFuture<T> future = new CompletableFuture<>();
        if (error != null)
            future.completeExceptionally(error);
        else
            future.complete(value);
        return future;
    }
}
