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

package net.dv8tion.jda.api.exceptions;

import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Utility class to simplify error handling with {@link RestAction RestActions} and {@link ErrorResponse ErrorResponses}.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * // Send message to user and delete it 30 seconds later, handles blocked messages in context channel.
 * public void sendMessage(TextChannel context, User user, String content) {
 *     user.openPrivateChannel()
 *         .flatMap(channel -> channel.sendMessage(content))
 *         .delay(Duration.ofSeconds(30))
 *         .flatMap(Message::delete) // delete after 30 seconds
 *         .queue(null, new ErrorHandler()
 *             .ignore(ErrorResponse.UNKNOWN_MESSAGE) // if delete fails that's fine
 *             .handle(
 *                 ErrorResponse.CANNOT_SEND_TO_USER,  // Fallback handling for blocked messages
 *                 (e) -> context.sendMessage("Failed to send message, you block private messages!").queue()));
 * }
 * }</pre>
 *
 * @see ErrorResponse
 * @see ErrorResponseException
 * @see RestAction#queue(Consumer, Consumer)
 *
 * @since 4.2.0
 */
public class ErrorHandler implements Consumer<Throwable>
{
    private static final Consumer<? super Throwable> empty = (e) -> {};
    private final Consumer<? super Throwable> base;
    private final Map<Predicate<? super Throwable>, Consumer<? super Throwable>> cases = new LinkedHashMap<>();

    /**
     * Create an ErrorHandler with {@link RestAction#getDefaultFailure()} as base consumer.
     * <br>If none of the provided ignore/handle cases apply, the base consumer is applied instead.
     */
    public ErrorHandler()
    {
        this(RestAction.getDefaultFailure());
    }

    /**
     * Create an ErrorHandler with the specified consumer as base consumer.
     * <br>If none of the provided ignore/handle cases apply, the base consumer is applied instead.
     *
     * @param base
     *        The base {@link Consumer}
     */
    public ErrorHandler(@Nonnull Consumer<? super Throwable> base)
    {
        Checks.notNull(base, "Consumer");
        this.base = base;
    }

    /**
     * Ignore the specified set of error responses.
     *
     * <h4>Example</h4>
     * <pre>{@code
     * // Creates a message with the provided content and deletes it 30 seconds later
     * public static void selfDestruct(MessageChannel channel, String content) {
     *     channel.sendMessage(content)
     *         .delay(Duration.ofSeconds(30))
     *         .flatMap(Message::delete)
     *         .queue(null, new ErrorHandler().ignore(UNKNOWN_MESSAGE));
     * }
     * }</pre>
     *
     * @param  ignored
     *         Ignored error response
     * @param  errorResponses
     *         Additional error responses to ignore
     *
     * @throws IllegalArgumentException
     *         If provided with null
     *
     * @return This ErrorHandler with the applied ignore cases
     */
    @Nonnull
    public ErrorHandler ignore(@Nonnull ErrorResponse ignored, @Nonnull ErrorResponse... errorResponses)
    {
        Checks.notNull(ignored, "ErrorResponse");
        Checks.noneNull(errorResponses, "ErrorResponse");
        return ignore(EnumSet.of(ignored, errorResponses));
    }

    /**
     * Ignore the specified set of error responses.
     *
     * <h4>Example</h4>
     * <pre>{@code
     * // Creates a message with the provided content and deletes it 30 seconds later
     * public static void selfDestruct(User user, String content) {
     *     user.openPrivateChannel()
     *         .flatMap(channel -> channel.sendMessage(content))
     *         .delay(Duration.ofSeconds(30))
     *         .flatMap(Message::delete)
     *         .queue(null, new ErrorHandler().ignore(EnumSet.of(UNKNOWN_MESSAGE, CANNOT_SEND_TO_USER)));
     * }
     * }</pre>
     *
     * @param  errorResponses
     *         The error responses to ignore
     *
     * @throws IllegalArgumentException
     *         If provided with null
     *
     * @return This ErrorHandler with the applied ignore cases
     */
    @Nonnull
    public ErrorHandler ignore(@Nonnull Collection<ErrorResponse> errorResponses)
    {
        return handle(errorResponses, empty);
    }

