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
package net.dv8tion.jda.api.utils.data

import net.dv8tion.jda.api.exceptions.ParsingException
import net.dv8tion.jda.internal.utils.Checks
import org.jetbrains.annotations.Contract
import java.nio.charset.StandardCharsets
import java.util.function.BiFunction
import java.util.regex.Pattern
import javax.annotation.Nonnull
import kotlin.math.min

/**
 * This utility class can be used to access nested values within [DataObjects][DataObject] and [DataArrays][DataArray].
 *
 *
 * **Path Expression Grammar**<br></br>
 *
 * The syntax for paths is given by this grammar:
 *
 * <pre>`<name-syntax>  ::= /[^.\[\]]+/;
 * <index-syntax> ::= "[" <number> "]";
 * <name>         ::= <name-syntax> | <name-syntax> "?";
 * <index>        ::= <index-syntax> | <index-syntax> "?";
 * <element>      ::= <name> ( <index> )*;
 * <path-start>   ::= <element> | <index> ( <index> )*;
 * <path>         ::= <path-start> ( "." <element> )*;
`</pre> *
 *
 *
 * **Examples**<br></br>
 * Given a JSON object such as:
 * <pre>`{
 * "array": [{
 * "foo": "bar",
 * }]
 * }
`</pre> *
 *
 * The content of `"foo"` can be accessed using the code:
 * <pre>`String foo = DataPath.getString(root, "array[0].foo")`</pre>
 *
 *
 * With the safe-access operator `"?"`, you can also allow missing values within your path:
 * <pre>`String foo = DataPath.getString(root, "array[1]?.foo", "default")`</pre>
 * This will result in `foo == "default"`, since the array element 1 is marked as optional, and missing in the actual object.
 */
object DataPath {
    private val INDEX_EXPRESSION = Pattern.compile("^\\[\\d+].*")
    private val NAME_EXPRESSION = Pattern.compile("^[^\\[.].*")

    /**
     * Parses the given `path` and finds the appropriate value within this [DataObject].
     *
     * @param  <T>
     * The result type
     *
     * @param  root
     * The root data object, which is the top level accessor.
     * <br></br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     * The path of the value, in accordance with the described grammar by [DataPath].
     * This must start with a name element, such as `"foo"`.
     * @param  fromObject
     * Object relative resolver of the value, this is used for the final reference and resolves the value.
     * The first parameter is the [DataObject] where you get the value from, and the second is the field name.
     * An example would be `(obj, name) -> obj.getString(name)` or as a method reference `DataObject::getString`.
     * @param  fromArray
     * Array relative resolver of the value, this is used for the final reference and resolves the value.
     * The first parameter is the [DataArray] where you get the value from, and the second is the field index.
     * An example would be `(array, index) -> obj.getString(index)` or as a method reference `DataArray::getString`.
     *
     * @throws ParsingException
     * If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     * If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     * If null is provided or the path is empty
     *
     * @return The value at the provided path, using the provided resolver functions.
     * Possibly null, if the path ends with a "?" operator, or the resolver function returns null.
    </T> */
    operator fun <T> get(
        @Nonnull root: DataObject?,
        @Nonnull path: String,
        @Nonnull fromObject: BiFunction<DataObject?, String, out T>,
        @Nonnull fromArray: BiFunction<DataArray?, Int, out T>
    ): T {
        Checks.notEmpty(path, "Path")
        Checks.matches(path, NAME_EXPRESSION, "Path")
        Checks.notNull(root, "DataObject")
        Checks.notNull(fromObject, "Object Resolver")
        Checks.notNull(fromArray, "Array Resolver")
        return getUnchecked(root, path, fromObject, fromArray)
    }

    private fun <T> getUnchecked(
        root: DataObject?,
        path: String,
        fromObject: BiFunction<DataObject?, String, out T>,
        fromArray: BiFunction<DataArray?, Int, out T>
    ): T? {
        var path = path
        val parts = path.split("\\.".toRegex(), limit = 2).toTypedArray()
        var current = parts[0]
        val child = if (parts.size > 1) parts[1] else null

        // if key is array according to index in path
        if (current.indexOf('[') > -1) {
            val arrayIndex = current.indexOf('[')
            var key = current.substring(0, arrayIndex)
            path = path.substring(arrayIndex)

            // isOptional
            if (key.endsWith("?")) {
                key = key.substring(0, key.length - 1)
                if (root!!.isNull(key)) return null
            }
            return getUnchecked(root!!.getArray(key), path, fromObject, fromArray)
        }
        val isOptional = current.endsWith("?")
        if (isOptional) current = current.substring(0, current.length - 1)
        if (child == null) {
            return if (isOptional && root!!.isNull(current)) null else fromObject.apply(root, current)
        }
        return if (isOptional && root!!.isNull(current)) null else getUnchecked(
            root!!.getObject(current),
            child,
            fromObject,
            fromArray
        )
    }

