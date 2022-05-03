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

import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.Requester;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.IOUtil;
import okhttp3.MultipartBody;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class FileUpload implements Closeable, AttachedFile
{
    private final InputStream resource;
    private final String name;

    protected FileUpload(InputStream resource, String name)
    {
        this.resource = resource;
        this.name = name;
    }

    @Nonnull
    public static FileUpload fromData(@Nonnull InputStream data, @Nonnull String name)
    {
        Checks.notNull(data, "Data");
        Checks.notNull(name, "Name");
        return new FileUpload(data, name);
    }

    @Nonnull
    public static FileUpload fromData(@Nonnull byte[] data, @Nonnull String name)
    {
        Checks.notNull(data, "Data");
        Checks.notNull(name, "Name");
        return fromData(new ByteArrayInputStream(data), name);
    }

    @Override
    public void close() throws IOException
    {
        if (resource != null)
            resource.close();
    }

    @Nonnull
    public String getName()
    {
        return name;
    }

    @Nonnull
    public InputStream getData()
    {
        return resource;
    }

    public void addPart(MultipartBody.Builder builder, int index)
    {
        builder.addFormDataPart("files[" + index + "]", name, IOUtil.createRequestBody(Requester.MEDIA_TYPE_OCTET, resource));
    }

    @Nonnull
    @Override
    public DataObject toAttachmentData(int index)
    {
        return DataObject.empty()
                .put("id", index)
                .put("filename", name);
    }
}
