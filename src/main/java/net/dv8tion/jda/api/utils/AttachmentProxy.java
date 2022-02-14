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

//TODO docs
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
     * Retrieves the {@link InputStream} of this image at the specified size
     * <br>The image may be resized at any size, however if the size does not fit the ratio of the image, then it will be cropped as to fit the target size
     *
     * @param width The width of this image
     * @param height The height of this image
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

    //TODO docs
    @Nonnull
    public CompletableFuture<Path> downloadToPath(int width, int height)
    {
        Checks.positive(width, "Image width");
        Checks.positive(height, "Image height");

        return downloadToPath(getUrl(width, height));
    }

    //TODO docs
    @Nonnull
    public CompletableFuture<File> downloadToFile(@Nonnull File file, int width, int height)
    {
        Checks.notNull(file, "File");
        Checks.positive(width, "Image width");
        Checks.positive(height, "Image height");

        return downloadToPath(getUrl(width, height), file.toPath()).thenApply(Path::toFile);
    }

    //TODO docs
    @Nonnull
    public CompletableFuture<Path> downloadToPath(@Nonnull Path path, int width, int height)
    {
        Checks.notNull(path, "Path");
        Checks.positive(width, "Image width");
        Checks.positive(height, "Image height");

        return downloadToPath(getUrl(width, height), path);
    }
}
