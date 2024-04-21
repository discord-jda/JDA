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
package net.dv8tion.jda.api.interactions.components.text

import javax.annotation.Nonnull

/**
 * The different styles a [TextInput] field can have.
 * <br></br>The different styles are:
 *
 *  * SHORT - Single line input
 *  * PARAGRAPH - Multiline input
 *
 *
 * @see TextInput
 *
 * @see Modal
 */
enum class TextInputStyle(
    /**
     * Returns the raw integer key for this TextInputStyle
     *
     *
     * This returns -1 if it's of type [.UNKNOWN].
     *
     * @return The raw int key
     */
    val raw: Int
) {
    UNKNOWN(-1),
    SHORT(1),
    PARAGRAPH(2);

    companion object {
        /**
         * Returns the TextInputStyle associated with the provided key.
         * <br></br>If an unknown key is provided, this returns [.UNKNOWN].
         *
         * @param  key
         * The key to convert
         *
         * @return The text input style or [.UNKNOWN]
         */
        @JvmStatic
        @Nonnull
        fun fromKey(key: Int): TextInputStyle {
            for (style in entries) {
                if (style.raw == key) return style
            }
            return UNKNOWN
        }
    }
}
