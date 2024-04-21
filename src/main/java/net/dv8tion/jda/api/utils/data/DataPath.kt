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

import net.dv8tion.jda.api.exceptions.ParsingException;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

/**
 * This utility class can be used to access nested values within {@link DataObject DataObjects} and {@link DataArray DataArrays}.
 *
 * <p><b>Path Expression Grammar</b><br>
 *
 * The syntax for paths is given by this grammar:
 *
 * <pre>{@code
 * <name-syntax>  ::= /[^.\[\]]+/;
 * <index-syntax> ::= "[" <number> "]";
 * <name>         ::= <name-syntax> | <name-syntax> "?";
 * <index>        ::= <index-syntax> | <index-syntax> "?";
 * <element>      ::= <name> ( <index> )*;
 * <path-start>   ::= <element> | <index> ( <index> )*;
 * <path>         ::= <path-start> ( "." <element> )*;
 * }</pre>
 *
 * <p><b>Examples</b><br>
 * Given a JSON object such as:
 * <pre>{@code
 * {
 *     "array": [{
 *         "foo": "bar",
 *     }]
 * }
 * }</pre>
 *
 * The content of {@code "foo"} can be accessed using the code:
 * <pre>{@code String foo = DataPath.getString(root, "array[0].foo")}</pre>
 *
 * <p>With the safe-access operator {@code "?"}, you can also allow missing values within your path:
 * <pre>{@code String foo = DataPath.getString(root, "array[1]?.foo", "default")}</pre>
 * This will result in {@code foo == "default"}, since the array element 1 is marked as optional, and missing in the actual object.
 */
public class DataPath
{
    private static final Pattern INDEX_EXPRESSION = Pattern.compile("^\\[\\d+].*");
    private static final Pattern NAME_EXPRESSION = Pattern.compile("^[^\\[.].*");

    /**
     * Parses the given {@code path} and finds the appropriate value within this {@link DataObject}.
     *
     * @param  <T>
     *         The result type
     *
     * @param  root
     *         The root data object, which is the top level accessor.
     *         <br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     *         The path of the value, in accordance with the described grammar by {@link DataPath}.
     *         This must start with a name element, such as {@code "foo"}.
     * @param  fromObject
     *         Object relative resolver of the value, this is used for the final reference and resolves the value.
     *         The first parameter is the {@link DataObject} where you get the value from, and the second is the field name.
     *         An example would be {@code (obj, name) -> obj.getString(name)} or as a method reference {@code DataObject::getString}.
     * @param  fromArray
     *         Array relative resolver of the value, this is used for the final reference and resolves the value.
     *         The first parameter is the {@link DataArray} where you get the value from, and the second is the field index.
     *         An example would be {@code (array, index) -> obj.getString(index)} or as a method reference {@code DataArray::getString}.
     *
     * @throws ParsingException
     *         If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     *         If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     *         If null is provided or the path is empty
     *
     * @return The value at the provided path, using the provided resolver functions.
     *         Possibly null, if the path ends with a "?" operator, or the resolver function returns null.
     */
    public static <T> T get(@Nonnull DataObject root, @Nonnull String path, @Nonnull BiFunction<DataObject, String, ? extends T> fromObject, @Nonnull BiFunction<DataArray, Integer, ? extends T> fromArray)
    {
        Checks.notEmpty(path, "Path");
        Checks.matches(path, NAME_EXPRESSION, "Path");
        Checks.notNull(root, "DataObject");
        Checks.notNull(fromObject, "Object Resolver");
        Checks.notNull(fromArray, "Array Resolver");
        return getUnchecked(root, path, fromObject, fromArray);
    }

    private static <T> T getUnchecked(DataObject root, String path, BiFunction<DataObject, String, ? extends T> fromObject, BiFunction<DataArray, Integer, ? extends T> fromArray)
    {
        String[] parts = path.split("\\.", 2);
        String current = parts[0];
        String child = parts.length > 1 ? parts[1] : null;

        // if key is array according to index in path
        if (current.indexOf('[') > -1)
        {
            int arrayIndex = current.indexOf('[');
            String key = current.substring(0, arrayIndex);
            path = path.substring(arrayIndex);

            // isOptional
            if (key.endsWith("?"))
            {
                key = key.substring(0, key.length() - 1);
                if (root.isNull(key))
                    return null;
            }

            return getUnchecked(root.getArray(key), path, fromObject, fromArray);
        }

        boolean isOptional = current.endsWith("?");
        if (isOptional)
            current = current.substring(0, current.length() - 1);

        if (child == null)
        {
            if (isOptional && root.isNull(current))
                return null;
            return fromObject.apply(root, current);
        }

        if (isOptional && root.isNull(current))
            return null;

        return getUnchecked(root.getObject(current), child, fromObject, fromArray);
    }

