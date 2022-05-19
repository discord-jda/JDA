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

import net.dv8tion.jda.api.exceptions.HttpException;
import net.dv8tion.jda.internal.requests.FunctionalCallback;
import net.dv8tion.jda.internal.requests.Requester;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.FutureUtil;
import net.dv8tion.jda.internal.utils.IOUtil;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * A utility class to download files.
 */
public class FileProxy
{
    private static volatile OkHttpClient defaultHttpClient;

    private final String url;
    private OkHttpClient customHttpClient;

    /**
     * Constructs a new {@link FileProxy} for the provided URL.
     *
     * @param url
     *        The URL to download from
     *
     * @throws IllegalArgumentException
     *         If the provided URL is null
     */
    public FileProxy(@Nonnull String url)
    {
        Checks.notNull(url, "URL");
        this.url = url;
    }

    /**
     * Sets the default OkHttpClient used by {@link FileProxy} and {@link ImageProxy}.
     * <br>This can still be overridden on a per-instance basis with {@link #withClient(OkHttpClient)}.
     *
     * @param  httpClient
     *         The default {@link OkHttpClient} to use while making HTTP requests
     *
     * @throws IllegalArgumentException
     *         If the provided {@link OkHttpClient} is null
     */
    public static void setDefaultHttpClient(@Nonnull OkHttpClient httpClient)
    {
        Checks.notNull(httpClient, "Default OkHttpClient");
        FileProxy.defaultHttpClient = httpClient;
    }

    /**
     * Returns the URL that has been passed to this proxy.
     * <br>This URL is always from Discord.
     *
     * @return The URL of the file.
     */
    @Nonnull
    public String getUrl()
    {
        return url;
    }

    /**
     * Sets the custom OkHttpClient used by this instance, regardless of if {@link #setDefaultHttpClient(OkHttpClient)} has been used or not.
     *
     * @param  customHttpClient
     *         The custom {@link OkHttpClient} to use while making HTTP requests
     *
     * @throws IllegalArgumentException
     *         If the provided {@link OkHttpClient} is null
     *
     * @return This proxy for chaining convenience.
     */
    @Nonnull
    public FileProxy withClient(@Nonnull OkHttpClient customHttpClient)
    {
        Checks.notNull(customHttpClient, "Custom HTTP client");
        this.customHttpClient = customHttpClient;
        return this;
    }


    // INTERNAL DOWNLOAD METHODS

    protected OkHttpClient getHttpClient()
    {
        // Return custom HTTP client if set
        if (customHttpClient != null)
            return customHttpClient;

        // Otherwise, see if a default one has been assigned
        //  If there is no client then create a default one
        if (defaultHttpClient == null)
        {
            synchronized (this)
            {
                if (defaultHttpClient == null)
                    defaultHttpClient = new OkHttpClient();
            }
        }

        return defaultHttpClient;
    }

    protected Request getRequest(String url)
    {
        return new Request.Builder()
                .url(url)
                .addHeader("user-agent", Requester.USER_AGENT)
                .addHeader("accept-encoding", "gzip, deflate")
                .build();
    }

    protected CompletableFuture<InputStream> download(String url)
    {
        // We need to apply a pattern of CompletableFuture as shown here https://discord.com/channels/125227483518861312/942488867167146005/942492134446088203
        // This CompletableFuture is going to be passed to the user / other proxy methods and must not be overridden with other "completion stages" (see CF#exceptionally return type)
        // This is done in order to make cancelling these downloads actually cancel all the tasks that depends on the previous ones.
        //    If we did not do this, CF#cancel would have only cancelled the *last* CompletableFuture, so the download would still have occurred for example
        // So since we return a completely different future, we need to use #complete / #completeExceptionally manually,
        //     i.e. When the underlying CompletableFuture (the actual download task) has completed in any state
        final DownloadTask downloadTask = downloadInternal(url);

        return FutureUtil.thenApplyCancellable(downloadTask.getFuture(), Function.identity(), downloadTask::cancelCall);
    }

    private DownloadTask downloadInternal(String url)
    {
        final CompletableFuture<InputStream> future = new CompletableFuture<>();

        final Request req = getRequest(url);
        final OkHttpClient httpClient = getHttpClient();
        final Call newCall = httpClient.newCall(req);

        newCall.enqueue(FunctionalCallback
                .onFailure((call, e) -> future.completeExceptionally(new UncheckedIOException(e)))
                .onSuccess((call, response) ->
                {
                    if (response.isSuccessful())
                    {
                        InputStream body = IOUtil.getBody(response);
                        if (!future.complete(body))
                            IOUtil.silentClose(response);
                    }
                    else
                    {
                        future.completeExceptionally(new HttpException(response.code() + ": " + response.message()));
                        IOUtil.silentClose(response);
                    }
                }).build());

        return new DownloadTask(newCall, future);
    }

