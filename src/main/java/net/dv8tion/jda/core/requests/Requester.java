/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.BaseRequest;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.body.MultipartBody;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.requests.Route.CompiledRoute;
import net.dv8tion.jda.core.requests.ratelimit.BotRateLimiter;
import net.dv8tion.jda.core.requests.ratelimit.ClientRateLimiter;
import net.dv8tion.jda.core.utils.SimpleLog;

public class Requester
{
    public static final SimpleLog LOG = SimpleLog.getLog("JDARequester");
    public static String USER_AGENT = "JDA DiscordBot (" + JDAInfo.GITHUB + ", " + JDAInfo.VERSION + ")";
    public static final String DISCORD_API_PREFIX = "https://discordapp.com/api/";

    private final JDAImpl api;
    private final RateLimiter rateLimiter;

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
    }

    public JDAImpl getJDA()
    {
        return api;
    }

    public void request(Request apiRequest)
    {
        if (rateLimiter.isShutdown)
            throw new IllegalStateException("The Requester has been shutdown! No new requests can be requested!");
        if (apiRequest.shouldQueue())
        {
            rateLimiter.queueRequest(apiRequest);
        }
        else
        {
            Long retryAfter = execute(apiRequest);
            if (retryAfter != null)
                apiRequest.getRestAction().handleResponse(new Response(429, null, retryAfter), apiRequest);
        }
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
    public Long execute(Request apiRequest)
    {
        CompiledRoute route = apiRequest.getRoute();
        Long retryAfter = rateLimiter.getRateLimit(route);
        if (retryAfter != null)
            return retryAfter;

        BaseRequest request;
        Object body = apiRequest.getData();

        //Special case handling for MessageChannel#sendFile.
        // If a MultipartBody request was passed as the body then we assume it was constructed correctly
        // and just wrap it in auth headers and allow processing.
        if (body instanceof MultipartBody)
        {
            request = addHeaders((MultipartBody) body);
        }
        else
        {
            String bodyData = body != null ? body.toString() : null;
            request = createRequest(route, bodyData);
        }

        try
        {
            HttpResponse<String> response;
            int attempt = 0;
            do
            {
                //If the request has been canceled via the Future, don't execute.
                if (apiRequest.isCanceled())
                    return null;
                response = request.asString();

                if (response.getStatus() < 500)
                    break;

                attempt++;
                LOG.debug(String.format("Requesting %s -> %s returned status %d... retrying (attempt %d)",
                        request.getHttpRequest().getHttpMethod().name(),
                        request.getHttpRequest().getUrl(),
                        response.getStatus(), attempt));
                try
                {
                    Thread.sleep(50 * attempt);
                }
                catch (InterruptedException ignored) {}
            }
            while (attempt < 3 && response.getStatus() >= 500);

            if (response.getStatus() >= 500)
            {
                //Epic failure from other end. Attempted 4 times.
                return null;
            }

            retryAfter = rateLimiter.handleResponse(route, response);
            if (retryAfter == null)
                apiRequest.getRestAction().handleResponse(new Response(response.getStatus(), response.getBody(), -1), apiRequest);

            return retryAfter;
        }
        catch (UnirestException e)
        {
            LOG.log(e); //This originally only printed on DEBUG in 2.x
            apiRequest.getRestAction().handleResponse(new Response(e), apiRequest);
            return null;
        }
    }

    public RateLimiter getRateLimiter()
    {
        return rateLimiter;
    }

    public void shutdown()
    {
        rateLimiter.shutdown();
    }

    private BaseRequest createRequest(Route.CompiledRoute route, String body)
    {
        String url = DISCORD_API_PREFIX + route.getCompiledRoute();
        BaseRequest request = null;
        switch (route.getMethod())
        {
            case GET:
                request = addHeaders(Unirest.get(url));
                break;
            case POST:
                request = addHeaders(Unirest.post(url)).body(body);
                break;
            case PUT:
                request = addHeaders(Unirest.put(url)).body(body);
                break;
            case DELETE:
                request = addHeaders(Unirest.delete(url));
                break;
            case PATCH:
                request = addHeaders(Unirest.patch(url)).body(body);
                break;
        }
        return request;
    }

    protected <T extends BaseRequest> T addHeaders(T baseRequest)
    {
        HttpRequest request = baseRequest.getHttpRequest();

        //adding token to all requests to the discord api or cdn pages
        //can't check for startsWith(DISCORD_API_PREFIX) due to cdn endpoints
        if (api.getToken() != null && request.getUrl().contains("discordapp.com"))
        {
            request.header("authorization", api.getToken());
        }
        if (!(request instanceof GetRequest) && !(baseRequest instanceof MultipartBody))
        {
            request.header("Content-Type", "application/json");
        }
        request.header("user-agent", USER_AGENT);
        request.header("Accept-Encoding", "gzip");
        return baseRequest;
    }
}