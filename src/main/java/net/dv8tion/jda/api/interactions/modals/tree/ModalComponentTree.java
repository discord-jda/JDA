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

package net.dv8tion.jda.api.interactions.modals.tree;

import net.dv8tion.jda.api.interactions.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.interactions.components.tree.ComponentTree;
import net.dv8tion.jda.api.interactions.modals.ModalTopLevelComponent;
import net.dv8tion.jda.api.interactions.modals.ModalTopLevelComponentUnion;
import net.dv8tion.jda.internal.interactions.components.tree.ModalComponentTreeImpl;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;

/**
 * Specialization of {@link ComponentTree} for {@link ModalTopLevelComponentUnion top-level modal components}
 *
 * @see ComponentTree
 */
public interface ModalComponentTree extends ComponentTree<ModalTopLevelComponentUnion>
{
    @Nonnull
    @Override
    ModalComponentTree disableAll();

    @Nonnull
    @Override
    ModalComponentTree replace(ComponentReplacer replacer);

    /**
     * Creates a {@link ModalComponentTree} from the given top-level modal components.
     *
     * @param  components
     *         List of components to construct the tree from
     *
     * @throws IllegalArgumentException
     *         If {@code null} is provided
     *
     * @return A {@link ModalComponentTree} containing the given components
     */
    @Nonnull
    static ModalComponentTree of(@Nonnull Collection<? extends ModalTopLevelComponent> components)
    {
        return ModalComponentTreeImpl.of(components);
    }

    /**
     * Creates a {@link ModalComponentTree} from the given top-level modal components.
     *
     * @param  components
     *         List of components to construct the tree from
     *
     * @throws IllegalArgumentException
     *         If {@code null} is provided
     *
     * @return A {@link ModalComponentTree} containing the given components
     */
    @Nonnull
    static ModalComponentTree of(@Nonnull ModalTopLevelComponent... components)
    {
        return of(Arrays.asList(components));
    }
}
