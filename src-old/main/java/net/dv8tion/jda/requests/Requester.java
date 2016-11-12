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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.requests;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.BaseRequest;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.body.RequestBodyEntity;
import net.dv8tion.jda.JDAInfo;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.utils.SimpleLog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Requester
{
    public static final SimpleLog LOG = SimpleLog.getLog("JDARequester");
    public static String USER_AGENT = "JDA DiscordBot (" + JDAInfo.GITHUB + ", " + JDAInfo.VERSION + ")";
    public static final String DISCORD_API_PREFIX = "https://discordapp.com/api/";

    protected final JDAImpl api;

    public Requester(JDAImpl api)
    {
        this.api = api;
    }

    public Response get(String url)
    {
        return exec(addHeaders(Unirest.get(url)));
    }

    public Response delete(String url)
    {
        return exec(addHeaders(Unirest.delete(url)));
    }

    public Response post(String url, JSONObject body)
    {
        return exec(addHeaders(Unirest.post(url)).body(body.toString()));
    }

    public Response post(String url, JSONArray body)
    {
        return exec(addHeaders(Unirest.post(url)).body(body.toString()));
    }

    public Response patch(String url, JSONObject body)
    {
        return exec(addHeaders(Unirest.patch(url)).body(body.toString()));
    }

    public Response patch(String url, JSONArray body)
    {
        return exec(addHeaders(Unirest.patch(url)).body(body.toString()));
    }

    public Response put(String url, JSONObject body)
    {
        return exec(addHeaders(Unirest.put(url)).body(body.toString()));
    }

    public Response put(String url, JSONArray body)
    {
        return exec(addHeaders(Unirest.put(url)).body(body.toString()));
    }

    protected Response exec(BaseRequest request)
    {
        HttpResponse<String> ret = null;
        try
        {
            String dbg = String.format("Requesting %s -> %s\n\tPayload: %s\n\tResponse: ", request.getHttpRequest().getHttpMethod().name(),
                    request.getHttpRequest().getUrl(), ((request instanceof RequestBodyEntity) ? ((RequestBodyEntity) request).getBody().toString() : "None"));
            ret = request.asString();
            if (ret.getBody() != null && ret.getBody().startsWith("<"))
            {
                LOG.debug(String.format("Requesting %s -> %s returned HTML... retrying", request.getHttpRequest().getHttpMethod().name(), request.getHttpRequest().getUrl()));
                try
                {
                    Thread.sleep(50);
                }
                catch (InterruptedException ignored) {}
                ret = request.asString();
            }
            Response response = new Response(ret.getStatus(), ret.getBody());
            LOG.trace(dbg + response.code + ": " + response.responseText);
            return response;
        }
        catch (UnirestException e)
        {
            if (LOG.getEffectiveLevel().compareTo(SimpleLog.Level.DEBUG) != 1)
            {
                LOG.log(e);
            }
            return new Response(e);
        }
    }

    protected <T extends HttpRequest> T addHeaders(T request)
    {
        //adding token to all requests to the discord api or cdn pages
        //can't check for startsWith(DISCORD_API_PREFIX) due to cdn endpoints
        if (api.getAuthToken() != null && request.getUrl().contains("discordapp.com"))
        {
            request.header("authorization", api.getAuthToken());
        }
        if (!(request instanceof GetRequest))
        {
            request.header("Content-Type", "application/json");
        }
        request.header("user-agent", USER_AGENT);
        request.header("Accept-Encoding", "gzip");
        return request;
    }

    public static class Response {
        public static final int connectionErrCode = -1;
        public final Exception exception;
        public final int code;
        public final String responseText;

        protected Response(int code, String response)
        {
            this.code = code;
            this.responseText = response;
            this.exception = null;
        }

        protected Response(Exception exception)
        {
            this.code = connectionErrCode;
            this.responseText = null;
            this.exception = exception;
        }

        public boolean isOk()
        {
            return code > 199 && code < 300;
        }

        public boolean isRateLimit()
        {
            return code == 429;
        }

        public JSONObject getObject()
        {
            try
            {
                return responseText == null ? null : new JSONObject(responseText);
            }
            catch (JSONException ex)
            {
                return null;
            }
        }

        public JSONArray getArray()
        {
            try
            {
                return responseText == null ? null : new JSONArray(responseText);
            }
            catch (JSONException ex)
            {
                return null;
            }
        }

        public String toString()
        {
            return exception == null ? "HTTPResponse[" + code + ": " + responseText + ']'
                    : "HTTPException[" + exception.getMessage() + ']';
        }
    }
}
