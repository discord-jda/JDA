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
public class ImageProxy extends FileProxy
{
    public ImageProxy(@Nonnull String url)
    {
        super(url);
    }

    @Nonnull
    private String getUrl(int size)
    {
        return getUrl() + "?size=" + size;
    }

    /**
     * Retrieves the {@link InputStream} of this image at the specified size
     * <br><b>The image may not be resized at any size, usually Discord only allows for a few powers of 2</b>, so numbers like 128, 256, 512..., 100 might also be a valid size
     * <p>If the image is not of a valid size, the CompletableFuture will hold an exception since the HTTP request would have returned a 404
     *
     * @param size The size of this image
     *
     * @return a {@link CompletableFuture} which would return an {@link InputStream}
     */
    @Nonnull
    public CompletableFuture<InputStream> download(int size)
    {
        Checks.positive(size, "Image size");

        return download(getUrl(size));
    }

    //TODO docs
    @Nonnull
    public CompletableFuture<Path> downloadToPath(int size)
    {
        Checks.positive(size, "Image size");

        return downloadToPath(getUrl(size));
    }

    //TODO docs
    @Nonnull
    public CompletableFuture<File> downloadToFile(@Nonnull File file, int size)
    {
        Checks.notNull(file, "File");
        Checks.positive(size, "Image size");

        return downloadToPath(getUrl(size), file.toPath()).thenApply(Path::toFile);
    }

    //TODO docs
    @Nonnull
    public CompletableFuture<Path> downloadToPath(@Nonnull Path path, int size)
    {
        Checks.notNull(path, "Path");
        Checks.positive(size, "Image size");

        return downloadToPath(getUrl(size), path);
    }
}
