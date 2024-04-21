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
package net.dv8tion.jda.api.utils.data.etf

import net.dv8tion.jda.api.utils.data.DataArray
import net.dv8tion.jda.api.utils.data.SerializableData
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.math.ceil

/**
 * Encodes an object into a binary ETF representation.
 *
 * @see .pack
 * @since  4.2.1
 */
object ExTermEncoder {
    /**
     * Encodes the provided object into an ETF buffer.
     *
     *
     * **The mapping is as follows:**<br></br>
     *
     *  * `String -> Binary`
     *  * `Map -> Map`
     *  * `Collection -> List | NIL`
     *  * `Byte -> Small Int`
     *  * `Integer, Short -> Int | Small Int`
     *  * `Long -> Small BigInt | Int | Small Int`
     *  * `Float, Double -> New Float`
     *  * `Boolean -> Atom(Boolean)`
     *  * `null -> Atom("nil")`
     *
     *
     * @param  data
     * The object to encode
     *
     * @throws UnsupportedOperationException
     * If there is no type mapping for the provided object
     *
     * @return [ByteBuffer] with the encoded ETF term
     */
    @JvmStatic
    fun pack(data: Any?): ByteBuffer {
        val buffer = ByteBuffer.allocate(1024)
        buffer.put(131.toByte())
        val packed = pack(buffer, data)
        // This cast prevents issues with backwards compatibility in the ABI (java 11 made breaking changes)
        (packed as Buffer).flip()
        return packed
    }

    private fun pack(buffer: ByteBuffer, value: Any?): ByteBuffer {
        if (value is String) return packBinary(buffer, value)
        if (value is Map<*, *>) return packMap(buffer, value as Map<String, Any>)
        if (value is SerializableData) return packMap(buffer, value.toData().toMap())
        if (value is Collection<*>) return packList(buffer, value as Collection<Any>)
        if (value is DataArray) return packList(buffer, value.toList())
        if (value is Byte) return packSmallInt(buffer, value)
        if (value is Int || value is Short) return packInt(buffer, (value as Number).toInt())
        if (value is Long) return packLong(buffer, value)
        if (value is Float || value is Double) return packFloat(buffer, (value as Number).toDouble())
        if (value is Boolean) return packAtom(buffer, value.toString())
        if (value == null) return packAtom(buffer, "nil")
        if (value is LongArray) return packArray(buffer, value)
        if (value is IntArray) return packArray(buffer, value)
        if (value is ShortArray) return packArray(buffer, value)
        if (value is ByteArray) return packArray(buffer, value)
        // omitting other array types because we don't use them anywhere
        if (value is Array<Any>) return packList(buffer, Arrays.asList(*value as Array<Any?>))
        throw UnsupportedOperationException("Cannot pack value of type " + value.javaClass.getName())
    }

    private fun realloc(buffer: ByteBuffer, length: Int): ByteBuffer {
        if (buffer.remaining() >= length) return buffer
        val allocated = ByteBuffer.allocate(buffer.position() + length shl 1)
        // This cast prevents issues with backwards compatibility in the ABI (java 11 made breaking changes)
        (buffer as Buffer).flip()
        allocated.put(buffer)
        return allocated
    }

    private fun packMap(buffer: ByteBuffer, data: Map<String, Any>): ByteBuffer {
        var buffer = buffer
        buffer = realloc(buffer, data.size + 5)
        buffer.put(ExTermTag.MAP)
        buffer.putInt(data.size)
        for ((key, value) in data) {
            buffer = packBinary(buffer, key)
            buffer = pack(buffer, value)
        }
        return buffer
    }

    private fun packList(buffer: ByteBuffer, data: Collection<Any>): ByteBuffer {
        var buffer = buffer
        if (data.isEmpty()) {
            // NIL is for empty lists
            return packNil(buffer)
        }
        buffer = realloc(buffer, data.size + 6)
        buffer.put(ExTermTag.LIST)
        buffer.putInt(data.size)
        for (element in data) buffer = pack(buffer, element)
        return packNil(buffer)
    }

    private fun packBinary(buffer: ByteBuffer, value: String): ByteBuffer {
        var buffer = buffer
        val encoded = value.toByteArray(StandardCharsets.UTF_8)
        buffer = realloc(buffer, encoded.size * 4 + 5)
        buffer.put(ExTermTag.BINARY)
        buffer.putInt(encoded.size)
        buffer.put(encoded)
        return buffer
    }

