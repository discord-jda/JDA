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
package net.dv8tion.jda.api.entities

import net.dv8tion.jda.api.managers.AccountManager
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.IOUtil
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.*
import javax.annotation.Nonnull

/**
 * Icon containing a base64 encoded jpeg/png/gif/gifv image.
 * <br></br>Used to represent various base64 images in the Discord api.
 * <br></br>Example: [AccountManager.setAvatar].
 *
 * @since 3.0
 *
 * @see .from
 * @see .from
 * @see .from
 * @see .from
 * @see .from
 * @see .from
 */
class Icon protected constructor(@Nonnull type: IconType, @Nonnull base64Encoding: String) {
    /**
     * The base64 encoded data for this Icon
     *
     * @return String representation of the encoded data for this icon
     */
    @JvmField
    @get:Nonnull
    val encoding: String

    init {
        //Note: the usage of `image/jpeg` does not mean png/gif are not supported!
        encoding = type.header + base64Encoding
    }

    /**
     * Supported image types for the Discord API.
     */
    enum class IconType(
        /**
         * The MIME Type
         *
         * @return The MIME Type
         *
         * @see [MIME](https://en.wikipedia.org/wiki/MIME)
         */
        @get:Nonnull
        @param:Nonnull val mIME: String
    ) {
        /** JPEG  */
        JPEG("image/jpeg"),

        /** PNG  */
        PNG("image/png"),

        /** WEBP  */
        WEBP("image/webp"),

        /** GIF  */
        GIF("image/gif"),

        /** Placeholder for unsupported IconTypes  */
        UNKNOWN("image/jpeg");

        /**
         * The data header for the encoding of an image.
         *
         * @return The data header
         */
        @get:Nonnull
        val header: String

        init {
            header = "data:$mIME;base64,"
        }

        companion object {
            /**
             * Resolves the provided MIME Type to the equivalent IconType.
             * <br></br>If the type is not supported, [.UNKNOWN] is returned.
             *
             * @param  mime
             * The MIME type
             *
             * @return The resolved IconType or [.UNKNOWN].
             */
            @Nonnull
            fun fromMIME(@Nonnull mime: String?): IconType {
                Checks.notNull(mime, "MIME Type")
                for (type in entries) {
                    if (type.mIME.equals(mime, ignoreCase = true)) return type
                }
                return UNKNOWN
            }

            /**
             * Resolves the provided file extension type to the equivalent IconType.
             * <br></br>If the type is not supported, [.UNKNOWN] is returned.
             *
             * @param  extension
             * The extension type
             *
             * @return The resolved IconType or [.UNKNOWN].
             */
            @Nonnull
            fun fromExtension(@Nonnull extension: String): IconType {
                Checks.notNull(extension, "Extension Type")
                when (extension.lowercase(Locale.getDefault())) {
                    "jpe", "jif", "jfif", "jfi", "jpg", "jpeg" -> return JPEG
                    "png" -> return PNG
                    "webp" -> return WEBP
                    "gif" -> return GIF
                }
                return UNKNOWN
            }
        }
    }

    companion object {
        /**
         * Creates an [Icon] with the specified [File][java.io.File].
         * <br></br>We here read the specified File and forward the retrieved byte data to [.from].
         *
         * @param  file
         * An existing, not-null file.
         *
         * @throws IllegalArgumentException
         * if the provided file is null, does not exist, or has an unsupported extension
         * @throws IOException
         * if there is a problem while reading the file.
         *
         * @return An Icon instance representing the specified File
         */
        @Nonnull
        @Throws(IOException::class)
        fun from(@Nonnull file: File): Icon {
            Checks.notNull(file, "Provided File")
            Checks.check(file.exists(), "Provided file does not exist!")
            val index = file.getName().lastIndexOf('.')
            if (index < 0) return from(file, IconType.JPEG)
            val ext = file.getName().substring(index + 1)
            val type = IconType.fromExtension(ext)
            return from(file, type)
        }

        /**
         * Creates an [Icon] with the specified [InputStream][java.io.InputStream].
         * <br></br>We here read the specified InputStream and forward the retrieved byte data to [.from].
         * This will use [net.dv8tion.jda.api.entities.Icon.IconType.JPEG] but discord is capable for
         * interpreting other types correctly either way.
         *
         * @param  stream
         * A not-null InputStream.
         *
         * @throws IllegalArgumentException
         * if the provided stream is null
         * @throws IOException
         * If the first byte cannot be read for any reason other than the end of the file,
         * if the input stream has been closed, or if some other I/O error occurs.
         *
         * @return An Icon instance representing the specified InputStream
         */
        @JvmStatic
        @Nonnull
        @Throws(IOException::class)
        fun from(@Nonnull stream: InputStream?): Icon {
            return from(stream, IconType.JPEG)
        }

        /**
         * Creates an [Icon] with the specified image data.
         * This will use [net.dv8tion.jda.api.entities.Icon.IconType.JPEG] but discord is capable for
         * interpreting other types correctly either way.
         *
         * @param  data
         * not-null image data bytes.
         *
         * @throws IllegalArgumentException
         * if the provided data is null
         *
         * @return An Icon instance representing the specified image data
         */
        @Nonnull
        fun from(@Nonnull data: ByteArray?): Icon {
            return from(data, IconType.JPEG)
        }

        /**
         * Creates an [Icon] with the specified [File][java.io.File].
         * <br></br>We here read the specified File and forward the retrieved byte data to [.from].
         *
         * @param  file
         * An existing, not-null file.
         * @param  type
         * The type of image
         *
         * @throws IllegalArgumentException
         * if the provided file is either null or does not exist
         * @throws IOException
         * if there is a problem while reading the file.
         *
         * @return An Icon instance representing the specified File
         */
        @Nonnull
        @Throws(IOException::class)
        fun from(@Nonnull file: File, @Nonnull type: IconType): Icon {
            Checks.notNull(file, "Provided File")
            Checks.notNull(type, "IconType")
            Checks.check(file.exists(), "Provided file does not exist!")
            return from(IOUtil.readFully(file), type)
        }

        /**
         * Creates an [Icon] with the specified [InputStream][java.io.InputStream].
         * <br></br>We here read the specified InputStream and forward the retrieved byte data to [.from].
         *
         * @param  stream
         * A not-null InputStream.
         * @param  type
         * The type of image
         *
         * @throws IllegalArgumentException
         * if the provided stream is null
         * @throws IOException
         * If the first byte cannot be read for any reason other than the end of the file,
         * if the input stream has been closed, or if some other I/O error occurs.
         *
         * @return An Icon instance representing the specified InputStream
         */
        @Nonnull
        @Throws(IOException::class)
        fun from(@Nonnull stream: InputStream?, @Nonnull type: IconType): Icon {
            Checks.notNull(stream, "InputStream")
            Checks.notNull(type, "IconType")
            return from(IOUtil.readFully(stream), type)
        }

        /**
         * Creates an [Icon] with the specified image data.
         *
         * @param  data
         * not-null image data bytes.
         * @param  type
         * The type of image
         *
         * @throws IllegalArgumentException
         * if the provided data is null
         *
         * @return An Icon instance representing the specified image data
         */
        @Nonnull
        fun from(@Nonnull data: ByteArray?, @Nonnull type: IconType): Icon {
            Checks.notNull(data, "Provided byte[]")
            Checks.notNull(type, "IconType")
            return Icon(type, String(Base64.getEncoder().encode(data), StandardCharsets.UTF_8))
        }
    }
}
