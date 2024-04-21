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

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.type.CollectionType
import net.dv8tion.jda.api.exceptions.ParsingException
import net.dv8tion.jda.api.utils.data.etf.ExTermDecoder
import net.dv8tion.jda.api.utils.data.etf.ExTermDecoder.unpackList
import net.dv8tion.jda.api.utils.data.etf.ExTermEncoder.pack
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.Helpers
import org.jetbrains.annotations.Contract
import org.slf4j.LoggerFactory
import java.io.*
import java.nio.ByteBuffer
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException
import java.util.*
import java.util.function.BiFunction
import java.util.function.Function
import java.util.function.UnaryOperator
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.annotation.Nonnull

/**
 * Represents a list of values used in communication with the Discord API.
 *
 *
 * Throws [java.lang.IndexOutOfBoundsException]
 * if provided with index out of bounds.
 *
 *
 * This class is not Thread-Safe
 */
class DataArray(val data: MutableList<Any>?) : Iterable<Any?>, SerializableArray {
    /**
     * Whether the value at the specified index is null.
     *
     * @param  index
     * The index to check
     *
     * @return True, if the value at the index is null
     */
    fun isNull(index: Int): Boolean {
        return index >= length() || data!![index] == null
    }

    /**
     * Whether the value at the specified index is of the specified type.
     *
     * @param  index
     * The index to check
     * @param  type
     * The type to check
     *
     * @return True, if the type check is successful
     *
     * @see net.dv8tion.jda.api.utils.data.DataType.isType
     */
    fun isType(index: Int, @Nonnull type: DataType): Boolean {
        return type.isType(data!![index])
    }

    /**
     * The length of the array.
     *
     * @return The length of the array
     */
    fun length(): Int {
        return data!!.size
    }

    val isEmpty: Boolean
        /**
         * Whether this array is empty
         *
         * @return True, if this array is empty
         */
        get() = data!!.isEmpty()

    /**
     * Resolves the value at the specified index to a DataObject
     *
     * @param  index
     * The index to resolve
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     * If the value is of the wrong type or missing
     *
     * @return The resolved DataObject
     */
    @Nonnull
    fun getObject(index: Int): DataObject {
        var child: Map<String, Any?>? = null
        try {
            child = get<Map<*, *>>(MutableMap::class.java, index) as Map<String, Any?>?
        } catch (ex: ClassCastException) {
            log.error("Unable to extract child data", ex)
        }
        if (child == null) throw valueError(index, "DataObject")
        return DataObject(child)
    }

    /**
     * Resolves the value at the specified index to a DataArray
     *
     * @param  index
     * The index to resolve
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     * If the value is of the wrong type or null
     *
     * @return The resolved DataArray
     */
    @Nonnull
    fun getArray(index: Int): DataArray {
        var child: MutableList<Any>? = null
        try {
            child = get(MutableList::class.java, index) as MutableList<Any>?
        } catch (ex: ClassCastException) {
            log.error("Unable to extract child data", ex)
        }
        if (child == null) throw valueError(index, "DataArray")
        return DataArray(child)
    }

    /**
     * Resolves the value at the specified index to a String.
     *
     * @param  index
     * The index to resolve
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     * If the value is of the wrong type or null
     *
     * @return The resolved String
     */
    @Nonnull
    fun getString(index: Int): String {
        return get(
            String::class.java,
            index,
            UnaryOperator.identity()
        ) { obj: Number? -> java.lang.String.valueOf(obj) }
            ?: throw valueError(index, "String")
    }

    /**
     * Resolves the value at the specified index to a String.
     *
     * @param  index
     * The index to resolve
     * @param  defaultValue
     * Alternative value to use when the value associated with the index is null
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     * If the value is of the wrong type
     *
     * @return The resolved String
     */
    @Contract("_, !null -> !null")
    fun getString(index: Int, defaultValue: String?): String? {
        val value =
            get(String::class.java, index, UnaryOperator.identity()) { obj: Number? -> java.lang.String.valueOf(obj) }
        return value ?: defaultValue
    }

