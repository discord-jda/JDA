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

package net.dv8tion.jda.api.exceptions;

import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Indicates an unhandled error that is returned by Discord API Request using {@link net.dv8tion.jda.api.requests.RestAction RestAction}
 * <br>It holds an {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponse}
 *
 * @see net.dv8tion.jda.api.exceptions.ErrorHandler
 */
public class ErrorResponseException extends RuntimeException
{
    private final ErrorResponse errorResponse;
    private final Response response;
    private final String meaning;
    private final int code;

    /**
     * Creates a new ErrorResponseException instance
     *
     * @param errorResponse
     *        The {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponse} corresponding
     *        for the received error response from Discord
     * @param response
     *        The Discord Response causing the ErrorResponse
     */
    private ErrorResponseException(ErrorResponse errorResponse, Response response, int code, String meaning)
    {
        super(code + ": " + meaning);

        this.response = response;
        if (response != null && response.getException() != null)
            initCause(response.getException());
        this.errorResponse = errorResponse;
        this.code = code;
        this.meaning = meaning;
    }

    /**
     * Whether this is an internal server error from discord (status 500)
     *
     * @return True, if this is an internal server error
     *         {@link net.dv8tion.jda.api.requests.ErrorResponse#SERVER_ERROR ErrorResponse.SERVER_ERROR}
     */
    public boolean isServerError()
    {
        return errorResponse == ErrorResponse.SERVER_ERROR;
    }

    /**
     * The meaning for this error.
     * <br>It is possible that the value from this method is different for {@link #isServerError() server errors}
     *
     * @return Never-null meaning of this error.
     */
    public String getMeaning()
    {
        return meaning;
    }

    /**
     * The discord error code for this error response.
     *
     * @return The discord error code.
     *
     * @see <a href="https://discordapp.com/developers/docs/topics/response-codes#json-error-response" target="_blank">Discord Error Codes</a>
     */
    public int getErrorCode()
    {
        return code;
    }

    /**
     * The {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponse} corresponding
     * for the received error response from Discord
     *
     * @return {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponse}
     */
    public ErrorResponse getErrorResponse()
    {
        return errorResponse;
    }

    /**
     * The Discord Response causing the ErrorResponse
     *
     * @return {@link net.dv8tion.jda.api.requests.Response Response}
     */
    public Response getResponse()
    {
        return response;
    }

    public static ErrorResponseException create(ErrorResponse errorResponse, Response response)
    {
        Optional<DataObject> optObj = response.optObject();
        String meaning = errorResponse.getMeaning();
        int code = errorResponse.getCode();
        if (response.isError() && response.getException() != null)
        {
            // this generally means that an exception occurred trying to
            //make an http request. e.g.:
            //SocketTimeoutException/ UnknownHostException
            code = response.code;
            meaning = response.getException().getClass().getName();
        }
        else if (optObj.isPresent())
        {
            DataObject obj = optObj.get();
            if (!obj.isNull("code") || !obj.isNull("message"))
            {
                if (!obj.isNull("code"))
                    code = obj.getInt("code");
                if (!obj.isNull("message"))
                    meaning = obj.getString("message");
            }
            else
            {
                // This means that neither code or message is provided
                //In that case we simply put the raw response in place!
                code = response.code;
                meaning = obj.toString();
            }
        }
        else
        {
            // error response body is not JSON
            code = response.code;
            meaning = response.getString();
        }

        return new ErrorResponseException(errorResponse, response, code, meaning);
    }

