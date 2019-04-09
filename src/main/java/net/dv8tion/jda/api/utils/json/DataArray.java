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
import com.fasterxml.jackson.databind.type.CollectionType;
import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class DataArray
{
    private static final Logger log = LoggerFactory.getLogger(DataObject.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final CollectionType listType = mapper.getTypeFactory().constructCollectionType(List.class, Object.class);

    protected final List<Object> data;

    protected DataArray(List<Object> data)
    {
        this.data = data;
    }

    public static DataArray empty()
    {
        return new DataArray(new ArrayList<>());
    }

    public static DataArray fromJson(String json)
    {
        try
        {
            return new DataArray(mapper.readValue(json, listType));
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    public int length()
    {
        return data.size();
    }

    public boolean isEmpty()
    {
        return data.isEmpty();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public DataObject getObject(int index)
    {
        Map<String, Object> child = null;
        try
        {
            child = (Map<String, Object>) get(Map.class, index);
        }
        catch (ClassCastException ex)
        {
            log.error("Unable to extract child data", ex);
        }
        return child != null ? new DataObject(child) : null;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public DataArray getArray(int index)
    {
        List<Object> child = null;
        try
        {
            child = (List<Object>) get(Map.class, index);
        }
        catch (ClassCastException ex)
        {
            log.error("Unable to extract child data", ex);
        }
        return child != null ? new DataArray(child) : null;
    }

    @Contract("_, null -> null; _, !null -> !null")
    public String getString(int index, @Nullable String defaultValue)
    {
        String value = get(String.class, index, UnaryOperator.identity(), String::valueOf);
        return value == null ? defaultValue : value;
    }

    public boolean getBoolean(int index)
    {
        return getBoolean(index, false);
    }

    public boolean getBoolean(int index, boolean defaultValue)
    {
        Boolean value = get(Boolean.class, index, Boolean::parseBoolean, null);
        return value == null ? defaultValue : value;
    }

    public int getInt(int index, int defaultValue)
    {
        Integer value = get(Integer.class, index, Integer::parseInt, Number::intValue);
        return value == null ? defaultValue : value;
    }

    public int getUnsignedInt(int index, int defaultValue)
    {
        Integer value = get(Integer.class, index, Integer::parseUnsignedInt, Number::intValue);
        return value == null ? defaultValue : value;
    }

    public long getLong(int index, long defaultValue)
    {
        Long value = get(Long.class, index, Long::parseLong, Number::longValue);
        return value == null ? defaultValue : value;
    }

    public long getUnsignedLong(int index, long defaultValue)
    {
        Long value = get(Long.class, index, Long::parseUnsignedLong, Number::longValue);
        return value == null ? defaultValue : value;
    }

    @Nonnull
    public DataArray add(@Nullable Object value)
    {
        data.add(value);
        return this;
    }

    @Nonnull
    public DataArray add(@Nonnull DataArray array)
    {
        return add(array.data);
    }

    @Nonnull
    public DataArray add(@Nonnull DataObject map)
    {
        return add(map.data);
    }

    @Nonnull
    public DataArray addAll(@Nonnull Collection<?> values)
    {
        values.forEach(this::add);
        return this;
    }

    @Nonnull
    public DataArray addAll(@Nonnull DataArray array)
    {
        return addAll(array.data);
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
    public List<Object> toList()
    {
        return data;
    }

    @Nullable
    private <T> T get(@Nonnull Class<T> type, int index)
    {
        return get(type, index, null, null);
    }

    @Nullable
    private <T> T get(@Nonnull Class<T> type, int index, @Nullable Function<String, T> stringMapper, @Nullable Function<Number, T> numberMapper)
    {
        Object value = data.get(index);
        if (value == null)
            return null;
        if (type.isAssignableFrom(value.getClass()))
            return type.cast(value);
        // attempt type coercion
        if (stringMapper != null && value instanceof String)
            return stringMapper.apply((String) value);
        else if (numberMapper != null && value instanceof Number)
            return numberMapper.apply((Number) value);

        throw new IllegalStateException(String.format("Cannot parse value for index %d into type %s: %s instance of %s",
                                                      index, type.getSimpleName(), value, value.getClass().getSimpleName()));
    }
}
