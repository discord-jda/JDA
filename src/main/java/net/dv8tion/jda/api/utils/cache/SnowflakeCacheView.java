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

package net.dv8tion.jda.api.utils.cache;

import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.utils.MiscUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * {@link net.dv8tion.jda.api.utils.cache.CacheView CacheView} implementation
 * specifically to view {@link net.dv8tion.jda.api.entities.ISnowflake ISnowflake} implementations.
 *
 * @see CacheView CacheView for details on Efficient Memory Usage
 */
public interface SnowflakeCacheView<T extends ISnowflake> extends CacheView<T> {
    /**
     * Retrieves the entity represented by the provided ID.
     *
     * @param  id
     *         The ID of the entity
     *
     * @return Possibly-null entity for the specified ID
     */
    @Nullable
    T getElementById(long id);

    /**
     * Retrieves the entity represented by the provided ID.
     *
     * @param  id
     *         The ID of the entity
     *
     * @throws java.lang.NumberFormatException
     *         If the provided String is {@code null} or
     *         cannot be resolved to an unsigned long id
     *
     * @return Possibly-null entity for the specified ID
     */
    @Nullable
    default T getElementById(@Nonnull String id) {
        return getElementById(MiscUtil.parseSnowflake(id));
    }
}
