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
package net.dv8tion.jda.api.requests

import net.dv8tion.jda.api.requests.Route.CompiledRoute
import net.dv8tion.jda.internal.JDAImpl
import net.dv8tion.jda.internal.requests.RestActionImpl
import okhttp3.RequestBody
import org.apache.commons.collections4.map.CaseInsensitiveMap
import java.util.concurrent.CompletableFuture
import java.util.function.BooleanSupplier

/**
 * Implementation of [CompletableFuture] used for [RestAction.submit].
 *
 * @param <T> The result type
</T> */
class RestFuture<T> : CompletableFuture<T> {
    val request: Request<T>?

    constructor(
        restAction: RestActionImpl<T>, shouldQueue: Boolean,
        checks: BooleanSupplier?, data: RequestBody?, rawData: Any?, deadline: Long, priority: Boolean,
        route: CompiledRoute, headers: CaseInsensitiveMap<String?, String?>?
    ) {
        request = Request(restAction, { value: T -> complete(value) }, { ex: Throwable? -> completeExceptionally(ex) },
            checks, shouldQueue, data, rawData, deadline, priority, route, headers
        )
        (restAction.jda as JDAImpl).requester.request(request)
    }

    constructor(t: T) {
        complete(t)
        request = null
    }

    constructor(t: Throwable?) {
        completeExceptionally(t)
        request = null
    }

    override fun cancel(mayInterrupt: Boolean): Boolean {
        if (request != null) request.cancel()
        return !isDone && !isCancelled() && super.cancel(mayInterrupt)
    }
}
