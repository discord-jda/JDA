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

package net.dv8tion.jda.api.components.mediagallery;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.internal.components.mediagallery.MediaGalleryImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

// TODO-components-v2 docs
public interface MediaGallery extends Component, MessageTopLevelComponent, ContainerChildComponent
{
    // TODO-components-v2 docs
    int MAX_ITEMS = 10;

    // TODO-components-v2 docs
    @Nonnull
    static MediaGallery of(@Nonnull Collection<? extends MediaGalleryItem> items)
    {
        return MediaGalleryImpl.of(items);
    }

    // TODO-components-v2 docs
    @Nonnull
    static MediaGallery of(@Nonnull MediaGalleryItem item, @Nonnull MediaGalleryItem... items)
    {
        Checks.notNull(item, "Item");
        Checks.noneNull(items, "Items");
        return of(Helpers.mergeVararg(item, items));
    }

    // TODO-components-v2 docs
    @Nonnull
    @Override
    @CheckReturnValue
    MediaGallery withUniqueId(int uniqueId);

    // TODO-components-v2 docs
    @Nonnull
    @Unmodifiable
    List<MediaGalleryItem> getItems();
}
