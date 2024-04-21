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
package net.dv8tion.jda.api.interactions.components

import net.dv8tion.jda.api.interactions.components.Component.Type
import net.dv8tion.jda.api.utils.data.SerializableData
import javax.annotation.Nonnull

/**
 * Component of a Message or Modal.
 * <br></br>These are used to extend messages with interactive elements such as buttons or select menus.
 * Components are also the primary building blocks for [Modals][Modal].
 *
 * <br></br>
 *
 *Not every component can be used in [Messages][net.dv8tion.jda.api.entities.Message] or [Modals][Modal].
 * Use [Type.isMessageCompatible] and [Type.isModalCompatible] to check whether a component can be used.
 *
 * @see ActionRow
 *
 *
 * @see Button
 *
 * @see SelectMenu
 *
 * @see TextInput
 */
interface Component : SerializableData {
    @JvmField
    @get:Nonnull
    val type: Type?

    /**
     * Whether this Component is compatible with [Messages][net.dv8tion.jda.api.entities.Message].
     * <br></br>If the component in question is a [LayoutComponent], this also checks every component inside it.
     *
     * @return True, if this Component is compatible with messages.
     */
    val isMessageCompatible: Boolean

    /**
     * Whether this Component is compatible with [Modals][Modal].
     * <br></br>If the component in question is a [LayoutComponent], this also checks every component inside it.
     *
     * @return True, if this Component is compatible with modals.
     */
    val isModalCompatible: Boolean

    /**
     * The component types
     */
    enum class Type(
        /**
         * Raw int representing this ComponentType
         *
         *
         * This returns -1 if it's of type [.UNKNOWN].
         *
         * @return Raw int representing this ComponentType
         */
        @JvmField val key: Int,
        /**
         * How many of these components can be added to one [ActionRow].
         *
         * @return The maximum amount an action row can contain
         */
        val maxPerRow: Int,
        /**
         * Whether this component can be used in [Messages][net.dv8tion.jda.api.entities.Message].
         *
         * @return Whether this component can be used in Messages.
         */
        val isMessageCompatible: Boolean,
        /**
         * Whether this component can be used in [Modals][Modal].
         *
         * @return Whether this component can be used in Modals.
         */
        val isModalCompatible: Boolean
    ) {
        UNKNOWN(-1, 0, false, false),

        /** A row of components  */
        ACTION_ROW(1, 0, true, true),

        /** A button  */
        BUTTON(2, 5, true, false),

        /** A select menu of strings  */
        STRING_SELECT(3, 1, true, false),

        /** A text input field  */
        TEXT_INPUT(4, 1, false, true),

        /** A select menu of users  */
        USER_SELECT(5, 1, true, false),

        /** A select menu of roles  */
        ROLE_SELECT(6, 1, true, false),

        /** A select menu of users and roles  */
        MENTIONABLE_SELECT(7, 1, true, false),

        /** A select menu of channels  */
        CHANNEL_SELECT(8, 1, true, false);

        companion object {
            /**
             * Maps the provided type id to the respective enum instance.
             *
             * @param  type
             * The raw type id
             *
             * @return The Type or [.UNKNOWN]
             */
            @JvmStatic
            @Nonnull
            fun fromKey(type: Int): Type {
                for (t in entries) {
                    if (t.key == type) return t
                }
                return UNKNOWN
            }
        }
    }
}
