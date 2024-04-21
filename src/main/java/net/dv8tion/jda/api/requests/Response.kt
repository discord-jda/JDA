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

import net.dv8tion.jda.api.exceptions.ParsingException
import net.dv8tion.jda.api.utils.IOFunction
import net.dv8tion.jda.api.utils.data.DataArray
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.internal.requests.RestActionImpl
import net.dv8tion.jda.internal.utils.*
import okhttp3.Response
import java.io.*
import java.util.*
import java.util.stream.Collectors
import javax.annotation.Nonnull

/**
 * Internal class used to represent HTTP responses or request failures.
 */
class Response(
    val rawResponse: Response?, @JvmField val code: Int, @param:Nonnull val message: String, @JvmField val retryAfter: Long, @get:Nonnull
    @param:Nonnull val cFRays: Set<String?>?
) : Closeable {
    private var body: InputStream? = null

    private var fallbackString: String? = null
    private var `object`: Any? = null
    private var attemptedParsing = false
    var exception: Exception? = null
        private set

    constructor(@Nonnull exception: Exception?, @Nonnull cfRays: Set<String?>?) : this(
        null,
        ERROR_CODE,
        ERROR_MESSAGE,
        -1,
        cfRays
    ) {
        this.exception = exception
    }

    init {
        if (rawResponse == null) {
            body = null
        } else  // weird compatibility issue, thinks some final isn't initialized if we return pre-maturely
            try {
                body = IOUtil.getBody(rawResponse)
            } catch (e: Exception) {
                throw java.lang.IllegalStateException(
                    "An error occurred while parsing the response for a RestAction",
                    e
                )
            }
    }

    constructor(retryAfter: Long, @Nonnull cfRays: Set<String?>?) : this(
        null,
        429,
        "TOO MANY REQUESTS",
        retryAfter,
        cfRays
    )

    constructor(@Nonnull response: Response, retryAfter: Long, @Nonnull cfRays: Set<String?>?) : this(
        response,
        response.code(),
        response.message(),
        retryAfter,
        cfRays
    )

    @get:Nonnull
    val array: DataArray
        get() = get(DataArray::class.java, JSON_SERIALIZE_ARRAY)

    @Nonnull
    fun optArray(): Optional<DataArray> {
        return parseBody(true, DataArray::class.java, JSON_SERIALIZE_ARRAY)
    }

    @get:Nonnull
    val `object`: DataObject?
        get() = get<DataObject?>(DataObject::class.java, JSON_SERIALIZE_OBJECT)

    @Nonnull
    fun optObject(): Optional<DataObject?> {
        return parseBody<DataObject?>(true, DataObject::class.java, JSON_SERIALIZE_OBJECT)
    }

    @get:Nonnull
    val string: String
        get() = parseBody(String::class.java) { reader: BufferedReader? -> readString(reader) }
            .orElseGet { if (fallbackString == null) "N/A" else fallbackString }

    @Nonnull
    operator fun <T> get(clazz: Class<T>, parser: IOFunction<BufferedReader?, T>): T {
        return parseBody(clazz, parser).orElseThrow { IllegalStateException() }
    }

    val isError: Boolean
        get() = this.code == ERROR_CODE
    val isOk: Boolean
        get() = this.code > 199 && this.code < 300
    val isRateLimit: Boolean
        get() = this.code == 429

    override fun toString(): String {
        val entityString = EntityString(if (exception == null) "HTTPResponse" else "HTTPException")
        if (exception == null) {
            entityString.addMetadata("code", code)
            if (`object` != null) entityString.addMetadata("object", `object`.toString())
        } else {
            entityString.addMetadata("exceptionMessage", exception!!.message)
        }
        return entityString.toString()
    }

    override fun close() {
        rawResponse?.close()
    }

    private fun readString(reader: BufferedReader?): String {
        return reader!!.lines().collect(Collectors.joining("\n"))
    }

    private fun <T> parseBody(clazz: Class<T>, parser: IOFunction<BufferedReader?, T>): Optional<T> {
        return parseBody(false, clazz, parser)
    }

    private fun <T> parseBody(opt: Boolean, clazz: Class<T>, parser: IOFunction<BufferedReader?, T>): Optional<T> {
        if (attemptedParsing) {
            return if (`object` != null && clazz.isAssignableFrom(`object`.javaClass)) Optional.of(clazz.cast(`object`)) else Optional.empty()
        }
        attemptedParsing = true
        if (body == null || rawResponse == null || rawResponse.body()!!.contentLength() == 0L) return Optional.empty()
        var reader: BufferedReader? = null
        return try {
            reader = BufferedReader(InputStreamReader(body))
            reader.mark(1024)
            val t = parser.apply(reader)
            this.`object` = t
            RestActionImpl.LOG.trace(
                "Parsed response body for response on url {}\n{}",
                rawResponse.request().url(),
                this.`object`
            )
            Optional.ofNullable(t)
        } catch (e: Exception) {
            try {
                reader!!.reset()
                fallbackString = readString(reader)
                reader.close()
            } catch (ignored: NullPointerException) {
            } catch (ignored: IOException) {
            }
            if (opt && e is ParsingException) Optional.empty() else throw java.lang.IllegalStateException(
                "An error occurred while parsing the response for a RestAction",
                e
            )
        }
    }

    companion object {
        const val ERROR_CODE = -1
        const val ERROR_MESSAGE = "ERROR"
        val JSON_SERIALIZE_OBJECT = IOFunction { stream: BufferedReader? ->
            DataObject.fromJson(
                stream!!
            )
        }
        val JSON_SERIALIZE_ARRAY = IOFunction { json: BufferedReader? ->
            DataArray.fromJson(
                json!!
            )
        }
    }
}
