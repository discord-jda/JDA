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
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EntityString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class FileDisplayFileUpload extends AbstractComponentImpl implements FileDisplay, MessageTopLevelComponentUnion, ContainerChildComponentUnion
{
    private final int uniqueId;
    private final FileUpload file;
    private final boolean spoiler;

    public FileDisplayFileUpload(FileUpload file)
    {
        this(-1, file, false);
    }

    private FileDisplayFileUpload(int uniqueId, FileUpload file, boolean spoiler)
    {
        this.uniqueId = uniqueId;
        this.file = file;
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
        Checks.notNegative(uniqueId, "Unique ID");
        return new FileDisplayFileUpload(uniqueId, file, spoiler);
    }

    @Nonnull
    @Override
    public FileDisplay withSpoiler(boolean spoiler)
    {
        return new FileDisplayFileUpload(uniqueId, file, spoiler);
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
    public List<FileUpload> getFiles()
    {
        return Collections.singletonList(file);
    }

    @Nullable
    @Override
    public ResolvedMedia getResolvedMedia()
    {
        return null;
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
                .put("file", DataObject.empty().put("url", getUrl()))
                .put("spoiler", spoiler);
        if (uniqueId >= -1)
            json.put("id", uniqueId);
        return json;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof FileDisplayFileUpload)) return false;
        FileDisplayFileUpload fileDisplay = (FileDisplayFileUpload) o;
        return uniqueId == fileDisplay.uniqueId && spoiler == fileDisplay.spoiler && Objects.equals(file, fileDisplay.file);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(uniqueId, file, spoiler);
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .addMetadata("id", uniqueId)
                .addMetadata("file", file)
                .addMetadata("spoiler", spoiler)
                .toString();
    }
}
