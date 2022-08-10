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

// TODO: Documentation and Unit Tests
public class DataPath
{
    private static final Pattern INDEX_EXPRESSION = Pattern.compile("^\\[\\d+].*");

    public static <T> T get(@Nonnull DataObject root, @Nonnull String path, @Nonnull BiFunction<DataObject, String, ? extends T> fromObject, @Nonnull BiFunction<DataArray, Integer, ? extends T> fromArray)
    {
        Checks.notEmpty(path, "Path");
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
            path = current.substring(arrayIndex);

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

            if (optional)
            {
                offset++;
                if (root.isNull(index))
                    return null;
            }

            if (offset == chars.length)
                return optional && root.isNull(index) ? null : fromArray.apply(root, index);

            if (chars[offset] == '[')
                root = root.getArray(index);
            else
                return getUnchecked(root.getObject(index), path.substring(offset), fromObject, fromArray);
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

    public static boolean getBoolean(@Nonnull DataObject root, @Nonnull String path)
    {
        Boolean bool = get(root, path, DataObject::getBoolean, DataArray::getBoolean);
        return bool != null && bool;
    }

    public static boolean getBoolean(@Nonnull DataObject root, @Nonnull String path, boolean fallback)
    {
        Boolean bool = get(root, path, (obj, key) -> obj.getBoolean(key, fallback), (arr, index) -> arr.getBoolean(index, fallback));
        return bool != null ? bool : fallback;
    }

    public static int getInt(@Nonnull DataObject root, @Nonnull String path)
    {
        Integer integer = get(root, path, DataObject::getInt, DataArray::getInt);
        if (integer == null)
            pathError(path, "int");
        return integer;
    }

    public static int getInt(@Nonnull DataObject root, @Nonnull String path, int fallback)
    {
        Integer integer = get(root, path, (obj, key) -> obj.getInt(key, fallback), (arr, index) -> arr.getInt(index, fallback));
        return integer == null ? fallback : integer;
    }

    public static int getUnsignedInt(@Nonnull DataObject root, @Nonnull String path)
    {
        Integer integer = get(root, path, DataObject::getUnsignedInt, DataArray::getUnsignedInt);
        if (integer == null)
            pathError(path, "unsigned int");
        return integer;
    }

    public static int getUnsignedInt(@Nonnull DataObject root, @Nonnull String path, int fallback)
    {
        Integer integer = get(root, path, (obj, key) -> obj.getUnsignedInt(key, fallback), (arr, index) -> arr.getUnsignedInt(index, fallback));
        return integer == null ? fallback : integer;
    }

    public static long getLong(@Nonnull DataObject root, @Nonnull String path)
    {
        Long longValue = get(root, path, DataObject::getLong, DataArray::getLong);
        if (longValue == null)
            pathError(path, "long");
        return longValue;
    }

    public static long getLong(@Nonnull DataObject root, @Nonnull String path, long fallback)
    {
        Long longValue = get(root, path, (obj, key) -> obj.getLong(key, fallback), (arr, index) -> arr.getLong(index, fallback));
        return longValue == null ? fallback : longValue;
    }

    public static long getUnsignedLong(@Nonnull DataObject root, @Nonnull String path)
    {
        Long longValue = get(root, path, DataObject::getUnsignedLong, DataArray::getUnsignedLong);
        if (longValue == null)
            throw pathError(path, "unsigned long");
        return longValue;
    }

    public static long getUnsignedLong(@Nonnull DataObject root, @Nonnull String path, long fallback)
    {
        Long longValue = get(root, path, (obj, key) -> obj.getUnsignedLong(key, fallback), (arr, index) -> arr.getUnsignedLong(index, fallback));
        return longValue == null ? fallback : longValue;
    }

    public static double getDouble(@Nonnull DataObject root, @Nonnull String path)
    {
        Double doubleValue = get(root, path, DataObject::getDouble, DataArray::getDouble);
        if (doubleValue == null)
            pathError(path, "double");
        return doubleValue;
    }

    public static double getDouble(@Nonnull DataObject root, @Nonnull String path, double fallback)
    {
        Double doubleValue = get(root, path, (obj, key) -> obj.getDouble(key, fallback), (arr, index) -> arr.getDouble(index, fallback));
        return doubleValue == null ? fallback : doubleValue;
    }

    @Nonnull
    public static String getString(@Nonnull DataObject root, @Nonnull String path)
    {
        String string = get(root, path, DataObject::getString, DataArray::getString);
        if (string == null)
            pathError(path, "String");
        return string;
    }

    @Contract("_, _, !null -> !null")
    public static String getString(@Nonnull DataObject root, @Nonnull String path, @Nullable String fallback)
    {
        String string = get(root, path, (obj, key) -> obj.getString(key, fallback), (arr, index) -> arr.getString(index, fallback));
        return string == null ? fallback : string;
    }

    @Nonnull
    public static DataObject getObject(@Nonnull DataObject root, @Nonnull String path)
    {
        DataObject obj = optObject(root, path);
        if (obj == null)
            pathError(path, "Object");
        return obj;
    }

    @Nullable
    public static DataObject optObject(@Nonnull DataObject root, @Nonnull String path)
    {
        if (!path.endsWith("?"))
            path += "?";
        return get(root, path, DataObject::getObject, DataArray::getObject);
    }

    @Nonnull
    public static DataArray getArray(@Nonnull DataObject root, @Nonnull String path)
    {
        DataArray array = optArray(root, path);
        if (array == null)
            pathError(path, "Array");
        return array;
    }

    @Nullable
    public static DataArray optArray(@Nonnull DataObject root, @Nonnull String path)
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