    /**
     * Ignore exceptions of the specified types.
     *
     * <h4>Example</h4>
     * <pre>{@code
     * // Ignore SocketTimeoutException
     * public static void ban(Guild guild, String userId) {
     *     guild.ban(userId).queue(null, new ErrorHandler().ignore(SocketTimeoutException.class);
     * }
     * }</pre>
     *
     * @param  clazz
     *         The class to ignore
     * @param  classes
     *         Additional classes to ignore
     *
     * @throws IllegalArgumentException
     *         If provided with null
     *
     * @return This ErrorHandler with the applied ignore case
     *
     * @see    java.net.SocketTimeoutException
     */
    @Nonnull
    public ErrorHandler ignore(@Nonnull Class<?> clazz, @Nonnull Class<?>... classes)
    {
        Checks.notNull(clazz, "Classes");
        Checks.noneNull(classes, "Classes");
        return ignore(it -> {
            if (clazz.isInstance(it))
                return true;
            for (Class<?> e : classes)
            {
                if (e.isInstance(it))
                    return true;
            }
            return false;
        });
    }

    /**
     * Ignore exceptions on specific conditions.
     *
     * <h4>Example</h4>
     * <pre>{@code
     * // Ignore all exceptions except for ErrorResponseException
     * public static void ban(Guild guild, String userId) {
     *     guild.ban(userId).queue(null, new ErrorHandler().ignore((ex) -> !(ex instanceof ErrorResponseException));
     * }
     * }</pre>
     *
     * @param  condition
     *         The condition to check
     *
     * @throws IllegalArgumentException
     *         If provided with null
     *
     * @return This ErrorHandler with the applied ignore case
     *
     * @see    ErrorResponseException
     */
    @Nonnull
    public ErrorHandler ignore(@Nonnull Predicate<? super Throwable> condition)
    {
        return handle(condition, empty);
    }

    /**
     * Handle specific {@link ErrorResponse ErrorResponses}.
     * <br>This will apply the specified handler to use instead of the base consumer if one of the provided ErrorResponses happens.
     *
     * <h4>Example</h4>
     * <pre>{@code
     * public static void sendMessage(TextChannel context, User user, String content) {
     *     user.openPrivateChannel()
     *         .flatMap(channel -> channel.sendMessage(content))
     *         .queue(null, new ErrorHandler()
     *             .handle(ErrorResponse.CANNOT_SEND_TO_USER,
     *                 (ex) -> context.sendMessage("Cannot send direct message, please enable direct messages from server members!").queue()));
     * }
     * }</pre>
     *
     * @param  response
     *         The first {@link ErrorResponse} to match
     * @param  handler
     *         The alternative handler
     *
     * @throws IllegalArgumentException
     *         If provided with null
     *
     * @return This ErrorHandler with the applied handler
     */
    @Nonnull
    public ErrorHandler handle(@Nonnull ErrorResponse response, @Nonnull Consumer<? super ErrorResponseException> handler)
    {
        Checks.notNull(response, "ErrorResponse");
        return handle(EnumSet.of(response), handler);
    }

    /**
     * Handle specific {@link ErrorResponse ErrorResponses}.
     * <br>This will apply the specified handler to use instead of the base consumer if one of the provided ErrorResponses happens.
     *
     * <h4>Example</h4>
     * <pre>{@code
     * public static void sendMessage(TextChannel context, User user, String content) {
     *     user.openPrivateChannel()
     *         .flatMap(channel -> channel.sendMessage(content))
     *         .queue(null, new ErrorHandler()
     *             .handle(EnumSet.of(ErrorResponse.CANNOT_SEND_TO_USER),
     *                 (ex) -> context.sendMessage("Cannot send direct message, please enable direct messages from server members!").queue()));
     * }
     * }</pre>
     *
     * @param  errorResponses
     *         The {@link ErrorResponse ErrorResponses} to match
     * @param  handler
     *         The alternative handler
     *
     * @throws IllegalArgumentException
     *         If provided with null
     *
     * @return This ErrorHandler with the applied handler
     */
    @Nonnull
    public ErrorHandler handle(@Nonnull Collection<ErrorResponse> errorResponses, @Nonnull Consumer<? super ErrorResponseException> handler)
    {
        Checks.notNull(handler, "Handler");
        Checks.noneNull(errorResponses, "ErrorResponse");
        return handle(ErrorResponseException.class, (it) -> errorResponses.contains(it.getErrorResponse()), handler);
    }

