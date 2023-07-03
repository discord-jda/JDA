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

package net.dv8tion.jda.api.requests;

import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import okhttp3.RequestBody;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;

/**
 * Implementation of {@link CompletableFuture} used for {@link RestAction#submit()}.
 *
 * @param <T> The result type
 */
public class RestFuture<T> extends CompletableFuture<T>
{
    final Request<T> request;

    public RestFuture(final RestActionImpl<T> restAction, final boolean shouldQueue,
                      final BooleanSupplier checks, final RequestBody data, final Object rawData, final long deadline, final boolean priority,
                      final Route.CompiledRoute route, final CaseInsensitiveMap<String, String> headers)
    {
        this.request = new Request<>(restAction, this::complete, this::completeExceptionally,
                                     checks, shouldQueue, data, rawData, deadline, priority, route, headers);
        ((JDAImpl) restAction.getJDA()).getRequester().request(this.request);
    }

    public RestFuture(final T t)
    {
        complete(t);
        this.request = null;
    }

    public RestFuture(final Throwable t)
    {
        completeExceptionally(t);
        this.request = null;
    }

    @Override
    public boolean cancel(final boolean mayInterrupt)
    {
        if (this.request != null)
            this.request.cancel();

        return (!isDone() && !isCancelled()) && super.cancel(mayInterrupt);
    }
}
