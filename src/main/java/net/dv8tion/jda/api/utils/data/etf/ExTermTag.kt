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

/**
 * Tags used for encoding and decoding for external terms.
 * <br></br>This list in incomplete as not all tags are used by this library.
 *
 * @since  4.2.1
 */
object ExTermTag {
    // 8 IEEE float
    const val NEW_FLOAT: Byte = 70

    // 4 Uncompressed Size | N ZLIB Compressed Data
    const val COMPRESSED: Byte = 80

    // 1 byte unsigned int value
    const val SMALL_INT: Byte = 97

    // 4 byte signed int value
    const val INT: Byte = 98

    // 31 bytes string of float value sprintf("%.20e")
    const val FLOAT: Byte = 99 // deprecated

    // 2 byte length | N bytes latin-1 string
    const val ATOM: Byte = 100 // deprecated

    // 0 bytes empty list
    const val NIL: Byte = 106

    // 2 byte length | N bytes
    const val STRING: Byte = 107

    // 4 byte length | N tags | 1 byte NIL tag
    const val LIST: Byte = 108

    // 4 byte length | N bytes UTF-8 string
    const val BINARY: Byte = 109

    // 1 byte length | 1 byte sign | N bytes unsigned SE int
    const val SMALL_BIGINT: Byte = 110

    // 1 byte length | N bytes latin-1 string
    const val SMALL_ATOM: Byte = 115 // deprecated

    // 4 byte length | N pairs of KEY,VALUE terms
    const val MAP: Byte = 116

    // 2 byte length | N bytes of UTF-8 string
    const val ATOM_UTF8: Byte = 118

    // 1 byte length | N bytes of UTF-8 string
    const val SMALL_ATOM_UTF8: Byte = 119
}
