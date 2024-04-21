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
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.internal.utils.Checks
import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate
import javax.annotation.Nonnull

/**
 * Utility class to simplify error handling with [RestActions][RestAction] and [ErrorResponses][ErrorResponse].
 *
 *
 * **Example**<br></br>
 * <pre>`// Send message to user and delete it 30 seconds later, handles blocked messages in context channel.
 * public void sendMessage(TextChannel context, User user, String content) {
 * user.openPrivateChannel()
 * .flatMap(channel -> channel.sendMessage(content))
 * .delay(Duration.ofSeconds(30))
 * .flatMap(Message::delete) // delete after 30 seconds
 * .queue(null, new ErrorHandler()
 * .ignore(ErrorResponse.UNKNOWN_MESSAGE) // if delete fails that's fine
 * .handle(
 * ErrorResponse.CANNOT_SEND_TO_USER,  // Fallback handling for blocked messages
 * (e) -> context.sendMessage("Failed to send message, you block private messages!").queue()));
 * }
`</pre> *
 *
 * @see ErrorResponse
 *
 * @see ErrorResponseException
 *
 * @see RestAction.queue
 * @since 4.2.0
 */
class ErrorHandler @JvmOverloads constructor(@Nonnull base: Consumer<in Throwable> = RestAction.getDefaultFailure()) :
    Consumer<Throwable> {
    private val base: Consumer<in Throwable>
    private val cases: MutableMap<Predicate<in Throwable>, Consumer<in Throwable>> = LinkedHashMap()
    /**
     * Create an ErrorHandler with the specified consumer as base consumer.
     * <br></br>If none of the provided ignore/handle cases apply, the base consumer is applied instead.
     *
     * @param base
     * The base [Consumer]
     */
    /**
     * Create an ErrorHandler with [RestAction.getDefaultFailure] as base consumer.
     * <br></br>If none of the provided ignore/handle cases apply, the base consumer is applied instead.
     */
    init {
        Checks.notNull(base, "Consumer")
        this.base = base
    }

    /**
     * Ignore the specified set of error responses.
     *
     *
     * **Example**<br></br>
     * <pre>`// Creates a message with the provided content and deletes it 30 seconds later
     * public static void selfDestruct(MessageChannel channel, String content) {
     * channel.sendMessage(content)
     * .delay(Duration.ofSeconds(30))
     * .flatMap(Message::delete)
     * .queue(null, new ErrorHandler().ignore(UNKNOWN_MESSAGE));
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
     * @return This ErrorHandler with the applied ignore cases
     */
    @Nonnull
    fun ignore(@Nonnull ignored: ErrorResponse?, @Nonnull vararg errorResponses: ErrorResponse?): ErrorHandler {
        Checks.notNull(ignored, "ErrorResponse")
        Checks.noneNull(errorResponses, "ErrorResponse")
        return ignore(EnumSet.of(ignored, *errorResponses))
    }

    /**
     * Ignore the specified set of error responses.
     *
     *
     * **Example**<br></br>
     * <pre>`// Creates a message with the provided content and deletes it 30 seconds later
     * public static void selfDestruct(User user, String content) {
     * user.openPrivateChannel()
     * .flatMap(channel -> channel.sendMessage(content))
     * .delay(Duration.ofSeconds(30))
     * .flatMap(Message::delete)
     * .queue(null, new ErrorHandler().ignore(EnumSet.of(UNKNOWN_MESSAGE, CANNOT_SEND_TO_USER)));
     * }
    `</pre> *
     *
     * @param  errorResponses
     * The error responses to ignore
     *
     * @throws IllegalArgumentException
     * If provided with null
     *
     * @return This ErrorHandler with the applied ignore cases
     */
    @Nonnull
    fun ignore(@Nonnull errorResponses: Collection<ErrorResponse?>): ErrorHandler {
        return handle(errorResponses, empty)
    }

    /**
     * Ignore exceptions of the specified types.
     *
     *
     * **Example**<br></br>
     * <pre>`// Ignore SocketTimeoutException
     * public static void ban(Guild guild, String userId) {
     * guild.ban(userId).queue(null, new ErrorHandler().ignore(SocketTimeoutException.class);
     * }
    `</pre> *
     *
     * @param  clazz
     * The class to ignore
     * @param  classes
     * Additional classes to ignore
     *
     * @throws IllegalArgumentException
     * If provided with null
     *
     * @return This ErrorHandler with the applied ignore case
     *
     * @see java.net.SocketTimeoutException
     */
    @Nonnull
    fun ignore(@Nonnull clazz: Class<*>, @Nonnull vararg classes: Class<*>): ErrorHandler {
        Checks.notNull(clazz, "Classes")
        Checks.noneNull(classes, "Classes")
        return ignore { it: Throwable? ->
            if (clazz.isInstance(it)) return@ignore true
            for (e in classes) {
                if (e.isInstance(it)) return@ignore true
            }
            false
        }
    }

    /**
     * Ignore exceptions on specific conditions.
     *
     *
     * **Example**<br></br>
     * <pre>`// Ignore all exceptions except for ErrorResponseException
     * public static void ban(Guild guild, String userId) {
     * guild.ban(userId).queue(null, new ErrorHandler().ignore((ex) -> !(ex instanceof ErrorResponseException));
     * }
    `</pre> *
     *
     * @param  condition
     * The condition to check
     *
     * @throws IllegalArgumentException
     * If provided with null
     *
     * @return This ErrorHandler with the applied ignore case
     *
     * @see ErrorResponseException
     */
    @Nonnull
    fun ignore(@Nonnull condition: Predicate<in Throwable>): ErrorHandler {
        return handle(condition, empty)
    }

    /**
     * Handle specific [ErrorResponses][ErrorResponse].
     * <br></br>This will apply the specified handler to use instead of the base consumer if one of the provided ErrorResponses happens.
     *
     *
     * **Example**<br></br>
     * <pre>`public static void sendMessage(TextChannel context, User user, String content) {
     * user.openPrivateChannel()
     * .flatMap(channel -> channel.sendMessage(content))
     * .queue(null, new ErrorHandler()
     * .handle(ErrorResponse.CANNOT_SEND_TO_USER,
     * (ex) -> context.sendMessage("Cannot send direct message, please enable direct messages from server members!").queue()));
     * }
    `</pre> *
     *
     * @param  response
     * The first [ErrorResponse] to match
     * @param  handler
     * The alternative handler
     *
     * @throws IllegalArgumentException
     * If provided with null
     *
     * @return This ErrorHandler with the applied handler
     */
    @Nonnull
    fun handle(@Nonnull response: ErrorResponse?, @Nonnull handler: Consumer<in ErrorResponseException>): ErrorHandler {
        Checks.notNull(response, "ErrorResponse")
        return handle(EnumSet.of(response), handler)
    }

    /**
     * Handle specific [ErrorResponses][ErrorResponse].
     * <br></br>This will apply the specified handler to use instead of the base consumer if one of the provided ErrorResponses happens.
     *
     *
     * **Example**<br></br>
     * <pre>`public static void sendMessage(TextChannel context, User user, String content) {
     * user.openPrivateChannel()
     * .flatMap(channel -> channel.sendMessage(content))
     * .queue(null, new ErrorHandler()
     * .handle(EnumSet.of(ErrorResponse.CANNOT_SEND_TO_USER),
     * (ex) -> context.sendMessage("Cannot send direct message, please enable direct messages from server members!").queue()));
     * }
    `</pre> *
     *
     * @param  errorResponses
     * The [ErrorResponses][ErrorResponse] to match
     * @param  handler
     * The alternative handler
     *
     * @throws IllegalArgumentException
     * If provided with null
     *
     * @return This ErrorHandler with the applied handler
     */
    @Nonnull
    fun handle(
        @Nonnull errorResponses: Collection<ErrorResponse?>,
        @Nonnull handler: Consumer<in ErrorResponseException>
    ): ErrorHandler {
        Checks.notNull(handler, "Handler")
        Checks.noneNull(errorResponses, "ErrorResponse")
        return handle(
            ErrorResponseException::class.java,
            { it: ErrorResponseException -> errorResponses.contains(it.errorResponse) },
            handler
        )
    }

    /**
     * Handle specific throwable types.
     * <br></br>This will apply the specified handler if the throwable is of the specified type. The check is done using [Class.isInstance].
     *
     *
     * **Example**<br></br>
     * <pre>`public static void logErrorResponse(RestAction<?> action) {
     * action.queue(null, new ErrorHandler()
     * .handle(ErrorResponseException.class,
     * (ex) -> System.out.println(ex.getErrorResponse())));
     * }
    `</pre> *
     *
     * @param  clazz
     * The throwable type
     * @param  handler
     * The alternative handler
     *
     * @param  <T>
     * The type
     *
     * @return This ErrorHandler with the applied handler
    </T> */
    @Nonnull
    fun <T> handle(@Nonnull clazz: Class<T>, @Nonnull handler: Consumer<in T>): ErrorHandler {
        Checks.notNull(clazz, "Class")
        Checks.notNull(handler, "Handler")
        return handle({ obj: Throwable? -> clazz.isInstance(obj) }) { ex: Throwable? -> handler.accept(clazz.cast(ex)) }
    }

    /**
     * Handle specific throwable types.
     * <br></br>This will apply the specified handler if the throwable is of the specified type. The check is done using [Class.isInstance].
     *
     *
     * **Example**<br></br>
     * <pre>`public static void logErrorResponse(RestAction<?> action) {
     * action.queue(null, new ErrorHandler()
     * .handle(ErrorResponseException.class,
     * ErrorResponseException::isServerError,
     * (ex) -> System.out.println(ex.getErrorCode() + ": " + ex.getMeaning())));
     * }
    `</pre> *
     *
     * @param  clazz
     * The throwable type
     * @param  condition
     * Additional condition that must apply to use this handler
     * @param  handler
     * The alternative handler
     *
     * @param  <T>
     * The type
     *
     * @return This ErrorHandler with the applied handler
    </T> */
    @Nonnull
    fun <T> handle(
        @Nonnull clazz: Class<T>,
        @Nonnull condition: Predicate<in T>,
        @Nonnull handler: Consumer<in T>
    ): ErrorHandler {
        Checks.notNull(clazz, "Class")
        Checks.notNull(handler, "Handler")
        return handle(
            { it: Throwable? -> clazz.isInstance(it) && condition.test(clazz.cast(it)) }
        ) { ex: Throwable? -> handler.accept(clazz.cast(ex)) }
    }

    /**
     * Handle specific throwable types.
     * <br></br>This will apply the specified handler if the throwable is of the specified type. The check is done using [Class.isInstance].
     *
     *
     * **Example**<br></br>
     * <pre>`public static void logErrorResponse(RestAction<?> action) {
     * action.queue(null, new ErrorHandler()
     * .handle(Arrays.asList(Throwable.class),
     * (ex) -> ex instanceof Error,
     * (ex) -> ex.printStackTrace()));
     * }
    `</pre> *
     *
     * @param  clazz
     * The throwable types
     * @param  condition
     * Additional condition that must apply to use this handler, or null to apply no additional condition
     * @param  handler
     * The alternative handler
     *
     * @return This ErrorHandler with the applied handler
     */
    @Nonnull
    fun handle(
        @Nonnull clazz: Collection<Class<*>?>?,
        condition: Predicate<in Throwable>?,
        @Nonnull handler: Consumer<in Throwable>
    ): ErrorHandler {
        Checks.noneNull(clazz, "Class")
        Checks.notNull(handler, "Handler")
        val classes: List<Class<*>?> = ArrayList(clazz)
        val check: Predicate<in Throwable> = Predicate { it: Throwable ->
            classes.stream()
                .anyMatch { c: Class<*>? -> c!!.isInstance(it) } && (condition == null || condition.test(it))
        }
        return handle(check, handler)
    }

    /**
     * Handle specific conditions.
     *
     *
     * **Example**<br></br>
     * <pre>`public static void logErrorResponse(RestAction<?> action) {
     * action.queue(null, new ErrorHandler()
     * .handle(
     * (ex) -> !(ex instanceof ErrorResponseException),
     * Throwable::printStackTrace));
     * }
    `</pre> *
     *
     * @param  condition
     * Condition that must apply to use this handler
     * @param  handler
     * The alternative handler
     *
     * @return This ErrorHandler with the applied handler
     */
    @Nonnull
    fun handle(@Nonnull condition: Predicate<in Throwable>, @Nonnull handler: Consumer<in Throwable>): ErrorHandler {
        Checks.notNull(condition, "Condition")
        Checks.notNull(handler, "Handler")
        cases[condition] = handler
        return this
    }

    override fun accept(t: Throwable) {
        for ((condition, callback) in cases) {
            if (condition.test(t)) {
                callback.accept(t)
                return
            }
        }
        base.accept(t)
    }

    companion object {
        private val empty: Consumer<in Throwable> = Consumer { e: Throwable? -> }
    }
}
