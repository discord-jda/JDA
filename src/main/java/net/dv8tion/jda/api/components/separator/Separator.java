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
import net.dv8tion.jda.api.utils.messages.MessageRequest;
import net.dv8tion.jda.internal.components.separator.SeparatorImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * A component to separate content vertically, you can change its size and make it invisible.
 *
 * <p><b>Requirements:</b> {@linkplain MessageRequest#useComponentsV2() Components V2} needs to be enabled!
 */
public interface Separator extends Component, MessageTopLevelComponent, ContainerChildComponent
{
    /**
     * Constructs a new, visible {@link Separator} from the given content.
     * <br>This is equivalent to {@code create(true, spacing)}.
     *
     * @param  spacing
     *         The spacing provided by the separator
     *
     * @throws IllegalArgumentException
     *         If {@code null} is provided
     *
     * @return The new {@link Separator}
     */
    @Nonnull
    static Separator createDivider(@Nonnull Spacing spacing)
    {
        return create(true, spacing);
    }

    /**
     * Constructs a new, invisible {@link Separator} from the given content.
     * <br>This is equivalent to {@code create(false, spacing)}.
     *
     * @param  spacing
     *         The spacing provided by the separator
     *
     * @throws IllegalArgumentException
     *         If {@code null} is provided
     *
     * @return The new {@link Separator}
     */
    @Nonnull
    static Separator createInvisible(@Nonnull Spacing spacing)
    {
        return create(false, spacing);
    }

    /**
     * Constructs a new {@link Separator} from the given content.
     *
     * @param  isDivider
     *         Whether the separator is visible
     * @param  spacing
     *         The spacing provided by the separator
     *
     * @throws IllegalArgumentException
     *         If {@code null} is provided
     *
     * @return The new {@link Separator}
     */
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

    /**
     * Creates a new {@link Separator} with the provided divider status.
     *
     * @param  divider
     *         The new divider status, {@code true} to make it visible, {@code false} is invisible
     *
     * @return The new {@link Separator}
     */
    @Nonnull
    @CheckReturnValue
    Separator withDivider(boolean divider);

    /**
     * Creates a new {@link Separator} with the provided spacing.
     *
     * @param  spacing
     *         The new spacing
     *
     * @return The new {@link Separator}
     */
    @Nonnull
    @CheckReturnValue
    Separator withSpacing(@Nonnull Spacing spacing);

    /**
     * Whether this separator is visible or not.
     *
     * @return {@code true} if this separate is a divider, and thus visible, {@code false} otherwise
     */
    boolean isDivider();

    /**
     * The spacing of this separator.
     *
     * @return The spacing
     */
    @Nonnull
    Spacing getSpacing();

    /**
     * Represents the amount of spacing a separator will create.
     */
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
         * Raw int representing this spacing
         *
         * <p>This returns -1 if it's of type {@link #UNKNOWN}.
         *
         * @return Raw int representing this Spacing
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
