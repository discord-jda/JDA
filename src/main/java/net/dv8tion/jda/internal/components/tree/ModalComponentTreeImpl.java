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

package net.dv8tion.jda.internal.components.tree;

import net.dv8tion.jda.api.components.ModalTopLevelComponent;
import net.dv8tion.jda.api.components.ModalTopLevelComponentUnion;
import net.dv8tion.jda.api.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.components.tree.ModalComponentTree;
import net.dv8tion.jda.internal.components.utils.ComponentsUtil;
import net.dv8tion.jda.internal.utils.Checks;

import java.util.Collection;

import javax.annotation.Nonnull;

public class ModalComponentTreeImpl extends AbstractComponentTree<ModalTopLevelComponentUnion>
        implements ModalComponentTree {
    private ModalComponentTreeImpl(Collection<ModalTopLevelComponentUnion> components) {
        super(components);
    }

    @Nonnull
    public static ModalComponentTree of(
            @Nonnull Collection<? extends ModalTopLevelComponent> components) {
        Checks.notEmpty(components, "Components");
        Checks.noneNull(components, "Components");

        // Allow unknown components so [[Modal#getComponentTree]] works
        Collection<ModalTopLevelComponentUnion> componentUnions =
                ComponentsUtil.membersToUnionWithUnknownType(
                        components, ModalTopLevelComponentUnion.class);
        return new ModalComponentTreeImpl(componentUnions);
    }

    @Nonnull
    @Override
    public Type getType() {
        return Type.MODAL;
    }

    @Nonnull
    @Override
    public ModalComponentTree replace(@Nonnull ComponentReplacer replacer) {
        Checks.notNull(replacer, "ComponentReplacer");
        return ComponentsUtil.doReplace(
                ModalTopLevelComponent.class, components, replacer, ModalComponentTreeImpl::new);
    }

    @Nonnull
    @Override
    public ModalComponentTree withDisabled(boolean disabled) {
        return (ModalComponentTree) super.withDisabled(disabled);
    }
}
