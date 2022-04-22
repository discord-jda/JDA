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

package net.dv8tion.jda.api.entities.sticker;

import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;

public interface Sticker extends ISnowflake
{
    /** Template for {@link #getIconUrl()} */
    String ICON_URL = "https://cdn.discordapp.com/stickers/%s.%s";

    StickerFormat getFormatType();

    /**
     * The name of the sticker.
     *
     * @return the name of the sticker
     */
    @Nonnull
    String getName();

    /**
     * The url of the sticker image.
     *
     * @throws java.lang.IllegalStateException
     *         If the {@link StickerFormat StickerFormat} of this sticker is {@link StickerFormat#UNKNOWN UNKNOWN}
     *
     * @return The image url of the sticker
     */
    @Nonnull
    default String getIconUrl()
    {
        return Helpers.format(ICON_URL, getId(), getFormatType().getExtension());
    }

    // TODO getIconProxy

    enum StickerFormat
    {
        /**
         * The PNG format.
         */
        PNG(1, "png"),
        /**
         * The APNG format.
         */
        APNG(2, "apng"),
        /**
         * The LOTTIE format.
         * <br>Lottie isn't a standard renderable image. It is a JSON with data that can be rendered using the lottie library.
         *
         * @see <a href="https://airbnb.io/lottie/">Lottie website</a>
         */
        LOTTIE(3, "json"),
        /**
         * Represents any unknown or unsupported {@link net.dv8tion.jda.api.entities.MessageSticker MessageSticker} format types.
         */
        UNKNOWN(-1, null);

        private final int id;
        private final String extension;

        StickerFormat(final int id, final String extension)
        {
            this.id = id;
            this.extension = extension;
        }

        /**
         * The file extension used for the sticker asset.
         *
         * @throws java.lang.IllegalStateException
         *         If the {@link StickerFormat StickerFormat} is {@link StickerFormat#UNKNOWN UNKNOWN}
         *
         * @return The file extension for this format
         */
        @Nonnull
        public String getExtension()
        {
            if (this == UNKNOWN)
                throw new IllegalStateException("Can only get extension of a known format");
            return extension;
        }

        /**
         * Resolves the specified format identifier to the StickerFormat enum constant.
         *
         * @param  id
         *         The id for this format
         *
         * @return The representative StickerFormat or UNKNOWN if it can't be resolved
         */
        @Nonnull
        public static StickerFormat fromId(int id)
        {
            for (StickerFormat stickerFormat : values())
            {
                if (stickerFormat.id == id)
                    return stickerFormat;
            }
            return UNKNOWN;
        }
    }

    enum Type
    {
        STANDARD(1),
        GUILD(2),
        UNKNOWN(-1);

        private final int id;

        Type(int id)
        {
            this.id = id;
        }

        public static Type fromId(int id)
        {
            for (Type type : values())
            {
                if (type.id == id)
                    return type;
            }
            return UNKNOWN;
        }
    }
}
