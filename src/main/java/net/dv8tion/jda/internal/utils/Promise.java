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
//TODO: Remove
package net.dv8tion.jda.internal.utils;

import net.dv8tion.jda.api.requests.RequestFuture;

import java.util.concurrent.CompletableFuture;

public class Promise<T> extends CompletableFuture<T> implements RequestFuture<T>
{
    public Promise() {}

    public Promise(final Throwable t)
    {
        this.completeExceptionally(t);
    }

    public Promise(final T t)
    {
        this.complete(t);
    }

    @Override
    public CompletableFuture<T> toCompletableFuture()
    {
        throw new UnsupportedOperationException("Access to the CompletableFuture is not supported to secure JDA integrity.");
    }
}
