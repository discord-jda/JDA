/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.core.requests;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Response implements Closeable
{
    public static final int ERROR_CODE = -1;
    public static final String ERROR_MESSAGE = "ERROR";

    public final int code;
    public final String message;
    public final long retryAfter;
    private final Object object;
    private final okhttp3.Response rawResponse;
    private final Set<String> cfRays;
    private Exception exception;

    protected Response(final okhttp3.Response response, final Exception exception, final Set<String> cfRays)
    {
        this(response, response != null ? response.code() : ERROR_CODE, ERROR_MESSAGE, -1, cfRays);
        this.exception = exception;
    }

    protected Response(final okhttp3.Response response, final int code, final String message, final long retryAfter, final Set<String> cfRays)
    {
        this.rawResponse = response;
        this.code = code;
        this.message = message;
        this.exception = null;
        this.retryAfter = retryAfter;
        this.cfRays = cfRays;

        if (response == null || response.body().contentLength() == 0)
        {
            this.object = null;
            return;
        }

        InputStream body = null;
        BufferedReader reader = null;
        try
        {
            body = Requester.getBody(response);
            // this doesn't add overhead as org.json would do that itself otherwise
            reader = new BufferedReader(new InputStreamReader(body));
            char begin; // not sure if I really like this... but we somehow have to get if this is an object or an array
            int mark = 1;
            do
            {
                reader.mark(mark++);
                begin = (char) reader.read();
            }
            while (Character.isWhitespace(begin));

            reader.reset();

            if (begin == '{')
                this.object = new JSONObject(new JSONTokener(reader));
            else if (begin == '[')
                this.object = new JSONArray(new JSONTokener(reader));
            else
                this.object = reader.lines().collect(Collectors.joining());
        }
        catch (final Exception e)
        {
            throw new RuntimeException("An error occurred while parsing the response for a RestAction", e);
        }
        finally
        {
            try
            {
                body.close();
                reader.close();
            } catch (NullPointerException | IOException ignored) {}
        }
    }

    protected Response(final long retryAfter, final Set<String> cfRays)
    {
        this(null, 429, "TOO MANY REQUESTS", retryAfter, cfRays);
    }

    protected Response(final okhttp3.Response response, final long retryAfter, final Set<String> cfRays)
    {
        this(response, response.code(), response.message(), retryAfter, cfRays);
    }

    public JSONArray getArray()
    {
        return this.object instanceof JSONArray ? (JSONArray) this.object : null;
    }

    public JSONObject getObject()
    {
        return this.object instanceof JSONObject ? (JSONObject) this.object : null;
    }

    public String getString()
    {
        return Objects.toString(object);
    }

    public okhttp3.Response getRawResponse()
    {
        return this.rawResponse;
    }

    public Set<String> getCFRays()
    {
        return cfRays;
    }

    public boolean isError()
    {
        return this.code == Response.ERROR_CODE;
    }

    public boolean isOk()
    {
        return this.code > 199 && this.code < 300;
    }

    public boolean isRateLimit()
    {
        return this.code == 429;
    }

    @Override
    public String toString()
    {
        return this.exception == null
                ? "HTTPResponse[" + this.code + (this.object == null ? "" : ", " + this.object.toString()) + ']'
                : "HTTPException[" + this.exception.getMessage() + ']';
    }

    @Override
    public void close()
    {
        if (rawResponse != null)
            rawResponse.close();
    }
}
