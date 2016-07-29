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

public abstract class JDAImpl implements JDA
{
    protected Requester requester = new Requester();
    protected Status status;

    public abstract void login(String token) throws LoginException;

    public Status getStatus()
    {
        return status;
    }

    public void setStatus(Status status)
    {
        //TODO: Fire status change event.
        this.status = status;
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
                throw new RuntimeException("When verifying the authenticity of the provided token, Discord returned an unknown response:\n" +
                        response.toString());
            }
        }
    }
}
