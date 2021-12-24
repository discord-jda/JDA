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

package net.dv8tion.jda.api.interactions.components.buttons;

import javax.annotation.Nonnull;

/**
 * The available styles used for {@link Button Buttons}.
 * <br>A button can have different styles to indicate its purpose.
 *
 * <p>To see what each button looks like here is an example cheatsheet:
 * <br>
 * <img alt="ButtonExample" src="https://raw.githubusercontent.com/DV8FromTheWorld/JDA/52377f69d1f3bfba909c51a449ac6b258f606956/assets/wiki/interactions/ButtonExamples.png">
 */
public enum ButtonStyle
{
    /** Placeholder for future styles */
    UNKNOWN(-1),
    /** Primary button style, usually in blue. Often used as the accept, submit, or acknowledge button. For confirm buttons (destructive action), use {@link  #DANGER}. */
    PRIMARY(1),
    /** Secondary button style, usually in gray. Often used as the cancel or lesser used option.  */
    SECONDARY(2),
    /** Success/Approve button style, usually in green. This should be used to indicate a positive action.  */
    SUCCESS(3),
    /** Danger/Deny button style, usually in red. This button should be used to indicate destructive actions.  */
    DANGER(4),
    /** Link button style, usually in gray and has a link attached  */
    LINK(5),
    ;

    private final int key;

    ButtonStyle(int key)
    {
        this.key = key;
    }

    /**
     * The raw style integer key
     *
     * @return The raw style key
     */
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
     * @return The button style or {@link #UNKNOWN}
     */
    @Nonnull
    public static ButtonStyle fromKey(int key)
    {
        for (ButtonStyle style : values())
        {
            if (style.key == key)
                return style;
        }
        return UNKNOWN;
    }
}
