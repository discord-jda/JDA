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

package net.dv8tion.jda.api.requests.restaction;

import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 * Requests which can use cached values instead of making a request to Discord.
 *
 * @param <T>
 *        The entity type
 */
public interface CacheRestAction<T> extends RestAction<T>
{
    @Nonnull
    @Override
    CacheRestAction<T> setCheck(@Nullable BooleanSupplier checks);

    @Nonnull
    @Override
    default CacheRestAction<T> addCheck(@Nonnull BooleanSupplier checks)
    {
        return (CacheRestAction<T>) RestAction.super.addCheck(checks);
    }

    @Nonnull
    @Override
    default CacheRestAction<T> timeout(long timeout, @Nonnull TimeUnit unit)
    {
        return (CacheRestAction<T>) RestAction.super.timeout(timeout, unit);
    }

    @Nonnull
    @Override
    default CacheRestAction<T> deadline(long timestamp)
    {
        return (CacheRestAction<T>) RestAction.super.deadline(timestamp);
    }

    /**
     * Sets whether this request should rely on cached entities, or always retrieve a new one.
     *
     * @param  useCache
     *         True if the cache should be used when available, even if the entity might be outdated.
     *         False, to always request a new instance from the API.
     *
     * @return This RestAction instance
     */
    @Nonnull
    @CheckReturnValue
    CacheRestAction<T> useCache(boolean useCache);
}
