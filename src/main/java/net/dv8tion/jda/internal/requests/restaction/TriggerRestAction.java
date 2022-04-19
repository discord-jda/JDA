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

package net.dv8tion.jda.internal.requests.restaction;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.exceptions.ContextException;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import okhttp3.RequestBody;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class TriggerRestAction<T> extends RestActionImpl<T>
{
    private final ReentrantLock mutex = new ReentrantLock();
    private final List<Runnable> callbacks = new LinkedList<>();
    private volatile boolean isReady;
    private volatile Throwable exception;

    public TriggerRestAction(JDA api, Route.CompiledRoute route)
    {
        super(api, route);
    }

    public TriggerRestAction(JDA api, Route.CompiledRoute route, DataObject data)
    {
        super(api, route, data);
    }

    public TriggerRestAction(JDA api, Route.CompiledRoute route, RequestBody data)
    {
        super(api, route, data);
    }

    public TriggerRestAction(JDA api, Route.CompiledRoute route, BiFunction<Response, Request<T>, T> handler)
    {
        super(api, route, handler);
    }

    public TriggerRestAction(JDA api, Route.CompiledRoute route, DataObject data, BiFunction<Response, Request<T>, T> handler)
    {
        super(api, route, data, handler);
    }

    public TriggerRestAction(JDA api, Route.CompiledRoute route, RequestBody data, BiFunction<Response, Request<T>, T> handler)
    {
        super(api, route, data, handler);
    }

    public void run()
    {
        MiscUtil.locked(mutex, () -> {
            isReady = true;
            callbacks.forEach(Runnable::run);
        });
    }

    public void fail(Throwable throwable)
    {
        MiscUtil.locked(mutex, () -> {
            exception = throwable;
            callbacks.forEach(Runnable::run);
        });
    }

    public void onReady(Runnable callback)
    {
        MiscUtil.locked(mutex, () -> {
            if (isReady || exception != null)
                callback.run();
            else
                callbacks.add(callback);
        });
    }

    @Override
    public void queue(Consumer<? super T> success, Consumer<? super Throwable> failure)
    {
        if (isReady)
        {
            super.queue(success, failure);
            return;
        }

        Consumer<? super Throwable> onFailure = wrapContext(failure);
        onReady(() -> {
            if (this.exception != null)
                onFailure.accept(exception);
            else
                super.queue(success, onFailure);
        });
    }

    @Nonnull
    @Override
    public CompletableFuture<T> submit(boolean shouldQueue)
    {
        if (isReady)
            return super.submit(shouldQueue);
        CompletableFuture<T> future = new CompletableFuture<>();
        Consumer<? super Throwable> onFailure = wrapContext(future::completeExceptionally);

        onReady(() -> {
            if (exception != null)
            {
                onFailure.accept(exception);
                return;
            }

            CompletableFuture<T> handle = super.submit(shouldQueue);
            handle.whenComplete((success, error) -> {
                if (error != null)
                    onFailure.accept(error);
                else
                    future.complete(success);
            });

            // Handle cancel forwarding
            future.whenComplete((r, e) -> {
                if (future.isCancelled())
                    handle.cancel(false);
            });
        });
        return future;
    }

    private Consumer<? super Throwable> wrapContext(Consumer<? super Throwable> failure)
    {
        failure = failure == null ? getDefaultFailure() : failure;
        if (!isPassContext() || (failure instanceof ContextException.ContextConsumer))
            return failure;
        return ContextException.here(failure);
    }
}
