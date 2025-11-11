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
import net.dv8tion.jda.api.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.components.replacer.IReplaceable;
import net.dv8tion.jda.api.components.tree.ComponentTree;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Component which has an associated custom id.
 */
public interface ICustomId extends Component {
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

    @Nonnull
    @Override
    ICustomId withUniqueId(int uniqueId);
}
