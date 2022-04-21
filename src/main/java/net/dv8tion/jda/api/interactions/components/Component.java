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
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.utils.data.SerializableData;

import javax.annotation.Nonnull;

/**
 * Component of a Message or Modal.
 * <br>These are used to extend messages with interactive elements such as buttons or select menus.
 * Components are also the primary building blocks for {@link Modal Modals}.
 *
 * <br><p>Not every component can be used in {@link net.dv8tion.jda.api.entities.Message Messages} or {@link Modal Modals}.
 * Use {@link Type#isMessageCompatible()} and {@link Type#isModalCompatible()} to check whether a component can be used.
 *
 *
 * @see ActionRow
 *
 * @see Button
 * @see SelectMenu
 * @see TextInput
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
     * Whether this Component is compatible with {@link net.dv8tion.jda.api.entities.Message Messages}.
     * <br>If the component in question is a {@link LayoutComponent}, this also checks every component inside it.
     *
     * @return True, if this Component is compatible with messages.
     */
    boolean isMessageCompatible();

    /**
     * Whether this Component is compatible with {@link Modal Modals}.
     * <br>If the component in question is a {@link LayoutComponent}, this also checks every component inside it.
     *
     * @return True, if this Component is compatible with modals.
     */
    boolean isModalCompatible();

    /**
     * The component types
     */
    enum Type
    {
        UNKNOWN(-1, 0, false, false),
        /** A row of components */
        ACTION_ROW(1, 0, true, true),
        /** A button */
        BUTTON(2, 5, true, false),
        /** A select menu */
        SELECT_MENU(3, 1, true, false),
        /** A text input field */
        TEXT_INPUT(4, 1, false, true)
        ;

        private final int key;
        private final int maxPerRow;
        private final boolean messageCompatible;
        private final boolean modalCompatible;

        Type(int key, int maxPerRow, boolean messageCompatible, boolean modalCompatible)
        {
            this.key = key;
            this.maxPerRow = maxPerRow;
            this.messageCompatible = messageCompatible;
            this.modalCompatible = modalCompatible;
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
         * Raw int representing this ComponentType
         *
         * <p>This returns -1 if it's of type {@link #UNKNOWN}.
         *
         * @return Raw int representing this ComponentType
         */
        public int getKey()
        {
            return key;
        }

        /**
         * Whether this component can be used in {@link net.dv8tion.jda.api.entities.Message Messages}.
         *
         * @return Whether this component can be used in Messages.
         */
        public boolean isMessageCompatible()
        {
            return messageCompatible;
        }

        /**
         * Whether this component can be used in {@link Modal Modals}.
         *
         * @return Whether this component can be used in Modals.
         */
        public boolean isModalCompatible()
        {
            return modalCompatible;
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
