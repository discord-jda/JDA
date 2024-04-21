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
package net.dv8tion.jda.api.requests.restaction

import net.dv8tion.jda.api.requests.RestAction
import java.util.concurrent.TimeUnit
import java.util.function.BooleanSupplier
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Requests which can use cached values instead of making a request to Discord.
 *
 * @param <T>
 * The entity type
</T> */
interface CacheRestAction<T> : RestAction<T?> {
    @Nonnull
    override fun setCheck(checks: BooleanSupplier?): CacheRestAction<T?>?
    @Nonnull
    override fun addCheck(@Nonnull checks: BooleanSupplier): CacheRestAction<T?>? {
        return super.addCheck(checks) as CacheRestAction<T?>
    }

    @Nonnull
    override fun timeout(timeout: Long, @Nonnull unit: TimeUnit): CacheRestAction<T?>? {
        return super.timeout(timeout, unit) as CacheRestAction<T?>
    }

    @Nonnull
    override fun deadline(timestamp: Long): CacheRestAction<T?>? {
        return super.deadline(timestamp) as CacheRestAction<T?>
    }

    /**
     * Sets whether this request should rely on cached entities, or always retrieve a new one.
     *
     * @param  useCache
     * True if the cache should be used when available, even if the entity might be outdated.
     * False, to always request a new instance from the API.
     *
     * @return This RestAction instance
     */
    @Nonnull
    @CheckReturnValue
    fun useCache(useCache: Boolean): CacheRestAction<T>?
}
