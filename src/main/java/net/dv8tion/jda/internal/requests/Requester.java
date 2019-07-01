/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.internal.requests;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.requests.ratelimit.BotRateLimiter;
import net.dv8tion.jda.internal.requests.ratelimit.ClientRateLimiter;
import net.dv8tion.jda.internal.utils.IOUtil;
import net.dv8tion.jda.internal.utils.JDALogger;
import net.dv8tion.jda.internal.utils.config.AuthorizationConfig;
import okhttp3.*;
import okhttp3.internal.http.HttpMethod;
import org.jetbrains.annotations.Async;
import org.slf4j.Logger;
import org.slf4j.MDC;

import javax.annotation.Nonnull;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipException;

public class Requester
{
    public static final Logger LOG = JDALogger.getLog(Requester.class);
    public static final String DISCORD_API_PREFIX = String.format("https://discordapp.com/api/v%d/", JDAInfo.DISCORD_REST_VERSION);
    public static final String USER_AGENT = "DiscordBot (" + JDAInfo.GITHUB + ", " + JDAInfo.VERSION + ")";
    public static final RequestBody EMPTY_BODY = RequestBody.create(null, new byte[0]);
    public static final MediaType MEDIA_TYPE_JSON  = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType MEDIA_TYPE_OCTET = MediaType.parse("application/octet-stream; charset=utf-8");

    protected final JDAImpl api;
    protected final AuthorizationConfig authConfig;
    private final RateLimiter rateLimiter;

    private final OkHttpClient httpClient;

    //when we actually set the shard info we can also set the mdc context map, before it makes no sense
    private boolean isContextReady = false;
    private ConcurrentMap<String, String> contextMap = null;

    private volatile boolean retryOnTimeout = false;

    public Requester(JDA api)
    {
        this(api, ((JDAImpl) api).getAuthorizationConfig());
    }

    public Requester(JDA api, AuthorizationConfig authConfig)
    {
        if (authConfig == null)
            throw new NullPointerException("Provided config was null!");

        this.authConfig = authConfig;
        this.api = (JDAImpl) api;
        if (authConfig.getAccountType() == AccountType.BOT)
            rateLimiter = new BotRateLimiter(this);
        else
            rateLimiter = new ClientRateLimiter(this);
        
        this.httpClient = this.api.getHttpClient();
    }

    public void setContextReady(boolean ready)
    {
        this.isContextReady = ready;
    }

    public void setContext()
    {
        if (!isContextReady)
            return;
        if (contextMap == null)
            contextMap = api.getContextMap();
        contextMap.forEach(MDC::put);
    }

    public JDAImpl getJDA()
    {
        return api;
    }

    public <T> void request(@Async.Schedule Request<T> apiRequest)
    {
        if (rateLimiter.isShutdown) 
            throw new IllegalStateException("The Requester has been shutdown! No new requests can be requested!");

        if (apiRequest.shouldQueue())
            rateLimiter.queueRequest(apiRequest);
        else
            execute(apiRequest, true);
    }

    private void attemptRequest(CompletableFuture<Long> task, okhttp3.Request request,
                                List<okhttp3.Response> responses, Set<String> rays,
                                Request apiRequest, String url,
                                boolean handleOnRatelimit, int attempt)
    {
        Route.CompiledRoute route = apiRequest.getRoute();
        okhttp3.Response lastResponse = responses.isEmpty() ? null : responses.get(responses.size() - 1);
        // If the request has been canceled via the Future, don't execute.
        if (apiRequest.isCanceled())
        {
            apiRequest.onFailure(new CancellationException("RestAction has been cancelled"));
            task.complete(null);
            return;
        }

        if (attempt >= 4)
        {
            //Epic failure from other end. Attempted 4 times.
            Response response = new Response(Objects.requireNonNull(lastResponse), -1, rays);
            apiRequest.handleResponse(response);
            task.complete(null);
            return;
        }
        Call call = httpClient.newCall(request);
        call.enqueue(new Callback()
        {
            @Override
            public void onFailure(@Nonnull Call call, @Nonnull IOException e)
            {
                if (e instanceof SocketException || e instanceof SocketTimeoutException)
                {
                    if (retryOnTimeout)
                    {
                        attemptRequest(task, request, responses, rays, apiRequest, url, true, 3);
                    }
                    else
                    {
                        LOG.error("Requester timed out while executing a request", e);
                        apiRequest.handleResponse(new Response(null, e, rays));
                        task.complete(null);
                    }
                    return;
                }
                LOG.error("There was an exception while executing a REST request", e); //This originally only printed on DEBUG in 2.x
                apiRequest.handleResponse(new Response(null, e, rays));
                task.complete(null);
            }

            @Override
            public void onResponse(@Nonnull Call call, @Nonnull okhttp3.Response response)
            {
                responses.add(response);
                String cfRay = response.header("CF-RAY");
                if (cfRay != null)
                    rays.add(cfRay);

                if (response.code() >= 500)
                {
                    LOG.debug("Requesting {} -> {} returned status {}... retrying (attempt {})",
                              route.getMethod(), url, response.code(), attempt);
                    attemptRequest(task, request, responses, rays, apiRequest, url, true, attempt + 1);
                    return;
                }

                Long retryAfter = rateLimiter.handleResponse(route, response);
                if (!rays.isEmpty())
                    LOG.debug("Received response with following cf-rays: {}", rays);

                if (retryAfter == null)
                    apiRequest.handleResponse(new Response(response, -1, rays));
                else if (handleOnRatelimit)
                    apiRequest.handleResponse(new Response(response, retryAfter, rays));

                task.complete(retryAfter);
            }
        });
    }

