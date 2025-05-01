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

package net.dv8tion.jda.internal.components.filedisplay;

import net.dv8tion.jda.api.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.components.ResolvedMedia;
import net.dv8tion.jda.api.components.container.ContainerChildComponentUnion;
import net.dv8tion.jda.api.components.filedisplay.FileDisplay;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.components.AbstractComponentImpl;
import net.dv8tion.jda.internal.components.ResolvedMediaImpl;
import net.dv8tion.jda.internal.entities.FileContainerMixin;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EntityString;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.stream.Stream;

public class FileDisplayImpl
        extends AbstractComponentImpl
        implements FileDisplay, MessageTopLevelComponentUnion, ContainerChildComponentUnion, FileContainerMixin
{
    private final int uniqueId;
    private final String url;
    private final ResolvedMedia media;
    private final boolean spoiler;

    public FileDisplayImpl(DataObject data)
    {
        this(
                data.getInt("id"),
                data.getObject("file").getString("url"),
                new ResolvedMediaImpl(data.getObject("file")),
                data.getBoolean("spoiler", false)
        );
    }

    private FileDisplayImpl(int uniqueId, String url, ResolvedMedia media, boolean spoiler)
    {
        this.uniqueId = uniqueId;
        this.url = url;
        this.media = media;
        this.spoiler = spoiler;
    }

    @Nonnull
    @Override
    public Type getType()
    {
        return Type.FILE_DISPLAY;
    }

    @Nonnull
    @Override
    public FileDisplay withUniqueId(int uniqueId)
    {
        Checks.positive(uniqueId, "Unique ID");
        return new FileDisplayImpl(uniqueId, url, media, spoiler);
    }

    @Nonnull
    @Override
    public FileDisplay withSpoiler(boolean spoiler)
    {
        return new FileDisplayImpl(uniqueId, url, media, spoiler);
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

    @Nonnull
    @Override
    public ResolvedMedia getResolvedMedia()
    {
        return media;
    }

    @Override
    public Stream<FileUpload> getFiles() {
        final String fileName = Helpers.getLastPathSegment(media.getUrl());
        return Stream.of(media.getProxy().downloadAsFileUpload(fileName));
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
        final String fileName = Helpers.getLastPathSegment(media.getUrl());
        final DataObject json = DataObject.empty()
                .put("type", getType().getKey())
                .put("file", DataObject.empty().put("url", "attachment://" + fileName))
                .put("spoiler", spoiler);
        if (uniqueId >= 0)
            json.put("id", uniqueId);
        return json;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof FileDisplayImpl)) return false;
        FileDisplayImpl fileDisplay = (FileDisplayImpl) o;
        return uniqueId == fileDisplay.uniqueId && spoiler == fileDisplay.spoiler && Objects.equals(url, fileDisplay.url);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(uniqueId, url, spoiler);
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .addMetadata("id", uniqueId)
                .addMetadata("url", url)
                .addMetadata("media", media)
                .addMetadata("spoiler", spoiler)
                .toString();
    }
}
