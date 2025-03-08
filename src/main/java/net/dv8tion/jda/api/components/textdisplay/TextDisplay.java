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

package net.dv8tion.jda.api.components.textdisplay;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.IdentifiableComponent;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.section.SectionContentComponent;
import net.dv8tion.jda.internal.components.textdisplay.TextDisplayImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

// TODO-components-v2 docs
public interface TextDisplay extends Component, IdentifiableComponent, MessageTopLevelComponent, ContainerChildComponent, SectionContentComponent
{
    // TODO-components-v2 docs
    @Nonnull
    static TextDisplay create(@Nonnull String content)
    {
        Checks.notNull(content, "Content");
        return new TextDisplayImpl(content);
    }

    @Nonnull
    @Override
    @CheckReturnValue
    TextDisplay withUniqueId(int uniqueId);

    // TODO-components-v2 docs
    @Nonnull
    @CheckReturnValue
    TextDisplay withContent(@Nonnull String content);

    // TODO-components-v2 docs
    @Nonnull
    String getContent();
}
