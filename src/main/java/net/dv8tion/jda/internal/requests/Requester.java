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

package net.dv8tion.jda.internal.requests;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.requests.*;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.utils.IOUtil;
import net.dv8tion.jda.internal.utils.JDALogger;
import net.dv8tion.jda.internal.utils.config.AuthorizationConfig;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.internal.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.MDC;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.ssl.SSLPeerUnverifiedException;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Consumer;

public class Requester
{
    public static final Logger LOG = JDALogger.getLog(Requester.class);
    @SuppressWarnings("deprecation")
    public static final RequestBody EMPTY_BODY = RequestBody.create(null, new byte[0]);
    public static final MediaType MEDIA_TYPE_JSON  = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType MEDIA_TYPE_OCTET = MediaType.parse("application/octet-stream; charset=utf-8");
    public static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
    public static final MediaType MEDIA_TYPE_GIF = MediaType.parse("image/gif");

    protected final JDAImpl api;
    protected final AuthorizationConfig authConfig;
    private final RestRateLimiter rateLimiter;
    private final String baseUrl;
    private final String userAgent;
    private final Consumer<? super okhttp3.Request.Builder> customBuilder;

    private final OkHttpClient httpClient;

    //when we actually set the shard info we can also set the mdc context map, before it makes no sense
    private boolean isContextReady = false;
    private ConcurrentMap<String, String> contextMap = null;

    private volatile boolean retryOnTimeout = false;

