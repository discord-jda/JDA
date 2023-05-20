/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.utils.EncodingUtil;
import okhttp3.RequestBody;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;

public class AuditableRestActionImpl<T> extends RestActionImpl<T> implements AuditableRestAction<T>
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

    public AuditableRestActionImpl(JDA api, Route.CompiledRoute route, DataObject data)
    {
        super(api, route, data);
    }

    public AuditableRestActionImpl(JDA api, Route.CompiledRoute route, BiFunction<Response, Request<T>, T> handler)
    {
        super(api, route, handler);
    }

    public AuditableRestActionImpl(JDA api, Route.CompiledRoute route, DataObject data, BiFunction<Response, Request<T>, T> handler)
    {
        super(api, route, data, handler);
    }

    public AuditableRestActionImpl(JDA api, Route.CompiledRoute route, RequestBody data, BiFunction<Response, Request<T>, T> handler)
    {
        super(api, route, data, handler);
    }

    @Nonnull
    @Override
    public AuditableRestAction<T> setCheck(BooleanSupplier checks)
    {
        return (AuditableRestAction<T>) super.setCheck(checks);
    }

    @Nonnull
    @Override
    public AuditableRestAction<T> timeout(long timeout, @Nonnull TimeUnit unit)
    {
        return (AuditableRestAction<T>) super.timeout(timeout, unit);
    }

    @Nonnull
    @Override
    public AuditableRestAction<T> deadline(long timestamp)
    {
        return (AuditableRestAction<T>) super.deadline(timestamp);
    }

    @Nonnull
    @CheckReturnValue
    public AuditableRestActionImpl<T> reason(@Nullable String reason)
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
        String formEncode = EncodingUtil.encodeUTF8(input);
        return formEncode.replace('+', ' ');
    }
}
