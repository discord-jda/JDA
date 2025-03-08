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

package net.dv8tion.jda.api.components.separator;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.internal.components.separator.SeparatorImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

// TODO-components-v2 docs
public interface Separator extends Component, MessageTopLevelComponent, ContainerChildComponent
{
    // TODO-components-v2 docs
    @Nonnull
    static Separator createDivider(@Nonnull Spacing spacing)
    {
        return create(true, spacing);
    }

    // TODO-components-v2 docs
    @Nonnull
    static Separator create(boolean isDivider, @Nonnull Spacing spacing)
    {
        Checks.notNull(spacing, "Spacing");
        return new SeparatorImpl(spacing, isDivider);
    }

    @Nonnull
    @Override
    @CheckReturnValue
    Separator withUniqueId(int uniqueId);

    // TODO-components-v2 docs
    @Nonnull
    @CheckReturnValue
    Separator withDivider(boolean divider);

    // TODO-components-v2 docs
    @Nonnull
    @CheckReturnValue
    Separator withSpacing(@Nonnull Spacing spacing);

    // TODO-components-v2 docs
    boolean isDivider();

    // TODO-components-v2 docs
    @Nonnull
    Spacing getSpacing();

    // TODO-components-v2 docs
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
