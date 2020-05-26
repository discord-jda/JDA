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
import net.dv8tion.jda.api.JDA;

import javax.annotation.Nonnull;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

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

    private final class Worker extends Thread
    {
        private final Queue<SessionConnectNode> queue = new ConcurrentLinkedQueue<>();

        public Worker(int id)
        {
            super("ConcurrentSessionController-Worker-" + id);
        }

        public void enqueue(SessionConnectNode node)
        {
            log.trace("Appending node to queue {}", node.getShardInfo());
            queue.add(node);
            execute();
        }

        public void dequeue(SessionConnectNode node)
        {
            log.trace("Removing node from queue {}", node.getShardInfo());
            queue.remove(node);
        }

        public void execute()
        {
            if (!isAlive())
            {
                log.debug("Running worker");
                start();
            }
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
            catch (IllegalStateException e)
            {
                Throwable t = e.getCause();
                if (t instanceof OpeningHandshakeException)
                    log.error("Failed opening handshake, appending to queue. Message: {}", e.getMessage());
                else if (!JDA.Status.RECONNECT_QUEUED.name().equals(t.getMessage()))
                    log.error("Failed to establish connection for a node, appending to queue", e);
                if (node != null)
                    appendSession(node);
            }
        }
    }
}