    /**
     * Parses the given {@code path} and finds the appropriate value within this {@link DataArray}.
     *
     * @param  <T>
     *         The result type
     *
     * @param  root
     *         The root data array, which is the top level accessor.
     *         <br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     *         The path of the value, in accordance with the described grammar by {@link DataPath}.
     *         This must start with an index element, such as {@code "[0]"}.
     * @param  fromObject
     *         Object relative resolver of the value, this is used for the final reference and resolves the value.
     *         The first parameter is the {@link DataObject} where you get the value from, and the second is the field name.
     *         An example would be {@code (obj, name) -> obj.getString(name)} or as a method reference {@code DataObject::getString}.
     * @param  fromArray
     *         Array relative resolver of the value, this is used for the final reference and resolves the value.
     *         The first parameter is the {@link DataArray} where you get the value from, and the second is the field index.
     *         An example would be {@code (array, index) -> obj.getString(index)} or as a method reference {@code DataArray::getString}.
     *
     * @throws ParsingException
     *         If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     *         If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     *         If null is provided or the path is empty
     *
     * @return The value at the provided path, using the provided resolver functions.
     *         Possibly null, if the path ends with a "?" operator, or the resolver function returns null.
     */
    public static <T> T get(@Nonnull DataArray root, @Nonnull String path, @Nonnull BiFunction<DataObject, String, ? extends T> fromObject, @Nonnull BiFunction<DataArray, Integer, ? extends T> fromArray)
    {
        Checks.notNull(root, "DataArray");
        Checks.notEmpty(path, "Path");
        Checks.matches(path, INDEX_EXPRESSION, "Path");
        Checks.notNull(fromObject, "Object Resolver");
        Checks.notNull(fromArray, "Array Resolver");
        return getUnchecked(root, path, fromObject, fromArray);
    }

    private static <T> T getUnchecked(DataArray root, String path, BiFunction<DataObject, String, ? extends T> fromObject, BiFunction<DataArray, Integer, ? extends T> fromArray)
    {
        byte[] chars = path.getBytes(StandardCharsets.UTF_8);
        int offset = 0;

        // This is just to prevent infinite loop if some strange thing happens, should be impossible
        for (int i = 0; i < chars.length; i++)
        {
            int end = indexOf(chars, offset + 1, ']');
            int index = Integer.parseInt(path.substring(offset + 1, end));

            offset = Math.min(chars.length, end + 1);
            boolean optional = offset != chars.length && chars[offset] == '?';

            boolean isMissing = root.length() <= index || root.isNull(index);
            if (optional)
            {
                offset++;
                if (isMissing)
                    return null;
            }

            if (offset == chars.length)
                return fromArray.apply(root, index);

            if (chars[offset] == '[')
                root = root.getArray(index);
            else
                return getUnchecked(root.getObject(index), path.substring(offset + 1), fromObject, fromArray);
        }

        throw new ParsingException("Array path nesting seems to be way too deep, we went " + chars.length + " arrays deep. Path: " + path);
    }

    private static int indexOf(byte[] chars, int offset, char c)
    {
        byte b = (byte) c;
        for (int i = offset; i < chars.length; i++)
            if (chars[i] == b)
                return i;
        return -1;
    }

    /**
     * Parses the given {@code path} and finds the appropriate value within this {@link DataObject}.
     *
     * @param  root
     *         The root data object, which is the top level accessor.
     *         <br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     *         The path of the value, in accordance with the described grammar by {@link DataPath}.
     *         This must start with a name element, such as {@code "foo"}.
     *
     * @throws ParsingException
     *         If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     *         If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     *         If null is provided or the path is empty
     *
     * @return The boolean value at the given path, if declared as optional this returns false when the value is missing.
     */
    public static boolean getBoolean(@Nonnull DataObject root, @Nonnull String path)
    {
        Boolean bool = get(root, path, DataObject::getBoolean, DataArray::getBoolean);
        return bool != null && bool;
    }

    /**
     * Parses the given {@code path} and finds the appropriate value within this {@link DataObject}.
     *
     * @param  root
     *         The root data object, which is the top level accessor.
     *         <br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     *         The path of the value, in accordance with the described grammar by {@link DataPath}.
     *         This must start with a name element, such as {@code "foo"}.
     *
     * @throws ParsingException
     *         If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     *         If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     *         If null is provided or the path is empty
     *
     * @return The boolean value at the given path, if declared as optional this returns the provided fallback when the value is missing.
     */
    public static boolean getBoolean(@Nonnull DataObject root, @Nonnull String path, boolean fallback)
    {
        Boolean bool = get(root, path, (obj, key) -> obj.getBoolean(key, fallback), (arr, index) -> arr.getBoolean(index, fallback));
        return bool != null ? bool : fallback;
    }

