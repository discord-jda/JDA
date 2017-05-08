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

import java.io.BufferedReader;
import java.io.Reader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Response
{
    public static final int ERROR_CODE = -1;
    public static final String ERROR_MESSAGE = "ERROR";

    public final Exception exception;
    public final int code;
    public final String message;
    public final long retryAfter;

    private Object object = null;

    protected Response(long retryAfter)
    {
        this(429, "TOO MANY REQUESTS", null, retryAfter);
    }

    protected Response(okhttp3.Response response, long retryAfter)
    {
        this(response.code(), response.message(), response, retryAfter);
    }

    protected Response(int code, String message, okhttp3.Response response, long retryAfter)
    {
        this.code = code;
        this.message = message;
        this.exception = null;
        this.retryAfter = retryAfter;

        if (response == null || response.body().contentLength() == 0)
            return;

        try (Reader reader = response.body().charStream().markSupported() 
                ? response.body().charStream()
                : new BufferedReader(response.body().charStream())) // this doesn't add overhead as org.json would do that itself otherwise
        {
            char begin; // not sure if I really like this... but we somehow have to get if this is an object or an array  
            int mark = 1;
            do
            {
                reader.mark(mark++);
                begin = (char) reader.read();
            }
            while (Character.isWhitespace(begin));

            reader.reset();

            if (begin == '{')
            {
                object = new JSONObject(new JSONTokener(reader));
            }
            else if (begin == '[')
            {
                object = new JSONArray(new JSONTokener(reader));
            }
        }
        catch (Exception e)
        {
            // TODO rethrow?
            e.printStackTrace();
        }
    }

    protected Response(Exception exception)
    {
        this.code = ERROR_CODE;
        this.message = ERROR_MESSAGE;
        this.object = null;
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
      return (JSONObject) object;
    }

    public JSONArray getArray()
    {
        return (JSONArray) object;
    }
}