    /**
     * Resolves the value at the specified index to a boolean.
     *
     * @param  index
     * The index to resolve
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     * If the value is of the wrong type
     *
     * @return True, if the value is present and set to true. Otherwise false.
     */
    fun getBoolean(index: Int): Boolean {
        return getBoolean(index, false)
    }

    /**
     * Resolves the value at the specified index to a boolean.
     *
     * @param  index
     * The index to resolve
     * @param  defaultValue
     * Alternative value to use when the value associated with the index is null
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     * If the value is of the wrong type
     *
     * @return True, if the value is present and set to true. False, if it is set to false. Otherwise defaultValue.
     */
    fun getBoolean(index: Int, defaultValue: Boolean): Boolean {
        val value = get(Boolean::class.java, index, { s: String -> s.toBoolean() }, null)
        return value ?: defaultValue
    }

    /**
     * Resolves the value at the specified index to an int.
     *
     * @param  index
     * The index to resolve
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     * If the value is of the wrong type
     *
     * @return The resolved int value
     */
    fun getInt(index: Int): Int {
        return get(Int::class.java, index, { s: String -> s.toInt() }) { obj: Number -> obj.toInt() }!!
            ?: throw valueError(index, "int")
    }

    /**
     * Resolves the value at the specified index to an int.
     *
     * @param  index
     * The index to resolve
     * @param  defaultValue
     * Alternative value to use when the value associated with the index is null
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     * If the value is of the wrong type
     *
     * @return The resolved int value
     */
    fun getInt(index: Int, defaultValue: Int): Int {
        val value = get(Int::class.java, index, { s: String -> s.toInt() }) { obj: Number -> obj.toInt() }
        return value ?: defaultValue
    }

    /**
     * Resolves the value at the specified index to an unsigned int.
     *
     * @param  index
     * The index to resolve
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     * If the value is of the wrong type
     *
     * @return The resolved unsigned int value
     */
    fun getUnsignedInt(index: Int): Int {
        return get(
            Int::class.java,
            index,
            { s: String? -> Integer.parseUnsignedInt(s) }) { obj: Number -> obj.toInt() }!!
            ?: throw valueError(index, "unsigned int")
    }

    /**
     * Resolves the value at the specified index to an unsigned int.
     *
     * @param  index
     * The index to resolve
     * @param  defaultValue
     * Alternative value to use when the value associated with the index is null
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     * If the value is of the wrong type
     *
     * @return The resolved unsigned int value
     */
    fun getUnsignedInt(index: Int, defaultValue: Int): Int {
        val value =
            get(Int::class.java, index, { s: String? -> Integer.parseUnsignedInt(s) }) { obj: Number -> obj.toInt() }
        return value ?: defaultValue
    }

    /**
     * Resolves the value at the specified index to a long.
     *
     * @param  index
     * The index to resolve
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     * If the value is of the wrong type
     *
     * @return The resolved long value
     */
    fun getLong(index: Int): Long {
        return get(Long::class.java, index, { s: String -> s.toLong() }) { obj: Number -> obj.toLong() }!!
            ?: throw valueError(index, "long")
    }

    /**
     * Resolves the value at the specified index to a long.
     *
     * @param  index
     * The index to resolve
     * @param  defaultValue
     * Alternative value to use when the value associated with the index is null
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     * If the value is of the wrong type
     *
     * @return The resolved long value
     */
    fun getLong(index: Int, defaultValue: Long): Long {
        val value = get(Long::class.java, index, { s: String -> s.toLong() }) { obj: Number -> obj.toLong() }
        return value ?: defaultValue
    }

    /**
     * Resolves the value at the specified index to an unsigned long.
     *
     * @param  index
     * The index to resolve
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     * If the value is of the wrong type
     *
     * @return The resolved unsigned long value
     */
    fun getUnsignedLong(index: Int): Long {
        return get(
            Long::class.java,
            index,
            { s: String? -> java.lang.Long.parseUnsignedLong(s) }) { obj: Number -> obj.toLong() }!!
            ?: throw valueError(index, "unsigned long")
    }

