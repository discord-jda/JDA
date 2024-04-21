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

import net.dv8tion.jda.api.exceptions.HttpException
import net.dv8tion.jda.api.requests.RestConfig
import net.dv8tion.jda.internal.requests.FunctionalCallback
import net.dv8tion.jda.internal.utils.*
import okhttp3.*
import okhttp3.OkHttpClient.Builder.build
import okhttp3.Request.Builder.addHeader
import okhttp3.Request.Builder.build
import okhttp3.Request.Builder.url
import java.io.*
import java.nio.file.*
import java.util.concurrent.CompletableFuture
import java.util.function.Function
import javax.annotation.Nonnull
import kotlin.concurrent.Volatile

/**
 * A utility class to download files.
 */
open class FileProxy(@Nonnull url: String?) {
    /**
     * Returns the URL that has been passed to this proxy.
     * <br></br>This URL is always from Discord.
     *
     * @return The URL of the file.
     */
    @get:Nonnull
    val url: String?
    private var customHttpClient: OkHttpClient? = null

    /**
     * Constructs a new [FileProxy] for the provided URL.
     *
     * @param url
     * The URL to download from
     *
     * @throws IllegalArgumentException
     * If the provided URL is null
     */
    init {
        Checks.notNull(url, "URL")
        this.url = url
    }

    /**
     * Sets the custom OkHttpClient used by this instance, regardless of if [.setDefaultHttpClient] has been used or not.
     *
     * @param  customHttpClient
     * The custom [OkHttpClient] to use while making HTTP requests
     *
     * @throws IllegalArgumentException
     * If the provided [OkHttpClient] is null
     *
     * @return This proxy for chaining convenience.
     */
    @Nonnull
    fun withClient(@Nonnull customHttpClient: OkHttpClient?): FileProxy {
        Checks.notNull(customHttpClient, "Custom HTTP client")
        this.customHttpClient = customHttpClient
        return this
    }

    protected val httpClient: OkHttpClient?
        // INTERNAL DOWNLOAD METHODS
        protected get() {
            // Return custom HTTP client if set
            if (customHttpClient != null) return customHttpClient

            // Otherwise, see if a default one has been assigned
            //  If there is no client then create a default one
            if (defaultHttpClient == null) {
                synchronized(this) { if (defaultHttpClient == null) defaultHttpClient = OkHttpClient() }
            }
            return defaultHttpClient
        }

    protected fun getRequest(url: String?): Request {
        return Builder()
            .url(url)
            .addHeader("user-agent", RestConfig.USER_AGENT)
            .addHeader("accept-encoding", "gzip, deflate")
            .build()
    }

    protected fun download(url: String?): CompletableFuture<InputStream> {
        // We need to apply a pattern of CompletableFuture as shown here https://discord.com/channels/125227483518861312/942488867167146005/942492134446088203
        // This CompletableFuture is going to be passed to the user / other proxy methods and must not be overridden with other "completion stages" (see CF#exceptionally return type)
        // This is done in order to make cancelling these downloads actually cancel all the tasks that depends on the previous ones.
        //    If we did not do this, CF#cancel would have only cancelled the *last* CompletableFuture, so the download would still have occurred for example
        // So since we return a completely different future, we need to use #complete / #completeExceptionally manually,
        //     i.e. When the underlying CompletableFuture (the actual download task) has completed in any state
        val downloadTask = downloadInternal(url)
        return FutureUtil.thenApplyCancellable(downloadTask.future, Function.identity()) { downloadTask.cancelCall() }
    }

    private fun downloadInternal(url: String?): DownloadTask {
        val future = CompletableFuture<InputStream>()
        val req = getRequest(url)
        val httpClient = httpClient
        val newCall = httpClient!!.newCall(req)
        newCall.enqueue(FunctionalCallback
            .onFailure { call: Call?, e: IOException? -> future.completeExceptionally(UncheckedIOException(e)) }
            .onSuccess { call: Call?, response: Response? ->
                if (response!!.isSuccessful) {
                    val body = IOUtil.getBody(response)
                    if (!future.complete(body)) IOUtil.silentClose(response)
                } else {
                    future.completeExceptionally(
                        HttpException(
                            response.code().toString() + ": " + response.message()
                        )
                    )
                    IOUtil.silentClose(response)
                }
            }.build()
        )
        return DownloadTask(newCall, future)
    }

    protected fun downloadToPath(url: String?): CompletableFuture<Path> {
        val parsedUrl: HttpUrl = parse.parse(url)
        Checks.check(parsedUrl != null, "URL '%s' is invalid", url)
        val segments = parsedUrl.pathSegments()
        val fileName = segments[segments.size - 1]

        //Download to a file named the same as the last segment of the URL
        return downloadToPath(Paths.get(fileName))
    }

