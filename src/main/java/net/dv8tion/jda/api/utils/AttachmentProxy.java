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

import javax.annotation.Nonnull;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * A utility class to retrieve attachments coming from Discord
 * <br>This supports downloading the images from the normal URL, as well as downloading the image with a specific width and height
 */
public class AttachmentProxy extends FileProxy
{
    public AttachmentProxy(@Nonnull String url)
    {
        super(url);
    }

    @Nonnull
    private String getUrl(int width, int height)
    {
        return getUrl() + "?width=" + width + "&height=" + height;
    }

    /**
     * Retrieves the {@link InputStream} of this attachment at the specified width and height
     * <br>The attachment, if an image, may be resized at any size, however if the size does not fit the ratio of the image, then it will be cropped as to fit the target size
     * <br>If the attachment is not an image then the size parameters are ignored and the file is downloaded
     *
     * @param  width
     *         The width of this image, must be positive
     * @param  height
     *         The height of this image, must be positive
     *
     * @throws IllegalArgumentException
     *         If any of the follow checks are true
     *         <ul>
     *             <li>The requested width is negative or 0</li>
     *             <li>The requested height is negative or 0</li>
     *         </ul>
     *
     * @return a {@link CompletableFuture} which would return an {@link InputStream}
     */
    @Nonnull
    public CompletableFuture<InputStream> download(int width, int height)
    {
        Checks.positive(width, "Image width");
        Checks.positive(height, "Image height");

        return download(getUrl(width, height));
    }

    /**
     * Retrieves the data of this attachment, at the specified width and height, and stores it in a file with the same name as the queried file name (this would be the last segment of the URL)
     * <br>The attachment, if an image, may be resized at any size, however if the size does not fit the ratio of the image, then it will be cropped as to fit the target size
     * <br>If the attachment is not an image then the size parameters are ignored and the file is downloaded
     *
     * @param   width
     *          The width of this image, must be positive
     * @param   height
     *          The height of this image, must be positive
     *
     * @throws  IllegalArgumentException
     *          If any of the follow checks are true
     *          <ul>
     *              <li>The requested width is negative or 0</li>
     *              <li>The requested height is negative or 0</li>
     *          </ul>
     *
     * @return a {@link CompletableFuture} which would return a {@link Path} which corresponds to the location the file has been downloaded
     *
     * @implNote
     *         The file is first downloaded into a temporary file, the file is then moved to its real destination when the download is complete
     */
    @Nonnull
    public CompletableFuture<Path> downloadToPath(int width, int height)
    {
        Checks.positive(width, "Image width");
        Checks.positive(height, "Image height");

        return downloadToPath(getUrl(width, height));
    }

    /**
     * Retrieves the data of this image, at the specified width and height, and stores it in the specified file
     * <br>The attachment, if an image, may be resized at any size, however if the size does not fit the ratio of the image, then it will be cropped as to fit the target size
     * <br>If the attachment is not an image then the size parameters are ignored and the file is downloaded
     *
     * @param  file
     *         The file in which to download the image
     * @param  width
     *         The width of this image, must be positive
     * @param  height
     *         The height of this image, must be positive
     *
     * @throws IllegalArgumentException
     *         If any of the follow checks are true
     *         <ul>
     *             <li>The requested width is negative or 0</li>
     *             <li>The requested height is negative or 0</li>
     *         </ul>
     *
     * @return a {@link CompletableFuture} which would return a {@link File}, it is the same as the file passed in the parameters
     *
     * @implNote
     *         The file is first downloaded into a temporary file, the file is then moved to its real destination when the download is complete
     */
    @Nonnull
    public CompletableFuture<File> downloadToFile(@Nonnull File file, int width, int height)
    {
        Checks.notNull(file, "File");
        Checks.positive(width, "Image width");
        Checks.positive(height, "Image height");

        return downloadToPath(getUrl(width, height), file.toPath()).thenApply(Path::toFile);
    }

    /**
     * Retrieves the data of this image, at the specified size, and stores it in the specified file
     * <br>The attachment, if an image, may be resized at any size, however if the size does not fit the ratio of the image, then it will be cropped as to fit the target size
     * <br>If the attachment is not an image then the size parameters are ignored and the file is downloaded
     *
     * @param  path
     *         The file in which to download the image
     * @param  width
     *         The width of this image, must be positive
     * @param  height
     *         The height of this image, must be positive
     *
     * @return a {@link CompletableFuture} which would return a {@link Path}, it is the same as the file passed in the parameters
     *
     * @throws IllegalArgumentException
     *         If any of the follow checks are true
     *         <ul>
     *             <li>The requested width is negative or 0</li>
     *             <li>The requested height is negative or 0</li>
     *         </ul>
     *
     * @implNote
     *         The file is first downloaded into a temporary file, the file is then moved to its real destination when the download is complete.
     *         <br>The given path can also target filesystems such as a ZIP filesystem
     */
    @Nonnull
    public CompletableFuture<Path> downloadToPath(@Nonnull Path path, int width, int height)
    {
        Checks.notNull(path, "Path");
        Checks.positive(width, "Image width");
        Checks.positive(height, "Image height");

        return downloadToPath(getUrl(width, height), path);
    }
}