    /**
     * Parses the given `path` and finds the appropriate value within this [DataArray].
     *
     * @param  <T>
     * The result type
     *
     * @param  root
     * The root data array, which is the top level accessor.
     * <br></br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     * The path of the value, in accordance with the described grammar by [DataPath].
     * This must start with an index element, such as `"[0]"`.
     * @param  fromObject
     * Object relative resolver of the value, this is used for the final reference and resolves the value.
     * The first parameter is the [DataObject] where you get the value from, and the second is the field name.
     * An example would be `(obj, name) -> obj.getString(name)` or as a method reference `DataObject::getString`.
     * @param  fromArray
     * Array relative resolver of the value, this is used for the final reference and resolves the value.
     * The first parameter is the [DataArray] where you get the value from, and the second is the field index.
     * An example would be `(array, index) -> obj.getString(index)` or as a method reference `DataArray::getString`.
     *
     * @throws ParsingException
     * If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     * If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     * If null is provided or the path is empty
     *
     * @return The value at the provided path, using the provided resolver functions.
     * Possibly null, if the path ends with a "?" operator, or the resolver function returns null.
    </T> */
    operator fun <T> get(
        @Nonnull root: DataArray?,
        @Nonnull path: String,
        @Nonnull fromObject: BiFunction<DataObject?, String, out T>,
        @Nonnull fromArray: BiFunction<DataArray?, Int, out T>
    ): T {
        Checks.notNull(root, "DataArray")
        Checks.notEmpty(path, "Path")
        Checks.matches(path, INDEX_EXPRESSION, "Path")
        Checks.notNull(fromObject, "Object Resolver")
        Checks.notNull(fromArray, "Array Resolver")
        return getUnchecked(root, path, fromObject, fromArray)
    }

    private fun <T> getUnchecked(
        root: DataArray?,
        path: String,
        fromObject: BiFunction<DataObject?, String, out T>,
        fromArray: BiFunction<DataArray?, Int, out T>
    ): T? {
        var root = root
        val chars = path.toByteArray(StandardCharsets.UTF_8)
        var offset = 0

        // This is just to prevent infinite loop if some strange thing happens, should be impossible
        for (i in chars.indices) {
            val end = indexOf(chars, offset + 1, ']')
            val index = path.substring(offset + 1, end).toInt()
            offset = min(chars.size.toDouble(), (end + 1).toDouble()).toInt()
            val optional = offset != chars.size && chars[offset] == '?'.code.toByte()
            val isMissing = root!!.length() <= index || root.isNull(index)
            if (optional) {
                offset++
                if (isMissing) return null
            }
            if (offset == chars.size) return fromArray.apply(root, index)
            root = if (chars[offset] == '['.code.toByte()) root.getArray(index) else return getUnchecked(
                root.getObject(index), path.substring(offset + 1), fromObject, fromArray
            )
        }
        throw ParsingException("Array path nesting seems to be way too deep, we went " + chars.size + " arrays deep. Path: " + path)
    }

    private fun indexOf(chars: ByteArray, offset: Int, c: Char): Int {
        val b = c.code.toByte()
        for (i in offset until chars.size) if (chars[i] == b) return i
        return -1
    }

    /**
     * Parses the given `path` and finds the appropriate value within this [DataObject].
     *
     * @param  root
     * The root data object, which is the top level accessor.
     * <br></br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     * The path of the value, in accordance with the described grammar by [DataPath].
     * This must start with a name element, such as `"foo"`.
     *
     * @throws ParsingException
     * If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     * If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     * If null is provided or the path is empty
     *
     * @return The boolean value at the given path, if declared as optional this returns false when the value is missing.
     */
    fun getBoolean(@Nonnull root: DataObject?, @Nonnull path: String): Boolean {
        val bool = get(
            root,
            path,
            { obj: DataObject?, key: String -> obj!!.getBoolean(key) }) { obj: DataArray?, index: Int ->
            obj!!.getBoolean(
                index
            )
        }
        return bool != null && bool
    }

    /**
     * Parses the given `path` and finds the appropriate value within this [DataObject].
     *
     * @param  root
     * The root data object, which is the top level accessor.
     * <br></br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     * The path of the value, in accordance with the described grammar by [DataPath].
     * This must start with a name element, such as `"foo"`.
     *
     * @throws ParsingException
     * If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     * If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     * If null is provided or the path is empty
     *
     * @return The boolean value at the given path, if declared as optional this returns the provided fallback when the value is missing.
     */
    fun getBoolean(@Nonnull root: DataObject?, @Nonnull path: String, fallback: Boolean): Boolean {
        val bool = get(
            root,
            path,
            { obj: DataObject?, key: String ->
                obj!!.getBoolean(
                    key,
                    fallback
                )
            }) { arr: DataArray?, index: Int -> arr!!.getBoolean(index, fallback) }
        return bool ?: fallback
    }

