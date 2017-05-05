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

package net.dv8tion.jda.core.requests.restaction;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.util.concurrent.Future;
import java.util.function.Consumer;

public abstract class AuditableRestAction<T> extends RestAction<T>
{

    protected String reason = null;

    public AuditableRestAction(JDA api, Route.CompiledRoute route, Object data)
    {
        super(api, route, data);
    }

    public AuditableRestAction<T> reason(String reason)
    {
        this.reason = reason;
        return this;
    }

    @Override
    protected CaseInsensitiveMap<String, String> finalizeHeaders()
    {
        if (reason == null)
            return null;
        CaseInsensitiveMap<String, String> map = new CaseInsensitiveMap<>();
        map.put("X-Audit-Log-Reason", reason);
        return map;
    }

    public static class EmptyRestAction<T> extends AuditableRestAction<T>
    {
        protected final T content;

        public EmptyRestAction(JDA api, T content)
        {
            super(api, null, null);
            this.content = content;
        }

        @Override
        public void queue(Consumer<T> success, Consumer<Throwable> failure)
        {
            if (success != null)
                success.accept(content);
        }

        @Override
        public Future<T> submit(boolean shouldQueue)
        {
            return new CompletedFuture<>(content);
        }

        @Override
        protected void handleResponse(Response response, Request<T> request) { }
    }
}
