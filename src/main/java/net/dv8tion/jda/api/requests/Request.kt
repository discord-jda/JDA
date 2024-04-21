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

import net.dv8tion.jda.api.audit.ThreadLocalReason.Companion.closable
import net.dv8tion.jda.api.audit.ThreadLocalReason.Companion.current
import net.dv8tion.jda.api.events.ExceptionEvent
import net.dv8tion.jda.api.events.http.HttpRequestEvent
import net.dv8tion.jda.api.exceptions.ContextException.ContextConsumer
import net.dv8tion.jda.api.exceptions.ContextException.here
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.exceptions.ErrorResponseException.Companion.create
import net.dv8tion.jda.api.exceptions.RateLimitedException
import net.dv8tion.jda.api.requests.Route.CompiledRoute
import net.dv8tion.jda.internal.JDAImpl
import net.dv8tion.jda.internal.requests.CallbackContext
import net.dv8tion.jda.internal.requests.RestActionImpl
import net.dv8tion.jda.internal.utils.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.apache.commons.collections4.map.CaseInsensitiveMap
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeoutException
import java.util.function.BooleanSupplier
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import javax.annotation.Nonnull

/**
 * Internal class used for representing HTTP requests.
 *
 * @param <T> The expected type of the response
</T> */
class Request<T>(
    private val restAction: RestActionImpl<T>,
    @get:Nonnull val onSuccess: Consumer<in T>,
    onFailure: Consumer<in Throwable?>?,
    checks: BooleanSupplier?,
    shouldQueue: Boolean,
    body: RequestBody?,
    rawBody: Any?,
    private val deadline: Long,
    val isPriority: Boolean,
    route: CompiledRoute,
    headers: CaseInsensitiveMap<String?, String?>?
) {
    @get:Nonnull
    val jDA: JDAImpl

    @get:Nonnull
    val onFailure: Consumer<in Throwable?>? = null
    private val checks: BooleanSupplier?
    private val shouldQueue: Boolean

    @JvmField
    @get:Nonnull
    val route: CompiledRoute
    @JvmField
    val body: RequestBody?
    @JvmField
    val rawBody: Any?
    @JvmField
    val headers: CaseInsensitiveMap<String?, String?>?
    private val localReason: String?
    private var done = false
    var isCancelled = false
        private set

    init {
        if (onFailure is ContextConsumer) this.onFailure =
            onFailure else if (RestActionImpl.isPassContext()) this.onFailure = here(
            onFailure!!
        ) else this.onFailure = onFailure
        this.checks = checks
        this.shouldQueue = shouldQueue
        this.body = body
        this.rawBody = rawBody
        this.route = route
        this.headers = headers
        jDA = restAction.jda as JDAImpl
        localReason = current
    }

    private fun cleanup() {
        // Try closing any open request bodies that were never read from
        if (body is MultipartBody) {
            body.parts()
                .stream()
                .map<RequestBody>(Function<Part, RequestBody> { body() })
                .filter(Predicate<RequestBody> { obj: RequestBody? -> AutoCloseable::class.java.isInstance(obj) })
                .map<AutoCloseable>(Function<RequestBody, AutoCloseable> { obj: RequestBody? ->
                    AutoCloseable::class.java.cast(
                        obj
                    )
                })
                .forEach { closeable: AutoCloseable? -> IOUtil.silentClose(closeable) }
        } else if (body is AutoCloseable) {
            IOUtil.silentClose(body as AutoCloseable?)
        }
    }

    fun onSuccess(successObj: T) {
        if (done) return
        done = true
        cleanup()
        RestActionImpl.LOG.trace(
            "Scheduling success callback for request with route {}/{}",
            route.method,
            route.getCompiledRoute()
        )
        jDA.callbackPool.execute {
            try {
                closable(localReason).use { __ ->
                    CallbackContext.getInstance().use { ___ ->
                        RestActionImpl.LOG.trace(
                            "Running success callback for request with route {}/{}",
                            route.method,
                            route.getCompiledRoute()
                        )
                        onSuccess.accept(successObj)
                    }
                }
            } catch (t: Throwable) {
                RestActionImpl.LOG.error("Encountered error while processing success consumer", t)
                if (t is Error) {
                    jDA.handleEvent(ExceptionEvent(jDA, t, true))
                    throw t
                }
            }
        }
    }

    fun onFailure(response: Response) {
        if (response.code == 429) {
            onRateLimited(response)
        } else {
            onFailure(createErrorResponseException(response))
        }
    }

    fun onRateLimited(response: Response) {
        onFailure(RateLimitedException(route, response.retryAfter))
    }

    @Nonnull
    fun createErrorResponseException(@Nonnull response: Response): ErrorResponseException {
        return create(
            ErrorResponse.Companion.fromJSON(response.optObject().orElse(null)), response
        )
    }

    fun onFailure(failException: Throwable?) {
        if (done) return
        done = true
        cleanup()
        RestActionImpl.LOG.trace(
            "Scheduling failure callback for request with route {}/{}",
            route.method,
            route.getCompiledRoute()
        )
        jDA.callbackPool.execute {
            try {
                closable(localReason).use { __ ->
                    CallbackContext.getInstance().use { ___ ->
                        RestActionImpl.LOG.trace(
                            "Running failure callback for request with route {}/{}",
                            route.method,
                            route.getCompiledRoute()
                        )
                        onFailure!!.accept(failException)
                        if (failException is Error) jDA.handleEvent(ExceptionEvent(jDA, failException, false))
                    }
                }
            } catch (t: Throwable) {
                RestActionImpl.LOG.error("Encountered error while processing failure consumer", t)
                if (t is Error) {
                    jDA.handleEvent(ExceptionEvent(jDA, t, true))
                    throw t
                }
            }
        }
    }

    fun onCancelled() {
        onFailure(CancellationException("RestAction has been cancelled"))
    }

    fun onTimeout() {
        onFailure(TimeoutException("RestAction has timed out"))
    }

    @Nonnull
    fun getRestAction(): RestAction<T> {
        return restAction
    }

    val isSkipped: Boolean
        get() {
            if (isTimeout) {
                onTimeout()
                return true
            }
            val skip = runChecks()
            if (skip) onCancelled()
            return skip
        }
    private val isTimeout: Boolean
        private get() = deadline > 0 && deadline < System.currentTimeMillis()

    private fun runChecks(): Boolean {
        return try {
            isCancelled || checks != null && !checks.asBoolean
        } catch (e: Exception) {
            onFailure(e)
            true
        }
    }

    fun shouldQueue(): Boolean {
        return shouldQueue
    }

    fun cancel() {
        if (!isCancelled) onCancelled()
        isCancelled = true
    }

    fun handleResponse(@Nonnull response: Response) {
        RestActionImpl.LOG.trace(
            "Handling response for request with route {}/{} and code {}",
            route.method,
            route.getCompiledRoute(),
            response.code
        )
        restAction.handleResponse(response, this)
        jDA.handleEvent(HttpRequestEvent(this, response))
    }
}
