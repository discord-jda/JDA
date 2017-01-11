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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.client.entities.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.dv8tion.jda.client.entities.Application;
import net.dv8tion.jda.client.managers.ApplicationManager;
import net.dv8tion.jda.client.managers.ApplicationManagerUpdatable;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import org.json.JSONArray;
import org.json.JSONObject;

public class ApplicationImpl implements Application
{

    private final JDA api;

    private BotImpl bot;
    private String description;
    private boolean doesBotRequireCodeGrant;
    private int flags;
    private String iconId;
    private String id;
    private boolean isBotPublic;
    private ApplicationManager manager;
    private ApplicationManagerUpdatable managerUpdatable;
    private String name;
    private List<String> redirectUris;
    private int rpcApplicationState;
    private List<String> rpcOrigins;
    private String secret;

    public ApplicationImpl(final JDA api, final JSONObject object)
    {
        this.api = api;

        this.updateFromJson(object);
    }

    @Override
    public RestAction<Application.Bot> createBot()
    {
        if (this.hasBot())
            return new RestAction.EmptyRestAction<>(this.bot);

        return new RestAction<Application.Bot>(this.api, Route.Applications.CREATE_BOT.compile(this.id), null)
        {
            @Override
            protected void handleResponse(final Response response, final Request request)
            {
                if (response.isOk())
                    request.onSuccess(ApplicationImpl.this.bot = new BotImpl(response.getObject()));
                else
                    request.onFailure(response);
            }
        };

    }

    @Override
    public RestAction<Void> delete()
    {
        return new RestAction<Void>(this.api, Route.Applications.DELETE_APPLICATION.compile(this.id), null)
        {
            @Override
            protected void handleResponse(final Response response, final Request request)
            {
                if (response.isOk())
                    request.onSuccess(Void.TYPE);
                else
                    request.onFailure(response);
            }
        };
    }

    @Override
    public final boolean doesBotRequireCodeGrant()
    {
        return this.doesBotRequireCodeGrant;
    }

    @Override
    public boolean equals(final Object obj)
    {
        return obj instanceof ApplicationImpl && this.id.equals(((ApplicationImpl) obj).id);
    }

    @Override
    public Bot getBot()
    {
        return this.bot;
    }

    @Override
    public String getDescription()
    {
        return this.description;
    }

