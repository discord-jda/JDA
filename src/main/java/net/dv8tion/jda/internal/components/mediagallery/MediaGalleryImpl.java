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

package net.dv8tion.jda.internal.components.mediagallery;

import net.dv8tion.jda.api.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.components.container.ContainerChildComponentUnion;
import net.dv8tion.jda.api.components.mediagallery.MediaGallery;
import net.dv8tion.jda.api.components.mediagallery.MediaGalleryItem;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.components.AbstractComponentImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EntityString;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MediaGalleryImpl
        extends AbstractComponentImpl
        implements MediaGallery, MessageTopLevelComponentUnion, ContainerChildComponentUnion
{
    private final int uniqueId;
    private final List<MediaGalleryItem> items;

    public MediaGalleryImpl(DataObject data)
    {
        this(
                data.getInt("id"),
                data.getArray("items")
                        .stream(DataArray::getObject)
                        .map(MediaGalleryItemImpl::new)
                        .collect(Collectors.toList())
        );
    }

    private MediaGalleryImpl(Collection<? extends MediaGalleryItem> items)
    {
        this(-1, items);
    }

    private MediaGalleryImpl(int uniqueId, Collection<? extends MediaGalleryItem> items)
    {
        this.uniqueId = uniqueId;
        this.items = Helpers.copyAsUnmodifiableList(items);
    }

    @Nonnull
    public static MediaGallery of(@Nonnull Collection<? extends MediaGalleryItem> items)
    {
        Checks.noneNull(items, "Items");
        Checks.check(items.size() <= MAX_ITEMS, "A media gallery can only contain %d items, provided: %d", MAX_ITEMS, items.size());
        return new MediaGalleryImpl(items);
    }

    @Nonnull
    @Override
    public Type getType()
    {
        return Type.MEDIA_GALLERY;
    }

    @Nonnull
    @Override
    public MediaGallery withUniqueId(int uniqueId)
    {
        Checks.notNegative(uniqueId, "Unique ID");
        return new MediaGalleryImpl(uniqueId, items);
    }

    @Override
    public int getUniqueId()
    {
        return uniqueId;
    }

    @Nonnull
    @Override
    public List<MediaGalleryItem> getItems()
    {
        return items;
    }

    @Override
    public List<FileUpload> getFiles()
    {
        return items.stream()
                .filter(MediaGalleryItemFileUpload.class::isInstance)
                .map(MediaGalleryItemFileUpload.class::cast)
                .map(MediaGalleryItemFileUpload::getFile)
                .collect(Collectors.toList());
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        final DataObject json = DataObject.empty()
                .put("type", getType().getKey())
                .put("items", DataArray.fromCollection(getItems()));
        if (uniqueId >= 0)
            json.put("id", uniqueId);
        return json;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof MediaGalleryImpl)) return false;
        MediaGalleryImpl that = (MediaGalleryImpl) o;
        return uniqueId == that.uniqueId && Objects.equals(items, that.items);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(uniqueId, items);
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .addMetadata("id", uniqueId)
                .addMetadata("items", items)
                .toString();
    }
}
