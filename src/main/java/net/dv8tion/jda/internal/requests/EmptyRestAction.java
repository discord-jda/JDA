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
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class EmptyRestAction<T> implements AuditableRestAction<T>
{
    private final JDA api;
    private final T returnObj;

    public EmptyRestAction(JDA api)
    {
        this(api, null);
    }

    public EmptyRestAction(JDA api, T returnObj)
    {
        this.api = api;
        this.returnObj = returnObj;
    }

    @Nonnull
    @Override
    public JDA getJDA()
    {
        return api;
    }

    @Nonnull
    @Override
    public AuditableRestAction<T> reason(String reason)
    {
        return this;
    }

    @Nonnull
    @Override
    public AuditableRestAction<T> setCheck(BooleanSupplier checks)
    {
        return this;
    }

    @Override
    public void queue(Consumer<? super T> success, Consumer<? super Throwable> failure)
    {
        if (success != null)
            success.accept(returnObj);
    }

    @Nonnull
    @Override
    public CompletableFuture<T> submit(boolean shouldQueue)
    {
        return CompletableFuture.completedFuture(returnObj);
    }

    @Override
    public T complete(boolean shouldQueue)
    {
        return returnObj;
    }
}
