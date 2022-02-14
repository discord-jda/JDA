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

//TODO docs
public class FileProxy
{
    private static volatile OkHttpClient defaultHttpClient;

    private final String url;
    private OkHttpClient customHttpClient;

    public FileProxy(@Nonnull String url)
    {
        Checks.notNull(url, "URL");

        this.url = url;
    }

    /**
     * Sets the default OkHttpClient used by {@link FileProxy} and {@link ImageProxy}
     * <br>This can still be overridden on a per-instance basis with {@link #withClient(OkHttpClient)}
     *
     * @param httpClient The default {@link OkHttpClient} to use while making HTTP requests
     */
    public static void setHttpClient(@Nonnull OkHttpClient httpClient)
    {
        Checks.notNull(httpClient, "Default OkHttpClient");

        FileProxy.defaultHttpClient = httpClient;
    }

    //TODO docs
    @Nonnull
    public String getUrl()
    {
        return url;
    }

    /**
     * Sets the custom OkHttpClient used by this instance, regardless of if {@link #setHttpClient(OkHttpClient)} has been used or not
     *
     * @param customHttpClient The custom {@link OkHttpClient} to use while making HTTP requests
     *
     * @return This proxy for chaining convenience
     */
    @Nonnull
    public FileProxy withClient(@Nonnull OkHttpClient customHttpClient)
    {
        Checks.notNull(customHttpClient, "Custom HTTP client");

        this.customHttpClient = customHttpClient;

        return this;
    }


    // INTERNAL DOWNLOAD METHODS

    protected OkHttpClient getHttpClient() {
        // Return custom HTTP client if set
        if (customHttpClient != null)
        {
            return customHttpClient;
        }

        // Otherwise, see if a default one has been assigned
        //  If there is no client then create a default one
        if (defaultHttpClient == null)
        {
            synchronized (this) {
                if (defaultHttpClient == null)
                {
                    defaultHttpClient = new OkHttpClient();
                }
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
        final CompletableFuture<InputStream> downloadFuture = new CompletableFuture<>();

        // We need to apply a pattern of CompletableFuture as shown here https://discord.com/channels/125227483518861312/942488867167146005/942492134446088203
        // This CompletableFuture is going to be passed to the user / other proxy methods and must not be overridden with other "completion stages" (see CF#exceptionally return type)
        // This is done in order to make cancelling these downloads actually cancel all the tasks that depends on the previous ones.
        //    If we did not do this, CF#cancel would have only cancelled the *last* CompletableFuture, so the download would still have occurred for example
        // So since we return a completely different future, we need to use #complete / #completeExceptionally manually,
        //     i.e. When the underlying CompletableFuture (the actual download task) has completed in any state
        final DownloadTask downloadTask = downloadInternal(url);

        downloadTask.getFuture()
                .thenAccept(downloadFuture::complete) //Pass data directly, no processing is required
                .exceptionally(throwable ->
                {
                    downloadFuture.completeExceptionally(throwable);

                    return null;
                });

        downloadFuture.whenComplete((p, throwable) ->
        {
            if (downloadFuture.isCancelled())
            {
                downloadTask.cancelCall();
            }
        });

        return downloadFuture;
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
        final HttpUrl parsedUrl = HttpUrl.parse(url); //TODO should we allow other schemes than http and https ?
        if (parsedUrl == null)
        {
            final CompletableFuture<Path> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("URL '" + url + "' is not valid"));

            return future;
        }

        final List<String> segments = parsedUrl.pathSegments();
        final String fileName = segments.get(segments.size() - 1);

        //Download to a file named the same as the last segment of the URL
        return downloadToPath(Paths.get(fileName));
    }

    protected CompletableFuture<Path> downloadToPath(String url, Path path) {
        final CompletableFuture<Path> downloadToPathFuture = new CompletableFuture<>();

        //Check if the parent path, the folder, exists
        if (Files.notExists(path.getParent()))
        {
            throw new IllegalArgumentException("Parent folder of the file '" + path.toAbsolutePath() + "' does not exist.");
        }

        final DownloadTask downloadTask = downloadInternal(url);

        downloadTask.getFuture().thenAccept(stream ->
        {
            try
            {
                final Path tmpPath = Files.createTempFile("image", null);

                Files.copy(stream, tmpPath, StandardCopyOption.REPLACE_EXISTING);

                Files.move(tmpPath, path, StandardCopyOption.REPLACE_EXISTING);

                downloadToPathFuture.complete(tmpPath);
            }
            catch (IOException e)
            {
                throw new UncheckedIOException(e);
            }
            finally
            {
                IOUtil.silentClose(stream);
            }
        }).exceptionally(throwable -> {
            downloadToPathFuture.completeExceptionally(throwable);

            return null;
        });

        downloadToPathFuture.whenComplete((p, throwable) -> {
            if (downloadToPathFuture.isCancelled())
            {
                downloadTask.cancelCall();
            }
        });

        return downloadToPathFuture;
    }


    //API DOWNLOAD METHOD

    //TODO docs
    @Nonnull
    public CompletableFuture<InputStream> download()
    {
        return download(url);
    }

    //TODO docs
    @Nonnull
    public CompletableFuture<File> downloadToFile(@Nonnull File file)
    {
        Checks.notNull(file, "File");

        return downloadToPath(url, file.toPath()).thenApply(Path::toFile);
    }

    //TODO docs
    @Nonnull
    public CompletableFuture<Path> downloadToPath()
    {
        return downloadToPath(url);
    }

    //TODO docs
    @Nonnull
    public CompletableFuture<Path> downloadToPath(@Nonnull Path path)
    {
        Checks.notNull(path, "Path");

        return downloadToPath(url, path);
    }

    protected static class DownloadTask {
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
