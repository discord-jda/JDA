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

package net.dv8tion.jda.api.interactions.components;

import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.utils.data.SerializableData;

import javax.annotation.Nonnull;

/**
 * Component of a Message.
 * <br>These are used to extend messages with interactive elements such as buttons or select menus.
 *
 * @see ActionRow
 *
 * @see Button
 * @see SelectMenu
 */
public interface Component extends SerializableData
{
    /**
     * The type of component.
     *
     * @return {@link Type}
     */
    @Nonnull
    Type getType();

    /**
     * The component types
     */
    enum Type
    {
        UNKNOWN(-1, 0),
        /** A row of components */
        ACTION_ROW(1, 0),
        /** A button */
        BUTTON(2, 5),
        /** A select menu */
        SELECT_MENU(3, 1),
        ;

        private final int key;
        private final int maxPerRow;

        Type(int key, int maxPerRow)
        {
            this.key = key;
            this.maxPerRow = maxPerRow;
        }

        /**
         * How many of these components can be added to one {@link ActionRow}.
         *
         * @return The maximum amount an action row can contain
         */
        public int getMaxPerRow()
        {
            return maxPerRow;
        }

        /**
         * Maps the provided type id to the respective enum instance.
         *
         * @param  type
         *         The raw type id
         *
         * @return The Type or {@link #UNKNOWN}
         */
        @Nonnull
        public static Type fromKey(int type)
        {
            for (Type t : values())
            {
                if (t.key == type)
                    return t;
            }
            return UNKNOWN;
        }
    }
}
