/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.core.requests.restaction;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.requests.*;
import net.dv8tion.jda.core.utils.MiscUtil;
import okhttp3.RequestBody;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.json.JSONObject;

import javax.annotation.CheckReturnValue;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Extension of RestAction to allow setting a reason, only available to accounts of {@link net.dv8tion.jda.core.AccountType#BOT AccountType.BOT}
 *
 * @param  <T>
 *         The return type
 *
 * @since  3.3.0
 */
public abstract class AuditableRestAction<T> extends RestAction<T>
{

    protected String reason = null;

    public AuditableRestAction(JDA api, Route.CompiledRoute route)
    {
        super(api, route);
    }

    public AuditableRestAction(JDA api, Route.CompiledRoute route, RequestBody data)
    {
        super(api, route, data);
    }

    public AuditableRestAction(JDA api, Route.CompiledRoute route, JSONObject data)
    {
        super(api, route, data);
    }

    @Override
    @SuppressWarnings("unchecked")
    public AuditableRestAction<T> setCheck(BooleanSupplier checks)
    {
        return (AuditableRestAction) super.setCheck(checks);
    }

    /**
     * Applies the specified reason as audit-log reason field.
     * <br>When the provided reason is empty or {@code null} it will be treated as not set.
     *
     * <p>Reasons for any AuditableRestAction may be retrieved
     * via {@link net.dv8tion.jda.core.audit.AuditLogEntry#getReason() AuditLogEntry.getReason()}
     * in iterable {@link net.dv8tion.jda.core.requests.restaction.pagination.AuditLogPaginationAction AuditLogPaginationActions}
     * from {@link net.dv8tion.jda.core.entities.Guild#getAuditLogs() Guild.getAuditLogs()}!
     *
     * <p>This will specify the reason via the {@code X-Audit-Log-Reason} Request Header.
     * <br><b>Note: This may not be available to accounts for {@link net.dv8tion.jda.core.AccountType#CLIENT AccountType.CLIENT}.
     * <br>Using methods with a reason parameter will always work and <u>override</u> this header.</b>
     * (ct. {@link net.dv8tion.jda.core.managers.GuildController#ban(net.dv8tion.jda.core.entities.User, int, String) GuildController.ban(User, int, String)})
     *
     * @param  reason
     *         The reason for this action which should be logged in the Guild's AuditLogs
     *
     * @return The current AuditableRestAction instance for chaining convenience
     */
    @CheckReturnValue
    public AuditableRestAction<T> reason(String reason)
    {
        this.reason = reason;
        return this;
    }

    @Override
    protected CaseInsensitiveMap<String, String> finalizeHeaders()
    {
        CaseInsensitiveMap<String, String> headers = super.finalizeHeaders();

        if (reason == null || reason.isEmpty())
            return headers;

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

    /**
     * Specialized form of {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction} that is used to provide information that
     * has already been retrieved or generated so that another request does not need to be made to Discord.
     * <br>Basically: Allows you to provide a value directly to the success returns.
     *
     * @param <T>
     *        The generic response type for this RestAction
     */
    public static class EmptyRestAction<T> extends AuditableRestAction<T>
    {
        protected final T content;

        public EmptyRestAction(JDA api)
        {
            this(api, null);
        }

        public EmptyRestAction(JDA api, T content)
        {
            super(api, null);
            this.content = content;
        }

        @Override
        public void queue(Consumer<? super T> success, Consumer<? super Throwable> failure)
        {
            if (success != null)
                success.accept(content);
        }

        @Override
        public RequestFuture<T> submit(boolean shouldQueue)
        {
            return new RestFuture<>(content);
        }

        @Override
        protected void handleResponse(Response response, Request<T> request) { }
    }
}