    /**
     * Parses the given {@code path} and finds the appropriate value within this {@link DataArray}.
     *
     * @param  root
     *         The root data array, which is the top level accessor.
     *         <br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     *         The path of the value, in accordance with the described grammar by {@link DataPath}.
     *         This must start with an index element, such as {@code "[0]"}.
     *
     * @throws ParsingException
     *         If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     *         If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     *         If null is provided or the path is empty
     *
     * @return The boolean value at the given path, if declared as optional this returns false when the value is missing.
     */
    public static boolean getBoolean(@Nonnull DataArray root, @Nonnull String path)
    {
        Boolean bool = get(root, path, DataObject::getBoolean, DataArray::getBoolean);
        return bool != null && bool;
    }

    /**
     * Parses the given {@code path} and finds the appropriate value within this {@link DataArray}.
     *
     * @param  root
     *         The root data array, which is the top level accessor.
     *         <br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     *         The path of the value, in accordance with the described grammar by {@link DataPath}.
     *         This must start with an index element, such as {@code "[0]"}.
     *
     * @throws ParsingException
     *         If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     *         If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     *         If null is provided or the path is empty
     *
     * @return The boolean value at the given path, if declared as optional this returns the provided fallback when the value is missing.
     */
    public static boolean getBoolean(@Nonnull DataArray root, @Nonnull String path, boolean fallback)
    {
        Boolean bool = get(root, path, (obj, key) -> obj.getBoolean(key, fallback), (arr, index) -> arr.getBoolean(index, fallback));
        return bool != null ? bool : fallback;
    }

    /**
     * Parses the given {@code path} and finds the appropriate value within this {@link DataObject}.
     * <br>If the resulting value is a string, this will parse the string using {@link Integer#parseInt(String)}.
     *
     * @param  root
     *         The root data object, which is the top level accessor.
     *         <br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     *         The path of the value, in accordance with the described grammar by {@link DataPath}.
     *         This must start with a name element, such as {@code "foo"}.
     *
     * @throws ParsingException
     *         If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     *         If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     *         If null is provided or the path is empty
     *
     * @return The int value at the given path
     */
    public static int getInt(@Nonnull DataObject root, @Nonnull String path)
    {
        Integer integer = get(root, path, DataObject::getInt, DataArray::getInt);
        if (integer == null)
            pathError(path, "int");
        return integer;
    }

    /**
     * Parses the given {@code path} and finds the appropriate value within this {@link DataObject}.
     * <br>If the resulting value is a string, this will parse the string using {@link Integer#parseInt(String)}.
     *
     * @param  root
     *         The root data object, which is the top level accessor.
     *         <br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     *         The path of the value, in accordance with the described grammar by {@link DataPath}.
     *         This must start with a name element, such as {@code "foo"}.
     *
     * @throws ParsingException
     *         If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     *         If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     *         If null is provided or the path is empty
     *
     * @return The int value at the given path, returning the fallback if the path resolves to an optional value that is missing.
     */
    public static int getInt(@Nonnull DataObject root, @Nonnull String path, int fallback)
    {
        Integer integer = get(root, path, (obj, key) -> obj.getInt(key, fallback), (arr, index) -> arr.getInt(index, fallback));
        return integer == null ? fallback : integer;
    }

    /**
     * Parses the given {@code path} and finds the appropriate value within this {@link DataArray}.
     * <br>If the resulting value is a string, this will parse the string using {@link Integer#parseInt(String)}.
     *
     * @param  root
     *         The root data array, which is the top level accessor.
     *         <br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     *         The path of the value, in accordance with the described grammar by {@link DataPath}.
     *         This must start with an index element, such as {@code "[0]"}.
     *
     * @throws ParsingException
     *         If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     *         If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     *         If null is provided or the path is empty
     *
     * @return The int value at the given path
     */
    public static int getInt(@Nonnull DataArray root, @Nonnull String path)
    {
        Integer integer = get(root, path, DataObject::getInt, DataArray::getInt);
        if (integer == null)
            pathError(path, "int");
        return integer;
    }

    /**
     * Parses the given {@code path} and finds the appropriate value within this {@link DataArray}.
     * <br>If the resulting value is a string, this will parse the string using {@link Integer#parseInt(String)}.
     *
     * @param  root
     *         The root data array, which is the top level accessor.
     *         <br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     *         The path of the value, in accordance with the described grammar by {@link DataPath}.
     *         This must start with an index element, such as {@code "[0]"}.
     *
     * @throws ParsingException
     *         If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     *         If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     *         If null is provided or the path is empty
     *
     * @return The int value at the given path, returning the fallback if the path resolves to an optional value that is missing.
     */
    public static int getInt(@Nonnull DataArray root, @Nonnull String path, int fallback)
    {
        Integer integer = get(root, path, (obj, key) -> obj.getInt(key, fallback), (arr, index) -> arr.getInt(index, fallback));
        return integer == null ? fallback : integer;
    }

