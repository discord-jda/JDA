/**
 * Copyright 2015 Austin Keener & Michael Ritter
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.requests;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.BaseRequest;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequest;
import net.dv8tion.jda.JDAInfo;
import net.dv8tion.jda.entities.impl.JDAImpl;
import org.json.JSONArray;
import org.json.JSONObject;

public class Requester
{
    private final JDAImpl api;

    public Requester(JDAImpl api)
    {
        this.api = api;
    }

    public JSONObject get(String url)
    {
        return toObject(addHeaders(Unirest.get(url)));
    }

    public JSONObject delete(String url)
    {
        return toObject(addHeaders(Unirest.delete(url)));
    }

    public JSONObject post(String url, JSONObject body)
    {
        return toObject(addHeaders(Unirest.post(url)).body(body.toString()));
    }

    public JSONObject patch(String url, JSONObject body)
    {
        return toObject(addHeaders(Unirest.patch(url)).body(body.toString()));
    }

    public JSONArray getA(String url)
    {
        return toArray(addHeaders(Unirest.get(url)));
    }

    public JSONArray deleteA(String url)
    {
        return toArray(addHeaders(Unirest.delete(url)));
    }

    public JSONArray postA(String url, JSONObject body)
    {
        return toArray(addHeaders(Unirest.post(url)).body(body.toString()));
    }

    public JSONArray patchA(String url, JSONObject body)
    {
        return toArray(addHeaders(Unirest.patch(url)).body(body.toString()));
    }

    private JSONObject toObject(BaseRequest request)
    {
        try
        {
            return request.asJson().getBody().getObject();
        }
        catch (UnirestException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private JSONArray toArray(BaseRequest request)
    {
        try
        {
            return request.asJson().getBody().getArray();
        }
        catch (UnirestException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private <T extends HttpRequest> T addHeaders(T request)
    {
        if (api.getAuthToken() != null)
        {
            request.header("authorization", api.getAuthToken());
        }
        if (!(request instanceof GetRequest))
        {
            request.header("Content-Type", "application/json");
        }
        request.header("user-agent", JDAInfo.GITHUB + " " + JDAInfo.VERSION);
        return request;
    }
}
