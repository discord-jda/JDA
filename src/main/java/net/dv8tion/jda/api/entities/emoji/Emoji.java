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
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.entities.emoji.EmojiImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EncodingUtil;

import javax.annotation.Nonnull;
import java.util.regex.Matcher;

/**
 * Represents a Discord Emoji.
 * <br>This can either be {@link Type#UNICODE} or {@link Type#CUSTOM}.
 *
 * @see #getName()
 */
public interface Emoji extends SerializableData
{
    /**
     * Creates a reference for a unicode emoji with the provided unicode.
     * <br>This has to be the unicode characters rather than the emoji name.
     *
     * @param  code
     *         The unicode characters, or codepoint notation such as {@code "U+1f649"}
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
        return new EmojiImpl(code, 0, false);
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
        return new EmojiImpl(name, id, animated);
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
     * Parses the provided markdown formatting to an Emoji instance.
     * <h4>Example</h4>
     * <pre>{@code
     * // animated custom emoji
     * parseMarkdown("<a:dance:123456789123456789>");
     * // not animated custom emoji
     * parseMarkdown("<:dog:123456789123456789>");
     * // unicode emoji, escape codes
     * parseMarkdown("\uD83D\uDE03");
     * // unicode emoji
     * parseMarkdown("😃");
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
    static Emoji fromMarkdown(@Nonnull String code)
    {
        Matcher matcher = Message.MentionType.EMOJI.getPattern().matcher(code);
        if (matcher.matches())
            return fromCustom(matcher.group(1), Long.parseUnsignedLong(matcher.group(2)), code.startsWith("<a"));
        else
            return fromUnicode(code);
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
    static Emoji fromData(@Nonnull DataObject emoji)
    {
        return new EmojiImpl(emoji.getString("name"),
                emoji.getUnsignedLong("id", 0),
                emoji.getBoolean("animated"));
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
         * Custom Guild Emoji.
         * <br>This represents emojis which were created by users and added to a guild.
         */
        CUSTOM,
    }

}
