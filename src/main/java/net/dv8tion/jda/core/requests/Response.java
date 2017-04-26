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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Response
{
    public static final int ERROR_CODE = -1;
    public final Exception exception;
    public final int code;
    public final long retryAfter;
    public final String responseText;

    protected Response(int code, String response, long retryAfter)
    {
        this.code = code;
        this.responseText = response;
        this.exception = null;
        this.retryAfter = retryAfter;
    }

    protected Response(Exception exception)
    {
        this.code = ERROR_CODE;
        this.responseText = null;
        this.exception = exception;
        this.retryAfter = -1;
    }

    public boolean isError()
    {
        return code == ERROR_CODE;
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

    public String getString()
    {
        return responseText;
    }

    public String toString()
    {
        return exception == null ? "HTTPResponse[" + code + ": " + responseText + ']'
                : "HTTPException[" + exception.getMessage() + ']';
    }
}