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

import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.components.tree.MessageComponentTree;
import net.dv8tion.jda.internal.components.utils.ComponentsUtil;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.Collection;

public class MessageComponentTreeImpl
        extends AbstractComponentTree<MessageTopLevelComponentUnion>
        implements MessageComponentTree
{
    private MessageComponentTreeImpl(Collection<MessageTopLevelComponentUnion> components)
    {
        super(components);
    }

    @Nonnull
    public static MessageComponentTree of(@Nonnull Collection<? extends MessageTopLevelComponent> _components)
    {
        // Empty trees are allowed
        Checks.noneNull(_components, "Components");

        final Collection<MessageTopLevelComponentUnion> components = ComponentsUtil.membersToUnion(_components, MessageTopLevelComponentUnion.class);
        return new MessageComponentTreeImpl(components);
    }

    @Nonnull
    @Override
    public MessageComponentTree replace(ComponentReplacer replacer)
    {
        Checks.notNull(replacer, "ComponentReplacer");
        return ComponentsUtil.doReplace(
                MessageTopLevelComponent.class,
                components,
                replacer,
                MessageComponentTreeImpl::new
        );
    }

    @Nonnull
    @Override
    public MessageComponentTree withDisabled(boolean disabled)
    {
        return (MessageComponentTree) super.withDisabled(disabled);
    }
}
