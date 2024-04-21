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
package net.dv8tion.jda.api.utils

import com.neovisionaries.ws.client.OpeningHandshakeException
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.utils.SessionController.SessionConnectNode
import net.dv8tion.jda.internal.utils.Helpers
import java.io.IOException
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit
import javax.annotation.Nonnull

/**
 * Implementation of [SessionController] which respects concurrent shard login.
 * <br></br>This makes use of the [.setConcurrency] hook to delegate buckets for individual shard queues.
 *
 *
 * The concurrency model works through a modulo over the concurrency limit.
 * <pre>`bucket = shard_id % concurrency`</pre>
 * This limit is different depending on the scale of the bot and is determined by discord. Bots who participate in
 * a larger set of guilds are eligible to login more shards at once than smaller bots. A bot in 250 guilds will only
 * be able to login 1 shard but a bot in 250K guilds can login 16 or 64 shards at once. Each bucket has a 5 second delay
 * between logins.
 *
 *
 * This implementation is rather naive. It will use one thread per bucket and use sleeps to backoff.
 * If desired, this could be done a lot more efficiently by using a scheduler.
 * However, it is rather unlikely to be an issue in most cases. The only time where 64 threads would actually be used
 * is during the initial startup. During runtime its not common for all shards to reconnect at once.
 */
class ConcurrentSessionController : SessionControllerAdapter(), SessionController {
    private var workers = arrayOfNulls<Worker>(1)
    override fun setConcurrency(level: Int) {
        // assertions are ignored at runtime by default, this is a sanity check
        assert(level > 0 && level < Int.MAX_VALUE)
        workers = arrayOfNulls(level)
    }

    override fun appendSession(@Nonnull node: SessionConnectNode) {
        getWorker(node).enqueue(node)
    }

    override fun removeSession(@Nonnull node: SessionConnectNode) {
        getWorker(node).dequeue(node)
    }

    @Synchronized
    private fun getWorker(node: SessionConnectNode): Worker {
        // get or create worker (synchronously since this should be thread-safe)
        val i = node.getShardInfo().shardId % workers.size
        var worker = workers[i]
        if (worker == null) {
            SessionControllerAdapter.Companion.log.debug("Creating new worker handle for shard pool {}", i)
            worker = Worker(i)
            workers[i] = worker
        }
        return worker
    }

    private class Worker(private val id: Int) : Runnable {
        private val queue: Queue<SessionConnectNode?> = ConcurrentLinkedQueue()
        private var thread: Thread? = null
        @Synchronized
        fun start() {
            if (thread == null) {
                thread = Thread(this, "ConcurrentSessionController-Worker-$id")
                SessionControllerAdapter.Companion.log.debug("Running worker")
                thread!!.start()
            }
        }

        @Synchronized
        fun stop() {
            thread = null
            if (!queue.isEmpty()) start()
        }

        fun enqueue(node: SessionConnectNode) {
            SessionControllerAdapter.Companion.log.trace("Appending node to queue {}", node.getShardInfo())
            queue.add(node)
            start()
        }

        fun dequeue(node: SessionConnectNode) {
            SessionControllerAdapter.Companion.log.trace("Removing node from queue {}", node.getShardInfo())
            queue.remove(node)
        }

        override fun run() {
            try {
                while (!queue.isEmpty()) {
                    processQueue()
                    // We always sleep here because its possible that we get a new session request before the rate limit expires
                    TimeUnit.SECONDS.sleep(SessionController.Companion.IDENTIFY_DELAY.toLong())
                }
            } catch (ex: InterruptedException) {
                SessionControllerAdapter.Companion.log.error("Worker failed to process queue", ex)
            } finally {
                stop()
            }
        }

        @Throws(InterruptedException::class)
        private fun processQueue() {
            var node: SessionConnectNode? = null
            try {
                node = queue.remove()
                SessionControllerAdapter.Companion.log.debug("Running connect node for shard {}", node.getShardInfo())
                node!!.run(false) // we don't use isLast anymore because it can be a problem with many reconnecting shards
            } catch (ignored: NoSuchElementException) { /* This means the node was removed before we started it */
            } catch (e: InterruptedException) {
                queue.add(node)
                throw e
            } catch (e: IllegalStateException) {
                if (Helpers.hasCause(
                        e,
                        OpeningHandshakeException::class.java
                    )
                ) SessionControllerAdapter.Companion.log.error(
                    "Failed opening handshake, appending to queue. Message: {}",
                    e.message
                ) else if (e is ErrorResponseException && e.cause is IOException) { /* This is already logged by the Requester */
                } else if (Helpers.hasCause(
                        e,
                        UnknownHostException::class.java
                    )
                ) SessionControllerAdapter.Companion.log.error(
                    "DNS resolution failed: {}",
                    e.message
                ) else if (e.cause != null && JDA.Status.RECONNECT_QUEUED.name != e.cause!!.message) SessionControllerAdapter.Companion.log.error(
                    "Failed to establish connection for a node, appending to queue",
                    e
                ) else SessionControllerAdapter.Companion.log.error("Unexpected exception when running connect node", e)
                if (node != null) queue.add(node)
            } catch (e: ErrorResponseException) {
                if (Helpers.hasCause(
                        e,
                        OpeningHandshakeException::class.java
                    )
                ) SessionControllerAdapter.Companion.log.error(
                    "Failed opening handshake, appending to queue. Message: {}",
                    e.message
                ) else if (e is ErrorResponseException && e.cause is IOException) {
                } else if (Helpers.hasCause(
                        e,
                        UnknownHostException::class.java
                    )
                ) SessionControllerAdapter.Companion.log.error(
                    "DNS resolution failed: {}",
                    e.message
                ) else if (e.cause != null && JDA.Status.RECONNECT_QUEUED.name != e.cause!!.message) SessionControllerAdapter.Companion.log.error(
                    "Failed to establish connection for a node, appending to queue",
                    e
                ) else SessionControllerAdapter.Companion.log.error("Unexpected exception when running connect node", e)
                if (node != null) queue.add(node)
            }
        }
    }
}