    /**
     * Resolves the value at the specified index to an [OffsetDateTime].
     * <br></br>**Note:** This method should be used on ISO8601 timestamps
     *
     * @param  index
     * The index to resolve
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     * If the value is missing, null, or not a valid ISO8601 timestamp
     *
     * @return Possibly-null [OffsetDateTime] object representing the timestamp
     */
    @Nonnull
    fun getOffsetDateTime(index: Int): OffsetDateTime {
        return getOffsetDateTime(index, null) ?: throw valueError(index, "OffsetDateTime")
    }

    /**
     * Resolves the value at the specified index to an [OffsetDateTime].
     * <br></br>**Note:** This method should only be used on ISO8601 timestamps
     *
     * @param  index
     * The index to resolve
     * @param  defaultValue
     * Alternative value to use when no value or null value is associated with the key
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     * If the value is not a valid ISO8601 timestamp
     *
     * @return Possibly-null [OffsetDateTime] object representing the timestamp
     */
    @Contract("_, !null -> !null")
    fun getOffsetDateTime(index: Int, defaultValue: OffsetDateTime?): OffsetDateTime? {
        val value: OffsetDateTime?
        value = try {
            get(OffsetDateTime::class.java, index, { text: String? -> OffsetDateTime.parse(text) }, null)
        } catch (e: DateTimeParseException) {
            val reason =
                "Cannot parse value for index %d into an OffsetDateTime object. Try double checking that %s is a valid ISO8601 timestamp"
            throw ParsingException(String.format(reason, index, e.parsedString))
        }
        return value ?: defaultValue
    }

    /**
     * Resolves the value at the specified index to an unsigned long.
     *
     * @param  index
     * The index to resolve
     * @param  defaultValue
     * Alternative value to use when the value associated with the index is null
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     * If the value is of the wrong type
     *
     * @return The resolved unsigned long value
     */
    fun getUnsignedLong(index: Int, defaultValue: Long): Long {
        val value = get(
            Long::class.java,
            index,
            { s: String? -> java.lang.Long.parseUnsignedLong(s) }) { obj: Number -> obj.toLong() }
        return value ?: defaultValue
    }

    /**
     * Resolves the value at the specified index to a double.
     *
     * @param  index
     * The index to resolve
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     * If the value is of the wrong type
     *
     * @return The resolved double value
     */
    fun getDouble(index: Int): Double {
        return get(Double::class.java, index, { s: String -> s.toDouble() }) { obj: Number -> obj.toDouble() }!!
            ?: throw valueError(index, "double")
    }

    /**
     * Resolves the value at the specified index to a double.
     *
     * @param  index
     * The index to resolve
     * @param  defaultValue
     * Alternative value to use when the value associated with the index is null
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     * If the value is of the wrong type
     *
     * @return The resolved double value
     */
    fun getDouble(index: Int, defaultValue: Double): Double {
        val value = get(Double::class.java, index, { s: String -> s.toDouble() }) { obj: Number -> obj.toDouble() }
        return value ?: defaultValue
    }

    /**
     * Appends the provided value to the end of the array.
     *
     * @param  value
     * The value to append
     *
     * @return A DataArray with the value inserted at the end
     */
    @Nonnull
    fun add(value: Any?): DataArray {
        if (value is SerializableData) data!!.add(value.toData().data!!) else if (value is SerializableArray) data!!.add(
            value.toDataArray().data!!
        ) else data!!.add(value!!)
        return this
    }

    /**
     * Appends the provided values to the end of the array.
     *
     * @param  values
     * The values to append
     *
     * @return A DataArray with the values inserted at the end
     */
    @Nonnull
    fun addAll(@Nonnull values: Collection<*>?): DataArray {
        values!!.forEach { value: Any? -> add(value) }
        return this
    }

    /**
     * Appends the provided values to the end of the array.
     *
     * @param  array
     * The values to append
     *
     * @return A DataArray with the values inserted at the end
     */
    @Nonnull
    fun addAll(@Nonnull array: DataArray): DataArray {
        return addAll(array.data)
    }