    public Requester(JDA api, AuthorizationConfig authConfig, RestConfig config, RestRateLimiter rateLimiter)
    {
        if (authConfig == null)
            throw new NullPointerException("Provided config was null!");

        this.authConfig = authConfig;
        this.api = (JDAImpl) api;
        this.rateLimiter = rateLimiter;
        this.baseUrl = config.getBaseUrl();
        this.userAgent = config.getUserAgent();
        this.customBuilder = config.getCustomBuilder();
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

    public <T> void request(Request<T> apiRequest)
    {
        if (rateLimiter.isStopped())
            throw new RejectedExecutionException("The Requester has been stopped! No new requests can be requested!");

        if (apiRequest.shouldQueue())
            rateLimiter.enqueue(new WorkTask(apiRequest));
        else
            execute(new WorkTask(apiRequest), true);
    }

    private static boolean isRetry(Throwable e)
    {
        return e instanceof SocketException             // Socket couldn't be created or access failed
            || e instanceof SocketTimeoutException      // Connection timed out
            || e instanceof SSLPeerUnverifiedException; // SSL Certificate was wrong
    }

    public okhttp3.Response execute(WorkTask task)
    {
        return execute(task, false);
    }

    /**
     * Used to execute a Request. Processes request related to provided bucket.
     *
     * @param  task
     *         The API request that needs to be sent
     * @param  handleOnRateLimit
     *         Whether to forward rate-limits, false if rate limit handling should take over
     *
     * @return Non-null if the request was ratelimited. Returns a Long containing retry_after milliseconds until
     *         the request can be made again. This could either be for the Per-Route ratelimit or the Global ratelimit.
     *         <br>Check if globalCooldown is {@code null} to determine if it was Per-Route or Global.
     */
    public okhttp3.Response execute(WorkTask task, boolean handleOnRateLimit)
    {
        return execute(task, false, handleOnRateLimit);
    }

    public okhttp3.Response execute(WorkTask task, boolean retried, boolean handleOnRatelimit)
    {
        Route.CompiledRoute route = task.getRoute();

        okhttp3.Request.Builder builder = new okhttp3.Request.Builder();

        String url = baseUrl + route.getCompiledRoute();
        builder.url(url);

        Request<?> apiRequest = task.request;

        applyBody(apiRequest, builder);
        applyHeaders(apiRequest, builder);
        if (customBuilder != null)
        {
            try
            {
                customBuilder.accept(builder);
            }
            catch (Exception e)
            {
                LOG.error("Custom request builder caused exception", e);
            }
        }

        okhttp3.Request request = builder.build();

        Set<String> rays = new LinkedHashSet<>();
        okhttp3.Response[] responses = new okhttp3.Response[4];
        // we have an array of all responses to later close them all at once
        //the response below this comment is used as the first successful response from the server
        okhttp3.Response lastResponse = null;
        try
        {
            LOG.trace("Executing request {} {}", task.getRoute().getMethod(), url);
            int code = 0;
            for (int attempt = 0; attempt < responses.length; attempt++)
            {
                if (apiRequest.isSkipped())
                    return null;

                Call call = httpClient.newCall(request);
                lastResponse = call.execute();
                code = lastResponse.code();
                responses[attempt] = lastResponse;
                String cfRay = lastResponse.header("CF-RAY");
                if (cfRay != null)
                    rays.add(cfRay);

                // Retry a few specific server errors that are related to server issues
                if (!shouldRetry(code))
                    break;

                LOG.debug("Requesting {} -> {} returned status {}... retrying (attempt {})",
                        apiRequest.getRoute().getMethod(),
                        url, code, attempt + 1);
                try
                {
                    Thread.sleep(500 << attempt);
                }
                catch (InterruptedException ignored)
                {
                    break;
                }
            }

            LOG.trace("Finished Request {} {} with code {}", route.getMethod(), lastResponse.request().url(), code);

            if (shouldRetry(code))
            {
                //Epic failure from other end. Attempted 4 times.
                Response response = new Response(lastResponse, -1, rays);
                apiRequest.handleResponse(response);
                task.done = true;
                return null;
            }

            if (!rays.isEmpty())
                LOG.debug("Received response with following cf-rays: {}", rays);

            if (handleOnRatelimit && code == 429)
            {
                long retryAfter = parseRetry(lastResponse);
                task.done = true;
                apiRequest.handleResponse(new Response(lastResponse, retryAfter, rays));
            }
            else if (code != 429)
            {
                task.handleResponse(lastResponse, rays);
            }
            else
            {
                // On 429, replace the retry-after header if its wrong (discord moment)
                // We just pick whichever is bigger between body and header
                try (InputStream body = IOUtil.getBody(lastResponse))
                {
                    long retryAfterBody = (long) Math.ceil(DataObject.fromJson(body).getDouble("retry_after", 0));
                    long retryAfterHeader = Long.parseLong(lastResponse.header(RestRateLimiter.RETRY_AFTER_HEADER));
                    lastResponse = lastResponse.newBuilder()
                            .header("retry-after", Long.toString(Math.max(retryAfterHeader, retryAfterBody)))
                            .build();
                }
                catch (Exception e)
                {
                    LOG.warn("Failed to parse retry-after response body", e);
                }
            }

            return lastResponse;
        }
        catch (UnknownHostException e)
        {
            LOG.error("DNS resolution failed: {}", e.getMessage());
            apiRequest.handleResponse(new Response(e, rays));
            return null;
        }
        catch (IOException e)
        {
            if (retryOnTimeout && !retried && isRetry(e))
                return execute(task, true, handleOnRatelimit);
            LOG.error("There was an I/O error while executing a REST request: {}", e.getMessage());
            apiRequest.handleResponse(new Response(e, rays));
            return null;
        }
        catch (Exception e)
        {
            LOG.error("There was an unexpected error while executing a REST request", e);
            apiRequest.handleResponse(new Response(e, rays));
            return null;
        }
        finally
        {
            for (okhttp3.Response r : responses)
            {
                if (r == null)
                    break;
                r.close();
            }
        }
    }

    private void applyBody(Request<?> apiRequest, okhttp3.Request.Builder builder)
    {
        String method = apiRequest.getRoute().getMethod().toString();
        RequestBody body = apiRequest.getBody();

        if (body == null && HttpMethod.requiresRequestBody(method))
            body = EMPTY_BODY;

        builder.method(method, body);
    }

    private void applyHeaders(Request<?> apiRequest, okhttp3.Request.Builder builder)
    {
        builder.header("user-agent", userAgent)
               .header("accept-encoding", "gzip")
               .header("authorization", authConfig.getToken())
               .header("x-ratelimit-precision", "millisecond"); // still sending this in case of regressions

        // Apply custom headers like X-Audit-Log-Reason
        // If customHeaders is null this does nothing
        if (apiRequest.getHeaders() != null)
        {
            for (Entry<String, String> header : apiRequest.getHeaders().entrySet())
                builder.addHeader(header.getKey(), header.getValue());
        }
    }

    public OkHttpClient getHttpClient()
    {
        return this.httpClient;
    }

    public RestRateLimiter getRateLimiter()
    {
        return rateLimiter;
    }

    public void setRetryOnTimeout(boolean retryOnTimeout)
    {
        this.retryOnTimeout = retryOnTimeout;
    }

    public void stop(boolean shutdown, Runnable callback)
    {
        rateLimiter.stop(shutdown, callback);
    }

    private static boolean shouldRetry(int code)
    {
        return code == 502 || code == 504 || code == 529;
    }

    private long parseRetry(okhttp3.Response response)
    {
        String retryAfter = response.header(RestRateLimiter.RETRY_AFTER_HEADER, "0");
        return (long) (Double.parseDouble(retryAfter) * 1000);
    }

    private class WorkTask implements RestRateLimiter.Work
    {
        private final Request<?> request;
        private boolean done;

        private WorkTask(Request<?> request)
        {
            this.request = request;
        }

        @Nonnull
        @Override
        public Route.CompiledRoute getRoute()
        {
            return request.getRoute();
        }

        @Nonnull
        @Override
        public JDA getJDA()
        {
            return request.getJDA();
        }

        @Nullable
        @Override
        public okhttp3.Response execute()
        {
            return Requester.this.execute(this);
        }

        @Override
        public boolean isSkipped()
        {
            return request.isSkipped();
        }

        @Override
        public boolean isDone()
        {
            return isSkipped() || done;
        }

        @Override
        public boolean isPriority()
        {
            return request.isPriority();
        }

        @Override
        public boolean isCancelled()
        {
            return request.isCancelled();
        }

        @Override
        public void cancel()
        {
            request.cancel();
        }

        private void handleResponse(okhttp3.Response response, Set<String> rays)
        {
            done = true;
            request.handleResponse(new Response(response, -1, rays));
        }
    }
}
