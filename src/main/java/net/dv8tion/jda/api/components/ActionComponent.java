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
import net.dv8tion.jda.api.components.attribute.IDisableable;
import net.dv8tion.jda.api.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.components.replacer.IReplaceable;
import net.dv8tion.jda.api.components.tree.ComponentTree;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;
import net.dv8tion.jda.api.interactions.components.ItemComponent;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Component which supports interactions via {@link ComponentInteraction}.
 */
public interface ActionComponent extends ItemComponent, IDisableable
{
    /**
     * An unique component ID or {@code null}.
     * <br>Some components such as link buttons don't have this.
     *
     * <p>Custom IDs can contain custom data,
     * this is typically used to pass data between a slash command and your button listener.
     *
     * <p>While this ID is unique and can be retrieved with {@link ComponentInteraction#getComponentId()},
     * you should use {@link #getUniqueId()} to identify a component in a single message,
     * such as when replacing components using {@link ComponentTree} or {@link IReplaceable#replace(ComponentReplacer)}.
     *
     * @return The component ID or null if not present
     *
     * @see    ComponentInteraction#getComponentId()
     * @see    Component#getUniqueId()
     *
     * @deprecated
     *         Replaced with {@link #getCustomId()}
     */
    @Nullable
    @Deprecated
    @ForRemoval
    @ReplaceWith("getCustomId()")
    default String getId()
    {
        return getCustomId();
    }

    /**
     * An unique component ID or {@code null}.
     * <br>Some components such as link buttons don't have this.
     *
     * <p>Custom IDs can contain custom data,
     * this is typically used to pass data between a slash command and your button listener.
     *
     * <p>While this ID is unique and can be retrieved with {@link ComponentInteraction#getComponentId()},
     * you should use {@link #getUniqueId()} to identify a component in a single message,
     * such as when replacing components using {@link ComponentTree} or {@link IReplaceable#replace(ComponentReplacer)}.
     *
     * @return The component ID or null if not present
     *
     * @see    ComponentInteraction#getComponentId()
     * @see    Component#getUniqueId()
     */
    @Nullable
    String getCustomId();

    /**
     * Whether this action component is disabled.
     *
     * <p>You can use {@link #asDisabled()} or {@link #asEnabled()} to create enabled/disabled instances.
     *
     * @return True, if this button is disabled
     */
    boolean isDisabled();

    /**
     * Returns a copy of this component with {@link #isDisabled()} set to true.
     *
     * @return New disabled component instance
     */
    @Nonnull
    @CheckReturnValue
    default ActionComponent asDisabled()
    {
        return (ActionComponent) IDisableable.super.asDisabled();
    }


    /**
     * Returns a copy of this component with {@link #isDisabled()} set to false.
     *
     * @return New enabled component instance
     */
    @Nonnull
    @CheckReturnValue
    default ActionComponent asEnabled()
    {
        return (ActionComponent) IDisableable.super.asEnabled();
    }

    /**
     * Returns a copy of this component with {@link #isDisabled()} set to the provided value.
     *
     * @param  disabled
     *         True, if this component should be disabled
     *
     * @throws UnsupportedOperationException
     *         If this component type cannot be disabled
     *
     * @return New enabled/disabled component instance
     */
    @Nonnull
    @CheckReturnValue
    ActionComponent withDisabled(boolean disabled);
}
