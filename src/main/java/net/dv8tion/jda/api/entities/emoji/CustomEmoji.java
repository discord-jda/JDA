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

import javax.annotation.Nonnull;

public interface CustomEmoji extends Emoji, IMentionable
{
    /** Template for {@link #getImageUrl()} */
    String ICON_URL = "https://cdn.discordapp.com/emojis/%s.%s";

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
}
