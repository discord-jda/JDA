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
import com.fasterxml.jackson.databind.type.MapType
import net.dv8tion.jda.api.exceptions.ParsingException
import net.dv8tion.jda.api.utils.MiscUtil
import net.dv8tion.jda.api.utils.data.etf.ExTermDecoder
import net.dv8tion.jda.api.utils.data.etf.ExTermDecoder.unpackMap
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
import java.util.function.Function
import java.util.function.UnaryOperator
import javax.annotation.Nonnull

/**
 * Represents a map of values used in communication with the Discord API.
 *
 *
 * Throws [java.lang.NullPointerException],
 * if a parameter annotated with [javax.annotation.Nonnull] is provided with `null`.
 *
 *
 * This class is not Thread-Safe.
 */
class DataObject(@param:Nonnull val data: Map<String, Any?>?) : SerializableData {
    /**
     * Whether the specified key is present.
     *
     * @param  key
     * The key to check
     *
     * @return True, if the specified key is present
     */
    fun hasKey(@Nonnull key: String): Boolean {
        return data!!.containsKey(key)
    }

    /**
     * Whether the specified key is missing or null
     *
     * @param  key
     * The key to check
     *
     * @return True, if the specified key is null or missing
     */
    fun isNull(@Nonnull key: String): Boolean {
        return data!![key] == null
    }

    /**
     * Whether the specified key is of the specified type.
     *
     * @param  key
     * The key to check
     * @param  type
     * The type to check
     *
     * @return True, if the type check is successful
     *
     * @see net.dv8tion.jda.api.utils.data.DataType.isType
     */
    fun isType(@Nonnull key: String, @Nonnull type: DataType): Boolean {
        return type.isType(data!![key])
    }

    /**
     * Resolves a DataObject to a key.
     *
     * @param  key
     * The key to check for a value
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     * If the type is incorrect or no value is present for the specified key
     *
     * @return The resolved instance of DataObject for the key
     */
    @Nonnull
    fun getObject(@Nonnull key: String): DataObject {
        return optObject(key).orElseThrow { valueError(key, "DataObject") }
    }

    /**
     * Resolves a DataObject to a key.
     *
     * @param  key
     * The key to check for a value
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     * If the type is incorrect
     *
     * @return The resolved instance of DataObject for the key, wrapped in [java.util.Optional]
     */
    @Nonnull
    fun optObject(@Nonnull key: String): Optional<DataObject> {
        var child: Map<String, Any?>? = null
        try {
            child = get<Map<*, *>>(MutableMap::class.java, key) as Map<String, Any?>?
        } catch (ex: ClassCastException) {
            log.error("Unable to extract child data", ex)
        }
        return if (child == null) Optional.empty() else Optional.of(DataObject(child))
    }

    /**
     * Resolves a DataArray to a key.
     *
     * @param  key
     * The key to check for a value
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     * If the type is incorrect or no value is present for the specified key
     *
     * @return The resolved instance of DataArray for the key
     */
    @Nonnull
    fun getArray(@Nonnull key: String): DataArray {
        return optArray(key).orElseThrow { valueError(key, "DataArray") }
    }

    /**
     * Resolves a DataArray to a key.
     *
     * @param  key
     * The key to check for a value
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     * If the type is incorrect
     *
     * @return The resolved instance of DataArray for the key, wrapped in [java.util.Optional]
     */
    @Nonnull
    fun optArray(@Nonnull key: String): Optional<DataArray> {
        var child: MutableList<Any>? = null
        try {
            child = get(MutableList::class.java, key) as MutableList<Any>?
        } catch (ex: ClassCastException) {
            log.error("Unable to extract child data", ex)
        }
        return if (child == null) Optional.empty() else Optional.of(DataArray(child))
    }

    /**
     * Resolves any type to the provided key.
     *
     * @param  key
     * The key to check for a value
     *
     * @return [java.util.Optional] with a possible value
     */
    @Nonnull
    fun opt(@Nonnull key: String): Optional<Any> {
        return Optional.ofNullable(data!![key])
    }