    public CompletableFuture<Long> execute(Request<?> apiRequest)
    {
        return execute(apiRequest, false);
    }

    /**
     * Used to execute a Request. Processes request related to provided bucket.
     *
     * @param  apiRequest
     *         The API request that needs to be sent
     * @param  handleOnRateLimit
     *         Whether to forward rate-limits, false if rate limit handling should take over
     *
     * @return Non-null if the request was ratelimited. Returns a Long containing retry_after milliseconds until
     *         the request can be made again. This could either be for the Per-Route ratelimit or the Global ratelimit.
     *         <br>Check if globalCooldown is {@code null} to determine if it was Per-Route or Global.
     */
    public CompletableFuture<Long> execute(Request<?> apiRequest, boolean handleOnRateLimit)
    {
        return execute(apiRequest, false, handleOnRateLimit);
    }

    public CompletableFuture<Long> execute(@Async.Execute Request<?> apiRequest, boolean retried, boolean handleOnRatelimit)
    {
        Route.CompiledRoute route = apiRequest.getRoute();
        Long retryAfter = rateLimiter.getRateLimit(route);
        if (retryAfter != null)
        {
            if (handleOnRatelimit)
                apiRequest.handleResponse(new Response(retryAfter, Collections.emptySet()));
            return CompletableFuture.completedFuture(retryAfter);
        }

        okhttp3.Request.Builder builder = new okhttp3.Request.Builder();

        String url = DISCORD_API_PREFIX + route.getCompiledRoute();
        builder.url(url);

        String method = apiRequest.getRoute().getMethod().toString();
        RequestBody body = apiRequest.getBody();

        if (body == null && HttpMethod.requiresRequestBody(method))
            body = EMPTY_BODY;

        builder.method(method, body)
               .header("user-agent", USER_AGENT)
               .header("accept-encoding", "gzip");

        //adding token to all requests to the discord api or cdn pages
        //we can check for startsWith(DISCORD_API_PREFIX) because the cdn endpoints don't need any kind of authorization
        if (url.startsWith(DISCORD_API_PREFIX))
            builder.header("authorization", authConfig.getToken());

        // Apply custom headers like X-Audit-Log-Reason
        // If customHeaders is null this does nothing
        if (apiRequest.getHeaders() != null)
        {
            for (Entry<String, String> header : apiRequest.getHeaders().entrySet())
                builder.addHeader(header.getKey(), header.getValue());
        }

        okhttp3.Request request = builder.build();

        Set<String> rays = new LinkedHashSet<>();
        List<okhttp3.Response> responses = new ArrayList<>(4);
        CompletableFuture<Long> task = new CompletableFuture<>();
        attemptRequest(task, request, responses, rays, apiRequest, url, handleOnRatelimit, 0);
        return task;
    }

    public OkHttpClient getHttpClient()
    {
        return this.httpClient;
    }

    public RateLimiter getRateLimiter()
    {
        return rateLimiter;
    }

    public void setRetryOnTimeout(boolean retryOnTimeout)
    {
        this.retryOnTimeout = retryOnTimeout;
    }

    public void shutdown()
    {
        rateLimiter.shutdown();
    }

    /**
     * Retrieves an {@link java.io.InputStream InputStream} for the provided {@link okhttp3.Response Response}.
     * <br>When the header for {@code content-encoding} is set with {@code gzip} this will wrap the body
     * in a {@link java.util.zip.GZIPInputStream GZIPInputStream} which decodes the data.
     *
     * <p>This is used to make usage of encoded responses more user-friendly in various parts of JDA.
     *
     * @param  response
     *         The not-null Response object
     *
     * @throws IOException
     *         If a GZIP format error has occurred or the compression method used is unsupported
     *
     * @return InputStream representing the body of this response
     */
    @SuppressWarnings("ConstantConditions") // methods here don't return null despite the annotations on them, read the docs
    public static InputStream getBody(okhttp3.Response response) throws IOException
    {
        String encoding = response.header("content-encoding", "");
        InputStream data = new BufferedInputStream(response.body().byteStream());
        data.mark(256);
        try
        {
            if (encoding.equalsIgnoreCase("gzip"))
                return new GZIPInputStream(data);
            else if (encoding.equalsIgnoreCase("deflate"))
                return new InflaterInputStream(data, new Inflater(true));
        }
        catch (ZipException ex)
        {
            data.reset(); // reset to get full content
            LOG.error("Failed to read gzip content for response. Headers: {}\nContent: '{}'",
                      response.headers(), JDALogger.getLazyString(() -> new String(IOUtil.readFully(data))), ex);
            return null;
        }
        return data;
    }
}
