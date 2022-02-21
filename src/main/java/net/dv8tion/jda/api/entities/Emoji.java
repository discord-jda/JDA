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

import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EncodingUtil;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.regex.Matcher;

/**
 * Represents a Discord Emoji.
 * <br>This can either be {@link #isUnicode() unicode} or {@link #isCustom() custom}.
 *
 * <p>If this emoji is unicode, then {@link #getIdLong()} will be {@code 0} and {@link #getTimeCreated()} will be constant at 2015.
 *
 * @see #getName()
 * @see #getAsMention()
 */
public class Emoji implements SerializableData, IMentionable
{
    private final String name;
    private final long id;
    private final boolean animated;

    private Emoji(String name, long id, boolean animated)
    {
        this.name = name;
        this.id = id;
        this.animated = animated;
    }

    /**
     * The name of this emoji.
     * <br>This will be the unicode characters if this emoji is not {@link #isCustom() custom}.
     *
     * @return The unicode or custom name
     */
    @Nonnull
    public String getName()
    {
        return name;
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    /**
     * Whether this emote is animated.
     *
     * @return True, if this emote is animated
     */
    public boolean isAnimated()
    {
        return animated;
    }

    /**
     * Whether this emoji is a standard unicode emoji.
     * <br>This means {@link #getName()} returns the unicode characters of this emoji
     * and {@link #getId()} returns 0.
     *
     * @return True, if this emoji is standard unicode
     */
    public boolean isUnicode()
    {
        return id == 0L;
    }

    /**
     * Whether this is a custom emote from a Guild.
     *
     * @return True, if this is a custom emote
     */
    public boolean isCustom()
    {
        return !isUnicode();
    }

    /**
     * Creates an emoji with the provided unicode.
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
    public static Emoji fromUnicode(@Nonnull String code)
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
        return new Emoji(code, 0, false);
    }

    /**
     * Creates an emoji with the provided name.
     *
     * @param  name
     *         The emote name
     * @param  id
     *         The emote id
     * @param  animated
     *         Whether this emote is animated
     *
     * @throws IllegalArgumentException
     *         If the name is null or empty
     *
     * @return The new emoji instance
     */
    @Nonnull
    public static Emoji fromEmote(@Nonnull String name, long id, boolean animated)
    {
        Checks.notEmpty(name, "Name");
        return new Emoji(name, id, animated);
    }

    /**
     * Creates an emoji from the provided {@link Emote}
     *
     * @param  emote
     *         The emote instance
     *
     * @throws IllegalArgumentException
     *         If the emote is null
     *
     * @return The new emoji instance
     */
    @Nonnull
    public static Emoji fromEmote(@Nonnull Emote emote)
    {
        Checks.notNull(emote, "Emote");
        return fromEmote(emote.getName(), emote.getIdLong(), emote.isAnimated());
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
    public static Emoji fromMarkdown(@Nonnull String code)
    {
        Matcher matcher = Message.MentionType.EMOTE.getPattern().matcher(code);
        if (matcher.matches())
            return fromEmote(matcher.group(1), Long.parseUnsignedLong(matcher.group(2)), code.startsWith("<a"));
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
    public static Emoji fromData(@Nonnull DataObject emoji)
    {
        return new Emoji(emoji.getString("name"),
                emoji.getUnsignedLong("id", 0),
                emoji.getBoolean("animated"));
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        DataObject json = DataObject.empty().put("name", name);
        if (id != 0)
        {
            json.put("id", id)
                    .put("animated", animated);
        }
        return json;
    }

    @Nonnull
    @Override
    public String getAsMention()
    {
        return id == 0L ? name : String.format("<%s:%s:%s>", animated ? "a" : "", name, Long.toUnsignedString(id));
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, id, animated);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this) return true;
        if (!(obj instanceof Emoji)) return false;
        Emoji other = (Emoji) obj;
        return other.id == id && other.animated == animated && Objects.equals(other.name, name);
    }

    @Override
    public String toString()
    {
        return "E:" + name + "(" + id + ")";
    }
}
