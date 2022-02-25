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
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.MapType;
import net.dv8tion.jda.api.exceptions.ParsingException;
import net.dv8tion.jda.api.utils.MiscUtil;
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
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Represents a map of values used in communication with the Discord API.
 *
 * <p>Throws {@link java.lang.NullPointerException},
 * if a parameter annotated with {@link javax.annotation.Nonnull} is provided with {@code null}.
 *
 * <p>This class is not Thread-Safe.
 */
public class DataObject implements SerializableData
{
    private static final Logger log = LoggerFactory.getLogger(DataObject.class);
    private static final ObjectMapper mapper;
    private static final SimpleModule module;
    private static final MapType mapType;

    static
    {
        mapper = new ObjectMapper();
        module = new SimpleModule();
        module.addAbstractTypeMapping(Map.class, HashMap.class);
        module.addAbstractTypeMapping(List.class, ArrayList.class);
        mapper.registerModule(module);
        mapType = mapper.getTypeFactory().constructRawMapType(HashMap.class);
    }

    protected final Map<String, Object> data;

    protected DataObject(@Nonnull Map<String, Object> data)
    {
        this.data = data;
    }

    /**
     * Creates a new empty DataObject, ready to be populated with values.
     *
     * @return An empty DataObject instance
     *
     * @see    #put(String, Object)
     */
    @Nonnull
    public static DataObject empty()
    {
        return new DataObject(new HashMap<>());
    }

    /**
     * Parses a JSON payload into a DataObject instance.
     *
     * @param  data
     *         The correctly formatted JSON payload to parse
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the provided json is incorrectly formatted
     *
     * @return A DataObject instance for the provided payload
     */
    @Nonnull
    public static DataObject fromJson(@Nonnull byte[] data)
    {
        try
        {
            Map<String, Object> map = mapper.readValue(data, mapType);
            return new DataObject(map);
        }
        catch (IOException ex)
        {
            throw new ParsingException(ex);
        }
    }

