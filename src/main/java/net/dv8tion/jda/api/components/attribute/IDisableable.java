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

package net.dv8tion.jda.api.components.attribute;

import net.dv8tion.jda.api.components.Component;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Component which supports being disabled or having its children be disabled.
 */
public interface IDisableable extends Component
{
    /**
     * Whether this component is disabled.
     *
     * <p>For layout components, this means all children are enabled.
     * <br>When this returns {@code false},
     * this does not mean {@link #isEnabled()} is {@code true}! (for example, when one component is enabled and the other is disabled)
     *
     * <p>You can use {@link #asDisabled()} or {@link #asEnabled()} to create enabled/disabled instances.
     *
     * @return {@code true}, if this component is disabled
     */
    boolean isDisabled();

    /**
     * Whether this component is enabled.
     *
     * <p>For layout components, this means all children are enabled.
     * <br>When this returns {@code false},
     * this does not mean {@link #isDisabled()} is {@code true}! (for example, when one component is enabled and the other is disabled)
     *
     * <p>You can use {@link #asDisabled()} or {@link #asEnabled()} to create enabled/disabled instances.
     *
     * @return {@code true} if this component is enabled
     */
    default boolean isEnabled()
    {
        return !isDisabled();
    }

    /**
     * Returns a new instance of this component in an enabled/disabled state.
     * <br>For layout components, this enables/disables all the components it contains.
     *
     * @return The new component in an enabled/disabled state
     *
     * @see net.dv8tion.jda.api.components.tree.ComponentTree#replace(net.dv8tion.jda.api.components.replacer.ComponentReplacer) ComponentTree.replace(ComponentReplacer)
     */
    @Nonnull
    @CheckReturnValue
    IDisableable withDisabled(boolean disabled);

    /**
     * Returns a new instance of this component in a disabled state.
     * <br>For layout components, this disables all the components it contains.
     *
     * @return The new component in a disabled state
     *
     * @see net.dv8tion.jda.api.components.tree.ComponentTree#replace(net.dv8tion.jda.api.components.replacer.ComponentReplacer) ComponentTree.replace(ComponentReplacer)
     */
    @Nonnull
    @CheckReturnValue
    default IDisableable asDisabled()
    {
        return withDisabled(true);
    }

    /**
     * Returns a new instance of this component in an enabled state.
     * <br>For layout components, this enables all the components it contains.
     *
     * @return The new component in an enabled state
     *
     * @see net.dv8tion.jda.api.components.tree.ComponentTree#replace(net.dv8tion.jda.api.components.replacer.ComponentReplacer) ComponentTree.replace(ComponentReplacer)
     */
    @Nonnull
    @CheckReturnValue
    default IDisableable asEnabled()
    {
        return withDisabled(false);
    }
}
