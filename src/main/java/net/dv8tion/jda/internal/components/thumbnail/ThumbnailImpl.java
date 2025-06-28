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

package net.dv8tion.jda.internal.components.thumbnail;

import net.dv8tion.jda.api.components.ResolvedMedia;
import net.dv8tion.jda.api.components.section.SectionAccessoryComponentUnion;
import net.dv8tion.jda.api.components.thumbnail.Thumbnail;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.AttachmentUpdate;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.components.AbstractComponentImpl;
import net.dv8tion.jda.internal.components.ResolvedMediaImpl;
import net.dv8tion.jda.internal.entities.FileContainerMixin;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EntityString;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Represents either an external link, an attachment:// link, or an existing item (which is also a link)
 */
public class ThumbnailImpl
        extends AbstractComponentImpl
        implements Thumbnail, SectionAccessoryComponentUnion, FileContainerMixin
{
    private final int uniqueId;
    private final String url;
    private final ResolvedMedia media;
    private final String description;
    private final boolean spoiler;

    public ThumbnailImpl(DataObject data)
    {
        this(
                data.getInt("id"),
                data.getObject("media").getString("url"),
                new ResolvedMediaImpl(data.getObject("media")),
                data.getString("description", null),
                data.getBoolean("spoiler", false)
        );
    }

    public ThumbnailImpl(String url)
    {
        this(-1, url, null, null, false);
    }

    private ThumbnailImpl(int uniqueId, String url, ResolvedMedia media, String description, boolean spoiler)
    {
        this.uniqueId = uniqueId;
        this.url = url;
        this.media = media;
        this.description = description;
        this.spoiler = spoiler;
    }

    @Nonnull
    @Override
    public Type getType()
    {
        return Type.THUMBNAIL;
    }

    @Nonnull
    @Override
    public Thumbnail withUniqueId(int uniqueId)
    {
        Checks.positive(uniqueId, "Unique ID");
        return new ThumbnailImpl(uniqueId, url, media, description, spoiler);
    }

    @Nonnull
    @Override
    public Thumbnail withDescription(@Nullable String description)
    {
        if (description != null)
        {
            Checks.notBlank(description, "Description");
            Checks.notLonger(description, MAX_DESCRIPTION_LENGTH, "Description");
        }
        return new ThumbnailImpl(uniqueId, url, media, description, spoiler);
    }

    @Nonnull
    @Override
    public Thumbnail withSpoiler(boolean spoiler)
    {
        return new ThumbnailImpl(uniqueId, url, media, description, spoiler);
    }

    @Override
    public int getUniqueId()
    {
        return uniqueId;
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

    @Override
    public Stream<AttachedFile> getFiles(boolean shouldRetain)
    {
        if (media != null) // Retain or reupload the entire file
        {
            final String fileName = Helpers.getLastPathSegment(media.getUrl());
            final String attachmentId = media.getAttachmentId();
            if (shouldRetain && attachmentId != null)
                return Stream.of(AttachmentUpdate.fromAttachment(attachmentId, fileName));

            return Stream.of(media.getProxy().downloadAsFileUpload(fileName));
        }
        else // External URL or user-managed attachment
            return Stream.empty();
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
        final String outputUrl;
        if (media != null) // Retain or reupload the entire file, both cases uses attachment://
            outputUrl = "attachment://" + Helpers.getLastPathSegment(media.getUrl());
        else // External URL or user-managed attachment
            outputUrl = url;
        final DataObject json = DataObject.empty()
                .put("type", getType().getKey())
                .put("media", DataObject.empty().put("url", outputUrl))
                .put("spoiler", spoiler);
        if (uniqueId >= 0)
            json.put("id", uniqueId);
        if (description != null)
            json.put("description", description);
        return json;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this) return true;
        if (!(o instanceof ThumbnailImpl)) return false;
        ThumbnailImpl thumbnail = (ThumbnailImpl) o;
        return uniqueId == thumbnail.uniqueId && spoiler == thumbnail.spoiler && Objects.equals(url, thumbnail.url) && Objects.equals(description, thumbnail.description);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(uniqueId, url, description, spoiler);
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .addMetadata("id", uniqueId)
                .addMetadata("url", url)
                .addMetadata("media", media)
                .addMetadata("spoiler", spoiler)
                .addMetadata("description", description)
                .toString();
    }
}
