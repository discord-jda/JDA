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

import java.util.Set;

import javax.annotation.Nonnull;

/**
 * SKUs (stock-keeping units) in Discord represent premium offerings that can be made available to application users or guilds.
 */
public interface SKU extends SkuSnowflake {
    /**
     * Type of the SKU
     *
     * @return SKU type
     */
    @Nonnull
    SKUType getType();

    /**
     * Customer-facing name of your premium offering
     *
     * @return SKU name
     */
    @Nonnull
    String getName();

    /**
     * System-generated URL slug based on the SKU's name
     *
     * @return SKU slug
     */
    @Nonnull
    String getSlug();

    /**
     * Flags can be used to differentiate user and server subscriptions
     *
     * @return set of flags.
     */
    @Nonnull
    Set<SKUFlag> getFlags();

    /**
     * Checks whether the SKU is available to be purchased for a guild.
     *
     * @return true if it's for guilds
     */
    default boolean isGuildSKU() {
        return getFlags().contains(SKUFlag.GUILD_SUBSCRIPTION);
    }

    /**
     * Checks whether the SKU is available to be purchased for a user.
     *
     * @return true if it's for users
     */
    default boolean isUserSKU() {
        return getFlags().contains(SKUFlag.USER_SUBSCRIPTION);
    }

    /**
     * Checks whether this SKU is available.
     *
     * @return true if available
     */
    default boolean isAvailable() {
        return getFlags().contains(SKUFlag.AVAILABLE);
    }
}
