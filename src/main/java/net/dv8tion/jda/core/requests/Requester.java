/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter & Florian Spieß
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
import net.dv8tion.jda.core.ShardedRateLimiter;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.requests.ratelimit.BotRateLimiter;
import net.dv8tion.jda.core.requests.ratelimit.ClientRateLimiter;
import net.dv8tion.jda.core.utils.SimpleLog;
import okhttp3.*;
import okhttp3.internal.http.HttpMethod;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

public class Requester
{
    public static final SimpleLog LOG = SimpleLog.getLog(Requester.class);
    public static final String DISCORD_API_PREFIX = String.format("https://discordapp.com/api/v%d/", JDAInfo.DISCORD_REST_VERSION);
    public static final String USER_AGENT = "DiscordBot (" + JDAInfo.GITHUB + ", " + JDAInfo.VERSION + ")";
    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    public static final RequestBody EMPTY_BODY = RequestBody.create(null, new byte[]{});

    private final JDAImpl api;
    private final RateLimiter rateLimiter;

    private final OkHttpClient httpClient;

    public Requester(JDA api, ShardedRateLimiter shardedRateLimiter)
    {
        this(api, api.getAccountType(), shardedRateLimiter);
    }

    public Requester(JDA api, AccountType accountType, ShardedRateLimiter shardedRateLimiter)
    {
        if (accountType == null)
            throw new NullPointerException("Provided accountType was null!");

        this.api = (JDAImpl) api;
        if (accountType == AccountType.BOT)
            rateLimiter = new BotRateLimiter(this, 5, shardedRateLimiter);
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
     *
     * @return Non-null if the request was ratelimited. Returns a Long containing retry_after milliseconds until
     *         the request can be made again. This could either be for the Per-Route ratelimit or the Global ratelimit.
     *         <br>Check if globalCooldown is {@code null} to determine if it was Per-Route or Global.
     */
    public Long execute(Request<?> apiRequest, boolean handleOnRatelimit)
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
        okhttp3.Response firstSuccess = null;
        try
        {
            int attempt = 0;
            do
            {
                //If the request has been canceled via the Future, don't execute.
                if (apiRequest.isCanceled())
                    return null;
                Call call = httpClient.newCall(request);
                firstSuccess = call.execute();
                responses[attempt] = firstSuccess;
                String cfRay = firstSuccess.header("CF-RAY");
                if (cfRay != null)
                    rays.add(cfRay);

                if (firstSuccess.code() < 500)
                    break; // break loop, got a successful response!

                attempt++;
                LOG.debug(String.format("Requesting %s -> %s returned status %d... retrying (attempt %d)",
                        apiRequest.getRoute().getMethod().toString(),
                        url, firstSuccess.code(), attempt));
                try
                {
                    Thread.sleep(50 * attempt);
                }
                catch (InterruptedException ignored) {}
            }
            while (attempt < 3 && firstSuccess.code() >= 500);

            if (firstSuccess.code() >= 500)
            {
                //Epic failure from other end. Attempted 4 times.
                return null;
            }

            retryAfter = rateLimiter.handleResponse(route, firstSuccess);
            if (!rays.isEmpty())
                LOG.debug("Received response with following cf-rays: " + rays);

            if (retryAfter == null)
                apiRequest.handleResponse(new Response(firstSuccess, -1, rays));
            else if (handleOnRatelimit)
                apiRequest.handleResponse(new Response(firstSuccess, retryAfter, rays));

            return retryAfter;
        }
        catch (Exception e)
        {
            LOG.fatal(e); //This originally only printed on DEBUG in 2.x
            apiRequest.handleResponse(new Response(firstSuccess, e, rays));
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
