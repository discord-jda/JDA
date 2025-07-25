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

package net.dv8tion.jda.api.components;

import net.dv8tion.jda.annotations.ForRemoval;
import net.dv8tion.jda.annotations.ReplaceWith;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.section.SectionAccessoryComponent;
import net.dv8tion.jda.api.components.section.SectionContentComponent;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalTopLevelComponent;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Component of a Message or Modal.
 * <br>These are used to extend messages with interactive elements such as buttons or select menus.
 * Components are also the primary building blocks for {@link Modal Modals}.
 *
 * <p>All components include a unique, positive, integer ID.
 * <br>These identifiers can be set manually or automatically assigned by Discord,
 * and are useful to identify <b>any</b> component.
 *
 * <br><p>Not every component can be used in {@link net.dv8tion.jda.api.entities.Message Messages} or {@link Modal Modals}.
 * Use {@link Type#isMessageCompatible()} and {@link Type#isModalCompatible()} to check whether a component can be used.
 *
 * @see MessageTopLevelComponent
 * @see ModalTopLevelComponent
 * @see ActionRowChildComponent
 * @see ContainerChildComponent
 * @see SectionContentComponent
 * @see SectionAccessoryComponent
 */
public interface Component
{
    /**
     * The type of component.
     *
     * @return {@link Type}
     */
    @Nonnull
    Type getType();

    /**
     * The unique, numeric identifier of this component.
     * <br>Can be set manually or automatically assigned by Discord (starting from {@code 1}).
     * If it has not been assigned yet, this will return {@code -1}.
     *
     * <p><b>Note:</b> Older messages may return {@code 0}.
     *
     * @return The unique identifier of this component, or {@code -1} if not assigned yet, or {@code 0} on older messages
     */
    int getUniqueId();

    /**
     * Creates a new component with the provided numeric ID.
     * <br>If no ID is set, Discord will generate IDs incrementally starting from 1
     * and will not use existing IDs from the same message/modal.
     *
     * @param  uniqueId
     *         The new ID; must be higher or equal to 1
     *
     * @throws IllegalArgumentException
     *         If the ID is less than 1
     *
     * @return The new component
     */
    @Nonnull
    @CheckReturnValue
    Component withUniqueId(int uniqueId);

    /**
     * Whether this Component is compatible with {@link net.dv8tion.jda.api.entities.Message Messages}.
     * <br>If the component in question contains other components, this also checks every component inside it.
     *
     * @return True, if this Component (and its children) is compatible with messages.
     */
    default boolean isMessageCompatible()
    {
        return getType().isMessageCompatible();
    }

    /**
     * Whether this Component is compatible with {@link Modal Modals}.
     * <br>If the component in question contains other components, this also checks every component inside it.
     *
     * @return True, if this Component (and its children) is compatible with modals.
     */
    default boolean isModalCompatible()
    {
        return getType().isModalCompatible();
    }

    /**
     * The component types
     */
    enum Type
    {
        UNKNOWN(-1, false, false),
        /** A row of components */
        ACTION_ROW(1, true, true),
        /** A button */
        BUTTON(2, true, false),
        /** A select menu of strings */
        STRING_SELECT(3, true, false),
        /** A text input field */
        TEXT_INPUT(4, false, true),
        /** A select menu of users */
        USER_SELECT(5, true, false),
        /** A select menu of roles */
        ROLE_SELECT(6, true, false),
        /** A select menu of users and roles */
        MENTIONABLE_SELECT(7, true, false),
        /** A select menu of channels */
        CHANNEL_SELECT(8, true, false),
        SECTION(9, true, false),
        TEXT_DISPLAY(10, true, false),
        THUMBNAIL(11, true, false),
        MEDIA_GALLERY(12, true, false),
        FILE_DISPLAY(13, true, false),
        SEPARATOR(14, true, false),
        CONTAINER(17, true, false),
        ;

        private final int key;
        private final boolean messageCompatible;
        private final boolean modalCompatible;

        Type(int key, boolean messageCompatible, boolean modalCompatible)
        {
            this.key = key;
            this.messageCompatible = messageCompatible;
            this.modalCompatible = modalCompatible;
        }

        /**
         * How many components of this type can be added to one {@link ActionRow}.
         *
         * @return The maximum amount an action row can contain
         *
         * @deprecated Replaced with {@link ActionRow#getMaxAllowed(Component.Type)}
         */
        @Deprecated
        @ForRemoval
        @ReplaceWith("ActionRow.getMaxAllowed(Component.Type)")
        public int getMaxPerRow()
        {
            return ActionRow.getMaxAllowed(this);
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
