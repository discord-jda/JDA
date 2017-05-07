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
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.EntityBuilder;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;

import java.util.Collection;

public class JDABotImpl implements JDABot
{
    protected final JDAImpl api;
    protected String clientId = null;
    protected ShardManager shardManager = null;

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
            protected void handleResponse(Response response, Request<ApplicationInfo> request)
            {
                if (!response.isOk())
                {
                    request.onFailure(response);
                    return;
                }

                ApplicationInfo info = api.getEntityBuilder().createApplicationInfo(response.getObject());
                JDABotImpl.this.clientId = info.getId();
                request.onSuccess(info);
            }
        };
    }

    @Override
    public String getInviteUrl(Permission... permissions)
    {
        StringBuilder builder = buildBaseInviteUrl();
        if (permissions != null && permissions.length > 0)
            builder.append("&permissions=").append(Permission.getRaw(permissions));
        return builder.toString();
    }

    @Override
    public String getInviteUrl(Collection<Permission> permissions)
    {
        StringBuilder builder = buildBaseInviteUrl();
        if (permissions != null && !permissions.isEmpty())
            builder.append("&permissions=").append(Permission.getRaw(permissions));
        return builder.toString();
    }

    private StringBuilder buildBaseInviteUrl()
    {
        if (clientId == null)
            getApplicationInfo().complete();
        StringBuilder builder = new StringBuilder("https://discordapp.com/oauth2/authorize?scope=bot&client_id=");
        builder.append(clientId);
        return builder;
    }

    public void setShardManager(ShardManager shardManager)
    {
        this.shardManager = shardManager;
    }

    @Override
    public ShardManager getShardManager()
    {
        return shardManager;
    }
}