    private fun packSmallInt(buffer: ByteBuffer, value: Byte): ByteBuffer {
        var buffer = buffer
        buffer = realloc(buffer, 2)
        buffer.put(ExTermTag.SMALL_INT)
        buffer.put(value)
        return buffer
    }

    private fun packInt(buffer: ByteBuffer, value: Int): ByteBuffer {
        var buffer = buffer
        if (countBytes(value.toLong()) <= 1 && value >= 0) return packSmallInt(buffer, value.toByte())
        buffer = realloc(buffer, 5)
        buffer.put(ExTermTag.INT)
        buffer.putInt(value)
        return buffer
    }

    private fun packLong(buffer: ByteBuffer, value: Long): ByteBuffer {
        var buffer = buffer
        var value = value
        val bytes = countBytes(value)
        if (bytes <= 1) // Use optimized small int encoding
            return packSmallInt(buffer, value.toByte())
        if (bytes <= 4 && value >= 0) {
            // Use int to encode it
            buffer = realloc(buffer, 5)
            buffer.put(ExTermTag.INT)
            buffer.putInt(value.toInt())
            return buffer
        }
        buffer = realloc(buffer, 3 + bytes)
        buffer.put(ExTermTag.SMALL_BIGINT)
        buffer.put(bytes)
        // We only use "unsigned" value so the sign is always positive
        buffer.put(0.toByte())
        while (value > 0) {
            buffer.put(value.toByte())
            value = value ushr 8
        }
        return buffer
    }

    private fun packFloat(buffer: ByteBuffer, value: Double): ByteBuffer {
        var buffer = buffer
        buffer = realloc(buffer, 9)
        buffer.put(ExTermTag.NEW_FLOAT)
        buffer.putDouble(value)
        return buffer
    }

    private fun packAtom(buffer: ByteBuffer, value: String): ByteBuffer {
        var buffer = buffer
        val array = value.toByteArray(StandardCharsets.ISO_8859_1)
        buffer = realloc(buffer, array.size + 3)
        buffer.put(ExTermTag.ATOM)
        buffer.putShort(array.size.toShort())
        buffer.put(array)
        return buffer
    }

    private fun packArray(buffer: ByteBuffer, array: LongArray): ByteBuffer {
        var buffer = buffer
        if (array.size == 0) return packNil(buffer)
        buffer = realloc(buffer, array.size * 8 + 6)
        buffer.put(ExTermTag.LIST)
        buffer.putInt(array.size)
        for (it in array) buffer = packLong(buffer, it)
        return packNil(buffer)
    }

    private fun packArray(buffer: ByteBuffer, array: IntArray): ByteBuffer {
        var buffer = buffer
        if (array.size == 0) return packNil(buffer)
        buffer = realloc(buffer, array.size * 4 + 6)
        buffer.put(ExTermTag.LIST)
        buffer.putInt(array.size)
        for (it in array) buffer = packInt(buffer, it)
        return packNil(buffer)
    }

    private fun packArray(buffer: ByteBuffer, array: ShortArray): ByteBuffer {
        var buffer = buffer
        if (array.size == 0) return packNil(buffer)
        buffer = realloc(buffer, array.size * 2 + 6)
        buffer.put(ExTermTag.LIST)
        buffer.putInt(array.size)
        for (it in array) buffer = packInt(buffer, it.toInt())
        return packNil(buffer)
    }

    private fun packArray(buffer: ByteBuffer, array: ByteArray): ByteBuffer {
        var buffer = buffer
        if (array.size == 0) return packNil(buffer)
        buffer = realloc(buffer, array.size + 6)
        buffer.put(ExTermTag.LIST)
        buffer.putInt(array.size)
        for (it in array) buffer = packSmallInt(buffer, it)
        return packNil(buffer)
    }

    private fun packNil(buffer: ByteBuffer): ByteBuffer {
        var buffer = buffer
        buffer = realloc(buffer, 1)
        buffer.put(ExTermTag.NIL)
        return buffer
    }

    private fun countBytes(value: Long): Byte {
        val leadingZeros = java.lang.Long.numberOfLeadingZeros(value)
        return ceil((64 - leadingZeros) / 8.0).toByte()
    }
}
