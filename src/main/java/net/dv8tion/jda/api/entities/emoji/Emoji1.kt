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

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.emoji.Emoji.Type
import net.dv8tion.jda.api.utils.MiscUtil
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.api.utils.data.SerializableData
import net.dv8tion.jda.internal.entities.emoji.CustomEmojiImpl
import net.dv8tion.jda.internal.entities.emoji.UnicodeEmojiImpl
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.EncodingUtil
import java.util.*
import javax.annotation.Nonnull

/**
 * Represents a Discord Emoji.
 * <br></br>This can either be [Type.UNICODE] or [Type.CUSTOM].
 *
 *
 * Implements [Formattable] with [.getFormatted].
 *
 * @see .getName
 */
interface Emoji : SerializableData, Formattable {
    @JvmField
    @get:Nonnull
    val type: Type?

    @JvmField
    @get:Nonnull
    val name: String?

    @JvmField
    @get:Nonnull
    val asReactionCode: String?

    @get:Nonnull
    val formatted: String?
    override fun formatTo(formatter: Formatter, flags: Int, width: Int, precision: Int) {
        val leftJustified = flags and FormattableFlags.LEFT_JUSTIFY == FormattableFlags.LEFT_JUSTIFY
        val out = formatted
        MiscUtil.appendTo(formatter, width, precision, leftJustified, out)
    }

    /**
     * Possible emoji types.
     */
    enum class Type {
        /**
         * Standard Unicode Emoji.
         * <br></br>This represents emoji such as `:smiley:`. These do not have an ID.
         */
        UNICODE,

        /**
         * Custom Guild Emoji.
         * <br></br>This represents emojis which were created by users and added to a guild.
         */
        CUSTOM
    }

    companion object {
        /**
         * Creates a reference for a unicode emoji with the provided unicode.
         * <br></br>This has to be the unicode characters rather than the emoji name.
         * <br></br>A reference of unicode emojis can be found here:
         * [Emoji Table](https://unicode.org/emoji/charts/full-emoji-list.html).
         *
         *
         * **Examples**<br></br>
         * <pre>`// unicode emoji, escape codes
         * fromUnicode("&#92;uD83D&#92;uDE03");
         * // codepoint notation
         * fromUnicode("U+1F602");
         * // unicode emoji
         * fromUnicode("😃");
        `</pre> *
         *
         * @param  code
         * The unicode characters, or codepoint notation such as `"U+1F602"`
         *
         * @throws IllegalArgumentException
         * If the code is null or empty
         *
         * @return The new emoji instance
         */
        @JvmStatic
        @Nonnull
        fun fromUnicode(@Nonnull code: String): UnicodeEmoji? {
            var code = code
            Checks.notEmpty(code, "Unicode")
            if (code.startsWith("U+") || code.startsWith("u+")) {
                val emoji = StringBuilder()
                val codepoints =
                    code.trim { it <= ' ' }.split("\\s*[uU]\\+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                for (codepoint in codepoints) emoji.append(
                    if (codepoint.isEmpty()) "" else EncodingUtil.decodeCodepoint(
                        "U+$codepoint"
                    )
                )
                code = emoji.toString()
            }
            return UnicodeEmojiImpl(code)
        }

        /**
         * Creates a reference for a custom emoji with the provided name.
         *
         * @param  name
         * The emoji name
         * @param  id
         * The emoji id
         * @param  animated
         * Whether this emoji is animated
         *
         * @throws IllegalArgumentException
         * If the name is null or empty
         *
         * @return The new emoji instance
         */
        @JvmStatic
        @Nonnull
        fun fromCustom(@Nonnull name: String?, id: Long, animated: Boolean): CustomEmoji? {
            Checks.notEmpty(name, "Name")
            return CustomEmojiImpl(name, id, animated)
        }

        /**
         * Creates a reference for a custom emoji from the provided [CustomEmoji]
         *
         * @param  emoji
         * The emoji instance
         *
         * @throws IllegalArgumentException
         * If the emoji is null
         *
         * @return The new emoji instance
         */
        @Nonnull
        fun fromCustom(@Nonnull emoji: CustomEmoji): CustomEmoji? {
            Checks.notNull(emoji, "Emoji")
            return fromCustom(emoji.getName(), emoji.idLong, emoji.isAnimated())
        }
        // either <a?:name:id> or just unicode
        /**
         * Parses the provided markdown formatting, or unicode characters, to an Emoji instance.
         *
         *
         * **Example**<br></br>
         * <pre>`// animated custom emoji
         * fromFormatted("<a:dance:123456789123456789>");
         * // not animated custom emoji
         * fromFormatted("<:dog:123456789123456789>");
         * // unicode emoji, escape codes
         * fromFormatted("&#92;uD83D&#92;uDE03");
         * // codepoint notation
         * fromFormatted("U+1F602");
         * // unicode emoji
         * fromFormatted("😃");
        `</pre> *
         *
         * @param  code
         * The code to parse
         *
         * @throws IllegalArgumentException
         * If the provided code is null or empty
         *
         * @return The emoji instance
         */
        @Nonnull
        fun fromFormatted(@Nonnull code: String): EmojiUnion? {
            Checks.notEmpty(code, "Formatting Code")
            val matcher = Message.MentionType.EMOJI.pattern.matcher(code)
            return if (matcher.matches()) fromCustom(
                matcher.group(1),
                java.lang.Long.parseUnsignedLong(matcher.group(2)),
                code.startsWith("<a")
            ) as EmojiUnion? else fromUnicode(code) as EmojiUnion?
        }

        /**
         * Parses the provided JSON representation to an emoji instance.
         *
         * @param  emoji
         * The emoji json
         * @throws net.dv8tion.jda.api.exceptions.ParsingException
         * If the JSON is not a valid emoji
         *
         * @return The emoji instance
         */
        @Nonnull
        fun fromData(@Nonnull emoji: DataObject): EmojiUnion? {
            Checks.notNull(emoji, "Emoji Data")
            return if (emoji.isNull("id")) fromUnicode(emoji.getString("name")) as EmojiUnion? else fromCustom(
                emoji.getString("name"),
                emoji.getUnsignedLong("id"),
                emoji.getBoolean("animated")
            ) as EmojiUnion?
        }
    }
}
