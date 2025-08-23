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

package net.dv8tion.jda.api.requests;

import net.dv8tion.jda.api.exceptions.ParsingException;
import net.dv8tion.jda.api.utils.IOFunction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.utils.EntityString;
import net.dv8tion.jda.internal.utils.IOUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Internal class used to represent HTTP responses or request failures.
 */
public class Response implements Closeable
{
    public static final int ERROR_CODE = -1;
    public static final String ERROR_MESSAGE = "ERROR";
    public static final IOFunction<BufferedReader, DataObject> JSON_SERIALIZE_OBJECT = DataObject::fromJson;
    public static final IOFunction<BufferedReader, DataArray> JSON_SERIALIZE_ARRAY = DataArray::fromJson;

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

    public Response(@Nonnull final Exception exception, @Nonnull final Set<String> cfRays)
    {
        this(null, ERROR_CODE, ERROR_MESSAGE, -1, cfRays);
        this.exception = exception;
    }

    public Response(@Nullable final okhttp3.Response response, final int code, @Nonnull final String message, final long retryAfter, @Nonnull final Set<String> cfRays)
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
            this.body = IOUtil.getBody(response);
        }
        catch (final Exception e)
        {
            throw new IllegalStateException("An error occurred while parsing the response for a RestAction", e);
        }
    }

    public Response(final long retryAfter, @Nonnull final Set<String> cfRays)
    {
        this(null, 429, "TOO MANY REQUESTS", retryAfter, cfRays);
    }

    public Response(@Nonnull final okhttp3.Response response, final long retryAfter, @Nonnull final Set<String> cfRays)
    {
        this(response, response.code(), response.message(), retryAfter, cfRays);
    }

    @Nonnull
    public DataArray getArray()
    {
        return get(DataArray.class, JSON_SERIALIZE_ARRAY);
    }

    @Nonnull
    public Optional<DataArray> optArray()
    {
        return parseBody(true, DataArray.class, JSON_SERIALIZE_ARRAY);
    }

    @Nonnull
    public DataObject getObject()
    {
        return get(DataObject.class, JSON_SERIALIZE_OBJECT);
    }

    @Nonnull
    public Optional<DataObject> optObject()
    {
        return parseBody(true, DataObject.class, JSON_SERIALIZE_OBJECT);
    }

    @Nonnull
    public String getString()
    {
        return parseBody(String.class, this::readString)
            .orElseGet(() -> fallbackString == null ? "N/A" : fallbackString);
    }

    @Nonnull
    public <T> T get(@Nonnull Class<T> clazz, @Nonnull IOFunction<BufferedReader, T> parser)
    {
        return parseBody(clazz, parser).orElseThrow(IllegalStateException::new);
    }

    @Nullable
    public okhttp3.Response getRawResponse()
    {
        return this.rawResponse;
    }

    @Nonnull
    public Set<String> getCFRays()
    {
        return cfRays;
    }

    @Nullable
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
        final EntityString entityString = new EntityString(exception == null ? "HTTPResponse" : "HTTPException");
        if (exception == null) {
            entityString.addMetadata("code", code);
            if (object != null)
                entityString.addMetadata("object", object.toString());
        } else {
            entityString.addMetadata("exceptionMessage", exception.getMessage());
        }

        return entityString.toString();
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
            RestActionImpl.LOG.trace("Parsed response body for response on url {}\n{}", rawResponse.request().url(), this.object);
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
            if (opt && e instanceof ParsingException)
                return Optional.empty();
            else
                throw new IllegalStateException("An error occurred while parsing the response for a RestAction", e);
        }
    }
}
