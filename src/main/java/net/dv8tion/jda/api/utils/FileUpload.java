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
import net.dv8tion.jda.internal.requests.Requester;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.IOUtil;
import okhttp3.MultipartBody;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class FileUpload implements Closeable
{
    private final InputStream resource;
    private final String name;
    private final long id;
    private IOConsumer<InputStream> onClose;

    protected FileUpload(InputStream resource, String name, long id, IOConsumer<InputStream> onClose)
    {
        this.resource = resource;
        this.name = name;
        this.id = id;
        this.onClose = onClose;
    }

    @Nonnull
    public static FileUpload fromData(@Nonnull InputStream data, @Nonnull String name)
    {
        Checks.notNull(data, "Data");
        Checks.notNull(name, "Name");
        return new FileUpload(data, name, 0, InputStream::close);
    }

    @Nonnull
    public static FileUpload fromData(@Nonnull byte[] data, @Nonnull String name)
    {
        Checks.notNull(data, "Data");
        Checks.notNull(name, "Name");
        return fromData(new ByteArrayInputStream(data), name);
    }

    @Nonnull
    public static FileUpload fromAttachment(long id)
    {
        return new FileUpload(null, null, id, null);
    }

    @Nonnull
    public static FileUpload fromAttachment(@Nonnull String id)
    {
        return fromAttachment(MiscUtil.parseSnowflake(id));
    }

    @Nonnull
    public static FileUpload fromAttachment(@Nonnull Message.Attachment attachment)
    {
        Checks.notNull(attachment, "Attachment");
        return fromAttachment(attachment.getIdLong());
    }

    @Override
    public void close() throws IOException
    {
        if (onClose != null && resource != null)
            onClose.accept(resource);
    }

    public boolean isData()
    {
        return resource != null;
    }

    @Nullable
    public String getName()
    {
        return name;
    }

    @Nullable
    public InputStream getData()
    {
        return resource;
    }

    public long getId()
    {
        return id;
    }

    public void addPart(MultipartBody.Builder builder, int index)
    {
        builder.addFormDataPart("files[" + index + "]", name, IOUtil.createRequestBody(Requester.MEDIA_TYPE_OCTET, resource));
    }

    public static MultipartBody.Builder createMultipartBody(List<? extends FileUpload> files)
    {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        for (int i = 0; i < files.size(); i++)
        {
            FileUpload file = files.get(i);
            file.addPart(builder, i);
        }
        return builder;
    }
}
