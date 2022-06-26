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

import net.dv8tion.jda.api.utils.data.DataObject;

import javax.annotation.Nonnull;

/**
 * Represents a standard unicode emoji such as 😃 (client alias: {@code :smiley:}).
 * 
 * <p>This type only encapsulates the unicode character and is unaware of the human-readable aliases used by the discord client.
 * 
 * @see Emoji#fromUnicode(String) 
 * @see Emoji#fromFormatted(String) 
 * @see Emoji#fromData(DataObject) 
 */
public interface UnicodeEmoji extends Emoji
{
    /**
     * Converts the unicode name into codepoint notation like {@code U+1F602}.
     *
     * @return String containing the codepoint representation of the emoji
     */
    @Nonnull
    String getAsCodepoints();

    @Nonnull
    @Override
    default Type getType()
    {
        return Type.UNICODE;
    }

    @Nonnull
    @Override
    default String getFormatted()
    {
        return getName();
    }
}