    /**
     * Parses the given {@code path} and finds the appropriate value within this {@link DataObject}.
     * <br>If the resulting value is a string, this will parse the string using {@link Integer#parseUnsignedInt(String)}.
     *
     * @param  root
     *         The root data object, which is the top level accessor.
     *         <br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     *         The path of the value, in accordance with the described grammar by {@link DataPath}.
     *         This must start with a name element, such as {@code "foo"}.
     *
     * @throws ParsingException
     *         If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     *         If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     *         If null is provided or the path is empty
     *
     * @return The unsigned int value at the given path
     */
    public static int getUnsignedInt(@Nonnull DataObject root, @Nonnull String path)
    {
        Integer integer = get(root, path, DataObject::getUnsignedInt, DataArray::getUnsignedInt);
        if (integer == null)
            pathError(path, "unsigned int");
        return integer;
    }

    /**
     * Parses the given {@code path} and finds the appropriate value within this {@link DataObject}.
     * <br>If the resulting value is a string, this will parse the string using {@link Integer#parseUnsignedInt(String)}.
     *
     * @param  root
     *         The root data object, which is the top level accessor.
     *         <br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     *         The path of the value, in accordance with the described grammar by {@link DataPath}.
     *         This must start with a name element, such as {@code "foo"}.
     *
     * @throws ParsingException
     *         If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     *         If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     *         If null is provided or the path is empty
     *
     * @return The unsigned int value at the given path, returning the fallback if the path resolves to an optional value that is missing.
     */
    public static int getUnsignedInt(@Nonnull DataObject root, @Nonnull String path, int fallback)
    {
        Integer integer = get(root, path, (obj, key) -> obj.getUnsignedInt(key, fallback), (arr, index) -> arr.getUnsignedInt(index, fallback));
        return integer == null ? fallback : integer;
    }

    /**
     * Parses the given {@code path} and finds the appropriate value within this {@link DataArray}.
     * <br>If the resulting value is a string, this will parse the string using {@link Integer#parseUnsignedInt(String)}.
     *
     * @param  root
     *         The root data array, which is the top level accessor.
     *         <br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     *         The path of the value, in accordance with the described grammar by {@link DataPath}.
     *         This must start with an index element, such as {@code "[0]"}.
     *
     * @throws ParsingException
     *         If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     *         If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     *         If null is provided or the path is empty
     *
     * @return The unsigned int value at the given path
     */
    public static int getUnsignedInt(@Nonnull DataArray root, @Nonnull String path)
    {
        Integer integer = get(root, path, DataObject::getUnsignedInt, DataArray::getUnsignedInt);
        if (integer == null)
            pathError(path, "unsigned int");
        return integer;
    }

    /**
     * Parses the given {@code path} and finds the appropriate value within this {@link DataArray}.
     * <br>If the resulting value is a string, this will parse the string using {@link Integer#parseUnsignedInt(String)}.
     *
     * @param  root
     *         The root data array, which is the top level accessor.
     *         <br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     *         The path of the value, in accordance with the described grammar by {@link DataPath}.
     *         This must start with an index element, such as {@code "[0]"}.
     *
     * @throws ParsingException
     *         If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     *         If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     *         If null is provided or the path is empty
     *
     * @return The unsigned int value at the given path, returning the fallback if the path resolves to an optional value that is missing.
     */
    public static int getUnsignedInt(@Nonnull DataArray root, @Nonnull String path, int fallback)
    {
        Integer integer = get(root, path, (obj, key) -> obj.getUnsignedInt(key, fallback), (arr, index) -> arr.getUnsignedInt(index, fallback));
        return integer == null ? fallback : integer;
    }

    /**
     * Parses the given {@code path} and finds the appropriate value within this {@link DataObject}.
     * <br>If the resulting value is a string, this will parse the string using {@link Long#parseLong(String)}.
     *
     * @param  root
     *         The root data object, which is the top level accessor.
     *         <br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     *         The path of the value, in accordance with the described grammar by {@link DataPath}.
     *         This must start with a name element, such as {@code "foo"}.
     *
     * @throws ParsingException
     *         If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     *         If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     *         If null is provided or the path is empty
     *
     * @return The long value at the given path
     */
    public static long getLong(@Nonnull DataObject root, @Nonnull String path)
    {
        Long longValue = get(root, path, DataObject::getLong, DataArray::getLong);
        if (longValue == null)
            pathError(path, "long");
        return longValue;
    }

