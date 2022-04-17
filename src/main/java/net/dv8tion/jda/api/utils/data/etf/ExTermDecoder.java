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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.InflaterOutputStream;

import static net.dv8tion.jda.api.utils.data.etf.ExTermTag.*;

/**
 * Decodes an ETF encoded payload to a java object representation.
 *
 * @see #unpack(ByteBuffer)
 * @see #unpackMap(ByteBuffer)
 * @see #unpackList(ByteBuffer)
 *
 * @since  4.2.1
 */
public class ExTermDecoder
{
    /**
     * Unpacks the provided term into a java object.
     *
     * <h4>The mapping is as follows:</h4>
     * <ul>
     *     <li>{@code Small Int | Int -> Integer}</li>
     *     <li>{@code Small BigInt -> Long}</li>
     *     <li>{@code Float | New Float -> Double}</li>
     *     <li>{@code Small Atom | Atom -> Boolean | null | String}</li>
     *     <li>{@code Binary | String -> String}</li>
     *     <li>{@code List | NIL -> List}</li>
     *     <li>{@code Map -> Map}</li>
     * </ul>
     *
     * @param  buffer
     *         The {@link ByteBuffer} containing the encoded term
     *
     * @throws IllegalArgumentException
     *         If the buffer does not start with the version byte {@code 131} or contains an unsupported tag
     *
     * @return The java object
     */
    public static Object unpack(ByteBuffer buffer)
    {
        if (buffer.get() != -125)
            throw new IllegalArgumentException("Failed header check");

        return unpack0(buffer);
    }

    /**
     * Unpacks the provided term into a java {@link Map}.
     *
     * <h4>The mapping is as follows:</h4>
     * <ul>
     *     <li>{@code Small Int | Int -> Integer}</li>
     *     <li>{@code Small BigInt -> Long}</li>
     *     <li>{@code Float | New Float -> Double}</li>
     *     <li>{@code Small Atom | Atom -> Boolean | null | String}</li>
     *     <li>{@code Binary | String -> String}</li>
     *     <li>{@code List | NIL -> List}</li>
     *     <li>{@code Map -> Map}</li>
     * </ul>
     *
     * @param  buffer
     *         The {@link ByteBuffer} containing the encoded term
     *
     * @throws IllegalArgumentException
     *         If the buffer does not start with a Map term, does not have the right version byte, or the format includes an unsupported tag
     *
     * @return The parsed {@link Map} instance
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> unpackMap(ByteBuffer buffer)
    {
        byte tag = buffer.get(1);
        if (tag != MAP)
            throw new IllegalArgumentException("Cannot unpack map from tag " + tag);
        return (Map<String, Object>) unpack(buffer);
    }

    /**
     * Unpacks the provided term into a java {@link List}.
     *
     * <h4>The mapping is as follows:</h4>
     * <ul>
     *     <li>{@code Small Int | Int -> Integer}</li>
     *     <li>{@code Small BigInt -> Long}</li>
     *     <li>{@code Float | New Float -> Double}</li>
     *     <li>{@code Small Atom | Atom -> Boolean | null | String}</li>
     *     <li>{@code Binary | String -> String}</li>
     *     <li>{@code List | NIL -> List}</li>
     *     <li>{@code Map -> Map}</li>
     * </ul>
     *
     * @param  buffer
     *         The {@link ByteBuffer} containing the encoded term
     *
     * @throws IllegalArgumentException
     *         If the buffer does not start with a List or NIL term, does not have the right version byte, or the format includes an unsupported tag
     *
     * @return The parsed {@link List} instance
     */
    @SuppressWarnings("unchecked")
    public static List<Object> unpackList(ByteBuffer buffer)
    {
        byte tag = buffer.get(1);
        if (tag != LIST)
            throw new IllegalArgumentException("Cannot unpack list from tag " + tag);

        return (List<Object>) unpack(buffer);
    }