    /**
     * Handle specific throwable types.
     * <br>This will apply the specified handler if the throwable is of the specified type. The check is done using {@link Class#isInstance(Object)}.
     *
     * <h4>Example</h4>
     * <pre>{@code
     * public static void logErrorResponse(RestAction<?> action) {
     *     action.queue(null, new ErrorHandler()
     *         .handle(ErrorResponseException.class,
     *             (ex) -> System.out.println(ex.getErrorResponse())));
     * }
     * }</pre>
     *
     * @param  clazz
     *         The throwable type
     * @param  handler
     *         The alternative handler
     *
     * @param  <T>
     *         The type
     *
     * @return This ErrorHandler with the applied handler
     */
    @Nonnull
    public <T> ErrorHandler handle(@Nonnull Class<T> clazz, @Nonnull Consumer<? super T> handler)
    {
        Checks.notNull(clazz, "Class");
        Checks.notNull(handler, "Handler");
        return handle(clazz::isInstance, (ex) -> handler.accept(clazz.cast(ex)));
    }

    /**
     * Handle specific throwable types.
     * <br>This will apply the specified handler if the throwable is of the specified type. The check is done using {@link Class#isInstance(Object)}.
     *
     * <h4>Example</h4>
     * <pre>{@code
     * public static void logErrorResponse(RestAction<?> action) {
     *     action.queue(null, new ErrorHandler()
     *         .handle(ErrorResponseException.class,
     *             ErrorResponseException::isServerError,
     *             (ex) -> System.out.println(ex.getErrorCode() + ": " + ex.getMeaning())));
     * }
     * }</pre>
     *
     * @param  clazz
     *         The throwable type
     * @param  condition
     *         Additional condition that must apply to use this handler
     * @param  handler
     *         The alternative handler
     *
     * @param  <T>
     *         The type
     *
     * @return This ErrorHandler with the applied handler
     */
    @Nonnull
    public <T> ErrorHandler handle(@Nonnull Class<T> clazz, @Nonnull Predicate<? super T> condition, @Nonnull Consumer<? super T> handler)
    {
        Checks.notNull(clazz, "Class");
        Checks.notNull(handler, "Handler");
        return handle(
            (it) -> clazz.isInstance(it) && condition.test(clazz.cast(it)),
            (ex) -> handler.accept(clazz.cast(ex)));
    }

    /**
     * Handle specific throwable types.
     * <br>This will apply the specified handler if the throwable is of the specified type. The check is done using {@link Class#isInstance(Object)}.
     *
     * <h4>Example</h4>
     * <pre>{@code
     * public static void logErrorResponse(RestAction<?> action) {
     *     action.queue(null, new ErrorHandler()
     *         .handle(Arrays.asList(Throwable.class),
     *             (ex) -> ex instanceof Error,
     *             (ex) -> ex.printStackTrace()));
     * }
     * }</pre>
     *
     * @param  clazz
     *         The throwable types
     * @param  condition
     *         Additional condition that must apply to use this handler, or null to apply no additional condition
     * @param  handler
     *         The alternative handler
     *
     * @return This ErrorHandler with the applied handler
     */
    @Nonnull
    public ErrorHandler handle(@Nonnull Collection<Class<?>> clazz, @Nullable Predicate<? super Throwable> condition, @Nonnull Consumer<? super Throwable> handler)
    {
        Checks.noneNull(clazz, "Class");
        Checks.notNull(handler, "Handler");
        List<Class<?>> classes = new ArrayList<>(clazz);
        Predicate<? super Throwable> check = (it) -> classes.stream().anyMatch(c -> c.isInstance(it)) && (condition == null || condition.test(it));
        return handle(check, handler);
    }

    /**
     * Handle specific conditions.
     *
     * <h4>Example</h4>
     * <pre>{@code
     * public static void logErrorResponse(RestAction<?> action) {
     *     action.queue(null, new ErrorHandler()
     *         .handle(
     *             (ex) -> !(ex instanceof ErrorResponseException),
     *             Throwable::printStackTrace));
     * }
     * }</pre>
     *
     * @param  condition
     *         Condition that must apply to use this handler
     * @param  handler
     *         The alternative handler
     *
     * @return This ErrorHandler with the applied handler
     */
    @Nonnull
    public ErrorHandler handle(@Nonnull Predicate<? super Throwable> condition, @Nonnull Consumer<? super Throwable> handler)
    {
        Checks.notNull(condition, "Condition");
        Checks.notNull(handler, "Handler");
        cases.put(condition, handler);
        return this;
    }

    @Override
    public void accept(Throwable t)
    {
        for (Map.Entry<Predicate<? super Throwable>, Consumer<? super Throwable>> entry : cases.entrySet())
        {
            Predicate<? super Throwable> condition = entry.getKey();
            Consumer<? super Throwable> callback = entry.getValue();
            if (condition.test(t))
            {
                callback.accept(t);
                return;
            }
        }

        base.accept(t);
    }
}
