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

package net.dv8tion.jda.client.entities.impl;

import net.dv8tion.jda.client.entities.AuthorizedApplication;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.requests.Route.CompiledRoute;

import java.util.Collections;
import java.util.List;

public class AuthorizedApplicationImpl implements AuthorizedApplication
{
    private final JDA api;

    private final String authId;
    private final String description;
    private final String iconId;
    private final String id;
    private final String name;
    private final List<String> scopes;

    public AuthorizedApplicationImpl(final JDA api, final String authId, final String description, final String iconId,
            final String id, final String name, final List<String> scopes)
    {
        this.api = api;
        this.authId = authId;
        this.description = description;
        this.iconId = iconId;
        this.id = id;
        this.name = name;
        this.scopes = Collections.unmodifiableList(scopes);
    }

    @Override
    public RestAction<Void> delete()
    {
        CompiledRoute route = Route.Applications.DELETE_AUTHORIZED_APPLICATION.compile(this.authId);

        return new RestAction<Void>(this.api, route, null)
        {
            @Override
            protected void handleResponse(final Response response, final Request request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    @Override
    public boolean equals(final Object obj)
    {
        return obj instanceof AuthorizedApplicationImpl && this.id.equals(((AuthorizedApplicationImpl) obj).id);
    }

    @Override
    public String getAuthId()
    {
        return this.authId;
    }

    @Override
    public String getDescription()
    {
        return this.description;
    }

    @Override
    public String getIconId()
    {
        return this.iconId;
    }

    @Override
    public String getIconUrl()
    {
        return this.iconId == null ? null
                : "https://cdn.discordapp.com/app-icons/" + this.id + '/' + this.iconId + ".png";
    }

    @Override
    public String getId()
    {
        return this.id;
    }

    @Override
    public JDA getJDA()
    {
        return this.api;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public List<String> getScopes()
    {
        return this.scopes;
    }

    @Override
    public int hashCode()
    {
        return this.id.hashCode();
    }

    @Override
    public String toString()
    {
        return "AuthorizedApplication(" + this.id + ")";
    }

}
