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
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.components.AbstractComponentImpl;
import net.dv8tion.jda.internal.entities.FileContainerMixin;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EntityString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.stream.Stream;

public class ThumbnailFileUpload
        extends AbstractComponentImpl
        implements Thumbnail, SectionAccessoryComponentUnion, FileContainerMixin
{
    private final int uniqueId;
    private final FileUpload file;
    private final String description;
    private final boolean spoiler;

    public ThumbnailFileUpload(FileUpload file)
    {
        this(-1, file, null, false);
    }

    private ThumbnailFileUpload(int uniqueId, FileUpload file, String description, boolean spoiler)
    {
        this.uniqueId = uniqueId;
        this.file = file;
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
        Checks.notNegative(uniqueId, "Unique ID");
        return new ThumbnailFileUpload(uniqueId, file, description, spoiler);
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
        return new ThumbnailFileUpload(uniqueId, file, description, spoiler);
    }

    @Nonnull
    @Override
    public Thumbnail withSpoiler(boolean spoiler)
    {
        return new ThumbnailFileUpload(uniqueId, file, description, spoiler);
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
        return "attachment://" + file.getName();
    }

    @Override
    public Stream<FileUpload> getFiles()
    {
        return Stream.of(file);
    }

    @Nullable
    @Override
    public ResolvedMedia getResolvedMedia()
    {
        return null;
    }

    @Nullable
    @Override
    public String getDescription()
    {
        final String fileDescription = file.getDescription();
        if (fileDescription != null)
            return fileDescription;
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
        final DataObject json = DataObject.empty()
                .put("type", getType().getKey())
                .put("media", DataObject.empty().put("url", getUrl()))
                .put("spoiler", spoiler);
        if (uniqueId >= 0)
            json.put("id", uniqueId);
        if (getDescription() != null)
            json.put("description", getDescription());
        return json;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof ThumbnailFileUpload)) return false;
        ThumbnailFileUpload thumbnail = (ThumbnailFileUpload) o;
        return uniqueId == thumbnail.uniqueId && spoiler == thumbnail.spoiler && Objects.equals(file, thumbnail.file) && Objects.equals(description, thumbnail.description);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(uniqueId, file, description, spoiler);
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .addMetadata("id", uniqueId)
                .addMetadata("file", file)
                .addMetadata("spoiler", spoiler)
                .addMetadata("description", description)
                .toString();
    }
}
