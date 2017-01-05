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

package net.dv8tion.jda.bot.entities.impl;

import net.dv8tion.jda.bot.JDABot;
import net.dv8tion.jda.bot.entities.ApplicationInfo;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.EntityBuilder;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;

public class JDABotImpl implements JDABot
{
    protected final JDAImpl api;

    public JDABotImpl(JDAImpl api)
    {
        this.api = api;
    }

    @Override
    public JDA getJDA()
    {
        return api;
    }

    @Override
    public RestAction<ApplicationInfo> getApplicationInfo()
    {
        Route.CompiledRoute route = Route.Applications.GET_BOT_APPLICATION.compile();
        return new RestAction<ApplicationInfo>(getJDA(), route, null)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (response.isOk())
                    request.onSuccess(EntityBuilder.get(api).createApplicationInfo(response.getObject()));
                else
                    request.onFailure(response);
            }
        };
    }
}
