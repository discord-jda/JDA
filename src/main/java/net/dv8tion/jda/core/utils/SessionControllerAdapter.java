/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.core.utils;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

public class SessionControllerAdapter implements SessionController
{
    protected Queue<SessionConnectNode> connectQueue;
    protected AtomicLong globalRatelimit;
    protected final Object lock = new Object();
    protected Thread workerHandle;
    protected long lastConnect = 0;

    public SessionControllerAdapter()
    {
        connectQueue = new ConcurrentLinkedQueue<>();
        globalRatelimit = new AtomicLong(Long.MIN_VALUE);
    }

    @Override
    public void appendSession(SessionConnectNode node)
    {
        connectQueue.add(node);
        runWorker();
    }

    @Override
    public void removeSession(SessionConnectNode node)
    {
        connectQueue.remove(node);
    }

    @Override
    public long getGlobalRatelimit()
    {
        return globalRatelimit.get();
    }

    @Override
    public void setGlobalRatelimit(long ratelimit)
    {
        globalRatelimit.set(ratelimit);
    }

    @Override
    public String getGateway(JDA api) throws RateLimitedException
    {
        Route.CompiledRoute route = Route.Misc.GATEWAY.compile();
        return new RestAction<String>(api, route)
        {
            @Override
            protected void handleResponse(Response response, Request<String> request)
            {
                if (response.isOk())
                    request.onSuccess(response.getObject().getString("url"));
                else
                    request.onFailure(response);
            }
        }.complete(false);
    }

    protected void runWorker()
    {
        synchronized (lock)
        {
            if (workerHandle == null)
            {
                workerHandle = new QueueWorker();
                workerHandle.start();
            }
        }
    }

    protected class QueueWorker extends Thread
    {
        public QueueWorker()
        {
            super("SessionControllerAdapter-Worker");
        }

        @Override
        public void run()
        {
            try
            {
                final long delay = System.currentTimeMillis() - lastConnect;
                if (delay < IDENTIFY_DELAY * 1000)
                    Thread.sleep(IDENTIFY_DELAY * 1000 - delay);
            }
            catch (InterruptedException ex)
            {
                JDALogger.getLog(SessionControllerAdapter.class).error("Unable to backoff", ex);
            }
            while (!connectQueue.isEmpty())
            {
                SessionConnectNode node = connectQueue.poll();
                try
                {
                    node.run(connectQueue.isEmpty());
                    lastConnect = System.currentTimeMillis();
                    if (connectQueue.isEmpty())
                        break;
                    Thread.sleep(1000 * IDENTIFY_DELAY);
                }
                catch (InterruptedException e)
                {
                    JDALogger.getLog(SessionControllerAdapter.class).error("Failed to run node", e);
                    appendSession(node);
                }
            }
            synchronized (lock)
            {
                workerHandle = null;
                if (!connectQueue.isEmpty())
                    runWorker();
            }
        }
    }
}
