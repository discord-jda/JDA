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

package net.dv8tion.jda.api.utils;

import com.neovisionaries.ws.client.OpeningHandshakeException;
import net.dv8tion.jda.annotations.DeprecatedSince;
import net.dv8tion.jda.annotations.ForRemoval;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.exceptions.AccountTypeException;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.JDALogger;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class SessionControllerAdapter implements SessionController
{
    @Deprecated
    @ForRemoval
    @DeprecatedSince("4.2.0")
    protected Queue<SessionConnectNode> connectQueue;
    @Deprecated
    @ForRemoval
    @DeprecatedSince("4.2.0")
    protected Thread workerHandle;
    @Deprecated
    @ForRemoval
    @DeprecatedSince("4.2.0")
    protected long lastConnect = 0;

    protected static final Logger log = JDALogger.getLog(SessionControllerAdapter.class);
    protected final Object lock = new Object();

    protected AtomicLong globalRatelimit;

    protected QueueWorker[] workers = new QueueWorker[1];

    public SessionControllerAdapter()
    {
        connectQueue = new ConcurrentLinkedQueue<>();
        globalRatelimit = new AtomicLong(Long.MIN_VALUE);
    }

    @Override
    public void setConcurrency(int level)
    {
        workers = new QueueWorker[Math.max(1, level)];
    }

    @Override
    public void appendSession(@Nonnull SessionConnectNode node)
    {
        synchronized (lock)
        {
            int shardId = node.getShardInfo().getShardId();
            int bucket = shardId % workers.length;
            QueueWorker worker = workers[bucket];
            if (worker == null)
                workers[bucket] = worker = new QueueWorker();
            worker.connectQueue.add(node);

            if (!worker.isAlive())
                worker.start();
        }
    }

    @Override
    public void removeSession(@Nonnull SessionConnectNode node)
    {
        synchronized (lock)
        {
            int shardId = node.getShardInfo().getShardId();
            int bucket = shardId % workers.length;
            if (workers[bucket] == null)
                return;
            workers[bucket].connectQueue.remove(node);
        }
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

    @Nonnull
    @Override
    public String getGateway(@Nonnull JDA api)
    {
        Route.CompiledRoute route = Route.Misc.GATEWAY.compile();
        return new RestActionImpl<String>(api, route,
            (response, request) -> response.getObject().getString("url")).priority().complete();
    }

    @Nonnull
    @Override
    public ShardedGateway getShardedGateway(@Nonnull JDA api)
    {
        AccountTypeException.check(api.getAccountType(), AccountType.BOT);
        return new RestActionImpl<ShardedGateway>(api, Route.Misc.GATEWAY_BOT.compile())
        {
            @Override
            public void handleResponse(Response response, Request<ShardedGateway> request)
            {
                try
                {
                    if (response.isOk())
                    {
                        DataObject object = response.getObject();

                        String url = object.getString("url");
                        int shards = object.getInt("shards");
                        int concurrency = object.getObject("session_start_limit")
                                                .getInt("max_concurrency", 1);

                        request.onSuccess(new ShardedGateway(url, shards, concurrency));
                    }
                    else if (response.code == 401)
                    {
                        api.shutdownNow();
                        throw new LoginException("The provided token is invalid!");
                    }
                    else
                    {
                        request.onFailure(new LoginException("When verifying the authenticity of the provided token, Discord returned an unknown response:\n" +
                            response.toString()));
                    }
                }
                catch (Exception e)
                {
                    request.onFailure(e);
                }
            }
        }.priority().complete();
    }

    @Nonnull
    @Override
    @SuppressWarnings({"deprecation", "RedundantSuppression"})
    public Pair<String, Integer> getGatewayBot(@Nonnull JDA api)
    {
        ShardedGateway bot = getShardedGateway(api);
        return Pair.of(bot.getUrl(), bot.getShardTotal());
    }

    @Deprecated
    @ForRemoval
    @DeprecatedSince("4.2.0")
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
        /** Delay (in milliseconds) to sleep between connecting sessions */
        protected final long delay;
        protected final Queue<SessionConnectNode> connectQueue = new ConcurrentLinkedQueue<>();
        protected long lastConnect = 0;

        public QueueWorker()
        {
            this(IDENTIFY_DELAY);
        }

        /**
         * Creates a QueueWorker
         *
         * @param delay
         *        delay (in seconds) to wait between starting sessions
         */
        public QueueWorker(int delay)
        {
            this(TimeUnit.SECONDS.toMillis(delay));
        }

        /**
         * Creates a QueueWorker
         *
         * @param delay
         *        delay (in milliseconds) to wait between starting sessions
         */
        public QueueWorker(long delay)
        {
            super("SessionControllerAdapter-Worker");
            this.delay = delay;
            super.setUncaughtExceptionHandler(this::handleFailure);
        }

        protected void handleFailure(Thread thread, Throwable exception)
        {
            log.error("Worker has failed with throwable!", exception);
        }

        @Override
        public void run()
        {
            try
            {
                if (this.delay > 0)
                {
                    final long interval = System.currentTimeMillis() - lastConnect;
                    if (interval < this.delay)
                        Thread.sleep(this.delay - interval);
                }
            }
            catch (InterruptedException ex)
            {
                log.error("Unable to backoff", ex);
                return;
            }
            processQueue();
            synchronized (lock)
            {
                if (!connectQueue.isEmpty())
                    run();
            }
        }

        protected void processQueue()
        {
            boolean isMultiple = connectQueue.size() > 1;
            while (!connectQueue.isEmpty())
            {
                SessionConnectNode node = connectQueue.poll();
                if (node == null)
                    break;
                try
                {
                    node.run(isMultiple && connectQueue.isEmpty());
                    isMultiple = true;
                    lastConnect = System.currentTimeMillis();
                    if (connectQueue.isEmpty())
                        break;
                    if (this.delay > 0)
                        Thread.sleep(this.delay);
                }
                catch (IllegalStateException e)
                {
                    Throwable t = e.getCause();
                    if (t instanceof OpeningHandshakeException)
                        log.error("Failed opening handshake, appending to queue. Message: {}", e.getMessage());
                    else if (t != null && !JDA.Status.RECONNECT_QUEUED.name().equals(t.getMessage()))
                        log.error("Failed to establish connection for a node, appending to queue", e);
                    else
                        log.error("Unexpected exception in queue worker", e);
                    connectQueue.add(node);
                }
                catch (InterruptedException e)
                {
                    log.error("Failed to run node", e);
                    connectQueue.add(node);
                    return; // caller should start a new thread
                }
            }
        }
    }
}
