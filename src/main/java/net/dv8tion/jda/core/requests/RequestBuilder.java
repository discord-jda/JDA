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

package net.dv8tion.jda.core.requests;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.BaseRequest;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequest;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class RequestBuilder
{
    public enum RequestType
    {
        GET, POST, PUT, DELETE, PATCH
    }

    private boolean isAsync = false;

    private RequestType type;
    private String url = null;
    private String body = "";

    //Special headers
    private String auth = null;
    private String userAgent = Requester.USER_AGENT;
    private String contentType = null;
    private String accept = null;

    private Map<String, String> customHeaders = new HashMap<>();

    private Function<String, String> bucketTransform = null;
    private String[] buckets = new String[0];

    public RequestBuilder(RequestType type)
    {
        this.type = type;
    }

    public RequestBuilder setUrl(String url)
    {
        this.url = url;
        return this;
    }

    public RequestBuilder setBody(String body)
    {
        this.body = body;
        this.contentType = "text/plain";
        return this;
    }

    public RequestBuilder setBody(JSONObject body)
    {
        this.body = body.toString();
        this.contentType = "application/json";
        return this;
    }

    public RequestBuilder setBody(JSONArray body)
    {
        this.body = body.toString();
        this.contentType = "application/json";
        return this;
    }

    public RequestBuilder setType(RequestType type)
    {
        this.type = type;
        return this;
    }

    public RequestBuilder setUserAgent(String userAgent)
    {
        this.userAgent = userAgent;
        return this;
    }

    public RequestBuilder setContentType(String contentType)
    {
        this.contentType = contentType;
        return this;
    }

    public RequestBuilder setAccept(String accept)
    {
        this.accept = accept;
        return this;
    }

    public RequestBuilder addHeader(String key, String value)
    {
        customHeaders.put(key, value);
        return this;
    }

    public RequestBuilder setAsync(boolean async)
    {
        isAsync = async;
        return this;
    }

    public RequestBuilder setBucketTransform(Function<String, String> bucketTransform)
    {
        this.bucketTransform = bucketTransform;
        return this;
    }

    public RequestBuilder setBuckets(String... buckets)
    {
        this.buckets = buckets;
        return this;
    }

    public Request build()
    {
        if(url == null || url.isEmpty())
            throw new IllegalArgumentException("URL Must not be null/empty");
        BaseRequest request = null;
        switch (type)
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
        return new Request(request, isAsync, bucketTransform, buckets);
    }

    private <T extends HttpRequest> T addHeaders(T request)
    {
        //adding token to all requests to the discord api or cdn pages
        //can't check for startsWith(DISCORD_API_PREFIX) due to cdn endpoints
        if (auth != null)
        {
            request.header("authorization", auth);
        }
        if (!(request instanceof GetRequest) && contentType != null)
        {
            request.header("Content-Type", contentType);
        }
        if (accept != null)
        {
            request.header("Accept", accept);
        }
        request.header("user-agent", userAgent);
        request.header("Accept-Encoding", "gzip");

        customHeaders.entrySet().forEach(h -> request.header(h.getKey(), h.getValue()));
        return request;
    }
}