    /**
     * Resolves any type to the provided key.
     *
     * @param  key
     * The key to check for a value
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     * If the value is missing or null
     *
     * @return The value of any type
     *
     * @see .opt
     */
    @Nonnull
    operator fun get(@Nonnull key: String): Any {
        return data!![key] ?: throw valueError(key, "any")
    }

    /**
     * Resolves a [java.lang.String] to a key.
     *
     * @param  key
     * The key to check for a value
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     * If the value is missing or null
     *
     * @return The String value
     */
    @Nonnull
    fun getString(@Nonnull key: String): String {
        return getString(key, null) ?: throw valueError(key, "String")
    }

    /**
     * Resolves a [java.lang.String] to a key.
     *
     * @param  key
     * The key to check for a value
     * @param  defaultValue
     * Alternative value to use when no value or null value is associated with the key
     *
     * @return The String value, or null if provided with null defaultValue
     */
    @Contract("_, !null -> !null")
    fun getString(@Nonnull key: String, defaultValue: String?): String? {
        val value =
            get(String::class.java, key, UnaryOperator.identity()) { obj: Number? -> java.lang.String.valueOf(obj) }
        return value ?: defaultValue
    }

    /**
     * Resolves a [java.lang.Boolean] to a key.
     *
     * @param  key
     * The key to check for a value
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     * If the value is of the wrong type
     *
     * @return True, if the value is present and set to true. False if the value is missing or set to false.
     */
    fun getBoolean(@Nonnull key: String): Boolean {
        return getBoolean(key, false)
    }

    /**
     * Resolves a [java.lang.Boolean] to a key.
     *
     * @param  key
     * The key to check for a value
     * @param  defaultValue
     * Alternative value to use when no value or null value is associated with the key
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     * If the value is of the wrong type
     *
     * @return True, if the value is present and set to true. False if the value is set to false. defaultValue if it is missing.
     */
    fun getBoolean(@Nonnull key: String, defaultValue: Boolean): Boolean {
        val value = get(Boolean::class.java, key, { s: String -> s.toBoolean() }, null)
        return value ?: defaultValue
    }

    /**
     * Resolves a long to a key.
     *
     * @param  key
     * The key to check for a value
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     * If the value is missing, null, or of the wrong type
     *
     * @return The long value for the key
     */
    fun getLong(@Nonnull key: String): Long {
        return get(
            Long::class.java,
            key,
            { input: String? -> MiscUtil.parseLong(input) }) { obj: Number -> obj.toLong() }!!
            ?: throw valueError(key, "long")
    }

    /**
     * Resolves a long to a key.
     *
     * @param  key
     * The key to check for a value
     * @param  defaultValue
     * Alternative value to use when no value or null value is associated with the key
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     * If the value is of the wrong type
     *
     * @return The long value for the key
     */
    fun getLong(@Nonnull key: String, defaultValue: Long): Long {
        val value = get(Long::class.java, key, { s: String -> s.toLong() }) { obj: Number -> obj.toLong() }
        return value ?: defaultValue
    }

    /**
     * Resolves an unsigned long to a key.
     *
     * @param  key
     * The key to check for a value
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     * If the value is missing, null, or of the wrong type
     *
     * @return The unsigned long value for the key
     */
    fun getUnsignedLong(@Nonnull key: String): Long {
        return get(
            Long::class.java,
            key,
            { s: String? -> java.lang.Long.parseUnsignedLong(s) }) { obj: Number -> obj.toLong() }!!
            ?: throw valueError(key, "unsigned long")
    }

    /**
     * Resolves an unsigned long to a key.
     *
     * @param  key
     * The key to check for a value
     * @param  defaultValue
     * Alternative value to use when no value or null value is associated with the key
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     * If the value is of the wrong type
     *
     * @return The unsigned long value for the key
     */
    fun getUnsignedLong(@Nonnull key: String, defaultValue: Long): Long {
        val value = get(
            Long::class.java,
            key,
            { s: String? -> java.lang.Long.parseUnsignedLong(s) }) { obj: Number -> obj.toLong() }
        return value ?: defaultValue
    }

