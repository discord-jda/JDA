/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.internal.requests;

import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.concurrent.CancellationException;

public abstract class RateLimiter
{
    //Implementations of this class exist in the net.dv8tion.jda.api.requests.ratelimit package.
    protected static final Logger log = JDALogger.getLog(RateLimiter.class);
    protected final Requester requester;
    protected volatile boolean isShutdown = false, isStopped = false;

    protected RateLimiter(Requester requester)
    {
        this.requester = requester;
    }

    protected boolean isSkipped(Iterator<Request> it, Request request)
    {
        try
        {
            if (request.isCanceled() || !request.runChecks())
            {
                cancel(it, request, new CancellationException("RestAction has been cancelled"));
                return true;
            }
        }
        catch (Throwable exception)
        {
            cancel(it, request, exception);
            return true;
        }
        return false;
    }

    private void cancel(Iterator<Request> it, Request request, Throwable exception)
    {
        request.onFailure(exception);
        it.remove();
    }

    // -- Required Implementations --
    public abstract Long getRateLimit(Route.CompiledRoute route);
    protected abstract void queueRequest(Request request);
    protected abstract Long handleResponse(Route.CompiledRoute route, okhttp3.Response response);


    // --- Default Implementations --

    public boolean isRateLimited(Route.CompiledRoute route)
    {
        return getRateLimit(route) != null;
    }

    public void init() {}

    // Return true if no more requests will be processed
    protected boolean stop()
    {
        isStopped = true;
        return true;
    }

    protected void shutdown()
    {
        isShutdown = true;
        stop();
    }
}
