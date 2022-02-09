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

package net.dv8tion.jda.api.interactions.components.text;

import org.jetbrains.annotations.NotNull;

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

    public int getKey()
    {
        return key;
    }

    /**
     * Returns the style associated with the provided key
     *
     * @param  key
     *         The key to convert
     *
     * @return The text input style or {@link #UNKNOWN}
     */
    @NotNull
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
