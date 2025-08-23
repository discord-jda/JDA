/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of {@link SessionController} which respects concurrent shard login.
 * <br>This makes use of the {@link #setConcurrency(int)} hook to delegate buckets for individual shard queues.
 *
 * <p>The concurrency model works through a modulo over the concurrency limit.
 * <pre>{@code bucket = shard_id % concurrency}</pre>
 * This limit is different depending on the scale of the bot and is determined by discord. Bots who participate in
 * a larger set of guilds are eligible to login more shards at once than smaller bots. A bot in 250 guilds will only
 * be able to login 1 shard but a bot in 250K guilds can login 16 or 64 shards at once. Each bucket has a 5 second delay
 * between logins.
 *
 * <p>This implementation is rather naive. It will use one thread per bucket and use sleeps to backoff.
 * If desired, this could be done a lot more efficiently by using a scheduler.
 * However, it is rather unlikely to be an issue in most cases. The only time where 64 threads would actually be used
 * is during the initial startup. During runtime its not common for all shards to reconnect at once.
 */
public class ConcurrentSessionController extends SessionControllerAdapter implements SessionController
{
    private Worker[] workers = new Worker[1];

    @Override
    public void setConcurrency(int level)
    {
        // assertions are ignored at runtime by default, this is a sanity check
        assert level > 0 && level < Integer.MAX_VALUE;
        workers = new Worker[level];
    }

    @Override
    public void appendSession(@Nonnull SessionConnectNode node)
    {
        getWorker(node).enqueue(node);
    }

    @Override
    public void removeSession(@Nonnull SessionConnectNode node)
    {
        getWorker(node).dequeue(node);
    }

    private synchronized Worker getWorker(SessionConnectNode node)
    {
        // get or create worker (synchronously since this should be thread-safe)
        int i = node.getShardInfo().getShardId() % workers.length;
        Worker worker = workers[i];
        if (worker == null)
        {
            log.debug("Creating new worker handle for shard pool {}", i);
            workers[i] = worker = new Worker(i);
        }
        return worker;
    }

    private static class Worker implements Runnable
    {
        private final Queue<SessionConnectNode> queue = new ConcurrentLinkedQueue<>();
        private final int id;
        private Thread thread;

        public Worker(int id)
        {
            this.id = id;
        }

        public synchronized void start()
        {
            if (thread == null)
            {
                thread = new Thread(this, "ConcurrentSessionController-Worker-" + id);
                log.debug("Running worker");
                thread.start();
            }
        }

        public synchronized void stop()
        {
            thread = null;
            if (!queue.isEmpty())
                start();
        }

        public void enqueue(@Nonnull SessionConnectNode node)
        {
            log.trace("Appending node to queue {}", node.getShardInfo());
            queue.add(node);
            start();
        }

        public void dequeue(@Nonnull SessionConnectNode node)
        {
            log.trace("Removing node from queue {}", node.getShardInfo());
            queue.remove(node);
        }

        @Override
        public void run()
        {
            try
            {
                while (!queue.isEmpty())
                {
                    processQueue();
                    // We always sleep here because its possible that we get a new session request before the rate limit expires
                    TimeUnit.SECONDS.sleep(SessionController.IDENTIFY_DELAY);
                }
            }
            catch (InterruptedException ex)
            {
                log.error("Worker failed to process queue", ex);
            }
            finally
            {
                stop();
            }
        }

        private void processQueue() throws InterruptedException
        {
            SessionConnectNode node = null;
            try
            {
                node = queue.remove();
                log.debug("Running connect node for shard {}", node.getShardInfo());
                node.run(false); // we don't use isLast anymore because it can be a problem with many reconnecting shards
            }
            catch (NoSuchElementException ignored) {/* This means the node was removed before we started it */}
            catch (InterruptedException e)
            {
                queue.add(node);
                throw e;
            }
            catch (IllegalStateException | ErrorResponseException e)
            {
                if (Helpers.hasCause(e, OpeningHandshakeException.class))
                    log.error("Failed opening handshake, appending to queue. Message: {}", e.getMessage());
                else if (e instanceof ErrorResponseException && e.getCause() instanceof IOException) { /* This is already logged by the Requester */ }
                else if (Helpers.hasCause(e, UnknownHostException.class))
                    log.error("DNS resolution failed: {}", e.getMessage());
                else if (e.getCause() != null && !JDA.Status.RECONNECT_QUEUED.name().equals(e.getCause().getMessage()))
                    log.error("Failed to establish connection for a node, appending to queue", e);
                else
                    log.error("Unexpected exception when running connect node", e);
                if (node != null)
                    queue.add(node);
            }
        }
    }
}
