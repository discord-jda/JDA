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

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import okhttp3.MultipartBody;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.InputStream;
import java.util.List;

/**
 * Represents files that are attached to requests.
 */
public interface AttachedFile extends Closeable
{
    /**
     * Create a new {@link FileUpload} for an input stream.
     * <br>This is used to upload data to discord for various purposes.
     *
     * <p>The {@link InputStream} will be closed on consumption by the request.
     * You can use {@link FileUpload#close()} to close the stream manually.
     *
     * @param  data
     *         The {@link InputStream} to upload
     * @param  name
     *         The representative name to use for the file
     *
     * @throws IllegalArgumentException
     *         If null is provided or the name is empty
     *
     * @return {@link FileUpload}
     *
     * @see    java.io.FileInputStream FileInputStream
     */
    @Nonnull
    static FileUpload fromData(@Nonnull InputStream data, @Nonnull String name)
    {
        return FileUpload.fromData(data, name);
    }

    /**
     * Create a new {@link FileUpload} for a byte array.
     * <br>This is used to upload data to discord for various purposes.
     *
     * @param  data
     *         The {@code byte[]} to upload
     * @param  name
     *         The representative name to use for the file
     *
     * @throws IllegalArgumentException
     *         If null is provided or the name is empty
     *
     * @return {@link FileUpload}
     */
    @Nonnull
    static FileUpload fromData(@Nonnull byte[] data, @Nonnull String name)
    {
        return FileUpload.fromData(data, name);
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
    static AttachmentUpdate fromAttachment(long id)
    {
        return AttachmentUpdate.fromAttachment(id);
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
    static AttachmentUpdate fromAttachment(@Nonnull String id)
    {
        return AttachmentUpdate.fromAttachment(id);
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
    static AttachmentUpdate fromAttachment(@Nonnull Message.Attachment attachment)
    {
        return AttachmentUpdate.fromAttachment(attachment);
    }

    /**
     * Used internally to build the multipart request.
     *
     * @param builder
     *        The {@link MultipartBody.Builder} used for the request body
     * @param index
     *        The index of the attachment, ignored for {@link AttachmentUpdate}
     */
    default void addPart(MultipartBody.Builder builder, int index) {}

    /**
     * Used internally to build attachment descriptions for requests.
     * <br>This contains the id/index of the attachment, and the name of the file.
     *
     * @param  index
     *         The reference index (should be same as {@link #addPart(MultipartBody.Builder, int)})
     *
     * @return {@link DataObject} for the attachment
     */
    @Nonnull
    DataObject toAttachmentData(int index);

    /**
     * Build a complete request using the provided files and payload data.
     * <br>If the provided {@code payloadJson} is null, the multipart request will not set {@code attachments}.
     *
     * @param  files
     *         The files to upload/edit
     * @param  payloadJson
     *         The payload data to send, excluding {@code attachments} field
     *
     * @throws IllegalArgumentException
     *         If the file list is null
     *
     * @return {@link MultipartBody.Builder}
     */
    @Nonnull
    static MultipartBody.Builder createMultipartBody(@Nonnull List<? extends AttachedFile> files, @Nullable DataObject payloadJson)
    {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        DataArray descriptors = DataArray.empty();
        for (int i = 0; i < files.size(); i++)
        {
            AttachedFile file = files.get(i);
            file.addPart(builder, i);
            descriptors.add(file.toAttachmentData(i));
        }

        if (payloadJson == null)
            return builder;
        payloadJson.put("attachments", descriptors);
        builder.addFormDataPart("payload_json", payloadJson.toString());
        return builder;
    }
}
