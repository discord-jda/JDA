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
import net.dv8tion.jda.api.entities.ISnowflake
import net.dv8tion.jda.api.utils.MiscUtil
import net.dv8tion.jda.internal.entities.sticker.StickerSnowflakeImpl
import javax.annotation.Nonnull

/**
 * Represents an abstract sticker reference by only the sticker ID.
 *
 *
 * This is used for methods which only need a sticker ID to function, you cannot use this for getting names or similar.
 * To get information about a sticker by their ID you can use [JDA.retrieveSticker] instead.
 */
interface StickerSnowflake : ISnowflake {
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
            return StickerSnowflakeImpl(id)
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
    }
}
