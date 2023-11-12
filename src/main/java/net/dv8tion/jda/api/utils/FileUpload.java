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
import net.dv8tion.jda.internal.utils.EntityString;
import net.dv8tion.jda.internal.utils.IOUtil;
import net.dv8tion.jda.internal.utils.requestbody.DataSupplierBody;
import net.dv8tion.jda.internal.utils.requestbody.TypedBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.Okio;
import okio.Source;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.function.Supplier;

/**
 * Represents a file that is intended to be uploaded to Discord for arbitrary requests.
 * <br>This is used to upload data to discord for various purposes.
 *
 * <p>The {@link InputStream} will be closed on consumption by the request.
 * You can use {@link #close()} to close the stream manually.
 */
public class FileUpload implements Closeable, AttachedFile
{
    private final InputStream resource;
    private final Supplier<? extends Source> resourceSupplier;
    private String name;
    private TypedBody<?> body;
    private String description;

    protected FileUpload(InputStream resource, String name)
    {
        this.resource = resource;
        this.resourceSupplier = null;
        this.name = name;
    }

    protected FileUpload(Supplier<? extends Source> resourceSupplier, String name)
    {
        this.resourceSupplier = resourceSupplier;
        this.resource = null;
        this.name = name;
    }

    /**
     * Creates a FileUpload that sources its data from the supplier.
     * <br>The supplier <em>must</em> return a new stream on every call.
     *
     * <p>The streams are expected to always be at the beginning, when they are taken from the supplier.
     * If the supplier returned the same stream instance, the reader would start at the wrong position when re-attempting a request.
     *
     * <p>When this supplier factory is used, {@link #getData()} will return a new instance on each call.
     * It is the responsibility of the caller to close that stream.
     *
     * @param  name
     *         The file name
     * @param  supplier
     *         The resource supplier, which returns a new stream on each call
     *
     * @throws IllegalArgumentException
     *         If null is provided or the name is blank
     *
     * @return {@link FileUpload}
     */
    @Nonnull
    public static FileUpload fromStreamSupplier(@Nonnull String name, @Nonnull Supplier<? extends InputStream> supplier)
    {
        Checks.notNull(supplier, "Supplier");
        return fromSourceSupplier(name, () -> Okio.source(supplier.get()));
    }