    /**
     * Parses the given `path` and finds the appropriate value within this [DataArray].
     *
     * @param  root
     * The root data array, which is the top level accessor.
     * <br></br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     * The path of the value, in accordance with the described grammar by [DataPath].
     * This must start with an index element, such as `"[0]"`.
     *
     * @throws ParsingException
     * If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     * If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     * If null is provided or the path is empty
     *
     * @return The boolean value at the given path, if declared as optional this returns false when the value is missing.
     */
    fun getBoolean(@Nonnull root: DataArray?, @Nonnull path: String): Boolean {
        val bool = get(
            root,
            path,
            { obj: DataObject?, key: String -> obj!!.getBoolean(key) }) { obj: DataArray?, index: Int ->
            obj!!.getBoolean(
                index
            )
        }
        return bool != null && bool
    }

    /**
     * Parses the given `path` and finds the appropriate value within this [DataArray].
     *
     * @param  root
     * The root data array, which is the top level accessor.
     * <br></br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     * The path of the value, in accordance with the described grammar by [DataPath].
     * This must start with an index element, such as `"[0]"`.
     *
     * @throws ParsingException
     * If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     * If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     * If null is provided or the path is empty
     *
     * @return The boolean value at the given path, if declared as optional this returns the provided fallback when the value is missing.
     */
    @JvmStatic
    fun getBoolean(@Nonnull root: DataArray?, @Nonnull path: String, fallback: Boolean): Boolean {
        val bool = get(
            root,
            path,
            { obj: DataObject?, key: String ->
                obj!!.getBoolean(
                    key,
                    fallback
                )
            }) { arr: DataArray?, index: Int -> arr!!.getBoolean(index, fallback) }
        return bool ?: fallback
    }

    /**
     * Parses the given `path` and finds the appropriate value within this [DataObject].
     * <br></br>If the resulting value is a string, this will parse the string using [Integer.parseInt].
     *
     * @param  root
     * The root data object, which is the top level accessor.
     * <br></br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     * The path of the value, in accordance with the described grammar by [DataPath].
     * This must start with a name element, such as `"foo"`.
     *
     * @throws ParsingException
     * If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     * If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     * If null is provided or the path is empty
     *
     * @return The int value at the given path
     */
    @JvmStatic
    fun getInt(@Nonnull root: DataObject?, @Nonnull path: String): Int {
        val integer = get(
            root,
            path,
            { obj: DataObject?, key: String -> obj!!.getInt(key) }) { obj: DataArray?, index: Int -> obj!!.getInt(index) }
        if (integer == null) pathError(path, "int")
        return integer
    }

    /**
     * Parses the given `path` and finds the appropriate value within this [DataObject].
     * <br></br>If the resulting value is a string, this will parse the string using [Integer.parseInt].
     *
     * @param  root
     * The root data object, which is the top level accessor.
     * <br></br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     * The path of the value, in accordance with the described grammar by [DataPath].
     * This must start with a name element, such as `"foo"`.
     *
     * @throws ParsingException
     * If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     * If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     * If null is provided or the path is empty
     *
     * @return The int value at the given path, returning the fallback if the path resolves to an optional value that is missing.
     */
    fun getInt(@Nonnull root: DataObject?, @Nonnull path: String, fallback: Int): Int {
        val integer = get(
            root,
            path,
            { obj: DataObject?, key: String ->
                obj!!.getInt(
                    key,
                    fallback
                )
            }) { arr: DataArray?, index: Int -> arr!!.getInt(index, fallback) }
        return integer ?: fallback
    }

    /**
     * Parses the given `path` and finds the appropriate value within this [DataArray].
     * <br></br>If the resulting value is a string, this will parse the string using [Integer.parseInt].
     *
     * @param  root
     * The root data array, which is the top level accessor.
     * <br></br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     * The path of the value, in accordance with the described grammar by [DataPath].
     * This must start with an index element, such as `"[0]"`.
     *
     * @throws ParsingException
     * If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     * If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     * If null is provided or the path is empty
     *
     * @return The int value at the given path
     */
    @JvmStatic
    fun getInt(@Nonnull root: DataArray?, @Nonnull path: String): Int {
        val integer = get(
            root,
            path,
            { obj: DataObject?, key: String -> obj!!.getInt(key) }) { obj: DataArray?, index: Int -> obj!!.getInt(index) }
        if (integer == null) pathError(path, "int")
        return integer
    }

    /**
     * Parses the given `path` and finds the appropriate value within this [DataArray].
     * <br></br>If the resulting value is a string, this will parse the string using [Integer.parseInt].
     *
     * @param  root
     * The root data array, which is the top level accessor.
     * <br></br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     * The path of the value, in accordance with the described grammar by [DataPath].
     * This must start with an index element, such as `"[0]"`.
     *
     * @throws ParsingException
     * If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     * If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     * If null is provided or the path is empty
     *
     * @return The int value at the given path, returning the fallback if the path resolves to an optional value that is missing.
     */
    fun getInt(@Nonnull root: DataArray?, @Nonnull path: String, fallback: Int): Int {
        val integer = get(
            root,
            path,
            { obj: DataObject?, key: String ->
                obj!!.getInt(
                    key,
                    fallback
                )
            }) { arr: DataArray?, index: Int -> arr!!.getInt(index, fallback) }
        return integer ?: fallback
    }