    /**
     * Resolves an int to a key.
     *
     * @param  key
     * The key to check for a value
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     * If the value is missing, null, or of the wrong type
     *
     * @return The int value for the key
     */
    fun getInt(@Nonnull key: String): Int {
        return get(Int::class.java, key, { s: String -> s.toInt() }) { obj: Number -> obj.toInt() }!!
            ?: throw valueError(key, "int")
    }

    /**
     * Resolves an int to a key.
     *
     * @param  key
     * The key to check for a value
     * @param  defaultValue
     * Alternative value to use when no value or null value is associated with the key
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     * If the value is of the wrong type
     *
     * @return The int value for the key
     */
    fun getInt(@Nonnull key: String, defaultValue: Int): Int {
        val value = get(Int::class.java, key, { s: String -> s.toInt() }) { obj: Number -> obj.toInt() }
        return value ?: defaultValue
    }

    /**
     * Resolves an unsigned int to a key.
     *
     * @param  key
     * The key to check for a value
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     * If the value is missing, null, or of the wrong type
     *
     * @return The unsigned int value for the key
     */
    fun getUnsignedInt(@Nonnull key: String): Int {
        return get(
            Int::class.java,
            key,
            { s: String? -> Integer.parseUnsignedInt(s) }) { obj: Number -> obj.toInt() }!!
            ?: throw valueError(key, "unsigned int")
    }

    /**
     * Resolves an unsigned int to a key.
     *
     * @param  key
     * The key to check for a value
     * @param  defaultValue
     * Alternative value to use when no value or null value is associated with the key
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     * If the value is of the wrong type
     *
     * @return The unsigned int value for the key
     */
    fun getUnsignedInt(@Nonnull key: String, defaultValue: Int): Int {
        val value =
            get(Int::class.java, key, { s: String? -> Integer.parseUnsignedInt(s) }) { obj: Number -> obj.toInt() }
        return value ?: defaultValue
    }

    /**
     * Resolves a double to a key.
     *
     * @param  key
     * The key to check for a value
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     * If the value is missing, null, or of the wrong type
     *
     * @return The double value for the key
     */
    fun getDouble(@Nonnull key: String): Double {
        return get(Double::class.java, key, { s: String -> s.toDouble() }) { obj: Number -> obj.toDouble() }!!
            ?: throw valueError(key, "double")
    }

    /**
     * Resolves a double to a key.
     *
     * @param  key
     * The key to check for a value
     * @param  defaultValue
     * Alternative value to use when no value or null value is associated with the key
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     * If the value is of the wrong type
     *
     * @return The double value for the key
     */
    fun getDouble(@Nonnull key: String, defaultValue: Double): Double {
        val value = get(Double::class.java, key, { s: String -> s.toDouble() }) { obj: Number -> obj.toDouble() }
        return value ?: defaultValue
    }

    /**
     * Resolves an [OffsetDateTime] to a key.
     * <br></br>**Note:** This method should be used on ISO8601 timestamps
     *
     * @param key
     * The key to check for a value
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     * If the value is missing, null, or not a valid ISO8601 timestamp
     *
     * @return Possibly-null [OffsetDateTime] object representing the timestamp
     */
    @Nonnull
    fun getOffsetDateTime(@Nonnull key: String): OffsetDateTime {
        return getOffsetDateTime(key, null) ?: throw valueError(key, "OffsetDateTime")
    }

    /**
     * Resolves an [OffsetDateTime] to a key.
     * <br></br>**Note:** This method should only be used on ISO8601 timestamps
     *
     * @param  key
     * The key to check for a value
     * @param  defaultValue
     * Alternative value to use when no value or null value is associated with the key
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     * If the value is not a valid ISO8601 timestamp
     *
     * @return Possibly-null [OffsetDateTime] object representing the timestamp
     */
    @Contract("_, !null -> !null")
    fun getOffsetDateTime(@Nonnull key: String, defaultValue: OffsetDateTime?): OffsetDateTime? {
        val value: OffsetDateTime?
        value = try {
            get(OffsetDateTime::class.java, key, { text: String? -> OffsetDateTime.parse(text) }, null)
        } catch (e: DateTimeParseException) {
            val reason =
                "Cannot parse value for %s into an OffsetDateTime object. Try double checking that %s is a valid ISO8601 timestmap"
            throw ParsingException(String.format(reason, key, e.parsedString))
        }
        return value ?: defaultValue
    }