    /**
     * Parses the given {@code path} and finds the appropriate value within this {@link DataObject}.
     * <br>If the resulting value is a string, this will parse the string using {@link Long#parseLong(String)}.
     *
     * @param  root
     *         The root data object, which is the top level accessor.
     *         <br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     *         The path of the value, in accordance with the described grammar by {@link DataPath}.
     *         This must start with a name element, such as {@code "foo"}.
     *
     * @throws ParsingException
     *         If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     *         If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     *         If null is provided or the path is empty
     *
     * @return The long value at the given path, returning the fallback if the path resolves to an optional value that is missing.
     */
    public static long getLong(@Nonnull DataObject root, @Nonnull String path, long fallback)
    {
        Long longValue = get(root, path, (obj, key) -> obj.getLong(key, fallback), (arr, index) -> arr.getLong(index, fallback));
        return longValue == null ? fallback : longValue;
    }

    /**
     * Parses the given {@code path} and finds the appropriate value within this {@link DataArray}.
     * <br>If the resulting value is a string, this will parse the string using {@link Long#parseLong(String)}.
     *
     * @param  root
     *         The root data array, which is the top level accessor.
     *         <br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     *         The path of the value, in accordance with the described grammar by {@link DataPath}.
     *         This must start with an index element, such as {@code "[0]"}.
     *
     * @throws ParsingException
     *         If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     *         If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     *         If null is provided or the path is empty
     *
     * @return The long value at the given path
     */
    public static long getLong(@Nonnull DataArray root, @Nonnull String path)
    {
        Long longValue = get(root, path, DataObject::getLong, DataArray::getLong);
        if (longValue == null)
            pathError(path, "long");
        return longValue;
    }

    /**
     * Parses the given {@code path} and finds the appropriate value within this {@link DataArray}.
     * <br>If the resulting value is a string, this will parse the string using {@link Long#parseLong(String)}.
     *
     * @param  root
     *         The root data array, which is the top level accessor.
     *         <br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     *         The path of the value, in accordance with the described grammar by {@link DataPath}.
     *         This must start with an index element, such as {@code "[0]"}.
     *
     * @throws ParsingException
     *         If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     *         If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     *         If null is provided or the path is empty
     *
     * @return The long value at the given path, returning the fallback if the path resolves to an optional value that is missing.
     */
    public static long getLong(@Nonnull DataArray root, @Nonnull String path, long fallback)
    {
        Long longValue = get(root, path, (obj, key) -> obj.getLong(key, fallback), (arr, index) -> arr.getLong(index, fallback));
        return longValue == null ? fallback : longValue;
    }

    /**
     * Parses the given {@code path} and finds the appropriate value within this {@link DataObject}.
     * <br>If the resulting value is a string, this will parse the string using {@link Long#parseUnsignedLong(String)}.
     *
     * @param  root
     *         The root data object, which is the top level accessor.
     *         <br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     *         The path of the value, in accordance with the described grammar by {@link DataPath}.
     *         This must start with a name element, such as {@code "foo"}.
     *
     * @throws ParsingException
     *         If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     *         If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     *         If null is provided or the path is empty
     *
     * @return The unsigned long value at the given path
     */
    public static long getUnsignedLong(@Nonnull DataObject root, @Nonnull String path)
    {
        Long longValue = get(root, path, DataObject::getUnsignedLong, DataArray::getUnsignedLong);
        if (longValue == null)
            throw pathError(path, "unsigned long");
        return longValue;
    }

    /**
     * Parses the given {@code path} and finds the appropriate value within this {@link DataObject}.
     * <br>If the resulting value is a string, this will parse the string using {@link Long#parseUnsignedLong(String)}.
     *
     * @param  root
     *         The root data object, which is the top level accessor.
     *         <br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     *         The path of the value, in accordance with the described grammar by {@link DataPath}.
     *         This must start with a name element, such as {@code "foo"}.
     *
     * @throws ParsingException
     *         If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     *         If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     *         If null is provided or the path is empty
     *
     * @return The unsigned long value at the given path, returning the fallback if the path resolves to an optional value that is missing.
     */
    public static long getUnsignedLong(@Nonnull DataObject root, @Nonnull String path, long fallback)
    {
        Long longValue = get(root, path, (obj, key) -> obj.getUnsignedLong(key, fallback), (arr, index) -> arr.getUnsignedLong(index, fallback));
        return longValue == null ? fallback : longValue;
    }

