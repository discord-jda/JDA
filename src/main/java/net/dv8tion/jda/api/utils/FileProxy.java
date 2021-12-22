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
import net.dv8tion.jda.api.exceptions.HttpException;
import net.dv8tion.jda.internal.requests.FunctionalCallback;
import net.dv8tion.jda.internal.requests.Requester;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.IOUtil;
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
    private final JDA jda;
    private final String url;

    public FileProxy(@Nonnull JDA jda, @Nonnull String url)
    {
        Checks.notNull(jda, "JDA");
        Checks.notNull(url, "URL");

        this.jda = jda;
        this.url = url;
    }

    //TODO docs
    @Nonnull
    public JDA getJDA()
    {
        return jda;
    }

    //TODO docs
    @Nonnull
    public String getUrl()
    {
        return url;
    }


    // INTERNAL DOWNLOAD METHODS

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
        CompletableFuture<InputStream> future = new CompletableFuture<>();
        Request req = getRequest(url);
        OkHttpClient httpClient = getJDA().getHttpClient();
        httpClient.newCall(req).enqueue(FunctionalCallback
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
        return future;
    }

    protected CompletableFuture<Path> downloadToPath(String url)
    {
        final HttpUrl parsedUrl = HttpUrl.parse(url);
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
        //Check if the parent path, the folder, exists
        if (Files.notExists(path.getParent()))
        {
            throw new IllegalArgumentException("Parent folder of the file '" + path.toAbsolutePath() + "' does not exist.");
        }


        return download(url).thenApply(stream ->
        {
            try
            {
                final Path tmpPath = Files.createTempFile("image", null);

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
        });
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
}
