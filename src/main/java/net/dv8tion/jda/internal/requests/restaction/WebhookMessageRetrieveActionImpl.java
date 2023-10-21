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
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageRetrieveAction;

import java.util.function.BiFunction;

public class WebhookMessageRetrieveActionImpl extends AbstractWebhookMessageActionImpl<Message, WebhookMessageRetrieveActionImpl> implements WebhookMessageRetrieveAction
{
    public WebhookMessageRetrieveActionImpl(JDA api, Route.CompiledRoute route, BiFunction<Response, Request<Message>, Message> handler)
    {
        super(api, route, handler);
    }

    @Override
    protected Route.CompiledRoute finalizeRoute()
    {
        Route.CompiledRoute route = super.finalizeRoute();
        if (threadId != null)
            route = route.withQueryParams("thread_id", threadId);
        return route;
    }
}
