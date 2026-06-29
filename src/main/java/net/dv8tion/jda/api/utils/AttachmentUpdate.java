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
import net.dv8tion.jda.internal.utils.EntityString;
import okhttp3.MultipartBody;
import org.jetbrains.annotations.Contract;

import java.util.Objects;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents existing message attachment.
 * <br>This is primarily used for message edit requests, to specify which attachments to retain in the message after the update.
 */
public class AttachmentUpdate implements AttachedFile, ISnowflake {
    private final long id;
    private final String name;
    private final String description;
    private final Boolean isSpoiler;

    protected AttachmentUpdate(long id, String name, String description, Boolean isSpoiler) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isSpoiler = isSpoiler;
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
    public static AttachmentUpdate fromAttachment(long id) {
        return new AttachmentUpdate(id, null, null, null);
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
    public static AttachmentUpdate fromAttachment(@Nonnull String id) {
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
    public static AttachmentUpdate fromAttachment(@Nonnull Message.Attachment attachment) {
        Checks.notNull(attachment, "Attachment");
        return new AttachmentUpdate(
                attachment.getIdLong(), attachment.getFileName(), attachment.getDescription(), attachment.isSpoiler());
    }

    /**
     * The existing attachment filename.
     *
     * @return The filename, or {@code null} if not provided
     */
    @Nullable
    public String getName() {
        return name;
    }

    /**
     * Whether this attachment will be marked as a spoiler after the update request.
     *
     * <p>This is {@code null} for attachment updates that do not overwrite the spoiler state.
     *
     * @return {@code true}, if the attachment will be marked as a spoiler, {@code false} otherwise, or {@code null} if not provided
     */
    @Nullable
    public Boolean getSpoiler() {
        return isSpoiler;
    }

    /**
     * The updated attachment description.
     *
     * @return The description, or {@code null} if not provided
     */
    @Nullable
    public String getDescription() {
        return description;
    }

    /**
     * The new description for this attachment.
     *
     * @param description
     *        The updated description
     *
     * @throws IllegalArgumentException
     *         If {@code null} is provided, or the description is longer than {@value AttachedFile#MAX_DESCRIPTION_LENGTH}
     *
     * @return The updated AttachmentUpdate
     */
    @Nonnull
    @Contract("_->new")
    @CheckReturnValue
    public AttachmentUpdate withDescription(@Nonnull String description) {
        Checks.notNull(description, "Description");
        Checks.notLonger(description, MAX_DESCRIPTION_LENGTH, "Description");
        return new AttachmentUpdate(id, name, description, isSpoiler);
    }

    /**
     * Whether this attachment will be marked as a spoiler after the update request.
     *
     * @param  spoiler
     *         True, if the attachment will be marked as a spoiler, false otherwise
     *
     * @return The updated AttachmentUpdate
     */
    @Nonnull
    @Contract("_->new")
    @CheckReturnValue
    public AttachmentUpdate withSpoiler(boolean spoiler) {
        return new AttachmentUpdate(id, name, description, spoiler);
    }

    @Override
    public long getIdLong() {
        return id;
    }

    @Override
    public void addPart(@Nonnull MultipartBody.Builder builder, int index) {}

    @Nonnull
    @Override
    public DataObject toAttachmentData(int index) {
        DataObject object = DataObject.empty().put("id", getId());
        if (name != null) {
            object.put("filename", name);
        }
        if (isSpoiler != null) {
            object.put("is_spoiler", isSpoiler);
        }
        if (description != null) {
            object.put("description", description);
        }
        return object;
    }

    @Override
    public void close() {}

    @Override
    public void forceClose() {}

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AttachmentUpdate)) {
            return false;
        }
        AttachmentUpdate that = (AttachmentUpdate) o;
        return id == that.id && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        EntityString entityString = new EntityString("AttachedFile").setType("Attachment");
        if (name != null) {
            entityString.setName(name);
        }
        return entityString.toString();
    }
}
