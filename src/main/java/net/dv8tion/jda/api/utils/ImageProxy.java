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

import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.FutureUtil;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * A utility class to retrieve images.
 * <br>This supports downloading the images from the normal URL, as well as downloading the image with a specific size (width is the same as the height).
 */
public class ImageProxy extends FileProxy
{
    /**
     * Constructs a new {@link ImageProxy} for the provided URL.
     *
     * @param  url
     *         The URL to download the image from
     *
     * @throws IllegalArgumentException
     *         If the provided URL is null
     */
    public ImageProxy(@Nonnull String url)
    {
        super(url);
    }

    /**
     * Returns the image URL for the specified size.
     * <br>The size is a best-effort resize from Discord, with recommended size values as powers of 2 such as 1024 or 512.
     *
     * @param  size
     *         The size of the image
     *
     * @return URL of the image with the specified size
     */
    @Nonnull
    public String getUrl(int size)
    {
        Checks.positive(size, "Image size");

        return getUrl() + "?size=" + size;
    }

    /**
     * Retrieves the {@link InputStream} of this image at the specified size.
     * <br><b>The image may not be resized at any size, usually Discord only allows for a few powers of 2</b>, so numbers like 128, 256, 512..., 100 might also be a valid size.
     *
     * <p>If the image is not of a valid size, the CompletableFuture will hold an exception since the HTTP request would have returned a 404.
     *
     * @param  size
     *         The size of this image
     *
     * @return {@link CompletableFuture} which holds an {@link InputStream}, the {@link InputStream} must be closed manually.
     */
    @Nonnull
    public CompletableFuture<InputStream> download(int size)
    {
        return download(getUrl(size));
    }

    /**
     * Downloads the data of this image, at the specified size, and stores it in a file with the same name as the queried file name (this would be the last segment of the URL).
     * <br><b>The image may not be resized at any size, usually Discord only allows for a few powers of 2</b>, so numbers like 128, 256, 512..., 100 might also be a valid size.
     *
     * <p>If the image is not of a valid size, the CompletableFuture will hold an exception since the HTTP request would have returned a 404.
     *
     * <p><b>Implementation note:</b>
     *       The file is first downloaded into a temporary file, the file is then moved to its real destination when the download is complete.
     *
     * @param  size
     *         The width and height of this image, must be positive
     *
     * @throws IllegalArgumentException
     *         If any of the follow checks are true
     *         <ul>
     *             <li>The requested size is negative or 0</li>
     *             <li>The URL's scheme is neither http or https</li>
     *         </ul>
     *
     * @return {@link CompletableFuture} which holds a {@link Path} which corresponds to the location the file has been downloaded.
     */
    @Nonnull
    public CompletableFuture<Path> downloadToPath(int size)
    {
        return downloadToPath(getUrl(size));
    }

    /**
     * Downloads the data of this image, at the specified size, and stores it in the specified file.
     * <br><b>The image may not be resized at any size, usually Discord only allows for a few powers of 2</b>, so numbers like 128, 256, 512..., 100 might also be a valid size.
     *
     * <p>If the image is not of a valid size, the CompletableFuture will hold an exception since the HTTP request would have returned a 404.
     *
     * <p><b>Implementation note:</b>
     *       The file is first downloaded into a temporary file, the file is then moved to its real destination when the download is complete.
     *
     * @param  file
     *         The file in which to download the image
     *
     * @throws IllegalArgumentException
     *         If any of the follow checks are true
     *         <ul>
     *             <li>The target file is null</li>
     *             <li>The parent folder of the target file does not exist</li>
     *             <li>The requested size is negative or 0</li>
     *         </ul>
     *
     * @return {@link CompletableFuture} which holds a {@link File}, it is the same as the file passed in the parameters.
     */
    @Nonnull
    public CompletableFuture<File> downloadToFile(@Nonnull File file, int size)
    {
        Checks.notNull(file, "File");

        final CompletableFuture<Path> downloadToPathFuture = downloadToPath(getUrl(size), file.toPath());
        return FutureUtil.thenApplyCancellable(downloadToPathFuture, Path::toFile);
    }

    /**
     * Downloads the data of this image, at the specified size, and stores it in the specified file.
     * <br><b>The image may not be resized at any size, usually Discord only allows for a few powers of 2</b>, so numbers like 128, 256, 512..., 100 might also be a valid size.
     *
     * <p>If the image is not of a valid size, the CompletableFuture will hold an exception since the HTTP request would have returned a 404.
     *
     * <p><b>Implementation note:</b>
     *       The file is first downloaded into a temporary file, the file is then moved to its real destination when the download is complete.
     *       <br>The given path can also target filesystems such as a ZIP filesystem.
     *
     * @param  path
     *         The file in which to download the image
     *
     * @throws IllegalArgumentException
     *         If any of the follow checks are true
     *         <ul>
     *             <li>The target path is null</li>
     *             <li>The parent folder of the target path does not exist</li>
     *             <li>The requested size is negative or 0</li>
     *         </ul>
     *
     * @return {@link CompletableFuture} which holds a {@link Path}, it is the same as the path passed in the parameters.
     */
    @Nonnull
    public CompletableFuture<Path> downloadToPath(@Nonnull Path path, int size)
    {
        Checks.notNull(path, "Path");

        return downloadToPath(getUrl(size), path);
    }
}
