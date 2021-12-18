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

public class ImageProxy
{
    private final JDA jda;
    private final String url;

    private ImageProxy(JDA jda, String url)
    {
        this.jda = jda;
        this.url = url;
    }

    //TODO docs
    // maybe not used externally ?
    @Nonnull
    public static ImageProxy fromUrl(@Nonnull JDA jda, @Nonnull String url)
    {
        Checks.notNull(jda, "JDA");
        Checks.notNull(url, "url");

        return new ImageProxy(jda, url);
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

    protected Request getRequest()
    {
        return new Request.Builder()
                .url(getUrl())
                .addHeader("user-agent", Requester.USER_AGENT)
                .addHeader("accept-encoding", "gzip, deflate")
                .build();
    }

    //TODO docs
    @Nonnull
    public CompletableFuture<InputStream> download()
    {
        CompletableFuture<InputStream> future = new CompletableFuture<>();
        Request req = getRequest();
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

    //TODO docs
    @Nonnull
    public CompletableFuture<Long> downloadToPath()
    {
        final HttpUrl parsedUrl = HttpUrl.parse(getUrl());
        if (parsedUrl == null)
        {
            final CompletableFuture<Long> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("URL '" + url + "' is not valid"));

            return future;
        }

        final List<String> segments = parsedUrl.pathSegments();
        final String fileName = segments.get(segments.size() - 1);

        //Download to a file named the same as the last segment of the URL
        return downloadToPath(Paths.get(fileName));
    }

    //TODO docs
    @Nonnull
    public CompletableFuture<Long> downloadToPath(@Nonnull String first, @Nonnull String... more)
    {
        Checks.notNull(first, "First path component");
        Checks.noneNull(more, "Additional path components");

        return downloadToPath(Paths.get(first, more));
    }

    //TODO docs
    @Nonnull
    public CompletableFuture<Long> downloadToFile(@Nonnull File file)
    {
        Checks.notNull(file, "File");

        return downloadToPath(file.toPath());
    }

    //TODO docs
    @Nonnull
    public CompletableFuture<Long> downloadToPath(@Nonnull Path path)
    {
        Checks.notNull(path, "Path");

        return download().thenApplyAsync(stream ->
        {
            try
            {
                final Path tmpPath = Files.createTempFile("image", null);

                final long copiedBytes = Files.copy(stream, tmpPath, StandardCopyOption.REPLACE_EXISTING);

                Files.move(tmpPath, path, StandardCopyOption.REPLACE_EXISTING);

                return copiedBytes;
            }
            catch (IOException e)
            {
                throw new UncheckedIOException(e);
            }
            finally
            {
                IOUtil.silentClose(stream);
            }
        }, getJDA().getCallbackPool());
    }
}
