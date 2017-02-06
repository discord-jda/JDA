/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.core.exceptions;

import net.dv8tion.jda.core.requests.ErrorResponse;
import net.dv8tion.jda.core.requests.Response;

/**
 * Indicates an unhandled error that is returned by Discord API Request using {@link net.dv8tion.jda.core.requests.RestAction RestAction}
 * <br>It holds an {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponse}
 */
public class ErrorResponseException extends RuntimeException
{
    private final ErrorResponse errorResponse;
    private final Response response;

    /**
     * Creates a new ErrorResponseException instance
     *
     * @param errorResponse
     *        The {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponse} corresponding
     *        for the received error response from Discord
     * @param response
     *        The Discord Response causing the ErrorResponse
     */
    public ErrorResponseException(ErrorResponse errorResponse, Response response)
    {
        super(errorResponse.getMeaning()
                + ((errorResponse == ErrorResponse.UNKNOWN_ERROR || errorResponse == ErrorResponse.UNDEFINED_ERROR)
                    ? " : " + response.getString()
                    : ""));
        this.response = response;
        this.errorResponse = errorResponse;
    }

    /**
     * The {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponse} corresponding
     * for the received error response from Discord
     *
     * @return {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponse}
     */
    public ErrorResponse getErrorResponse()
    {
        return errorResponse;
    }

    /**
     * The Discord Response causing the ErrorResponse
     *
     * @return {@link net.dv8tion.jda.core.requests.Response Response}
     */
    public Response getResponse()
    {
        return response;
    }
}
