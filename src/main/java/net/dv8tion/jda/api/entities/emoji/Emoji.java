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

package net.dv8tion.jda.api.entities.emoji;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.entities.emoji.CustomEmojiImpl;
import net.dv8tion.jda.internal.entities.emoji.UnicodeEmojiImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EncodingUtil;

import javax.annotation.Nonnull;
import java.util.Formattable;
import java.util.FormattableFlags;
import java.util.Formatter;
import java.util.regex.Matcher;

/**
 * Represents a Discord Emoji.
 * <br>This can either be {@link Type#UNICODE} or {@link Type#CUSTOM}.
 *
 * <p>Implements {@link Formattable} with {@link #getFormatted()}.
 *
 * @see #getName()
 */
public interface Emoji extends SerializableData, Formattable
{
    /**
     * Creates a reference for a unicode emoji with the provided unicode.
     * <br>This has to be the unicode characters rather than the emoji name.
     * <br>A reference of unicode emojis can be found here:
     * <a href="https://unicode.org/emoji/charts/full-emoji-list.html" target="_blank">Emoji Table</a>.
     *
     * <p><b>Examples</b><br>
     * <pre>{@code
     * // unicode emoji, escape codes
     * fromUnicode("&#92;uD83D&#92;uDE03");
     * // codepoint notation
     * fromUnicode("U+1F602");
     * // unicode emoji
     * fromUnicode("😃");
     * }</pre>
     *
     * @param  code
     *         The unicode characters, or codepoint notation such as {@code "U+1F602"}
     *
     * @throws IllegalArgumentException
     *         If the code is null or empty
     *
     * @return The new emoji instance
     */
    @Nonnull
    static UnicodeEmoji fromUnicode(@Nonnull String code)
    {
        Checks.notEmpty(code, "Unicode");
        if (code.startsWith("U+") || code.startsWith("u+"))
        {
            StringBuilder emoji = new StringBuilder();
            String[] codepoints = code.trim().split("\\s*[uU]\\+");
            for (String codepoint : codepoints)
                emoji.append(codepoint.isEmpty() ? "" : EncodingUtil.decodeCodepoint("U+" + codepoint));
            code = emoji.toString();
        }
        return new UnicodeEmojiImpl(code);
    }

    /**
     * Creates a reference for a custom emoji with the provided name.
     *
     * @param  name
     *         The emoji name
     * @param  id
     *         The emoji id
     * @param  animated
     *         Whether this emoji is animated
     *
     * @throws IllegalArgumentException
     *         If the name is null or empty
     *
     * @return The new emoji instance
     */
    @Nonnull
    static CustomEmoji fromCustom(@Nonnull String name, long id, boolean animated)
    {
        Checks.notEmpty(name, "Name");
        return new CustomEmojiImpl(name, id, animated);
    }

    /**
     * Creates a reference for a custom emoji from the provided {@link CustomEmoji}
     *
     * @param  emoji
     *         The emoji instance
     *
     * @throws IllegalArgumentException
     *         If the emoji is null
     *
     * @return The new emoji instance
     */
    @Nonnull
    static CustomEmoji fromCustom(@Nonnull CustomEmoji emoji)
    {
        Checks.notNull(emoji, "Emoji");
        return fromCustom(emoji.getName(), emoji.getIdLong(), emoji.isAnimated());
    }

    // either <a?:name:id> or just unicode

    /**
     * Parses the provided markdown formatting, or unicode characters, to an Emoji instance.
     *
     * <p><b>Example</b><br>
     * <pre>{@code
     * // animated custom emoji
     * fromFormatted("<a:dance:123456789123456789>");
     * // not animated custom emoji
     * fromFormatted("<:dog:123456789123456789>");
     * // unicode emoji, escape codes
     * fromFormatted("&#92;uD83D&#92;uDE03");
     * // codepoint notation
     * fromFormatted("U+1F602");
     * // unicode emoji
     * fromFormatted("😃");
     * }</pre>
     *
     * @param  code
     *         The code to parse
     *
     * @throws IllegalArgumentException
     *         If the provided code is null or empty
     *
     * @return The emoji instance
     */
    @Nonnull
    static EmojiUnion fromFormatted(@Nonnull String code)
    {
        Checks.notEmpty(code, "Formatting Code");
        Matcher matcher = Message.MentionType.EMOJI.getPattern().matcher(code);
        if (matcher.matches())
            return (EmojiUnion) fromCustom(matcher.group(1), Long.parseUnsignedLong(matcher.group(2)), code.startsWith("<a"));
        else
            return (EmojiUnion) fromUnicode(code);
    }

    /**
     * Parses the provided JSON representation to an emoji instance.
     *
     * @param  emoji
     *         The emoji json
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the JSON is not a valid emoji
     *
     * @return The emoji instance
     */
    @Nonnull
    static EmojiUnion fromData(@Nonnull DataObject emoji)
    {
        Checks.notNull(emoji, "Emoji Data");
        if (emoji.isNull("id"))
            return (EmojiUnion) fromUnicode(emoji.getString("name"));
        else
            return (EmojiUnion) fromCustom(emoji.getString("name"), emoji.getUnsignedLong("id"), emoji.getBoolean("animated"));
    }

    /**
     * The {@link Type} of this emoji.
     *
     * @return The {@link Type}
     */
    @Nonnull
    Type getType();

    /**
     * The name of this emoji.
     * <br>This will be the unicode characters if this emoji is not of {@link #getType()} {@link Type#CUSTOM CUSTOM}.
     *
     * @return The unicode or custom name
     */
    @Nonnull
    String getName();

    /**
     * The reaction code for this emoji.
     * <br>For unicode emojis this will be the unicode of said emoji rather than an alias like {@code :smiley:}.
     * <br>For custom emojis this will be the name and id of said emoji in the format {@code <name>:<id>}.
     *
     * @return The unicode if it is an emoji, or the name and id in the format {@code <name>:<id>}
     */
    @Nonnull
    String getAsReactionCode();

    /**
     * Formatted string used in messages.
     * <br>For unicode emoji, this is simply {@link #getName()}. For custom emoji, this will be the mention markdown format {@code <:name:id>}.
     *
     * @return The formatted message string
     */
    @Nonnull
    String getFormatted();

    @Override
    default void formatTo(Formatter formatter, int flags, int width, int precision)
    {
        boolean leftJustified = (flags & FormattableFlags.LEFT_JUSTIFY) == FormattableFlags.LEFT_JUSTIFY;
        String out = getFormatted();
        MiscUtil.appendTo(formatter, width, precision, leftJustified, out);
    }

    /**
     * Possible emoji types.
     */
    enum Type
    {
        /**
         * Standard Unicode Emoji.
         * <br>This represents emoji such as {@code :smiley:}. These do not have an ID.
         */
        UNICODE,
        /**
         * Custom Guild Emoji or Custom Application Emoji.
         * <br>This represents emojis which were created by users and added to a guild.
         * <br>This can also represent emojis which were created by users and added to a jda.
         */
        CUSTOM
    }
}