    /**
     * Parses the given {@code path} and finds the appropriate value within this {@link DataArray}.
     * <br>If the resulting value is a string, this will parse the string using {@link Long#parseUnsignedLong(String)}.
     *
     * @param  root
     *         The root data array, which is the top level accessor.
     *         <br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     *         The path of the value, in accordance with the described grammar by {@link DataPath}.
     *         This must start with an index element, such as {@code "[0]"}.
     *
     * @throws ParsingException
     *         If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     *         If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     *         If null is provided or the path is empty
     *
     * @return The unsigned long value at the given path
     */
    public static long getUnsignedLong(@Nonnull DataArray root, @Nonnull String path)
    {
        Long longValue = get(root, path, DataObject::getUnsignedLong, DataArray::getUnsignedLong);
        if (longValue == null)
            throw pathError(path, "unsigned long");
        return longValue;
    }

    /**
     * Parses the given {@code path} and finds the appropriate value within this {@link DataArray}.
     * <br>If the resulting value is a string, this will parse the string using {@link Long#parseUnsignedLong(String)}.
     *
     * @param  root
     *         The root data array, which is the top level accessor.
     *         <br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     *         The path of the value, in accordance with the described grammar by {@link DataPath}.
     *         This must start with an index element, such as {@code "[0]"}.
     *
     * @throws ParsingException
     *         If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     *         If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     *         If null is provided or the path is empty
     *
     * @return The unsigned long value at the given path, returning the fallback if the path resolves to an optional value that is missing.
     */
    public static long getUnsignedLong(@Nonnull DataArray root, @Nonnull String path, long fallback)
    {
        Long longValue = get(root, path, (obj, key) -> obj.getUnsignedLong(key, fallback), (arr, index) -> arr.getUnsignedLong(index, fallback));
        return longValue == null ? fallback : longValue;
    }

    /**
     * Parses the given {@code path} and finds the appropriate value within this {@link DataObject}.
     * <br>If the resulting value is a string, this will parse the string using {@link Double#parseDouble(String)}.
     *
     * @param  root
     *         The root data object, which is the top level accessor.
     *         <br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     *         The path of the value, in accordance with the described grammar by {@link DataPath}.
     *         This must start with a name element, such as {@code "foo"}.
     *
     * @throws ParsingException
     *         If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     *         If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     *         If null is provided or the path is empty
     *
     * @return The double value at the given path
     */
    public static double getDouble(@Nonnull DataObject root, @Nonnull String path)
    {
        Double doubleValue = get(root, path, DataObject::getDouble, DataArray::getDouble);
        if (doubleValue == null)
            pathError(path, "double");
        return doubleValue;
    }

    /**
     * Parses the given {@code path} and finds the appropriate value within this {@link DataObject}.
     * <br>If the resulting value is a string, this will parse the string using {@link Double#parseDouble(String)}.
     *
     * @param  root
     *         The root data object, which is the top level accessor.
     *         <br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     *         The path of the value, in accordance with the described grammar by {@link DataPath}.
     *         This must start with a name element, such as {@code "foo"}.
     *
     * @throws ParsingException
     *         If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     *         If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     *         If null is provided or the path is empty
     *
     * @return The double value at the given path, returning the fallback if the path resolves to an optional value that is missing.
     */
    public static double getDouble(@Nonnull DataObject root, @Nonnull String path, double fallback)
    {
        Double doubleValue = get(root, path, (obj, key) -> obj.getDouble(key, fallback), (arr, index) -> arr.getDouble(index, fallback));
        return doubleValue == null ? fallback : doubleValue;
    }

    /**
     * Parses the given {@code path} and finds the appropriate value within this {@link DataArray}.
     * <br>If the resulting value is a string, this will parse the string using {@link Double#parseDouble(String)}.
     *
     * @param  root
     *         The root data array, which is the top level accessor.
     *         <br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     *         The path of the value, in accordance with the described grammar by {@link DataPath}.
     *         This must start with an index element, such as {@code "[0]"}.
     *
     * @throws ParsingException
     *         If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     *         If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     *         If null is provided or the path is empty
     *
     * @return The double value at the given path
     */
    public static double getDouble(@Nonnull DataArray root, @Nonnull String path)
    {
        Double doubleValue = get(root, path, DataObject::getDouble, DataArray::getDouble);
        if (doubleValue == null)
            pathError(path, "double");
        return doubleValue;
    }

    /**
     * Parses the given {@code path} and finds the appropriate value within this {@link DataArray}.
     * <br>If the resulting value is a string, this will parse the string using {@link Double#parseDouble(String)}.
     *
     * @param  root
     *         The root data array, which is the top level accessor.
     *         <br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     *         The path of the value, in accordance with the described grammar by {@link DataPath}.
     *         This must start with an index element, such as {@code "[0]"}.
     *
     * @throws ParsingException
     *         If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     *         If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     *         If null is provided or the path is empty
     *
     * @return The double value at the given path, returning the fallback if the path resolves to an optional value that is missing.
     */
    public static double getDouble(@Nonnull DataArray root, @Nonnull String path, double fallback)
    {
        Double doubleValue = get(root, path, (obj, key) -> obj.getDouble(key, fallback), (arr, index) -> arr.getDouble(index, fallback));
        return doubleValue == null ? fallback : doubleValue;
    }

