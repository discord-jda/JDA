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

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.zip.InflaterOutputStream

/**
 * Decodes an ETF encoded payload to a java object representation.
 *
 * @see .unpack
 * @see .unpackMap
 * @see .unpackList
 * @since  4.2.1
 */
object ExTermDecoder {
    /**
     * Unpacks the provided term into a java object.
     *
     *
     * **The mapping is as follows:**<br></br>
     *
     *  * `Small Int | Int -> Integer`
     *  * `Small BigInt -> Long`
     *  * `Float | New Float -> Double`
     *  * `Small Atom | Atom -> Boolean | null | String`
     *  * `Binary | String -> String`
     *  * `List | NIL -> List`
     *  * `Map -> Map`
     *
     *
     * @param  buffer
     * The [ByteBuffer] containing the encoded term
     *
     * @throws IllegalArgumentException
     * If the buffer does not start with the version byte `131` or contains an unsupported tag
     *
     * @return The java object
     */
    fun unpack(buffer: ByteBuffer): Any? {
        require(buffer.get().toInt() == -125) { "Failed header check" }
        return unpack0(buffer)
    }

    /**
     * Unpacks the provided term into a java [Map].
     *
     *
     * **The mapping is as follows:**<br></br>
     *
     *  * `Small Int | Int -> Integer`
     *  * `Small BigInt -> Long`
     *  * `Float | New Float -> Double`
     *  * `Small Atom | Atom -> Boolean | null | String`
     *  * `Binary | String -> String`
     *  * `List | NIL -> List`
     *  * `Map -> Map`
     *
     *
     * @param  buffer
     * The [ByteBuffer] containing the encoded term
     *
     * @throws IllegalArgumentException
     * If the buffer does not start with a Map term, does not have the right version byte, or the format includes an unsupported tag
     *
     * @return The parsed [Map] instance
     */
    @JvmStatic
    fun unpackMap(buffer: ByteBuffer): Map<String, Any>? {
        val tag = buffer[1]
        require(tag == ExTermTag.MAP) { "Cannot unpack map from tag $tag" }
        return unpack(buffer) as Map<String, Any>?
    }

    /**
     * Unpacks the provided term into a java [List].
     *
     *
     * **The mapping is as follows:**<br></br>
     *
     *  * `Small Int | Int -> Integer`
     *  * `Small BigInt -> Long`
     *  * `Float | New Float -> Double`
     *  * `Small Atom | Atom -> Boolean | null | String`
     *  * `Binary | String -> String`
     *  * `List | NIL -> List`
     *  * `Map -> Map`
     *
     *
     * @param  buffer
     * The [ByteBuffer] containing the encoded term
     *
     * @throws IllegalArgumentException
     * If the buffer does not start with a List or NIL term, does not have the right version byte, or the format includes an unsupported tag
     *
     * @return The parsed [List] instance
     */
    @JvmStatic
    fun unpackList(buffer: ByteBuffer): List<Any>? {
        val tag = buffer[1]
        require(tag == ExTermTag.LIST) { "Cannot unpack list from tag $tag" }
        return unpack(buffer) as List<Any>?
    }

    private fun unpack0(buffer: ByteBuffer): Any? {
        val tag = buffer.get().toInt()
        return when (tag) {
            ExTermTag.COMPRESSED -> unpackCompressed(buffer)
            ExTermTag.SMALL_INT -> unpackSmallInt(buffer)
            ExTermTag.SMALL_BIGINT -> unpackSmallBigint(buffer)
            ExTermTag.INT -> unpackInt(buffer)
            ExTermTag.FLOAT -> unpackOldFloat(buffer)
            ExTermTag.NEW_FLOAT -> unpackFloat(buffer)
            ExTermTag.SMALL_ATOM_UTF8 -> unpackSmallAtom(buffer, StandardCharsets.UTF_8)
            ExTermTag.SMALL_ATOM -> unpackSmallAtom(buffer, StandardCharsets.ISO_8859_1)
            ExTermTag.ATOM_UTF8 -> unpackAtom(buffer, StandardCharsets.UTF_8)
            ExTermTag.ATOM -> unpackAtom(buffer, StandardCharsets.ISO_8859_1)
            ExTermTag.MAP -> unpackMap0(buffer)
            ExTermTag.LIST -> unpackList0(buffer)
            ExTermTag.NIL -> emptyList<Any>()
            ExTermTag.STRING -> unpackString(buffer)
            ExTermTag.BINARY -> unpackBinary(buffer)
            else -> throw IllegalArgumentException("Unknown tag $tag")
        }
    }