    /**
     * Removes the value associated with the specified key.
     * If no value is associated with the key, this does nothing.
     *
     * @param  key
     * The key to unlink
     *
     * @return A DataObject with the removed key
     */
    @Nonnull
    fun remove(@Nonnull key: String): DataObject {
        data.remove(key)
        return this
    }

    /**
     * Upserts a null value for the provided key.
     *
     * @param  key
     * The key to upsert
     *
     * @return A DataObject with the updated value
     */
    @Nonnull
    fun putNull(@Nonnull key: String): DataObject {
        data.put(key, null)
        return this
    }

    /**
     * Upserts a new value for the provided key.
     *
     * @param  key
     * The key to upsert
     * @param  value
     * The new value
     *
     * @return A DataObject with the updated value
     */
    @Nonnull
    fun put(@Nonnull key: String, value: Any?): DataObject {
        if (value is SerializableData) data.put(key, value.toData().data) else if (value is SerializableArray) data.put(
            key,
            value.toDataArray().data
        ) else data.put(key, value)
        return this
    }

    /**
     * Renames an existing field to the new name.
     * <br></br>This is a shorthand to [remove][.remove] under the old key and then [put][.put] under the new key.
     *
     *
     * If there is nothing mapped to the old key, this does nothing.
     *
     * @param  key
     * The old key
     * @param  newKey
     * The new key
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return A DataObject with the updated value
     */
    @Nonnull
    fun rename(@Nonnull key: String, @Nonnull newKey: String): DataObject {
        Checks.notNull(key, "Key")
        Checks.notNull(newKey, "Key")
        if (!data!!.containsKey(key)) return this
        data.put(newKey, data.remove(key))
        return this
    }

    /**
     * [java.util.Collection] of all values in this DataObject.
     *
     * @return [java.util.Collection] for all values
     */
    @Nonnull
    fun values(): Collection<Any?> {
        return data!!.values
    }

    /**
     * [java.util.Set] of all keys in this DataObject.
     *
     * @return [Set] of keys
     */
    @Nonnull
    fun keys(): Set<String> {
        return data!!.keys
    }

    /**
     * Serialize this object as JSON.
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
     * Serializes this object as ETF MAP term.
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
     * Converts this DataObject to a [java.util.Map]
     *
     * @return The resulting map
     */
    @Nonnull
    fun toMap(): Map<String, Any?>? {
        return data
    }

    @Nonnull
    override fun toData(): DataObject {
        return this
    }

    private fun valueError(key: String, expectedType: String): ParsingException {
        return ParsingException("Unable to resolve value with key " + key + " to type " + expectedType + ": " + data!![key])
    }

    private operator fun <T> get(@Nonnull type: Class<T>, @Nonnull key: String): T? {
        return get(type, key, null, null)
    }

    private operator fun <T> get(
        @Nonnull type: Class<T>,
        @Nonnull key: String,
        stringParse: Function<String, T>?,
        numberParse: Function<Number, T>?
    ): T? {
        val value = data!![key] ?: return null
        if (type.isInstance(value)) return type.cast(value)
        if (type == String::class.java) return type.cast(value.toString())
        // attempt type coercion
        if (value is Number && numberParse != null) return numberParse.apply(value) else if (value is String && stringParse != null) return stringParse.apply(
            value
        )
        throw ParsingException(
            Helpers.format(
                "Cannot parse value for %s into type %s: %s instance of %s",
                key, type.getSimpleName(), value, value.javaClass.getSimpleName()
            )
        )
    }

    override fun equals(obj: Any?): Boolean {
        if (obj === this) return true
        return if (obj !is DataObject) false else obj.toMap() == this.toMap()
    }

