/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.api.utils.concurrent;

import net.dv8tion.jda.internal.requests.WebSocketClient;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class AsyncHandle<T> implements Task<T>
{
    private final Runnable onCancel;
    private final CompletableFuture<T> future;
    private Consumer<? super T> success;
    private Consumer<? super Throwable> failure;

    public AsyncHandle(CompletableFuture<T> future, Runnable onCancel)
    {
        this.future = future;
        this.onCancel = onCancel;
    }

    @Override
    public boolean isStarted()
    {
        return true;
    }

    @Override
    public Task<T> onError(@Nonnull Consumer<? super Throwable> callback)
    {
        Checks.notNull(callback, "Callback");
        future.exceptionally(error -> {
            callback.accept(error);
            return null;
        });
        return this;
    }

    @Override
    public Task<T> onSuccess(@Nonnull Consumer<? super T> callback)
    {
        Checks.notNull(callback, "Callback");
        future.thenAccept(callback);
        return this;
    }

    @Override
    public T get()
    {
        if (WebSocketClient.WS_THREAD.get())
            throw new UnsupportedOperationException("Blocking operations are not permitted on the gateway thread");
        return future.join();
    }

    @Override
    public void cancel()
    {
        onCancel.run();
    }
}
