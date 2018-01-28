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

package net.dv8tion.jda.core.managers.impl;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;
import org.json.JSONObject;

import java.util.function.Consumer;

public abstract class ManagerBase extends AuditableRestAction<Void>
{
    protected int set = 0;

    protected ManagerBase(JDA api, Route.CompiledRoute route)
    {
        super(api, route);
    }

    public ManagerBase reset(int fields)
    {
        //logic explanation:
        //fields=0101
        //set=1100
        //field & set=0100
        //~(field & set)=1011
        //set & ~(fields&set)=1000
        set &= ~(fields & set);
        return this;
    }

    protected ManagerBase reset()
    {
        set = 0;
        return this;
    }

    @Override
    public void queue(Consumer<Void> success, Consumer<Throwable> failure)
    {
        if (shouldUpdate())
            super.queue(success, failure);
        else
            success.accept(null);
    }

    @Override
    public Void complete(boolean shouldQueue) throws RateLimitedException
    {
        if (shouldUpdate())
            return super.complete(shouldQueue);
        return null;
    }

    @Override
    protected void handleResponse(Response response, Request<Void> request)
    {
        if (response.isOk())
            request.onSuccess(null);
        else
            request.onFailure(response);
    }

    protected Object opt(Object it)
    {
        return it == null ? JSONObject.NULL : it;
    }

    protected boolean shouldUpdate()
    {
        return set != 0;
    }

    protected boolean shouldUpdate(int bit)
    {
        return (set & bit) == bit;
    }
}
