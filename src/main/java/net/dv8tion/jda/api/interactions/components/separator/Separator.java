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

package net.dv8tion.jda.api.interactions.components.separator;

import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.interactions.components.container.ContainerChildComponent;
import net.dv8tion.jda.internal.interactions.components.separator.SeparatorImpl;

import javax.annotation.Nonnull;

public interface Separator extends Component, MessageTopLevelComponent, ContainerChildComponent
{
    static Separator create(boolean isDivider, Spacing spacing)
    {
        return new SeparatorImpl(spacing, isDivider);
    }

    Spacing getSpacing();

    boolean hasDivider();

    enum Spacing {
        UNKNOWN(-1),
        SMALL(1),
        LARGE(2);

        private final int key;

        Spacing(int key)
        {
            this.key = key;
        }

        /**
         * Raw int representing this SeparatorSpacing
         *
         * <p>This returns -1 if it's of type {@link #UNKNOWN}.
         *
         * @return Raw int representing this SeparatorSpacing
         */
        public int getKey()
        {
            return key;
        }

        /**
         * Maps the provided spacing type id to the respective enum instance.
         *
         * @param  type
         *         The raw spacing type id
         *
         * @return The Type or {@link #UNKNOWN}
         */
        @Nonnull
        public static Spacing fromKey(int type)
        {
            for (Spacing t : values())
            {
                if (t.key == type)
                    return t;
            }
            return UNKNOWN;
        }
    }
}
