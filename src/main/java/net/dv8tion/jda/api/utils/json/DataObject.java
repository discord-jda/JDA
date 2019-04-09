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

package net.dv8tion.jda.api.utils.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class DataObject implements SerializableData
{
    private static final Logger log = LoggerFactory.getLogger(DataObject.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final MapType mapType = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);

    protected final Map<String, Object> data;

    protected DataObject(@Nonnull Map<String, Object> data)
    {
        this.data = data;
    }

    @Nonnull
    public static DataObject empty()
    {
        return new DataObject(new HashMap<>());
    }

    @Nonnull
    public static DataObject fromJson(@Nonnull String json)
    {
        try
        {
            Map<String, Object> map = mapper.readValue(json, mapType);
            return new DataObject(map);
        }
        catch (IOException ex)
        {
            throw new UncheckedIOException(ex);
        }
    }

    @Nonnull
    public static DataObject fromJson(@Nonnull InputStream stream)
    {
        try
        {
            Map<String, Object> map = mapper.readValue(stream, mapType);
            return new DataObject(map);
        }
        catch (IOException ex)
        {
            throw new UncheckedIOException(ex);
        }
    }


    @Nonnull
    public static DataObject fromJson(@Nonnull Reader stream)
    {
        try
        {
            Map<String, Object> map = mapper.readValue(stream, mapType);
            return new DataObject(map);
        }
        catch (IOException ex)
        {
            throw new UncheckedIOException(ex);
        }
    }

    public boolean hasKey(@Nonnull String key)
    {
        return data.containsKey(key);
    }

    public boolean isNull(@Nonnull String key)
    {
        return data.get(key) == null;
    }

    public boolean isArray(@Nonnull String key)
    {
        return optArray(key).isPresent();
    }

    public boolean isObject(@Nonnull String key)
    {
        return optObject(key).isPresent();
    }

    @Nonnull
    public DataObject getObject(@Nonnull String key)
    {
        return optObject(key).orElseThrow(() -> valueError(key, "DataObject"));
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public Optional<DataObject> optObject(@Nonnull String key)
    {
        Map<String, Object> child = null;
        try
        {
            child = (Map<String, Object>) get(Map.class, key);
        }
        catch (ClassCastException ex)
        {
            log.error("Unable to extract child data", ex);
        }
        return child == null ? Optional.empty() : Optional.of(new DataObject(child));
    }

    @Nonnull
    public DataArray getArray(@Nonnull String key)
    {
        return optArray(key).orElseThrow(() -> valueError(key, "DataArray"));
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public Optional<DataArray> optArray(@Nonnull String key)
    {
        List<Object> child = null;
        try
        {
            child = (List<Object>) get(List.class, key);
        }
        catch (ClassCastException ex)
        {
            log.error("Unable to extract child data", ex);
        }
        return child == null ? Optional.empty() : Optional.of(new DataArray(child));
    }

    @Nonnull
    public Optional<Object> opt(@Nonnull String key)
    {
        return Optional.ofNullable(data.get(key));
    }

    @Nonnull
    public Object get(@Nonnull String key)
    {
        Object value = data.get(key);
        if (value == null)
            throw valueError(key, "any");
        return value;
    }

    @Nonnull
    public String getString(@Nonnull String key)
    {
        String value = getString(key, null);
        if (value == null)
            throw valueError(key, "String");
        return value;
    }

    @Contract("_, !null -> !null")
    public String getString(@Nonnull String key, @Nullable String defaultValue)
    {
        String value = get(String.class, key, UnaryOperator.identity(), String::valueOf);
        return value == null ? defaultValue : value;
    }

    public boolean getBoolean(@Nonnull String key)
    {
        return getBoolean(key, false);
    }

    public boolean getBoolean(@Nonnull String key, boolean defaultValue)
    {
        Boolean value = get(Boolean.class, key, Boolean::parseBoolean, null);
        return value == null ? defaultValue : value;
    }

    public long getLong(@Nonnull String key)
    {
        Long value = get(Long.class, key, Long::parseLong, Number::longValue);
        if (value == null)
            throw valueError(key, "long");
        return value;
    }

    public long getLong(@Nonnull String key, long defaultValue)
    {
        Long value = get(Long.class, key, Long::parseLong, Number::longValue);
        return value == null ? defaultValue : value;
    }

    public long getUnsignedLong(@Nonnull String key)
    {
        Long value = get(Long.class, key, Long::parseUnsignedLong, Number::longValue);
        if (value == null)
            throw valueError(key, "unsigned long");
        return value;
    }

    public long getUnsignedLong(@Nonnull String key, long defaultValue)
    {
        Long value = get(Long.class, key, Long::parseUnsignedLong, Number::longValue);
        return value == null ? defaultValue : value;
    }

    public int getInt(@Nonnull String key)
    {
        Integer value = get(Integer.class, key, Integer::parseInt, Number::intValue);
        if (value == null)
            throw valueError(key, "int");
        return value;
    }

    public int getInt(@Nonnull String key, int defaultValue)
    {
        Integer value = get(Integer.class, key, Integer::parseInt, Number::intValue);
        return value == null ? defaultValue : value;
    }

    public int getUnsignedInt(@Nonnull String key)
    {
        Integer value = get(Integer.class, key, Integer::parseUnsignedInt, Number::intValue);
        if (value == null)
            throw valueError(key, "unsigned int");
        return value;
    }

    public int getUnsignedInt(@Nonnull String key, int defaultValue)
    {
        Integer value = get(Integer.class, key, Integer::parseUnsignedInt, Number::intValue);
        return value == null ? defaultValue : value;
    }

    @Nonnull
    public DataObject putNull(@Nonnull String key)
    {
        data.put(key, null);
        return this;
    }

    @Nonnull
    public DataObject put(@Nonnull String key, @Nullable Object value)
    {
        if (value instanceof SerializableData)
            data.put(key, ((SerializableData) value).toData().data);
        else if (value instanceof DataArray)
            data.put(key, ((DataArray) value).data);
        else
            data.put(key, value);
        return this;
    }

    @Nonnull
    public Collection<Object> values()
    {
        return data.values();
    }

    @Nonnull
    public Set<String> keys()
    {
        return data.keySet();
    }

    @Override
    public String toString()
    {
        try
        {
            return mapper.writeValueAsString(data);
        }
        catch (JsonProcessingException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    @Nonnull
    public Map<String, Object> toMap()
    {
        return data;
    }

    @Override
    public DataObject toData()
    {
        return this;
    }

    private IllegalStateException valueError(String key, String expectedType)
    {
        return new IllegalStateException("Unable to access " + key + " with type " + expectedType + ": " + data.get(key));
    }

    @Nullable
    private <T> T get(@Nonnull Class<T> type, @Nonnull String key)
    {
        return get(type, key, null, null);
    }

    @Nullable
    private <T> T get(@Nonnull Class<T> type, @Nonnull String key, @Nullable Function<String, T> stringParse, @Nullable Function<Number, T> numberParse)
    {
        Object value = data.get(key);
        if (value == null)
            return null;
        if (type.isAssignableFrom(value.getClass()))
            return type.cast(value);
        // attempt type coercion
        if (value instanceof Number && numberParse != null)
            return numberParse.apply((Number) value);
        else if (value instanceof String && stringParse != null)
            return stringParse.apply((String) value);

        throw new IllegalStateException(String.format("Cannot parse value for %s into type %s: %s instance of %s",
                                                      key, type.getSimpleName(), value, value.getClass().getSimpleName()));
    }
}
