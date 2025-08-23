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

package net.dv8tion.jda.api.utils.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.CollectionType;
import net.dv8tion.jda.api.exceptions.ParsingException;
import net.dv8tion.jda.api.utils.data.etf.ExTermDecoder;
import net.dv8tion.jda.api.utils.data.etf.ExTermEncoder;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;
import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.ByteBuffer;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Represents a list of values used in communication with the Discord API.
 *
 * <p>Throws {@link java.lang.IndexOutOfBoundsException}
 * if provided with index out of bounds.
 *
 * <p>This class is not Thread-Safe
 */
public class DataArray implements Iterable<Object>, SerializableArray
{
    private static final Logger log = LoggerFactory.getLogger(DataObject.class);
    private static final ObjectMapper mapper;
    private static final SimpleModule module;
    private static final CollectionType listType;

    static
    {
        mapper = new ObjectMapper();
        module = new SimpleModule();
        module.addAbstractTypeMapping(Map.class, HashMap.class);
        module.addAbstractTypeMapping(List.class, ArrayList.class);
        mapper.registerModule(module);
        listType = mapper.getTypeFactory().constructRawCollectionType(ArrayList.class);
    }

    protected final List<Object> data;

    protected DataArray(List<Object> data)
    {
        this.data = data;
    }

    /**
     * Creates a new empty DataArray, ready to be populated with values.
     *
     * @return An empty DataArray instance
     *
     * @see    #add(Object)
     */
    @Nonnull
    public static DataArray empty()
    {
        return new DataArray(new ArrayList<>());
    }

    /**
     * Creates a new DataArray and populates it with the contents
     * of the provided collection.
     *
     * @param  col
     *         The {@link java.util.Collection}
     *
     * @return A new DataArray populated with the contents of the collection
     */
    @Nonnull
    public static DataArray fromCollection(@Nonnull Collection<?> col)
    {
        return empty().addAll(col);
    }

    /**
     * Parses a JSON Array into a DataArray instance.
     *
     * @param  json
     *         The correctly formatted JSON Array
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the provided JSON is incorrectly formatted
     *
     * @return A new DataArray instance for the provided array
     */
    @Nonnull
    public static DataArray fromJson(@Nonnull String json)
    {
        try
        {
            return new DataArray(mapper.readValue(json, listType));
        }
        catch (IOException e)
        {
            throw new ParsingException(e);
        }
    }

    /**
     * Parses a JSON Array into a DataArray instance.
     *
     * @param  json
     *         The correctly formatted JSON Array
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the provided JSON is incorrectly formatted or an I/O error occurred
     *
     * @return A new DataArray instance for the provided array
     */
    @Nonnull
    public static DataArray fromJson(@Nonnull InputStream json)
    {
        try
        {
            return new DataArray(mapper.readValue(json, listType));
        }
        catch (IOException e)
        {
            throw new ParsingException(e);
        }
    }

    /**
     * Parses a JSON Array into a DataArray instance.
     *
     * @param  json
     *         The correctly formatted JSON Array
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the provided JSON is incorrectly formatted or an I/O error occurred
     *
     * @return A new DataArray instance for the provided array
     */
    @Nonnull
    public static DataArray fromJson(@Nonnull Reader json)
    {
        try
        {
            return new DataArray(mapper.readValue(json, listType));
        }
        catch (IOException e)
        {
            throw new ParsingException(e);
        }
    }

    /**
     * Parses using {@link ExTermDecoder}.
     * The provided data must start with the correct version header (131).
     *
     * @param  data
     *         The data to decode
     *
     * @throws IllegalArgumentException
     *         If the provided data is null
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the provided ETF payload is incorrectly formatted or an I/O error occurred
     *
     * @return A DataArray instance for the provided payload
     *
     * @since  4.2.1
     */
    @Nonnull
    public static DataArray fromETF(@Nonnull byte[] data)
    {
        Checks.notNull(data, "Data");
        try
        {
            List<Object> list = ExTermDecoder.unpackList(ByteBuffer.wrap(data));
            return new DataArray(list);
        }
        catch (Exception ex)
        {
            log.error("Failed to parse ETF data {}", Arrays.toString(data), ex);
            throw new ParsingException(ex);
        }
    }

    /**
     * Whether the value at the specified index is null.
     *
     * @param  index
     *         The index to check
     *
     * @return True, if the value at the index is null
     */
    public boolean isNull(int index)
    {
        return index >= length() || data.get(index) == null;
    }

