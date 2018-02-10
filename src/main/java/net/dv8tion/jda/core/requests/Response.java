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

package net.dv8tion.jda.core.requests;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Response implements Closeable
{
    public static final int ERROR_CODE = -1;
    public static final String ERROR_MESSAGE = "ERROR";

    public final int code;
    public final String message;
    public final long retryAfter;
    private final InputStream body;
    private final okhttp3.Response rawResponse;
    private final Set<String> cfRays;
    private Object object;
    private boolean attemptedParsing = false;
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

        if (response == null)
        {
            this.body = null;
            return;
        }

        try
        {
            this.body = Requester.getBody(response);
        }
        catch (final Exception e)
        {
            throw new IllegalStateException("An error occurred while parsing the response for a RestAction", e);
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

    @Nullable
    public JSONArray getArray()
    {
        return parseBody(JSONArray.class, stream -> new JSONArray(getTokenizer(stream)));
    }

    @Nullable
    public JSONObject getObject()
    {
        return parseBody(JSONObject.class, stream -> new JSONObject(getTokenizer(stream)));
    }

    @Nullable
    public String getString()
    {
        return parseBody(String.class, stream -> new BufferedReader(new InputStreamReader(stream)).lines().collect(Collectors.joining()));
    }

    @Nullable
    public <T> T get(Class<T> clazz, Function<InputStream, T> parser)
    {
        return parseBody(clazz, parser);
    }

    public okhttp3.Response getRawResponse()
    {
        return this.rawResponse;
    }

    public Set<String> getCFRays()
    {
        return cfRays;
    }

    public Exception getException()
    {
        return exception;
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

    private JSONTokener getTokenizer(InputStream stream)
    {
        return new JSONTokener(new InputStreamReader(stream));
    }

    @Nullable
    @SuppressWarnings("ConstantConditions")
    private <T> T parseBody(Class<T> clazz, Function<InputStream, T> parser)
    {
        if (attemptedParsing)
        {
            if (object != null && clazz.isAssignableFrom(object.getClass()))
                return clazz.cast(object);
            return null;
        }

        attemptedParsing = true;
        if (body == null || rawResponse == null || rawResponse.body().contentLength() == 0)
            return null;

        try
        {
            T t = parser.apply(body);
            this.object = t;
            return t;
        }
        catch (final Exception e)
        {
            throw new IllegalStateException("An error occurred while parsing the response for a RestAction", e);
        }
        finally
        {
            try
            {
                body.close();
            }
            catch (NullPointerException | IOException ignored) {}
        }
    }
}