    override fun hashCode(): Int {
        return toMap().hashCode()
    }

    companion object {
        private val log = LoggerFactory.getLogger(DataObject::class.java)
        private val mapper: ObjectMapper? = null
        private val module: SimpleModule? = null
        private val mapType: MapType? = null

        init {
            mapper = ObjectMapper()
            module = SimpleModule()
            module.addAbstractTypeMapping<Map<*, *>>(MutableMap::class.java, HashMap::class.java)
            module.addAbstractTypeMapping<List<*>>(MutableList::class.java, ArrayList::class.java)
            mapper.registerModule(module)
            mapType = mapper.getTypeFactory().constructRawMapType(HashMap::class.java)
        }

        /**
         * Creates a new empty DataObject, ready to be populated with values.
         *
         * @return An empty DataObject instance
         *
         * @see .put
         */
        @JvmStatic
        @Nonnull
        fun empty(): DataObject {
            return DataObject(HashMap())
        }

        /**
         * Parses a JSON payload into a DataObject instance.
         *
         * @param  data
         * The correctly formatted JSON payload to parse
         *
         * @throws net.dv8tion.jda.api.exceptions.ParsingException
         * If the provided json is incorrectly formatted
         *
         * @return A DataObject instance for the provided payload
         */
        @JvmStatic
        @Nonnull
        fun fromJson(@Nonnull data: ByteArray?): DataObject {
            return try {
                val map = mapper!!.readValue<Map<String, Any?>>(data, mapType)
                DataObject(map)
            } catch (ex: IOException) {
                throw ParsingException(ex)
            }
        }

        /**
         * Parses a JSON payload into a DataObject instance.
         *
         * @param  json
         * The correctly formatted JSON payload to parse
         *
         * @throws net.dv8tion.jda.api.exceptions.ParsingException
         * If the provided json is incorrectly formatted
         *
         * @return A DataObject instance for the provided payload
         */
        @JvmStatic
        @Nonnull
        fun fromJson(@Nonnull json: String?): DataObject {
            return try {
                val map = mapper!!.readValue<Map<String, Any?>>(json, mapType)
                DataObject(map)
            } catch (ex: IOException) {
                throw ParsingException(ex)
            }
        }

        /**
         * Parses a JSON payload into a DataObject instance.
         *
         * @param  stream
         * The correctly formatted JSON payload to parse
         *
         * @throws net.dv8tion.jda.api.exceptions.ParsingException
         * If the provided json is incorrectly formatted or an I/O error occurred
         *
         * @return A DataObject instance for the provided payload
         */
        @JvmStatic
        @Nonnull
        fun fromJson(@Nonnull stream: InputStream?): DataObject {
            return try {
                val map = mapper!!.readValue<Map<String, Any?>>(stream, mapType)
                DataObject(map)
            } catch (ex: IOException) {
                throw ParsingException(ex)
            }
        }

        /**
         * Parses a JSON payload into a DataObject instance.
         *
         * @param  stream
         * The correctly formatted JSON payload to parse
         *
         * @throws net.dv8tion.jda.api.exceptions.ParsingException
         * If the provided json is incorrectly formatted or an I/O error occurred
         *
         * @return A DataObject instance for the provided payload
         */
        @JvmStatic
        @Nonnull
        fun fromJson(@Nonnull stream: Reader?): DataObject {
            return try {
                val map = mapper!!.readValue<Map<String, Any?>>(stream, mapType)
                DataObject(map)
            } catch (ex: IOException) {
                throw ParsingException(ex)
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
         * @return A DataObject instance for the provided payload
         *
         * @since  4.2.1
         */
        @JvmStatic
        @Nonnull
        fun fromETF(@Nonnull data: ByteArray): DataObject {
            Checks.notNull(data, "Data")
            return try {
                val map: Map<String, Any?>? = unpackMap(ByteBuffer.wrap(data))
                DataObject(map)
            } catch (ex: Exception) {
                log.error("Failed to parse ETF data {}", data.contentToString(), ex)
                throw ParsingException(ex)
            }
        }
    }
}
