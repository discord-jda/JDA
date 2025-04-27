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

package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.internal.entities.SkuSnowflakeImpl;

import javax.annotation.Nonnull;

/**
 * Represents an abstract SKU reference by only the SKU ID.
 *
 * <p>This is used for methods which only need a SKU ID to function, you cannot use this for getting any properties.
 */
public interface SkuSnowflake extends ISnowflake
{
    /**
     * Creates a SKU instance which only wraps an ID.
     *
     * @param  id
     *         The SKU id
     *
     * @return A SKU snowflake instance
     */
    @Nonnull
    static SkuSnowflake fromId(long id)
    {
        return new SkuSnowflakeImpl(id);
    }

    /**
     * Creates a SKU instance which only wraps an ID.
     *
     * @param  id
     *         The SKU id
     *
     * @throws IllegalArgumentException
     *         If the provided ID is not a valid snowflake
     *
     * @return A SKU snowflake instance
     */
    @Nonnull
    static SkuSnowflake fromId(@Nonnull String id)
    {
        return fromId(MiscUtil.parseSnowflake(id));
    }
}
