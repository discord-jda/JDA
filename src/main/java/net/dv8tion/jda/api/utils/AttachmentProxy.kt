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

import net.dv8tion.jda.api.entities.Icon
import net.dv8tion.jda.api.entities.Icon.Companion.from
import net.dv8tion.jda.internal.utils.*
import java.io.*
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import javax.annotation.Nonnull

/**
 * A utility class to retrieve attachments.
 * <br></br>This supports downloading the images from the normal URL, as well as downloading the image with a specific width and height.
 */
class AttachmentProxy
/**
 * Constructs a new [AttachmentProxy] for the provided URL.
 *
 * @param  url
 * The URL to download the attachment from
 *
 * @throws IllegalArgumentException
 * If the provided URL is null
 */
    (@Nonnull url: String?) : FileProxy(url) {
    /**
     * Returns the attachment URL for the specified width and height.
     * <br></br>The width and height is a best-effort resize from Discord.
     *
     * @param  width
     * The width of the image
     * @param  height
     * The height of the image
     *
     * @return URL of the attachment with the specified width and height
     */
    @Nonnull
    fun getUrl(width: Int, height: Int): String {
        Checks.positive(width, "Image width")
        Checks.positive(height, "Image height")
        return IOUtil.addQuery(url, "width", width, "height", height)
    }

    /**
     * Retrieves the [InputStream] of this attachment at the specified width and height.
     * <br></br>The attachment, if an image, may be resized at any size, however if the size does not fit the ratio of the image, then it will be cropped as to fit the target size.
     * <br></br>If the attachment is not an image then the size parameters are ignored and the file is downloaded.
     *
     * @param  width
     * The width of this image, must be positive
     * @param  height
     * The height of this image, must be positive
     *
     * @throws IllegalArgumentException
     * If any of the follow checks are true
     *
     *  * The requested width is negative or 0
     *  * The requested height is negative or 0
     *
     *
     * @return [CompletableFuture] which holds an [InputStream], the [InputStream] must be closed manually.
     */
    @Nonnull
    fun download(width: Int, height: Int): CompletableFuture<InputStream?>? {
        return download(getUrl(width, height))
    }

    /**
     * Downloads the data of this attachment, at the specified width and height, and stores it in a file with the same name as the queried file name (this would be the last segment of the URL).
     * <br></br>The attachment, if an image, may be resized at any size, however if the size does not fit the ratio of the image, then it will be cropped as to fit the target size.
     * <br></br>If the attachment is not an image then the size parameters are ignored and the file is downloaded.
     *
     *
     * **Implementation note:**
     * The file is first downloaded into a temporary file, the file is then moved to its real destination when the download is complete.
     *
     * @param  width
     * The width of this image, must be positive
     * @param  height
     * The height of this image, must be positive
     *
     * @throws IllegalArgumentException
     * If any of the follow checks are true
     *
     *  * The requested width is negative or 0
     *  * The requested height is negative or 0
     *  * The URL's scheme is neither http or https
     *
     *
     * @return [CompletableFuture] which holds a [Path] which corresponds to the location the file has been downloaded.
     */
    @Nonnull
    fun downloadToPath(width: Int, height: Int): CompletableFuture<Path?>? {
        return downloadToPath(getUrl(width, height))
    }

    /**
     * Downloads the data of this attachment, at the specified width and height, and stores it in the specified file.
     * <br></br>The attachment, if an image, may be resized at any size, however if the size does not fit the ratio of the image, then it will be cropped as to fit the target size.
     * <br></br>If the attachment is not an image then the size parameters are ignored and the file is downloaded.
     *
     *
     * **Implementation note:**
     * The file is first downloaded into a temporary file, the file is then moved to its real destination when the download is complete.
     *
     * @param  file
     * The file in which to download the image
     * @param  width
     * The width of this image, must be positive
     * @param  height
     * The height of this image, must be positive
     *
     * @throws IllegalArgumentException
     * If any of the follow checks are true
     *
     *  * The target file is null
     *  * The parent folder of the target file does not exist
     *  * The target file exists and is not a [regular file][Files.isRegularFile]
     *  * The target file exists and is not [writable][Files.isWritable]
     *  * The requested width is negative or 0
     *  * The requested height is negative or 0
     *
     *
     * @return [CompletableFuture] which holds a [File], it is the same as the file passed in the parameters.
     */
    @Nonnull
    fun downloadToFile(@Nonnull file: File, width: Int, height: Int): CompletableFuture<File> {
        Checks.notNull(file, "File")
        val downloadToPathFuture = downloadToPath(getUrl(width, height), file.toPath())
        return FutureUtil.thenApplyCancellable(downloadToPathFuture) { obj: Path? -> obj!!.toFile() }
    }

    /**
     * Downloads the data of this attachment, at the specified size, and stores it in the specified file.
     * <br></br>The attachment, if an image, may be resized at any size, however if the size does not fit the ratio of the image, then it will be cropped as to fit the target size.
     * <br></br>If the attachment is not an image then the size parameters are ignored and the file is downloaded.
     *
     *
     * **Implementation note:**
     * The file is first downloaded into a temporary file, the file is then moved to its real destination when the download is complete.
     * <br></br>The given path can also target filesystems such as a ZIP filesystem.
     *
     * @param  path
     * The file in which to download the image
     * @param  width
     * The width of this image, must be positive
     * @param  height
     * The height of this image, must be positive
     *
     * @throws IllegalArgumentException
     * If any of the follow checks are true
     *
     *  * The target path is null
     *  * The parent folder of the target path does not exist
     *  * The target path exists and is not a [regular file][Files.isRegularFile]
     *  * The target path exists and is not [writable][Files.isWritable]
     *  * The requested width is negative or 0
     *  * The requested height is negative or 0
     *
     *
     * @return [CompletableFuture] which holds a [Path], it is the same as the path passed in the parameters.
     */
    @Nonnull
    fun downloadToPath(@Nonnull path: Path?, width: Int, height: Int): CompletableFuture<Path?>? {
        Checks.notNull(path, "Path")
        return downloadToPath(getUrl(width, height), path!!)
    }

    @Nonnull
    private fun downloadAsIcon(url: String?): CompletableFuture<Icon> {
        val downloadFuture = download(url)
        return FutureUtil.thenApplyCancellable(downloadFuture) { stream: InputStream? ->
            try {
                stream.use { ignored -> return@thenApplyCancellable from(stream) }
            } catch (e: IOException) {
                throw UncheckedIOException(e)
            }
        }
    }

    /**
     * Downloads the data of this attachment, and constructs an [Icon] from the data.
     *
     * @return [CompletableFuture] which holds an [Icon].
     */
    @Nonnull
    fun downloadAsIcon(): CompletableFuture<Icon> {
        return downloadAsIcon(url)
    }

    /**
     * Downloads the data of this attachment, at the specified size, and constructs an [Icon] from the data.
     * <br></br>The attachment, if an image, may be resized at any size, however if the size does not fit the ratio of the image, then it will be cropped as to fit the target size.
     * <br></br>If the attachment is not an image then the size parameters are ignored and the file is downloaded.
     *
     * @param  width
     * The width of this image, must be positive
     * @param  height
     * The height of this image, must be positive
     *
     * @throws IllegalArgumentException
     * If any of the follow checks are true
     *
     *  * The requested width is negative or 0
     *  * The requested height is negative or 0
     *
     *
     * @return [CompletableFuture] which holds an [Icon].
     */
    @Nonnull
    fun downloadAsIcon(width: Int, height: Int): CompletableFuture<Icon> {
        return downloadAsIcon(getUrl(width, height))
    }
}
