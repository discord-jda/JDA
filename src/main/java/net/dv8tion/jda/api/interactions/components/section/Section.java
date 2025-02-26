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

package net.dv8tion.jda.api.interactions.components.section;

import net.dv8tion.jda.api.interactions.components.IdentifiableComponent;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.interactions.components.container.ContainerChildComponent;
import net.dv8tion.jda.internal.interactions.components.replacer.IReplacerAware;
import net.dv8tion.jda.internal.interactions.components.section.SectionImpl;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public interface Section extends LayoutComponent<SectionContentComponentUnion>, IdentifiableComponent, MessageTopLevelComponent, ContainerChildComponent, IReplacerAware<Section>
{
    static Section of(Collection<? extends SectionContentComponent> children)
    {
        return SectionImpl.of(children);
    }

    static Section of(SectionContentComponent... children)
    {
        return of(Arrays.asList(children));
    }

    // TODO-components-v2 docs
    @Nonnull
    @CheckReturnValue
    Section withUniqueId(int uniqueId);

    @Nonnull
    @Unmodifiable
    List<SectionContentComponentUnion> getComponents();

    @Nullable
    SectionAccessoryComponentUnion getAccessory();

    //TODO maybe this should be part of a builder? maybe not as there's not many things to configure
    Section withAccessory(@Nullable SectionAccessoryComponent accessory);
}
