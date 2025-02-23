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

package net.dv8tion.jda.api.interactions.components.text_input;

import net.dv8tion.jda.api.interactions.modals.Modal;

import javax.annotation.Nonnull;

/**
 * The different styles a {@link TextInput TextInput} field can have.
 * <br>The different styles are:
 * <ul>
 *     <li>SHORT - Single line input</li>
 *     <li>PARAGRAPH - Multiline input</li>
 * </ul>
 *
 * @see    TextInput
 * @see    Modal
 */
public enum TextInputStyle
{
    UNKNOWN(-1),
    SHORT(1),
    PARAGRAPH(2);

    private final int key;

    TextInputStyle(int type)
    {
        this.key = type;
    }

    /**
     * Returns the raw integer key for this TextInputStyle
     *
     * <p>This returns -1 if it's of type {@link #UNKNOWN}.
     *
     * @return The raw int key
     */
    public int getRaw()
    {
        return key;
    }

    /**
     * Returns the TextInputStyle associated with the provided key.
     * <br>If an unknown key is provided, this returns {@link #UNKNOWN}.
     *
     * @param  key
     *         The key to convert
     *
     * @return The text input style or {@link #UNKNOWN}
     */
    @Nonnull
    public static TextInputStyle fromKey(int key)
    {
        for (TextInputStyle style : values())
        {
            if (style.key == key)
                return style;
        }
        return UNKNOWN;
    }
}