    /**
     * Creates a FileUpload that sources its data from the supplier.
     * <br>The supplier <em>must</em> return a new stream on every call.
     *
     * <p>The streams are expected to always be at the beginning, when they are taken from the supplier.
     * If the supplier returned the same stream instance, the reader would start at the wrong position when re-attempting a request.
     *
     * <p>When this supplier factory is used, {@link #getData()} will return a new instance on each call.
     * It is the responsibility of the caller to close that stream.
     *
     * @param  name
     *         The file name
     * @param  supplier
     *         The resource supplier, which returns a new {@link Source} on each call
     *
     * @throws IllegalArgumentException
     *         If null is provided or the name is blank
     *
     * @return {@link FileUpload}
     */
    @Nonnull
    public static FileUpload fromSourceSupplier(@Nonnull String name, @Nonnull Supplier<? extends Source> supplier)
    {
        Checks.notNull(supplier, "Supplier");
        Checks.notBlank(name, "Name");
        return new FileUpload(supplier, name);
    }

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
    public static FileUpload fromData(@Nonnull InputStream data, @Nonnull String name)
    {
        Checks.notNull(data, "Data");
        Checks.notBlank(name, "Name");
        return new FileUpload(data, name);
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
    public static FileUpload fromData(@Nonnull byte[] data, @Nonnull String name)
    {
        Checks.notNull(data, "Data");
        Checks.notNull(name, "Name");
        return fromData(new ByteArrayInputStream(data), name);
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
    public static FileUpload fromData(@Nonnull File file, @Nonnull String name)
    {
        Checks.notNull(file, "File");
        try
        {
            return fromData(new FileInputStream(file), name);
        }
        catch (FileNotFoundException e)
        {
            throw new UncheckedIOException(e);
        }
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
    public static FileUpload fromData(@Nonnull File file)
    {
        Checks.notNull(file, "File");
        try
        {
            return fromData(new FileInputStream(file), file.getName());
        }
        catch (FileNotFoundException e)
        {
            throw new UncheckedIOException(e);
        }
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
    public static FileUpload fromData(@Nonnull Path path, @Nonnull String name, @Nonnull OpenOption... options)
    {
        Checks.notNull(path, "Path");
        Checks.noneNull(options, "Options");
        Checks.check(Files.isReadable(path), "File for specified path cannot be read. Path: %s", path);
        try
        {
            return fromData(Files.newInputStream(path, options), name);
        }
        catch (IOException e)
        {
            throw new UncheckedIOException("Could not open file for specified path. Path: " + path, e);
        }
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
    public static FileUpload fromData(@Nonnull Path path, @Nonnull OpenOption... options)
    {
        Checks.notNull(path, "Path");
        Path fileName = path.getFileName();
        Checks.check(fileName != null, "Path does not have a file name. Path: %s", path);
        return fromData(path, fileName.toString(), options);
    }

    /**
     * Changes the name of this file, to be prefixed as {@code SPOILER_}.
     * <br>This will cause the file to be rendered as a spoiler attachment in the client.
     *
     * @return The updated FileUpload instance
     */
    @Nonnull
    public FileUpload asSpoiler()
    {
        if (name.startsWith("SPOILER_"))
            return this;
        return setName("SPOILER_" + name);
    }

    /**
     * Changes the name of this file.
     *
     * @param  name
     *         The new filename
     *
     * @throws IllegalArgumentException
     *         If the name is null, blank, or empty
     *
     * @return The updated FileUpload instance
     */
    @Nonnull
    public FileUpload setName(@Nonnull String name)
    {
        Checks.notBlank(name, "Name");
        this.name = name;
        return this;
    }

    /**
     * Set the file description used as ALT text for screenreaders.
     *
     * @param  description
     *         The alt text describing this file attachment (up to {@value MAX_DESCRIPTION_LENGTH} characters)
     *
     * @throws IllegalArgumentException
     *         If the description is longer than {@value MAX_DESCRIPTION_LENGTH} characters
     *
     * @return The same FileUpload instance with the new description
     */
    @Nonnull
    public FileUpload setDescription(@Nullable String description)
    {
        if (description != null)
            Checks.notLonger(description = description.trim(), MAX_DESCRIPTION_LENGTH, "Description");
        this.description = description;
        return this;
    }

    /**
     * The filename for the file.
     *
     * @return The filename
     */
    @Nonnull
    public String getName()
    {
        return name;
    }

    /**
     * The description for the file.
     *
     * @return The description
     */
    @Nullable
    public String getDescription()
    {
        return description;
    }

    /**
     * The {@link InputStream} representing the data to upload as a file.
     *
     * @return The {@link InputStream}
     */
    @Nonnull
    public InputStream getData()
    {
        if (resource != null)
            return resource;
        else
            return Okio.buffer(resourceSupplier.get()).inputStream();
    }

    /**
     * Creates a re-usable instance of {@link RequestBody} with the specified content-type.
     *
     * <p>This body will automatically close the {@link #getData() resource} when the request is done.
     * However, since the body buffers the data, it can be used multiple times regardless.
     *
     * @param  type
     *         The content-type to use for the body (e.g. {@code "application/octet-stream"})
     *
     * @throws IllegalArgumentException
     *         If the content-type is null
     *
     * @return {@link RequestBody}
     */
    @Nonnull
    public synchronized RequestBody getRequestBody(@Nonnull MediaType type)
    {
        Checks.notNull(type, "Type");
        if (body != null) // This allows FileUpload to be used more than once!
            return body.withType(type);

        if (resource == null)
            return body = new DataSupplierBody(type, resourceSupplier);
        else
            return body = IOUtil.createRequestBody(type, resource);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public synchronized void addPart(@Nonnull MultipartBody.Builder builder, int index)
    {
        builder.addFormDataPart("files[" + index + "]", name, getRequestBody(Requester.MEDIA_TYPE_OCTET));
    }

    @Nonnull
    @Override
    public DataObject toAttachmentData(int index)
    {
        return DataObject.empty()
                .put("id", index)
                .put("description", description == null ? "" : description)
                .put("filename", name);
    }

    @Override
    public synchronized void close() throws IOException
    {
        if (body == null)
            forceClose();
    }

    @Override
    public void forceClose() throws IOException
    {
        if (resource != null)
            resource.close();
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void finalize()
    {
        if (body == null && resource != null) // Only close if the resource was never used
            IOUtil.silentClose(resource);
    }

    @Override
    public String toString()
    {
        return new EntityString("AttachedFile")
                .setType("Data")
                .setName(name)
                .toString();
    }
}
