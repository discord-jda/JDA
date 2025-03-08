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

import net.dv8tion.jda.api.components.ResolvedMedia;
import net.dv8tion.jda.api.components.mediagallery.MediaGalleryItem;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.components.ResolvedMediaImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EntityString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class MediaGalleryItemImpl implements MediaGalleryItem
{
    private final String url, description;
    private final ResolvedMedia media;
    private final boolean spoiler;

    public MediaGalleryItemImpl(DataObject obj)
    {
        this(
                obj.getObject("media").getString("url"),
                obj.getString("description", null),
                new ResolvedMediaImpl(obj.getObject("media")),
                obj.getBoolean("spoiler", false)
        );
    }

    public MediaGalleryItemImpl(String url)
    {
        this(url, null, null, false);
    }

    public MediaGalleryItemImpl(String url, String description, ResolvedMedia media, boolean spoiler)
    {
        this.url = url;
        this.media = media;
        this.description = description;
        this.spoiler = spoiler;
    }

    @Nonnull
    @Override
    public MediaGalleryItem withDescription(@Nonnull String description)
    {
        Checks.notNull(description, "Description");
        Checks.notLonger(description, MAX_DESCRIPTION_LENGTH, "Description");
        return new MediaGalleryItemImpl(url, description, media, spoiler);
    }

    @Nonnull
    @Override
    public MediaGalleryItem withSpoiler(boolean spoiler)
    {
        return new MediaGalleryItemImpl(url, description, media, spoiler);
    }

    @Nonnull
    @Override
    public String getUrl()
    {
        return url;
    }

    @Nullable
    @Override
    public ResolvedMedia getResolvedMedia()
    {
        return media;
    }

    @Nullable
    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public boolean isSpoiler()
    {
        return spoiler;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        return DataObject.empty()
                .put("media", DataObject.empty().put("url", url))
                .put("description", description)
                .put("spoiler", spoiler);
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof MediaGalleryItemImpl)) return false;
        MediaGalleryItemImpl that = (MediaGalleryItemImpl) o;
        return spoiler == that.spoiler && Objects.equals(url, that.url) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(url, description, spoiler);
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .addMetadata("url", url)
                .addMetadata("media", media)
                .addMetadata("spoiler", spoiler)
                .addMetadata("description", description)
                .toString();
    }
}
