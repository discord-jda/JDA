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
import java.io.Closeable;
import java.io.InputStream;
import java.util.List;

public interface AttachedFile extends Closeable
{
    @Nonnull
    static FileUpload fromData(@Nonnull InputStream data, @Nonnull String name)
    {
        return FileUpload.fromData(data, name);
    }

    @Nonnull
    static FileUpload fromData(@Nonnull byte[] data, @Nonnull String name)
    {
        return FileUpload.fromData(data, name);
    }

    @Nonnull
    static AttachmentUpdate fromAttachment(long id)
    {
        return AttachmentUpdate.fromAttachment(id);
    }

    @Nonnull
    static AttachmentUpdate fromAttachment(@Nonnull String id)
    {
        return AttachmentUpdate.fromAttachment(id);
    }

    @Nonnull
    static AttachmentUpdate fromAttachment(@Nonnull Message.Attachment attachment)
    {
        return AttachmentUpdate.fromAttachment(attachment);
    }

    default void addPart(MultipartBody.Builder builder, int index) {}

    @Nonnull
    DataObject toAttachmentData(int index);

    @Nonnull
    static MultipartBody.Builder createMultipartBody(List<? extends AttachedFile> files, DataObject payloadJson)
    {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        DataArray descriptors = DataArray.empty();
        for (int i = 0; i < files.size(); i++)
        {
            AttachedFile file = files.get(i);
            file.addPart(builder, i);
            descriptors.add(file.toAttachmentData(i));
        }
// TODO: Add this when appropriate methods for editing attachments are provided in future changes
//        if (payloadJson == null)
//            payloadJson = DataObject.empty();
//        payloadJson.put("attachments", descriptors);
//        builder.addFormDataPart("payload_json", payloadJson.toString());
        return builder;
    }
}
