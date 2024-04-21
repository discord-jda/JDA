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
package net.dv8tion.jda.api.sharding

import net.dv8tion.jda.internal.utils.Checks
import java.util.concurrent.ExecutorService
import java.util.function.IntFunction
import javax.annotation.Nonnull

/**
 * Called by [DefaultShardManager] when building a JDA instance.
 * <br></br>Every time a JDA instance is built, the manager will first call [.provide] followed by
 * a call to [.shouldShutdownAutomatically].
 *
 * @param <T>
 * The type of executor
</T> */
interface ThreadPoolProvider<T : ExecutorService?> {
    /**
     * Provides an instance of the specified executor, or null
     *
     * @param  shardId
     * The current shard id
     *
     * @return The Executor Service
     */
    fun provide(shardId: Int): T?

    /**
     * Whether the previously provided executor should be shutdown by [net.dv8tion.jda.api.JDA.shutdown].
     *
     * @param  shardId
     * The current shard id
     *
     * @return True, if the executor should be shutdown by JDA
     */
    fun shouldShutdownAutomatically(shardId: Int): Boolean {
        return false
    }

    class LazySharedProvider<T : ExecutorService?> internal constructor(@param:Nonnull private val initializer: IntFunction<T>) :
        ThreadPoolProvider<T?> {
        private var pool: T? = null

        /**
         * Called with the shard total to initialize the shared pool.
         *
         *
         * This also destroys the temporary pool created for fetching the recommended shard total.
         *
         * @param shardTotal
         * The shard total
         */
        @Synchronized
        fun init(shardTotal: Int) {
            if (pool == null) pool = initializer.apply(shardTotal)
        }

        /**
         * Shuts down the shared pool and the temporary pool.
         */
        @Synchronized
        fun shutdown() {
            if (pool != null) {
                pool!!.shutdown()
                pool = null
            }
        }

        /**
         * Provides the initialized pool or the temporary pool if not initialized yet.
         *
         * @param  shardId
         * The current shard id
         *
         * @return The thread pool instance
         */
        @Synchronized
        override fun provide(shardId: Int): T? {
            return pool
        }
    }

    companion object {
        /**
         * Provider that initializes with a [shard_total][DefaultShardManagerBuilder.setShardsTotal]
         * and provides the same pool to share between shards.
         *
         * @param  init
         * Function to initialize the shared pool, called with the shard total
         *
         * @param  <T>
         * The type of executor
         *
         * @return The lazy pool provider
        </T> */
        @Nonnull
        fun <T : ExecutorService?> lazy(@Nonnull init: IntFunction<T>): LazySharedProvider<T>? {
            Checks.notNull(init, "Initializer")
            return LazySharedProvider(init)
        }
    }
}