    /**
     * Ignore the specified set of error responses.
     *
     * <h2>Example</h2>
     * <pre>{@code
     * // Creates a message with the provided content and deletes it 30 seconds later
     * public static void selfDestruct(MessageChannel channel, String content) {
     *     channel.sendMessage(content).queue((message) ->
     *         message.delete().queueAfter(30, SECONDS, null, ignore(EnumSet.of(UNKNOWN_MESSAGE)))
     *     );
     * }
     * }</pre>
     *
     * @param  set
     *         Set of ignored error responses
     *
     * @throws IllegalArgumentException
     *         If provided with null or an empty collection
     *
     * @return {@link Consumer} decorator for {@link RestAction#getDefaultFailure()}
     *         which ignores the specified {@link ErrorResponse ErrorResponses}
     */
    @Nonnull
    public static Consumer<Throwable> ignore(@Nonnull Collection<ErrorResponse> set)
    {
        return ignore(RestAction.getDefaultFailure(), set);
    }

    /**
     * Ignore the specified set of error responses.
     *
     * <h2>Example</h2>
     * <pre>{@code
     * // Creates a message with the provided content and deletes it 30 seconds later
     * public static void selfDestruct(MessageChannel channel, String content) {
     *     channel.sendMessage(content).queue((message) ->
     *         message.delete().queueAfter(30, SECONDS, null, ignore(UNKNOWN_MESSAGE))
     *     );
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
     * @return {@link Consumer} decorator for {@link RestAction#getDefaultFailure()}
     *         which ignores the specified {@link ErrorResponse ErrorResponses}
     */
    @Nonnull
    public static Consumer<Throwable> ignore(@Nonnull ErrorResponse ignored, @Nonnull ErrorResponse... errorResponses)
    {
        return ignore(RestAction.getDefaultFailure(), ignored, errorResponses);
    }

    /**
     * Ignore the specified set of error responses.
     *
     * <h2>Example</h2>
     * <pre>{@code
     * // Creates a message with the provided content and deletes it 30 seconds later
     * public static void selfDestruct(MessageChannel channel, String content) {
     *     channel.sendMessage(content).queue((message) ->
     *         message.delete().queueAfter(30, SECONDS, null, ignore(Throwable::printStackTrace, UNKNOWN_MESSAGE))
     *     );
     * }
     * }</pre>
     *
     * @param  orElse
     *         Behavior to default to if the error response is not ignored
     * @param  ignored
     *         Ignored error response
     * @param  errorResponses
     *         Additional error responses to ignore
     *
     * @throws IllegalArgumentException
     *         If provided with null
     *
     * @return {@link Consumer} decorator for the provided callback
     *         which ignores the specified {@link ErrorResponse ErrorResponses}
     */
    @Nonnull
    public static Consumer<Throwable> ignore(@Nonnull Consumer<? super Throwable> orElse, @Nonnull ErrorResponse ignored, @Nonnull ErrorResponse... errorResponses)
    {
        return ignore(orElse, EnumSet.of(ignored, errorResponses));
    }

    /**
     * Ignore the specified set of error responses.
     *
     * <h2>Example</h2>
     * <pre>{@code
     * // Creates a message with the provided content and deletes it 30 seconds later
     * public static void selfDestruct(MessageChannel channel, String content) {
     *     channel.sendMessage(content).queue((message) ->
     *         message.delete().queueAfter(30, SECONDS, null, ignore(Throwable::printStackTrace, EnumSet.of(UNKNOWN_MESSAGE)))
     *     );
     * }
     * }</pre>
     *
     * @param  orElse
     *         Behavior to default to if the error response is not ignored
     * @param  set
     *         Set of ignored error responses
     *
     * @throws IllegalArgumentException
     *         If provided with null or an empty collection
     *
     * @return {@link Consumer} decorator for the provided callback
     *         which ignores the specified {@link ErrorResponse ErrorResponses}
     */
    @Nonnull
    public static Consumer<Throwable> ignore(@Nonnull Consumer<? super Throwable> orElse, @Nonnull Collection<ErrorResponse> set)
    {
        Checks.notNull(orElse, "Callback");
        Checks.notEmpty(set, "Ignored collection");
        // Make an enum set copy (for performance, memory efficiency, and thread-safety)
        final EnumSet<ErrorResponse> ignored = EnumSet.copyOf(set);
        return new ErrorHandler(orElse).ignore(ignored);
    }
}
