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
import java.io.*;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
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
     * Create a new {@link FileUpload} for a local file.
     * <br>This is used to upload data to discord for various purposes.
     *
     * <p>This opens a {@link FileInputStream}, which will be closed on consumption by the request.
     * You can use {@link FileUpload#close()} to close the stream manually.
     *
     * @param  file
     *         The {@link File} to upload
     * @param  name
     *         The representative name to use for the file
     *
     * @throws IllegalArgumentException
     *         If null is provided or the name is empty
     * @throws UncheckedIOException
     *         If an IOException is thrown while opening the file
     *
     * @return {@link FileUpload}
     *
     * @see    java.io.FileInputStream FileInputStream
     */
    @Nonnull
    static FileUpload fromData(@Nonnull File file, @Nonnull String name)
    {
        return FileUpload.fromData(file, name);
    }

    /**
     * Create a new {@link FileUpload} for a local file.
     * <br>This is used to upload data to discord for various purposes.
     *
     * <p>This opens a {@link FileInputStream}, which will be closed on consumption by the request.
     * You can use {@link FileUpload#close()} to close the stream manually.
     *
     * @param  file
     *         The {@link File} to upload
     *
     * @throws IllegalArgumentException
     *         If null is provided
     * @throws UncheckedIOException
     *         If an IOException is thrown while opening the file
     *
     * @return {@link FileUpload}
     *
     * @see    java.io.FileInputStream FileInputStream
     * @see    #fromData(File, String)
     */
    @Nonnull
    static FileUpload fromData(@Nonnull File file)
    {
        return FileUpload.fromData(file);
    }

    /**
     * Create a new {@link FileUpload} for a local file.
     * <br>This is used to upload data to discord for various purposes.
     *
     * <p>This opens the path using {@link Files#newInputStream(Path, OpenOption...)}, which will be closed on consumption by the request.
     * You can use {@link FileUpload#close()} to close the stream manually.
     *
     * @param  path
     *         The {@link Path} of the file to upload
     * @param  name
     *         The representative name to use for the file
     * @param  options
     *         The {@link OpenOption OpenOptions} specifying how the file is opened
     *
     * @throws IllegalArgumentException
     *         If null is provided or the name is empty
     * @throws UncheckedIOException
     *         If an IOException is thrown while opening the file
     *
     * @return {@link FileUpload}
     */
    @Nonnull
    static FileUpload fromData(@Nonnull Path path, @Nonnull String name, @Nonnull OpenOption... options)
    {
        return FileUpload.fromData(path, name, options);
    }

    /**
     * Create a new {@link FileUpload} for a local file.
     * <br>This is used to upload data to discord for various purposes.
     * Uses {@link Path#getFileName()} to specify the name of the file, to customize the filename use {@link #fromData(Path, String, OpenOption...)}.
     *
     * <p>This opens the path using {@link Files#newInputStream(Path, OpenOption...)}, which will be closed on consumption by the request.
     * You can use {@link FileUpload#close()} to close the stream manually.
     *
     * @param  path
     *         The {@link Path} of the file to upload
     * @param  options
     *         The {@link OpenOption OpenOptions} specifying how the file is opened
     *
     * @throws IllegalArgumentException
     *         If null is provided
     * @throws UncheckedIOException
     *         If an IOException is thrown while opening the file
     *
     * @return {@link FileUpload}
     */
    @Nonnull
    static FileUpload fromData(@Nonnull Path path, @Nonnull OpenOption... options)
    {
        return FileUpload.fromData(path, options);
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
     * Marks this attachment as used and throws if it has already been used.
     * <br>This does nothing on {@link AttachmentUpdate}.
     *
     * @throws IllegalStateException
     *         If this attachment has already been used
     */
    void claim();

    /**
     * Whether this attached file has already been used.
     * <br>When this is true, {@link #claim()} will throw an {@link IllegalStateException}.
     *
     * <p>Resources cannot be read multiple times, so repeated use of this instance is not allowed.
     *
     * @return True if this attachment has already been used
     */
    boolean isClaimed();

    /**
     * Used internally to build the multipart request.
     *
     * <p>The index can be used as a unique identifier for the multipart name, which is required to be unique by Discord.
     *
     * @param builder
     *        The {@link MultipartBody.Builder} used for the request body
     * @param index
     *        The index of the attachment, ignored for {@link AttachmentUpdate}
     */
    void addPart(@Nonnull MultipartBody.Builder builder, int index);

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
