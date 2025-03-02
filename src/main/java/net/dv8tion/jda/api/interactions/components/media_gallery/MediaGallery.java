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

package net.dv8tion.jda.api.interactions.components.media_gallery;

import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.IdentifiableComponent;
import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.interactions.components.container.ContainerChildComponent;
import net.dv8tion.jda.internal.interactions.components.media_gallery.MediaGalleryImpl;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.List;

public interface MediaGallery extends Component, IdentifiableComponent, MessageTopLevelComponent, ContainerChildComponent
{
    static MediaGallery of(MediaGalleryItem... items)
    {
        return new MediaGalleryImpl(items);
    }

    @Nonnull
    @Override
    @CheckReturnValue
    MediaGallery withUniqueId(int uniqueId);

    @Nonnull
    @Unmodifiable
    List<MediaGalleryItem> getItems();
}
