/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.dv8tion.jda.client.managers;

import net.dv8tion.jda.client.entities.Application;
import net.dv8tion.jda.client.entities.impl.ApplicationImpl;
import net.dv8tion.jda.client.managers.fields.ApplicationField;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.managers.fields.Field;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;

import org.json.JSONObject;

import java.util.List;
import java.util.regex.Pattern;

public class ApplicationManagerUpdatable
{
    public final static Pattern URL_PATTERN = Pattern.compile("\\s*https?:\\/\\/.+\\..{2,}\\s*",
            Pattern.CASE_INSENSITIVE);

    private final ApplicationImpl application;

    private ApplicationField<String> description;
    private ApplicationField<Boolean> doesBotRequireCodeGrant;
    private ApplicationField<Icon> icon;
    private ApplicationField<Boolean> isBotPublic;
    private ApplicationField<String> name;
    private ApplicationField<List<String>> redirectUris;

    private ApplicationField<List<String>> rpcOrigins;

    public ApplicationManagerUpdatable(final ApplicationImpl application)
    {
        this.application = application;

        this.setupFields();
    }

    public final Application getApplication()
    {
        return this.application;
    }

    public final ApplicationField<String> getDescriptionField()
    {
        return this.description;
    }

    public final ApplicationField<Boolean> getDoesBotRequireCodeGrantField()
    {
        return this.doesBotRequireCodeGrant;
    }

    public final ApplicationField<Icon> getIconField()
    {
        return this.icon;
    }

    public final ApplicationField<Boolean> getIsBotPublicField()
    {
        return this.isBotPublic;
    }

    public JDA getJDA()
    {
        return this.application.getJDA();
    }

    public final ApplicationField<String> getNameField()
    {
        return this.name;
    }

    public final ApplicationField<List<String>> getRedirectUrisField()
    {
        return this.redirectUris;
    }

    public final ApplicationField<List<String>> getRpcOriginsField()
    {
        return this.rpcOrigins;
    }

    protected boolean needsUpdate()
    {
        return this.description.shouldUpdate() || this.doesBotRequireCodeGrant.shouldUpdate()
                || this.icon.shouldUpdate() || this.isBotPublic.shouldUpdate() || this.name.shouldUpdate()
                || this.redirectUris.shouldUpdate() || this.rpcOrigins.shouldUpdate();
    }

    /**
     * Resets this Manager to default values.
     */
    public void reset()
    {
        this.description.reset();
        this.doesBotRequireCodeGrant.reset();
        this.icon.reset();
        this.isBotPublic.reset();
        this.name.reset();
        this.redirectUris.reset();
        this.rpcOrigins.reset();
    }

    protected void setupFields()
    {
        this.description = new ApplicationField<String>(this, this.application::getDescription)
        {
            @Override
            public void checkValue(final String value)
            {
                Field.checkNull(value, "application description");
                if (value.length() > 400)
                    throw new IllegalArgumentException("Application description must be 0 to 400 characters in length");
            }
        };

        this.doesBotRequireCodeGrant = new ApplicationField<Boolean>(this, this.application::doesBotRequireCodeGrant)
        {
            @Override
            public void checkValue(final Boolean value)
            {
                Field.checkNull(value, "doesBotRequireCodeGrant");
            }
        };

        this.icon = new ApplicationField<Icon>(this, null)
        {
            @Override
            public void checkValue(final Icon value)
            {}

            @Override
            public Icon getOriginalValue()
            {
                throw new UnsupportedOperationException(
                        "Cannot easily provide the original Avatar. Use Application#getIconUrl() and download it yourself.");
            }

            @Override
            public boolean shouldUpdate()
            {
                return this.isSet();
            }
        };

        this.isBotPublic = new ApplicationField<Boolean>(this, this.application::isBotPublic)
        {
            @Override
            public void checkValue(final Boolean value)
            {
                Field.checkNull(value, "isBotPublic");
            }
        };

        this.name = new ApplicationField<String>(this, this.application::getName)
        {
            @Override
            public void checkValue(final String value)
            {
                Field.checkNull(value, "application name");
                if (value.length() < 2 || value.length() > 32)
                    throw new IllegalArgumentException("Application name must be 2 to 32 characters in length");
            }
        };

        this.redirectUris = new ApplicationField<List<String>>(this, this.application::getRedirectUris)
        {
            @Override
            public void checkValue(final List<String> value)
            {
                Field.checkNull(value, "redirect uris");
                for (final String url : value)
                {

                    Field.checkNull(url, "redirect uri");
                    if (!ApplicationManagerUpdatable.URL_PATTERN.matcher(url).matches())
                        throw new IllegalArgumentException("URL must be a valid http or https url.");
                }
            }
        };

        this.rpcOrigins = new ApplicationField<List<String>>(this, this.application::getRedirectUris)
        {
            @Override
            public void checkValue(final List<String> value)
            {
                Field.checkNull(value, "rpc origins");
                for (final String url : value)
                {
                    Field.checkNull(url, "rpc origin");
                    if (!ApplicationManagerUpdatable.URL_PATTERN.matcher(url).matches())
                        throw new IllegalArgumentException("URL must be a valid http or https url.");
                }
            }
        };
    }

    public RestAction<Application> update()
    {
        if (!this.needsUpdate())
            return new RestAction.EmptyRestAction<>(this.application);

        final JSONObject body = new JSONObject();

        // All fields are required or they are resetted to default

        body.put("description",
                this.description.shouldUpdate() ? this.description.getValue() : this.description.getOriginalValue());

        body.put("bot_require_code_grant", this.doesBotRequireCodeGrant.shouldUpdate()
                ? this.doesBotRequireCodeGrant.getValue() : this.doesBotRequireCodeGrant.getOriginalValue());

        body.put("icon",
                this.icon.shouldUpdate()
                        ? this.icon.getValue() == null ? JSONObject.NULL : this.icon.getValue().getEncoding()
                        : this.application.getIconUrl());

        body.put("bot_public",
                this.isBotPublic.shouldUpdate() ? this.isBotPublic.getValue() : this.isBotPublic.getOriginalValue());

        body.put("name", this.name.shouldUpdate() ? this.name.getValue() : this.name.getOriginalValue());

        if (this.redirectUris.shouldUpdate())
        {
            // This is neccessary because Lists are mutable and thus can be modified before sending
            this.redirectUris.checkValue(this.redirectUris.getValue());
            body.put("redirect_uris", this.redirectUris.getValue());
        }
        else
            body.put("redirect_uris", this.redirectUris.getOriginalValue());

        if (this.rpcOrigins.shouldUpdate())
        {
            // same here
            this.rpcOrigins.checkValue(this.rpcOrigins.getValue());
            body.put("rpc_origins", this.rpcOrigins.getValue());
        }
        else
            body.put("rpc_origins", this.rpcOrigins.getOriginalValue());

        return new RestAction<Application>(this.getJDA(),
                Route.Applications.MODIFY_APPLICATION.compile(this.application.getId()), body)
        {
            @Override
            protected void handleResponse(final Response response, final Request request)
            {
                if (response.isOk())
                {
                    ApplicationManagerUpdatable.this.application.updateFromJson(response.getObject());
                    request.onSuccess(ApplicationManagerUpdatable.this.application);
                }
                else
                    request.onFailure(response);
            }
        };
    }
}
