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
import net.dv8tion.jda.api.exceptions.InvalidTokenException
import net.dv8tion.jda.api.requests.*
import net.dv8tion.jda.api.requests.RestRateLimiter.GlobalRateLimit
import net.dv8tion.jda.api.requests.RestRateLimiter.GlobalRateLimit.Companion.create
import net.dv8tion.jda.api.utils.SessionController.SessionConnectNode
import net.dv8tion.jda.api.utils.SessionController.ShardedGateway
import net.dv8tion.jda.internal.requests.RestActionImpl
import net.dv8tion.jda.internal.utils.JDALogger
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit
import javax.annotation.Nonnull

/**
 * Simple implementation of [SessionController] without supporting concurrency.
 *
 * @see ConcurrentSessionController
 */
open class SessionControllerAdapter : SessionController {
    protected val lock = Any()
    protected var connectQueue: Queue<SessionConnectNode>

    @get:Nonnull
    override var rateLimitHandle: GlobalRateLimit?
        protected set
    protected var workerHandle: Thread? = null
    protected var lastConnect: Long = 0

    init {
        connectQueue = ConcurrentLinkedQueue()
        rateLimitHandle = create()
    }

    override fun appendSession(@Nonnull node: SessionConnectNode) {
        removeSession(node)
        connectQueue.add(node)
        runWorker()
    }

    override fun removeSession(@Nonnull node: SessionConnectNode) {
        connectQueue.remove(node)
    }

    @Nonnull
    override fun getShardedGateway(@Nonnull api: JDA): ShardedGateway? {
        return object : RestActionImpl<ShardedGateway?>(api, Route.Misc.GATEWAY_BOT.compile()) {
            override fun handleResponse(response: Response, request: Request<ShardedGateway>) {
                if (response.isOk) {
                    val `object` = response.`object`
                    val url = `object`!!.getString("url")
                    val shards = `object`.getInt("shards")
                    val concurrency = `object`.getObject("session_start_limit").getInt("max_concurrency", 1)
                    request.onSuccess(ShardedGateway(url, shards, concurrency))
                } else if (response.code == 401) {
                    api.shutdownNow()
                    request.onFailure(InvalidTokenException())
                } else {
                    request.onFailure(response)
                }
            }
        }.priority().complete()
    }

    protected fun runWorker() {
        synchronized(lock) {
            if (workerHandle == null) {
                workerHandle = QueueWorker()
                workerHandle!!.start()
            }
        }
    }

    protected inner class QueueWorker(
        /** Delay (in milliseconds) to sleep between connecting sessions  */
        protected val delay: Long
    ) : Thread("SessionControllerAdapter-Worker") {
        /**
         * Creates a QueueWorker
         *
         * @param delay
         * delay (in seconds) to wait between starting sessions
         */
        @JvmOverloads
        constructor(delay: Int = SessionController.Companion.IDENTIFY_DELAY) : this(TimeUnit.SECONDS.toMillis(delay.toLong()))

        /**
         * Creates a QueueWorker
         *
         * @param delay
         * delay (in milliseconds) to wait between starting sessions
         */
        init {
            super.setUncaughtExceptionHandler { thread: Thread?, exception: Throwable? ->
                handleFailure(
                    thread,
                    exception
                )
            }
        }

        protected fun handleFailure(thread: Thread?, exception: Throwable?) {
            log.error("Worker has failed with throwable!", exception)
        }

        override fun run() {
            try {
                if (delay > 0) {
                    val interval = System.currentTimeMillis() - lastConnect
                    if (interval < delay) sleep(delay - interval)
                }
            } catch (ex: InterruptedException) {
                log.error("Unable to backoff", ex)
            }
            processQueue()
            synchronized(lock) {
                workerHandle = null
                if (!connectQueue.isEmpty()) runWorker()
            }
        }

        protected fun processQueue() {
            var isMultiple = connectQueue.size > 1
            while (!connectQueue.isEmpty()) {
                val node = connectQueue.poll()
                try {
                    node.run(isMultiple && connectQueue.isEmpty())
                    isMultiple = true
                    lastConnect = System.currentTimeMillis()
                    if (connectQueue.isEmpty()) break
                    if (delay > 0) sleep(delay)
                } catch (e: IllegalStateException) {
                    val t = e.cause
                    if (t is OpeningHandshakeException) log.error(
                        "Failed opening handshake, appending to queue. Message: {}",
                        e.message
                    ) else if (t != null && JDA.Status.RECONNECT_QUEUED.name != t.message) log.error(
                        "Failed to establish connection for a node, appending to queue",
                        e
                    ) else log.error("Unexpected exception when running connect node", e)
                    appendSession(node)
                } catch (e: InterruptedException) {
                    log.error("Failed to run node", e)
                    appendSession(node)
                    return  // caller should start a new thread
                }
            }
        }
    }

    companion object {
        val log = JDALogger.getLog(SessionControllerAdapter::class.java)
    }
}
