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

package net.dv8tion.jda.api.components.tree;

import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.components.replacer.ComponentReplacer;
import net.dv8tion.jda.internal.components.tree.MessageComponentTreeImpl;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;

/**
 * Specialization of {@link ComponentTree} for {@link MessageTopLevelComponentUnion top-level message components}.
 *
 * <p>
 * Use the static methods to construct a {@link MessageComponentTree}.
 *
 * @see ComponentTree
 */
public interface MessageComponentTree extends ComponentTree<MessageTopLevelComponentUnion>
{
    @Nonnull
    @Override
    @CheckReturnValue
    MessageComponentTree replace(ComponentReplacer replacer);

    @Nonnull
    @Override
    @CheckReturnValue
    MessageComponentTree withDisabled(boolean disabled);

    @Nonnull
    @Override
    @CheckReturnValue
    default MessageComponentTree asDisabled()
    {
        return (MessageComponentTree) ComponentTree.super.asDisabled();
    }

    @Nonnull
    @Override
    @CheckReturnValue
    default MessageComponentTree asEnabled()
    {
        return (MessageComponentTree) ComponentTree.super.asEnabled();
    }

    /**
     * Creates a {@link MessageComponentTree} from the given top-level message components.
     *
     * @param  components
     *         List of components to construct the tree from
     *
     * @throws IllegalArgumentException
     *         If {@code null} is provided
     *
     * @return A {@link MessageComponentTree} containing the given components
     */
    @Nonnull
    static MessageComponentTree of(@Nonnull Collection<? extends MessageTopLevelComponent> components)
    {
        return MessageComponentTreeImpl.of(components);
    }

    /**
     * Creates a {@link MessageComponentTree} from the given top-level message components.
     *
     * @param  components
     *         List of components to construct the tree from
     *
     * @throws IllegalArgumentException
     *         If {@code null} is provided
     *
     * @return A {@link MessageComponentTree} containing the given components
     */
    @Nonnull
    static MessageComponentTree of(@Nonnull MessageTopLevelComponent... components)
    {
        return of(Arrays.asList(components));
    }
}
