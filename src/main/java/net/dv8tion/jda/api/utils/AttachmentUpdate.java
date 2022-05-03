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

package net.dv8tion.jda.api.utils;

import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AttachmentUpdate implements AttachedFile, ISnowflake
{
    private final long id;
    private final String name;

    protected AttachmentUpdate(long id, String name)
    {
        this.id = id;
        this.name = name;
    }

    @Nonnull
    public static AttachmentUpdate fromAttachment(long id)
    {
        return new AttachmentUpdate(id, null);
    }

    @Nonnull
    public static AttachmentUpdate fromAttachment(@Nonnull String id)
    {
        return fromAttachment(MiscUtil.parseSnowflake(id));
    }

    @Nonnull
    public static AttachmentUpdate fromAttachment(@Nonnull Message.Attachment attachment)
    {
        Checks.notNull(attachment, "Attachment");
        return new AttachmentUpdate(attachment.getIdLong(), attachment.getFileName());
    }

    @Nullable
    public String getName()
    {
        return name;
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @Nonnull
    @Override
    public DataObject toAttachmentData(int index)
    {
        DataObject object = DataObject.empty().put("id", getId());
        if (name != null)
            object.put("filename", name);
        return object;
    }

    @Override
    public void close() {}
}