    protected fun downloadToPath(url: String?, path: Path): CompletableFuture<Path> {
        //Turn this into an absolute path so we can check the parent folder
        val absolute = path.toAbsolutePath()
        //Check if the parent path, the folder, exists
        val parent = absolute.parent
        Checks.check(parent != null && Files.exists(parent), "Parent folder of the file '%s' does not exist.", absolute)
        if (Files.exists(absolute)) {
            Checks.check(Files.isRegularFile(absolute), "Path '%s' is not a regular file.", absolute)
            Checks.check(Files.isWritable(absolute), "File at '%s' is not writable.", absolute)
        }
        val downloadTask = downloadInternal(url)
        return FutureUtil.thenApplyCancellable(downloadTask.future, { stream: InputStream? ->
            try {
                //Temporary file follows this pattern: filename + random_number + ".part"
                // The random number is generated until a filename becomes valid, until no file with the same name exists in the tmp directory
                val tmpPath = Files.createTempFile(absolute.fileName.toString(), ".part")
                //A user might use a file's presence as an indicator of something being successfully downloaded,
                //This might prevent a file from being partial, say, if the user shuts down its bot while it's downloading something
                //Meanwhile, the time window to "corrupt" a file is very small when moving it
                //This is why we copy the file into a temporary file and then move it.
                Files.copy(stream, tmpPath, StandardCopyOption.REPLACE_EXISTING)
                Files.move(tmpPath, absolute, StandardCopyOption.REPLACE_EXISTING)
                return@thenApplyCancellable absolute
            } catch (e: IOException) {
                throw UncheckedIOException(e)
            } finally {
                IOUtil.silentClose(stream)
            }
        }) { downloadTask.cancelCall() }
    }
    //API DOWNLOAD METHOD
    /**
     * Retrieves the [InputStream] of this file
     *
     * @return [CompletableFuture] which holds an [InputStream], the [InputStream] must be closed manually
     */
    @Nonnull
    fun download(): CompletableFuture<InputStream> {
        return download(url)
    }

    /**
     * Downloads the data of this file, and stores it in a file with the same name as the queried file name (this would be the last segment of the URL).
     *
     *
     * **Implementation note:**
     * The file is first downloaded into a temporary file, the file is then moved to its real destination when the download is complete.
     *
     * @return [CompletableFuture] which holds a [Path] which corresponds to the location the file has been downloaded.
     */
    @Nonnull
    fun downloadToPath(): CompletableFuture<Path> {
        return downloadToPath(url)
    }

    /**
     * Downloads the data of this file into the specified file.
     *
     *
     * **Implementation note:**
     * The file is first downloaded into a temporary file, the file is then moved to its real destination when the download is complete.
     *
     * @param  file
     * The file in which to download the data
     *
     * @throws IllegalArgumentException
     * If any of the follow checks are true
     *
     *  * The target file is null
     *  * The parent folder of the target file does not exist
     *  * The target file exists and is not a [regular file][Files.isRegularFile]
     *  * The target file exists and is not [writable][Files.isWritable]
     *
     *
     * @return [CompletableFuture] which holds a [File], it is the same as the file passed in the parameters.
     */
    @Nonnull
    fun downloadToFile(@Nonnull file: File): CompletableFuture<File> {
        Checks.notNull(file, "File")
        val downloadToPathFuture = downloadToPath(url, file.toPath())
        return FutureUtil.thenApplyCancellable(downloadToPathFuture) { obj: Path -> obj.toFile() }
    }

    /**
     * Downloads the data of this file into the specified file.
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
     *
     *
     * @return [CompletableFuture] which holds a [Path], it is the same as the path passed in the parameters.
     */
    @Nonnull
    fun downloadToPath(@Nonnull path: Path): CompletableFuture<Path> {
        Checks.notNull(path, "Path")
        return downloadToPath(url, path)
    }

    protected class DownloadTask(private val call: Call, val future: CompletableFuture<InputStream>) {

        fun cancelCall() {
            call.cancel()
        }
    }

    companion object {
        @Volatile
        private var defaultHttpClient: OkHttpClient? = null

        /**
         * Sets the default OkHttpClient used by [FileProxy] and [ImageProxy].
         * <br></br>This can still be overridden on a per-instance basis with [.withClient].
         *
         * @param  httpClient
         * The default [OkHttpClient] to use while making HTTP requests
         *
         * @throws IllegalArgumentException
         * If the provided [OkHttpClient] is null
         */
        fun setDefaultHttpClient(@Nonnull httpClient: OkHttpClient?) {
            Checks.notNull(httpClient, "Default OkHttpClient")
            defaultHttpClient = httpClient
        }
    }
}
