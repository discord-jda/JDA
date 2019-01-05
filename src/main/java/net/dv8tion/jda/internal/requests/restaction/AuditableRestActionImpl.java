/*
 * Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.internal.requests.restaction;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.audit.ThreadLocalReason;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.internal.requests.AbstractRestAction;
import net.dv8tion.jda.internal.requests.Route;
import okhttp3.RequestBody;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.json.JSONObject;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;

public class AuditableRestActionImpl<T> extends AbstractRestAction<T> implements AuditableRestAction<T>
{
    protected String reason = null;

    public AuditableRestActionImpl(JDA api, Route.CompiledRoute route)
    {
        super(api, route);
    }

    public AuditableRestActionImpl(JDA api, Route.CompiledRoute route, RequestBody data)
    {
        super(api, route, data);
    }

    public AuditableRestActionImpl(JDA api, Route.CompiledRoute route, JSONObject data)
    {
        super(api, route, data);
    }

    public AuditableRestActionImpl(JDA api, Route.CompiledRoute route, BiFunction<Response, Request<T>, T> handler)
    {
        super(api, route, handler);
    }

    public AuditableRestActionImpl(JDA api, Route.CompiledRoute route, JSONObject data, BiFunction<Response, Request<T>, T> handler)
    {
        super(api, route, data, handler);
    }

    public AuditableRestActionImpl(JDA api, Route.CompiledRoute route, RequestBody data, BiFunction<Response, Request<T>, T> handler)
    {
        super(api, route, data, handler);
    }

    @Override
    @SuppressWarnings("unchecked")
    public AuditableRestAction<T> setCheck(BooleanSupplier checks)
    {
        return (AuditableRestActionImpl) super.setCheck(checks);
    }

    @CheckReturnValue
    public AuditableRestActionImpl<T> reason(String reason)
    {
        this.reason = reason;
        return this;
    }

    @Override
    protected CaseInsensitiveMap<String, String> finalizeHeaders()
    {
        CaseInsensitiveMap<String, String> headers = super.finalizeHeaders();

        if (reason == null || reason.isEmpty())
        {
            String localReason = ThreadLocalReason.getCurrent();
            if (localReason == null || localReason.isEmpty())
                return headers;
            else
                return generateHeaders(headers, localReason);
        }

        return generateHeaders(headers, reason);
    }

    @Nonnull
    private CaseInsensitiveMap<String, String> generateHeaders(CaseInsensitiveMap<String, String> headers, String reason)
    {
        if (headers == null)
            headers = new CaseInsensitiveMap<>();

        headers.put("X-Audit-Log-Reason", uriEncode(reason));
        return headers;
    }

    private String uriEncode(String input)
    {
        String formEncode = MiscUtil.encodeUTF8(input);
        return formEncode.replace('+', ' ');
    }
}