    /**
     * Parses the given `path` and finds the appropriate value within this [DataObject].
     * <br></br>If the resulting value is a string, this will parse the string using [Integer.parseUnsignedInt].
     *
     * @param  root
     * The root data object, which is the top level accessor.
     * <br></br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     * The path of the value, in accordance with the described grammar by [DataPath].
     * This must start with a name element, such as `"foo"`.
     *
     * @throws ParsingException
     * If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     * If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     * If null is provided or the path is empty
     *
     * @return The unsigned int value at the given path
     */
    fun getUnsignedInt(@Nonnull root: DataObject?, @Nonnull path: String): Int {
        val integer = get(
            root,
            path,
            { obj: DataObject?, key: String -> obj!!.getUnsignedInt(key) }) { obj: DataArray?, index: Int ->
            obj!!.getUnsignedInt(
                index
            )
        }
        if (integer == null) pathError(path, "unsigned int")
        return integer
    }

    /**
     * Parses the given `path` and finds the appropriate value within this [DataObject].
     * <br></br>If the resulting value is a string, this will parse the string using [Integer.parseUnsignedInt].
     *
     * @param  root
     * The root data object, which is the top level accessor.
     * <br></br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     * The path of the value, in accordance with the described grammar by [DataPath].
     * This must start with a name element, such as `"foo"`.
     *
     * @throws ParsingException
     * If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     * If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     * If null is provided or the path is empty
     *
     * @return The unsigned int value at the given path, returning the fallback if the path resolves to an optional value that is missing.
     */
    fun getUnsignedInt(@Nonnull root: DataObject?, @Nonnull path: String, fallback: Int): Int {
        val integer = get(
            root,
            path,
            { obj: DataObject?, key: String ->
                obj!!.getUnsignedInt(
                    key,
                    fallback
                )
            }) { arr: DataArray?, index: Int -> arr!!.getUnsignedInt(index, fallback) }
        return integer ?: fallback
    }

    /**
     * Parses the given `path` and finds the appropriate value within this [DataArray].
     * <br></br>If the resulting value is a string, this will parse the string using [Integer.parseUnsignedInt].
     *
     * @param  root
     * The root data array, which is the top level accessor.
     * <br></br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     * The path of the value, in accordance with the described grammar by [DataPath].
     * This must start with an index element, such as `"[0]"`.
     *
     * @throws ParsingException
     * If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     * If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     * If null is provided or the path is empty
     *
     * @return The unsigned int value at the given path
     */
    @JvmStatic
    fun getUnsignedInt(@Nonnull root: DataArray?, @Nonnull path: String): Int {
        val integer = get(
            root,
            path,
            { obj: DataObject?, key: String -> obj!!.getUnsignedInt(key) }) { obj: DataArray?, index: Int ->
            obj!!.getUnsignedInt(
                index
            )
        }
        if (integer == null) pathError(path, "unsigned int")
        return integer
    }

    /**
     * Parses the given `path` and finds the appropriate value within this [DataArray].
     * <br></br>If the resulting value is a string, this will parse the string using [Integer.parseUnsignedInt].
     *
     * @param  root
     * The root data array, which is the top level accessor.
     * <br></br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     * The path of the value, in accordance with the described grammar by [DataPath].
     * This must start with an index element, such as `"[0]"`.
     *
     * @throws ParsingException
     * If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     * If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     * If null is provided or the path is empty
     *
     * @return The unsigned int value at the given path, returning the fallback if the path resolves to an optional value that is missing.
     */
    @JvmStatic
    fun getUnsignedInt(@Nonnull root: DataArray?, @Nonnull path: String, fallback: Int): Int {
        val integer = get(
            root,
            path,
            { obj: DataObject?, key: String ->
                obj!!.getUnsignedInt(
                    key,
                    fallback
                )
            }) { arr: DataArray?, index: Int -> arr!!.getUnsignedInt(index, fallback) }
        return integer ?: fallback
    }

    /**
     * Parses the given `path` and finds the appropriate value within this [DataObject].
     * <br></br>If the resulting value is a string, this will parse the string using [Long.parseLong].
     *
     * @param  root
     * The root data object, which is the top level accessor.
     * <br></br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     * The path of the value, in accordance with the described grammar by [DataPath].
     * This must start with a name element, such as `"foo"`.
     *
     * @throws ParsingException
     * If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     * If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     * If null is provided or the path is empty
     *
     * @return The long value at the given path
     */
    @JvmStatic
    fun getLong(@Nonnull root: DataObject?, @Nonnull path: String): Long {
        val longValue = get(
            root,
            path,
            { obj: DataObject?, key: String -> obj!!.getLong(key) }) { obj: DataArray?, index: Int ->
            obj!!.getLong(
                index
            )
        }
        if (longValue == null) pathError(path, "long")
        return longValue
    }

