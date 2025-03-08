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

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.ComponentUnion;
import net.dv8tion.jda.api.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.components.tree.ComponentTree;
import net.dv8tion.jda.internal.components.utils.ComponentsUtil;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.Collection;

public final class ComponentTreeImpl<E extends ComponentUnion> extends AbstractComponentTree<E>
{
    private final Class<? extends Component> componentType;

    public ComponentTreeImpl(Class<? extends Component> componentType, Collection<E> components)
    {
        super(components);
        this.componentType = componentType;
    }

    @Nonnull
    @Override
    public ComponentTree<E> replace(ComponentReplacer replacer)
    {
        Checks.notNull(replacer, "ComponentReplacer");
        return ComponentsUtil.doReplace(
                componentType,
                components,
                replacer,
                (newComponents) -> new ComponentTreeImpl<>(componentType, newComponents)
        );
    }
}
