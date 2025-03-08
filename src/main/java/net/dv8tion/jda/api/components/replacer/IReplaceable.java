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

package net.dv8tion.jda.api.components.replacer;

import net.dv8tion.jda.api.components.Component;

import javax.annotation.Nonnull;

/**
 * Component which supports its children being replaced.
 *
 * <p>This is equivalent to {@link net.dv8tion.jda.api.components.tree.ComponentTree#replace(ComponentReplacer) ComponentTree.replace(ComponentReplacer)},
 * but on a smaller scope.
 */
public interface IReplaceable
{
    /**
     * Replaces and/or removes children components using the provided {@link ComponentReplacer},
     * and construct a new component from the result.
     *
     * @param  replacer
     *         The {@link ComponentReplacer} to apply
     *
     * @throws IllegalArgumentException
     *         If {@code null} is provided
     *
     * @return The new, updated component
     *
     * @see ComponentReplacer
     */
    @Nonnull
    Component replace(@Nonnull ComponentReplacer replacer);
}
