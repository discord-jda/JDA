/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

import javax.annotation.Nullable;
import java.util.concurrent.ExecutorService;

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
}
