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

import net.dv8tion.jda.core.utils.IOFunction;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Response implements Closeable
{
    public static final int ERROR_CODE = -1;
    public static final String ERROR_MESSAGE = "ERROR";
    public static final IOFunction<BufferedReader, JSONObject> JSON_SERIALIZE_OBJECT = (reader) -> new JSONObject(new JSONTokener(reader));
    public static final IOFunction<BufferedReader, JSONArray> JSON_SERIALIZE_ARRAY = (reader) -> new JSONArray(new JSONTokener(reader));

    public final int code;
    public final String message;
    public final long retryAfter;
    private final InputStream body;
    private final okhttp3.Response rawResponse;
    private final Set<String> cfRays;
    private String fallbackString;
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
        }
        else // weird compatibility issue, thinks some final isn't initialized if we return pre-maturely
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

    public JSONArray getArray()
    {
        return get(JSONArray.class, JSON_SERIALIZE_ARRAY);
    }

    public Optional<JSONArray> optArray()
    {
        return parseBody(true, JSONArray.class, JSON_SERIALIZE_ARRAY);
    }

    public JSONObject getObject()
    {
        return get(JSONObject.class, JSON_SERIALIZE_OBJECT);
    }

    public Optional<JSONObject> optObject()
    {
        return parseBody(true, JSONObject.class, JSON_SERIALIZE_OBJECT);
    }

    public String getString()
    {
        return parseBody(String.class, this::readString)
            .orElseGet(() -> fallbackString == null ? "N/A" : fallbackString);
    }

    public <T> T get(Class<T> clazz, IOFunction<BufferedReader, T> parser)
    {
        return parseBody(clazz, parser).orElseThrow(IllegalStateException::new);
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

    private String readString(BufferedReader reader)
    {
        return reader.lines().collect(Collectors.joining("\n"));
    }

    private <T> Optional<T> parseBody(Class<T> clazz, IOFunction<BufferedReader, T> parser)
    {
        return parseBody(false, clazz, parser);
    }

    @SuppressWarnings("ConstantConditions")
    private <T> Optional<T> parseBody(boolean opt, Class<T> clazz, IOFunction<BufferedReader, T> parser)
    {
        if (attemptedParsing)
        {
            if (object != null && clazz.isAssignableFrom(object.getClass()))
                return Optional.of(clazz.cast(object));
            return Optional.empty();
        }

        attemptedParsing = true;
        if (body == null || rawResponse == null || rawResponse.body().contentLength() == 0)
            return Optional.empty();

        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new InputStreamReader(body));
            reader.mark(1024);
            T t = parser.apply(reader);
            this.object = t;
            return Optional.ofNullable(t);
        }
        catch (final Exception e)
        {
            try
            {
                reader.reset();
                this.fallbackString = readString(reader);
                reader.close();
            }
            catch (NullPointerException | IOException ignored) {}
            if (opt && e instanceof JSONException)
                return Optional.empty();
            else
                throw new IllegalStateException("An error occurred while parsing the response for a RestAction", e);
        }
    }
}
