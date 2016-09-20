/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.dv8tion.jda.core.requests;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.BaseRequest;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequest;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.requests.Route.CompiledRoute;
import net.dv8tion.jda.core.requests.ratelimit.BotRateLimiter;
import net.dv8tion.jda.core.requests.ratelimit.ClientRateLimiter;
import net.dv8tion.jda.core.requests.ratelimit.IRateLimiter;
import net.dv8tion.jda.core.utils.SimpleLog;

public class Requester
{
    public static final SimpleLog LOG = SimpleLog.getLog("JDARequester");
    public static String USER_AGENT = "JDA DiscordBot (" + JDAInfo.GITHUB + ", " + JDAInfo.VERSION + ")";
    public static final String DISCORD_API_PREFIX = "https://discordapp.com/api/";

    private final JDA api;
    private final IRateLimiter ratelimiter;

    public Requester(JDA api)
    {
        this(api, api.getAccountType());
    }

    public Requester(JDA api, AccountType accountType)
    {
        this.api = api;
        if (accountType == AccountType.BOT)
            ratelimiter = new BotRateLimiter(this);
        else
            ratelimiter = new ClientRateLimiter(this);

    }

    public void request(Request apiRequest)
    {
        if (apiRequest.shouldQueue())
        {
            ratelimiter.queueRequest(apiRequest);
        }
        else
        {
            Long retryAfter = execute(apiRequest);
            if (retryAfter != null)
                apiRequest.getRestAction().handleResponse(new Response(429, null, retryAfter), apiRequest);
        }
    }

    /**
     * Used to execute an Request. Processes request related to provided bucket.
     *
     * @param apiRequest The API request that needs to be sent
     * @return Returns non-null if the request was ratelimited. Returns a Long containing retry_after milliseconds until
     * the request can be made again. This could either be for the Per-Route ratelimit or the Global ratelimit.
     * Check if globalCooldown is null to determine if it was Per-Route or Global.
     */
    public Long execute(Request apiRequest)
    {
        CompiledRoute route = apiRequest.getRoute();
        Long retryAfter = ratelimiter.getRateLimit(route);
        if (retryAfter != null)
            return retryAfter;

        Object body = apiRequest.getData();

        String bodyData = body != null ? body.toString() : null;
        BaseRequest request = createRequest(route, bodyData);
        try
        {
            HttpResponse<String> response = request.asString();
            int attempt = 1;
            while (attempt < 4 && response.getStatus() != 429 && response.getBody() != null && response.getBody().startsWith("<"))
            {
                LOG.debug(String.format("Requesting %s -> %s returned HTML... retrying (attempt %d)",
                        request.getHttpRequest().getHttpMethod().name(),
                        request.getHttpRequest().getUrl(),
                        attempt));
                try
                {
                    Thread.sleep(50 * attempt);
                }
                catch (InterruptedException ignored)
                {
                }
                response = request.asString();
                attempt++;
            }
            if (response.getBody() != null && response.getBody().startsWith("<"))
            {
                //Epic failure due to cloudfare. Attempted 4 times.
                return null;
            }

            retryAfter = ratelimiter.handleResponse(route, response);
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

    protected <T extends HttpRequest> T addHeaders(T request)
    {
        //adding token to all requests to the discord api or cdn pages
        //can't check for startsWith(DISCORD_API_PREFIX) due to cdn endpoints
        if (api.getToken() != null && request.getUrl().contains("discordapp.com"))
        {
            request.header("authorization", api.getToken());
        }
        if (!(request instanceof GetRequest))
        {
            request.header("Content-Type", "application/json");
        }
        request.header("user-agent", USER_AGENT);
        request.header("Accept-Encoding", "gzip");
        return request;
    }
}