    /**
     * Parses the given {@code path} and finds the appropriate value within this {@link DataObject}.
     *
     * @param  root
     *         The root data object, which is the top level accessor.
     *         <br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     *         The path of the value, in accordance with the described grammar by {@link DataPath}.
     *         This must start with a name element, such as {@code "foo"}.
     *
     * @throws ParsingException
     *         If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     *         If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     *         If null is provided or the path is empty
     *
     * @return The String value at the given path
     */
    @Nonnull
    public static String getString(@Nonnull DataObject root, @Nonnull String path)
    {
        String string = get(root, path, DataObject::getString, DataArray::getString);
        if (string == null)
            pathError(path, "String");
        return string;
    }

    /**
     * Parses the given {@code path} and finds the appropriate value within this {@link DataObject}.
     *
     * @param  root
     *         The root data object, which is the top level accessor.
     *         <br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     *         The path of the value, in accordance with the described grammar by {@link DataPath}.
     *         This must start with a name element, such as {@code "foo"}.
     *
     * @throws ParsingException
     *         If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     *         If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     *         If null is provided or the path is empty
     *
     * @return The String value at the given path, returning the fallback if the path resolves to an optional value that is missing.
     */
    @Contract("_, _, !null -> !null")
    public static String getString(@Nonnull DataObject root, @Nonnull String path, @Nullable String fallback)
    {
        String string = get(root, path, (obj, key) -> obj.getString(key, fallback), (arr, index) -> arr.getString(index, fallback));
        return string == null ? fallback : string;
    }

    /**
     * Parses the given {@code path} and finds the appropriate value within this {@link DataArray}.
     *
     * @param  root
     *         The root data array, which is the top level accessor.
     *         <br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     *         The path of the value, in accordance with the described grammar by {@link DataPath}.
     *         This must start with an index element, such as {@code "[0]"}.
     *
     * @throws ParsingException
     *         If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     *         If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     *         If null is provided or the path is empty
     *
     * @return The String value at the given path
     */
    @Nonnull
    public static String getString(@Nonnull DataArray root, @Nonnull String path)
    {
        String string = get(root, path, DataObject::getString, DataArray::getString);
        if (string == null)
            pathError(path, "String");
        return string;
    }

    /**
     * Parses the given {@code path} and finds the appropriate value within this {@link DataArray}.
     *
     * @param  root
     *         The root data array, which is the top level accessor.
     *         <br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     *         The path of the value, in accordance with the described grammar by {@link DataPath}.
     *         This must start with an index element, such as {@code "[0]"}.
     *
     * @throws ParsingException
     *         If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     *         If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     *         If null is provided or the path is empty
     *
     * @return The String value at the given path, returning the fallback if the path resolves to an optional value that is missing.
     */
    @Contract("_, _, !null -> !null")
    public static String getString(@Nonnull DataArray root, @Nonnull String path, @Nullable String fallback)
    {
        String string = get(root, path, (obj, key) -> obj.getString(key, fallback), (arr, index) -> arr.getString(index, fallback));
        return string == null ? fallback : string;
    }

    /**
     * Parses the given {@code path} and finds the appropriate value within this {@link DataObject}.
     *
     * @param  root
     *         The root data object, which is the top level accessor.
     *         <br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     *         The path of the value, in accordance with the described grammar by {@link DataPath}.
     *         This must start with a name element, such as {@code "foo"}.
     *
     * @throws ParsingException
     *         If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     *         If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     *         If null is provided or the path is empty
     *
     * @return The DataObject at the given path
     */
    @Nonnull
    public static DataObject getObject(@Nonnull DataObject root, @Nonnull String path)
    {
        DataObject obj = optObject(root, path);
        if (obj == null)
            pathError(path, "Object");
        return obj;
    }

    /**
     * Parses the given {@code path} and finds the appropriate value within this {@link DataObject}.
     *
     * @param  root
     *         The root data object, which is the top level accessor.
     *         <br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     *         The path of the value, in accordance with the described grammar by {@link DataPath}.
     *         This must start with a name element, such as {@code "foo"}.
     *
     * @throws ParsingException
     *         If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     *         If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     *         If null is provided or the path is empty
     *
     * @return The DataObject at the given path, or null if the path resolves to an optional value that is missing.
     */
    @Nullable
    public static DataObject optObject(@Nonnull DataObject root, @Nonnull String path)
    {
        if (!path.endsWith("?"))
            path += "?";
        return get(root, path, DataObject::getObject, DataArray::getObject);
    }

