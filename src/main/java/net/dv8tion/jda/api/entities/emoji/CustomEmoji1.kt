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
package net.dv8tion.jda.api.entities.emoji

import net.dv8tion.jda.api.entities.IMentionable
import net.dv8tion.jda.api.utils.ImageProxy
import java.util.*
import javax.annotation.Nonnull

/**
 * Represents a minimal custom emoji.
 *
 *
 * This contains the most minimal representation of a custom emoji, via id and name.
 *
 *
 * **This does not represent unicode emojis like they are used in the official client!
 * The format `:smiley:` is a client-side alias which is replaced by the unicode emoji, not a custom emoji.**
 *
 * @see Emoji.fromCustom
 * @see Emoji.fromCustom
 * @see Emoji.fromFormatted
 * @see Emoji.fromData
 */
interface CustomEmoji : Emoji, IMentionable {
    @get:Nonnull
    override val type: Emoji.Type?
        get() = Emoji.Type.CUSTOM

    /**
     * Whether this emoji is animated.
     *
     * @return True, if this emoji is animated
     */
    val isAnimated: Boolean

    @get:Nonnull
    val imageUrl: String?
        /**
         * A String representation of the URL which leads to image displayed within the official Discord client
         * when this emoji is used
         *
         * @return Discord CDN link to the emoji's image
         */
        get() = String.format(ICON_URL, id, if (isAnimated) "gif" else "png")

    @get:Nonnull
    val image: ImageProxy?
        /**
         * Returns an [ImageProxy] for this emoji's image.
         *
         * @return Never-null [ImageProxy] of this emoji's image
         *
         * @see .getImageUrl
         */
        get() = ImageProxy(imageUrl!!)

    @get:Nonnull
    override val asMention: String?
        /**
         * Usable representation of this emoji (used to display in the client just like mentions with a specific format)
         * <br></br>Emojis are used with the format `<:[getName()][.getName]:[getId()][.getId]>`
         *
         * @return A usable String representation for this emoji
         *
         * @see [Message Formatting](https://discord.com/developers/docs/resources/channel.message-formatting)
         */
        get() = (if (isAnimated) "<a:" else "<:") + getName() + ":" + id + ">"

    @get:Nonnull
    override val formatted: String
        get() = asMention!!

    override fun formatTo(formatter: Formatter, flags: Int, width: Int, precision: Int) {
        super<Emoji>.formatTo(formatter, flags, width, precision)
    }

    companion object {
        /** Template for [.getImageUrl]  */
        const val ICON_URL = "https://cdn.discordapp.com/emojis/%s.%s"
    }
}