    private static Object unpack0(ByteBuffer buffer)
    {
        int tag = buffer.get();
        switch (tag) {
        case COMPRESSED: return unpackCompressed(buffer);
        case SMALL_INT: return unpackSmallInt(buffer);
        case SMALL_BIGINT: return unpackSmallBigint(buffer);
        case INT: return unpackInt(buffer);

        case FLOAT: return unpackOldFloat(buffer);
        case NEW_FLOAT: return unpackFloat(buffer);

        case SMALL_ATOM_UTF8: return unpackSmallAtom(buffer, StandardCharsets.UTF_8);
        case SMALL_ATOM: return unpackSmallAtom(buffer, StandardCharsets.ISO_8859_1);
        case ATOM_UTF8: return unpackAtom(buffer, StandardCharsets.UTF_8);
        case ATOM: return unpackAtom(buffer, StandardCharsets.ISO_8859_1);

        case MAP: return unpackMap0(buffer);
        case LIST: return unpackList0(buffer);
        case NIL: return Collections.emptyList();

        case STRING: return unpackString(buffer);
        case BINARY: return unpackBinary(buffer);
        default:
            throw new IllegalArgumentException("Unknown tag " + tag);
        }
    }

    private static Object unpackCompressed(ByteBuffer buffer)
    {
        int size = buffer.getInt();
        ByteArrayOutputStream decompressed = new ByteArrayOutputStream(size);
        try (InflaterOutputStream inflater = new InflaterOutputStream(decompressed))
        {
            inflater.write(buffer.array(), buffer.position(), buffer.remaining());
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }

        buffer = ByteBuffer.wrap(decompressed.toByteArray());
        return unpack0(buffer);
    }

    private static double unpackOldFloat(ByteBuffer buffer)
    {
        String bytes = getString(buffer, StandardCharsets.ISO_8859_1, 31);
        return Double.parseDouble(bytes);
    }

    private static double unpackFloat(ByteBuffer buffer)
    {
        return buffer.getDouble();
    }

    private static long unpackSmallBigint(ByteBuffer buffer)
    {
        int arity = Byte.toUnsignedInt(buffer.get());
        int sign = Byte.toUnsignedInt(buffer.get());
        long sum = 0;
        long offset = 0;
        while (arity-- > 0)
        {
            sum += Byte.toUnsignedLong(buffer.get()) << offset;
            offset += 8;
        }

        return sign == 0 ? sum : -sum;
    }

    private static int unpackSmallInt(ByteBuffer buffer)
    {
        return Byte.toUnsignedInt(buffer.get());
    }

    private static int unpackInt(ByteBuffer buffer)
    {
        return buffer.getInt();
    }

    private static List<Object> unpackString(ByteBuffer buffer)
    {
        int length = Short.toUnsignedInt(buffer.getShort());
        List<Object> bytes = new ArrayList<>(length);
        while (length-- > 0)
            bytes.add(buffer.get());
        return bytes;
    }

    private static String unpackBinary(ByteBuffer buffer)
    {
        int length = buffer.getInt();
        return getString(buffer, StandardCharsets.UTF_8, length);
    }

    private static Object unpackSmallAtom(ByteBuffer buffer, Charset charset)
    {
        int length = Byte.toUnsignedInt(buffer.get());
        return unpackAtom(buffer, charset, length);
    }

    private static Object unpackAtom(ByteBuffer buffer, Charset charset)
    {
        int length = Short.toUnsignedInt(buffer.getShort());
        return unpackAtom(buffer, charset, length);
    }

    private static Object unpackAtom(ByteBuffer buffer, Charset charset, int length)
    {
        String value = getString(buffer, charset, length);
        switch (value)
        {
        case "true": return true;
        case "false": return false;
        case "nil": return null;
        default: return value;
        }
    }

    private static String getString(ByteBuffer buffer, Charset charset, int length)
    {
        byte[] array = new byte[length];
        buffer.get(array);
        return new String(array, charset);
    }

    private static List<Object> unpackList0(ByteBuffer buffer)
    {
        int length = buffer.getInt();
        List<Object> list = new ArrayList<>(length);
        while (length-- > 0)
        {
            list.add(unpack0(buffer));
        }
        Object tail = unpack0(buffer);
        if (tail != Collections.emptyList())
            throw new IllegalArgumentException("Unexpected tail " + tail);
        return list;
    }

    private static Map<String, Object> unpackMap0(ByteBuffer buffer)
    {
        Map<String, Object> map = new HashMap<>();
        int arity = buffer.getInt();
        while (arity-- > 0)
        {
            String key = (String) unpack0(buffer);
            Object value = unpack0(buffer);
            map.put(key, value);
        }
        return map;
    }
}
