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
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class DataObject
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

    public boolean hasKey(@Nonnull String key)
    {
        return data.containsKey(key);
    }

    public boolean isNull(@Nonnull String key)
    {
        return data.get(key) == null;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public DataObject getObject(@Nonnull String key)
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
        return child != null ? new DataObject(child) : null;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public DataArray getArray(@Nonnull String key)
    {
        List<Object> child = null;
        try
        {
            child = (List<Object>) get(Map.class, key);
        }
        catch (ClassCastException ex)
        {
            log.error("Unable to extract child data", ex);
        }
        return child != null ? new DataArray(child) : null;
    }

    @Contract("_, null -> null; _, !null -> !null")
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

    public long getLong(@Nonnull String key, long defaultValue)
    {
        Long value = get(Long.class, key, Long::parseLong, Number::longValue);
        return value == null ? defaultValue : value;
    }

    public long getUnsignedLong(@Nonnull String key, long defaultValue)
    {
        Long value = get(Long.class, key, Long::parseUnsignedLong, Number::longValue);
        return value == null ? defaultValue : value;
    }

    public int getInt(@Nonnull String key, int defaultValue)
    {
        Integer value = get(Integer.class, key, Integer::parseInt, Number::intValue);
        return value == null ? defaultValue : value;
    }

    public int getUnsignedInt(@Nonnull String key, int defaultValue)
    {
        Integer value = get(Integer.class, key, Integer::parseUnsignedInt, Number::intValue);
        return value == null ? defaultValue : value;
    }

    @Nonnull
    public DataObject put(@Nonnull String key, @Nullable Object value)
    {
        data.put(key, value);
        return this;
    }

    @Nonnull
    public DataObject put(@Nonnull String key, @Nonnull DataObject map)
    {
        return put(key, map.data);
    }

    @Nonnull
    public DataObject put(@Nonnull String key, @Nonnull DataArray list)
    {
        return put(key, list.data);
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
            throw new IllegalStateException(e);
        }
    }

    @Nonnull
    public Map<String, Object> toMap()
    {
        return data;
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
