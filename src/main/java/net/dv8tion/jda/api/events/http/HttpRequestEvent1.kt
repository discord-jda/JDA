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
package net.dv8tion.jda.api.events.http

import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.requests.Request
import net.dv8tion.jda.api.requests.Response
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.Route.CompiledRoute
import net.dv8tion.jda.api.utils.data.DataArray
import net.dv8tion.jda.api.utils.data.DataObject
import okhttp3.Headers
import okhttp3.RequestBody
import okhttp3.ResponseBody
import javax.annotation.Nonnull

/**
 * Indicates that a [RestAction][net.dv8tion.jda.api.requests.RestAction] has been executed.
 *
 *
 * Depending on the request and its result not all values have to be populated.
 */
class HttpRequestEvent(
    @get:Nonnull
    @param:Nonnull val request: Request<*>, @param:Nonnull private val response: Response
) : Event(
    request.jda
) {
    val requestBody: RequestBody?
        get() = request.body
    val requestBodyRaw: Any?
        get() = request.rawBody
    val requestHeaders: Headers?
        get() = if (response.rawResponse == null) null else response.rawResponse!!.request().headers()
    val requestRaw: okhttp3.Request?
        get() = if (response.rawResponse == null) null else response.rawResponse!!.request()

    fun getResponse(): Response? {
        return response
    }

    val responseBody: ResponseBody?
        get() = if (response.rawResponse == null) null else response.rawResponse!!.body()
    val responseBodyAsArray: DataArray?
        get() = response.array
    val responseBodyAsObject: DataObject?
        get() = response.getObject()
    val responseBodyAsString: String?
        get() = response.string
    val responseHeaders: Headers?
        get() = if (response.rawResponse == null) null else response.rawResponse!!.headers()
    val responseRaw: okhttp3.Response?
        get() = response.rawResponse

    @get:Nonnull
    val cFRays: Set<String>
        get() = response.cfRays

    @get:Nonnull
    val restAction: RestAction<*>
        get() = request.restAction

    @get:Nonnull
    val route: CompiledRoute
        get() = request.route
    val isRateLimit: Boolean
        get() = response.isRateLimit
}