    /**
     * Parses the given `path` and finds the appropriate value within this [DataObject].
     * <br></br>If the resulting value is a string, this will parse the string using [Long.parseLong].
     *
     * @param  root
     * The root data object, which is the top level accessor.
     * <br></br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     * The path of the value, in accordance with the described grammar by [DataPath].
     * This must start with a name element, such as `"foo"`.
     *
     * @throws ParsingException
     * If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     * If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     * If null is provided or the path is empty
     *
     * @return The long value at the given path, returning the fallback if the path resolves to an optional value that is missing.
     */
    @JvmStatic
    fun getLong(@Nonnull root: DataObject?, @Nonnull path: String, fallback: Long): Long {
        val longValue = get(
            root,
            path,
            { obj: DataObject?, key: String ->
                obj!!.getLong(
                    key,
                    fallback
                )
            }) { arr: DataArray?, index: Int -> arr!!.getLong(index, fallback) }
        return longValue ?: fallback
    }

    /**
     * Parses the given `path` and finds the appropriate value within this [DataArray].
     * <br></br>If the resulting value is a string, this will parse the string using [Long.parseLong].
     *
     * @param  root
     * The root data array, which is the top level accessor.
     * <br></br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     * The path of the value, in accordance with the described grammar by [DataPath].
     * This must start with an index element, such as `"[0]"`.
     *
     * @throws ParsingException
     * If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     * If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     * If null is provided or the path is empty
     *
     * @return The long value at the given path
     */
    fun getLong(@Nonnull root: DataArray?, @Nonnull path: String): Long {
        val longValue = get(
            root,
            path,
            { obj: DataObject?, key: String -> obj!!.getLong(key) }) { obj: DataArray?, index: Int ->
            obj!!.getLong(
                index
            )
        }
        if (longValue == null) pathError(path, "long")
        return longValue
    }

    /**
     * Parses the given `path` and finds the appropriate value within this [DataArray].
     * <br></br>If the resulting value is a string, this will parse the string using [Long.parseLong].
     *
     * @param  root
     * The root data array, which is the top level accessor.
     * <br></br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     * The path of the value, in accordance with the described grammar by [DataPath].
     * This must start with an index element, such as `"[0]"`.
     *
     * @throws ParsingException
     * If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     * If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     * If null is provided or the path is empty
     *
     * @return The long value at the given path, returning the fallback if the path resolves to an optional value that is missing.
     */
    fun getLong(@Nonnull root: DataArray?, @Nonnull path: String, fallback: Long): Long {
        val longValue = get(
            root,
            path,
            { obj: DataObject?, key: String ->
                obj!!.getLong(
                    key,
                    fallback
                )
            }) { arr: DataArray?, index: Int -> arr!!.getLong(index, fallback) }
        return longValue ?: fallback
    }

    /**
     * Parses the given `path` and finds the appropriate value within this [DataObject].
     * <br></br>If the resulting value is a string, this will parse the string using [Long.parseUnsignedLong].
     *
     * @param  root
     * The root data object, which is the top level accessor.
     * <br></br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     * The path of the value, in accordance with the described grammar by [DataPath].
     * This must start with a name element, such as `"foo"`.
     *
     * @throws ParsingException
     * If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     * If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     * If null is provided or the path is empty
     *
     * @return The unsigned long value at the given path
     */
    fun getUnsignedLong(
        @Nonnull root: DataObject?,
        @Nonnull path: String
    ): Long {
        return get(
            root,
            path,
            { obj: DataObject?, key: String -> obj!!.getUnsignedLong(key) }) { obj: DataArray?, index: Int ->
            obj!!.getUnsignedLong(
                index
            )
        }
            ?: throw pathError(path, "unsigned long")
    }

    /**
     * Parses the given `path` and finds the appropriate value within this [DataObject].
     * <br></br>If the resulting value is a string, this will parse the string using [Long.parseUnsignedLong].
     *
     * @param  root
     * The root data object, which is the top level accessor.
     * <br></br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     * The path of the value, in accordance with the described grammar by [DataPath].
     * This must start with a name element, such as `"foo"`.
     *
     * @throws ParsingException
     * If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     * If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     * If null is provided or the path is empty
     *
     * @return The unsigned long value at the given path, returning the fallback if the path resolves to an optional value that is missing.
     */
    fun getUnsignedLong(@Nonnull root: DataObject?, @Nonnull path: String, fallback: Long): Long {
        val longValue = get(
            root,
            path,
            { obj: DataObject?, key: String ->
                obj!!.getUnsignedLong(
                    key,
                    fallback
                )
            }) { arr: DataArray?, index: Int -> arr!!.getUnsignedLong(index, fallback) }
        return longValue ?: fallback
    }

    /**
     * Parses the given `path` and finds the appropriate value within this [DataArray].
     * <br></br>If the resulting value is a string, this will parse the string using [Long.parseUnsignedLong].
     *
     * @param  root
     * The root data array, which is the top level accessor.
     * <br></br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     * The path of the value, in accordance with the described grammar by [DataPath].
     * This must start with an index element, such as `"[0]"`.
     *
     * @throws ParsingException
     * If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     * If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     * If null is provided or the path is empty
     *
     * @return The unsigned long value at the given path
     */
    fun getUnsignedLong(
        @Nonnull root: DataArray?,
        @Nonnull path: String
    ): Long {
        return get(
            root,
            path,
            { obj: DataObject?, key: String -> obj!!.getUnsignedLong(key) }) { obj: DataArray?, index: Int ->
            obj!!.getUnsignedLong(
                index
            )
        }
            ?: throw pathError(path, "unsigned long")
    }

