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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

//TODO docs
public class ImageProxy extends FileProxy
{
    private final String id;
    private final String extension;

    public ImageProxy(JDA jda, String url, String id, String extension)
    {
        super(jda, url);

        Checks.notNull(id, "ID");
        Checks.notNull(extension, "Extension");

        this.id = id;
        this.extension = extension;
    }

    //TODO docs
    @Nonnull
    public String getExtension()
    {
        return extension;
    }

    //TODO docs
    @Nonnull
    public String getId()
    {
        return id;
    }

    @Nonnull
    private String getUrl(int size)
    {
        return getUrl() + "?size=" + size;
    }

    //TODO docs
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