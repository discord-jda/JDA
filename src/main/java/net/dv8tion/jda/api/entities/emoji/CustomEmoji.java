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

import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.utils.ImageProxy;
import net.dv8tion.jda.api.utils.data.DataObject;

import javax.annotation.Nonnull;
import java.util.Formatter;

/**
 * Represents a minimal custom emoji.
 *
 * <p>This contains the most minimal representation of a custom emoji, via id and name.
 *
 * <p><b>This does not represent unicode emojis like they are used in the official client!
 * The format {@code :smiley:} is a client-side alias which is replaced by the unicode emoji, not a custom emoji.</b>
 *
 * @see Emoji#fromCustom(String, long, boolean)
 * @see Emoji#fromCustom(CustomEmoji)
 * @see Emoji#fromFormatted(String)
 * @see Emoji#fromData(DataObject)
 */
public interface CustomEmoji extends Emoji, IMentionable
{
    /** Template for {@link #getImageUrl()} */
    String ICON_URL = "https://cdn.discordapp.com/emojis/%s.%s";

    @Nonnull
    @Override
    default Type getType()
    {
        return Type.CUSTOM;
    }

    /**
     * Whether this emoji is animated.
     *
     * @return True, if this emoji is animated
     */
    boolean isAnimated();

    /**
     * A String representation of the URL which leads to image displayed within the official Discord&trade; client
     * when this emoji is used
     *
     * @return Discord CDN link to the emoji's image
     */
    @Nonnull
    default String getImageUrl()
    {
        return String.format(ICON_URL, getId(), isAnimated() ? "gif" : "png");
    }

    /**
     * Returns an {@link ImageProxy} for this emoji's image.
     *
     * @return Never-null {@link ImageProxy} of this emoji's image
     *
     * @see    #getImageUrl()
     */
    @Nonnull
    default ImageProxy getImage()
    {
        return new ImageProxy(getImageUrl());
    }

    /**
     * Usable representation of this emoji (used to display in the client just like mentions with a specific format)
     * <br>Emojis are used with the format <code>&lt;:{@link #getName getName()}:{@link #getId getId()}&gt;</code>
     *
     * @return A usable String representation for this emoji
     *
     * @see    <a href="https://discord.com/developers/docs/resources/channel#message-formatting">Message Formatting</a>
     */
    @Nonnull
    @Override
    default String getAsMention()
    {
        return (isAnimated() ? "<a:" : "<:") + getName() + ":" + getId() + ">";
    }

    @Nonnull
    @Override
    default String getFormatted()
    {
        return getAsMention();
    }

    @Override
    default void formatTo(Formatter formatter, int flags, int width, int precision)
    {
        Emoji.super.formatTo(formatter, flags, width, precision);
    }
}