    /**
     * Parses the given `path` and finds the appropriate value within this [DataArray].
     * <br></br>If the resulting value is a string, this will parse the string using [Long.parseUnsignedLong].
     *
     * @param  root
     * The root data array, which is the top level accessor.
     * <br></br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     * The path of the value, in accordance with the described grammar by [DataPath].
     * This must start with an index element, such as `"[0]"`.
     *
     * @throws ParsingException
     * If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     * If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     * If null is provided or the path is empty
     *
     * @return The unsigned long value at the given path, returning the fallback if the path resolves to an optional value that is missing.
     */
    fun getUnsignedLong(@Nonnull root: DataArray?, @Nonnull path: String, fallback: Long): Long {
        val longValue = get(
            root,
            path,
            { obj: DataObject?, key: String ->
                obj!!.getUnsignedLong(
                    key,
                    fallback
                )
            }) { arr: DataArray?, index: Int -> arr!!.getUnsignedLong(index, fallback) }
        return longValue ?: fallback
    }

    /**
     * Parses the given `path` and finds the appropriate value within this [DataObject].
     * <br></br>If the resulting value is a string, this will parse the string using [Double.parseDouble].
     *
     * @param  root
     * The root data object, which is the top level accessor.
     * <br></br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     * The path of the value, in accordance with the described grammar by [DataPath].
     * This must start with a name element, such as `"foo"`.
     *
     * @throws ParsingException
     * If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     * If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     * If null is provided or the path is empty
     *
     * @return The double value at the given path
     */
    fun getDouble(@Nonnull root: DataObject?, @Nonnull path: String): Double {
        val doubleValue = get(
            root,
            path,
            { obj: DataObject?, key: String -> obj!!.getDouble(key) }) { obj: DataArray?, index: Int ->
            obj!!.getDouble(
                index
            )
        }
        if (doubleValue == null) pathError(path, "double")
        return doubleValue
    }

    /**
     * Parses the given `path` and finds the appropriate value within this [DataObject].
     * <br></br>If the resulting value is a string, this will parse the string using [Double.parseDouble].
     *
     * @param  root
     * The root data object, which is the top level accessor.
     * <br></br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     * The path of the value, in accordance with the described grammar by [DataPath].
     * This must start with a name element, such as `"foo"`.
     *
     * @throws ParsingException
     * If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     * If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     * If null is provided or the path is empty
     *
     * @return The double value at the given path, returning the fallback if the path resolves to an optional value that is missing.
     */
    fun getDouble(@Nonnull root: DataObject?, @Nonnull path: String, fallback: Double): Double {
        val doubleValue = get(
            root,
            path,
            { obj: DataObject?, key: String ->
                obj!!.getDouble(
                    key,
                    fallback
                )
            }) { arr: DataArray?, index: Int -> arr!!.getDouble(index, fallback) }
        return doubleValue ?: fallback
    }

    /**
     * Parses the given `path` and finds the appropriate value within this [DataArray].
     * <br></br>If the resulting value is a string, this will parse the string using [Double.parseDouble].
     *
     * @param  root
     * The root data array, which is the top level accessor.
     * <br></br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     * The path of the value, in accordance with the described grammar by [DataPath].
     * This must start with an index element, such as `"[0]"`.
     *
     * @throws ParsingException
     * If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     * If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     * If null is provided or the path is empty
     *
     * @return The double value at the given path
     */
    @JvmStatic
    fun getDouble(@Nonnull root: DataArray?, @Nonnull path: String): Double {
        val doubleValue = get(
            root,
            path,
            { obj: DataObject?, key: String -> obj!!.getDouble(key) }) { obj: DataArray?, index: Int ->
            obj!!.getDouble(
                index
            )
        }
        if (doubleValue == null) pathError(path, "double")
        return doubleValue
    }

    /**
     * Parses the given `path` and finds the appropriate value within this [DataArray].
     * <br></br>If the resulting value is a string, this will parse the string using [Double.parseDouble].
     *
     * @param  root
     * The root data array, which is the top level accessor.
     * <br></br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     * The path of the value, in accordance with the described grammar by [DataPath].
     * This must start with an index element, such as `"[0]"`.
     *
     * @throws ParsingException
     * If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     * If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     * If null is provided or the path is empty
     *
     * @return The double value at the given path, returning the fallback if the path resolves to an optional value that is missing.
     */
    @JvmStatic
    fun getDouble(@Nonnull root: DataArray?, @Nonnull path: String, fallback: Double): Double {
        val doubleValue = get(
            root,
            path,
            { obj: DataObject?, key: String ->
                obj!!.getDouble(
                    key,
                    fallback
                )
            }) { arr: DataArray?, index: Int -> arr!!.getDouble(index, fallback) }
        return doubleValue ?: fallback
    }

