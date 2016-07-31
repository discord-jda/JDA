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

package net.dv8tion.jda.core.entities.impl;

import com.mashape.unirest.http.Unirest;
import net.dv8tion.jda.bot.JDABot;
import net.dv8tion.jda.client.JDAClient;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.exceptions.AccountTypeException;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.RequestBuilder;
import net.dv8tion.jda.core.requests.Requester;
import org.apache.http.HttpHost;
import org.json.JSONObject;

import javax.security.auth.login.LoginException;
import java.io.IOException;

public abstract class JDAImpl implements JDA
{
    //Set by the JDAClientImpl and JDABotImpl constructors.
    protected HttpHost proxy;
    protected boolean audioEnabled;
    protected boolean useShutdownHook;
    protected boolean bulkDeleteSplittingEnabled;

    protected Requester requester = new Requester();
    protected Status status = Status.INITIALIZING;
    protected String authToken = null;
    protected boolean reconnect = true;

    public void login(String token) throws LoginException
    {
        setStatus(Status.LOGGING_IN);
        if (token == null || token.isEmpty())
            throw new LoginException("Provided token was null or empty!");

        verifyToken(token);
        this.authToken = token;

        if (useShutdownHook)
        {
            Runtime.getRuntime().addShutdownHook(new Thread("JDA Shutdown Hook")
            {
                @Override
                public void run()
                {
                    JDAImpl.this.shutdown();
                }
            });
        }
    }

    public void setStatus(Status status)
    {
        synchronized (this.status)
        {
            Status oldStatus = this.status;
            this.status = status;

            //TODO: Fire status change event.
//            eventManager.handle(new StatusChangeEvent(this, status, oldStatus));
        }
    }

    public void setAuthToken(String token)
    {
        this.authToken = token;
    }

    public void verifyToken(String token) throws LoginException
    {
        Request request = new RequestBuilder(RequestBuilder.RequestType.GET)
                .setUrl(Requester.DISCORD_API_PREFIX + "users/@me")
                .addHeader("authorization", token)
                .build();
        Requester.Response response = requester.handle(request);
        if (response.isOk())
        {
            JSONObject json = response.getObject();
            if (getAccountType() == AccountType.BOT)
            {
                if (!json.has("bot") || !json.getBoolean("bot"))
                    throw new AccountTypeException(AccountType.BOT, "Attempted to login as a BOT with a CLIENT token!");
            }
            else
            {
                if (json.has("bot") && json.getBoolean("bot"))
                    throw new AccountTypeException(AccountType.CLIENT, "Attempted to login as a CLIENT with a BOT token!");
            }
        }
        else if (response.isRateLimit())
            throw new RateLimitedException("auth/login");//TODO: Do more here somehow.
        else
        {
            if (response.code == 401)
            {
                throw new LoginException("The provided token was invalid!");
            }
            else
            {
                throw new LoginException("When verifying the authenticity of the provided token, Discord returned an unknown response:\n" +
                        response.toString());
            }
        }
    }

    @Override
    public String getAuthToken()
    {
        return authToken;
    }

    @Override
    public HttpHost getGlobalProxy()
    {
        return proxy;
    }

    @Override
    public boolean isAudioEnabled()
    {
        return audioEnabled;
    }

    @Override
    public boolean isBulkDeleteSplittingEnabled()
    {
        return bulkDeleteSplittingEnabled;
    }

    @Override
    public void setAutoReconnect(boolean reconnect)
    {
        this.reconnect = reconnect;
    }

    @Override
    public boolean isAutoReconnect()
    {
        return reconnect;
    }

    @Override
    public Status getStatus()
    {
        return status;
    }

    @Override
    public void shutdown()
    {
        shutdown(true);
    }

    @Override
    public void shutdown(boolean free)
    {
        setStatus(Status.SHUTTING_DOWN);
        //TODO: Shutdown ASYNC system
        //TODO: Shutdown audio connections.
        //TODO: Shutdown Main Websocket.

        if (free)
        {
            try
            {
                Unirest.shutdown();
            }
            catch (IOException ignored) {}
        }
        setStatus(Status.SHUTTING_DOWN);
    }

    @Override
    public void installAuxiliaryCable(int port) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException("Nice try m8!");
    }

    @Override
    public JDAClient asClient()
    {
        throw new AccountTypeException(AccountType.BOT);
    }

    @Override
    public JDABot asBot()
    {
        throw new AccountTypeException(AccountType.CLIENT);
    }
}
