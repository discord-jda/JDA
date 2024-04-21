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
package net.dv8tion.jda.api.exceptions

import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.api.requests.Response
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.utils.data.DataArray
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.Helpers
import net.dv8tion.jda.internal.utils.JDALogger
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors
import javax.annotation.Nonnull

/**
 * Indicates an unhandled error that is returned by Discord API Request using [RestAction][net.dv8tion.jda.api.requests.RestAction]
 * <br></br>It holds an [ErrorResponse][net.dv8tion.jda.api.requests.ErrorResponse]
 *
 * @see net.dv8tion.jda.api.exceptions.ErrorHandler
 */
class ErrorResponseException private constructor(
    errorResponse: ErrorResponse,
    /**
     * The Discord Response causing the ErrorResponse
     *
     * @return [Response][net.dv8tion.jda.api.requests.Response]
     */
    val response: Response?, code: Int, meaning: String, schemaErrors: List<SchemaError>
) : RuntimeException(
    "$code: $meaning" + if (schemaErrors.isEmpty()) "" else """
     
     ${schemaErrors.stream().map { obj: SchemaError -> obj.toString() }.collect(Collectors.joining("\n"))}
     """.trimIndent()
) {
    /**
     * The [ErrorResponse][net.dv8tion.jda.api.requests.ErrorResponse] corresponding
     * for the received error response from Discord
     *
     * @return [ErrorResponse][net.dv8tion.jda.api.requests.ErrorResponse]
     */
    @JvmField
    val errorResponse: ErrorResponse

    /**
     * The meaning for this error.
     * <br></br>It is possible that the value from this method is different for [server errors][.isServerError]
     *
     * @return Never-null meaning of this error.
     */
    val meaning: String

    /**
     * The discord error code for this error response.
     *
     * @return The discord error code.
     *
     * @see [Discord Error Codes](https://discord.com/developers/docs/topics/opcodes-and-status-codes.json-json-error-codes)
     */
    val errorCode: Int

    /**
     * The [SchemaErrors][SchemaError] for this error response.
     * <br></br>These errors provide more context of what part in the body caused the error, and more explanation for the error itself.
     *
     * @return Possibly-empty list of [SchemaError]
     */
    @get:Nonnull
    val schemaErrors: List<SchemaError>

    /**
     * Creates a new ErrorResponseException instance
     *
     * @param errorResponse
     * The [ErrorResponse][net.dv8tion.jda.api.requests.ErrorResponse] corresponding
     * for the received error response from Discord
     * @param response
     * The Discord Response causing the ErrorResponse
     */
    init {
        if (response != null && response.exception != null) initCause(response.exception)
        this.errorResponse = errorResponse
        errorCode = code
        this.meaning = meaning
        this.schemaErrors = schemaErrors
    }

    val isServerError: Boolean
        /**
         * Whether this is an internal server error from discord (status 500)
         *
         * @return True, if this is an internal server error
         * [ErrorResponse.SERVER_ERROR][net.dv8tion.jda.api.requests.ErrorResponse.SERVER_ERROR]
         */
        get() = errorResponse == ErrorResponse.SERVER_ERROR

    /**
     * An error for a [SchemaError].
     * <br></br>This provides the machine parsable error code name and the human readable message.
     */
    class ErrorCode internal constructor(
        /**
         * The machine parsable error code
         *
         * @return The error code
         */
        @get:Nonnull val code: String,
        /**
         * The human readable explanation message for this error
         *
         * @return The message
         */
        @get:Nonnull val message: String
    ) {

        override fun toString(): String {
            return "$code: $message"
        }
    }

    /**
     * Schema error which supplies more context to a ErrorResponse.
     * <br></br>This provides a list of [ErrorCodes][ErrorCode] and a [location][.getLocation] for the errors.
     */
    class SchemaError(
        /**
         * The JSON-path for the error.
         * <br></br>This path describes the location of the error, within the request json body.
         *
         *
         * **Example:** `embed.fields[3].name`
         *
         * @return The JSON-path location
         */
        @get:Nonnull val location: String,
        /**
         * The list of [ErrorCodes][ErrorCode] associated with this schema error.
         *
         * @return The error codes
         */
        @get:Nonnull val errors: List<ErrorCode>
    ) {

        override fun toString(): String {
            return (if (location.isEmpty()) "" else location + "\n") + "\t- " + errors.stream()
                .map { obj: ErrorCode -> obj.toString() }
                .collect(Collectors.joining("\n\t- "))
        }
    }

    companion object {
        @JvmStatic
        fun create(errorResponse: ErrorResponse, response: Response): ErrorResponseException {
            var meaning = errorResponse.meaning
            var code = errorResponse.code
            val schemaErrors: MutableList<SchemaError> = ArrayList()
            try {
                val optObj = response.optObject()
                if (response.isError && response.exception != null) {
                    // this generally means that an exception occurred trying to
                    //make an http request. e.g.:
                    //SocketTimeoutException/ UnknownHostException
                    code = response.code
                    meaning = response.exception!!.javaClass.getName()
                } else if (optObj.isPresent) {
                    val obj = optObj.get()
                    if (!obj.isNull("code") || !obj.isNull("message")) {
                        if (!obj.isNull("code")) code = obj.getInt("code")
                        if (!obj.isNull("message")) meaning = obj.getString("message")
                    } else {
                        // This means that neither code or message is provided
                        //In that case we simply put the raw response in place!
                        code = response.code
                        meaning = obj.toString()
                    }
                    obj.optObject("errors").ifPresent { schema: DataObject -> parseSchema(schemaErrors, "", schema) }
                } else {
                    // error response body is not JSON
                    code = response.code
                    meaning = response.string
                }
            } catch (e: Exception) {
                JDALogger.getLog(ErrorResponseException::class.java)
                    .error("Failed to parse parts of error response. Body: {}", response.string, e)
            }
            return ErrorResponseException(errorResponse, response, code, meaning, schemaErrors)
        }

        private fun parseSchema(schemaErrors: MutableList<SchemaError>, currentLocation: String, errors: DataObject) {
            // check what kind of errors we are dealing with
            for (name in errors.keys()) {
                if (name == "_errors") {
                    schemaErrors.add(parseSchemaError(currentLocation, errors))
                    continue
                }
                val schemaError = errors.getObject(name)
                if (!schemaError.isNull("_errors")) {
                    // We are dealing with an Object Error
                    schemaErrors.add(parseSchemaError(currentLocation + name, schemaError))
                } else if (schemaError.keys().stream().allMatch { input: String? -> Helpers.isNumeric(input) }) {
                    // We have an Array Error
                    for (index in schemaError.keys()) {
                        val properties = schemaError.getObject(index)
                        val location = String.format("%s%s[%s].", currentLocation, name, index)
                        if (properties.hasKey("_errors")) schemaErrors.add(
                            parseSchemaError(
                                location.substring(
                                    0,
                                    location.length - 1
                                ), properties
                            )
                        ) else parseSchema(schemaErrors, location, properties)
                    }
                } else {
                    // We have a nested schema error, use recursion!
                    val location = String.format("%s%s.", currentLocation, name)
                    parseSchema(schemaErrors, location, schemaError)
                }
            }
        }

        private fun parseSchemaError(location: String, obj: DataObject): SchemaError {
            val codes = obj.getArray("_errors")
                .stream { obj: DataArray, index: Int? ->
                    obj.getObject(
                        index!!
                    )
                }
                .map { json: DataObject -> ErrorCode(json.getString("code"), json.getString("message")) }
                .collect(Collectors.toList())
            return SchemaError(location, codes)
        }

        /**
         * Ignore the specified set of error responses.
         *
         *
         * **Example**<br></br>
         * <pre>`// Creates a message with the provided content and deletes it 30 seconds later
         * public static void selfDestruct(MessageChannel channel, String content) {
         * channel.sendMessage(content).queue((message) ->
         * message.delete().queueAfter(30, SECONDS, null, ignore(EnumSet.of(UNKNOWN_MESSAGE)))
         * );
         * }
        `</pre> *
         *
         * @param  set
         * Set of ignored error responses
         *
         * @throws IllegalArgumentException
         * If provided with null or an empty collection
         *
         * @return [Consumer] decorator for [RestAction.getDefaultFailure]
         * which ignores the specified [ErrorResponses][ErrorResponse]
         */
        @Nonnull
        fun ignore(@Nonnull set: Collection<ErrorResponse?>?): Consumer<Throwable>? {
            return ignore(RestAction.getDefaultFailure(), set)
        }

        /**
         * Ignore the specified set of error responses.
         *
         *
         * **Example**<br></br>
         * <pre>`// Creates a message with the provided content and deletes it 30 seconds later
         * public static void selfDestruct(MessageChannel channel, String content) {
         * channel.sendMessage(content).queue((message) ->
         * message.delete().queueAfter(30, SECONDS, null, ignore(UNKNOWN_MESSAGE))
         * );
         * }
        `</pre> *
         *
         * @param  ignored
         * Ignored error response
         * @param  errorResponses
         * Additional error responses to ignore
         *
         * @throws IllegalArgumentException
         * If provided with null
         *
         * @return [Consumer] decorator for [RestAction.getDefaultFailure]
         * which ignores the specified [ErrorResponses][ErrorResponse]
         */
        @Nonnull
        fun ignore(
            @Nonnull ignored: ErrorResponse?,
            @Nonnull vararg errorResponses: ErrorResponse?
        ): Consumer<Throwable>? {
            return ignore(RestAction.getDefaultFailure(), ignored, *errorResponses)
        }

        /**
         * Ignore the specified set of error responses.
         *
         *
         * **Example**<br></br>
         * <pre>`// Creates a message with the provided content and deletes it 30 seconds later
         * public static void selfDestruct(MessageChannel channel, String content) {
         * channel.sendMessage(content).queue((message) ->
         * message.delete().queueAfter(30, SECONDS, null, ignore(Throwable::printStackTrace, UNKNOWN_MESSAGE))
         * );
         * }
        `</pre> *
         *
         * @param  orElse
         * Behavior to default to if the error response is not ignored
         * @param  ignored
         * Ignored error response
         * @param  errorResponses
         * Additional error responses to ignore
         *
         * @throws IllegalArgumentException
         * If provided with null
         *
         * @return [Consumer] decorator for the provided callback
         * which ignores the specified [ErrorResponses][ErrorResponse]
         */
        @Nonnull
        fun ignore(
            @Nonnull orElse: Consumer<in Throwable>,
            @Nonnull ignored: ErrorResponse?,
            @Nonnull vararg errorResponses: ErrorResponse?
        ): Consumer<Throwable>? {
            return ignore(orElse, EnumSet.of(ignored, *errorResponses))
        }

        /**
         * Ignore the specified set of error responses.
         *
         *
         * **Example**<br></br>
         * <pre>`// Creates a message with the provided content and deletes it 30 seconds later
         * public static void selfDestruct(MessageChannel channel, String content) {
         * channel.sendMessage(content).queue((message) ->
         * message.delete().queueAfter(30, SECONDS, null, ignore(Throwable::printStackTrace, EnumSet.of(UNKNOWN_MESSAGE)))
         * );
         * }
        `</pre> *
         *
         * @param  orElse
         * Behavior to default to if the error response is not ignored
         * @param  set
         * Set of ignored error responses
         *
         * @throws IllegalArgumentException
         * If provided with null or an empty collection
         *
         * @return [Consumer] decorator for the provided callback
         * which ignores the specified [ErrorResponses][ErrorResponse]
         */
        @Nonnull
        fun ignore(
            @Nonnull orElse: Consumer<in Throwable>,
            @Nonnull set: Collection<ErrorResponse?>?
        ): Consumer<Throwable>? {
            Checks.notNull(orElse, "Callback")
            Checks.notEmpty(set, "Ignored collection")
            // Make an enum set copy (for performance, memory efficiency, and thread-safety)
            val ignored = EnumSet.copyOf(set)
            return ErrorHandler(orElse).ignore(ignored)
        }
    }
}