    /**
     * Parses the given `path` and finds the appropriate value within this [DataObject].
     *
     * @param  root
     * The root data object, which is the top level accessor.
     * <br></br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     * The path of the value, in accordance with the described grammar by [DataPath].
     * This must start with a name element, such as `"foo"`.
     *
     * @throws ParsingException
     * If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     * If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     * If null is provided or the path is empty
     *
     * @return The String value at the given path
     */
    @JvmStatic
    @Nonnull
    fun getString(@Nonnull root: DataObject?, @Nonnull path: String): String? {
        val string = get<String?>(
            root,
            path,
            { obj: DataObject?, key: String -> obj!!.getString(key) }) { obj: DataArray?, index: Int ->
            obj!!.getString(
                index
            )
        }
        if (string == null) pathError(path, "String")
        return string
    }

    /**
     * Parses the given `path` and finds the appropriate value within this [DataObject].
     *
     * @param  root
     * The root data object, which is the top level accessor.
     * <br></br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     * The path of the value, in accordance with the described grammar by [DataPath].
     * This must start with a name element, such as `"foo"`.
     *
     * @throws ParsingException
     * If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     * If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     * If null is provided or the path is empty
     *
     * @return The String value at the given path, returning the fallback if the path resolves to an optional value that is missing.
     */
    @JvmStatic
    @Contract("_, _, !null -> !null")
    fun getString(@Nonnull root: DataObject?, @Nonnull path: String, fallback: String?): String? {
        val string = get(
            root,
            path,
            { obj: DataObject?, key: String ->
                obj!!.getString(
                    key,
                    fallback
                )
            }) { arr: DataArray?, index: Int -> arr!!.getString(index, fallback) }
        return string ?: fallback
    }

    /**
     * Parses the given `path` and finds the appropriate value within this [DataArray].
     *
     * @param  root
     * The root data array, which is the top level accessor.
     * <br></br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     * The path of the value, in accordance with the described grammar by [DataPath].
     * This must start with an index element, such as `"[0]"`.
     *
     * @throws ParsingException
     * If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     * If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     * If null is provided or the path is empty
     *
     * @return The String value at the given path
     */
    @Nonnull
    fun getString(@Nonnull root: DataArray?, @Nonnull path: String): String? {
        val string = get<String?>(
            root,
            path,
            { obj: DataObject?, key: String -> obj!!.getString(key) }) { obj: DataArray?, index: Int ->
            obj!!.getString(
                index
            )
        }
        if (string == null) pathError(path, "String")
        return string
    }

    /**
     * Parses the given `path` and finds the appropriate value within this [DataArray].
     *
     * @param  root
     * The root data array, which is the top level accessor.
     * <br></br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     * The path of the value, in accordance with the described grammar by [DataPath].
     * This must start with an index element, such as `"[0]"`.
     *
     * @throws ParsingException
     * If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     * If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     * If null is provided or the path is empty
     *
     * @return The String value at the given path, returning the fallback if the path resolves to an optional value that is missing.
     */
    @Contract("_, _, !null -> !null")
    fun getString(@Nonnull root: DataArray?, @Nonnull path: String, fallback: String?): String? {
        val string = get(
            root,
            path,
            { obj: DataObject?, key: String ->
                obj!!.getString(
                    key,
                    fallback
                )
            }) { arr: DataArray?, index: Int -> arr!!.getString(index, fallback) }
        return string ?: fallback
    }

    /**
     * Parses the given `path` and finds the appropriate value within this [DataObject].
     *
     * @param  root
     * The root data object, which is the top level accessor.
     * <br></br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     * The path of the value, in accordance with the described grammar by [DataPath].
     * This must start with a name element, such as `"foo"`.
     *
     * @throws ParsingException
     * If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     * If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     * If null is provided or the path is empty
     *
     * @return The DataObject at the given path
     */
    @Nonnull
    fun getObject(@Nonnull root: DataObject?, @Nonnull path: String): DataObject? {
        val obj = optObject(root, path)
        if (obj == null) pathError(path, "Object")
        return obj
    }

    /**
     * Parses the given `path` and finds the appropriate value within this [DataObject].
     *
     * @param  root
     * The root data object, which is the top level accessor.
     * <br></br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     * The path of the value, in accordance with the described grammar by [DataPath].
     * This must start with a name element, such as `"foo"`.
     *
     * @throws ParsingException
     * If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     * If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     * If null is provided or the path is empty
     *
     * @return The DataObject at the given path, or null if the path resolves to an optional value that is missing.
     */
    fun optObject(@Nonnull root: DataObject?, @Nonnull path: String): DataObject? {
        var path = path
        if (!path.endsWith("?")) path += "?"
        return get<DataObject?>(
            root,
            path,
            { obj: DataObject?, key: String -> obj!!.getObject(key) }) { obj: DataArray?, index: Int ->
            obj!!.getObject(
                index
            )
        }
    }

