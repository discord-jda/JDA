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

import net.dv8tion.jda.api.utils.data.SerializableData;

import javax.annotation.Nonnull;
import java.util.List;

public interface ComponentLayout extends SerializableData, Iterable<Component>
{
    /**
     * List representation of this action row.
     * <br>This list is modifiable. Note that empty action rows are not supported.
     *
     * @return {@link List} of components in this action row
     */
    @Nonnull
    List<Component> getComponents();

    /**
     * List of buttons in this action row.
     *
     * @return Immutable {@link List} of {@link Button Buttons}
     */
    @Nonnull
    List<Button> getButtons();

    /**
     * The {@link Type} of layout
     *
     * @return The layout {@link Type} or {@link Type#UNKNOWN}
     */
    @Nonnull
    Type getType();

    /**
     * The layout types
     */
    enum Type
    {
        UNKNOWN(-1),
        ACTION_ROW(1)
        ;
        private final int key;

        Type(int key)
        {
            this.key = key;
        }

        /**
         * The raw type value
         *
         * @return The raw type value
         */
        public int getKey()
        {
            return key;
        }
    }
}