    /**
     * Parses a JSON payload into a DataObject instance.
     *
     * @param  json
     *         The correctly formatted JSON payload to parse
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the provided json is incorrectly formatted
     *
     * @return A DataObject instance for the provided payload
     */
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
            throw new ParsingException(ex);
        }
    }

    /**
     * Parses a JSON payload into a DataObject instance.
     *
     * @param  stream
     *         The correctly formatted JSON payload to parse
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the provided json is incorrectly formatted or an I/O error occurred
     *
     * @return A DataObject instance for the provided payload
     */
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
            throw new ParsingException(ex);
        }
    }

    /**
     * Parses a JSON payload into a DataObject instance.
     *
     * @param  stream
     *         The correctly formatted JSON payload to parse
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the provided json is incorrectly formatted or an I/O error occurred
     *
     * @return A DataObject instance for the provided payload
     */
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
            throw new ParsingException(ex);
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
     * @return A DataObject instance for the provided payload
     *
     * @since  4.2.1
     */
    @Nonnull
    public static DataObject fromETF(@Nonnull byte[] data)
    {
        Checks.notNull(data, "Data");
        try
        {
            Map<String, Object> map = ExTermDecoder.unpackMap(ByteBuffer.wrap(data));
            return new DataObject(map);
        }
        catch (Exception ex)
        {
            log.error("Failed to parse ETF data {}", Arrays.toString(data), ex);
            throw new ParsingException(ex);
        }
    }

    /**
     * Whether the specified key is present.
     *
     * @param  key
     *         The key to check
     *
     * @return True, if the specified key is present
     */
    public boolean hasKey(@Nonnull String key)
    {
        return data.containsKey(key);
    }

    /**
     * Whether the specified key is missing or null
     *
     * @param  key
     *         The key to check
     *
     * @return True, if the specified key is null or missing
     */
    public boolean isNull(@Nonnull String key)
    {
        return data.get(key) == null;
    }

    /**
     * Whether the specified key is of the specified type.
     *
     * @param  key
     *         The key to check
     * @param  type
     *         The type to check
     *
     * @return True, if the type check is successful
     *
     * @see    net.dv8tion.jda.api.utils.data.DataType#isType(Object) DataType.isType(Object)
     */
    public boolean isType(@Nonnull String key, @Nonnull DataType type)
    {
        return type.isType(data.get(key));
    }

    /**
     * Resolves a DataObject to a key.
     *
     * @param  key
     *         The key to check for a value
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the type is incorrect or no value is present for the specified key
     *
     * @return The resolved instance of DataObject for the key
     */
    @Nonnull
    public DataObject getObject(@Nonnull String key)
    {
        return optObject(key).orElseThrow(() -> valueError(key, "DataObject"));
    }

    /**
     * Resolves a DataObject to a key.
     *
     * @param  key
     *         The key to check for a value
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the type is incorrect
     *
     * @return The resolved instance of DataObject for the key, wrapped in {@link java.util.Optional}
     */
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

    /**
     * Resolves a DataArray to a key.
     *
     * @param  key
     *         The key to check for a value
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the type is incorrect or no value is present for the specified key
     *
     * @return The resolved instance of DataArray for the key
     */
    @Nonnull
    public DataArray getArray(@Nonnull String key)
    {
        return optArray(key).orElseThrow(() -> valueError(key, "DataArray"));
    }

    /**
     * Resolves a DataArray to a key.
     *
     * @param  key
     *         The key to check for a value
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the type is incorrect
     *
     * @return The resolved instance of DataArray for the key, wrapped in {@link java.util.Optional}
     */
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

    /**
     * Resolves any type to the provided key.
     *
     * @param  key
     *         The key to check for a value
     *
     * @return {@link java.util.Optional} with a possible value
     */
    @Nonnull
    public Optional<Object> opt(@Nonnull String key)
    {
        return Optional.ofNullable(data.get(key));
    }

    /**
     * Resolves any type to the provided key.
     *
     * @param  key
     *         The key to check for a value
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is missing or null
     *
     * @return The value of any type
     *
     * @see    #opt(String)
     */
    @Nonnull
    public Object get(@Nonnull String key)
    {
        Object value = data.get(key);
        if (value == null)
            throw valueError(key, "any");
        return value;
    }

    /**
     * Resolves a {@link java.lang.String} to a key.
     *
     * @param  key
     *         The key to check for a value
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is missing or null
     *
     * @return The String value
     */
    @Nonnull
    public String getString(@Nonnull String key)
    {
        String value = getString(key, null);
        if (value == null)
            throw valueError(key, "String");
        return value;
    }

    /**
     * Resolves a {@link java.lang.String} to a key.
     *
     * @param  key
     *         The key to check for a value
     * @param  defaultValue
     *         Alternative value to use when no value or null value is associated with the key
     *
     * @return The String value, or null if provided with null defaultValue
     */
    @Contract("_, !null -> !null")
    public String getString(@Nonnull String key, @Nullable String defaultValue)
    {
        String value = get(String.class, key, UnaryOperator.identity(), String::valueOf);
        return value == null ? defaultValue : value;
    }

    /**
     * Resolves a {@link java.lang.Boolean} to a key.
     *
     * @param  key
     *         The key to check for a value
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is of the wrong type
     *
     * @return True, if the value is present and set to true. False if the value is missing or set to false.
     */
    public boolean getBoolean(@Nonnull String key)
    {
        return getBoolean(key, false);
    }

    /**
     * Resolves a {@link java.lang.Boolean} to a key.
     *
     * @param  key
     *         The key to check for a value
     * @param  defaultValue
     *         Alternative value to use when no value or null value is associated with the key
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is of the wrong type
     *
     * @return True, if the value is present and set to true. False if the value is set to false. defaultValue if it is missing.
     */
    public boolean getBoolean(@Nonnull String key, boolean defaultValue)
    {
        Boolean value = get(Boolean.class, key, Boolean::parseBoolean, null);
        return value == null ? defaultValue : value;
    }

    /**
     * Resolves a long to a key.
     *
     * @param  key
     *         The key to check for a value
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is missing, null, or of the wrong type
     *
     * @return The long value for the key
     */
    public long getLong(@Nonnull String key)
    {
        Long value = get(Long.class, key, MiscUtil::parseLong, Number::longValue);
        if (value == null)
            throw valueError(key, "long");
        return value;
    }

    /**
     * Resolves a long to a key.
     *
     * @param  key
     *         The key to check for a value
     * @param  defaultValue
     *         Alternative value to use when no value or null value is associated with the key
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is of the wrong type
     *
     * @return The long value for the key
     */
    public long getLong(@Nonnull String key, long defaultValue)
    {
        Long value = get(Long.class, key, Long::parseLong, Number::longValue);
        return value == null ? defaultValue : value;
    }

    /**
     * Resolves an unsigned long to a key.
     *
     * @param  key
     *         The key to check for a value
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is missing, null, or of the wrong type
     *
     * @return The unsigned long value for the key
     */
    public long getUnsignedLong(@Nonnull String key)
    {
        Long value = get(Long.class, key, Long::parseUnsignedLong, Number::longValue);
        if (value == null)
            throw valueError(key, "unsigned long");
        return value;
    }

    /**
     * Resolves an unsigned long to a key.
     *
     * @param  key
     *         The key to check for a value
     * @param  defaultValue
     *         Alternative value to use when no value or null value is associated with the key
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is of the wrong type
     *
     * @return The unsigned long value for the key
     */
    public long getUnsignedLong(@Nonnull String key, long defaultValue)
    {
        Long value = get(Long.class, key, Long::parseUnsignedLong, Number::longValue);
        return value == null ? defaultValue : value;
    }

    /**
     * Resolves an int to a key.
     *
     * @param  key
     *         The key to check for a value
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is missing, null, or of the wrong type
     *
     * @return The int value for the key
     */
    public int getInt(@Nonnull String key)
    {
        Integer value = get(Integer.class, key, Integer::parseInt, Number::intValue);
        if (value == null)
            throw valueError(key, "int");
        return value;
    }

    /**
     * Resolves an int to a key.
     *
     * @param  key
     *         The key to check for a value
     * @param  defaultValue
     *         Alternative value to use when no value or null value is associated with the key
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is of the wrong type
     *
     * @return The int value for the key
     */
    public int getInt(@Nonnull String key, int defaultValue)
    {
        Integer value = get(Integer.class, key, Integer::parseInt, Number::intValue);
        return value == null ? defaultValue : value;
    }

    /**
     * Resolves an unsigned int to a key.
     *
     * @param  key
     *         The key to check for a value
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is missing, null, or of the wrong type
     *
     * @return The unsigned int value for the key
     */
    public int getUnsignedInt(@Nonnull String key)
    {
        Integer value = get(Integer.class, key, Integer::parseUnsignedInt, Number::intValue);
        if (value == null)
            throw valueError(key, "unsigned int");
        return value;
    }

    /**
     * Resolves an unsigned int to a key.
     *
     * @param  key
     *         The key to check for a value
     * @param  defaultValue
     *         Alternative value to use when no value or null value is associated with the key
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is of the wrong type
     *
     * @return The unsigned int value for the key
     */
    public int getUnsignedInt(@Nonnull String key, int defaultValue)
    {
        Integer value = get(Integer.class, key, Integer::parseUnsignedInt, Number::intValue);
        return value == null ? defaultValue : value;
    }

    /**
     * Resolves a double to a key.
     * 
     * @param  key
     *         The key to check for a value
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is missing, null, or of the wrong type
     * 
     * @return The double value for the key
     */
    public double getDouble(@Nonnull String key)
    {
        Double value = get(Double.class, key, Double::parseDouble, Number::doubleValue);
        if(value == null)
            throw valueError(key, "double");
        return value;
    }

    /**
     * Resolves a double to a key.
     *
     * @param  key
     *         The key to check for a value
     * @param  defaultValue
     *         Alternative value to use when no value or null value is associated with the key
     * 
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is of the wrong type
     * 
     * @return The double value for the key
     */
    public double getDouble(@Nonnull String key, double defaultValue)
    {
        Double value = get(Double.class, key, Double::parseDouble, Number::doubleValue);
        return value == null ? defaultValue : value;
    }

    /**
     * Resolves an {@link OffsetDateTime} to a key.
     * <br><b>NOte:</b> This method should be used on ISO8601 timestamps
     *
     * @param key
     *        The key to check for a value
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is missing, null, or not a valid ISO8601 timestamp
     */
    public OffsetDateTime getOffsetDateTime(@Nonnull String key)
    {
        OffsetDateTime value = getOffsetDateTime(key, null);
        if(value == null)
            throw valueError(key, "OffsetDateTime");
        return value;
    }
    /**
     * Resolves an {@link OffsetDateTime} to a key.
     * <br><b>Note:</b> This method should only be used on ISO8601 timestamps
     *
     * @param  key
     *         The key to check for a value
     * @param  defaultValue
     *         Alternative value to use when no value or null value is associated with the key
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is not a valid ISO8601 timestamp
     *
     * @return An {@link OffsetDateTime} object representing the timestamp
     */
    public OffsetDateTime getOffsetDateTime(@Nonnull String key, @Nullable OffsetDateTime defaultValue)
    {
        OffsetDateTime value;
        try
        {
            value = get(OffsetDateTime.class, key, OffsetDateTime::parse, null);
        }
        catch (DateTimeParseException e)
        {
            String reason = "Cannot parse value for %s into an OffsetDateTime object. Try double checking that %s is a valid ISO8601 timestmap";
            throw new ParsingException(String.format(reason, key, e.getParsedString()));
        }
        return value == null ? defaultValue : value;
    }

    /**
     * Removes the value associated with the specified key.
     * If no value is associated with the key, this does nothing.
     *
     * @param  key
     *         The key to unlink
     *
     * @return A DataObject with the removed key
     */
    @Nonnull
    public DataObject remove(@Nonnull String key)
    {
        data.remove(key);
        return this;
    }

    /**
     * Upserts a null value for the provided key.
     *
     * @param  key
     *         The key to upsert
     *
     * @return A DataObject with the updated value
     */
    @Nonnull
    public DataObject putNull(@Nonnull String key)
    {
        data.put(key, null);
        return this;
    }

    /**
     * Upserts a new value for the provided key.
     *
     * @param  key
     *         The key to upsert
     * @param  value
     *         The new value
     *
     * @return A DataObject with the updated value
     */
    @Nonnull
    public DataObject put(@Nonnull String key, @Nullable Object value)
    {
        if (value instanceof SerializableData)
            data.put(key, ((SerializableData) value).toData().data);
        else if (value instanceof SerializableArray)
            data.put(key, ((SerializableArray) value).toDataArray().data);
        else
            data.put(key, value);
        return this;
    }

    /**
     * {@link java.util.Collection} of all values in this DataObject.
     *
     * @return {@link java.util.Collection} for all values
     */
    @Nonnull
    public Collection<Object> values()
    {
        return data.values();
    }

    /**
     * {@link java.util.Set} of all keys in this DataObject.
     *
     * @return {@link Set} of keys
     */
    @Nonnull
    public Set<String> keys()
    {
        return data.keySet();
    }

    /**
     * Serialize this object as JSON.
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
     * Serializes this object as ETF MAP term.
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
        DefaultPrettyPrinter.Indenter indent = new DefaultIndenter("    ", DefaultIndenter.SYS_LF);
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
        printer.withObjectIndenter(indent).withArrayIndenter(indent);
        try
        {
            return mapper.writer(printer).writeValueAsString(data);
        }
        catch (JsonProcessingException e)
        {
            throw new ParsingException(e);
        }
    }

    /**
     * Converts this DataObject to a {@link java.util.Map}
     *
     * @return The resulting map
     */
    @Nonnull
    public Map<String, Object> toMap()
    {
        return data;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        return this;
    }

    private ParsingException valueError(String key, String expectedType)
    {
        return new ParsingException("Unable to resolve value with key " + key + " to type " + expectedType + ": " + data.get(key));
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
        if (type.isInstance(value))
            return type.cast(value);
        if (type == String.class)
            return type.cast(value.toString());
        // attempt type coercion
        if (value instanceof Number && numberParse != null)
            return numberParse.apply((Number) value);
        else if (value instanceof String && stringParse != null)
            return stringParse.apply((String) value);

        throw new ParsingException(Helpers.format("Cannot parse value for %s into type %s: %s instance of %s",
                                                      key, type.getSimpleName(), value, value.getClass().getSimpleName()));
    }
}