    @Override
    public final int getFlags()
    {
        return this.flags;
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
                : "https://cdn.discordapp.com/app-icons/" + this.id + '/' + this.iconId + ".jpg";
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
    public synchronized ApplicationManager getManager()
    {
        ApplicationManager m = this.manager;

        if (m == null)
            m = this.manager = new ApplicationManager(this);

        return m;
    }

    @Override
    public synchronized ApplicationManagerUpdatable getManagerUpdatable()
    {
        ApplicationManagerUpdatable m = this.managerUpdatable;

        if (m == null)
            m = this.managerUpdatable = new ApplicationManagerUpdatable(this);

        return m;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public final List<String> getRedirectUris()
    {
        return Collections.unmodifiableList(this.redirectUris);
    }

    @Override
    public final int getRpcApplicationState()
    {
        return this.rpcApplicationState;
    }

    @Override
    public final List<String> getRpcOrigins()
    {
        return Collections.unmodifiableList(this.rpcOrigins);
    }

    @Override
    public String getSecret()
    {
        return this.secret;
    }

    @Override
    public boolean hasBot()
    {
        return this.bot != null;
    }

    @Override
    public final boolean isBotPublic()
    {
        return this.isBotPublic;
    }

    @Override
    public RestAction<Application> resetSecret()
    {
        return new RestAction<Application>(this.api, Route.Applications.RESET_BOT_TOKEN.compile(this.id), null)
        {
            @Override
            protected void handleResponse(final Response response, final Request request)
            {
                if (response.isOk())
                    request.onSuccess(ApplicationImpl.this.updateFromJson(response.getObject()));
                else
                    request.onFailure(response);
            }
        };
    }

    @Override
    public String toString()
    {
        return "Application(" + this.id + ")";
    }

    public ApplicationImpl updateFromJson(final JSONObject object)
    {
        if (object.has("bot"))
        {
            final JSONObject botObject = object.getJSONObject("bot");

            if (this.bot == null)
                this.bot = new BotImpl(botObject);
            else
                this.bot.updateFromJson(botObject);
        }
        else
        {
            this.bot = null;
        }

        this.isBotPublic = object.getBoolean("bot_public");
        this.doesBotRequireCodeGrant = object.getBoolean("bot_require_code_grant");
        this.description = object.getString("description");
        this.flags = object.getInt("flags");
        this.iconId = object.has("icon") ? null : object.getString("icon");
        this.id = object.getString("id");
        this.name = object.getString("name");

        final JSONArray redirectUriArray = object.getJSONArray("redirect_uris");
        if (this.redirectUris == null)
            this.redirectUris = new ArrayList<>(redirectUriArray.length());
        else
            this.redirectUris.clear();

        for (int i = 0; i < redirectUriArray.length(); i++)
            this.redirectUris.add(redirectUriArray.getString(i));

        this.rpcApplicationState = object.getInt("rpc_application_state");

        final JSONArray rpcOriginArray = object.getJSONArray("rpc_origins");
        if (this.rpcOrigins == null)
            this.rpcOrigins = new ArrayList<>(rpcOriginArray.length());
        else
            this.rpcOrigins.clear();
        for (int i = 0; i < rpcOriginArray.length(); i++)
            this.rpcOrigins.add(rpcOriginArray.getString(i));

        this.secret = object.getString("secret");

        return this;
    }

    public class BotImpl implements Application.Bot
    {
        private String avatarId;
        private String discriminator;
        private String id;
        private String name;
        private String token;

        private BotImpl(final JSONObject object)
        {
            this.updateFromJson(object);
        }

        @Override
        public boolean equals(final Object obj)
        {
            return obj instanceof BotImpl && this.id.equals(((BotImpl) obj).id);
        }

        @Override
        public Application getApplication()
        {
            return ApplicationImpl.this;
        }

        @Override
        public String getAvatarId()
        {
            return this.avatarId;
        }

        @Override
        public String getAvatarUrl()
        {
            return this.avatarId == null ? null
                    : "https://cdn.discordapp.com/avatars/" + this.id + "/" + this.avatarId + ".jpg";
        }

        @Override
        public String getDiscriminator()
        {
            return this.discriminator;
        }

        @Override
        public String getId()
        {
            return this.id;
        }

        @Override
        public String getInviteUrl(final Collection<Permission> permissions)
        {
            return this.getInviteUrl(null, permissions);
        }

        @Override
        public String getInviteUrl(final Permission... permissions)
        {
            return this.getInviteUrl(null, permissions);
        }

        @Override
        public String getInviteUrl(final String guildId, final Collection<Permission> permissions)
        {
            return "https://discordapp.com/oauth2/authorize?client_id=" + this.getId() + "&scope=bot"
                    + (permissions == null || permissions.isEmpty() ? "" : "&permissions=" + Permission.getRaw(permissions))
                    + (guildId == null ? "" : "&guild_id=" + guildId);
        }

        @Override
        public String getInviteUrl(final String guildId, final Permission... permissions)
        {
            return "https://discordapp.com/oauth2/authorize?client_id=" + this.getId() + "&scope=bot"
                    + (permissions == null || permissions.length == 0 ? ""
                            : "&permissions=" + Permission.getRaw(permissions))
                    + (guildId == null ? "" : "&guild_id=" + guildId);
        }

        public JDA getJDA()
        {
            return ApplicationImpl.this.getJDA();
        }

        @Override
        public String getName()
        {
            return this.name;
        }

        @Override
        public String getToken()
        {
            return this.token;
        }

        @Override
        public int hashCode()
        {
            return this.id.hashCode();
        }

        @Override
        public RestAction<Bot> resetToken()
        {
            return new RestAction<Bot>(ApplicationImpl.this.api, Route.Applications.RESET_BOT_TOKEN.compile(this.id),
                    null)
            {
                @Override
                protected void handleResponse(final Response response, final Request request)
                {
                    if (response.isOk())
                        request.onSuccess(BotImpl.this.updateFromJson(response.getObject()));
                    else
                        request.onFailure(response);
                }
            };
        }

        @Override
        public String toString()
        {
            return "Application.Bot(" + this.id + ")";
        }

        public BotImpl updateFromJson(final JSONObject object)
        {
            this.name = object.getString("username");
            this.discriminator = object.getString("discriminator");
            this.token = object.getString("token");
            this.id = object.getString("id");
            this.avatarId = object.has("avatar") ? null : object.getString("avatar");

            return this;
        }
    }
}