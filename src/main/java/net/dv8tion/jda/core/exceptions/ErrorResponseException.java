/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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
import org.json.JSONObject;

import java.util.Optional;

/**
 * Indicates an unhandled error that is returned by Discord API Request using {@link net.dv8tion.jda.core.requests.RestAction RestAction}
 * <br>It holds an {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponse}
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
     *        The {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponse} corresponding
     *        for the received error response from Discord
     * @param response
     *        The Discord Response causing the ErrorResponse
     */
    private ErrorResponseException(ErrorResponse errorResponse, Response response, int code, String meaning)
    {
        super(code + ": " + meaning);

        this.response = response;
        this.errorResponse = errorResponse;
        this.code = code;
        this.meaning = meaning;
    }

    /**
     * Whether this is an internal server error from discord (status 500)
     *
     * @return True, if this is an internal server error
     *         {@link net.dv8tion.jda.core.requests.ErrorResponse#SERVER_ERROR ErrorResponse.SERVER_ERROR}
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

    public static ErrorResponseException create(ErrorResponse errorResponse, Response response)
    {
        Optional<JSONObject> optObj = response.optObject();
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
            JSONObject obj = optObj.get();
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
}