    protected CompletableFuture<Path> downloadToPath(String url)
    {
        final HttpUrl parsedUrl = HttpUrl.parse(url);
        Checks.check(parsedUrl != null, "URL '" + url + "' is not valid");

        final List<String> segments = parsedUrl.pathSegments();
        final String fileName = segments.get(segments.size() - 1);

        //Download to a file named the same as the last segment of the URL
        return downloadToPath(Paths.get(fileName));
    }

    protected CompletableFuture<Path> downloadToPath(String url, Path path)
    {
        //Check if the parent path, the folder, exists
        Checks.check(Files.exists(path.getParent()), "Parent folder of the file '" + path.toAbsolutePath() + "' does not exist.");

        final DownloadTask downloadTask = downloadInternal(url);

        return FutureUtil.thenApplyCancellable(downloadTask.getFuture(), stream -> {
            try
            {
                //Temporary file follows this pattern: filename + random_number + ".part"
                // The random number is generated until a filename becomes valid, until no file with the same name exists in the tmp directory
                final Path tmpPath = Files.createTempFile(path.getFileName().toString(), ".part");
                //A user might use a file's presence as an indicator of something being successfully downloaded,
                //This might prevent a file from being partial, say, if the user shuts down its bot while it's downloading something
                //Meanwhile, the time window to "corrupt" a file is very small when moving it
                //This is why we copy the file into a temporary file and then move it.
                Files.copy(stream, tmpPath, StandardCopyOption.REPLACE_EXISTING);
                Files.move(tmpPath, path, StandardCopyOption.REPLACE_EXISTING);
                return path;
            }
            catch (IOException e)
            {
                throw new UncheckedIOException(e);
            }
            finally
            {
                IOUtil.silentClose(stream);
            }
        }, downloadTask::cancelCall);
    }


    //API DOWNLOAD METHOD

    /**
     * Retrieves the {@link InputStream} of this file
     *
     * @return {@link CompletableFuture} which holds an {@link InputStream}, the {@link InputStream} must be closed manually
     */
    @Nonnull
    public CompletableFuture<InputStream> download()
    {
        return download(url);
    }

    /**
     * Downloads the data of this file, and stores it in a file with the same name as the queried file name (this would be the last segment of the URL).
     *
     * <p><b>Implementation note:</b>
     *       The file is first downloaded into a temporary file, the file is then moved to its real destination when the download is complete.
     *
     * @return {@link CompletableFuture} which holds a {@link Path} which corresponds to the location the file has been downloaded.
     */
    @Nonnull
    public CompletableFuture<Path> downloadToPath()
    {
        return downloadToPath(url);
    }

    /**
     * Downloads the data of this file into the specified file.
     *
     * <p><b>Implementation note:</b>
     *       The file is first downloaded into a temporary file, the file is then moved to its real destination when the download is complete.
     *
     * @param  file
     *         The file in which to download the data
     *
     * @throws IllegalArgumentException
     *         If any of the follow checks are true
     *         <ul>
     *             <li>The target file is null</li>
     *             <li>The parent folder of the target file does not exist</li>
     *         </ul>
     *
     * @return {@link CompletableFuture} which holds a {@link File}, it is the same as the file passed in the parameters.
     */
    @Nonnull
    public CompletableFuture<File> downloadToFile(@Nonnull File file)
    {
        Checks.notNull(file, "File");

        final CompletableFuture<Path> downloadToPathFuture = downloadToPath(url, file.toPath());
        return FutureUtil.thenApplyCancellable(downloadToPathFuture, Path::toFile);
    }

    /**
     * Downloads the data of this file into the specified file.
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
     *         </ul>
     *
     * @return {@link CompletableFuture} which holds a {@link Path}, it is the same as the path passed in the parameters.
     */
    @Nonnull
    public CompletableFuture<Path> downloadToPath(@Nonnull Path path)
    {
        Checks.notNull(path, "Path");

        return downloadToPath(url, path);
    }

    protected static class DownloadTask
    {
        private final Call call;
        private final CompletableFuture<InputStream> future;

        public DownloadTask(Call call, CompletableFuture<InputStream> future)
        {
            this.call = call;
            this.future = future;
        }

        protected void cancelCall()
        {
            call.cancel();
        }

        protected CompletableFuture<InputStream> getFuture()
        {
            return future;
        }
    }
}
