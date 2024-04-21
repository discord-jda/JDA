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

import net.dv8tion.jda.annotations.ForRemoval
import net.dv8tion.jda.annotations.ReplaceWith
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDA.ShardInfo
import net.dv8tion.jda.api.requests.RestRateLimiter.GlobalRateLimit
import javax.annotation.Nonnull

/**
 * Controls states and behaviour of one or multiple [JDA][net.dv8tion.jda.api.JDA] instances.
 * <br></br>One instance of this should be used when sharding a bot account in order to keep track of session information
 * between shards.
 *
 *
 * The [SessionControllerAdapter][net.dv8tion.jda.api.utils.SessionControllerAdapter] provides
 * a default implementation that can be extended and overridden.
 *
 *
 * **States &amp; Behaviour**<br></br>
 * **Identify Ratelimit Handling**
 * <br></br>This will enable handling of (re-)connecting gateway sessions.
 *
 *
 * **Global REST Ratelimit**
 * <br></br>The global REST ratelimit is not bound to a single session and should be
 * handled on all JDA instances. This controller will receive updates of this ratelimit through [.setGlobalRatelimit]
 * and should report the last ratelimit information it received through [.getGlobalRatelimit].
 *
 *
 * **Gateway Provider**
 * <br></br>This provider can be used to change the gateway retrieval (using cache, http, or static) and
 * allows to set a custom gateway endpoint. **Use carefully.**
 *
 *
 * **Examples**<br></br>
 *
 * **Using [JDABuilder][net.dv8tion.jda.api.JDABuilder]**
 * <br></br>
 * <pre>`
 * JDABuilder builder = JDABuilder.createDefault(BOT_TOKEN);
 * builder.setSessionController(new SessionControllerAdapter() {
 * @Override
 * public void appendSession(SessionConnectNode node) {
 * System.out.println("[SessionController] Adding SessionConnectNode to Queue!");
 * super.appendSession(node);
 * }
 * });
 * builder.addEventListeners(myListener);
 * for (int i = 0; i < 10; i++) {
 * builder.useSharding(i, 10).build();
 * }
`</pre> *
 *
 *
 * **Using [ShardManager][net.dv8tion.jda.api.sharding.ShardManager]**
 * <br></br>
 * <pre>`
 * DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(BOT_TOKEN);
 * builder.setSessionController(new SessionControllerAdapter() {
 * @Override
 * public Pair<String, Integer> getGatewayBot(JDA api) {
 * return Pair.of(getGateway(), 10);
 * }
 * });
 * builder.addEventListeners(myListener);
 * builder.build();
`</pre> *
 */
interface SessionController {
    /**
     * Apply the `max_concurrency` for this bot. This property is only useful for very large bots
     * which get access to higher concurrency when starting their shards.
     *
     *
     * Currently, there are 3 different levels of concurrency 1, 16, and 64.
     * The concurrency means the bot can connect multiple shards at once without hitting the IDENTIFY rate-limit.
     * This works by applying the concurrency level as a modulo operand to the shard id: `shard_id % concurrency`.
     * We use one thread per bucket in this implementation.
     *
     *
     * An implementation of this interface is not required to use this concurrency level.
     * [SessionControllerAdapter] does not support this due to backwards compatibility.
     *
     * @param  level
     * The concurrency level
     *
     * @throws AssertionError
     * If the provided level is not a valid array length size
     *
     * @since  4.2.0
     */
    fun setConcurrency(level: Int) {}

    /**
     * Called by a JDA session when a WebSocket should be started. (Connecting and Reconnecting)
     * <br></br>This should only add the node to a queue and execute the queue with respect to the [.IDENTIFY_DELAY].
     *
     * @param  node
     * The [SessionConnectNode][net.dv8tion.jda.api.utils.SessionController.SessionConnectNode]
     */
    fun appendSession(@Nonnull node: SessionConnectNode)

    /**
     * Called by a JDA session when a shutdown has been requested.
     * <br></br>When this happened the [SessionConnectNode.run(boolean)][net.dv8tion.jda.api.utils.SessionController.SessionConnectNode.run]
     * will be a no-op and does not contribute to the [.IDENTIFY_DELAY].
     *
     * @param node
     * The [SessionConnectNode][net.dv8tion.jda.api.utils.SessionController.SessionConnectNode] to remove from the queue.
     */
    fun removeSession(@Nonnull node: SessionConnectNode)

    @get:Deprecated("Use {@link #getRateLimitHandle()} instead")
    @get:ReplaceWith("getRateLimitHandle().getClassic()")
    @get:ForRemoval(deadline = "5.0.0")
    @set:Deprecated("Use {@link #getRateLimitHandle()} instead")
    @set:ReplaceWith("getRateLimitHandle().getClassic()")
    @set:ForRemoval(deadline = "5.0.0")
    var globalRatelimit: Long
        /**
         * Provides the cross-session global REST ratelimit it received through [.setGlobalRatelimit].
         *
         * @return The current global REST ratelimit or -1 if unset
         *
         */
        get() = -1
        /**
         * Called by the RateLimiter if the global rest ratelimit has changed.
         *
         * @param ratelimit
         * The new global ratelimit
         *
         */
        set(ratelimit) {}

