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

package net.dv8tion.jda.core.events.http;

import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route.CompiledRoute;
import okhttp3.Headers;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Set;

/**
 * Fired when a Rest request has been executed.
 * 
 * <p>Depending on the request and its result not all values have to be populated.
 */
@ParametersAreNonnullByDefault
public class HttpRequestEvent extends Event
{
    private final Request<?> request;
    private final Response response;

    public HttpRequestEvent(final Request<?> request, final Response response)
    {
        super(request.getJDA());

        this.request = request;
        this.response = response;
    }

    @Nonnull
    public Request<?> getRequest()
    {
        return this.request;
    }

    @Nullable
    public RequestBody getRequestBody()
    {
        return this.request.getBody();
    }

    @Nullable
    public Object getRequestBodyRaw()
    {
        return this.request.getRawBody();
    }

    @Nullable
    public Headers getRequestHeaders()
    {
        return this.response.getRawResponse() == null ? null : this.response.getRawResponse().request().headers();
    }

    @Nullable
    public okhttp3.Request getRequestRaw()
    {
        return this.response.getRawResponse() == null ? null : this.response.getRawResponse().request();
    }

    @Nonnull
    public Response getResponse()
    {
        return this.response;
    }

    @Nullable
    public ResponseBody getResponseBody()
    {
        return this.response.getRawResponse() == null ? null : this.response.getRawResponse().body();
    }

    @Nullable
    public JSONArray getResponseBodyAsArray()
    {
        return this.response.getArray();
    }

    @Nullable
    public JSONObject getResponseBodyAsObject()
    {
        return this.response.getObject();
    }

    @Nonnull
    public String getResponseBodyAsString()
    {
        return this.response.getString();
    }

    @Nullable
    public Headers getResponseHeaders()
    {
        return this.response.getRawResponse() == null ? null : this.response.getRawResponse().headers();
    }

    @Nullable
    public okhttp3.Response getResponseRaw()
    {
        return this.response.getRawResponse();
    }

    @Nonnull
    public Set<String> getCFRays()
    {
        return this.response.getCFRays();
    }

    @Nonnull
    public RestAction<?> getRestAction()
    {
        return this.request.getRestAction();
    }

    @Nonnull
    public CompiledRoute getRoute()
    {
        return this.request.getRoute();
    }

    public boolean isRateLimit()
    {
        return this.response.isRateLimit();
    }

}
