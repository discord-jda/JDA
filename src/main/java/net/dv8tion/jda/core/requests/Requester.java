/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.core.requests;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.requests.ratelimit.BotRateLimiter;
import net.dv8tion.jda.core.requests.ratelimit.ClientRateLimiter;
import net.dv8tion.jda.core.utils.JDALogger;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.internal.http.HttpMethod;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

public class Requester
{
    public static final Logger LOG = JDALogger.getLog(Requester.class);
    public static final String DISCORD_API_PREFIX = String.format("https://discordapp.com/api/v%d/", JDAInfo.DISCORD_REST_VERSION);
    public static final String USER_AGENT = "DiscordBot (" + JDAInfo.GITHUB + ", " + JDAInfo.VERSION + ")";
    public static final RequestBody EMPTY_BODY = RequestBody.create(null, new byte[0]);
    public static final MediaType MEDIA_TYPE_JSON  = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType MEDIA_TYPE_OCTET = MediaType.parse("application/octet-stream; charset=utf-8");

    protected final JDAImpl api;
    private final RateLimiter rateLimiter;

    private final OkHttpClient httpClient;

    private volatile boolean retryOnTimeout = false;

    public Requester(JDA api)
    {
        this(api, api.getAccountType());
    }

    public Requester(JDA api, AccountType accountType)
    {
        if (accountType == null)
            throw new NullPointerException("Provided accountType was null!");

        this.api = (JDAImpl) api;
        if (accountType == AccountType.BOT)
            rateLimiter = new BotRateLimiter(this, 5);
        else
            rateLimiter = new ClientRateLimiter(this, 5);
        
        this.httpClient = this.api.getHttpClientBuilder().build();
    }

    public JDAImpl getJDA()
    {
        return api;
    }

    public <T> void request(Request<T> apiRequest)
    {
        if (rateLimiter.isShutdown) 
            throw new IllegalStateException("The Requester has been shutdown! No new requests can be requested!");

        if (apiRequest.shouldQueue())
            rateLimiter.queueRequest(apiRequest);
        else
            execute(apiRequest, true);
    }

    public Long execute(Request<?> apiRequest)
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
    public Long execute(Request<?> apiRequest, boolean handleOnRateLimit)
    {
        return execute(apiRequest, false, handleOnRateLimit);
    }

    public Long execute(Request<?> apiRequest, boolean retried, boolean handleOnRatelimit)
    {
        Route.CompiledRoute route = apiRequest.getRoute();
        Long retryAfter = rateLimiter.getRateLimit(route);
        if (retryAfter != null)
        {
            if (handleOnRatelimit)
                apiRequest.handleResponse(new Response(retryAfter, Collections.emptySet()));
            return retryAfter;
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
        if (url.startsWith(DISCORD_API_PREFIX) && api.getToken() != null)
            builder.header("authorization", api.getToken());

        // Apply custom headers like X-Audit-Log-Reason
        // If customHeaders is null this does nothing
        if (apiRequest.getHeaders() != null)
        {
            for (Entry<String, String> header : apiRequest.getHeaders().entrySet())
                builder.addHeader(header.getKey(), header.getValue());
        }

        okhttp3.Request request = builder.build();

        Set<String> rays = new LinkedHashSet<>();
        okhttp3.Response[] responses = new okhttp3.Response[4];
        // we have an array of all responses to later close them all at once
        //the response below this comment is used as the first successful response from the server
        okhttp3.Response lastResponse = null;
        try
        {
            int attempt = 0;
            do
            {
                //If the request has been canceled via the Future, don't execute.
                //if (apiRequest.isCanceled())
                //    return null;
                Call call = httpClient.newCall(request);
                lastResponse = call.execute();
                responses[attempt] = lastResponse;
                String cfRay = lastResponse.header("CF-RAY");
                if (cfRay != null)
                    rays.add(cfRay);

                if (lastResponse.code() < 500)
                    break; // break loop, got a successful response!

                attempt++;
                LOG.debug("Requesting {} -> {} returned status {}... retrying (attempt {})",
                        apiRequest.getRoute().getMethod(),
                        url, lastResponse.code(), attempt);
                try
                {
                    Thread.sleep(50 * attempt);
                }
                catch (InterruptedException ignored) {}
            }
            while (attempt < 3 && lastResponse.code() >= 500);

            if (lastResponse.code() >= 500)
            {
                //Epic failure from other end. Attempted 4 times.
                Response response = new Response(lastResponse, -1, rays);
                apiRequest.handleResponse(response);
                return null;
            }

            retryAfter = rateLimiter.handleResponse(route, lastResponse);
            if (!rays.isEmpty())
                LOG.debug("Received response with following cf-rays: {}", rays);

            if (retryAfter == null)
                apiRequest.handleResponse(new Response(lastResponse, -1, rays));
            else if (handleOnRatelimit)
                apiRequest.handleResponse(new Response(lastResponse, retryAfter, rays));

            return retryAfter;
        }
        catch (SocketTimeoutException e)
        {
            if (retryOnTimeout && !retried)
                return execute(apiRequest, true, handleOnRatelimit);
            LOG.error("Requester timed out while executing a request", e);
            apiRequest.handleResponse(new Response(lastResponse, e, rays));
            return null;
        }
        catch (Exception e)
        {
            LOG.error("There was an exception while executing a REST request", e); //This originally only printed on DEBUG in 2.x
            apiRequest.handleResponse(new Response(lastResponse, e, rays));
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

    public void shutdown(long time, TimeUnit unit)
    {
        rateLimiter.shutdown(time, unit);
    }

    public void shutdownNow()
    {
        rateLimiter.forceShutdown();
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
    public static InputStream getBody(okhttp3.Response response) throws IOException
    {
        String encoding = response.header("content-encoding", "");
        if (encoding.equals("gzip"))
            return new GZIPInputStream(response.body().byteStream());
        return response.body().byteStream();
    }
}