    private fun unpackCompressed(buffer: ByteBuffer): Any? {
        var buffer = buffer
        val size = buffer.getInt()
        val decompressed = ByteArrayOutputStream(size)
        try {
            InflaterOutputStream(decompressed).use { inflater ->
                inflater.write(
                    buffer.array(),
                    buffer.position(),
                    buffer.remaining()
                )
            }
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }
        buffer = ByteBuffer.wrap(decompressed.toByteArray())
        return unpack0(buffer)
    }

    private fun unpackOldFloat(buffer: ByteBuffer): Double {
        val bytes = getString(buffer, StandardCharsets.ISO_8859_1, 31)
        return bytes.toDouble()
    }

    private fun unpackFloat(buffer: ByteBuffer): Double {
        return buffer.getDouble()
    }

    private fun unpackSmallBigint(buffer: ByteBuffer): Long {
        var arity = java.lang.Byte.toUnsignedInt(buffer.get())
        val sign = java.lang.Byte.toUnsignedInt(buffer.get())
        var sum: Long = 0
        var offset: Long = 0
        while (arity-- > 0) {
            sum += java.lang.Byte.toUnsignedLong(buffer.get()) shl offset.toInt()
            offset += 8
        }
        return if (sign == 0) sum else -sum
    }

    private fun unpackSmallInt(buffer: ByteBuffer): Int {
        return java.lang.Byte.toUnsignedInt(buffer.get())
    }

    private fun unpackInt(buffer: ByteBuffer): Int {
        return buffer.getInt()
    }

    private fun unpackString(buffer: ByteBuffer): List<Any> {
        var length = java.lang.Short.toUnsignedInt(buffer.getShort())
        val bytes: MutableList<Any> = ArrayList(length)
        while (length-- > 0) bytes.add(buffer.get())
        return bytes
    }

    private fun unpackBinary(buffer: ByteBuffer): String {
        val length = buffer.getInt()
        return getString(buffer, StandardCharsets.UTF_8, length)
    }

    private fun unpackSmallAtom(buffer: ByteBuffer, charset: Charset): Any? {
        val length = java.lang.Byte.toUnsignedInt(buffer.get())
        return unpackAtom(buffer, charset, length)
    }

    private fun unpackAtom(buffer: ByteBuffer, charset: Charset): Any? {
        val length = java.lang.Short.toUnsignedInt(buffer.getShort())
        return unpackAtom(buffer, charset, length)
    }

    private fun unpackAtom(buffer: ByteBuffer, charset: Charset, length: Int): Any? {
        val value = getString(buffer, charset, length)
        return when (value) {
            "true" -> true
            "false" -> false
            "nil" -> null
            else -> value.intern()
        }
    }

    private fun getString(buffer: ByteBuffer, charset: Charset, length: Int): String {
        val array = ByteArray(length)
        buffer[array]
        return String(array, charset)
    }

    private fun unpackList0(buffer: ByteBuffer): List<Any?> {
        var length = buffer.getInt()
        val list: MutableList<Any?> = ArrayList(length)
        while (length-- > 0) {
            list.add(unpack0(buffer))
        }
        val tail = unpack0(buffer)
        require(tail === emptyList<Any>()) { "Unexpected tail $tail" }
        return list
    }

    private fun unpackMap0(buffer: ByteBuffer): Map<String, Any?> {
        val map: MutableMap<String, Any?> = HashMap()
        var arity = buffer.getInt()
        while (arity-- > 0) {
            val rawKey = unpack0(buffer)
            val key = rawKey.toString()
            val value = unpack0(buffer)
            map[key] = value
        }
        return map
    }
}
