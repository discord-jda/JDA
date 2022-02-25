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

package net.dv8tion.jda.api.utils.data.etf;

import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.SerializableData;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static net.dv8tion.jda.api.utils.data.etf.ExTermTag.*;

/**
 * Encodes an object into a binary ETF representation.
 *
 * @see #pack(Object)
 *
 * @since  4.2.1
 */
public class ExTermEncoder
{
    /**
     * Encodes the provided object into an ETF buffer.
     *
     * <h4>The mapping is as follows:</h4>
     * <ul>
     *     <li>{@code String -> Binary}</li>
     *     <li>{@code Map -> Map}</li>
     *     <li>{@code Collection -> List | NIL}</li>
     *     <li>{@code Byte -> Small Int}</li>
     *     <li>{@code Integer, Short -> Int | Small Int}</li>
     *     <li>{@code Long -> Small BigInt | Int | Small Int}</li>
     *     <li>{@code Float, Double -> New Float}</li>
     *     <li>{@code Boolean -> Atom(Boolean)}</li>
     *     <li>{@code null -> Atom("nil")}</li>
     * </ul>
     *
     * @param  data
     *         The object to encode
     *
     * @throws UnsupportedOperationException
     *         If there is no type mapping for the provided object
     *
     * @return {@link ByteBuffer} with the encoded ETF term
     */
    public static ByteBuffer pack(Object data)
    {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put((byte) 131);

        ByteBuffer packed = pack(buffer, data);
        // This cast prevents issues with backwards compatibility in the ABI (java 11 made breaking changes)
        ((Buffer) packed).flip();
        return packed;
    }

    @SuppressWarnings("unchecked")
    private static ByteBuffer pack(ByteBuffer buffer, Object value)
    {
        if (value instanceof String)
            return packBinary(buffer, (String) value);
        if (value instanceof Map)
            return packMap(buffer, (Map<String, Object>) value);
        if (value instanceof SerializableData)
            return packMap(buffer, ((SerializableData) value).toData().toMap());
        if (value instanceof Collection)
            return packList(buffer, (Collection<Object>) value);
        if (value instanceof DataArray)
            return packList(buffer, ((DataArray) value).toList());
        if (value instanceof Byte)
            return packSmallInt(buffer, (byte) value);
        if (value instanceof Integer || value instanceof Short)
            return packInt(buffer, (int) value);
        if (value instanceof Long)
            return packLong(buffer, (long) value);
        if (value instanceof Float || value instanceof Double)
            return packFloat(buffer, (double) value);
        if (value instanceof Boolean)
            return packAtom(buffer, String.valueOf(value));
        if (value == null)
            return packAtom(buffer, "nil");
        // imagine we had templates :O
        if (value instanceof long[])
            return packArray(buffer, (long[]) value);
        if (value instanceof int[])
            return packArray(buffer, (int[]) value);
        if (value instanceof short[])
            return packArray(buffer, (short[]) value);
        if (value instanceof byte[])
            return packArray(buffer, (byte[]) value);
        // omitting other array types because we don't use them anywhere
        if (value instanceof Object[])
            return packList(buffer, Arrays.asList((Object[]) value));

        throw new UnsupportedOperationException("Cannot pack value of type " + value.getClass().getName());
    }

    private static ByteBuffer realloc(ByteBuffer buffer, int length)
    {
        if (buffer.remaining() >= length)
            return buffer;

        ByteBuffer allocated = ByteBuffer.allocate((buffer.position() + length) << 1);
        // This cast prevents issues with backwards compatibility in the ABI (java 11 made breaking changes)
        ((Buffer) buffer).flip();
        allocated.put(buffer);
        return allocated;
    }

    private static ByteBuffer packMap(ByteBuffer buffer, Map<String, Object> data)
    {
        buffer = realloc(buffer, data.size() + 5);
        buffer.put(MAP);
        buffer.putInt(data.size());

        for (Map.Entry<String, Object> entry : data.entrySet())
        {
            buffer = packBinary(buffer, entry.getKey());
            buffer = pack(buffer, entry.getValue());
        }

        return buffer;
    }