    /**
     * Inserts the specified value at the provided index.
     *
     * @param  index
     * The target index
     * @param  value
     * The value to insert
     *
     * @return A DataArray with the value inserted at the specified index
     */
    @Nonnull
    fun insert(index: Int, value: Any?): DataArray {
        if (value is SerializableData) data!!.add(
            index,
            value.toData().data!!
        ) else if (value is SerializableArray) data!!.add(index, value.toDataArray().data!!) else data!!.add(
            index,
            value!!
        )
        return this
    }

    /**
     * Removes the value at the specified index.
     *
     * @param  index
     * The target index to remove
     *
     * @return A DataArray with the value removed
     */
    @Nonnull
    fun remove(index: Int): DataArray {
        data!!.removeAt(index)
        return this
    }

    /**
     * Removes the specified value.
     *
     * @param  value
     * The value to remove
     *
     * @return A DataArray with the value removed
     */
    @Nonnull
    fun remove(value: Any?): DataArray {
        data!!.remove(value!!)
        return this
    }

    /**
     * Serializes this object as JSON.
     *
     * @return byte array containing the JSON representation of this object
     */
    @Nonnull
    fun toJson(): ByteArray {
        return try {
            val outputStream = ByteArrayOutputStream()
            mapper!!.writeValue(outputStream, data)
            outputStream.toByteArray()
        } catch (e: IOException) {
            throw UncheckedIOException(e)
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
    fun toETF(): ByteArray {
        val buffer = pack(data)
        return Arrays.copyOfRange(buffer.array(), buffer.arrayOffset(), buffer.arrayOffset() + buffer.limit())
    }

    override fun toString(): String {
        return try {
            mapper!!.writeValueAsString(data)
        } catch (e: JsonProcessingException) {
            throw ParsingException(e)
        }
    }

    @Nonnull
    fun toPrettyString(): String {
        return try {
            mapper!!.writer(DefaultPrettyPrinter())
                .with(SerializationFeature.INDENT_OUTPUT)
                .with(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                .writeValueAsString(data)
        } catch (e: JsonProcessingException) {
            throw ParsingException(e)
        }
    }

    /**
     * Converts this DataArray to a [java.util.List].
     *
     * @return The resulting list
     */
    @Nonnull
    fun toList(): List<Any>? {
        return data
    }

    private fun valueError(index: Int, expectedType: String): ParsingException {
        return ParsingException("Unable to resolve value at " + index + " to type " + expectedType + ": " + data!![index])
    }

    private operator fun <T> get(@Nonnull type: Class<T>, index: Int): T? {
        return get(type, index, null, null)
    }

    private operator fun <T> get(
        @Nonnull type: Class<T>,
        index: Int,
        stringMapper: Function<String, T>?,
        numberMapper: Function<Number, T>?
    ): T? {
        if (index < 0) throw IndexOutOfBoundsException("Index out of range: $index")
        val value = (if (index < data!!.size) data[index] else null) ?: return null
        if (type.isInstance(value)) return type.cast(value)
        if (type == String::class.java) return type.cast(value.toString())
        // attempt type coercion
        if (stringMapper != null && value is String) return stringMapper.apply(value) else if (numberMapper != null && value is Number) return numberMapper.apply(
            value
        )
        throw ParsingException(
            Helpers.format(
                "Cannot parse value for index %d into type %s: %s instance of %s",
                index, type.getSimpleName(), value, value.javaClass.getSimpleName()
            )
        )
    }

    @Nonnull
    override fun iterator(): MutableIterator<Any> {
        return data!!.iterator()
    }

    @Nonnull
    fun <T> stream(mapper: BiFunction<in DataArray?, Int?, out T>): Stream<T> {
        return IntStream.range(0, length())
            .mapToObj { index: Int -> mapper.apply(this, index) }
    }

    @Nonnull
    override fun toDataArray(): DataArray {
        return this
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is DataArray) return false
        return data == o.data
    }

    override fun hashCode(): Int {
        return Objects.hash(data)
    }

    companion object {
        private val log = LoggerFactory.getLogger(DataObject::class.java)
        private val mapper: ObjectMapper? = null
        private val module: SimpleModule? = null
        private val listType: CollectionType? = null

        init {
            mapper = ObjectMapper()
            module = SimpleModule()
            module.addAbstractTypeMapping<Map<*, *>>(MutableMap::class.java, HashMap::class.java)
            module.addAbstractTypeMapping<List<*>>(MutableList::class.java, ArrayList::class.java)
            mapper.registerModule(module)
            listType = mapper.getTypeFactory().constructRawCollectionType(ArrayList::class.java)
        }

        /**
         * Creates a new empty DataArray, ready to be populated with values.
         *
         * @return An empty DataArray instance
         *
         * @see .add
         */
        @JvmStatic
        @Nonnull
        fun empty(): DataArray {
            return DataArray(ArrayList())
        }

        /**
         * Creates a new DataArray and populates it with the contents
         * of the provided collection.
         *
         * @param  col
         * The [java.util.Collection]
         *
         * @return A new DataArray populated with the contents of the collection
         */
        @Nonnull
        fun fromCollection(@Nonnull col: Collection<*>?): DataArray {
            return empty().addAll(col)
        }

        /**
         * Parses a JSON Array into a DataArray instance.
         *
         * @param  json
         * The correctly formatted JSON Array
         *
         * @throws net.dv8tion.jda.api.exceptions.ParsingException
         * If the provided JSON is incorrectly formatted
         *
         * @return A new DataArray instance for the provided array
         */
        @JvmStatic
        @Nonnull
        fun fromJson(@Nonnull json: String?): DataArray {
            return try {
                DataArray(mapper!!.readValue(json, listType))
            } catch (e: IOException) {
                throw ParsingException(e)
            }
        }

        /**
         * Parses a JSON Array into a DataArray instance.
         *
         * @param  json
         * The correctly formatted JSON Array
         *
         * @throws net.dv8tion.jda.api.exceptions.ParsingException
         * If the provided JSON is incorrectly formatted or an I/O error occurred
         *
         * @return A new DataArray instance for the provided array
         */
        @JvmStatic
        @Nonnull
        fun fromJson(@Nonnull json: InputStream?): DataArray {
            return try {
                DataArray(mapper!!.readValue(json, listType))
            } catch (e: IOException) {
                throw ParsingException(e)
            }
        }

        /**
         * Parses a JSON Array into a DataArray instance.
         *
         * @param  json
         * The correctly formatted JSON Array
         *
         * @throws net.dv8tion.jda.api.exceptions.ParsingException
         * If the provided JSON is incorrectly formatted or an I/O error occurred
         *
         * @return A new DataArray instance for the provided array
         */
        @JvmStatic
        @Nonnull
        fun fromJson(@Nonnull json: Reader?): DataArray {
            return try {
                DataArray(mapper!!.readValue(json, listType))
            } catch (e: IOException) {
                throw ParsingException(e)
            }
        }

        /**
         * Parses using [ExTermDecoder].
         * The provided data must start with the correct version header (131).
         *
         * @param  data
         * The data to decode
         *
         * @throws IllegalArgumentException
         * If the provided data is null
         * @throws net.dv8tion.jda.api.exceptions.ParsingException
         * If the provided ETF payload is incorrectly formatted or an I/O error occurred
         *
         * @return A DataArray instance for the provided payload
         *
         * @since  4.2.1
         */
        @JvmStatic
        @Nonnull
        fun fromETF(@Nonnull data: ByteArray): DataArray {
            Checks.notNull(data, "Data")
            return try {
                val list = unpackList(ByteBuffer.wrap(data))
                DataArray(list)
            } catch (ex: Exception) {
                log.error("Failed to parse ETF data {}", data.contentToString(), ex)
                throw ParsingException(ex)
            }
        }
    }
}
