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
package net.dv8tion.jda.api.entities.sticker

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.utils.ImageProxy
import net.dv8tion.jda.api.utils.MiscUtil
import net.dv8tion.jda.internal.utils.Helpers
import javax.annotation.Nonnull

/**
 * Abstract representation of all sticker types.
 *
 *
 * This is specialized in [StandardSticker] and [GuildSticker].
 */
interface Sticker : StickerSnowflake {
    @get:Nonnull
    val formatType: StickerFormat

    @JvmField
    @get:Nonnull
    val name: String?

    @get:Nonnull
    val iconUrl: String?
        /**
         * The url of the sticker image.
         * <br></br>Note that [LOTTIE][StickerFormat.LOTTIE] stickers don't provide an image, but a JSON format.
         *
         * @throws java.lang.IllegalStateException
         * If the [StickerFormat] of this sticker is [UNKNOWN][StickerFormat.UNKNOWN]
         *
         * @return The image url of the sticker
         */
        get() = Helpers.format(ICON_URL, id, formatType.getExtension())

    @get:Nonnull
    val icon: ImageProxy?
        /**
         * Returns an [ImageProxy] for this sticker's image.
         *
         *
         * The size parameter for [ImageProxy] is not supported for stickers of type [LOTTIE][StickerFormat.LOTTIE].
         *
         * @throws java.lang.IllegalStateException
         * If the [StickerFormat] of this sticker is [UNKNOWN][StickerFormat.UNKNOWN]
         *
         * @return Never-null [ImageProxy] of this sticker's image
         *
         * @see .getIconUrl
         */
        get() = ImageProxy(iconUrl!!)

    /**
     * The various formats used for stickers and the respective file extensions.
     */
    enum class StickerFormat(private val id: Int, private val extension: String?) {
        /**
         * The PNG format.
         */
        PNG(1, "png"),

        /**
         * The APNG format.
         */
        APNG(2, "png"),

        /**
         * The LOTTIE format.
         * <br></br>Lottie isn't a standard renderable image. It is a JSON with data that can be rendered using the lottie library.
         *
         * @see [Lottie website](https://airbnb.io/lottie/)
         */
        LOTTIE(3, "json"),

        /**
         * The GIF format.
         */
        GIF(4, "gif"),

        /**
         * Represents any unknown or unsupported format types.
         */
        UNKNOWN(-1, null);

        /**
         * The file extension used for the sticker asset.
         *
         * @throws java.lang.IllegalStateException
         * If the [StickerFormat] is [UNKNOWN][StickerFormat.UNKNOWN]
         *
         * @return The file extension for this format
         */
        @Nonnull
        fun getExtension(): String? {
            check(this != UNKNOWN) { "Cannot get file extension for StickerFormat.UNKNOWN" }
            return extension
        }

        companion object {
            /**
             * Resolves the specified format identifier to the StickerFormat enum constant.
             *
             * @param  id
             * The id for this format
             *
             * @return The representative StickerFormat or UNKNOWN if it can't be resolved
             */
            @JvmStatic
            @Nonnull
            fun fromId(id: Int): StickerFormat {
                for (stickerFormat in entries) {
                    if (stickerFormat.id == id) return stickerFormat
                }
                return UNKNOWN
            }
        }
    }

    /**
     * The specific types of stickers
     */
    enum class Type(
        /**
         * The Discord defined id key for this sticker type.
         *
         * @return the id key
         */
        @JvmField val id: Int
    ) {
        /**
         * A sticker provided by nitro sticker packs. Such as wumpus or doggo stickers.
         * <br></br>These are also used for the wave buttons on welcome messages.
         */
        STANDARD(1),

        /**
         * A custom sticker created for a [Guild][net.dv8tion.jda.api.entities.Guild].
         */
        GUILD(2),

        /**
         * Placeholder for future stickers which are currently unsupported.
         */
        UNKNOWN(-1);

        companion object {
            /**
             * Gets the sticker type related to the provided id.
             * <br></br>If an unknown id is provided, this returns [.UNKNOWN].
             *
             * @param  id
             * The raw id for the type
             *
             * @return The Type that has the key provided, or [.UNKNOWN]
             */
            @JvmStatic
            @Nonnull
            fun fromId(id: Int): Type {
                for (type in entries) {
                    if (type.id == id) return type
                }
                return UNKNOWN
            }
        }
    }

    companion object {
        /**
         * Creates a sticker snowflake instance which only wraps an ID.
         *
         *
         * This is primarily used for message sending purposes.
         *
         * @param  id
         * The sticker id
         *
         * @return A sticker snowflake instance
         *
         * @see JDA.retrieveSticker
         */
        @JvmStatic
        @Nonnull
        fun fromId(id: Long): StickerSnowflake? {
            return StickerSnowflake.Companion.fromId(id)
        }

        /**
         * Creates a sticker snowflake instance which only wraps an ID.
         *
         *
         * This is primarily used for message sending purposes.
         *
         * @param  id
         * The sticker id
         *
         * @throws IllegalArgumentException
         * If the provided ID is not a valid snowflake
         *
         * @return A sticker snowflake instance
         *
         * @see JDA.retrieveSticker
         */
        @Nonnull
        fun fromId(@Nonnull id: String?): StickerSnowflake? {
            return fromId(MiscUtil.parseSnowflake(id))
        }

        /** Template for [.getIconUrl]  */
        const val ICON_URL = "https://cdn.discordapp.com/stickers/%s.%s"
    }
}
