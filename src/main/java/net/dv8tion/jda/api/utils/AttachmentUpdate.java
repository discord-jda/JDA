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
import okhttp3.MultipartBody;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents existing message attachment.
 * <br>This is primarily used for message edit requests, to specify which attachments to retain in the message after the update.
 */
public class AttachmentUpdate implements AttachedFile, ISnowflake
{
    private final long id;
    private final String name;

    protected AttachmentUpdate(long id, String name)
    {
        this.id = id;
        this.name = name;
    }

    /**
     * Creates an {@link AttachmentUpdate} with the given attachment id.
     * <br>This is primarily used for message edit requests, to specify which attachments to retain in the message after the update.
     *
     * @param  id
     *         The id of the attachment to retain
     *
     * @return {@link AttachmentUpdate}
     */
    @Nonnull
    public static AttachmentUpdate fromAttachment(long id)
    {
        return new AttachmentUpdate(id, null);
    }

    /**
     * Creates an {@link AttachmentUpdate} with the given attachment id.
     * <br>This is primarily used for message edit requests, to specify which attachments to retain in the message after the update.
     *
     * @param  id
     *         The id of the attachment to retain
     *
     * @throws IllegalArgumentException
     *         If the id is not a valid snowflake
     *
     * @return {@link AttachmentUpdate}
     */
    @Nonnull
    public static AttachmentUpdate fromAttachment(@Nonnull String id)
    {
        return fromAttachment(MiscUtil.parseSnowflake(id));
    }

    /**
     * Creates an {@link AttachmentUpdate} with the given attachment.
     * <br>This is primarily used for message edit requests, to specify which attachments to retain in the message after the update.
     *
     * @param  attachment
     *         The attachment to retain
     *
     * @return {@link AttachmentUpdate}
     */
    @Nonnull
    public static AttachmentUpdate fromAttachment(@Nonnull Message.Attachment attachment)
    {
        Checks.notNull(attachment, "Attachment");
        return new AttachmentUpdate(attachment.getIdLong(), attachment.getFileName());
    }

    /**
     * The existing attachment filename.
     *
     * @return The filename, or {@code null} if not provided
     */
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

    @Override
    public void claim() {}

    @Override
    public boolean isClaimed()
    {
        return false;
    }

    @Override
    public void addPart(@Nonnull MultipartBody.Builder builder, int index) {}

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

    @Override
    public String toString()
    {
        return "AttachedFile[Attachment]" + (name == null ? "" : ":" + name) + '(' + id + ')';
    }
}