    /**
     * Parses the given `path` and finds the appropriate value within this [DataArray].
     *
     * @param  root
     * The root data array, which is the top level accessor.
     * <br></br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     * The path of the value, in accordance with the described grammar by [DataPath].
     * This must start with an index element, such as `"[0]"`.
     *
     * @throws ParsingException
     * If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     * If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     * If null is provided or the path is empty
     *
     * @return The DataObject at the given path
     */
    @JvmStatic
    @Nonnull
    fun getObject(@Nonnull root: DataArray?, @Nonnull path: String): DataObject? {
        val obj = optObject(root, path)
        if (obj == null) pathError(path, "Object")
        return obj
    }

    /**
     * Parses the given `path` and finds the appropriate value within this [DataArray].
     *
     * @param  root
     * The root data array, which is the top level accessor.
     * <br></br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     * The path of the value, in accordance with the described grammar by [DataPath].
     * This must start with an index element, such as `"[0]"`.
     *
     * @throws ParsingException
     * If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     * If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     * If null is provided or the path is empty
     *
     * @return The DataObject at the given path, or null if the path resolves to an optional value that is missing.
     */
    fun optObject(@Nonnull root: DataArray?, @Nonnull path: String): DataObject? {
        var path = path
        if (!path.endsWith("?")) path += "?"
        return get<DataObject?>(
            root,
            path,
            { obj: DataObject?, key: String -> obj!!.getObject(key) }) { obj: DataArray?, index: Int ->
            obj!!.getObject(
                index
            )
        }
    }

    /**
     * Parses the given `path` and finds the appropriate value within this [DataObject].
     *
     * @param  root
     * The root data object, which is the top level accessor.
     * <br></br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     * The path of the value, in accordance with the described grammar by [DataPath].
     * This must start with a name element, such as `"foo"`.
     *
     * @throws ParsingException
     * If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     * If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     * If null is provided or the path is empty
     *
     * @return The [DataArray] at the given path
     */
    @Nonnull
    fun getArray(@Nonnull root: DataObject?, @Nonnull path: String): DataArray? {
        val array = optArray(root, path)
        if (array == null) pathError(path, "Array")
        return array
    }

    /**
     * Parses the given `path` and finds the appropriate value within this [DataObject].
     *
     * @param  root
     * The root data object, which is the top level accessor.
     * <br></br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     * The path of the value, in accordance with the described grammar by [DataPath].
     * This must start with a name element, such as `"foo"`.
     *
     * @throws ParsingException
     * If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     * If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     * If null is provided or the path is empty
     *
     * @return The [DataArray] at the given path, or null if the path resolves to an optional value that is missing.
     */
    fun optArray(@Nonnull root: DataObject?, @Nonnull path: String): DataArray? {
        var path = path
        if (!path.endsWith("?")) path += "?"
        return get<DataArray?>(
            root,
            path,
            { obj: DataObject?, key: String -> obj!!.getArray(key) }) { obj: DataArray?, index: Int ->
            obj!!.getArray(
                index
            )
        }
    }

    /**
     * Parses the given `path` and finds the appropriate value within this [DataArray].
     *
     * @param  root
     * The root data array, which is the top level accessor.
     * <br></br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     * The path of the value, in accordance with the described grammar by [DataPath].
     * This must start with an index element, such as `"[0]"`.
     *
     * @throws ParsingException
     * If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     * If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     * If null is provided or the path is empty
     *
     * @return The [DataArray] at the given path
     */
    @Nonnull
    fun getArray(@Nonnull root: DataArray?, @Nonnull path: String): DataArray? {
        val array = optArray(root, path)
        if (array == null) pathError(path, "Array")
        return array
    }

    /**
     * Parses the given `path` and finds the appropriate value within this [DataArray].
     *
     * @param  root
     * The root data array, which is the top level accessor.
     * <br></br>The very first element in the path corresponds to a field of that name within this root object.
     * @param  path
     * The path of the value, in accordance with the described grammar by [DataPath].
     * This must start with an index element, such as `"[0]"`.
     *
     * @throws ParsingException
     * If the path is invalid or resolving fails due to missing elements
     * @throws IndexOutOfBoundsException
     * If any of the elements in the path refer to an array index that is out of bounds
     * @throws IllegalArgumentException
     * If null is provided or the path is empty
     *
     * @return The [DataArray] at the given path, or null if the path resolves to an optional value that is missing.
     */
    fun optArray(@Nonnull root: DataArray?, @Nonnull path: String): DataArray? {
        var path = path
        if (!path.endsWith("?")) path += "?"
        return get<DataArray?>(
            root,
            path,
            { obj: DataObject?, key: String -> obj!!.getArray(key) }) { obj: DataArray?, index: Int ->
            obj!!.getArray(
                index
            )
        }
    }

    private fun pathError(path: String, type: String): ParsingException {
        throw ParsingException("Could not resolve value of type $type at path \"$path\"")
    }
}
