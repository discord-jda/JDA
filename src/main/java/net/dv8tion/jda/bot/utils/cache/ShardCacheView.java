/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.bot.utils.cache;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.utils.cache.CacheView;

/**
 * Read-only view on internal ShardManager cache of JDA instances.
 * <br>This can be useful to check information such as size without creating
 * an immutable snapshot first.
 */
public interface ShardCacheView extends CacheView<JDA>
{
    /**
     * Retrieves the JDA instance represented by the provided shard ID.
     *
     * @param  id
     *         The ID of the entity
     *
     * @return Possibly-null entity for the specified shard ID
     */
    JDA getElementById(int id);

    /**
     * Retrieves the JDA instance represented by the provided shard ID
     * or {@code null} if none of the connected shards match the provided id.
     *
     * @param  id
     *         The ID of the shard
     *
     * @throws java.lang.NumberFormatException
     *         If the provided String is {@code null} or
     *         cannot be resolved to an unsigned int id
     *
     * @return Possibly-null entity for the specified shard ID
     */
    default JDA getElementById(String id)
    {
        return getElementById(Integer.parseUnsignedInt(id));
    }
}
