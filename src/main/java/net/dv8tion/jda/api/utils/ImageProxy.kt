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
package net.dv8tion.jda.api.utils

import net.dv8tion.jda.internal.utils.*
import java.io.*
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import javax.annotation.Nonnull

/**
 * A utility class to retrieve images.
 * <br></br>This supports downloading the images from the normal URL, as well as downloading the image with a specific size (width is the same as the height).
 */
class ImageProxy
/**
 * Constructs a new [ImageProxy] for the provided URL.
 *
 * @param  url
 * The URL to download the image from
 *
 * @throws IllegalArgumentException
 * If the provided URL is null
 */
    (@Nonnull url: String?) : FileProxy(url) {
    /**
     * Returns the image URL for the specified size.
     * <br></br>The size is a best-effort resize from Discord, with recommended size values as powers of 2 such as 1024 or 512.
     *
     * @param  size
     * The size of the image
     *
     * @return URL of the image with the specified size
     */
    @Nonnull
    fun getUrl(size: Int): String {
        Checks.positive(size, "Image size")
        return IOUtil.addQuery(url, "size", size)
    }

    /**
     * Retrieves the [InputStream] of this image at the specified size.
     * <br></br>**The image may not be resized at any size, usually Discord only allows for a few powers of 2**, so numbers like 128, 256, 512..., 100 might also be a valid size.
     *
     *
     * If the image is not of a valid size, the CompletableFuture will hold an exception since the HTTP request would have returned a 404.
     *
     * @param  size
     * The size of this image
     *
     * @return [CompletableFuture] which holds an [InputStream], the [InputStream] must be closed manually.
     */
    @Nonnull
    fun download(size: Int): CompletableFuture<InputStream?>? {
        return download(getUrl(size))
    }

    /**
     * Downloads the data of this image, at the specified size, and stores it in a file with the same name as the queried file name (this would be the last segment of the URL).
     * <br></br>**The image may not be resized at any size, usually Discord only allows for a few powers of 2**, so numbers like 128, 256, 512..., 100 might also be a valid size.
     *
     *
     * If the image is not of a valid size, the CompletableFuture will hold an exception since the HTTP request would have returned a 404.
     *
     *
     * **Implementation note:**
     * The file is first downloaded into a temporary file, the file is then moved to its real destination when the download is complete.
     *
     * @param  size
     * The width and height of this image, must be positive
     *
     * @throws IllegalArgumentException
     * If any of the follow checks are true
     *
     *  * The requested size is negative or 0
     *  * The URL's scheme is neither http or https
     *
     *
     * @return [CompletableFuture] which holds a [Path] which corresponds to the location the file has been downloaded.
     */
    @Nonnull
    fun downloadToPath(size: Int): CompletableFuture<Path?>? {
        return downloadToPath(getUrl(size))
    }

    /**
     * Downloads the data of this image, at the specified size, and stores it in the specified file.
     * <br></br>**The image may not be resized at any size, usually Discord only allows for a few powers of 2**, so numbers like 128, 256, 512..., 100 might also be a valid size.
     *
     *
     * If the image is not of a valid size, the CompletableFuture will hold an exception since the HTTP request would have returned a 404.
     *
     *
     * **Implementation note:**
     * The file is first downloaded into a temporary file, the file is then moved to its real destination when the download is complete.
     *
     * @param  file
     * The file in which to download the image
     *
     * @throws IllegalArgumentException
     * If any of the follow checks are true
     *
     *  * The target file is null
     *  * The parent folder of the target file does not exist
     *  * The target file exists and is not a [regular file][Files.isRegularFile]
     *  * The target file exists and is not [writable][Files.isWritable]
     *  * The requested size is negative or 0
     *
     *
     * @return [CompletableFuture] which holds a [File], it is the same as the file passed in the parameters.
     */
    @Nonnull
    fun downloadToFile(@Nonnull file: File, size: Int): CompletableFuture<File> {
        Checks.notNull(file, "File")
        val downloadToPathFuture = downloadToPath(getUrl(size), file.toPath())
        return FutureUtil.thenApplyCancellable(downloadToPathFuture) { obj: Path? -> obj!!.toFile() }
    }

    /**
     * Downloads the data of this image, at the specified size, and stores it in the specified file.
     * <br></br>**The image may not be resized at any size, usually Discord only allows for a few powers of 2**, so numbers like 128, 256, 512..., 100 might also be a valid size.
     *
     *
     * If the image is not of a valid size, the CompletableFuture will hold an exception since the HTTP request would have returned a 404.
     *
     *
     * **Implementation note:**
     * The file is first downloaded into a temporary file, the file is then moved to its real destination when the download is complete.
     * <br></br>The given path can also target filesystems such as a ZIP filesystem.
     *
     * @param  path
     * The file in which to download the image
     *
     * @throws IllegalArgumentException
     * If any of the follow checks are true
     *
     *  * The target path is null
     *  * The parent folder of the target path does not exist
     *  * The target path exists and is not a [regular file][Files.isRegularFile]
     *  * The target path exists and is not [writable][Files.isWritable]
     *  * The requested size is negative or 0
     *
     *
     * @return [CompletableFuture] which holds a [Path], it is the same as the path passed in the parameters.
     */
    @Nonnull
    fun downloadToPath(@Nonnull path: Path?, size: Int): CompletableFuture<Path?>? {
        Checks.notNull(path, "Path")
        return downloadToPath(getUrl(size), path!!)
    }
}
