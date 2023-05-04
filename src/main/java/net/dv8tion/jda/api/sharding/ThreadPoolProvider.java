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

package net.dv8tion.jda.api.sharding;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ExecutorService;
import java.util.function.IntFunction;

/**
 * Called by {@link DefaultShardManager} when building a JDA instance.
 * <br>Every time a JDA instance is built, the manager will first call {@link #provide(int)} followed by
 * a call to {@link #shouldShutdownAutomatically(int)}.
 *
 * @param <T>
 *        The type of executor
 */
public interface ThreadPoolProvider<T extends ExecutorService>
{
    /**
     * Provides an instance of the specified executor, or null
     *
     * @param  shardId
     *         The current shard id
     *
     * @return The Executor Service
     */
    @Nullable
    T provide(int shardId);

    /**
     * Whether the previously provided executor should be shutdown by {@link net.dv8tion.jda.api.JDA#shutdown()}.
     *
     * @param  shardId
     *         The current shard id
     *
     * @return True, if the executor should be shutdown by JDA
     */
    default boolean shouldShutdownAutomatically(int shardId)
    {
        return false;
    }

    @Nonnull
    static <T extends ExecutorService> LazySharedProvider<T> lazy(@Nonnull IntFunction<T> init)
    {
        return new LazySharedProvider<>(init);
    }

    final class LazySharedProvider<T extends ExecutorService> implements ThreadPoolProvider<T>
    {
        private final IntFunction<T> initializer;
        private volatile T temporaryPool;
        private volatile T pool;

        public LazySharedProvider(IntFunction<T> initializer)
        {
            this.initializer = initializer;
        }

        public synchronized void init(int shardTotal)
        {
            if (pool == null)
                pool = initializer.apply(shardTotal);

            if (temporaryPool != null)
            {
                temporaryPool.shutdownNow();
                temporaryPool = null;
            }
        }

        @Nullable
        @Override
        public synchronized T provide(int shardId)
        {
            if (pool == null)
            {
                if (temporaryPool == null)
                    temporaryPool = initializer.apply(1);
                return temporaryPool;
            }

            return pool;
        }
    }
}