    private static ByteBuffer packList(ByteBuffer buffer, Collection<Object> data)
    {
        if (data.isEmpty())
        {
            // NIL is for empty lists
            return packNil(buffer);
        }

        buffer = realloc(buffer, data.size() + 6);
        buffer.put(LIST);
        buffer.putInt(data.size());
        for (Object element : data)
            buffer = pack(buffer, element);
        return packNil(buffer);
    }

    private static ByteBuffer packBinary(ByteBuffer buffer, String value)
    {
        byte[] encoded = value.getBytes(StandardCharsets.UTF_8);
        buffer = realloc(buffer, encoded.length * 4 + 5);
        buffer.put(BINARY);
        buffer.putInt(encoded.length);
        buffer.put(encoded);
        return buffer;
    }

    private static ByteBuffer packSmallInt(ByteBuffer buffer, byte value)
    {
        buffer = realloc(buffer, 2);
        buffer.put(SMALL_INT);
        buffer.put(value);
        return buffer;
    }

    private static ByteBuffer packInt(ByteBuffer buffer, int value)
    {
        if (countBytes(value) <= 1 && value >= 0)
            return packSmallInt(buffer, (byte) value);
        buffer = realloc(buffer, 5);
        buffer.put(INT);
        buffer.putInt(value);
        return buffer;
    }

    private static ByteBuffer packLong(ByteBuffer buffer, long value)
    {
        byte bytes = countBytes(value);
        if (bytes <= 1) // Use optimized small int encoding
            return packSmallInt(buffer, (byte) value);
        if (bytes <= 4 && value >= 0)
        {
            // Use int to encode it
            buffer = realloc(buffer, 5);
            buffer.put(INT);
            buffer.putInt((int) value);
            return buffer;
        }

        buffer = realloc(buffer, 3 + bytes);
        buffer.put(SMALL_BIGINT);
        buffer.put(bytes);
        // We only use "unsigned" value so the sign is always positive
        buffer.put((byte) 0);
        while (value > 0)
        {
            buffer.put((byte) value);
            value >>>= 8;
        }

        return buffer;
    }

    private static ByteBuffer packFloat(ByteBuffer buffer, double value)
    {
        buffer = realloc(buffer, 9);
        buffer.put(NEW_FLOAT);
        buffer.putDouble(value);
        return buffer;
    }

    private static ByteBuffer packAtom(ByteBuffer buffer, String value)
    {
        byte[] array = value.getBytes(StandardCharsets.ISO_8859_1);
        buffer = realloc(buffer, array.length + 3);
        buffer.put(ATOM);
        buffer.putShort((short) array.length);
        buffer.put(array);
        return buffer;
    }

    private static ByteBuffer packArray(ByteBuffer buffer, long[] array)
    {
        if (array.length == 0)
            return packNil(buffer);

        buffer = realloc(buffer, array.length * 8 + 6);
        buffer.put(LIST);
        buffer.putInt(array.length);
        for (long it : array)
            buffer = packLong(buffer, it);
        return packNil(buffer);
    }

    private static ByteBuffer packArray(ByteBuffer buffer, int[] array)
    {
        if (array.length == 0)
            return packNil(buffer);

        buffer = realloc(buffer, array.length * 4 + 6);
        buffer.put(LIST);
        buffer.putInt(array.length);
        for (int it : array)
            buffer = packInt(buffer, it);
        return packNil(buffer);
    }

    private static ByteBuffer packArray(ByteBuffer buffer, short[] array)
    {
        if (array.length == 0)
            return packNil(buffer);

        buffer = realloc(buffer, array.length * 2 + 6);
        buffer.put(LIST);
        buffer.putInt(array.length);
        for (short it : array)
            buffer = packInt(buffer, it);
        return packNil(buffer);
    }

    private static ByteBuffer packArray(ByteBuffer buffer, byte[] array)
    {
        if (array.length == 0)
            return packNil(buffer);

        buffer = realloc(buffer, array.length + 6);
        buffer.put(LIST);
        buffer.putInt(array.length);
        for (byte it : array)
            buffer = packSmallInt(buffer, it);
        return packNil(buffer);
    }

    private static ByteBuffer packNil(ByteBuffer buffer)
    {
        buffer = realloc(buffer, 1);
        buffer.put(NIL);
        return buffer;
    }

    private static byte countBytes(long value)
    {
        int leadingZeros = Long.numberOfLeadingZeros(value);
        return (byte) Math.ceil((64 - leadingZeros) / 8.);
    }
}
