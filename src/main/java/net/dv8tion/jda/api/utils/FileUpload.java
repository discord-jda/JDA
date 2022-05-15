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
import java.io.*;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;

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
    private final String name;
    private boolean claimed = false;

    protected FileUpload(InputStream resource, String name)
    {
        this.resource = resource;
        this.name = name;
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
     * The {@link InputStream} representing the data to upload as a file.
     *
     * @return The {@link InputStream}
     */
    @Nonnull
    public InputStream getData()
    {
        return resource;
    }

    @Override
    public synchronized void claim()
    {
        if (claimed)
            throw new IllegalStateException("Instances of FileUpload can only be used once. Create a new instance with a new data source for each use.");
        claimed = true;
    }

    @Override
    public synchronized boolean isClaimed()
    {
        return claimed;
    }

    @Override
    public void addPart(@Nonnull MultipartBody.Builder builder, int index)
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

    @Override
    public void close() throws IOException
    {
        if (resource != null)
            resource.close();
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void finalize()
    {
        IOUtil.silentClose(resource);
    }

    @Override
    public String toString()
    {
        return "AttachedFile[Data]:" + name;
    }
}