    /**
     * Parses the given {@code path} and finds the appropriate value within this {@link DataArray}.
     *
     * @param  root
     *         The root data array, which is the top level accessor.
     *         <br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     *         The path of the value, in accordance with the described grammar by {@link DataPath}.
     *         This must start with an index element, such as {@code "[0]"}.
     *
     * @throws ParsingException
     *         If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     *         If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     *         If null is provided or the path is empty
     *
     * @return The DataObject at the given path
     */
    @Nonnull
    public static DataObject getObject(@Nonnull DataArray root, @Nonnull String path)
    {
        DataObject obj = optObject(root, path);
        if (obj == null)
            pathError(path, "Object");
        return obj;
    }

    /**
     * Parses the given {@code path} and finds the appropriate value within this {@link DataArray}.
     *
     * @param  root
     *         The root data array, which is the top level accessor.
     *         <br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     *         The path of the value, in accordance with the described grammar by {@link DataPath}.
     *         This must start with an index element, such as {@code "[0]"}.
     *
     * @throws ParsingException
     *         If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     *         If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     *         If null is provided or the path is empty
     *
     * @return The DataObject at the given path, or null if the path resolves to an optional value that is missing.
     */
    @Nullable
    public static DataObject optObject(@Nonnull DataArray root, @Nonnull String path)
    {
        if (!path.endsWith("?"))
            path += "?";
        return get(root, path, DataObject::getObject, DataArray::getObject);
    }

    /**
     * Parses the given {@code path} and finds the appropriate value within this {@link DataObject}.
     *
     * @param  root
     *         The root data object, which is the top level accessor.
     *         <br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     *         The path of the value, in accordance with the described grammar by {@link DataPath}.
     *         This must start with a name element, such as {@code "foo"}.
     *
     * @throws ParsingException
     *         If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     *         If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     *         If null is provided or the path is empty
     *
     * @return The {@link DataArray} at the given path
     */
    @Nonnull
    public static DataArray getArray(@Nonnull DataObject root, @Nonnull String path)
    {
        DataArray array = optArray(root, path);
        if (array == null)
            pathError(path, "Array");
        return array;
    }

    /**
     * Parses the given {@code path} and finds the appropriate value within this {@link DataObject}.
     *
     * @param  root
     *         The root data object, which is the top level accessor.
     *         <br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     *         The path of the value, in accordance with the described grammar by {@link DataPath}.
     *         This must start with a name element, such as {@code "foo"}.
     *
     * @throws ParsingException
     *         If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     *         If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     *         If null is provided or the path is empty
     *
     * @return The {@link DataArray} at the given path, or null if the path resolves to an optional value that is missing.
     */
    @Nullable
    public static DataArray optArray(@Nonnull DataObject root, @Nonnull String path)
    {
        if (!path.endsWith("?"))
            path += "?";
        return get(root, path, DataObject::getArray, DataArray::getArray);
    }

    /**
     * Parses the given {@code path} and finds the appropriate value within this {@link DataArray}.
     *
     * @param  root
     *         The root data array, which is the top level accessor.
     *         <br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     *         The path of the value, in accordance with the described grammar by {@link DataPath}.
     *         This must start with an index element, such as {@code "[0]"}.
     *
     * @throws ParsingException
     *         If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     *         If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     *         If null is provided or the path is empty
     *
     * @return The {@link DataArray} at the given path
     */
    @Nonnull
    public static DataArray getArray(@Nonnull DataArray root, @Nonnull String path)
    {
        DataArray array = optArray(root, path);
        if (array == null)
            pathError(path, "Array");
        return array;
    }

    /**
     * Parses the given {@code path} and finds the appropriate value within this {@link DataArray}.
     *
     * @param  root
     *         The root data array, which is the top level accessor.
     *         <br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     *         The path of the value, in accordance with the described grammar by {@link DataPath}.
     *         This must start with an index element, such as {@code "[0]"}.
     *
     * @throws ParsingException
     *         If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     *         If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     *         If null is provided or the path is empty
     *
     * @return The {@link DataArray} at the given path, or null if the path resolves to an optional value that is missing.
     */
    @Nullable
    public static DataArray optArray(@Nonnull DataArray root, @Nonnull String path)
    {
        if (!path.endsWith("?"))
            path += "?";
        return get(root, path, DataObject::getArray, DataArray::getArray);
    }

    private static ParsingException pathError(String path, String type)
    {
        throw new ParsingException("Could not resolve value of type " + type + " at path \"" + path + "\"");
    }
}