    /**
     * Whether the value at the specified index is of the specified type.
     *
     * @param  index
     *         The index to check
     * @param  type
     *         The type to check
     *
     * @return True, if the type check is successful
     *
     * @see    net.dv8tion.jda.api.utils.data.DataType#isType(Object) DataType.isType(Object)
     */
    public boolean isType(int index, @Nonnull DataType type)
    {
        return type.isType(data.get(index));
    }

    /**
     * The length of the array.
     *
     * @return The length of the array
     */
    public int length()
    {
        return data.size();
    }

    /**
     * Whether this array is empty
     *
     * @return True, if this array is empty
     */
    public boolean isEmpty()
    {
        return data.isEmpty();
    }

    /**
     * Resolves the value at the specified index to a DataObject
     *
     * @param  index
     *         The index to resolve
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is of the wrong type or missing
     *
     * @return The resolved DataObject
     */
    @Nonnull
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
        if (child == null)
            throw valueError(index, "DataObject");
        return new DataObject(child);
    }

    /**
     * Resolves the value at the specified index to a DataArray
     *
     * @param  index
     *         The index to resolve
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is of the wrong type or null
     *
     * @return The resolved DataArray
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public DataArray getArray(int index)
    {
        List<Object> child = null;
        try
        {
            child = (List<Object>) get(List.class, index);
        }
        catch (ClassCastException ex)
        {
            log.error("Unable to extract child data", ex);
        }
        if (child == null)
            throw valueError(index, "DataArray");
        return new DataArray(child);
    }

    /**
     * Resolves the value at the specified index to a String.
     *
     * @param  index
     *         The index to resolve
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is of the wrong type or null
     *
     * @return The resolved String
     */
    @Nonnull
    public String getString(int index)
    {
        String value = get(String.class, index, UnaryOperator.identity(), String::valueOf);
        if (value == null)
            throw valueError(index, "String");
        return value;
    }

    /**
     * Resolves the value at the specified index to a String.
     *
     * @param  index
     *         The index to resolve
     * @param  defaultValue
     *         Alternative value to use when the value associated with the index is null
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is of the wrong type
     *
     * @return The resolved String
     */
    @Contract("_, !null -> !null")
    public String getString(int index, @Nullable String defaultValue)
    {
        String value = get(String.class, index, UnaryOperator.identity(), String::valueOf);
        return value == null ? defaultValue : value;
    }

    /**
     * Resolves the value at the specified index to a boolean.
     *
     * @param  index
     *         The index to resolve
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is of the wrong type
     *
     * @return True, if the value is present and set to true. Otherwise false.
     */
    public boolean getBoolean(int index)
    {
        return getBoolean(index, false);
    }

    /**
     * Resolves the value at the specified index to a boolean.
     *
     * @param  index
     *         The index to resolve
     * @param  defaultValue
     *         Alternative value to use when the value associated with the index is null
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is of the wrong type
     *
     * @return True, if the value is present and set to true. False, if it is set to false. Otherwise defaultValue.
     */
    public boolean getBoolean(int index, boolean defaultValue)
    {
        Boolean value = get(Boolean.class, index, Boolean::parseBoolean, null);
        return value == null ? defaultValue : value;
    }

    /**
     * Resolves the value at the specified index to an int.
     *
     * @param  index
     *         The index to resolve
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is of the wrong type
     *
     * @return The resolved int value
     */
    public int getInt(int index)
    {
        Integer value = get(Integer.class, index, Integer::parseInt, Number::intValue);
        if (value == null)
            throw valueError(index, "int");
        return value;
    }

    /**
     * Resolves the value at the specified index to an int.
     *
     * @param  index
     *         The index to resolve
     * @param  defaultValue
     *         Alternative value to use when the value associated with the index is null
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is of the wrong type
     *
     * @return The resolved int value
     */
    public int getInt(int index, int defaultValue)
    {
        Integer value = get(Integer.class, index, Integer::parseInt, Number::intValue);
        return value == null ? defaultValue : value;
    }

    /**
     * Resolves the value at the specified index to an unsigned int.
     *
     * @param  index
     *         The index to resolve
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is of the wrong type
     *
     * @return The resolved unsigned int value
     */
    public int getUnsignedInt(int index)
    {
        Integer value = get(Integer.class, index, Integer::parseUnsignedInt, Number::intValue);
        if (value == null)
            throw valueError(index, "unsigned int");
        return value;
    }

    /**
     * Resolves the value at the specified index to an unsigned int.
     *
     * @param  index
     *         The index to resolve
     * @param  defaultValue
     *         Alternative value to use when the value associated with the index is null
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is of the wrong type
     *
     * @return The resolved unsigned int value
     */
    public int getUnsignedInt(int index, int defaultValue)
    {
        Integer value = get(Integer.class, index, Integer::parseUnsignedInt, Number::intValue);
        return value == null ? defaultValue : value;
    }

    /**
     * Resolves the value at the specified index to a long.
     *
     * @param  index
     *         The index to resolve
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is of the wrong type
     *
     * @return The resolved long value
     */
    public long getLong(int index)
    {
        Long value = get(Long.class, index, Long::parseLong, Number::longValue);
        if (value == null)
            throw valueError(index, "long");
        return value;
    }

    /**
     * Resolves the value at the specified index to a long.
     *
     * @param  index
     *         The index to resolve
     * @param  defaultValue
     *         Alternative value to use when the value associated with the index is null
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is of the wrong type
     *
     * @return The resolved long value
     */
    public long getLong(int index, long defaultValue)
    {
        Long value = get(Long.class, index, Long::parseLong, Number::longValue);
        return value == null ? defaultValue : value;
    }

    /**
     * Resolves the value at the specified index to an unsigned long.
     *
     * @param  index
     *         The index to resolve
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is of the wrong type
     *
     * @return The resolved unsigned long value
     */
    public long getUnsignedLong(int index)
    {
        Long value = get(Long.class, index, Long::parseUnsignedLong, Number::longValue);
        if (value == null)
            throw valueError(index, "unsigned long");
        return value;
    }

    /**
     * Resolves the value at the specified index to an {@link OffsetDateTime}.
     * <br><b>Note:</b> This method should be used on ISO8601 timestamps
     *
     * @param  index
     *         The index to resolve
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is missing, null, or not a valid ISO8601 timestamp
     *
     * @return Possibly-null {@link OffsetDateTime} object representing the timestamp
     */
    @Nonnull
    public OffsetDateTime getOffsetDateTime(int index)
    {
        OffsetDateTime value = getOffsetDateTime(index, null);
        if(value == null)
            throw valueError(index, "OffsetDateTime");
        return value;
    }
    /**
     * Resolves the value at the specified index to an {@link OffsetDateTime}.
     * <br><b>Note:</b> This method should only be used on ISO8601 timestamps
     *
     * @param  index
     *         The index to resolve
     * @param  defaultValue
     *         Alternative value to use when no value or null value is associated with the key
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is not a valid ISO8601 timestamp
     *
     * @return Possibly-null {@link OffsetDateTime} object representing the timestamp
     */
    @Contract("_, !null -> !null")
    public OffsetDateTime getOffsetDateTime(int index, @Nullable OffsetDateTime defaultValue)
    {
        OffsetDateTime value;
        try
        {
            value = get(OffsetDateTime.class, index, OffsetDateTime::parse, null);
        }
        catch (DateTimeParseException e)
        {
            String reason = "Cannot parse value for index %d into an OffsetDateTime object. Try double checking that %s is a valid ISO8601 timestamp";
            throw new ParsingException(String.format(reason, index, e.getParsedString()));
        }
        return value == null ? defaultValue : value;
    }

    /**
     * Resolves the value at the specified index to an unsigned long.
     *
     * @param  index
     *         The index to resolve
     * @param  defaultValue
     *         Alternative value to use when the value associated with the index is null
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is of the wrong type
     *
     * @return The resolved unsigned long value
     */
    public long getUnsignedLong(int index, long defaultValue)
    {
        Long value = get(Long.class, index, Long::parseUnsignedLong, Number::longValue);
        return value == null ? defaultValue : value;
    }

    /**
     * Resolves the value at the specified index to a double.
     *
     * @param  index
     *         The index to resolve
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is of the wrong type
     *
     * @return The resolved double value
     */
    public double getDouble(int index)
    {
        Double value = get(Double.class, index, Double::parseDouble, Number::doubleValue);
        if (value == null)
            throw valueError(index, "double");
        return value;
    }

    /**
     * Resolves the value at the specified index to a double.
     *
     * @param  index
     *         The index to resolve
     * @param  defaultValue
     *         Alternative value to use when the value associated with the index is null
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is of the wrong type
     *
     * @return The resolved double value
     */
    public double getDouble(int index, double defaultValue)
    {
        Double value = get(Double.class, index, Double::parseDouble, Number::doubleValue);
        return value == null ? defaultValue : value;
    }

    /**
     * Appends the provided value to the end of the array.
     *
     * @param  value
     *         The value to append
     *
     * @return A DataArray with the value inserted at the end
     */
    @Nonnull
    public DataArray add(@Nullable Object value)
    {
        if (value instanceof SerializableData)
            data.add(((SerializableData) value).toData().data);
        else if (value instanceof SerializableArray)
            data.add(((SerializableArray) value).toDataArray().data);
        else
            data.add(value);
        return this;
    }

    /**
     * Appends the provided values to the end of the array.
     *
     * @param  values
     *         The values to append
     *
     * @return A DataArray with the values inserted at the end
     */
    @Nonnull
    public DataArray addAll(@Nonnull Collection<?> values)
    {
        values.forEach(this::add);
        return this;
    }

    /**
     * Appends the provided values to the end of the array.
     *
     * @param  array
     *         The values to append
     *
     * @return A DataArray with the values inserted at the end
     */
    @Nonnull
    public DataArray addAll(@Nonnull DataArray array)
    {
        return addAll(array.data);
    }

    /**
     * Inserts the specified value at the provided index.
     *
     * @param  index
     *         The target index
     * @param  value
     *         The value to insert
     *
     * @return A DataArray with the value inserted at the specified index
     */
    @Nonnull
    public DataArray insert(int index, @Nullable Object value)
    {
        if (value instanceof SerializableData)
            data.add(index, ((SerializableData) value).toData().data);
        else if (value instanceof SerializableArray)
            data.add(index, ((SerializableArray) value).toDataArray().data);
        else
            data.add(index, value);
        return this;
    }

    /**
     * Removes the value at the specified index.
     *
     * @param  index
     *         The target index to remove
     *
     * @return A DataArray with the value removed
     */
    @Nonnull
    public DataArray remove(int index)
    {
        data.remove(index);
        return this;
    }

    /**
     * Removes the specified value.
     *
     * @param  value
     *         The value to remove
     *
     * @return A DataArray with the value removed
     */
    @Nonnull
    public DataArray remove(@Nullable Object value)
    {
        data.remove(value);
        return this;
    }

    /**
     * Serializes this object as JSON.
     *
     * @return byte array containing the JSON representation of this object
     */
    @Nonnull
    public byte[] toJson()
    {
        try
        {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            mapper.writeValue(outputStream, data);
            return outputStream.toByteArray();
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Serializes this object as ETF LIST term.
     *
     * @return byte array containing the encoded ETF term
     *
     * @since  4.2.1
     */
    @Nonnull
    public byte[] toETF()
    {
        ByteBuffer buffer = ExTermEncoder.pack(data);
        return Arrays.copyOfRange(buffer.array(), buffer.arrayOffset(), buffer.arrayOffset() + buffer.limit());
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
            throw new ParsingException(e);
        }
    }

    @Nonnull
    public String toPrettyString()
    {
        try
        {
            return mapper.writer(new DefaultPrettyPrinter())
                    .with(SerializationFeature.INDENT_OUTPUT)
                    .with(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                    .writeValueAsString(data);
        }
        catch (JsonProcessingException e)
        {
            throw new ParsingException(e);
        }
    }

    /**
     * Converts this DataArray to a {@link java.util.List}.
     *
     * @return The resulting list
     */
    @Nonnull
    public List<Object> toList()
    {
        return data;
    }

    private ParsingException valueError(int index, String expectedType)
    {
        return new ParsingException("Unable to resolve value at " + index + " to type " + expectedType + ": " + data.get(index));
    }

    @Nullable
    private <T> T get(@Nonnull Class<T> type, int index)
    {
        return get(type, index, null, null);
    }

    @Nullable
    private <T> T get(@Nonnull Class<T> type, int index, @Nullable Function<String, T> stringMapper, @Nullable Function<Number, T> numberMapper)
    {
        if (index < 0)
            throw new IndexOutOfBoundsException("Index out of range: " + index);
        Object value = index < data.size() ? data.get(index) : null;
        if (value == null)
            return null;
        if (type.isInstance(value))
            return type.cast(value);
        if (type == String.class)
            return type.cast(value.toString());
        // attempt type coercion
        if (stringMapper != null && value instanceof String)
            return stringMapper.apply((String) value);
        else if (numberMapper != null && value instanceof Number)
            return numberMapper.apply((Number) value);

        throw new ParsingException(Helpers.format("Cannot parse value for index %d into type %s: %s instance of %s",
                                                      index, type.getSimpleName(), value, value.getClass().getSimpleName()));
    }

    @Nonnull
    @Override
    public Iterator<Object> iterator()
    {
        return data.iterator();
    }

    @Nonnull
    public <T> Stream<T> stream(@Nonnull BiFunction<? super DataArray, Integer, ? extends T> mapper)
    {
        return IntStream.range(0, length())
                .mapToObj(index -> mapper.apply(this, index));
    }

    @Nonnull
    @Override
    public DataArray toDataArray()
    {
        return this;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof DataArray))
            return false;
        DataArray objects = (DataArray) o;
        return Objects.equals(data, objects.data);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(data);
    }
}
