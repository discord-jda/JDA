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
package net.dv8tion.jda.api.utils.cache

import net.dv8tion.jda.api.JDA
import javax.annotation.Nonnull

/**
 * Read-only view on internal ShardManager cache of JDA instances.
 * <br></br>This can be useful to check information such as size without creating
 * an immutable snapshot first.
 *
 * @see CacheView CacheView for details on Efficient Memory Usage
 */
interface ShardCacheView : CacheView<JDA?> {
    /**
     * Retrieves the JDA instance represented by the provided shard ID.
     *
     * @param  id
     * The ID of the entity
     *
     * @return Possibly-null entity for the specified shard ID
     */
    fun getElementById(id: Int): JDA?

    /**
     * Retrieves the JDA instance represented by the provided shard ID
     * or `null` if none of the connected shards match the provided id.
     *
     * @param  id
     * The ID of the shard
     *
     * @throws java.lang.NumberFormatException
     * If the provided String is `null` or
     * cannot be resolved to an unsigned int id
     *
     * @return Possibly-null entity for the specified shard ID
     */
    fun getElementById(@Nonnull id: String?): JDA? {
        return getElementById(Integer.parseUnsignedInt(id))
    }
}
