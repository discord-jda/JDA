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

/**
 * Fired when a Rest request has been executed.<br>
 * 
 * Depending on the request and it's result not all values have to be populated. 
 */
public class HttpRequestEvent extends Event
{
    private final Request<?> reqeust;
    /** May be {@code null} */
    private final Response response;

    public HttpRequestEvent(final Request<?> request, final Response response)
    {
        super(request.getRestAction().getJDA(), -1);

        this.reqeust = request;
        this.response = response;
    }

    public Request<?> getRequest()
    {
        return this.reqeust;
    }

    public RequestBody getRequestBody()
    {
        return this.reqeust.getBody();
    }

    public Object getRequestBodyRaw()
    {
        return this.reqeust.getRawBody();
    }

    public Headers getRequestHeaders()
    {
        return this.response.getRawResponse().request().headers();
    }

    public okhttp3.Request getRequestRaw()
    {
        return this.response == null ? null : this.response.getRawResponse().request();
    }

    public Response getResponse()
    {
        return this.response;
    }

    public ResponseBody getResponseBody()
    {
        return this.response == null ? null : this.response.getRawResponse().body();
    }

    public JSONArray getResponseBodyAsArray()
    {
        return this.response == null ? null : this.response.getArray();
    }

    public JSONObject getResponseBodyAsObject()
    {
        return this.response == null ? null : this.response.getObject();
    }

    public String getResponseBodyAsString()
    {
        return this.response == null ? null : this.response.getString();
    }

    public Headers getResponseHeaders()
    {
        return this.response == null ? null : this.response.getRawResponse() == null ? null : this.response.getRawResponse().headers();
    }

    public okhttp3.Response getResponseRaw()
    {
        return this.response == null ? null : this.response.getRawResponse();
    }

    public RestAction<?> getRestAction()
    {
        return this.reqeust.getRestAction();
    }

    public CompiledRoute getRoute()
    {
        return this.reqeust.getRoute();
    }

    public boolean isRateLimit()
    {
        return this.response.isRateLimit();
    }

}