    @get:Nonnull
    val rateLimitHandle: GlobalRateLimit?
        /**
         * The store for global rate-limits of all types.
         * <br></br>This can be used to share the global rate-limit information between shards on the same IP.
         *
         * @return The global rate-limiter
         */
        get() = GlobalRateLimitAdapter(this)

    @get:Nonnull
    val gateway: String?
        /**
         * Discord's gateway URL, which is used to receive events.
         *
         *
         * Called by JDA when starting a new gateway session (Connecting, Reconnecting).
         *
         * @return The gateway endpoint
         */
        get() = "wss://gateway.discord.gg/"

    /**
     * Called by [DefaultShardManager][net.dv8tion.jda.api.sharding.DefaultShardManager]
     * when a new shards is starting.
     * <br></br>Should provide a [ShardedGateway] with `(gateway, shardTotal)`.
     *
     * @param  api
     * The current JDA instance (used for RestActions and ShardInfo)
     *
     * @return The ShardedGateway instance consisting of the gateway endpoint to connect to and the shardTotal
     *
     * @see .getGateway
     */
    @Nonnull
    fun getShardedGateway(@Nonnull api: JDA): ShardedGateway?

    /**
     * POJO containing the gateway endpoint and recommended shard total for a shard manager.
     */
    class ShardedGateway
    /**
     * Creates a new GatewayBot instance with the provided properties
     *
     * @param url
     * The gateway endpoint (wss)
     * @param shardTotal
     * The recommended shard total
     */ @JvmOverloads constructor(
        /**
         * The gateway endpoint
         *
         * @return The endpoint
         */
        val url: String,
        /**
         * The recommended shard total
         *
         * @return The shard total
         */
        val shardTotal: Int,
        /**
         * The concurrency level for this bot.
         * <br></br>This should not be a custom value as discord determines the eligible concurrency.
         * Using a different concurrency value could result in issues and possibly a ban due to login spam.
         *
         * @return The concurrency level
         *
         * @see .setConcurrency
         */
        val concurrency: Int = 1
    )

    /**
     * Represents a WebSocketClient request to start a session.
     * <br></br>Not implemented by library user.
     *
     *
     * **Note: None of the provided session nodes can be resumed, the resume timeframe has already passed**
     */
    interface SessionConnectNode {
        /**
         * Whether this node is reconnecting. Can be used to setup a priority based system.
         *
         * @return True, if this session is reconnecting
         */
        val isReconnect: Boolean

        @get:Nonnull
        val jDA: JDA?

        @get:Nonnull
        val shardInfo: ShardInfo

        /**
         * When called, establishes the session.
         * <br></br>This will return once the required payload to start the session has been delivered.
         *
         * @param  isLast
         * True, if this is the last node in a queue worker.
         * When true this will not wait for the payload to be delivered.
         *
         * @throws InterruptedException
         * If the calling thread is interrupted
         */
        @Throws(InterruptedException::class)
        fun run(isLast: Boolean)
    }

    /**
     * Wrapper for [.getGlobalRatelimit] and [.setGlobalRatelimit].
     */
    @Deprecated("")
    @ForRemoval(deadline = "5.0.0")
    @ReplaceWith("getRateLimitHandle()")
    class GlobalRateLimitAdapter(@Nonnull controller: SessionController) : GlobalRateLimit {
        private val controller: SessionController

        init {
            SessionControllerAdapter.Companion.log.warn("Using outdated implementation of global rate-limit handling. It is recommended to use GlobalRateLimit interface instead!")
            this.controller = controller
        }

        override var classic: Long
            /**
             * Forwarding to [SessionController.getGlobalRatelimit]
             *
             * @return The current global ratelimit
             */
            get() = controller.globalRatelimit
            /**
             * Forwarding to [SessionController.setGlobalRatelimit]
             *
             * @param ratelimit
             * The new global ratelimit
             */
            set(ratelimit) {
                controller.globalRatelimit = ratelimit
            }
        override var cloudflare: Long
            /**
             * Forwarding to [SessionController.getGlobalRatelimit]
             *
             * @return The current global ratelimit
             */
            get() = classic
            /**
             * Forwarding to [SessionController.setGlobalRatelimit]
             *
             * @param timestamp
             * The new global ratelimit
             */
            set(timestamp) {
                classic = timestamp
            }
    }

    companion object {
        /**
         * The default delay (in seconds) to wait between running [SessionConnectNodes][net.dv8tion.jda.api.utils.SessionController.SessionConnectNode]
         */
        const val IDENTIFY_DELAY = 5
    }
}
