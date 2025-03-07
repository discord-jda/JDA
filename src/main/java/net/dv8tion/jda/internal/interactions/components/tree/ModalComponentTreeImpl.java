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

package net.dv8tion.jda.internal.interactions.components.tree;

import net.dv8tion.jda.api.interactions.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.interactions.modals.ModalTopLevelComponent;
import net.dv8tion.jda.api.interactions.modals.ModalTopLevelComponentUnion;
import net.dv8tion.jda.api.interactions.modals.tree.ModalComponentTree;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.ComponentsUtil;

import javax.annotation.Nonnull;
import java.util.Collection;

public class ModalComponentTreeImpl extends AbstractComponentTree<ModalTopLevelComponentUnion> implements ModalComponentTree
{
    private ModalComponentTreeImpl(Collection<ModalTopLevelComponentUnion> components)
    {
        super(components);
    }

    @Nonnull
    public static ModalComponentTree of(@Nonnull Collection<? extends ModalTopLevelComponent> _components)
    {
        // Empty trees are allowed
        Checks.noneNull(_components, "Components");

        final Collection<ModalTopLevelComponentUnion> components = ComponentsUtil.membersToUnion(_components, ModalTopLevelComponentUnion.class);
        return new ModalComponentTreeImpl(components);
    }

    @Nonnull
    @Override
    public ModalComponentTree replace(ComponentReplacer replacer)
    {
        Checks.notNull(replacer, "ComponentReplacer");
        return ComponentsUtil.doReplace(
                ModalTopLevelComponent.class,
                components,
                replacer,
                ModalComponentTreeImpl::new
        );
    }

    @Nonnull
    @Override
    public ModalComponentTree disableAll()
    {
        return (ModalComponentTree) super.disableAll();
    }
}
