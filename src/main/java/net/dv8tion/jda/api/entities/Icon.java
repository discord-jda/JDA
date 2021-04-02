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

package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.managers.AccountManager;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.IOUtil;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Icon containing a base64 encoded jpeg/png/gif/gifv image.
 * <br>Used to represent various base64 images in the Discord api.
 * <br>Example: {@link AccountManager#setAvatar(Icon)}.
 *
 * @since 3.0
 *
 * @see #from(File)
 * @see #from(byte[])
 * @see #from(InputStream)
 *
 * @see #from(File, IconType)
 * @see #from(byte[], IconType)
 * @see #from(InputStream, IconType)
 */
public class Icon
{
    protected final String encoding;

    protected Icon(@Nonnull IconType type, @Nonnull String base64Encoding)
    {
        //Note: the usage of `image/jpeg` does not mean png/gif are not supported!
        this.encoding = type.getHeader() + base64Encoding;
    }

    /**
     * The base64 encoded data for this Icon
     *
     * @return String representation of the encoded data for this icon
     */
    @Nonnull
    public String getEncoding()
    {
        return encoding;
    }

    /**
     * Creates an {@link Icon Icon} with the specified {@link java.io.File File}.
     * <br>We here read the specified File and forward the retrieved byte data to {@link #from(byte[], IconType)}.
     *
     * @param  file
     *         An existing, not-null file.
     *
     * @throws IllegalArgumentException
     *         if the provided file is null, does not exist, or has an unsupported extension
     * @throws IOException
     *         if there is a problem while reading the file.
     *
     * @return An Icon instance representing the specified File
     */
    @Nonnull
    public static Icon from(@Nonnull File file) throws IOException
    {
        Checks.notNull(file, "Provided File");
        Checks.check(file.exists(), "Provided file does not exist!");
        int index = file.getName().lastIndexOf('.');
        if (index < 0)
            return from(file, IconType.JPEG);
        String ext = file.getName().substring(index + 1);
        IconType type = IconType.fromExtension(ext);
        return from(file, type);
    }

    /**
     * Creates an {@link Icon Icon} with the specified {@link java.io.InputStream InputStream}.
     * <br>We here read the specified InputStream and forward the retrieved byte data to {@link #from(byte[], IconType)}.
     * This will use {@link net.dv8tion.jda.api.entities.Icon.IconType#JPEG} but discord is capable for
     * interpreting other types correctly either way.
     *
     * @param  stream
     *         A not-null InputStream.
     *
     * @throws IllegalArgumentException
     *         if the provided stream is null
     * @throws IOException
     *         If the first byte cannot be read for any reason other than the end of the file,
     *         if the input stream has been closed, or if some other I/O error occurs.
     *
     * @return An Icon instance representing the specified InputStream
     */
    @Nonnull
    public static Icon from(@Nonnull InputStream stream) throws IOException
    {
        return from(stream, IconType.JPEG);
    }

    /**
     * Creates an {@link Icon Icon} with the specified image data.
     * This will use {@link net.dv8tion.jda.api.entities.Icon.IconType#JPEG} but discord is capable for
     * interpreting other types correctly either way.
     *
     * @param  data
     *         not-null image data bytes.
     *
     * @throws IllegalArgumentException
     *         if the provided data is null
     *
     * @return An Icon instance representing the specified image data
     */
    @Nonnull
    public static Icon from(@Nonnull byte[] data)
    {
        return from(data, IconType.JPEG);
    }

    /**
     * Creates an {@link Icon Icon} with the specified {@link java.io.File File}.
     * <br>We here read the specified File and forward the retrieved byte data to {@link #from(byte[], IconType)}.
     *
     * @param  file
     *         An existing, not-null file.
     * @param  type
     *         The type of image
     *
     * @throws IllegalArgumentException
     *         if the provided file is either null or does not exist
     * @throws IOException
     *         if there is a problem while reading the file.
     *
     * @return An Icon instance representing the specified File
     */
    @Nonnull
    public static Icon from(@Nonnull File file, @Nonnull IconType type) throws IOException
    {
        Checks.notNull(file, "Provided File");
        Checks.notNull(type, "IconType");
        Checks.check(file.exists(), "Provided file does not exist!");

        return from(IOUtil.readFully(file), type);
    }

    /**
     * Creates an {@link Icon Icon} with the specified {@link java.io.InputStream InputStream}.
     * <br>We here read the specified InputStream and forward the retrieved byte data to {@link #from(byte[], IconType)}.
     *
     * @param  stream
     *         A not-null InputStream.
     * @param  type
     *         The type of image
     *
     * @throws IllegalArgumentException
     *         if the provided stream is null
     * @throws IOException
     *         If the first byte cannot be read for any reason other than the end of the file,
     *         if the input stream has been closed, or if some other I/O error occurs.
     *
     * @return An Icon instance representing the specified InputStream
     */
    @Nonnull
    public static Icon from(@Nonnull InputStream stream, @Nonnull IconType type) throws IOException
    {
        Checks.notNull(stream, "InputStream");
        Checks.notNull(type, "IconType");

        return from(IOUtil.readFully(stream), type);
    }

    /**
     * Creates an {@link Icon Icon} with the specified image data.
     *
     * @param  data
     *         not-null image data bytes.
     * @param  type
     *         The type of image
     *
     * @throws IllegalArgumentException
     *         if the provided data is null
     *
     * @return An Icon instance representing the specified image data
     */
    @Nonnull
    public static Icon from(@Nonnull byte[] data, @Nonnull IconType type)
    {
        Checks.notNull(data, "Provided byte[]");
        Checks.notNull(type, "IconType");

        return new Icon(type, new String(Base64.getEncoder().encode(data), StandardCharsets.UTF_8));
    }

    /**
     * Supported image types for the Discord API.
     */
    public enum IconType
    {
        /** JPEG */
        JPEG("image/jpeg"),
        /** PNG */
        PNG("image/png"),
        /** WEBP */
        WEBP("image/webp"),
        /** GIF */
        GIF("image/gif"),

        /** Placeholder for unsupported IconTypes */
        UNKNOWN("image/jpeg");

        private final String mime;
        private final String header;

        IconType(@Nonnull String mime)
        {
            this.mime = mime;
            this.header = "data:" + mime + ";base64,";
        }

        /**
         * The MIME Type
         *
         * @return The MIME Type
         *
         * @see    <a href="https://en.wikipedia.org/wiki/MIME" target="_blank">MIME</a>
         */
        @Nonnull
        public String getMIME()
        {
            return mime;
        }

        /**
         * The data header for the encoding of an image.
         *
         * @return The data header
         */
        @Nonnull
        public String getHeader()
        {
            return header;
        }

        /**
         * Resolves the provided MIME Type to the equivalent IconType.
         * <br>If the type is not supported, {@link #UNKNOWN} is returned.
         *
         * @param  mime
         *         The MIME type
         *
         * @return The resolved IconType or {@link #UNKNOWN}.
         */
        @Nonnull
        public static IconType fromMIME(@Nonnull String mime)
        {
            Checks.notNull(mime, "MIME Type");
            for (IconType type : values())
            {
                if (type.mime.equalsIgnoreCase(mime))
                    return type;
            }
            return UNKNOWN;
        }

        /**
         * Resolves the provided file extension type to the equivalent IconType.
         * <br>If the type is not supported, {@link #UNKNOWN} is returned.
         *
         * @param  extension
         *         The extension type
         *
         * @return The resolved IconType or {@link #UNKNOWN}.
         */
        @Nonnull
        public static IconType fromExtension(@Nonnull String extension)
        {
            Checks.notNull(extension, "Extension Type");
            switch (extension.toLowerCase())
            {
                case "jpe":
                case "jif":
                case "jfif":
                case "jfi":
                case "jpg":
                case "jpeg":
                    return JPEG;
                case "png":
                    return PNG;
                case "webp":
                    return WEBP;
                case "gif":
                    return GIF;
            }
            return UNKNOWN;
        }
    }
}
